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

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

if [ "$JAVA_CMD" != "" -a -x "$JAVA_CMD" ]
then
    :
elif [ -x "$JAVA_HOME/bin/java" ]
then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD=$(which java)
fi

if [ ! -x "$JAVA_CMD" ]
then
    echo 'valid $JAVA_CMD is not defined' 1>&2
    exit 1
fi

exec java $JAVA_OPTS -Dcli.name=workflow.sh -jar "$_ROOT/lib/asakusa-workflow.jar" "$@"
