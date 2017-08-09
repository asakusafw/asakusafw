#!/bin/bash
#
# Copyright 2011-2017 Asakusa Framework Team.
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

if [ $# -lt 1 ]
then
    exit 2
fi

_BOOTSTRAP="$1"
shift

import "${ASAKUSA_HOME:?ASAKUSA_HOME is not defined}/hadoop/libexec/configure-hadoop.sh"

if [ "$_HADOOP_CMD" != "" ]
then
    export HADOOP_CLASSPATH="$_BOOTSTRAP:$HADOOP_CLASSPATH"
    exec "$_HADOOP_CMD" \
        "$@"
else
    _CLASSPATH=("$_BOOTSTRAP")
    _CLASSPATH+=("${_HADOOP_EMBED_CLASSPATH[@]}")
    _CLASSPATH+=("${_HADOOP_EMBED_LOGGING_CLASSPATH[@]}")
    import "$ASAKUSA_HOME/core/libexec/configure-java.sh"
    exec "$_JAVA_CMD" $JAVA_OPTS \
        -classpath "$(IFS=:; echo "${_CLASSPATH[*]}")" \
        "$@"
fi
