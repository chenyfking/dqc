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

if [ $? -ne 0 ]; then
    echo 1
fi

sh $APP_HOME/bin/startup.sh