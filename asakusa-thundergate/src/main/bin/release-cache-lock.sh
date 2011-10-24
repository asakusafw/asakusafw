#!/bin/bash

usage() {
    cat << __EOF__
Releases locks for cache mechanism.

Usage:
    $0 target-name [execution-id]

Parameters:
    target-name
        profile name (used for detect database connection properties)
    execution-id
        execution ID of current execution
__EOF__
}

if [ $# -ne 1 -a $# -ne 2 ]
then
    usage
    exit 1
fi

. ~/.bulkloader_db_profile
export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader

LOGFILE_BASENAME="recoverer"
CLASS_NAME="com.asakusafw.bulkloader.cache.ReleaseCacheLock"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh
cd "$ASAKUSA_HOME"

"$JAVA_HOME"/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$@"
_RET=$?

exit $_RET
