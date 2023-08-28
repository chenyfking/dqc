package com.beagledata.gaea.common;

/**
 * Created by liulu on 2020/7/20.
 */
public class RedisConstants {
    public static final class Gateway {
        /**
         * 刷新发布订阅KEY
         */
        public static final String CHANNEL_REFRESH_KEY = "channel:refresh";
        /**
         * 刷新令牌
         */
        public static final String CHANNEL_REFRESH_TOKEN = "token";
        /**
         * 刷新路由
         */
        public static final String CHANNEL_REFRESH_ROUTE = "route";
    }

    public static final class Login {
        /**
         * 连续登陆错误次数
         */
        public static final String LOGIN_ERR_RETRY_KEY_PATTERN = "login:err:retry:{0}";
        /**
         * 登录错误锁定用户
         */
        public static final String LOGIN_ERR_LOCK_KEY_PATTERN = "login:err:lock:{0}";
        /**
         * 初次登陆修改密码
         */
        public static final String FIRST_LOGIN_TO_EDITPWD = "firstLogin2EditPwd";
        /**
         * 最近一次修改密码时间
         */
        public static final String LAST_RESET_PWD_DATE = "lastResetPwdDate";
        /**
         * 用户登录session
         */
        public static final String SPRING_SESSION_PATTERN = "spring:session:sessions:{0}";


    }
}
