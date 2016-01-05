#!/bin/bash
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
Collects cache information.
*** This program is for only ThundeGate internal use. ***

Usage:
    $0 target-name batch-id flow-id execution-id

Parameters:
    target-name
        profile name (used for detect database connection properties)
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution

I/O:
    standard input:
        FileList protocol
    standard output:
        FileList protocol
    standard error:
        print information in execution
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

if [ $# -ne 4 ]; then
  usage
  exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift

import "$_TG_ROOT/conf/env.sh"
import "$_TG_ROOT/libexec/validate-env.sh"
import "$_TG_ROOT/libexec/configure-hadoop-cmd.sh"

_TG_CLASSPATH="$ASAKUSA_BATCHAPPS_HOME/$_BATCH_ID/lib/jobflow-${_FLOW_ID}.jar"
import "$_TG_ROOT/libexec/configure-classpath.sh"

export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader 1>&2

LOGFILE_BASENAME="get-cache-info"
CLASS_NAME="com.asakusafw.bulkloader.cache.GetCacheInfoRemote"
USER_NAME="$(whoami)"

export HADOOP_CLASSPATH="$_TG_CLASSPATH"
export HADOOP_USER_CLASSPATH_FIRST=true
HADOOP_OPTS="$HADOOP_OPTS -Dasakusa.home=$ASAKUSA_HOME"
HADOOP_OPTS="$HADOOP_OPTS -Dlogfile.basename=$LOGFILE_BASENAME"
export HADOOP_OPTS

cd

"$HADOOP_CMD" \
    "$CLASS_NAME" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID" \
    "$USER_NAME"
rc=$?
exit $rc
