#!/bin/bash
#LocalFileCleaner起動コマンド

usage() {
	cat <<EOF
LocalFileCleanerを起動します。

起動シェルスクリプト
cleanLocalFS.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   動作モード(normal or recursive)    必須
 2   コンフィグレーションファイル       必須

EOF
}

LOGFILE_BASENAME="localFileCleaner"
CLASS_NAME="com.asakusafw.cleaner.main.LocalFileCleaner"

D=`dirname $0`
DIR=`cd $D/..;pwd`
USER_NAME=$(whoami)

CLEAN_CLASSPATH=$DIR/conf
CLEAN_CLASSPATH=$CLEAN_CLASSPATH:$DIR/lib/*

export CLEANER_HOME=$DIR
. $DIR/conf/.clean_local_profile

if [ $# -eq 2 ]; then
  $JAVA_HOME/bin/java $LOCALFILECLEANER_JAVA_OPTS -Dclean.home=$DIR -Dlogfile.basename=$LOGFILE_BASENAME -classpath $CLEAN_CLASSPATH $CLASS_NAME $*
  rc=$?
else
  usage
  rc=1
fi

exit $rc
