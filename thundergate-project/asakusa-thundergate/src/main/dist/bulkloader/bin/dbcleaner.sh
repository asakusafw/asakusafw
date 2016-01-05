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

# DBをクリーニングするツール
# ThunderGateで使用するシステムテーブルの内容を全て削除する。
# 実行すると以下の処理を行う。
# ・ジョブフロー実行テーブル(RUNNING_JOBFLOWS)のレコードを全件削除
# ・ジョブフロー排他テーブル(JOBFLOW_INSTANCE_LOCK)のレコードを全件削除
# ・ロック(テーブルロック・レコードロック)を全て解除
# ・エクスポートテンポラリテーブルを全て削除
# ・エクスポートテンポラリ管理のレコードを全件削除

usage() {
	cat <<EOF
DBをクリーニングするツールを起動します。

起動シェルスクリプト
dbcleaner.sh

 順  引数                                       必須/任意
 ---------------------------------------------------------
 1   ターゲット名                                 必須

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

if [ $# -ne 1 ]; then
    usage
    exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_TG_ROOT/conf/env.sh"
import "$_TG_ROOT/libexec/validate-env.sh"
import "$_TG_ROOT/libexec/configure-hadoop-cmd.sh"
import "$_TG_ROOT/libexec/configure-classpath.sh"

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

CLASS_NAME=com.asakusafw.bulkloader.tools.DBCleaner

export HADOOP_CLASSPATH="$_TG_CLASSPATH"
export HADOOP_USER_CLASSPATH_FIRST=true

cd

"$HADOOP_CMD" \
    $CLASS_NAME \
    "$@"

rc=$?
echo RETURN_CODE:$rc
exit $rc
