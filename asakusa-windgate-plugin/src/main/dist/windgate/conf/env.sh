#!/bin/sh

if [ "$HADOOP_HOME" = "" ]
then
    export HADOOP_HOME="/usr/lib/hadoop"
    echo "HADOOP_HOME was not set. We assumes HADOOP_HOME=\"$HADOOP_HOME\"" 2>&1
fi

export HADOOP_USER_CLASSPATH_FIRST=true
