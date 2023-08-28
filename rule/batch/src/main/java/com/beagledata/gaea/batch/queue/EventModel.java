package com.beagledata.gaea.batch.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 周庚新
 * @date 2020-11-04
 */
public class EventModel implements Serializable {
	private static final long serialVersionUID = 8181797692060201290L;

	public static final EventModel EOF = new EventModel();

	/**
	* id
	*/
	private Long id;
	/**
	 * 输入参数
	 */
	private Map<String, Object> input = new HashMap<>();
	/**
	 * 处理节点
	 */
	private String processingOwner;
	/**
	 * 处理时间
	 */
	private String processingTime;
	/**
	 * 处理状态
	 * AVAILABLE: 等待处理
	 * IN_PROCESSING : 正在处理
	 * PROCESSED : 处理完成
	 * FAILED 处理失败，超过了重试次数，等待后续处理
	 */
	private String processingState;
	/**
	 * 错误次数
	 */
	private int errorCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, Object> getInput() {
		return input;
	}

	public void setInput(Map<String, Object> input) {
		this.input = input;
	}

	public String getProcessingOwner() {
		return processingOwner;
	}

	public void setProcessingOwner(String processingOwner) {
		this.processingOwner = processingOwner;
	}

	public String getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(String processingTime) {
		this.processingTime = processingTime;
	}

	public String getProcessingState() {
		return processingState;
	}

	public void setProcessingState(String processingState) {
		this.processingState = processingState;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("EventModel{");
		sb.append("id=").append(id);
		sb.append(", input=").append(input);
		sb.append(", processingOwner='").append(processingOwner).append('\'');
		sb.append(", processingTime='").append(processingTime).append('\'');
		sb.append(", processingState='").append(processingState).append('\'');
		sb.append(", errorCount=").append(errorCount);
		sb.append('}');
		return sb.toString();
	}
}