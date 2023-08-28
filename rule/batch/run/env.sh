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

# 数据根目录
setEnv DATA_HOME /gaea/data/batch
# workbench接口地址
setEnv WORKBENCH_BASE_URL http://localhost:33005/gaea

# 日志配置文件路径
# classpath: 使用classpath下默认的配置文件
# file: 使用磁盘上的配置文件
setEnv LOG_CONFIG classpath:logback-spring.xml
# 日志存储路径
setEnv LOG_HOME /gaea/logs/batch
# 日志保存天数
setEnv LOG_MAXHISTORY 60
# 日志滚动大小
setEnv LOG_MAXSIZE 10Mb
# 日志ROOT级别
setEnv LOG_LEVEL INFO

# 处理线程数，<=0 自动分配，默认两倍cpu
setEnv PROCESSING_THREAD_NUM -1
# 最大拉取记录数量
setEnv MAX_ENTRIES_CLAIMED 50
# 拉取时间间隔
setEnv CLAIM_TIME 1
# 每条记录最大重试次数
setEnv MAX_FAILURE_RETRIES 3
# 每条记录处理超时时间，单位秒
setEnv PROCESSING_TIMEOUT 300

# REDIS 配置
# 单机配置
setEnv REDIS_HOST localhost
setEnv REDIS_PORT 6379
# 主从哨兵模式
#setEnv REDIS_SENTINEL_MASTER mymaster
#setEnv REDIS_SENTINEL_NODES "172.16.210.201:26379,172.16.210.202:26379,172.16.210.203:26379"
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
