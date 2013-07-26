#!/bin/sh
#
# Copyright 2011-2013 Asakusa Framework Team.
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
Asakusa TestDriver Hadoop Kick-script

Usage:
    $0 jar-file class-name batch-id [direct-arguments...]

Parameters:
    jar-file
        Full path of execution library
    class-name
        Fully qualified class name of program entry
    batch-id
        batch ID of current execution
    direct-arguments...
        Direct arguments for Hadoop
EOF
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

if [ $# -lt 3 ]
then
    echo "$@" 1>&2
    usage
    exit 1
fi

_OPT_APP_LIB="$1"
shift
_OPT_CLASS_NAME="$1"
shift
_OPT_BATCH_ID="$1"
shift

_TD_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_TD_ROOT/conf/env.sh"
import "$_TD_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_TD_TOOL_LAUNCHER="com.asakusafw.runtime.stage.ToolLauncher"
_TD_RUNTIME_LIB="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
_TD_PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"

_TD_LIBJARS="$_OPT_APP_LIB"
import "$_TD_ROOT/libexec/configure-libjars.sh"
import "$_TD_ROOT/libexec/configure-hadoop-cmd.sh"

cat << __EOF__
Starting TestDriver Hadoop Job:
 Hadoop Command: $HADOOP_CMD
    App Library: $_OPT_APP_LIB
   Execution ID: $_OPT_EXECUTION_ID
          Class: $_OPT_CLASS_NAME
  All Libraries: $_TD_LIBJARS
  Defined Props: $TD_HADOOP_PROPERTIES
    Extra Props: $*
__EOF__

"$HADOOP_CMD" jar \
    "$_TD_RUNTIME_LIB" \
    "$_TD_TOOL_LAUNCHER" \
    "$_OPT_CLASS_NAME" \
    -conf "$_TD_PLUGIN_CONF" \
    -libjars "$_TD_LIBJARS" \
    $TD_HADOOP_PROPERTIES \
    "$@"

_TD_RET=$?
if [ $_TD_RET -ne 0 ]
then
    cat 1>&2 << __EOF__
TestDriver Hadoop Job failed with exit code: $_TD_RET
   Runtime Lib: $_TD_RUNTIME_LIB
      Launcher: $_TD_TOOL_LAUNCHER
   Stage Class: $_OPT_CLASS_NAME
 Configuration: -conf $_TD_PLUGIN_CONF
     Libraries: -libjars $_TD_LIBJARS
 Defined Props: $TD_HADOOP_PROPERTIES
   Extra Props: $*
__EOF__
    exit $_TD_RET
fi
