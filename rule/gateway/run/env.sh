#!/bin/bash

source /etc/profile

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
setEnv SERVER_CONTEXT_PATH /gateway
# 服务端口
setEnv SERVER_PORT 33007

# 最大信号量
setEnv ZUUL_SEMAPHORE_MAX 5000
# 连接超时时间(ms)
setEnv RIBBON_CONNECT_TIMEOUT 250
# 通信超时时间（读取数据超时）(ms)
setEnv RIBBON_READ_TIMEOUT 120000
# 是否对所有操作重试
setEnv RIBBON_OK_TO_RETRY_ON_ALL_OPERATIONS true
# 同一服务不同实例重试次数
setEnv RIBBON_MAX_AUTO_RETRIES_NEXT_SERVER 2
# 同一实例重试次数
setEnv RIBBON_MAX_AUTO_RETRIES 1
# 刷新负载serverlist的时间间隔（ms）
setEnv RIBBON_SERVER_LIST_REFERSH_INTERVAL 60000
# 熔断超时时长
setEnv HYSTRIX_TIMEOUT 250000

# 日志配置文件路径
# classpath: 使用classpath下默认的配置文件
# file: 使用磁盘上的配置文件
setEnv LOG_CONFIG classpath:logback-spring.xml
# 日志存储路径
setEnv LOG_HOME /gaea/logs/gateway
# 日志保存天数
setEnv LOG_MAXHISTORY 60
# 日志滚动大小
setEnv LOG_MAXSIZE 10Mb
# 日志ROOT级别
setEnv LOG_LEVEL INFO

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
