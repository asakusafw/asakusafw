#!/bin/bash
#
# Copyright 2011-2021 Asakusa Framework Team.
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

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_ROOT/conf/env.sh"
import "$_ROOT/libexec/validate-env.sh"

import "$ASAKUSA_HOME/hadoop/libexec/configure-hadoop.sh"
import "$_ROOT/libexec/configure-classpath.sh"

_WG_LAUNCHER="com.asakusafw.windgate.hadoopfs.ssh.StdoutEscapeMain"
_CLASS_NAME="com.asakusafw.windgate.hadoopfs.ssh.WindGateHadoopPut"

echo "$_CLASS_NAME" 1>&2
for f in "$@"
do
    echo "  $f" 1>&2
done

_CLASSPATH_DELIMITER="${WG_CLASSPATH_DELIMITER-:}"

if [ "$_HADOOP_CMD" != "" ]
then
    export HADOOP_CLASSPATH="$(IFS=$_CLASSPATH_DELIMITER; echo "${_CLASSPATH[*]}")$_CLASSPATH_DELIMITER$HADOOP_CLASSPATH"
    "$_HADOOP_CMD" \
        "$_WG_LAUNCHER" \
        "$_CLASS_NAME" \
        "$@"
    _RET=$?
else
    import "$ASAKUSA_HOME/core/libexec/configure-java.sh"
    _CLASSPATH+=("${_HADOOP_EMBED_CLASSPATH[@]}")
    "$_JAVA_CMD" \
        -classpath "$(IFS=$_CLASSPATH_DELIMITER; echo "${_CLASSPATH[*]}")" \
        "$_WG_LAUNCHER" \
        "$_CLASS_NAME" \
        "$@"
    _RET=$?
fi

if [ $_RET -ne 0 ]
then
    echo "$_CLASS_NAME failed with exit code: $_RET" 1>&2
    for f in "$@"
    do
        echo "  $f" 1>&2
    done
    exit $_RET
fi
