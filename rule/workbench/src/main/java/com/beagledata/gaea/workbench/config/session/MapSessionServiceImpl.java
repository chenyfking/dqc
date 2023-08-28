package com.beagledata.gaea.workbench.config.session;

import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.session.ExpiringSession;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cyf on 2020/5/21
 * 使用内存存储session (单机版)
 **/
public class MapSessionServiceImpl implements SessionService{
    private static Logger logger = LogManager.getLogger(MapSessionServiceImpl.class);
    private Map<String, ExpiringSession> sessions;
    private UserMapper userMapper;

    public MapSessionServiceImpl(Map<String, ExpiringSession> sessions) {
        this.sessions = sessions;
    }

    public MapSessionServiceImpl(Map<String, ExpiringSession> sessions, UserMapper userMapper) {
        this.sessions = sessions;
        this.userMapper = userMapper;
    }

    @Override
    public List<OnlineUser> getOnlineUsers(String sortField, String sortDirection) {
        List<OnlineUser> onlineList = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User)es.getAttribute("user");
            if (null == user) {
                continue;
            }

            if (StringUtils.isBlank(user.getLastLoginIP())) {
                String host =  es.getAttribute("org.apache.shiro.web.session.HttpServletSession.HOST_SESSION_KEY");
                user.setLastLoginIP(host);
            }

            Long loginMillion = es.getCreationTime();//登录时间
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
    public void clearSession(String key, User user) {
        sessions.remove(key);
    }

    @Override
    public boolean isEffective(String uuid) {
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User) es.getAttribute("user");
            if (null == user) {
                continue;
            }
            if (uuid.equals(user.getUuid())) {
                int maxInactiveIntervalInSeconds = es.getMaxInactiveIntervalInSeconds();
                long lastAccessedTime = es.getLastAccessedTime();
                long diff = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis((long) maxInactiveIntervalInSeconds);
                return maxInactiveIntervalInSeconds < 0 ? false :  diff < lastAccessedTime;
            }
        }
        return false;
    }

    @Override
    public User getUserByUuid(String userUuid) {
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User) es.getAttribute("user");
            if (null == user) {
                continue;
            }
            if (userUuid.equals(user.getUuid())) {
               return user;            }
        }
        return null;
    }

    @Override
    public void forceLogOut(String uuid, String loginIp) {
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User) es.getAttribute("user");
            if (null == user) {
                continue;
            }
            if (uuid.equals(user.getUuid())) {
                User user1 = new User();
                user1.setUuid(uuid);
                user1.setForceLogout(true);
                userMapper.updateByPrimaryKeySelective(user1);
                sessions.remove(entry.getKey());
            }
        }
    }

    @Override
    public boolean isForceLogOut(String userName, String loginIp) {
        User user = userMapper.selectByUsername(userName);
		if (user != null && !user.isForceLogout()) {
		    return false;
        }
		return true;
    }

    @Override
    public void forceLogOutOtherPlace(String uuid, String sessionId) {
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User) es.getAttribute("user");
            if (user == null) {
                continue;
            }
            if (uuid.equals(user.getUuid())) {
                User loginUser = new User();
                loginUser.setUuid(uuid);
                loginUser.setForceLogout(true);
                userMapper.updateByPrimaryKeySelective(loginUser);
                sessions.remove(entry.getKey());
            }
        }
    }

    @Override
    public void forceLogOutCurrent(String sessionId) {

    }

    @Override
    public boolean hasLoged(String uuid) {
        for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
            ExpiringSession es = entry.getValue();
            User user = (User) es.getAttribute("user");
            if (user == null) {
                continue;
            }
            if (uuid.equals(user.getUuid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasLoged(String uuid, String sessionId) {
        return false;
    }
}