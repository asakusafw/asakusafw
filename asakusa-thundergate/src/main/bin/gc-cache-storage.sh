#!/bin/bash

usage() {
    cat << __EOF__
Releases previously deleted cache storages.

Usage:
    $0 target-name

Parameters:
    target-name
        profile name (used for detect database connection properties)
__EOF__
}

if [ $# -ne 1 ]
then
    usage
    exit 1
fi

_OPT_TARGET_NAME="$1"

. ~/.bulkloader_db_profile

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
    exit 1
fi

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="gc-cache-storage"
CLASS_NAME="com.asakusafw.bulkloader.cache.GcCacheStorage"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh
cd "$ASAKUSA_HOME"

echo "Starting gc-cache-storage:"
echo "   Target Name: $_OPT_TARGET_NAME"

"$JAVA_HOME"/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_OPT_TARGET_NAME"

_TGC_RET=$?
if [ $_TGC_RET -ne 0 ]
then
    echo "GC Cache Storage failed with exit code: $_TGC_RET" 1>&2
    echo "   Target Name: $_OPT_TARGET_NAME" 1>&2
    exit $_TGC_RET
fi
