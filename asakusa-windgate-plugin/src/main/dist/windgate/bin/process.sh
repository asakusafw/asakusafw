#!/bin/sh

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

if [ $# -ne 7 ]; then
    usage
    exit 1
fi

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
    exit 1
fi

_OPT_PROFILE="$1"
_OPT_SESSION_KIND="$2"
_OPT_SCRIPT="$3"
_OPT_BATCH_ID="$4"
_OPT_FLOW_ID="$5"
_OPT_EXECUTION_ID="$6"
_OPT_ARGUMENTS="$7"

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

_WG_PLUGIN=""
if [ -d "$_WG_ROOT/plugin" ]
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

_WG_CLASSPATH="$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/lib/jobflow-${_OPT_FLOW_ID}.jar"
_WG_CLASSPATH="${_WG_CLASSPATH}${_WG_CLASSPATH_DELIMITER}${_WG_ROOT}/conf"
if [ -d "$_WG_ROOT/lib" ]
then
    for f in $(ls "$_WG_ROOT/lib/")
    do
        _WG_CLASSPATH="${_WG_CLASSPATH}${_WG_CLASSPATH_DELIMITER}${_WG_ROOT}/lib/$f"
    done
fi
if [ -d "$ASAKUSA_HOME/core/lib" ]
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

_WG_CLASS="com.asakusafw.windgate.bootstrap.WindGate"

echo "Starting WindGate"
echo "  -classpath $_WG_CLASSPATH"
echo "  -mode $_WG_MODE"
echo "  -profile $_WG_PROFILE"
echo "  -script $_WG_SCRIPT"
echo "  -session $_WG_SESSION"
echo "  -plugin $_WG_PLUGIN"
echo "  -arguments $_WG_ARGUMENTS"

export WINDGATE_PROFILE="$_OPT_PROFILE"

if [ -d "$HADOOP_HOME" ]
then
    export HADOOP_CLASSPATH="$_WG_CLASSPATH"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.batchId=${_OPT_BATCH_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.flowId=${_OPT_FLOW_ID:-(unknown)}"
    HADOOP_OPTS="$HADOOP_OPTS -Dcom.asakusafw.windgate.log.executionId=${_OPT_EXECUTION_ID:-(unknown)}"
    export HADOOP_OPTS
    "$HADOOP_HOME/bin/hadoop" \
        "$_WG_CLASS" \
        -mode "$_WG_MODE" \
        -profile "$_WG_PROFILE" \
        -script "$_WG_SCRIPT" \
        -session "$_WG_SESSION" \
        -plugin "$_WG_PLUGIN" \
        -arguments "$_WG_ARGUMENTS"
else
    java \
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
	echo "WindGate failed with exit code: $_WG_RET" 1>&2
	echo "  -classpath $_WG_CLASSPATH" 1>&2
	echo "  -mode $_WG_MODE" 1>&2
	echo "  -profile $_WG_PROFILE" 1>&2
	echo "  -script $_WG_SCRIPT" 1>&2
	echo "  -session $_WG_SESSION" 1>&2
	echo "  -plugin $_WG_PLUGIN" 1>&2
	echo "  -arguments $_WG_ARGUMENTS" 1>&2
	exit $_WG_RET
fi

