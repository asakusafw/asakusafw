#!/bin/bash
#
# Copyright 2011-2017 Asakusa Framework Team.
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

_YS_GENERATE_EXECUTION_ID="<generate>"

usage() {
    cat 1>&2 <<EOF
YAESS - A portable Asakusa workflow processor

Usage:
    yaess-phase.sh batch-id flow-id phase-name execution-id \\
        [-A <key>=<value> [-A <key>=<value>  [...]]] \\
        [-D <key>=<value> [-D <key>=<value>  [...]]] \\
        [-V <key>=<value> [-V <key>=<value>  [...]]] \\
        [-X-<name> <value> [-X-<name> <value> [...]]]

Parameters:
    batch-id
        The ID of execution target batch
    flow-id
        The ID of execution target jobflow
    phase-name
        phase name of current, one of:
            setup initialize import prologue main
            epilogue export finalize cleanup
    execution-id
        Unique ID of jobflow execution
        If "$_YS_GENERATE_EXECUTION_ID" is specified, the execution ID is 
        generated automatically (experimental feature)
    -A <key>=<value>
        argument for this execution
    -D <key>=<value>
        definition for this execution
    -V <key>=<value>
        custom variables (inherits current environment variables)
    -X-<name> <value>
        argument for <name> extension

Definitions:
    -D profile=<custom-profile-name>
        uses a custom profile: "conf/<custom-profile-name>.properties"
        instead of "conf/yaess.properties"
    -D dryRun, -D dryRun=true
        executes each stage as simulation mode
    -D verifyApplication=false
        turns off verifying library consistency of each stage

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

if [ $# -lt 4 ]; then
    usage
    exit 1
fi

_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
shift
_OPT_PHASE_NAME="$1"
shift
_OPT_EXECUTION_ID="$1"
shift

_YS_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

import "$_YS_ROOT/conf/env.sh"
import "$_YS_ROOT/libexec/validate-env.sh"
import "$_YS_ROOT/libexec/configure-classpath.sh"
import "$_YS_ROOT/libexec/configure-plugin.sh"
import "$ASAKUSA_HOME/core/libexec/configure-java.sh"

if [ "$_OPT_EXECUTION_ID" = "$_YS_GENERATE_EXECUTION_ID" ]
then
    _OPT_EXECUTION_ID=$("$_YS_ROOT/bin/yaess-execution-id.sh" "$_OPT_BATCH_ID" "$_OPT_FLOW_ID" "$@")
    _YS_GEN_RET=$?
    if [ $_YS_GEN_RET -ne 0 ]
    then
        echo 1>&2 << __EOF__
YAESS Failed (to generate execution ID) with exit code: $_YS_GEN_RET
    Batch ID: $_OPT_BATCH_ID
     Flow ID: $_OPT_FLOW_ID
    Plug-ins: $_YS_PLUGIN
   Arguments: $@
  Properties: $YAESS_OPTS
__EOF__

        echo "Finished: FAILURE"
        exit $_YS_GEN_RET
    fi
fi

_YS_PROFILE="$_YS_ROOT/conf/yaess.properties"
_YS_SCRIPT="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/etc/yaess-script.properties"
_YS_CLASS="com.asakusafw.yaess.bootstrap.Yaess"

cat << __EOF__
Starting YAESS
     Profile: $_YS_PROFILE
      Script: $_YS_SCRIPT
    Batch ID: $_OPT_BATCH_ID
     Flow ID: $_OPT_FLOW_ID
  Phase Name: $_OPT_PHASE_NAME
Execution ID: $_OPT_EXECUTION_ID
    Plug-ins: $_YS_PLUGIN
   Classpath: $_YS_CLASSPATH
  Main Class: $_YS_CLASS
   Arguments: $@
  Properties: $YAESS_OPTS
__EOF__

"$_JAVA_CMD" $JAVA_OPTS \
    $YAESS_OPTS \
    "-Dcom.asakusafw.yaess.log.batchId=$_OPT_BATCH_ID" \
    -classpath "$_YS_CLASSPATH" \
    "$_YS_CLASS" \
    -profile "$_YS_PROFILE" \
    -script "$_YS_SCRIPT" \
    -batch "$_OPT_BATCH_ID" \
    -flow "$_OPT_FLOW_ID" \
    -phase "$_OPT_PHASE_NAME" \
    -execution "$_OPT_EXECUTION_ID" \
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
     Flow ID: $_OPT_FLOW_ID
  Phase Name: $_OPT_PHASE_NAME
Execution ID: $_OPT_EXECUTION_ID
    Plug-ins: $_YS_PLUGIN
   Arguments: $@
  Properties: $YAESS_OPTS
__EOF__

    echo "Finished: FAILURE"
    exit $_YS_RET
fi

echo "Finished: SUCCESS"
