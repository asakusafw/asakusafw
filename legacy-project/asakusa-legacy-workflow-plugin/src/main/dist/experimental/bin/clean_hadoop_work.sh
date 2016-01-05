#!/bin/bash
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


_OUTPUT_PATH=$1
_BATCH_ID=$2
_FLOW_ID=$3
_EXECUTION_ID=$4
_BATCH_ARGS=$5

# please validate $_OUTPUT_PATH 

echo "OUTPUT_PATH=$_OUTPUT_PATH"
echo "BATCH_ID=$_BATCH_ID"
echo "FLOW_ID=$_FLOW_ID"
echo "EXECUTION_ID=$_EXECUTION_ID"
echo "BATCH_ARGS=$_BATCH_ARGS"
