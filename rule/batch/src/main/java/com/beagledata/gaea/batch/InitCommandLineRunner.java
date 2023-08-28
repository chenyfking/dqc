package com.beagledata.gaea.batch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.batch.configs.Configs;
import com.beagledata.gaea.batch.configs.ExitCode;
import com.beagledata.gaea.batch.queue.Dispatcher;
import com.beagledata.gaea.batch.queue.EventHandler;
import com.beagledata.gaea.common.OkHttpClientFactory;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.executioncore.MicroExecutor;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.RuleContext;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.util.StringUtils;
import okhttp3.*;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulu on 2020/5/20.
 */
@Component
public class InitCommandLineRunner implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(InitCommandLineRunner.class);

	@Autowired
	private EventHandler eventHandler;
	@Autowired
	private ApplicationContext ctx;
	@Autowired
	private Configs configs;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private MicroExecutor microExecutor;
	@Autowired
	private ShutDownManager shutDownManager;

	@Override
	public void run(String... args) throws Exception {
		SpringBeanHolder.initApplicationContext(ctx);
		// 服务启动把数据源注入到规则上下文
		RuleContext.setDataSource(dataSource);
		System.setProperty("gaea.tmpdir", SafelyFiles.newFile(configs.getDataHome(), "tmp").getAbsolutePath());

		CommandLine line = parseCommand(args);
		if (!checkCommand(line)) {
			return;
		}

		String microUuid = line.getOptionValue("s");
		String inputTableName = line.getOptionValue("i");
		String outputTableName = line.getOptionValue("o");
		String bizDate = line.getOptionValue("d");
		configs.setMicroUuid(microUuid);
		configs.setOutputTableName(outputTableName);
		configs.setBizDate(bizDate);
		logger.info("\n跑批参数:\n服务uuid: {}\n输入表名称: {}\n输出表名称: {}\n业务日期: {}",
				microUuid,
				inputTableName,
				outputTableName,
				bizDate
		);
		try {
			//从workbench获取远程访问zip包
			byte[] microZipBytes = getRemoteMicroZip(microUuid);
			loadRules(new ByteArrayInputStream(microZipBytes), microUuid);

			Dispatcher dispatcher = new Dispatcher(eventHandler, inputTableName, outputTableName,
					configs.getProcessingThreadNum(), configs.getMaxEntriesClaimed(), configs.getClaimTime(), configs.getMaxFailureRetries(), configs.getProcessingTimeout());
			dispatcher.start();
		} catch (Exception e) {
			logger.error("跑批失败", e);
			shutDownManager.shutdown(ExitCode.FAILED);
		}
	}


	private CommandLine parseCommand(String... args) throws IOException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Option.builder("s").longOpt("service").hasArg().desc("服务uuid").required().build());
		options.addOption(Option.builder("i").longOpt("input").hasArg().desc("输入表名称").required().build());
		options.addOption(Option.builder("o").longOpt("output").hasArg().desc("输出表名称").required().build());
		options.addOption(Option.builder("d").longOpt("date").hasArg().desc("业务日期").required().build());
		try {
			return parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				formatter.printHelp(pw, formatter.getWidth(), "sh startup.sh",
						null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null);
				pw.flush();
				logger.info(sw.toString());
				shutDownManager.shutdown(ExitCode.FAILED);
				return null;
			}
		}
	}

	private boolean checkCommand(CommandLine line) {
		if (line == null) {
			logger.error("参数读取失败");
			shutDownManager.shutdown(ExitCode.FAILED);
			return false;
		}

		String microUuid = line.getOptionValue("s");
		if (StringUtils.isBlank(microUuid)) {
			logger.error("服务不能为空");
			shutDownManager.shutdown(ExitCode.Command.MISSING_MICRO);
			return false;
		}
		String inputTableName = line.getOptionValue("i");
		if (StringUtils.isBlank(inputTableName)) {
			logger.error("输入表不能为空");
			shutDownManager.shutdown(ExitCode.Command.MISSING_INPUT_TABLE);
			return false;
		}
		if (!inputTableName.matches("[_a-zA-Z0-9]")) {
			logger.error("输入表不合法： {}", inputTableName);
			shutDownManager.shutdown(ExitCode.Command.INVALID_INPUT_TABLE);
			return false;
		}
		String outputTableName = line.getOptionValue("o");
		if (StringUtils.isBlank(outputTableName)) {
			logger.error("输出表不能为空");
			shutDownManager.shutdown(ExitCode.Command.MISSING_OUTPUT_TABLE);
			return false;
		}
		if (!outputTableName.matches("[_a-zA-Z0-9]")) {
			logger.error("输出表不合法： {}", outputTableName);
			shutDownManager.shutdown(ExitCode.Command.INVALID_OUTPUT_TABLE);
			return false;
		}
		String bizDate = line.getOptionValue("d");
		if (bizDate != null) {
			try {
				DateUtils.parseDateStrictly(bizDate, "yyyyMMdd");
			} catch (java.text.ParseException e) {
				logger.error("业务日期不合法： {}", bizDate);
				shutDownManager.shutdown(ExitCode.Command.INVALID_BIZ_DATE);
				return false;
			}
		}
		return true;
	}

	private byte[] getRemoteMicroZip(String microUuid) throws IOException {
		String remoteUrl = configs.getWorkbench().getBaseUrl() + RestConstants.Workbench.Endpoints.GET_MICRO_PACKAGE;
		OkHttpClient httpClient = OkHttpClientFactory.get();
		RequestBody formBody = new FormBody.Builder().
				add("microUuid", microUuid).
				build();
		Request request = new Request.Builder().
				url(remoteUrl).
				post(formBody).
				build();
		logger.info("开始获取远程可执行服务zip包. url: {}", remoteUrl);
		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				logger.error("获取远程可执行服务zip包失败. 接口报错: {}", response);
				throw new IllegalStateException("获取远程可执行服务zip包失败");
			}

			ResponseBody body = response.body();
			if ("json".equals(body.contentType().subtype())) {
				logger.error("获取远程可执行服务zip包失败. 接口报错: {}", body.string());
				throw new IllegalStateException("获取远程可执行服务zip包失败");
			}
			logger.info("获取远程可执行服务zip包成功");
			return body.bytes();
		} catch (IOException e) {
			logger.error("获取远程可执行服务zip包失败", e);
			throw e;
		}
	}

	private void loadRules(ByteArrayInputStream ins, String microUuid) {
		try {
			microExecutor.load(ins);
		} catch (IllegalArgumentException | RuleException e) {
			throw e;
		} catch (Exception e) {
			logger.error("服务加载失败. microUuid: {}", microUuid, e);
			throw new IllegalStateException("服务加载失败");
		}
	}
}
