package com.beagledata.gaea.workbench.exception;

/**
 * @Auther: yinrj
 * @Date: 0020 2018/8/20 14:23
 * @Description: 输入参数格式不正确异常
 */
public class InputNotMatchException extends RuntimeException {
    private String msg;

    public InputNotMatchException() {
        super("输入的参数格式不正确！");
    }

    public InputNotMatchException(String msg) {
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
