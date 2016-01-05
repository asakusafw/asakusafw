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

#Importer起動コマンド

usage() {
	cat <<EOF
Importerを起動します。

起動シェルスクリプト
importer.sh

 順  引数                                 必須/任意
 --------------------------------------------------
 1   Import処理区分(primary or secondary)  必須
 2   ターゲット名                          必須
 3   バッチID                              必須
 4   ジョブフローID                        必須
 5   ジョブフロー実行ID                    必須
 6   ジョブフロー終了予定時刻              必須
 7   変数表の文字列表記                    必須
 8   リカバリ対象テーブル                  任意

 ※ジョブネットの終了予定時刻は「YYYYMMDDHHMMSS」形式で指定する。
 ※リカバリ対象テーブルが複数ある場合は半角カンマ「,」で区切って指定する。

EOF
}

import() {
    _SCRIPT="$1"
    if [ -e "$_SCRIPT" ]
    then
        . "$_SCRIPT"
    else
        echo "$_SCRIPT is not found" 1>&2
        exit 1
    fi
}

if [ $# -ne 7 -a $# -ne 8 ]; then
  usage
  exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

_IMPORTER_CLASS="$1"
shift
_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift
_ESTIMATED_END="$1"
shift
export BULKLOADER_ARGS="$1"
shift

import "$_TG_ROOT/conf/env.sh"
import "$_TG_ROOT/libexec/validate-env.sh"
import "$_TG_ROOT/libexec/configure-hadoop-cmd.sh"

_TG_CLASSPATH="$ASAKUSA_BATCHAPPS_HOME/$_BATCH_ID/lib/jobflow-${_FLOW_ID}.jar"
import "$_TG_ROOT/libexec/configure-classpath.sh"

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="importer"
CLASS_NAME="com.asakusafw.bulkloader.importer.Importer"

export HADOOP_CLASSPATH="$_TG_CLASSPATH"
export HADOOP_USER_CLASSPATH_FIRST=true
HADOOP_OPTS="$HADOOP_OPTS $IMPORTER_JAVA_OPTS"
HADOOP_OPTS="$HADOOP_OPTS -Dasakusa.home=$ASAKUSA_HOME"
HADOOP_OPTS="$HADOOP_OPTS -Dlogfile.basename=$LOGFILE_BASENAME"
export HADOOP_OPTS

cd

"$HADOOP_CMD" \
    "$CLASS_NAME" \
    "$_IMPORTER_CLASS" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID" \
    "$_ESTIMATED_END" \
    $*
rc=$?

exit $rc

