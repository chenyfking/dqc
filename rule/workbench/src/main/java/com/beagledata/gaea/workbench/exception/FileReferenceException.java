package com.beagledata.gaea.workbench.exception;

/**
 * 删除的categroy或var被其他文件引用，不能删除
 * Created by Chenyafeng on 2018/8/14.
 */
public class FileReferenceException extends RuntimeException{
    private static final long serialVersionUID = -3140099368178238088L;
    private String msg;

    public FileReferenceException() {
        super("不能删除正在被其它文件引用的文件或属性");
    }

    public FileReferenceException(String msg) {
        this.msg = msg;
        fillInStackTrace();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
