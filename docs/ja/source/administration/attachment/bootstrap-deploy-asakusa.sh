#!/bin/bash
set -e

### Change it ###
_bucket=<sample-bucket>
_asakusafw_version=0.4.0
_batchapp_project_name=directio-example
_batchapp_version=1.0-SNAPSHOT
### Change it (END) ###

_asakusafw_filename="asakusafw-${_asakusafw_version}-prod-directio.tar.gz"
_asakusafw_path="asakusafw/${_asakusafw_filename}"

_batchapp_filename="${_batchapp_project_name}-batchapps-${_batchapp_version}.jar"
_batchapp_path="batchapps/${_batchapp_filename}"

_asakusa_resources_path="conf/asakusa-resources.xml"

# Deploy asakusafw
hadoop fs -get "s3://${_bucket}/${_asakusafw_path}" .
mkdir -p /home/hadoop/asakusa
tar -C /home/hadoop/asakusa -xzf ${_asakusafw_filename}
find /home/hadoop/asakusa -name "*.sh" | xargs chmod u+x

# Deploy batchapp
hadoop fs -get "s3://${_bucket}/${_batchapp_path}" .
cp ${_batchapp_filename} /home/hadoop/asakusa/batchapps
cd /home/hadoop/asakusa/batchapps
jar -xf /home/hadoop/asakusa/batchapps/${_batchapp_filename}

# Deploy configuration
hadoop fs -get "s3://${_bucket}/${_asakusa_resources_path}" .
cp asakusa-resources.xml /home/hadoop/asakusa/core/conf

cd $HOME

