package com.beagledata.gaea.workbench.exception;

/**
 *
 * @since 2016年9月1日
 */
public class NoPermissionException extends RuntimeException {
	private static final long serialVersionUID = 441877650698078466L;
	public NoPermissionException() {
		super("权限不足!");
	}
}
