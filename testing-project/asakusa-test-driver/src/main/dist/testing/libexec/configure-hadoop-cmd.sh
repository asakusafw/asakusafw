#
# Copyright 2011-2014 Asakusa Framework Team.
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

if [ "$HADOOP_CMD" = "" ]
then
    if [ "$HADOOP_HOME" != "" ]
    then
        HADOOP_CMD="$HADOOP_HOME/bin/hadoop"
        unset HADOOP_HOME
    else
        HADOOP_CMD="$(which hadoop)"
        _RET=$?
        if [ $_RET -ne 0 ]
        then
            echo 'hadoop command is not found' 1>&2
            exit 1
        fi
    fi
fi

if [ ! -x "$HADOOP_CMD" ]
then
    echo "$HADOOP_CMD is not executable" 1>&2
    exit 1
fi
