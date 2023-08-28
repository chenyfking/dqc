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

if [ -f "$APP_HOME/app.pid" ];then
    PID=`cat $APP_HOME/app.pid`
    if [ -n $PID ];then
        COUNT=`ps -ef|grep $PID |grep $APP_HOME |grep -v "grep" |wc -l`
        if [ 0 != $COUNT ]; then
            kill $PID

            startTime=`date +%s`
            endTime=`date +%s`
            shutdownSuccess=0
            maxShutdownTimeout=63

            if [ $SHUTDOWN_TIMEOUT ]; then
                maxShutdownTimeout=`expr $SHUTDOWN_TIMEOUT + 3`
            fi

            while (($endTime-$startTime < $maxShutdownTimeout)) # 等待最大超时
            do
                endTime=`date +%s`
                COUNT=`ps -ef|grep $PID |grep $APP_HOME |grep -v "grep" |wc -l`
                if [[ 0 == $COUNT ]]; then
                  shutdownSuccess=1
                  break
                fi
            done

            if [ $shutdownSuccess == 1 ]; then
                echo "停止服务成功"
                exit 0
            else
                echo "停止服务失败"
                echo "查看日志：$LOG_HOME/all/all.log"
                exit 1
            fi
        fi
    fi
fi