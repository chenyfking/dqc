package com.beagledata.gaea.workbench.security;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.DefaultConfigs;
import com.beagledata.gaea.workbench.entity.Logs;
import com.beagledata.gaea.workbench.entity.Permission;
import com.beagledata.gaea.workbench.entity.Role;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.LogsMapper;
import com.beagledata.gaea.workbench.mapper.PermissionMapper;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.util.IpUtils;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.security.exception.IncorrectCaptchaException;
import com.beagledata.security.listener.SimpleSecurityListener;
import com.beagledata.security.util.PermissionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by liulu on 2019/9/17.
 */
@Component
public class CustomSecurityListener extends SimpleSecurityListener {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private PermissionMapper permissionMapper;
	@Autowired
	private UserSessionManager userSessionManager;
	@Autowired
	private LogsMapper logsMapper;
	@Autowired
	private AssetsService assetsService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private DefaultConfigs defaultConfigs;

	/**
	 * 登录成功回调
	 * 登录用户存session
	 * 更新最近登录时间
	 * 更新强制下线状态
	 * 查询用户信息和权限集合返回前端
	 *
	 * @author liulu
	 * 2019/9/17 14:48
	 */
	@Override
	public Object onLoginSuccess(String username, Session session, HttpServletRequest request) {
		Result result = null;
		try {
			User user = userMapper.selectBaseByUsername(username);
			if (user == null) {
				result = Result.newError().withMsg("登录失败");
				saveLogs(user, request, result, false);
				return result;
			}

			Date expiredTime = user.getExpiredTime();
			if (expiredTime != null && expiredTime.before(new Date())) {
				SecurityUtils.getSubject().logout();
				result = Result.newError().withMsg("账户已过期");
				saveLogs(user, request, result,false);
				return result;
			}

			doAfterLogin(user, session);
			result = Result.newSuccess().withData(user);
			saveLogs(user, request, result,true);
			return result;
		} catch (Exception e) {
			logger.error("登录失败. username: {}", username, e);
			result = Result.newError().withMsg("登录失败");
			saveLogs(null, request, result,false);
			return result;
		}
	}

	private void doAfterLogin(User user, Session session) {
		// 设置用户的权限集合
		setPermissions(user);
		// 更新用户登录信息
		updateUserForLogin(user);
		session.setAttribute(Constants.SESSION_KEY_USER, user);
	}

	private void setPermissions(User user) {
		List<Role> roles = userMapper.selectUserRole(user.getUuid());
		for (Role role : roles) {
			List<Permission> permissions = permissionMapper.selectByRoleId(role.getId(), 0, Integer.MAX_VALUE);
			for (Permission p : permissions) {
				user.getPermissions().add(PermissionUtils.parse(p.getResource(), p.getAction(), p.getIdentity()));
			}
		}
	}

	private void updateUserForLogin(User user) {
		user.setForceLogout(false);
		user.setLastLoginTime(new Date());
		user.setLastLoginIP(getLoginIp());
		userMapper.updateByPrimaryKeySelective(user);
	}

	/**
	 * 通过ThreadContext得到HttpServletRequest,获取真实IP
	 */
	private String getLoginIp() {
		try {
			Object o = ThreadContext.get("org.apache.shiro.util.ThreadContext_SUBJECT_KEY");
			WebDelegatingSubject delegatSubject = (WebDelegatingSubject)o;
			ServletRequest servletRequest = delegatSubject.getServletRequest();
			ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest)servletRequest;
			HttpServletRequest request = (HttpServletRequest)shiroRequest.getRequest();
			return IpUtils.getRealIp(request);
		} catch (Exception e) {
			logger.warn("解析真实IP失败", e);
		}
		return null;
	}

	/**
	 * 记住我回调
	 * 根据记住用户名查询用户
	 * 登录用户存session
	 * 更新最近登录时间
	 *
	 * @author liulu
	 * 2019/9/17 16:06
	 */
	@Override
	public boolean onRememberMe(Object principal, Session session) {
		User user = userMapper.selectBaseByUsername((String) principal);
		if (user == null) {
			return false;
		}

		if (user.isForceLogout()) {
			// 被管理员强制退出了
			String sessionId = session.getId().toString();
			userSessionManager.clearSession(sessionId, (User) session.getAttribute("user"));
			return false;
		}

		boolean otherPlaceHasLogin = userSessionManager.hasLoged(user.getUuid());
		boolean currentHasLogin = userSessionManager.hasLoged(user.getUuid(), session.getId().toString());
		if (otherPlaceHasLogin && !currentHasLogin) {
			// 当前sessionId在redis不存在或没有用户信息，且redis存在其他当前用户的sessionId，说明其他用户强制登录了
			return false;
		}

		doAfterLogin(user, session);
		return true;
	}

	@Override
	public boolean onPermittedBefore(String username, String permission) {
		return UserHolder.hasAdminPermission();
	}

	@Override
    public Object onLoginError(String username, Exception e, HttpServletRequest request) {
		Result errorResult = null;
		String msg = "用户名或密码错误";
		User user = userMapper.selectBaseByUsername(username);
		if (e instanceof IncorrectCaptchaException) {
			msg = e.getLocalizedMessage();
		}
		errorResult = Result.newError().withMsg(msg);
		saveLogs(user, request, errorResult, false);
		return errorResult;
    }

	@Override
	public Object onAuthorizeDenied() {
		return Result.newInstance(HttpStatus.FORBIDDEN.value()).withMsg("没有执行此操作的权限");
	}

	@Override
	public void onLogout(String username) {
		// 解锁当前用户所有的在编辑文件
		User user = UserHolder.currentUser();
		if (user != null) {
			assetsService.updateEditor(null, false, user.getUuid());
		}
		// 清除redis的session信息
		Session session = SecurityUtils.getSubject().getSession();
		userSessionManager.forceLogOutCurrent(session.getId().toString());
	}

	private void saveLogs(User user, HttpServletRequest request, Result result, boolean isSuccess) {
		Logs logs = new Logs();
		logs.setOptName("用户登录");
		if (user != null) {
			logs.setUser(user.getUuid());
		}
		logs.setClientIp(IpUtils.getRealIp(request));
		logs.setBeginTime((Date) request.getAttribute("loginTime"));
		logs.setEndTime(new Date());
		logs.setUseTime((int) (logs.getEndTime().getTime() - logs.getBeginTime().getTime()));

		Collection<String[]> maps =  request.getParameterMap().values();
		List<String> list = new ArrayList<String>();
		maps.forEach(v -> list.add(v[0]));
		logs.setRequestParams(list.toString());

		logs.setRequestUrl(request.getRequestURI());
		logs.setSuccess(isSuccess);
		logs.setRequestType(request.getMethod());
		logs.setBackValue(result.toString());
		logsMapper.insert(logs);
	}
}
