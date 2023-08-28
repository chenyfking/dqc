package com.beagledata.gaea.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-10-10
 */
public class BaseConfigs {
	/**
	* 数据目录
	*/
	private String dataHome;
	/**
	 * 停止服务超时时间，单位秒
	 */
	private int shutdownTimeout;
	/**
	 * 任务配置
	 */
	private List<Task> tasks = new ArrayList<>();
	/**
	 * workbench 相关配置
	 */
	private Workbench workbench = new Workbench();

	public static class Task {
		/**
		 * 任务执行类全路径
		 */
		private String className;
		/**
		 * 任务cron
		 */
		private String cron;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Task{");
			sb.append("className='").append(className).append('\'');
			sb.append(", cron='").append(cron).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	public static class Workbench {
		/**
		 * workbench 服务地址
		 */
		private String baseUrl;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Workbench{");
			sb.append("baseUrl='").append(baseUrl).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	public String getDataHome() {
		return dataHome;
	}

	public void setDataHome(String dataHome) {
		this.dataHome = dataHome;
	}

	public int getShutdownTimeout() {
		return shutdownTimeout;
	}

	public void setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Workbench getWorkbench() {
		return workbench;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("BaseConfigs{");
		sb.append("dataHome='").append(dataHome).append('\'');
		sb.append(", shutdownTimeout=").append(shutdownTimeout);
		sb.append(", tasks=").append(tasks);
		sb.append(", workbench=").append(workbench);
		sb.append('}');
		return sb.toString();
	}
}