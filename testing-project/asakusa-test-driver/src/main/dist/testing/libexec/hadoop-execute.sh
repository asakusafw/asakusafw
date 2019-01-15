#!/bin/bash
#
# Copyright 2011-2019 Asakusa Framework Team.
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
Asakusa test-driver Hadoop job launcher.

Usage:
    $0 class-name batch-id flow-id [direct-arguments...]

Parameters:
    class-name
        Fully qualified class name of program entry
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
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

if [ $# -lt 4 ]
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

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"
import "$_ROOT/conf/env.sh"
import "$_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_RUNTIME_LIB="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"

if [ -e "$ASAKUSA_HOME/hadoop/libexec/configure-hadoop.sh" ]
then
    import "$ASAKUSA_HOME/hadoop/libexec/configure-hadoop.sh"
else
    _HADOOP_CMD="${HADOOP_CMD:?}"
fi

import "$_ROOT/libexec/configure-classpath.sh"

echo "Starting test-driver Hadoop job:"
echo " Hadoop Command: ${_HADOOP_CMD:-N/A}"
echo "    Application: $_OPT_CLASS_NAME"
echo "       Batch ID: $_OPT_BATCH_ID"
echo "        Flow ID: $_OPT_FLOW_ID"

if [ "$_HADOOP_CMD" != "" ]
then
    "$_HADOOP_CMD" jar \
        "$_RUNTIME_LIB" \
        "$_TOOL_LAUNCHER" \
        "$_OPT_CLASS_NAME" \
        -conf "$_PLUGIN_CONF" \
        -libjars "$(IFS=,; echo "${_CLASSPATH[*]}")" \
        "$@"
    _RET=$?
else
    _CLASSPATH_DELIMITER=":"
    if [ -e "$ASAKUSA_HOME/core/libexec/configure-java.sh" ]
    then
        import "$ASAKUSA_HOME/core/libexec/configure-java.sh"
    else
        _JAVA_CMD=java
    fi
    _CLASSPATH+=("${_HADOOP_EMBED_CLASSPATH[@]}")
    _CLASSPATH+=("${_HADOOP_EMBED_LOGGING_CLASSPATH[@]}")
    "$_JAVA_CMD" $JAVA_OPTS \
        -classpath "$(IFS=$_CLASSPATH_DELIMITER; echo "${_CLASSPATH[*]}")" \
        "$_TOOL_LAUNCHER" \
        "$_OPT_CLASS_NAME" \
        -conf "$_PLUGIN_CONF" \
        "$@"
    _RET=$?
fi

if [ $_RET -ne 0 ]
then
    echo "Hadoop job failed with exit code: $_RET" 1>&2
    echo " Hadoop Command: ${_HADOOP_CMD:-N/A}" 1>&2
    echo "    Application: $_OPT_CLASS_NAME" 1>&2
    echo "       Batch ID: $_OPT_BATCH_ID" 1>&2
    echo "        Flow ID: $_OPT_FLOW_ID" 1>&2
    echo "  Configuration: $_PLUGIN_CONF" 1>&2
    echo "      Libraries: ${_CLASSPATH[*]}" 1>&2
    echo "    Extra Props: $*" 1>&2
    exit $_RET
fi
