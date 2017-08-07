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

# Defines a variable either _HADOOP_CMD or _HADOOP_EMBED_CLASSPATH.
#
# input variables:
#   ASAKUSA_HOME
#   HADOOP_CMD (optional)
#   HADOOP_HOME (optional)
#   HADOOP_CONF_DIR (optional)
# 
# output variables:
#   _HADOOP_CMD (optional)
#   _HADOOP_EMBED_CLASSPATH (optional, array)
#   _HADOOP_EMBED_LOGGING_CLASSPATH (optional, array)


if [ -d "${ASAKUSA_HOME:-/-}/hadoop/lib" ]
then
    unset _HADOOP_CMD
    _HADOOP_EMBED_CLASSPATH=()
    _HADOOP_EMBED_LOGGING_CLASSPATH=()
    if [ "$HADOOP_CONF" != "" -a -d "$HADOOP_CONF_DIR" ]
    then
        _HADOOP_EMBED_CLASSPATH+=("$HADOOP_CONF_DIR")
    elif [ -d "$ASAKUSA_HOME/hadoop/conf" ]
    then
        _HADOOP_EMBED_CLASSPATH+=("$ASAKUSA_HOME/hadoop/conf")
    fi
    for f in $(ls "$ASAKUSA_HOME/hadoop/lib")
    do
        _HADOOP_EMBED_CLASSPATH+=("$ASAKUSA_HOME/hadoop/lib/$f")
    done
    for f in $(ls "$ASAKUSA_HOME/hadoop/lib/logging")
    do
        _HADOOP_EMBED_LOGGING_CLASSPATH+=("$ASAKUSA_HOME/hadoop/lib/logging/$f")
    done
else
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
                echo "\$HADOOP_CMD is not defined" 1>&2
                exit 1
            fi
        fi
    fi

    _HADOOP_CMD="$HADOOP_CMD"
    unset _HADOOP_EMBED_CLASSPATH
    unset _HADOOP_EMBED_LOGGING_CLASSPATH

    if [ ! -x "$_HADOOP_CMD" ]
    then
        echo "$_HADOOP_CMD is not executable" 1>&2
        exit 1
    fi
fi
