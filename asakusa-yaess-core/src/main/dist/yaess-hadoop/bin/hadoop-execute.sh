#!/bin/sh

usage() {
    cat 1>&2 <<EOF
YAESS Hadoop Command Line

Usage:
    $0 class-name batch-id flow-id execution-id batch-arguments [direct-arguments...]

Parameters:
    class-name
        Fully qualified class name of program entry
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

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
    exit 1
fi

_OPT_CLASS_NAME="$1"
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

_YS_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_YS_CORE_LIB_DIR="$ASAKUSA_HOME/core/lib"
_YS_EXT_LIB_DIR="$ASAKUSA_HOME/ext/lib"
_YS_RUNTIME_LIB="$_YS_CORE_LIB_DIR/asakusa-runtime.jar"
_YS_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_YS_APP_LIB="$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"

_YS_LIBJAR="$_YS_APP_LIB"

if [ -d "$_YS_CORE_LIB_DIR" ]
then
    for f in $(ls "$_YS_CORE_LIB_DIR")
    do
        _YS_LIBJAR="$_YS_LIBJAR,$_YS_CORE_LIB_DIR/$f"
    done
fi

if [ -d "$_YS_EXT_LIB_DIR" ]
then
    for f in $(ls "$_YS_EXT_LIB_DIR")
    do
        _YS_LIBJAR="$_YS_LIBJAR,$_YS_EXT_LIB_DIR/$f"
    done
fi

echo "Starting Asakusa Hadoop:"
echo "   App Library: $_YS_APP_LIB"
echo "      Batch ID: $_OPT_BATCH_ID"
echo "       Flow ID: $_OPT_FLOW_ID"
echo "  Execution ID: $_OPT_EXECUTION_ID"
echo "         Class: $_OPT_CLASS_NAME"

"$HADOOP_HOME/bin/hadoop" jar \
    "$_YS_RUNTIME_LIB" \
    "$_YS_TOOL_LAUNCHER" \
    "$_OPT_CLASS_NAME" \
    -conf "$_YS_PLUGIN_CONF" \
    -libjars "$_YS_LIBJAR" \
    -D "com.asakusafw.user=$USER" \
    -D "com.asakusafw.executionId=$_OPT_EXECUTION_ID" \
    -D "com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" \
    $YS_HADOOP_PROPERTIES \
    "$@"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then
    echo "YAESS Hadoop failed with exit code: $_YS_RET" 1>&2
    echo "  Runtime Lib: $_YS_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_YS_TOOL_LAUNCHER"  1>&2
    echo "  Stage Class: $_OPT_CLASS_NAME" 1>&2
    echo "Configuration: -conf $_YS_PLUGIN_CONF"  1>&2
    echo "    Libraries: -libjars $_YS_LIBJAR"  1>&2
    echo "Built-in Prop: -D com.asakusafw.user=$USER" 1>&2
    echo "Built-in Prop: -D com.asakusafw.executionId=$_OPT_EXECUTION_ID" 1>&2
    echo "Built-in Prop: -D com.asakusafw.batchArgs=$_OPT_BATCH_ARGUMENTS" 1>&2
    echo "Defined Props: $YS_HADOOP_PROPERTIES" 1>&2
    echo "  Extra Props: $@" 1>&2
    exit $_YS_RET
fi
