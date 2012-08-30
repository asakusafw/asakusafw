#!/bin/sh
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

_WG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_WG_ROOT/conf/env.sh"
import "$_WG_ROOT/libexec/validate-env.sh"
import "$_WG_ROOT/libexec/configure-classpath.sh"
import "$_WG_ROOT/libexec/configure-hadoop-cmd.sh"

_WG_LAUNCHER="com.asakusafw.windgate.hadoopfs.ssh.StdoutEscapeMain"
_WG_CLASS="com.asakusafw.windgate.hadoopfs.ssh.WindGateHadoopPut"

echo "$_WG_CLASS" 1>&2
for f in $*
do
    echo "  $f" 1>&2
done

export HADOOP_CLASSPATH="$_WG_CLASSPATH"
"$HADOOP_CMD" "$_WG_LAUNCHER" "$_WG_CLASS" $*

_WG_RET=$?
if [ $_WG_RET -ne 0 ]
then
    echo "$_WG_CLASS failed with exit code: $_WG_RET" 1>&2
    for f in $*
    do
        echo "$f" 1>&2
    done
    exit $_WG_RET
fi
