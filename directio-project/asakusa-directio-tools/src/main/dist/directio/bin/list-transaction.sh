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

usage() {
    cat 1>&2 <<EOF
Show Direct I/O Transactions

Usage:
    $0
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

if [ $# -ne 0 ]
then
    usage
    exit 1
fi

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_ROOT/conf/env.sh"
import "$_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_CLASS_NAME="com.asakusafw.directio.tools.DirectIoListTransaction"
_ASAKUSA_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"

import "$ASAKUSA_HOME/hadoop/libexec/configure-hadoop.sh"
import "$_ROOT/libexec/configure-classpath.sh"

echo "Starting List Direct I/O Transactions:"
echo " Hadoop Command: ${_HADOOP_CMD:-N/A}"
echo "          Class: $_CLASS_NAME"
echo "      Libraries: ${_CLASSPATH[*]}"
echo "      Arguments: $*"

if [ "$_HADOOP_CMD" != "" ]
then
    export HADOOP_CLASSPATH="$(IFS=:; echo "${_CLASSPATH[*]}"):$HADOOP_CLASSPATH"
    "$_HADOOP_CMD" \
        "$_CLASS_NAME" \
        -conf "$_ASAKUSA_CONF" \
        "$@"
    _DIO_RET=$?
else
    _CLASSPATH+=("${_HADOOP_EMBED_CLASSPATH[@]}")
    _CLASSPATH+=("${_HADOOP_EMBED_LOGGING_CLASSPATH[@]}")
    import "$ASAKUSA_HOME/core/libexec/configure-java.sh"
    "$_JAVA_CMD" $JAVA_OPTS \
        -classpath "$(IFS=:; echo "${_CLASSPATH[*]}")" \
        "$_CLASS_NAME" \
        -conf "$_ASAKUSA_CONF" \
        "$@"
    
    _DIO_RET=$?
fi

if [ $_DIO_RET -ne 0 ]
then
    echo "List Direct I/O Transactions failed with exit code: $_DIO_RET" 1>&2
    echo " Hadoop Command: ${_HADOOP_CMD:-N/A}" 1>&2
    echo "          Class: $_CLASS_NAME" 1>&2
    echo "      Libraries: ${_CLASSPATH[*]}"  1>&2
    echo "      Arguments: $*" 1>&2
    exit $_DIO_RET
fi
