#!/bin/bash
set -e

### Change it ###
_bucket=[sample-bucket]

export USER="hadoop"
export HOME="/home/$USER"
export ASAKUSA_HOME="$HOME/asakusa"

_asakusafw_filename="asakusafw-*.tar.gz"
_asakusafw_path="asakusafw/${_asakusafw_filename}"
_batchapp_filename="*-batchapps*.jar"
_batchapp_path="batchapps/${_batchapp_filename}"
_asakusa_resources_filename="asakusa-resources.xml"
_asakusa_resources_path="conf/${_asakusa_resources_filename}"

# Deploy asakusafw
hadoop fs -get "s3://${_bucket}/${_asakusafw_path}" ${HOME}
mkdir -p ${ASAKUSA_HOME}
tar -C ${ASAKUSA_HOME} -xzf ${_asakusafw_filename}
find ${ASAKUSA_HOME} -name "*.sh" | xargs chmod u+x

# Deploy batchapp
hadoop fs -get "s3://${_bucket}/${_batchapp_path}" ${ASAKUSA_HOME}/batchapps
cd ${ASAKUSA_HOME}/batchapps
for f in ${_batchapp_filename}
do
    jar xf "$f"
done
cd -

# Deploy configuration
mv ${ASAKUSA_HOME}/core/conf/${_asakusa_resources_filename} ${ASAKUSA_HOME}/core/conf/${_asakusa_resources_filename}.orig
hadoop fs -get "s3://${_bucket}/${_asakusa_resources_path}" ${ASAKUSA_HOME}/core/conf/${_asakusa_resources_filename}

