#!/bin/sh

usage() {
    cat 1>&2 <<EOF
YAESS Explain

Usage:
    $0 yaess-script

Parameters:
    yaess-script
        Path to YAESS script.
        This script is ordinary on \$ASAKUSA_HOME/batchapps/<batch-id>/etc/yaess-script.properties
EOF
}

if [ $# -lt 1 ]; then
    usage
    exit 1
fi

_OPT_YAESS_SCRIPT="$1"
shift

_YS_ROOT="$(dirname $0)/.."
if [ -e "$_YS_ROOT/conf/env.sh" ]
then
    . "$_YS_ROOT/conf/env.sh"
fi

if [ "$YS_PATH_SEPARATOR" = "" ]
then
    _YS_PATH_SEPARATOR=':'
else 
    _YS_PATH_SEPARATOR="$YS_PATH_SEPARATOR"
fi

_YS_CLASSPATH=""
if [ -d "$_YS_ROOT/conf" ]
then
    _YS_CLASSPATH="$_YS_ROOT/conf"
fi
if [ -d "$_YS_ROOT/lib" ]
then
    for f in $(ls "$_YS_ROOT/lib/")
    do
        if [ "$_YS_CLASSPATH" = "" ]
        then
            _YS_CLASSPATH="${_YS_ROOT}/lib/$f"
        else
            _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}${_YS_ROOT}/lib/$f"
        fi
    done
fi
if [ -d "$_YS_ROOT/tools" ]
then
    for f in $(ls "$_YS_ROOT/tools/")
    do
        if [ "$_YS_CLASSPATH" = "" ]
        then
            _YS_CLASSPATH="${_YS_ROOT}/tools/$f"
        else
            _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}${_YS_ROOT}/tools/$f"
        fi
    done
fi

_YS_CLASS="com.asakusafw.yaess.tools.Explain"

java \
    -classpath "$_YS_CLASSPATH" \
    "$_YS_CLASS" \
    -script "$_OPT_YAESS_SCRIPT"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then
    echo "YAESS Failed with exit code: $_YS_RET" 1>&2
    echo "Classpath: $_YS_CLASSPATH" 1>&2
    echo "   Script: $_OPT_YAESS_SCRIPT" 1>&2
    echo "Finished: FAILURE"
    exit $_YS_RET
fi
