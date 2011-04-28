#!/bin/sh
#Importer起動コマンド

usage() {
	cat <<EOF
Importerを起動します。

起動シェルスクリプト
importer.sh

 順  引数                                 必須/任意
 --------------------------------------------------
 1   Import処理区分(primary or secondary)  必須
 2   ターゲット名                          必須
 3   バッチID                              必須
 4   ジョブフローID                        必須
 5   ジョブフロー実行ID                    必須
 6   ジョブフロー終了予定時刻              必須
 7   変数表の文字列表記                    必須
 8   リカバリ対象テーブル                  任意

 ※ジョブネットの終了予定時刻は「YYYYMMDDHHMMSS」形式で指定する。
 ※リカバリ対象テーブルが複数ある場合は半角カンマ「,」で区切って指定する。

EOF
}

if [ $# -ne 7 -a $# -ne 8 ]; then
  usage
  exit 1
fi

_IMPORTER_CLASS="$1"
shift
_TARGET_NAME="$1"
shift
_BATCH_ID="$1"
shift
_FLOW_ID="$1"
shift
_EXECUTION_ID="$1"
shift
_ESTIMATED_END="$1"
shift
export BULKLOADER_ARGS="$1"
shift

. ~/.bulkloader_db_profile
export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

LOGFILE_BASENAME="importer"
CLASS_NAME="com.asakusafw.bulkloader.importer.Importer"

. "$ASAKUSA_HOME"/bulkloader/bin/set-classpath-db.sh "$_BATCH_ID" "$_FLOW_ID"

cd "$ASAKUSA_HOME"

$JAVA_HOME/bin/java \
    $IMPORTER_JAVA_OPTS \
    -Dasakusa.home="$ASAKUSA_HOME" \
    -Dlogfile.basename="$LOGFILE_BASENAME" \
    -classpath "$BULK_LOADER_CLASSPATH" \
    "$CLASS_NAME" \
    "$_IMPORTER_CLASS" \
    "$_TARGET_NAME" \
    "$_BATCH_ID" \
    "$_FLOW_ID" \
    "$_EXECUTION_ID" \
    "$_ESTIMATED_END" \
    $*
rc=$?

exit $rc

