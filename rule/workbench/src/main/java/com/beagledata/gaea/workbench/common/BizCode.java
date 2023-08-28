package com.beagledata.gaea.workbench.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 业务状态码
 * @author 周庚新
 * @date 2020-11-10
 */
public class BizCode {

	/**
	* 哎模型已经存在，更新模型
	*/
	public static final int AIMODEL_ISEXIST = -3;
	/**
	 *登录用户已存在
	 */
	public static final int LOGIN_USER_EXISTS = 1;
	/**
	 *用户被锁定
	 */
	public static final int LOGIN_USER_LOCKED = -1;
	/**
	 *初次登陆强制修改密码
	 */
	public static final int FIRST_LOGIN_TO_EDITPWD = 2;
	/**
	 *密码过期
	 */
	public static final int PASSWORD_EXPIRED = 3;

}