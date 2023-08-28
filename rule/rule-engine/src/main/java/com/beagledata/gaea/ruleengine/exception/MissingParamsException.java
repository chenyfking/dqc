package com.beagledata.gaea.ruleengine.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 缺少参数异常
 * @author 周庚新
 * @date 2020-11-02
 */
public class MissingParamsException extends RuleException {
	private static final long serialVersionUID = 9139775739198453125L;

	public MissingParamsException() {}

	public MissingParamsException(String message) {
		super(message);
	}

	public MissingParamsException(Throwable cause) {
		super(cause);
	}

	public MissingParamsException(String message, Throwable cause) {
		super(message, cause);
	}

}