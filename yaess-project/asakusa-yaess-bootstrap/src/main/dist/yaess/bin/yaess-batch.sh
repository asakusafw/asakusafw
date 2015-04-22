#!/bin/sh
#
# Copyright 2011-2015 Asakusa Framework Team.
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
YAESS - A portable Asakusa workflow processor

Usage:
    yaess-batch.sh batch-id \\
        [-A <key>=<value> [-A <key>=<value>  [...]]] \\
        [-D <key>=<value> [-D <key>=<value>  [...]]]

Parameters:
    batch-id
        batch ID of current execution
    -A <key>=<value>
        argument for this execution
    -D <key>=<value>
        definition for this execution

Definitions:
    -D skipFlows=<flowId>[,<flowId>[,...]]
        ignores target jobflow execution
    -D serializeFlows , -D serializeFlows=true
        serializes each jobflow execution (for debug)
    -D dryRun, -D dryRun=true
        executes each stage as simulation mode
    -D verifyApplication=false
        turns off verifying library consistency of each stage

Examples:
    # run a batch "example.batch"
    yaess-batch.sh example.batch
    
    # run a batch "example.params" with {date="2011-03-31", code="123"}
    yaess-batch.sh example.params -A date=2011-03-31 -A code=123
    
    # run a batch "example.skip" except joblows "first" and "second"
    yaess-batch.sh example.skip -D skipFlows=first,second

Environment Variables:
    \$ASAKUSA_HOME
        The location where the Asakusa Framework is installed
    \$YAESS_OPTS
        Java VM options for YAESS execution
    \$YS_PATH_SEPARATOR
        Path separator character (default is ':')
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

if [ $# -lt 1 ]; then
    usage
    exit 1
fi

_OPT_BATCH_ID="$1"
shift

_YS_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_YS_ROOT/conf/env.sh"
import "$_YS_ROOT/libexec/validate-env.sh"
import "$_YS_ROOT/libexec/configure-classpath.sh"
import "$_YS_ROOT/libexec/configure-plugin.sh"

_YS_PROFILE="$_YS_ROOT/conf/yaess.properties"
_YS_SCRIPT="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/etc/yaess-script.properties"
_YS_CLASS="com.asakusafw.yaess.bootstrap.Yaess"

cat << __EOF__
Starting YAESS
     Profile: $_YS_PROFILE
      Script: $_YS_SCRIPT
    Batch ID: $_OPT_BATCH_ID
    Plug-ins: $_YS_PLUGIN
   Classpath: $_YS_CLASSPATH
  Main Class: $_YS_CLASS
   Arguments: $@
  Properties: $YAESS_OPTS
__EOF__

java \
    $YAESS_OPTS \
    "-Dcom.asakusafw.yaess.log.batchId=$_OPT_BATCH_ID" \
    -classpath "$_YS_CLASSPATH" \
    "$_YS_CLASS" \
    -profile "$_YS_PROFILE" \
    -script "$_YS_SCRIPT" \
    -batch "$_OPT_BATCH_ID" \
    -plugin "$_YS_PLUGIN" \
    "$@"

_YS_RET=$?
if [ $_YS_RET -ne 0 ]
then

    cat 1>&2 << __EOF__
YAESS Failed with exit code: $_YS_RET
   Classpath: $_YS_CLASSPATH
     Profile: $_YS_PROFILE
      Script: $_YS_SCRIPT
    Batch ID: $_OPT_BATCH_ID
    Plug-ins: $_YS_PLUGIN
   Arguments: $@
  Properties: $YAESS_OPTS
__EOF__

    echo "Finished: FAILURE"
    exit $_YS_RET
fi

echo "Finished: SUCCESS"
