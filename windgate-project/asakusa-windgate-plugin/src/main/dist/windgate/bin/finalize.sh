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

usage() {
    cat 1>&2 <<EOF
WindGate FInalizer

Usage:
    $0 profile
    or
    $0 profile [batch-id flow-id] execuion-id

Parameters:
    profile
        name of WindGate profile name
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
        if not specified, this cleans all sessions
EOF
}

import() {
    _SCRIPT="$1"
    if [ -e "$_SCRIPT" ]
    then
        . "$_SCRIPT"
    else
        echo "$_SCRIPT is not found" 2>&1
        exit 1
    fi
}

if [ $# -eq 1 ]
then
    _OPT_PROFILE="$1"
elif [ $# -eq 2 ]
then
    _OPT_PROFILE="$1"
    _OPT_EXECUTION_ID="$2"
elif [ $# -eq 4 ]
then
    _OPT_PROFILE="$1"
    _OPT_BATCH_ID="$2"
    _OPT_FLOW_ID="$3"
    _OPT_EXECUTION_ID="$4"
else
    usage
    exit 1
fi

_dirname=$(dirname "$0")
_WG_ROOT="$(cd "$_dirname" ; pwd)/.."

import "$_WG_ROOT/conf/env.sh"
import "$_WG_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_WG_PROFILE="$_WG_ROOT/profile/${_OPT_PROFILE}.properties"
_WG_SESSION="$_OPT_EXECUTION_ID"
_WG_CLASS="com.asakusafw.windgate.bootstrap.WindGateAbort"

import "$_WG_ROOT/libexec/configure-classpath.sh"
import "$_WG_ROOT/libexec/configure-plugin.sh"
import "$_WG_ROOT/libexec/configure-hadoop-cmd.sh"

export WINDGATE_PROFILE="$_OPT_PROFILE"

cat << __EOF__
Finalizing WindGate Session(s)
  -classpath $_WG_CLASSPATH
  -profile $_WG_PROFILE
  -session $_WG_SESSION
  -plugin $_WG_PLUGIN
  WINDGATE_OPTS="$WINDGATE_OPTS"
__EOF__

if [ -x "$HADOOP_CMD" ]
then
    export HADOOP_CLASSPATH="$_WG_CLASSPATH"
    HADOOP_OPTS="$HADOOP_OPTS $WINDGATE_OPTS"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.batchId=${_OPT_BATCH_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.flowId=${_OPT_FLOW_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.executionId=${_OPT_EXECUTION_ID:-(unknown)}"
    export HADOOP_OPTS
    "$HADOOP_CMD" \
        "$_WG_CLASS" \
        -profile "$_WG_PROFILE" \
        -session "$_WG_SESSION" \
        -plugin "$_WG_PLUGIN"
else
    java \
        $WINDGATE_OPTS \
        -classpath "$_WG_CLASSPATH" \
        "-Dcom.asakusafw.windgate.log.batchId=${_OPT_BATCH_ID:-(unknown)}" \
        "-Dcom.asakusafw.windgate.log.flowId=${_OPT_FLOW_ID:-(unknown)}" \
        "-Dcom.asakusafw.windgate.log.executionId=${_OPT_EXECUTION_ID:-(unknown)}" \
        "$_WG_CLASS" \
        -profile "$_WG_PROFILE" \
        -session "$_WG_SESSION" \
        -plugin "$_WG_PLUGIN"
fi

_WG_RET=$?
if [ $_WG_RET -ne 0 ]
then
    cat 1>&2 << __EOF__
WindGateAbort failed with exit code: $_WG_RET
  -classpath $_WG_CLASSPATH
  -profile $_WG_PROFILE
  -session $_WG_SESSION
  -plugin $_WG_PLUGIN
  WINDGATE_OPTS="$WINDGATE_OPTS"
__EOF__
    exit $_WG_RET
fi
