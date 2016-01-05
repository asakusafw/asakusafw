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
