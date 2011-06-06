#!/bin/bash
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

LOGFILE_BASENAME="hdfsCleaner"
CLASS_NAME="com.asakusafw.cleaner.main.HDFSCleaner"

D=`dirname $0`
DIR=`cd $D;pwd`
USER_NAME=$(whoami)

CLEAN_CLASSPATH=$DIR/conf
CLEAN_CLASSPATH=$CLEAN_CLASSPATH:$DIR/lib/*

export CLEANER_HOME=$DIR
. $DIR/conf/.clean_hdfs_profile

if [ $# -eq 2 ]; then
  $JAVA_HOME/bin/java $HDFSCLEANER_JAVA_OPTS -Dclean.home=$DIR -Dlogfile.basename=$LOGFILE_BASENAME -classpath $CLEAN_CLASSPATH $CLASS_NAME $1 $USER_NAME $2
  rc=$?
else
  usage
  rc=1
fi

exit $rc
