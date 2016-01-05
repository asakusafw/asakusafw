#!/bin/bash
#
# Copyright 2011-2016 Asakusa Framework Team.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#HDFSCleaner起動コマンド

usage() {
	cat <<EOF
HDFSCleanerを起動します。

起動シェルスクリプト
cleanHDFS.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   動作モード(normal or recursive)    必須
 2   コンフィグレーションファイル       必須

EOF
}

if [ $# -ne 2 ]
then
    usage
    exit 1
fi

LOGFILE_BASENAME="hdfsCleaner"
CLASS_NAME="com.asakusafw.cleaner.main.HDFSCleaner"

D=$(dirname $0)
DIR=$(cd $D/..;pwd)
USER_NAME=$(whoami)

CLEAN_CLASSPATH=$DIR/conf
CLEAN_CLASSPATH=$CLEAN_CLASSPATH:$DIR/lib/*

CLEAN_MODE="$1"
CLEAN_CONF="$2"

export CLEANER_HOME=$DIR
. $DIR/conf/.clean_hdfs_profile

if [ "$HADOOP_CMD" = "" ]
then
    if [ "$HADOOP_HOME" != "" ]
    then
        HADOOP_CMD="$HADOOP_HOME/bin/hadoop"
        unset HADOOP_HOME
    else
        HADOOP_CMD="$(which hadoop)"
        _RET=$?
        if [ $_RET -ne 0 ]
        then
            echo 'hadoop command is not found' 1>&2
            exit 1
        fi
    fi
fi

if [ ! -x "$HADOOP_CMD" ]
then
    echo "$HADOOP_CMD is not executable" 1>&2
    exit 1
fi

export HADOOP_CLASSPATH="$CLEAN_CLASSPATH"
HADOOP_OPTS="$HADOOP_OPTS $HDFSCLEANER_JAVA_OPTS"
HADOOP_OPTS="$HADOOP_OPTS -Dclean.home=$DIR"
HADOOP_OPTS="$HADOOP_OPTS -Dlogfile.basename=$LOGFILE_BASENAME"
export HADOOP_OPTS
"$HADOOP_CMD"  $CLASS_NAME "$CLEAN_MODE" "$USER_NAME" "$CLEAN_CONF"
rc=$?

exit $rc
