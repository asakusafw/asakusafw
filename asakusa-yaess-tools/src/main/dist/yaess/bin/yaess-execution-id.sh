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
YAESS Execution ID Generator

Usage:
    $0 batch-id flow-id [-A <key>=<value>]*

Parameters:
    batch-id
        The ID of execution target batch
    flow-id
        The ID of execution target jobflow
    -A <key>=<value>
        argument for this execution
EOF
}

if [ $# -lt 2 ]; then
    usage
    exit 1
fi

_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
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

_YS_CLASS="com.asakusafw.yaess.tools.GenerateExecutionId"

java \
    -classpath "$_YS_CLASSPATH" \
    "$_YS_CLASS" \
    -batch "$_OPT_BATCH_ID" \
    -flow "$_OPT_FLOW_ID" \
    "$@"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then
    echo "YAESS Failed with exit code: $_YS_RET" 1>&2
    echo "   Classpath: $_YS_CLASSPATH" 1>&2
    echo "    Batch ID: $_OPT_BATCH_ID" 1>&2
    echo "     Flow ID: $_OPT_FLOW_ID" 1>&2
    echo "   Arguments: $@" 1>&2
    echo "Finished: FAILURE"
    exit $_YS_RET
fi
