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


REMOTE_HADOOP_JOB_RUN_SH=/home/asakusa/asakusa/experimental/bin/hadoop_job_run.sh
SSHPATH=/usr/bin/ssh
HCHOST=localhost
HCUSER=asakusa

echo SSHPATH=$SSHPATH
echo HCHOST=$HCHOST
echo HCUSER=$HCUSER

RUNCMD="$SSHPATH -l $HCUSER $HCHOST . .bash_profile;$REMOTE_HADOOP_JOB_RUN_SH $*"
echo $RUNCMD

$SSHPATH -l $HCUSER $HCHOST ". .bash_profile;$REMOTE_HADOOP_JOB_RUN_SH $*"
exit $?
