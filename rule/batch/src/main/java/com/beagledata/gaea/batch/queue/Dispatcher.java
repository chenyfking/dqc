package com.beagledata.gaea.batch.queue;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.batch.ShutDownManager;
import com.beagledata.gaea.batch.configs.ExitCode;
import com.beagledata.gaea.batch.mapper.DispatcherMapper;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-04
 */
public class Dispatcher {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 默认最大处理线程数
	 */
	private static final int MAX_PROCESSING_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;
	/**
	 * 默认最大拉取记录数
	 */
	private static final int DEFAULT_MAX_ENTRIES_CLAIMED = 50;
	/**
	 * 默认拉取时间间隔
	 */
	private static final int DEFAULT_CLAIM_TIME = 1;
	/**
	 * 默认每条记录最大重试次数
	 */
	private static final int DEFAULT_MAX_FAILURE_RETRIES = 3;
	/**
	 * 默认每条记录处理超时时间
	 */
	private static final int DEFAULT_PROCESSING_TIMEOUT = 5 * 60;

	private static final String COLUMN_ID = "gaea_id";
	private static final String COLUMN_PROCESSING_OWNER = "gaea_processing_owner";
	private static final String COLUMN_PROCESSING_TIME = "gaea_processing_time";
	private static final String COLUMN_PROCESSING_STATE = "gaea_processing_state";
	private static final String COLUMN_ERROR_COUNT = "gaea_error_count";

	private DispatcherMapper dispatcherMapper = SpringBeanHolder.getBean(DispatcherMapper.class);
	private DataSourceTransactionManager transactionManager = SpringBeanHolder.getAvailableBean(DataSourceTransactionManager.class);
	private Timer timer = new Timer();
	private CountDownLatch latch;

	/**
	 * 开始时间
	 */
	private long startTime;
	/**
	 * 记录处理器
	 */
	private EventHandler eventHandler;
	/**
	 * 当前节点标识
	 */
	private String identifier;
	/**
	 * 输入表名称
	 */
	private String inputTableName;
	/**
	 * 输出表名称
	 */
	private String outputTableName;
	/**
	 * 缓冲队列
	 */
	private ArrayBlockingQueue<EventModel> queue;
	/**
	 * 缓冲队列容量
	 */
	private int queueCapacity;
	/**
	 * 处理线程数
	 */
	private int processingThreadNum = MAX_PROCESSING_THREAD_NUM;
	/**
	 * 最大拉取记录数
	 */
	private int maxEntriesClaimed = DEFAULT_MAX_ENTRIES_CLAIMED;
	/**
	 * 拉取时间间隔，单位秒
	 */
	private int claimTime = DEFAULT_CLAIM_TIME;
	/**
	 * 默认每条记录最大重试次数
	 */
	private int maxFailureRetries = DEFAULT_MAX_FAILURE_RETRIES;
	/**
	 * 每条记录处理超时时间
	 */
	private int processingTimeout = DEFAULT_PROCESSING_TIMEOUT;
	/**
	 * 可处理记录总数
	 */
	private int totalCount;
	/**
	 * 拉取记录数
	 */
	private AtomicInteger claimCount = new AtomicInteger();
	/**
	 * 成功记录数
	 */
	private AtomicInteger successCount = new AtomicInteger();
	/**
	 * 错误次数
	 */
	private AtomicInteger errorCount = new AtomicInteger();
	/**
	 * 失败记录数
	 */
	private AtomicInteger failedCount = new AtomicInteger();
	/**
	 * 处理进度
	 */
	private int progress;

	public Dispatcher(EventHandler eventHandler, String inputTableName, String outputTableName) {
		this.eventHandler = eventHandler;
		this.inputTableName = inputTableName;
		this.outputTableName = outputTableName;
		this.identifier = getIdentifier();
	}

	public Dispatcher(EventHandler eventHandler, String inputTableName, String outputTableName,
					  int processingThreadNum, int maxEntriesClaimed, int claimTime, int maxFailureRetries, int processingTimeout) {
		this(eventHandler, inputTableName, outputTableName);
		this.processingThreadNum = calcProcessingThreadNum(processingThreadNum);
		this.maxEntriesClaimed = maxEntriesClaimed;
		this.claimTime = claimTime;
		this.maxFailureRetries = maxFailureRetries;
		this.processingTimeout = processingTimeout;
	}

	public void start() {
		this.startTime = System.currentTimeMillis();
		init();
		printConf();
	}

	public void stop() {
		this.timer.cancel();
		for (int i = 0; i < processingThreadNum; i++) {
			try {
				queue.put(EventModel.EOF);
			} catch (InterruptedException e) {
				logger.error("添加EOF记录失败", e);
			}
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("等待所有执行线程停止失败", e);
		}

		printStat();
		SpringBeanHolder.getBean(ShutDownManager.class).shutdown();
	}

	private void init() {
		initTable();
		initQueue();
		initDispatchTask();
		initProcessingThread();
	}

	private void initTable() {
		if (dispatcherMapper.countInputTableForExists(inputTableName) <= 0) {
			logger.error("输入表不存在：{}", inputTableName);
			SpringBeanHolder.getBean(ShutDownManager.class).shutdown(ExitCode.INPUT_TABLE_NOT_EXISTA);
			return;
		}

		logger.info("初始化输入表：{}", inputTableName);
		List<String> columns = dispatcherMapper.selectInputTableColumns(inputTableName);
		logger.info("输入表字段：{}", columns);
		if (!columns.contains(COLUMN_ID)) {
			try {
				dispatcherMapper.updateInputTableColumns(inputTableName);
			} catch (BadSqlGrammarException e) {
				if (!e.getMessage().contains("Duplicate column name")) {
					logger.error("初始化输入表失败. inputTableName：{}", inputTableName, e);
					throw e;
				}
			} catch (Exception e) {
				logger.error("初始化输入表失败. inputTableName：{}", inputTableName, e);
				throw e;
			}
		}
		logger.info("初始化输出表：{}", outputTableName);
		try {
			dispatcherMapper.createOutputTable(outputTableName);
		} catch (Exception e) {
			logger.error("初始化输出表失败. outputTableName：{}", outputTableName, e);
			throw e;
		}

		totalCount = dispatcherMapper.countInputTableEntries(inputTableName, getTimeout());
	}

	private void initQueue() {
		queueCapacity = calcQueueCapacity(maxEntriesClaimed);
		queue = new ArrayBlockingQueue<>(queueCapacity);
	}

	private void initDispatchTask() {
		timer.schedule(new DispatchTask(), 0, claimTime * 1000);
	}

	private void initProcessingThread() {
		latch = new CountDownLatch(processingThreadNum);
		for (int i = 0; i < processingThreadNum; i++) {
			new Thread(new ProcessingThread()).start();
		}
	}

	private void printConf() {
		logger.info("\n配置详情:\n记录处理器: {}\n当前节点标识: {}\n输入表名称: {}\n输出表名称: {}\n缓冲队列容量: {}\n处理线程数: {}\n最大拉取记录数: {}\n拉取时间间隔: {}秒\n每条记录最大重试次数: {}\n每条记录处理超时时间: {}秒",
				this.eventHandler,
				this.identifier,
				this.inputTableName,
				this.outputTableName,
				this.queueCapacity,
				this.processingThreadNum,
				this.maxEntriesClaimed,
				this.claimTime,
				this.maxFailureRetries,
				this.processingTimeout
		);
	}

	private int calcProcessingThreadNum(int processingThreadNum) {
		if (processingThreadNum <= 0) {
			return MAX_PROCESSING_THREAD_NUM;
		}
		return Math.min(processingThreadNum, MAX_PROCESSING_THREAD_NUM);
	}

	private int calcQueueCapacity(int maxEntriesClaimed) {
		return maxEntriesClaimed * 2;
	}

	/**
	 * 描述: 获取节点唯一标识
	 *
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/11/4
	 * @return: java.lang.String
	 */
	private String getIdentifier() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	/**
	 * 描述: 开启事务
	 *
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/11/4
	 * @return: org.springframework.transaction.TransactionStatus
	 */
	private TransactionStatus startTransaction() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		return transactionManager.getTransaction(def);
	}

	private String getTimeout() {
		return DateFormatUtils.format(new Date(System.currentTimeMillis() - processingTimeout * 1000), "yyyyMMddHHmmss");
	}

	private void printProgress() {
		if (totalCount < 0) {
		    return;
		}

		int successNum = successCount.get();
		int failedNum = failedCount.get();
		int processedNum = successNum + failedNum;
		int progress = Double.valueOf(((double)processedNum/(double)totalCount) * 100).intValue();
		if (progress != this.progress) {
		    logger.info("\n进度统计: {}/{} ({}%)", processedNum, totalCount, progress);
		    this.progress = progress;
		}
	}

	private void printStat() {
		logger.info("\n停止跑批，数据统计: \n总数: {}\n处理: {}\n成功: {}\n失败: {}\n时间: {}ms", totalCount, claimCount.get(), successCount.get(), failedCount.get(), System.currentTimeMillis() - startTime);
	}

	class DispatchTask extends TimerTask {

		@Override
		public void run() {
			int claimNum = Math.min(queueCapacity - queue.size(), maxEntriesClaimed);
			if (claimNum <= 0) {
				//队列已满暂停分发
			    return;
			}

			String processingTime = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			//先标识当前节点占用
			claimEntries(processingTime, claimNum);
			//获取已占用的记录
			List<EventModel> list = getReadyEntries(processingTime, claimNum);
			//往队列添加新的记录
			putEntries(list);
		}

		private void claimEntries(String processingTime, int claimNum) {
			TransactionStatus status = startTransaction();
			try {
				dispatcherMapper.updateClaimEntries(inputTableName, identifier, processingTime, getTimeout(), claimNum);
				transactionManager.commit(status);
			} catch (Exception e) {
				logger.error("标记占用记录失败.", e);
				transactionManager.rollback(status);
			}
		}

		private List<EventModel> getReadyEntries(String processingTime, int claimNum) {
			try {
				List<Map<String, Object>> results = dispatcherMapper.selectReadyEntries(inputTableName, identifier, processingTime, claimNum);
				List<EventModel> events = new ArrayList<>(results.size());
				results.forEach(map -> {
					EventModel event = new EventModel();
					map.forEach((k, v) -> {
						if (COLUMN_ID.equals(k)) {
						    event.setId((Long) v);
						} else if (COLUMN_PROCESSING_OWNER.equals(k)) {
						    event.setProcessingOwner((String) v);
						} else if (COLUMN_PROCESSING_TIME.equals(k)) {
						    event.setProcessingTime((String) v);
						} else if (COLUMN_PROCESSING_STATE.equals(k)) {
							event.setProcessingState((String) v);
						} else if (COLUMN_ERROR_COUNT.equals(k)) {
						    event.setErrorCount((Integer) v);
						} else {
							event.getInput().put(k, v);
						}
					});
					events.add(event);
				});
				return events;
			} catch (Exception e) {
				logger.error("获取占用记录失败.", e);
				return Collections.emptyList();
			}
		}

		private void putEntries(List<EventModel> list) {
			printProgress();
			if (list.isEmpty()) {
			    logger.info("没有新的可处理记录");
			    stop();
			    return;
			}

			for (EventModel model : list) {
				try {
					queue.put(model);
					claimCount.incrementAndGet();
				} catch (Exception e) {
					logger.error("向队列添加新的记录失败.", e);
				}
			}
		}
	}
	class ProcessingThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				EventModel event = takeOne();
				if (EventModel.EOF.equals(event)) {
				    break;
				}

				try {
					eventHandler.onEvent(event.getInput());
					onSuccess(event);
				} catch (Exception e) {
					logger.error("处理记录失败. event: {}", event, e);
					if (event.getErrorCount() >= maxFailureRetries) {
					    onFailed(event);
					} else {
						onError(event);
					}
				}
			}
			latch.countDown();
		}

		private EventModel takeOne() {
			try {
				return queue.take();
			} catch (Exception e) {
				logger.error("从队列获取新的记录失败.", e);
				return EventModel.EOF;
			}
		}

		private void onSuccess(EventModel event) {
			dispatcherMapper.updateOnSuccess(inputTableName, event.getId());
			successCount.incrementAndGet();
		}

		private void onError(EventModel event) {
			dispatcherMapper.updateOnError(inputTableName, event.getId());
			errorCount.incrementAndGet();
		}

		private void onFailed(EventModel event) {
			dispatcherMapper.updateOnFailed(inputTableName, event.getId());
			failedCount.incrementAndGet();
		}
	}
}