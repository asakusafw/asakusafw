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

if [ "$YS_PATH_SEPARATOR" = "" ]
then
    _YS_PATH_SEPARATOR=':'
else 
    _YS_PATH_SEPARATOR="$YS_PATH_SEPARATOR"
fi

if [ -d "$_YS_ROOT/plugin" ]
then
    for f in $(ls "$_YS_ROOT/plugin/")
    do
        if [ "$_YS_PLUGIN" = "" ]
        then
            _YS_PLUGIN="$_YS_ROOT/plugin/$f"
        else
            _YS_PLUGIN="${_YS_PLUGIN}${_YS_PATH_SEPARATOR}${_YS_ROOT}/plugin/$f"
        fi
    done
fi
