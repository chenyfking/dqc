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
nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $APP_CLASSPATH com.beagledata.gaea.batch.BatchApplication $@
echo $! >../app.pid