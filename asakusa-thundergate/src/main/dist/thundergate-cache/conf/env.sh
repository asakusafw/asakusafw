#!/bin/sh

# Set your Hadoop installation path
if [ "$HADOOP_HOME" = "" ]
then
    export HADOOP_HOME="/usr/lib/hadoop"
fi

# Set your Asakusa installation path
if [ "$ASAKUSA_HOME" = "" ]
then
    export ASAKUSA_HOME="$HOME/asauksa"
fi
