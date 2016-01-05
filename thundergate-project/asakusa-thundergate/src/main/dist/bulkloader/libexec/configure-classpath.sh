#
# Copyright 2011-2016 Asakusa Framework Team.
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

if [ "$TG_PATH_SEPARATOR" = "" ]
then
    _TG_PATH_SEPARATOR=':'
else
    _TG_PATH_SEPARATOR="$TG_PATH_SEPARATOR"
fi

if [ -d "$_TG_ROOT/conf" ]
then
        if [ "$_TG_CLASSPATH" = "" ]
        then
            _TG_CLASSPATH="$_TG_ROOT/conf"
        else
            _TG_CLASSPATH="${_TG_CLASSPATH}${_TG_PATH_SEPARATOR}$_TG_ROOT/conf"
        fi
fi
if [ -d "$_TG_ROOT/lib" ]
then
    for f in $(ls "$_TG_ROOT/lib/")
    do
        if [ "$_TG_CLASSPATH" = "" ]
        then
            _TG_CLASSPATH="${_TG_ROOT}/lib/$f"
        else
            _TG_CLASSPATH="${_TG_CLASSPATH}${_TG_PATH_SEPARATOR}${_TG_ROOT}/lib/$f"
        fi
    done
fi

if [ "$ASAKUSA_HOME" != "" -a -d "$ASAKUSA_HOME/core/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/core/lib/")
    do
        if [ "$_TG_CLASSPATH" = "" ]
        then
            _TG_CLASSPATH="${ASAKUSA_HOME}/core/lib/$f"
        else
            _TG_CLASSPATH="${_TG_CLASSPATH}${_TG_PATH_SEPARATOR}${ASAKUSA_HOME}/core/lib/$f"
        fi
    done
fi

if [ "$_OPT_BATCH_ID" != "" -a -d "$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/usr/lib" ]
then
    _OPT_LIBRARIES_PATH="$ASAKUSA_BATCHAPPS_HOME/$_OPT_BATCH_ID/usr/lib"
    for f in $(ls "$_OPT_LIBRARIES_PATH")
    do
        if [ -f "$_OPT_LIBRARIES_PATH/$f" ]
        then
            if [ "$_TG_CLASSPATH" = "" ]
            then
                _TG_CLASSPATH="$_OPT_LIBRARIES_PATH/$f"
            else
                _TG_CLASSPATH="$_TG_CLASSPATH,$_OPT_LIBRARIES_PATH/$f"
            fi
        fi
    done
fi

if [ "$ASAKUSA_HOME" != "" -a -d "$ASAKUSA_HOME/ext/lib" ]
then
    for f in $(ls "$ASAKUSA_HOME/ext/lib/")
    do
        if [ "$_TG_CLASSPATH" = "" ]
        then
            _TG_CLASSPATH="${ASAKUSA_HOME}/ext/lib/$f"
        else
            _TG_CLASSPATH="${_TG_CLASSPATH}${_TG_PATH_SEPARATOR}${ASAKUSA_HOME}/ext/lib/$f"
        fi
    done
fi
