#!/bin/bash

usage() {
    cat <<EOF
Deletes cache contents.
*** This program is for only ThundeGate internal use. ***

Usage:
    $0 target-name

Parameters:
    target-name
        profile name (used for detect database connection properties)

I/O:
    standard input:
        FileList protocol
    standard output:
        FileList protocol
    standard error:
        print information in execution
EOF
}

if [ $# -ne 1 ]; then
  usage
  exit 1
fi

_TARGET_NAME="$1"
shift

. ~/.bulkloader_hc_profile 1>&2
export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader 1>&2

LOGFILE_BASENAME="delete-cache-storage"
CLASS_NAME="com.asakusafw.bulkloader.cache.DeleteCacheStorageRemote"
USER_NAME="$(whoami)"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-hc.sh "undefined" "undefined"

cd "$ASAKUSA_HOME" 1>&2

$JAVA_HOME/bin/java \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_TARGET_NAME" \
    "$USER_NAME"
rc=$?
exit $rc
