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

sh $APP_HOME/bin/shutdown.sh

if [ -f "$APP_HOME/app.pid" ];then
    PID=`cat $APP_HOME/app.pid`
    COUNT=1
    if [ -n $PID ];then
        while [ 0 != $COUNT ]
        do
            COUNT=`ps -ef|grep $PID |grep $APP_HOME |grep -v "grep" |wc -l`
        done
    fi
fi

sh $APP_HOME/bin/startup.sh