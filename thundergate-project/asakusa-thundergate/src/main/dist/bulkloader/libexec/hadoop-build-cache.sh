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
    cat <<EOF
Builds cache for ThunderGate.

Usage:
    $0 subcommand batch-id flow-id execution-id cache-path model-class

Parameters:
    subcommand
        "create" - create a new cache
        "update" - update a cache
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
    cache-path
        path to the cache location
    model-class
        Fully qualified class name of data model class
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

_TGC_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_TGC_ROOT/conf/env.sh"
import "$_TGC_ROOT/libexec/validate-env.sh"

if [ $# -ne 6 ]
then
    echo "$@" 1>&2
    usage
    exit 1
fi

_OPT_SUBCOMMAND="$1"
shift
_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
shift
_OPT_EXECUTION_ID="$1"
shift
_OPT_CACHE_PATH="$1"
shift
_OPT_MODEL_CLASS="$1"
shift

# Move to home directory
cd

_TGC_CLASS_NAME="com.asakusafw.thundergate.runtime.cache.mapreduce.CacheBuildClient"
_TGC_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_TGC_RUNTIME_LIB="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
_TGC_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_TGC_APP_LIB="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"

_TGC_LIBJARS="$_TGC_APP_LIB"
import "$_TGC_ROOT/libexec/configure-libjars.sh"
import "$_TGC_ROOT/libexec/configure-hadoop-cmd.sh"

echo "Starting Build ThunderGate Cache:"
echo " Hadoop Command: $HADOOP_CMD"
echo "    Sub Command: $_OPT_SUBCOMMAND"
echo "       Batch ID: $_OPT_BATCH_ID"
echo "        Flow ID: $_OPT_FLOW_ID"
echo "   Execution ID: $_OPT_EXECUTION_ID"
echo "     Cache Path: $_OPT_CACHE_PATH"
echo "      Data Type: $_OPT_MODEL_CLASS"

export HADOOP_CLASSPATH=""
"$HADOOP_CMD" jar \
    "$_TGC_RUNTIME_LIB" \
    "$_TGC_TOOL_LAUNCHER" \
    "$_TGC_CLASS_NAME" \
    -conf "$_TGC_PLUGIN_CONF" \
    -libjars "$_TGC_LIBJARS" \
    "$_OPT_SUBCOMMAND" \
    "$_OPT_CACHE_PATH" \
    "$_OPT_MODEL_CLASS"

_TGC_RET=$?
if [ $_TGC_RET -ne 0 ]
then
    echo "Build ThunderGate Cache failed with exit code: $_TGC_RET" 1>&2
    echo "  Runtime Lib: $_TGC_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_TGC_TOOL_LAUNCHER"  1>&2
    echo "   Main Class: $_TGC_CLASS_NAME" 1>&2
    echo "  Sub Command: $_OPT_SUBCOMMAND"  1>&2
    echo "   Cache Path: $_OPT_CACHE_PATH"  1>&2
    echo "   Model Type: $_OPT_MODEL_CLASS"  1>&2
    echo "    Libraries: -libjars $_TGC_LIBJARS"  1>&2
    exit $_TGC_RET
fi
