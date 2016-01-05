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
    cat << __EOF__
Releases locks for cache mechanism.

Usage:
    $0 target-name [execution-id]

Parameters:
    target-name
        profile name (used for detect database connection properties)
    execution-id
        execution ID of current execution
__EOF__
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

if [ $# -ne 1 -a $# -ne 2 ]
then
    usage
    exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

_OPT_TARGET_NAME="$1"
shift
_OPT_EXECUTION_ID="$1"

import "$_TG_ROOT/conf/env.sh"
import "$_TG_ROOT/libexec/validate-env.sh"
import "$_TG_ROOT/libexec/configure-hadoop-cmd.sh"
import "$_TG_ROOT/libexec/configure-classpath.sh"

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="release-cache-lock"
CLASS_NAME="com.asakusafw.bulkloader.cache.ReleaseCacheLock"

export HADOOP_CLASSPATH="$_TG_CLASSPATH"
export HADOOP_USER_CLASSPATH_FIRST=true
HADOOP_OPTS="$HADOOP_OPTS -Dasakusa.home=$ASAKUSA_HOME"
HADOOP_OPTS="$HADOOP_OPTS -Dlogfile.basename=$LOGFILE_BASENAME"
export HADOOP_OPTS

cd

echo "Starting release-cache-lock:"
echo "   Target Name: $_OPT_TARGET_NAME"
echo "  Execution ID: $_OPT_EXECUTION_ID"

"$HADOOP_CMD" \
    "$CLASS_NAME" \
    "$_OPT_TARGET_NAME" \
    "$@"

_TGC_RET=$?
if [ $_TGC_RET -ne 0 ]
then
    echo "Release Cache Lock failed with exit code: $_TGC_RET" 1>&2
    echo "   Target Name: $_OPT_TARGET_NAME" 1>&2
    echo "  Execution ID: $_OPT_EXECUTION_ID" 1>&2
    exit $_TGC_RET
fi
