#!/bin/sh

cd
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

if [ "$WG_CLASSPATH_DELIMITER" = "" ]
then
    _WG_CLASSPATH_DELIMITER=':'
else 
    _WG_CLASSPATH_DELIMITER=$WG_CLASSPATH_DELIMITER
fi

_WG_ROOT="$(dirname $0)/.."
if [ -e "$_WG_ROOT/conf/env.sh" ]
then
    . "$_WG_ROOT/conf/env.sh"
fi

_WG_PROFILE="$_WG_ROOT/profile/${_OPT_PROFILE}.properties"
_WG_SESSION="$_OPT_EXECUTION_ID"

_WG_PLUGIN=""
if [ -e "$_WG_ROOT/plugin" ]
then
    for f in $(ls "$_WG_ROOT/plugin/")
    do
        if [ "$_WG_PLUGIN" = "" ]
        then
            _WG_PLUGIN="$_WG_ROOT/plugin/$f"
        else
            _WG_PLUGIN="${_WG_PLUGIN}${_WG_CLASSPATH_DELIMITER}${_WG_ROOT}/plugin/$f"
        fi
    done
fi

_WG_CLASSPATH="$_WG_ROOT/conf"
if [ -d "$_WG_ROOT/lib" ]
then
    for f in $(ls "$_WG_ROOT/lib/")
    do
        _WG_CLASSPATH="${_WG_CLASSPATH}${_WG_CLASSPATH_DELIMITER}${_WG_ROOT}/lib/$f"
    done
fi
if [ "$ASAKUSA_HOME" != "" -a -d "$ASAKUSA_HOME/core/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/core/lib/")
    do
        _WG_CLASSPATH="${_WG_CLASSPATH}${_WG_CLASSPATH_DELIMITER}${ASAKUSA_HOME}/core/lib/$f"
    done
fi
if [ -d "$ASAKUSA_HOME/ext/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/ext/lib/")
    do
        _WG_CLASSPATH="${_WG_CLASSPATH}${_WG_CLASSPATH_DELIMITER}${ASAKUSA_HOME}/ext/lib/$f"
    done
fi

export WINDGATE_PROFILE="$_OPT_PROFILE"

_WG_CLASS="com.asakusafw.windgate.bootstrap.WindGateAbort"

echo "Finalizing WindGate Session(s)"
echo "  -classpath $_WG_CLASSPATH"
echo "  -profile $_WG_PROFILE"
echo "  -session $_WG_SESSION"
echo "  -plugin $_WG_PLUGIN"
echo "  WINDGATE_OPTS=\"$WINDGATE_OPTS\""

if [ -d "$HADOOP_HOME" ]
then
    export HADOOP_CLASSPATH="$_WG_CLASSPATH"
    HADOOP_OPTS="$HADOOP_OPTS $WINDGATE_OPTS"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.batchId=${_OPT_BATCH_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.flowId=${_OPT_FLOW_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.executionId=${_OPT_EXECUTION_ID:-(unknown)}"
    export HADOOP_OPTS
    "$HADOOP_HOME/bin/hadoop" \
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
    echo "WindGateAbort failed with exit code: $_WG_RET" 1>&2
    echo "  -classpath $_WG_CLASSPATH" 1>&2
    echo "  -profile $_WG_PROFILE" 1>&2
    echo "  -session $_WG_SESSION" 1>&2
    echo "  -plugin $_WG_PLUGIN" 1>&2
    echo "  WINDGATE_OPTS=\"$WINDGATE_OPTS\"" 1>&2
    exit $_WG_RET
fi
