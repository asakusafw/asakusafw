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

# Defines a variable either _JAVA_CMD.
#
# input variables:
#   JAVA_CMD (optional)
#   JAVA_HOME (optional)
# 
# output variables:
#   _JAVA_CMD

if [ "$JAVA_CMD" != "" -a -x "$JAVA_CMD" ]
then
    _JAVA_CMD="$JAVA_CMD"
elif [ -x "$JAVA_HOME/bin/java" ]
then
    _JAVA_CMD="$JAVA_HOME/bin/java"
else
    _JAVA_CMD=$(which java)
fi

if [ ! -x "$_JAVA_CMD" ]
then
    echo 'valid java command is not found' 1>&2
    exit 1
fi
