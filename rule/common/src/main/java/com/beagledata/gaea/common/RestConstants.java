package com.beagledata.gaea.common;

/**
 * workbench、openapi提供rest接口的相关常量
 *
 * Created by liulu on 2020/5/13.
 */
public class RestConstants {
    /**
     * 服务类型
     */
    public enum MicroType {
        /**
         * 规则服务
         */
        RULE,
        /**
         * AI模型服务
         */
        MODEL,
        /**
         * PMML模型服务
         */
        PMML
    }

    public final class Workbench {
        public final class Endpoints {
            /**
             * 注册集群节点
             */
            public static final String REGISTER_CLIENT = "/rest/client/regist";
            /**
             * 获取服务包
             */
            public static final String GET_MICRO_PACKAGE = "/rest/micropackage";
        }
    }

    public final class OpenApi {
        public final class Endpoints {
            /**
             * 获取license序列号
             */
            public static final String SERIALNUMBER = "/rest/serialnumber";
            /**
             * 上传license
             */
            public static final String UPLOAD_LICENSE = "/rest/license";
            /**
             * 发布服务
             */
            public static final String DEPLOY = "/rest/deploy";
            /**
             * 卸载服务
             */
            public static final String UNDEPLOY = "/rest/undeploy";
            /**
             * API调用文档
             */
            public static final String APIDOC = "/rest/apidoc";
            /**
             * 同步执行规则
             */
            public static final String EXECUTE_SYNC = "/execute";
            /**
             * 异步执行规则
             */
            public static final String EXECUTE_ASYNC = "/executeasync";
            /**
             * 异步执行规则，返回序列号的key 值
             */
            public static final String SERIAL_KEY = "seqNo";
            /**
             * 查询执行结果
             */
            public static final String QUERY = "/query";

        }
    }
}
