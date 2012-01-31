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


cd
usage() {
    cat 1>&2 <<EOF
Aborts Direct I/O Transaction

Usage:
    $0 execuion-id

Parameters:
    execution-id
        execution ID of target execution
EOF
}

if [ $# -eq 1 ]
then
    _OPT_EXECUTION_ID="$1"
else
    usage
    exit 1
fi

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME is not defined' 1>&2
    exit 1
fi

if [ "$HADOOP_HOME" = "" ]
then
    echo '$HADOOP_HOME is not defined' 1>&2
    exit 1
fi

_DIO_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_DIO_CORE_LIB_DIR="$ASAKUSA_HOME/core/lib"
_DIO_EXT_LIB_DIR="$ASAKUSA_HOME/ext/lib"
_DIO_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"
_DIO_RUNTIME_LIB="$_DIO_CORE_LIB_DIR/asakusa-runtime.jar"
_DIO_CLASS_NAME="com.asakusafw.runtime.directio.hadoop.DirectIoAbortTransaction"

if [ -d "$_DIO_CORE_LIB_DIR" ]
then
    for f in $(ls "$_DIO_CORE_LIB_DIR")
    do
        _DIO_TMP="$_DIO_CORE_LIB_DIR/$f"
        if [ "$_DIO_LIBJAR" = "" ]
            _DIO_LIBJAR="$_DIO_TMP"
        then
            _DIO_LIBJAR="$_DIO_LIBJAR,$_DIO_TMP"
        fi
    done
fi

if [ -d "$_DIO_EXT_LIB_DIR" ]
then
    for f in $(ls "$_DIO_EXT_LIB_DIR")
    do
        _DIO_TMP="$_DIO_EXT_LIB_DIR/$f"
        if [ "$_DIO_LIBJAR" = "" ]
            _DIO_LIBJAR="$_DIO_TMP"
        then
            _DIO_LIBJAR="$_DIO_LIBJAR,$_DIO_TMP"
        fi
    done
fi

echo "Starting Abort Direct I/O Transaction:"
echo "   App Library: $_DIO_APP_LIB"
echo "         Class: $_DIO_CLASS_NAME"
echo "  Execution ID: $_OPT_EXECUTION_ID"

"$HADOOP_HOME/bin/hadoop" jar \
    "$_DIO_RUNTIME_LIB" \
    "$_DIO_TOOL_LAUNCHER" \
    "$_DIO_CLASS_NAME" \
    -conf "$_DIO_PLUGIN_CONF" \
    -libjars "$_DIO_LIBJAR" \
    "$_OPT_EXECUTION_ID"

_DIO_RET=$?
if [ $_DIO_RET -ne 0 ]
then
    echo "Abort Direct I/O Transaction failed with exit code: $_DIO_RET" 1>&2
    echo " Execution ID: $_OPT_EXECUTION_ID"  1>&2
    echo "  Runtime Lib: $_DIO_RUNTIME_LIB"  1>&2
    echo "     Launcher: $_DIO_TOOL_LAUNCHER"  1>&2
    echo "  Stage Class: $_DIO_CLASS_NAME" 1>&2
    echo "Configuration: -conf $_DIO_PLUGIN_CONF"  1>&2
    echo "    Libraries: -libjars $_DIO_LIBJAR"  1>&2
    exit $_DIO_RET
fi
