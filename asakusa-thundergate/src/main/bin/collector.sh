#!/bin/bash
#Collector起動コマンド

usage() {
	cat <<EOF
Collectorを起動します。

起動シェルスクリプト
collector.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   バッチID                          必須
 3   ジョブフローID                    必須
 4   ジョブフロー実行ID                必須
 5   変数表の文字列表記                必須

EOF
}

if [ $# -ne 5 ]; then
  usage
  exit 1
fi

_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift
export BULKLOADER_ARGS="$1"
shift

. ~/.bulkloader_hc_profile 1>&2
export BULKLOADER_HOME="$ASAKUSA_HOME"/bulkloader 1>&2

LOGFILE_BASENAME="collector"
CLASS_NAME="com.asakusafw.bulkloader.collector.Collector"
USER_NAME=$(whoami)

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-hc.sh "$_BATCH_ID" "$_FLOW_ID"

cd "$ASAKUSA_HOME" 1>&2

$JAVA_HOME/bin/java \
    $COLLECTOR_JAVA_OPTS \
    -Dasakusa.home=$ASAKUSA_HOME \
    -Dlogfile.basename=$LOGFILE_BASENAME \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID" \
    "$USER_NAME"
rc=$?

exit $rc
