#!/bin/bash

usage() {
    cat << __EOF__
Forces delete cache information.

Usage:
    $0 subcommand target-name [arguments...]

Parameters:
    subcommand:
        one of "cache", "table" or "all" (see later section)
    target-name
        profile name (used for detect database connection properties)
    arguments...
        arguments for the specified subcommand

Subcommands:
    $0 cache target-name cache-id
    Deletes cache described as "cache-id"
    
    $0 table target-name table-name
    Deletes all cache corresponded to "table-name"
    
    $0 all target-name
    Deletes all cache in the "target-name"
__EOF__
}

if [ $# -lt 2 ]
then
    usage
    exit 1
fi

_OPT_SUBCOMMAND="$1"
shift
_OPT_TARGET_NAME="$1"
shift

. ~/.bulkloader_db_profile

if [ "$ASAKUSA_HOME" = "" ]
then
    echo '$ASAKUSA_HOME'" is not defined" 1>&2
    exit 1
fi

export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="delete-cache-info"
CLASS_NAME="com.asakusafw.bulkloader.cache.DeleteCacheInfo"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh
cd "$ASAKUSA_HOME"

echo "Starting delete-cache-info:"
echo "    Subcommand: $_OPT_SUBCOMMAND"
echo "   Target Name: $_OPT_TARGET_NAME"
echo "     Arguments: $@"

"$JAVA_HOME"/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_OPT_SUBCOMMAND" \
    "$_OPT_TARGET_NAME" \
    "$@"


_TGC_RET=$?
if [ $_TGC_RET -ne 0 ]
then
    echo "DeleteCacheInfo failed with exit code: $_TGC_RET" 1>&2
    echo "    Subcommand: $_OPT_SUBCOMMAND"
    echo "   Target Name: $_OPT_TARGET_NAME"
    echo "     Arguments: $@"
    exit $_TGC_RET
fi
