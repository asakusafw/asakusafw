#!/bin/bash
# DBをクリーニングするツール
# ThunderGateで使用するシステムテーブルの内容を全て削除する。
# 実行すると以下の処理を行う。
# ・ジョブフロー実行テーブル(RUNNING_JOBFLOWS)のレコードを全件削除
# ・ジョブフロー排他テーブル(JOBFLOW_INSTANCE_LOCK)のレコードを全件削除
# ・ロック(テーブルロック・レコードロック)を全て解除
# ・エクスポートテンポラリテーブルを全て削除
# ・エクスポートテンポラリ管理のレコードを全件削除

usage() {
	cat <<EOF
DBをクリーニングするツールを起動します。

起動シェルスクリプト
dbcleaner.sh

 順  引数                                       必須/任意
 ---------------------------------------------------------
 1   ターゲット名                                 必須

EOF
}

. ~/.bulkloader_db_profile
export BULKLOADER_HOME="$ASAKUSA_HOME/bulkloader"

CLASSPATH=$ASAKUSA_HOME/bulkloader/conf
CLASSPATH=$CLASSPATH:$ASAKUSA_HOME/bulkloader/lib/*

CLASS_NAME=com.asakusafw.bulkloader.tools.DBCleaner

cd $ASAKUSA_HOME

if [ $# -ne 1 ]; then
  usage
  exit 1
fi
$JAVA_HOME/bin/java -classpath $CLASSPATH $CLASS_NAME $*
rc=$?
echo RETURN_CODE:$rc
exit $rc
