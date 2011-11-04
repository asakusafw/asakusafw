#!/bin/sh

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
if [ -e "$_YS_ROOT/conf/env.sh" ]
then
    . "$_YS_ROOT/conf/env.sh"
fi

# Move to home directory
cd

echo "Starting Asakusa Hadoop CleanUp:"
echo "  Working Directory: $_OPT_WORKING_DIRECTORY"

"$HADOOP_HOME/bin/hadoop" fs \
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
