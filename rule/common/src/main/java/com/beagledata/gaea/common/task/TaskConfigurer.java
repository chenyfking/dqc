package com.beagledata.gaea.common.task;

import com.beagledata.gaea.common.BaseConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 任务配置
 *
 * @author 周庚新
 * @date 2020-10-14
 */
public class TaskConfigurer implements SchedulingConfigurer {
	private Logger loggger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private BaseConfigs baseConfigs;

	private List<Task> tasks = new ArrayList<>();

	@PostConstruct
	public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (BaseConfigs.Task taskConf : baseConfigs.getTasks()) {
			Class clazz = Class.forName(taskConf.getClassName());
			Object task = clazz.newInstance();
			while (clazz != null && AbstractTask.class.equals(clazz)) {
				clazz = clazz.getSuperclass();
			}
			if (clazz == null) {
			    throw new IllegalArgumentException("任务配置不正确，类未找到");
			}

			Field field = clazz.getDeclaredField("cron");
			field.setAccessible(true);
			field.set(task, taskConf.getCron());
			field.setAccessible(false);
			add((Task) task);
		}
	}

	public void add(Task task) {
		tasks.add(task);
		loggger.info("添加任务. desc: {}, cron: {}, class: {}", task.desc(), task.cron(), task.getClass().getName());
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		for (Task task : tasks) {
			TaskLocker taskLocker = new RedisTaskLocker(task.getClass().getName(), stringRedisTemplate);
			TaskExecutor taskExecutor = new TaskExecutor(task, taskLocker);
			taskRegistrar.addCronTask(taskExecutor, task.cron());
		}
	}
}