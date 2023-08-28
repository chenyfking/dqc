package com.beagledata.gaea.common.task;

/**
 * 描述:
 * 定时任务
 *
 * @author 周庚新
 * @date 2020-09-18
 */
public abstract class AbstractTask implements Task {
	private String cron;

	@Override
	public String cron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}
}