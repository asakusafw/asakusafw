#!/bin/bash
#
# Copyright 2012 Asakusa Framework Team.
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
JobQueue Hadoop Command Line

Usage:
    $0 class-name batch-id flow-id execution-id jrid \\
        batch-arguments [direct-arguments...]

Parameters:
    class-name
        Fully qualified class name of program entry
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
    jrid
        Job Request ID
    batch-arguments
        The arguments for this execution
        This must be form of "key1=value1,key2=value2,...",
        and the special characters '=', ',', '\' can be escaped by '\'.
    direct-arguments...
        Direct arguments for Hadoop
EOF
}

if [ $# -lt 6 ]
then
    echo "Invalid arguments: [$@]" 1>&2
    usage
    exit 1
fi

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
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
_OPT_JRID="$1"
shift
_OPT_BATCH_ARGUMENTS="$1"
shift

_JQ_ROOT="$(dirname $0)/.."
if [ -e "$_JQ_ROOT/conf/env.sh" ]
then
    . "$_JQ_ROOT/conf/env.sh"
fi

# Move to home directory

cd ~

_JQ_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_JQ_CORE_LIB_DIR="$ASAKUSA_HOME/core/lib"
_JQ_EXT_LIB_DIR="$ASAKUSA_HOME/ext/lib"
_JQ_RUNTIME_LIB="$_JQ_CORE_LIB_DIR/asakusa-runtime.jar"
_JQ_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_JQ_APP_LIB="$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"

_JQ_LIBJAR="$_JQ_APP_LIB"

if [ -d "$_JQ_CORE_LIB_DIR" ]
then
    for f in $(ls "$_JQ_CORE_LIB_DIR")
    do
        _JQ_LIBJAR="$_JQ_LIBJAR,$_JQ_CORE_LIB_DIR/$f"
    done
fi

if [ -d "$_JQ_EXT_LIB_DIR" ]
then
    for f in $(ls "$_JQ_EXT_LIB_DIR")
    do
        _JQ_LIBJAR="$_JQ_LIBJAR,$_JQ_EXT_LIB_DIR/$f"
    done
fi

if [ "$HADOOP_TMP_DIR" != "" ]
then
    _OPT_HADOOP_TMP_DIR="-D hadoop.tmp.dir=$HADOOP_TMP_DIR/$_OPT_JRID"
    _OPT_MAPRED_LOCAL_DIR="-D mapred.local.dir=$HADOOP_TMP_DIR/$_OPT_JRID/mapred/local"
    _OPT_MAPRED_SYSTEM_DIR="-D mapred.system.dir=$HADOOP_TMP_DIR/$_OPT_JRID/mapred/system"
    _OPT_MAPREDUCE_JOBTRACKER_STAGING_ROOT_DIR="-D mapreduce.jobtracker.staging.root.dir=$HADOOP_TMP_DIR/$_OPT_JRID/mapred/staging"
    _OPT_MAPRED_TEMP_DIR="-D mapred.temp.dir=$HADOOP_TMP_DIR/$_OPT_JRID/mapred/temp"
fi

echo "Starting Asakusa Hadoop:"
echo "          JRID: $_OPT_JRID"
echo "   App Library: $_JQ_APP_LIB"
echo "      Batch ID: $_OPT_BATCH_ID"
echo "       Flow ID: $_OPT_FLOW_ID"
echo "  Execution ID: $_OPT_EXECUTION_ID"
echo "         Class: $_OPT_CLASS_NAME"
echo " Hadoop Properties:"
echo "   $_OPT_HADOOP_TMP_DIR"
echo "   $_OPT_MAPRED_LOCAL_DIR"
echo "   $_OPT_MAPRED_SYSTEM_DIR"
echo "   $_OPT_MAPREDUCE_JOBTRACKER_STAGING_ROOT_DIR"
echo "   $_OPT_MAPRED_TEMP_DIR"

"$HADOOP_HOME/bin/hadoop" jar \
    "$_JQ_RUNTIME_LIB" \
    "$_JQ_TOOL_LAUNCHER" \
    "$_OPT_CLASS_NAME" \
    -conf "$_JQ_PLUGIN_CONF" \
    -libjars "$_JQ_LIBJAR" \
    -D "com.asakusafw.user=$USER" \
    -D "com.asakusafw.executionId=$_OPT_EXECUTION_ID" \
    -D "com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" \
    $_OPT_HADOOP_TMP_DIR \
    $_OPT_MAPRED_LOCAL_DIR \
    $_OPT_MAPRED_SYSTEM_DIR \
    $_OPT_MAPREDUCE_JOBTRACKER_STAGING_ROOT_DIR \
    $_OPT_MAPRED_TEMP_DIR \
    $JQ_HADOOP_PROPERTIES \
    "$@"

_JQ_RET=$?
if [ $_JQ_RET -eq 0 ]
then
    if [ "$HADOOP_TMP_DIR" != "" ]
    then
        rm -rf "$HADOOP_TMP_DIR/$_OPT_JRID"
    fi
    exit $_JQ_RET
else
    echo "JobQueue Hadoop failed with exit code: $_JQ_RET" 1>&2
    echo "         JRID: $_OPT_JRID" 1>&2
    echo "  Runtime Lib: $_JQ_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_JQ_TOOL_LAUNCHER"  1>&2
    echo "  Stage Class: $_OPT_CLASS_NAME" 1>&2
    echo "Configuration: -conf $_JQ_PLUGIN_CONF"  1>&2
    echo "    Libraries: -libjars $_JQ_LIBJAR"  1>&2
    echo "Built-in Prop: -D com.asakusafw.user=$USER" 1>&2
    echo "Built-in Prop: -D com.asakusafw.executionId=$_OPT_EXECUTION_ID" 1>&2
    echo "Built-in Prop: -D com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" 1>&2
    echo "  Hadoop Prop: $_OPT_HADOOP_TMP_DIR" 1>&2
    echo "  Hadoop Prop: $_OPT_MAPRED_LOCAL_DIR" 1>&2
    echo "  Hadoop Prop: $_OPT_MAPRED_SYSTEM_DIR" 1>&2
    echo "  Hadoop Prop: $_OPT_MAPREDUCE_JOBTRACKER_STAGING_ROOT_DIR" 1>&2
    echo "  Hadoop Prop: $_OPT_MAPRED_TEMP_DIR" 1>&2
    echo "Defined Props: $JQ_HADOOP_PROPERTIES" 1>&2
    echo "  Extra Props: $@" 1>&2
    exit $_JQ_RET
fi
