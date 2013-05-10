#!/bin/sh -e
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

if [ ! -r "target/batchc/example.summarizeSales" ]
then
    echo 'Compiled Batch Application does not found.' 1>&2
    echo 'Move current directory to project root and run "mvn clean package".' 1>&2
    exit 1
fi

rm "$ASAKUSA_HOME"/batchapps/* -fr
cp -pr target/batchc/* "$ASAKUSA_HOME"/batchapps

PROJECT_PATH=$PWD
$ASAKUSA_HOME/directio/bin/delete-file.sh -r "/" "*"
cd $HOME
hadoop fs -put "$PROJECT_PATH"/src/test/example-dataset/master target/testing/directio/master
hadoop fs -put "$PROJECT_PATH"/src/test/example-dataset/sales target/testing/directio/sales
$ASAKUSA_HOME/directio/bin/list-file.sh "/" "**"

echo 'SUCCESS. Deployed sample Batch Application and test data set.'
exit 0

