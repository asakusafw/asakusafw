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

if [ "$WG_CLASSPATH_DELIMITER" = "" ]
then
    _WG_CLASSPATH_DELIMITER=':'
else 
    _WG_CLASSPATH_DELIMITER=$WG_CLASSPATH_DELIMITER
fi

if [ -e "$_WG_ROOT/plugin" ]
then
    for f in $(ls "$_WG_ROOT/plugin/")
    do
        if [ "$_WG_PLUGIN" = "" ]
        then
            _WG_PLUGIN="$_WG_ROOT/plugin/$f"
        else
            _WG_PLUGIN="${_WG_PLUGIN}${_WG_CLASSPATH_DELIMITER}${_WG_ROOT}/plugin/$f"
        fi
    done
fi
