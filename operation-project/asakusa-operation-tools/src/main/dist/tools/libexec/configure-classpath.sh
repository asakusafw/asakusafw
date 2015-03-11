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

if [ "$TL_PATH_SEPARATOR" = "" ]
then
    _TL_PATH_SEPARATOR=':'
else 
    _TL_PATH_SEPARATOR="$TL_PATH_SEPARATOR"
fi

_TL_CLASSPATH=""
if [ -d "$_TL_ROOT/conf" ]
then
    _TL_CLASSPATH="$_TL_ROOT/conf"
fi
if [ -d "$_TL_ROOT/lib" ]
then
    for f in $(ls "$_TL_ROOT/lib/")
    do
        if [ "$_TL_CLASSPATH" = "" ]
        then
            _TL_CLASSPATH="${_YS_ROOT}/lib/$f"
        else
            _TL_CLASSPATH="${_TL_CLASSPATH}${_TL_PATH_SEPARATOR}${_TL_ROOT}/lib/$f"
        fi
    done
fi
