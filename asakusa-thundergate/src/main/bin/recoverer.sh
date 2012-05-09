#!/bin/bash
#
# Copyright 2011-2012 Asakusa Framework Team.
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

#Recoverer起動コマンド

usage() {
	cat <<EOF
Recovererを起動します。

起動シェルスクリプト
recoverer.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   ジョブフロー実行ID                任意

EOF
}

. ~/.bulkloader_db_profile
export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader

LOGFILE_BASENAME="recoverer"
CLASS_NAME="com.asakusafw.bulkloader.recoverer.Recoverer"

. $ASAKUSA_HOME/bulkloader/bin/set-classpath-db.sh

LIB_DIR=$ASAKUSA_HOME/batchapps
for dname in `ls $LIB_DIR`
do
   for fname in `ls $LIB_DIR/$dname/lib/*.jar`
   do
     BULK_LOADER_CLASSPATH=$BULK_LOADER_CLASSPATH:$fname
   done
done

cd $ASAKUSA_HOME

if [ $# -eq 1 -o $# -eq 2 ]; then
  $JAVA_HOME/bin/java $RECOVERER_JAVA_OPTS -Dasakusa.home=$ASAKUSA_HOME -Dlogfile.basename=$LOGFILE_BASENAME -classpath $BULK_LOADER_CLASSPATH $CLASS_NAME $*
  rc=$?
else
  usage
  rc=1
fi

exit $rc
