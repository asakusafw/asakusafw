#!/bin/sh
#Recoverer起動コマンド

usage() {
	cat <<EOF
Recovererを起動します。

起動シェルスクリプト
recoverer.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   ジョブフロー実行ID                必須

EOF
}

source ~/.bulkloader_db_profile
export BULKLOADER_HOME=$ASAKUSA_HOME/bulkloader

LOGFILE_BASENAME="recoverer"
CLASS_NAME="com.asakusafw.bulkloader.recoverer.Recoverer"

. $ASAKUSA_HOME/bulkloader/bin/set-classpath-db.sh

LIB_DIR=$ASAKUSA_HOME/batchapps
for dname in `ls $LIB_DIR`
do
   for fname in `ls $LIB_DIR/$dname/lib/*.jar`
   do
     BULK_LOADER_CLASSPATH=$BULK_LOADER_CLASSPATH:$fname
   done
done

cd $ASAKUSA_HOME

if [ $# -eq 1 -o $# -eq 2 ]; then
  $JAVA_HOME/bin/java $RECOVERER_JAVA_OPTS -Dasakusa.home=$ASAKUSA_HOME -Dlogfile.basename=$LOGFILE_BASENAME -classpath $BULK_LOADER_CLASSPATH $CLASS_NAME $*
  rc=$?
else
  usage
  rc=1
fi

exit $rc
