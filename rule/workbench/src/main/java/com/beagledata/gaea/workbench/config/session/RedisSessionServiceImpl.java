package com.beagledata.gaea.workbench.config.session;

import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.util.StringUtils;
import org.apache.shiro.web.session.HttpServletSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.*;

/**
 * Created by Cyf on 2020/5/21
 * 使用redis存储session (集群版)
 **/
public class RedisSessionServiceImpl implements SessionService{


    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;
    private final static String SPRINT_SESSION = "spring:session:sessions:";

    public RedisSessionServiceImpl(RedisTemplate template) {
        this.redisTemplate = template;
    }

    public RedisSessionServiceImpl(RedisTemplate template, UserMapper userMapper) {
        this.redisTemplate = template;
        this.userMapper = userMapper;
    }

    @Override
    public List<OnlineUser> getOnlineUsers(String sortField, String sortDirection) {
        List<OnlineUser> onlineList = new ArrayList<>();
        Calendar c = Calendar.getInstance();

        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        Set<String> keys1 = redisTemplate.keys(SPRINT_SESSION + "expires:*");
        Set<String> expiresKeys = new HashSet<>();
        keys1.forEach(k -> expiresKeys.add(k.replace("expires:", "")));
        HashOperations ops = redisTemplate.opsForHash();
        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }
            if (!expiresKeys.contains(key)) {
                redisTemplate.delete(key);
                continue;
            }
            //用户信息
            User user = (User) ops.get(key, "sessionAttr:user");
            if (null == user) {
                continue;
            }

            //用户登录成功后，通过HttpServletRequest解析真实IP,并添加到缓存
            //若缓存中没有则通过HttpServletSession获取Host
            if (StringUtils.isBlank(user.getLastLoginIP())) {
                String host = (String) ops.get(key, "sessionAttr:org.apache.shiro.web.session.HttpServletSession.HOST_SESSION_KEY");
                user.setLastLoginIP(host);
            }

            Long loginMillion = (Long) ops.get(key, "creationTime");//登录时间
            if (loginMillion == null) {
                if (user.getLastLoginTime() == null) {
                    continue;
                }
                loginMillion = user.getLastLoginTime().getTime();
            }

            //user.setUuid(key.replaceAll(".*:", ""));
            OnlineUser onlineUser = new OnlineUser();
            onlineUser.setUser(user);
            c.setTimeInMillis(loginMillion);
            onlineUser.setLoginTime(c.getTime());

            long nowTime = System.currentTimeMillis();
            long dateDiff = nowTime - loginMillion;//在线时长
            if (dateDiff >= 0) {//在线时长大于0
                onlineUser.setOnlineMillis(dateDiff);
                onlineUser.setOnlineTime(getOnlineTimeStr(dateDiff));//设置在线时长
            }

            addOnlineUserToList(onlineList, onlineUser);
        }

        if (onlineList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return sort(onlineList, sortField, sortDirection);
    }

    @Override
    public void clearSession(String sessionId, User user) {
        if (sessionId.startsWith(SPRINT_SESSION)) {
            redisTemplate.delete(sessionId);
            redisTemplate.delete(SPRINT_SESSION + "expires:" + sessionId.substring(SPRINT_SESSION.length(), sessionId.length()));
        } else {
            redisTemplate.delete(SPRINT_SESSION + sessionId);
            redisTemplate.delete(SPRINT_SESSION + "expires:" + sessionId);
        }
    }

    @Override
    public boolean isEffective(String uuid) {
		Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
		if (keys.isEmpty()) {
			return false;
		}
		Set<String> keys1 = redisTemplate.keys(SPRINT_SESSION + "expires:*");
		Set<String> expiresKeys = new HashSet<>(keys1.size());
		keys1.forEach(k -> expiresKeys.add(k.replace("expires:", "")));
		HashOperations ops = redisTemplate.opsForHash();
		for (String key : keys) {
			if (key.contains("expires")) {
				continue;
			}
			//用户信息
			User user = (User) ops.get(key, "sessionAttr:user");
			if (user != null && uuid.equals(user.getUuid())) {
				return expiresKeys.contains(key);
			}
		}
		return false;
    }

    @Override
    public User getUserByUuid(String userUuid) {
        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        if (keys.isEmpty()) {
            return null;
        }
        Set<String> keys1 = redisTemplate.keys(SPRINT_SESSION + "expires:*");
        Set<String> expiresKeys = new HashSet<>(keys1.size());
        keys1.forEach(k -> expiresKeys.add(k.replace("expires:", "")));
        HashOperations ops = redisTemplate.opsForHash();
        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }
            //用户信息
            User user = (User) ops.get(key, "sessionAttr:user");
            if (user != null && userUuid.equals(user.getUuid())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void forceLogOut(String uuid, String loginIp) {
        User usr = new User();
        usr.setUuid(uuid);
        usr.setForceLogout(true);
        userMapper.updateByPrimaryKeySelective(usr);
        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        HashOperations ops = redisTemplate.opsForHash();

        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }
            //用户信息
            User user = (User) ops.get(key, "sessionAttr:user");
            String host = (String) ops.get(key, "sessionAttr:org.apache.shiro.web.session.HttpServletSession.HOST_SESSION_KEY");
            if (user != null && StringUtils.isNotBlank(user.getUuid()) && user.getUuid().equals(uuid) && host.equals(loginIp)) {
                user.setLastLoginIP(loginIp);
                this.clearSession(key, user);
            }
        }
    }


    @Override
    public boolean isForceLogOut(String userName, String loginIp) {
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(loginIp)) {
			return false;
		}
        User user = userMapper.selectByUsername(userName);
		if (user != null && !user.isForceLogout()) {
		    return false;
        }
		return true;
    }

    @Override
    public void forceLogOutOtherPlace(String uuid, String sessionId) {
        User loginUser = new User();
        loginUser.setUuid(uuid);
        loginUser.setForceLogout(true);
        userMapper.updateByPrimaryKeySelective(loginUser);

        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        HashOperations ops = redisTemplate.opsForHash();
        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }

            //用户信息
            User user = (User) ops.get(key, "sessionAttr:user");
            String host = (String) ops.get(key, "org.apache.shiro.web.session.HttpServletSession.HOST_SESSION_KEY");
            if (user != null && StringUtils.isNotBlank(user.getUuid()) && user.getUuid().equals(uuid) && !key.contains(sessionId)) {
               this.clearSession(key, user);
            }
        }
    }

    @Override
    public void forceLogOutCurrent(String sessionId) {
        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }
            if (key.contains(sessionId)) {
                this.clearSession(key, null);
            }
        }
    }

    @Override
    public boolean hasLoged(String uuid) {
        Set<String> keys = redisTemplate.keys(SPRINT_SESSION + "*");
        HashOperations ops = redisTemplate.opsForHash();
        for (String key : keys) {
            if (key.contains("expires")) {
                continue;
            }
            
            //用户信息
            User user = (User) ops.get(key, "sessionAttr:user");
            if (user != null && StringUtils.isNotBlank(user.getUuid()) && user.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasLoged(String uuid, String sessionId) {
        HashOperations ops = redisTemplate.opsForHash();
        User user = (User) ops.get(SPRINT_SESSION + sessionId, "sessionAttr:user");
        if (user != null && StringUtils.isNotBlank(user.getUuid()) && user.getUuid().equals(uuid)) {
            return true;
        }
        return false;
    }
}