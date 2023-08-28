package com.beagledata.gaea.workbench.common;

/**
 * 描述:
 * 规则验证类型
 *
 * @author zgx
 * @date 2020-07-10
 */
public enum VeriferMsgType {

	/**
	 * 提示
	 */
	INFO("info"),
	/**
	 * 错误
	 */
	ERROR("error"),
	/**
	 * 警告
	 */
	WARN("warn");

	private String type;

	VeriferMsgType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}
}
