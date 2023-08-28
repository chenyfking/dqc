package com.beagledata.gaea.workbench.exception;

/**
 * 模型异常
 * Created by Cyf on 2019/12/10
 **/
public class AimodelInvalidException extends RuntimeException {
    private static final long serialVersionUID = 441866650698078455L;

    private String msg;

    public AimodelInvalidException() {
        super("AI模型异常");
    }

    public AimodelInvalidException(String msg) {
        this.msg = msg;
        fillInStackTrace();
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }
}