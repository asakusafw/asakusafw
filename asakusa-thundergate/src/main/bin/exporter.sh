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

#Exporter起動コマンド

usage() {
	cat <<EOF
Exporterを起動します。

起動シェルスクリプト
exporter.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   バッチID                          必須
 3   ジョブフローID                    必須
 4   ジョブフロー実行ID                必須
 5   変数表の文字列表記                必須

EOF
}

if [ $# -ne 5 ]; then
  usage
  exit 1
fi

_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift
export BULKLOADER_ARGS="$1"
shift

. ~/.bulkloader_db_profile
export BULKLOADER_HOME="$ASAKUSA_HOME"/bulkloader

LOGFILE_BASENAME="exporter"
CLASS_NAME="com.asakusafw.bulkloader.exporter.Exporter"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh "$_BATCH_ID" "$_FLOW_ID"

cd "$ASAKUSA_HOME"

$JAVA_HOME/bin/java \
    $EXPORTER_JAVA_OPTS \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID"
rc=$?

exit $rc
