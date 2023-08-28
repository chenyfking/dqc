package com.beagledata.gaea.workbench.exception;

public class RelationFileNotExistException extends RuntimeException {
	private static final long serialVersionUID = 441877650698078466L;
	private String msg;

	public RelationFileNotExistException() {
		super("引用的数据已经被删除或修改,请重新设置数据");
	}

	public RelationFileNotExistException(String msg) {
		if(msg.contains("was not found")){
			msg = msg.replaceAll("was not found","")+" 引用的数据已经被修改，请重新设置数据";
		}
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
