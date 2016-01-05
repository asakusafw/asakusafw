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
Creates/upadtes individual cache for table.

Usage:
    $0 target-name batch-id flow-id table-name

Parameters:
    target-name
        profile name (used for detect database connection properties)
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    table-name
        target table name
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

if [ $# -ne 4 ]
then
    usage
    exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

_OPT_TARGET_NAME="$1"
_OPT_BATCH_ID="$2"
_OPT_FLOW_ID="$3"
_OPT_TABLE_NAME="$4"

import "$_TG_ROOT/conf/env.sh"
import "$_TG_ROOT/libexec/validate-env.sh"
import "$_TG_ROOT/libexec/configure-hadoop-cmd.sh"

_TG_CLASSPATH="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"
import "$_TG_ROOT/libexec/configure-classpath.sh"

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"
LOGFILE_BASENAME="build-cache"
CLASS_NAME="com.asakusafw.bulkloader.cache.BuildCache"

export HADOOP_CLASSPATH="$_TG_CLASSPATH"
export HADOOP_USER_CLASSPATH_FIRST=true
HADOOP_OPTS="$HADOOP_OPTS -Dasakusa.home=$ASAKUSA_HOME"
HADOOP_OPTS="$HADOOP_OPTS -Dlogfile.basename=$LOGFILE_BASENAME"
export HADOOP_OPTS

cd

echo "Starting build-cache:"
echo "  Target Name: $_OPT_TARGET_NAME"
echo "     Batch ID: $_OPT_BATCH_ID"
echo "      Flow ID: $_OPT_FLOW_ID"
echo "   Table Name: $_OPT_TABLE_NAME"
"$HADOOP_CMD" \
    "$CLASS_NAME" \
    "$_OPT_TARGET_NAME" \
    "$_OPT_BATCH_ID" \
    "$_OPT_FLOW_ID" \
    "$_OPT_TABLE_NAME"

_TGC_RET=$?
if [ $_TGC_RET -ne 0 ]
then
    echo "build-cache failed with exit code: $_TGC_RET" 1>&2
    echo "   Target Name: $_OPT_TARGET_NAME" 1>&2
    echo "      Batch ID: $_OPT_BATCH_ID" 1>&2
    echo "       Flow ID: $_OPT_FLOW_ID" 1>&2
    echo "    Table Name: $_OPT_TABLE_NAME" 1>&2
    exit $_TGC_RET
fi
