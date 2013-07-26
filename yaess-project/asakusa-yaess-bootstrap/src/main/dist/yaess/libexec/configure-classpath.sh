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

if [ "$YS_PATH_SEPARATOR" = "" ]
then
    _YS_PATH_SEPARATOR=':'
else
    _YS_PATH_SEPARATOR="$YS_PATH_SEPARATOR"
fi

if [ -d "$_YS_ROOT/conf" ]
then
        if [ "$_YS_CLASSPATH" = "" ]
        then
            _YS_CLASSPATH="$_YS_ROOT/conf"
        else
            _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}$_YS_ROOT/conf"
        fi
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

if [ -d "$ASAKUSA_HOME/core/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/core/lib/")
    do
        _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}${ASAKUSA_HOME}/core/lib/$f"
    done
fi

if [ "$_OPT_BATCH_ID" != "" -a -d "$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/usr/lib" ]
then
    _OPT_LIBRARIES_PATH="$ASAKUSA_HOME/batchapps/$_OPT_BATCH_ID/usr/lib"
    for f in $(ls "$_OPT_LIBRARIES_PATH")
    do
        if [ -f "$_OPT_LIBRARIES_PATH/$f" ]
        then
            _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}${_OPT_LIBRARIES_PATH}/$f"
        fi
    done
fi

if [ -d "$ASAKUSA_HOME/ext/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/ext/lib/")
    do
        _YS_CLASSPATH="${_YS_CLASSPATH}${_YS_PATH_SEPARATOR}${ASAKUSA_HOME}/ext/lib/$f"
    done
fi
