#!/bin/sh -ex
#
# Copyright 2011-2012 Asakusa Framework Team.
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
$ASAKUSA_HOME/directio/bin/list-file.sh "/" "**"
$ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01
$ASAKUSA_HOME/directio/bin/list-file.sh "/" "**"

cd $HOME
hadoop fs -text target/testing/directio/result/category/result.csv
hadoop fs -text target/testing/directio/result/error/2011-04-01.csv

