package com.beagledata.gaea.workbench.entity;


import com.beagledata.gaea.common.BaseEntity;

import java.util.Date;

/**
 * @ClassName: Logs 
 * @Description:  日志记录实体类
 * @author: yinrj
 * @date 2018年8月10日 下午3:00:37
 */
public class Logs extends BaseEntity {
	/**
	 * 操作描述
	 */
	private String optName;
	/**
	 * 操作账号
	 */
	private String user;
	/**
	 * 集群节点IP
	 */
	private String clientIp;
	/**
	 * 请求路径
	 */
	private String requestUrl;
	/**
	 * 请求方式
	 */
	private String requestType;
	/**
	 * 请求的方法
	 */
	private String requestMethod;
	/**
	 * 请求的参数
	 */
	private String requestParams;
	/**
	 * 请求开始时间
	 */
	private Date beginTime;
	/**
	 * 请求结束时间
	 */
	private Date endTime;
	/**
	 * 请求返回值
	 */
	private String backValue;
	/**
	 * 是否请求成功
	 */
	private boolean success;
	/**
	 * 请求用时（毫秒）
	 */
	private Integer useTime;

	public String getOptName() {
		return optName;
	}

	public void setOptName(String optName) {
		this.optName = optName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(String requestParams) {
		this.requestParams = requestParams;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getBackValue() {
		return backValue;
	}

	public void setBackValue(String backValue) {
		this.backValue = backValue;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Integer getUseTime() {
		return useTime;
	}

	public void setUseTime(Integer useTime) {
		this.useTime = useTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Logs{");
		sb.append("optName='").append(optName).append('\'');
		sb.append(", user='").append(user).append('\'');
		sb.append(", clientIp='").append(clientIp).append('\'');
		sb.append(", requestUrl='").append(requestUrl).append('\'');
		sb.append(", requestType='").append(requestType).append('\'');
		sb.append(", requestMethod='").append(requestMethod).append('\'');
		sb.append(", requestParams='").append(requestParams).append('\'');
		sb.append(", beginTime=").append(beginTime);
		sb.append(", endTime=").append(endTime);
		sb.append(", backValue='").append(backValue).append('\'');
		sb.append(", success=").append(success);
		sb.append(", useTime=").append(useTime);
		sb.append('}');
		sb.append(" ").append(super.toString());
		return sb.toString();
	}
}
