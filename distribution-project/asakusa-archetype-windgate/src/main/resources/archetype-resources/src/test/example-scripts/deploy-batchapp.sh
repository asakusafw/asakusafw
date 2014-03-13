#!/bin/sh -e
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

if [ ! -r "target/batchc/example.summarizeSales" ]
then
    echo 'Compiled Batch Application does not found.' 1>&2
    echo 'Move current directory to project root and run "mvn clean package".' 1>&2
    exit 1
fi

if [ "$ASAKUSA_BATCHAPPS_HOME" = "" ]
then
    export ASAKUSA_BATCHAPPS_HOME="$ASAKUSA_HOME/batchapps"
fi

rm "$ASAKUSA_BATCHAPPS_HOME"/* -fr
cp -pr target/batchc/* "$ASAKUSA_BATCHAPPS_HOME"

mkdir -p /tmp/windgate-$USER
rm /tmp/windgate-$USER/* -rf
cp -a src/test/example-dataset/* /tmp/windgate-$USER/

echo 'SUCCESS. Deployed sample Batch Application and test data set.'
exit 0

