package com.beagledata.gaea.ruleengine.exception;

/**
 * 执行内置函数异常
 *
 * Created by liulu on 2018/10/15.
 */
public class ExecuteBuildinFunctionException extends RuleException {
    private static final long serialVersionUID = -5424816713782825705L;

    public ExecuteBuildinFunctionException() {
    }

    public ExecuteBuildinFunctionException(String message) {
        super(message);
    }

    public ExecuteBuildinFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecuteBuildinFunctionException(Throwable cause) {
        super(cause);
    }
}
