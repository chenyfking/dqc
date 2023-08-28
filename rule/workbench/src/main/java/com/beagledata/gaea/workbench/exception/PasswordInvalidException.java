package com.beagledata.gaea.workbench.exception;

/**
 * @Auther: yinrj
 * @Date: 0020 2021/1/20 09:43
 * @Description: 密码校验异常
 */
public class PasswordInvalidException extends RuntimeException {

    private String msg;
    private int code;

    public PasswordInvalidException() {
        super();
    }

    public PasswordInvalidException(String msg) {
        this.msg = msg;
        fillInStackTrace();
    }

    public PasswordInvalidException(int code, String msg) {
        this.code = code;
        this.msg = msg;
        fillInStackTrace();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
