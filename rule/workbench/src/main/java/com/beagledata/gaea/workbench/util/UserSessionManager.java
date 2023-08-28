package com.beagledata.gaea.workbench.util;

import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.config.session.MapSessionServiceImpl;
import com.beagledata.gaea.workbench.config.session.RedisSessionServiceImpl;
import com.beagledata.gaea.workbench.config.session.SessionService;
import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 用户及服务信息管理
 * Created by chenyf on 2018/1/29
 * @author zhoug
 */
@Component
public class UserSessionManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired(required = false)
	private SessionRepository sessionRepository;
	@Autowired
	private UserMapper userMapper;

	private SessionService sessionService;
	private static Set<String> dqcUserInfo = new HashSet<>();

	@PostConstruct
	public void initSessionService() throws Exception {
		if (sessionRepository == null) {
			logger.info("不设置session存储");
			Map<String, ExpiringSession> sessions = new HashMap<>();
			sessionService = new MapSessionServiceImpl(sessions);
			return;
		}
		if (sessionRepository instanceof RedisOperationsSessionRepository) {
			logger.info("spring-session:集群版");
			RedisOperationsSessionRepository repository = (RedisOperationsSessionRepository) sessionRepository;
			Class clazz = repository.getClass();
			Field templateField = clazz.getDeclaredField("sessionRedisOperations");
			templateField.setAccessible(true);
			RedisTemplate template = (RedisTemplate) templateField.get(repository);
			sessionService = new RedisSessionServiceImpl(template, userMapper);
		} else if (sessionRepository instanceof MapSessionRepository) {
			logger.info("spring-session:单机版");
			MapSessionRepository repository = (MapSessionRepository) sessionRepository;
			Class clazz = repository.getClass();
			Field sessionsField = clazz.getDeclaredField("sessions");
			sessionsField.setAccessible(true);
			Map<String, ExpiringSession> sessions = (Map) sessionsField.get(repository);
			sessionService = new MapSessionServiceImpl(sessions, userMapper);
		} else {
			throw new IllegalArgumentException("session配置有误");
		}
	}

	/**
	 * 清除用户在session中的数据
	 */
	public void clearSession(String key, User user) {
		if (null != user && StringUtils.isNotBlank(key)) {
			notRememberMeReLogin(user);
			sessionService.clearSession(key, user);
		}
	}

	/**
	 * 描述: 是否被强制下线
	 * @param: [userName, loginIp]
	 * @author: 周庚新
	 * @date: 2019/12/27
	 * @return: boolean
	 */
	public boolean isForceLogOut(String userName, String loginIp) {
		return sessionService.isForceLogOut(userName, loginIp);
	}

	/**
	 * 删除 rememberMe的 cookie信息
	 */
	public void notRememberMeReLogin(User user) {
		UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
		token.setRememberMe(false);
	}

	/**
	 * 检测用户session是否过期
	 * uuid, 用户uuid
	 * return true:session有效 false:session无效
	 */
	public boolean sessionEffective(String uuid) {
		return sessionService.isEffective(uuid);
	}

	public User getUserFromSession(String userUuid) {
		return sessionService.getUserByUuid(userUuid);
	}

	/**
	 * 获取在线用户
	 */
	public List<OnlineUser> getOnlineUsers(String sortField, String sortDirection) {
		return sessionService.getOnlineUsers(sortField, sortDirection);
	}

	/**
	 * 强制下线
	 * @param uuid
	 * @param loginIp
	 */
	public void forceLogOut(String uuid, String loginIp) {
		sessionService.forceLogOut(uuid, loginIp);
	}

	/**
	 * 描述: 是否异地登录
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: boolean
	 */
	public boolean hasLoged(String uuid) {
		return sessionService.hasLoged(uuid);
	}
	/**
	 * 描述: sessionId是否登录
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: boolean
	 */
	public boolean hasLoged(String uuid, String sessionId) {
		return sessionService.hasLoged(uuid, sessionId);
	}

	/**
	* 描述: 强制登出当前用户异地登录的账号
	* @param: [uuid, currentSessionId]
	* @author: 周庚新
	* @date: 2020/11/11
	* @return: void
	*/
	public void forceLogOutOtherPlace(String uuid, String currentSessionId) {
		sessionService.forceLogOutOtherPlace(uuid, currentSessionId);
	}
	/**
	 * 描述: 强制登出当前用户
	 * @param: [uuid, currentSessionId]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	public void forceLogOutCurrent(String currentSessionId) {
		sessionService.forceLogOutCurrent(currentSessionId);
	}

	/**
	 * 替换license
	 */
	public void replaceLicense(InputStream inputStream) {
		try {
			String license = IOUtils.toString(inputStream);
			FileUtils.writeStringToFile(new File(resourceResolver.getLicensePath()), license, "UTF-8");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalStateException("上传License失败");
		}
	}

	public boolean addDqcUserInfo(String info) {
		try {
			dqcUserInfo.add(info);
			logger.info("已添加用户[{}]相关信息", info);
			return true;
		} catch (Exception e) {
			logger.error("增加dqcUserInfo[{}]出错", info, e.getLocalizedMessage(), e);
			return false;
		}
	}

	public boolean delDqcUserInfo(String info) {
		try {
			dqcUserInfo.remove(info);
			logger.info("已删除用户[{}]相关信息", info);
			return true;
		} catch (Exception e) {
			logger.error("删除dqcUserInfo[{}]出错", info, e.getLocalizedMessage(), e);
			return false;
		}
	}

	public boolean containsDqcInfo(String info) {
		try {logger.info("判定是否有用户[{}]相关信息", info);
			return dqcUserInfo.contains(info);
		} catch (Exception e) {
			logger.error("判断是否包含dqcUserInfo[{}]出错", info, e.getLocalizedMessage(), e);
			return false;
		}
	}

	public void showInfo() {
		if (dqcUserInfo.size() > 0) {
			logger.info("dqc user info : ");
			for (String info : dqcUserInfo) {
				logger.info("info: {}", info);
			}
		} else {
			logger.info("userinfo is null ");
		}
	}

}
