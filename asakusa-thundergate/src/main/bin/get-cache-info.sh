#!/bin/bash

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

if [ $# -ne 4 ]; then
  usage
  exit 1
fi

_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift

. ~/.bulkloader_hc_profile 1>&2
export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader 1>&2

LOGFILE_BASENAME="get-cache-info"
CLASS_NAME="com.asakusafw.bulkloader.cache.GetCacheInfoRemote"
USER_NAME="$(whoami)"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-hc.sh "$_BATCH_ID" "$_FLOW_ID"

cd "$ASAKUSA_HOME" 1>&2

$JAVA_HOME/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID" \
    "$USER_NAME"
rc=$?
exit $rc
