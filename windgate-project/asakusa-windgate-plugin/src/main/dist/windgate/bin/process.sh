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
WindGate - A portable data transfer tool

Usage:
    $0 profile script session-kind batch-id flow-id execuion-id arguments

Parameters:
    profile
        name of WindGate profile name
    session-kind
        process kind of WindGate execution
        this must be one of:
              "begin" - creates a new session and then keep it
                "end" - opens a created session and then complete it
            "oneshot" - creates a new session and then complete it
    script
        path to the WindGate script
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
    arguments
        The arguments for this execution
        This must be form of "key1=value1,key2=value2,...",
        and the special characters '=', ',', '\' can be escaped by '\'.
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

if [ $# -ne 7 ]; then
    usage
    exit 1
fi

_OPT_PROFILE="$1"
_OPT_SESSION_KIND="$2"
_OPT_SCRIPT="$3"
_OPT_BATCH_ID="$4"
_OPT_FLOW_ID="$5"
_OPT_EXECUTION_ID="$6"
_OPT_ARGUMENTS="$7"

_dirname=$(dirname "$0")
_WG_ROOT="$(cd "$_dirname" ; pwd)/.."

import "$_WG_ROOT/conf/env.sh"
import "$_WG_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_WG_PROFILE="$_WG_ROOT/profile/${_OPT_PROFILE}.properties"
_WG_SCRIPT="$_OPT_SCRIPT"

case "$_OPT_SESSION_KIND" in
    begin)
        _WG_MODE="begin"
    ;;
    end)
        _WG_MODE="end"
    ;;
    oneshot)
        _WG_MODE="oneshot"
    ;;
    *)
        echo "Unknown process kind: \"$_OPT_SESSION_KIND\"" 1>&2
        exit 1
    ;;
esac

_WG_SESSION="$_OPT_EXECUTION_ID"
_WG_ARGUMENTS="$_OPT_ARGUMENTS"
_WG_ARGUMENTS="$_WG_ARGUMENTS,user=$USER"
_WG_ARGUMENTS="$_WG_ARGUMENTS,batch_id=$_OPT_BATCH_ID"
_WG_ARGUMENTS="$_WG_ARGUMENTS,flow_id=$_OPT_FLOW_ID"
_WG_ARGUMENTS="$_WG_ARGUMENTS,execution_id=$_OPT_EXECUTION_ID"
_WG_CLASSPATH="$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"
_WG_CLASS="com.asakusafw.windgate.bootstrap.WindGate"

import "$_WG_ROOT/libexec/configure-classpath.sh"
import "$_WG_ROOT/libexec/configure-plugin.sh"
import "$_WG_ROOT/libexec/configure-hadoop-cmd.sh"

export WINDGATE_PROFILE="$_OPT_PROFILE"

cat << __EOF__
Starting WindGate
  -classpath $_WG_CLASSPATH
  -mode $_WG_MODE
  -profile $_WG_PROFILE
  -script $_WG_SCRIPT
  -session $_WG_SESSION
  -plugin $_WG_PLUGIN
  -arguments $_WG_ARGUMENTS
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
        -mode "$_WG_MODE" \
        -profile "$_WG_PROFILE" \
        -script "$_WG_SCRIPT" \
        -session "$_WG_SESSION" \
        -plugin "$_WG_PLUGIN" \
        -arguments "$_WG_ARGUMENTS"
else
    java \
        $WINDGATE_OPTS \
        -classpath "$_WG_CLASSPATH" \
        "-Dcom.asakusafw.windgate.log.batchId=${_OPT_BATCH_ID:-(unknown)}" \
        "-Dcom.asakusafw.windgate.log.flowId=${_OPT_FLOW_ID:-(unknown)}" \
        "-Dcom.asakusafw.windgate.log.executionId=${_OPT_EXECUTION_ID:-(unknown)}" \
        "$_WG_CLASS" \
        -mode "$_WG_MODE" \
        -profile "$_WG_PROFILE" \
        -script "$_WG_SCRIPT" \
        -session "$_WG_SESSION" \
        -plugin "$_WG_PLUGIN" \
        -arguments "$_WG_ARGUMENTS"
fi

_WG_RET=$?
if [ $_WG_RET -ne 0 ]
then
    cat 1>&2 << __EOF__
WindGate failed with exit code: $_WG_RET
  -classpath $_WG_CLASSPATH
  -mode $_WG_MODE
  -profile $_WG_PROFILE
  -script $_WG_SCRIPT
  -session $_WG_SESSION
  -plugin $_WG_PLUGIN
  -arguments $_WG_ARGUMENTS
  WINDGATE_OPTS="$WINDGATE_OPTS"
__EOF__
    exit $_WG_RET
fi
