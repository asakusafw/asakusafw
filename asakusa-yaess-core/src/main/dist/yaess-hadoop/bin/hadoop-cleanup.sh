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
YAESS Hadoop Cleanup Tool

Usage:
    $0 working-directory batch-id flow-id execution-id batch-arguments [direct-arguments...]

Parameters:
    working-directory
        Path to the working directory
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
        echo "$_SCRIPT is not found" 2>&1
        exit 1
    fi
}

if [ $# -lt 5 ]
then
    echo "$@" 1>&2
    usage
    exit 1
fi

_OPT_WORKING_DIRECTORY="$1"
shift
_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
shift
_OPT_EXECUTION_ID="$1"
shift
_OPT_BATCH_ARGUMENTS="$1"
shift

_YS_ROOT="$(dirname $0)/.."
import "$_YS_ROOT/conf/env.sh"
import "$_YS_ROOT/libexec/configure-hadoop-cmd.sh"

# Move to home directory
cd

echo "Starting Asakusa Hadoop CleanUp:"
echo "     Hadoop Command: $HADOOP_CMD"
echo "  Working Directory: $_OPT_WORKING_DIRECTORY"

"$HADOOP_CMD" fs \
    -rmr \
    "$_OPT_WORKING_DIRECTORY" \
    "$@"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then
    echo "Asakusa Hadoop Cleanup failed with exit code: $_YS_RET" 1>&2
    echo "  Working Directory: $_OPT_WORKING_DIRECTORY"
    exit $_YS_RET
fi
