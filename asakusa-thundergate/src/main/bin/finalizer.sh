#!/bin/sh
#Finalizer起動コマンド

usage() {
	cat <<EOF
Finalizerを起動します。

起動シェルスクリプト
finalizer.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   バッチID                          必須
 3   ジョブフローID                    必須
 4   ジョブフロー実行ID                必須

EOF
}

$ASAKUSA_HOME/bulkloader/bin/recoverer.sh $1 $4
rc=$?

exit $rc
