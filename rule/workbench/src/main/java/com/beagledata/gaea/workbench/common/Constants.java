package com.beagledata.gaea.workbench.common;

import org.apache.commons.io.FileUtils;

import java.text.SimpleDateFormat;

/**
 * Created by liulu on 2017/12/6.
 */
public class Constants {
    /**
     * session中账户的key
     */
    public static final String SESSION_KEY_USER = "user";

    /**
     * 列表分页默认显示数据行数
     */
    public static final String PAGE_ROWS = "10";

    public static final class User {
        /**
         * 超级管理员账户默认登录用户名
         */
        public static final String SUPERADMIN_USERNAME = "superadmin";
    }


    public static final class License{
        /**
         * 登录页面license无效 code值
         */
        public static final int LOGIN_LICENSE_INVALID = -2;
    }

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final class BaselineStat {
        //待发布
        public static final int UNPUBLISHED = 0;
        //待生效
        public static final int UNEFFECTIVE = 2;
        //已生效
        public static final int EFFECTIVE = 3;
        //待审核
        public static final int AUDIT = 1;
        //已拒绝
        public static final int REFUSE = 4;
    }
}
