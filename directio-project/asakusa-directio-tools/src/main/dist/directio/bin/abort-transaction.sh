#!/bin/sh
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

usage() {
    cat 1>&2 <<EOF
Aborts Direct I/O Transaction

Usage:
    $0 execuion-id

Parameters:
    execution-id
        execution ID of target execution
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

if [ $# -eq 1 ]
then
    _OPT_EXECUTION_ID="$1"
else
    usage
    exit 1
fi

_DIO_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_DIO_ROOT/conf/env.sh"
import "$_DIO_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_DIO_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_DIO_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_DIO_RUNTIME_LIB="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
_DIO_CLASS_NAME="com.asakusafw.directio.tools.DirectIoAbortTransaction"

import "$_DIO_ROOT/libexec/configure-libjars.sh"
import "$_DIO_ROOT/libexec/configure-hadoop-cmd.sh"

echo "Starting Abort Direct I/O Transaction:"
echo " Hadoop Command: $HADOOP_CMD"
echo "          Class: $_DIO_CLASS_NAME"
echo "   Execution ID: $_OPT_EXECUTION_ID"
echo "      Libraries: $_DIO_LIBJARS"

"$HADOOP_CMD" jar \
    "$_DIO_RUNTIME_LIB" \
    "$_DIO_TOOL_LAUNCHER" \
    "$_DIO_CLASS_NAME" \
    -conf "$_DIO_PLUGIN_CONF" \
    -libjars "$_DIO_LIBJARS" \
    "$_OPT_EXECUTION_ID"

_DIO_RET=$?
if [ $_DIO_RET -ne 0 ]
then
    echo "Abort Direct I/O Transaction failed with exit code: $_DIO_RET" 1>&2
    echo " Execution ID: $_OPT_EXECUTION_ID"  1>&2
    echo "  Runtime Lib: $_DIO_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_DIO_TOOL_LAUNCHER"  1>&2
    echo "        Class: $_DIO_CLASS_NAME" 1>&2
    echo "Configuration: -conf $_DIO_PLUGIN_CONF"  1>&2
    echo "    Libraries: -libjars $_DIO_LIBJARS"  1>&2
    exit $_DIO_RET
fi
