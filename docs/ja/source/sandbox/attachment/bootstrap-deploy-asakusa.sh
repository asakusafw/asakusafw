#!/bin/bash
set -e

### Change it ###
_bucket=[sample-bucket]

export USER="hadoop"
export HOME="/home/$USER"
export ASAKUSA_HOME="$HOME/asakusa"

_asakusafw_filename="asakusafw-*.tar.gz"
_asakusafw_path="asakusafw/${_asakusafw_filename}"

# Deploy asakusafw
mkdir -p ${ASAKUSA_HOME}
hadoop fs -get "s3://${_bucket}/${_asakusafw_path}" ${ASAKUSA_HOME}
cd ${ASAKUSA_HOME}
tar -xzf ${_asakusafw_filename}
find ${ASAKUSA_HOME} -name "*.sh" | xargs chmod u+x

