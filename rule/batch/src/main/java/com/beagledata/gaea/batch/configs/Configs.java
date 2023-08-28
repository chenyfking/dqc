package com.beagledata.gaea.batch.configs;

import com.beagledata.gaea.common.BaseConfigs;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 描述:
 * 配置类
 *
 * @author 周庚新
 * @date 2020-11-04
 */
@Component
@ConfigurationProperties(prefix = "config")
public class Configs extends BaseConfigs {
	/**
	 * 服务uuid
	 */
	private String MicroUuid;
	/**
	 * 输出表名
	 */
	private String outputTableName;
	/**
	 * 处理线程数
	 */
	private int processingThreadNum;
	/**
	 * 最大拉取记录数量
	 */
	private int maxEntriesClaimed;
	/**
	 * 拉取时间间隔
	 */
	private int claimTime;
	/**
	 * 每条记录最大重试次数
	 */
	private int maxFailureRetries;
	/**
	 * 每条记录处理超时时间
	 */
	private int processingTimeout;
	/**
	 * 业务日期
	 */
	private String bizDate;

	public String getMicroUuid() {
		return MicroUuid;
	}

	public void setMicroUuid(String microUuid) {
		MicroUuid = microUuid;
	}

	public String getOutputTableName() {
		return outputTableName;
	}

	public void setOutputTableName(String outputTableName) {
		this.outputTableName = outputTableName;
	}

	public int getProcessingThreadNum() {
		return processingThreadNum;
	}

	public void setProcessingThreadNum(int processingThreadNum) {
		this.processingThreadNum = processingThreadNum;
	}

	public int getMaxEntriesClaimed() {
		return maxEntriesClaimed;
	}

	public void setMaxEntriesClaimed(int maxEntriesClaimed) {
		this.maxEntriesClaimed = maxEntriesClaimed;
	}

	public int getClaimTime() {
		return claimTime;
	}

	public void setClaimTime(int claimTime) {
		this.claimTime = claimTime;
	}

	public int getMaxFailureRetries() {
		return maxFailureRetries;
	}

	public void setMaxFailureRetries(int maxFailureRetries) {
		this.maxFailureRetries = maxFailureRetries;
	}

	public int getProcessingTimeout() {
		return processingTimeout;
	}

	public void setProcessingTimeout(int processingTimeout) {
		this.processingTimeout = processingTimeout;
	}

	public String getBizDate() {
		return bizDate;
	}

	public void setBizDate(String bizDate) {
		this.bizDate = bizDate;
	}

	public String getDecisionLogPath() {
		return SafelyFiles.newFile(getDataHome(), "decisionLog").getAbsolutePath();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Configs{");
		sb.append("MicroUuid='").append(MicroUuid).append('\'');
		sb.append(", outputTableName='").append(outputTableName).append('\'');
		sb.append(", processingThreadNum=").append(processingThreadNum);
		sb.append(", maxEntriesClaimed=").append(maxEntriesClaimed);
		sb.append(", claimTime=").append(claimTime);
		sb.append(", maxFailureRetries=").append(maxFailureRetries);
		sb.append(", processingTimeout=").append(processingTimeout);
		sb.append(", bizDate='").append(bizDate).append('\'');
		sb.append('}');
		return sb.toString();
	}
}