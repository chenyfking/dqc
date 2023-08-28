package com.beagledata.gaea.workbench.util;

import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.entity.RoleSet;
import com.beagledata.util.StringUtils;
import org.apache.shiro.SecurityUtils;

/**
 * Created by liulu on 2019/9/17.
 */
public class UserHolder {
	/**
	 * 获取当前登录用户
	 * @author liulu
	 * 2019/9/17 16:00
	 */
	public static User currentUser() {
		return (User) SecurityUtils.getSubject().getSession().getAttribute(Constants.SESSION_KEY_USER);
	}


	/**
	 * 获取当前登录用户uuid
	 * @author liulu
	 * 2019/9/17 15:59
	 */
	public static String currentUserUuid() {
		return currentUser().getUuid();
	}

	/**
	 * @return 当前登录账户名
	 */
	public static String currentUserName() {
		User user = currentUser();
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	/**
	 * 当前登录用户是否拥有管理员权限
	 * @author chenyafeng
	 * @date 2018/11/30
	 */
	public static boolean hasAdminPermission() {
		User user = currentUser();
		if (user == null || StringUtils.isBlank(user.getUsername())) {
			return false;
		}

		return SecurityUtils.getSubject().hasRole(RoleSet.SystemAdmin.getCode()) || user.getUsername().equals(Constants.User.SUPERADMIN_USERNAME);
	}

	public static boolean isOrgAdmin() {
		User user = currentUser();
		if (user == null || StringUtils.isBlank(user.getUsername())) {
			return false;
		}

		return SecurityUtils.getSubject().hasRole(RoleSet.OrgAdmin.getCode());

	}
}
