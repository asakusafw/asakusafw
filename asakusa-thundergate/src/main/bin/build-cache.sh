#!/bin/bash
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

if [ $# -ne 4 ]
then
    usage
    exit 1
fi

_OPT_TARGET_NAME="$1"
_OPT_BATCH_ID="$2"
_OPT_FLOW_ID="$3"
_OPT_TABLE_NAME="$4"

. ~/.bulkloader_db_profile

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
    exit 1
fi

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="build-cache"
CLASS_NAME="com.asakusafw.bulkloader.cache.BuildCache"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh "$_OPT_BATCH_ID" "$_OPT_FLOW_ID"
cd "$ASAKUSA_HOME"

echo "Starting build-cache:"
echo "  Target Name: $_OPT_TARGET_NAME"
echo "     Batch ID: $_OPT_BATCH_ID"
echo "      Flow ID: $_OPT_FLOW_ID"
echo "   Table Name: $_OPT_TABLE_NAME"

"$JAVA_HOME"/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
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
