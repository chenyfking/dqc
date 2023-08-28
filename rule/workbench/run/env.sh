#!/bin/bash

setEnv() {
  if [ -z "${!1}" ]; then
    export "$1"="$2"
  fi
}

DIR=$(dirname $(readlink -f "$0"))

setEnv JAVA_OPTS "-XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError -XX:ErrorFile=$DIR/hs_err_%p.log -Xms2048m -Xmx2048m"
setEnv LANG "zh_CN.UTF-8"

# 部署环境
# dev: 开发环境
# prod: 生产环境
setEnv ENV prod

# 服务根路径
setEnv SERVER_CONTEXT_PATH /gaea
# 服务端口
setEnv SERVER_PORT 33005

# 数据根目录
setEnv DATA_HOME /opt/gaea/workbench

# 前端包目录
setEnv FRONTEND_PATH /opt/gaea/frontend

#dqc 登录地址
setEnv DQC_URL http://127.0.0.1:8087/dqc/index.html
#是否开启dqc会话判断
setEnv DQC_SESSION false

# 上传文件大小限制
setEnv UPLOAD_MAX_FILE_SIZE 100Mb
# 接口请求参数大小限制
setEnv UPLOAD_MAX_REQUEST_SIZE 1000Mb

# 最大允许密码输入错误次数
setEnv LOGIN_RETRY_NUMBER 10
# 允许输入错误次数的时间间隔(分钟)
setEnv LOGIN_RETRY_TIME 5
# 锁定时间（小时）
setEnv LOGIN_LOCK_TIME 24
# 密码修改时间间隔（天）
setEnv PWD_EXPIRED_TIME 180

# session存储类型
# hash_map: 存储在本地
# redis: 存储在redis, 必须配置redis连接信息
setEnv SESSION_STORE_TYPE hash_map

# 日志配置文件路径
# classpath: 使用classpath下默认的配置文件
# file: 使用磁盘上的配置文件
setEnv LOG_CONFIG classpath:logback-spring.xml
# 日志存储路径
setEnv LOG_HOME /opt/gaea/logs
# 日志保存天数
setEnv LOG_MAXHISTORY 60
# 日志滚动大小
setEnv LOG_MAXSIZE 10Mb
# 日志ROOT级别
setEnv LOG_LEVEL INFO

# 停止服务等待时间，单位秒
setEnv SHUTDOWN_TIMEOUT 60

# 定时任务表达式
# 预统计前一天报表
setEnv PRESTATREPORT_CRON "0 0 1 * * ?"
# 解锁所有资源文件的编辑者
setEnv UNLOCKASSETSEDITOR_CROM "0 0 0 * * ?"

# REDIS 配置
# 单机配置
setEnv REDIS_HOST localhost
setEnv REDIS_PORT 6379
# 主从哨兵模式
setEnv REDIS_SENTINEL_MASTER mymaster
setEnv REDIS_SENTINEL_NODES "172.16.210.201:26379,172.16.210.202:26379,172.16.210.203:26379"
# REDIS 基本配置
setEnv REDIS_PASSWORD redis@skycloud123
setEnv REDIS_DATABASE 0
setEnv REDIS_POOL_MAX_ACTIVE 20
setEnv REDIS_POOL_MAX_IDLE 10
setEnv REDIS_POOL_MIN_IDLE 10
setEnv REDIS_POOL_MAX_WAIT 3000

# MYSQL 配置
setEnv DATABASE_IP localhost
setEnv DATABASE_NAME gaea
setEnv DATABASE_USERNAME gaea
setEnv DATABASE_PASSWORD gaea@skycloud123
setEnv DATABASE_INITIAL_SIZE 10
setEnv DATABASE_MIN_IDLE 10
setEnv DATABASE_MAX_ACTIVE 100
setEnv DATABASE_MAX_WAIT 60000