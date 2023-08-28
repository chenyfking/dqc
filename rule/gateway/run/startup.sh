#!/bin/bash

THIS="$0"

while [ -h "$THIS" ]; do
    ls=`ls -ld "$THIS"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
        THIS="$link"
    else
        THIS=`dirname "$THIS"`/"$link"
    fi
done

THIS_DIR=`dirname "$THIS"`
APP_HOME=`cd "$THIS_DIR/.." ; pwd`
source $APP_HOME/bin/env.sh
APP_CLASSPATH=""

if [ -z $JAVA_HOME ]; then
    echo "启动服务失败：JDK找不到"
    exit 1
fi

if [ -f "$APP_HOME/app.pid" ]; then
    PID=`cat $APP_HOME/app.pid`
    COUNT=0
    if [ -n $PID ];then
        COUNT=`ps -ef|grep $PID |grep $APP_HOME |grep -v "grep" |wc -l`
        if [ 0 != $COUNT ]; then
            echo "服务正在运行，请先停止服务"
            exit 1
        fi
    fi
fi

if [ -d "${APP_HOME}/command" ]; then
   jarLibs=$(ls ${APP_HOME}/command)
   for jarLib in $jarLibs;do
      APP_CLASSPATH=${APP_CLASSPATH}:${APP_HOME}/command/${jarLib}
   done
fi

cd ${APP_HOME}/conf
nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $APP_CLASSPATH com.beagledata.gaea.gateway.GatewayApplication >/dev/null 2>&1 &
echo $! >../app.pid

echo "正在启动服务..."

startTime=`date +%s`
endTime=`date +%s`
logFile=$LOG_HOME/all/all.log
logExists=0

while (($endTime-$startTime < 3)) # 等待3秒产生日志文件
do
    endTime=`date +%s`
    if [ -f $logFile ]; then
        logExists=1
        for i in $(seq 1 10) # 追加10行空行，避免日志文件没有变化
        do
            echo "" >> $logFile
        done
        break
    fi
done

if [ $logExists == 0 ]; then
    echo "启动服务失败"
    exit 1
fi

startupSuccess=0
maxStartupTimeout=60

while (($endTime-$startTime < $maxStartupTimeout)) # 等待最大超时
do
    endTime=`date +%s`
    str=`tail $logFile`
    if [[ $str =~ "Started" ]]; then # 监控启动成功日志
      startupSuccess=1
      break
    fi
done

if [ $startupSuccess == 1 ]; then
    echo "启动服务成功"
    exit 0
else
    echo "启动服务失败"
    echo "查看日志：$logFile"
    exit 1
fi