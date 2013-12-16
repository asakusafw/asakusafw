#!/bin/bash

. ~/.bash_profile
export USER="hadoop"
export HOME="/home/$USER"
export ASAKUSA_HOME="$HOME/asakusa"
export _CMD_LOG="$ASAKUSA_HOME/job-step.log"

# Run YAESS
echo "$0 $*" >> $_CMD_LOG
$ASAKUSA_HOME/yaess/bin/yaess-batch.sh "$@" 2>&1 | tee -a $_CMD_LOG

exit "${PIPESTATUS[0]}"
