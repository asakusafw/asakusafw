#!/bin/sh
#
# Copyright 2011-2014 Asakusa Framework Team.
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
YAESS Hadoop Command Line

Usage:
    $0 class-name batch-id flow-id execution-id batch-arguments [direct-arguments...]

Parameters:
    class-name
        Fully qualified class name of program entry
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
    batch-arguments
        The arguments for this execution
        This must be form of "key1=value1,key2=value2,...",
        and the special characters '=', ',', '\' can be escaped by '\'.
    direct-arguments...
        Direct arguments for Hadoop
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

if [ $# -lt 5 ]
then
    echo "$@" 1>&2
    usage
    exit 1
fi

_OPT_CLASS_NAME="$1"
shift
_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
shift
_OPT_EXECUTION_ID="$1"
shift
_OPT_BATCH_ARGUMENTS="$1"
shift

_YS_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"
import "$_YS_ROOT/conf/env.sh"
import "$_YS_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_YS_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_YS_RUNTIME_LIB="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
_YS_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_YS_APP_LIB="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"

_YS_LIBJARS="$_YS_APP_LIB"
import "$_YS_ROOT/libexec/configure-libjars.sh"
import "$_YS_ROOT/libexec/configure-hadoop-cmd.sh"

echo "Starting Asakusa Hadoop:"
echo " Hadoop Command: $HADOOP_CMD"
echo "    App Library: $_YS_APP_LIB"
echo "       Batch ID: $_OPT_BATCH_ID"
echo "        Flow ID: $_OPT_FLOW_ID"
echo "   Execution ID: $_OPT_EXECUTION_ID"
echo "          Class: $_OPT_CLASS_NAME"

"$HADOOP_CMD" jar \
    "$_YS_RUNTIME_LIB" \
    "$_YS_TOOL_LAUNCHER" \
    "$_OPT_CLASS_NAME" \
    -conf "$_YS_PLUGIN_CONF" \
    -libjars "$_YS_LIBJARS" \
    -D "com.asakusafw.user=$USER" \
    -D "com.asakusafw.executionId=$_OPT_EXECUTION_ID" \
    -D "com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" \
    $YS_HADOOP_PROPERTIES \
    "$@"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then
    echo "YAESS Hadoop failed with exit code: $_YS_RET" 1>&2
    echo "  Runtime Lib: $_YS_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_YS_TOOL_LAUNCHER"  1>&2
    echo "  Stage Class: $_OPT_CLASS_NAME" 1>&2
    echo "Configuration: -conf $_YS_PLUGIN_CONF"  1>&2
    echo "    Libraries: -libjars $_YS_LIBJARS"  1>&2
    echo "Built-in Prop: -D com.asakusafw.user=$USER" 1>&2
    echo "Built-in Prop: -D com.asakusafw.executionId=$_OPT_EXECUTION_ID" 1>&2
    echo "Built-in Prop: -D com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" 1>&2
    echo "Defined Props: $YS_HADOOP_PROPERTIES" 1>&2
    echo "  Extra Props: $@" 1>&2
    exit $_YS_RET
fi
