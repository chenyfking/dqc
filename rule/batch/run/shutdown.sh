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

if [ -f "$APP_HOME/app.pid" ];then
    PID=`cat $APP_HOME/app.pid`
    COUNT=0
    if [ -n $PID ];then
        COUNT=`ps -ef|grep $PID |grep $APP_HOME |grep -v "grep" |wc -l`
        if [ 0 != $COUNT ]; then
            kill $PID
        fi
    fi
fi