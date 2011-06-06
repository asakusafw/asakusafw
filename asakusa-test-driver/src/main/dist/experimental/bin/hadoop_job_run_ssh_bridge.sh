#!/bin/bash

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
