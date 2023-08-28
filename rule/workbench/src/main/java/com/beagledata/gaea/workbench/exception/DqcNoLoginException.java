package com.beagledata.gaea.workbench.exception;

/**
 * Dqc主系统未登录异常
 **/
public class DqcNoLoginException extends RuntimeException {

    private String msg;

    public DqcNoLoginException() {
        super("数据质量工具系统未登录");
    }

    public DqcNoLoginException(String msg) {
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