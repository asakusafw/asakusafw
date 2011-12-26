#!/bin/bash

#---------------------------------------------------------
# Configurable valiarble

#DPROP="-D mapreduce.job.maps=1 -D mapreduce.job.reduces=1"

#---------------------------------------------------------

#HadoopJob起動コマンド

usage() {
	cat <<EOF
Hadoop Jobを起動します。

起動シェルスクリプト
hadoop_job_run.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   StageClientの完全修飾クラス名     必須
 2   ジョブフローのJarファイル名       必須
 3以降   Hadoopコマンドに渡す引数         任意

EOF
}

if [ $# -lt 2 ]; then
  usage
  exit 1
fi

STAGE_CLIENT_CLASSNAME=$1
shift
FLOW_JARNAME=$1
shift

TOOL_LAUNCHER_CLASSNAME="com.asakusafw.runtime.stage.ToolLauncher"
BATCH_RUNTIME_JAR="$ASAKUSA_HOME/core/lib/asakusa-runtime.jar"
PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"

LIBJAR=$FLOW_JARNAME

for i in "$ASAKUSA_HOME/core/lib/"*.jar
do
    LIBJAR="$LIBJAR","$i"
done

if [ -d "$ASAKUSA_HOME/ext/lib" ]
then
    EXTLIBCNT=$(ls -1 "$ASAKUSA_HOME/ext/lib" | wc -l)
    if [ $EXTLIBCNT != "0" ]
    then
      for i in "$ASAKUSA_HOME/ext/lib/"*.jar
      do
        LIBJAR="$LIBJAR","$i"
      done
    fi
fi

cd $HOME

RUNCMD="$HADOOP_HOME/bin/hadoop jar $BATCH_RUNTIME_JAR $TOOL_LAUNCHER_CLASSNAME $STAGE_CLIENT_CLASSNAME -conf $PLUGIN_CONF -libjars $LIBJAR" 

echo "[COMMAND] $RUNCMD" "$@" $DPROP
$RUNCMD "$@" $DPROP
exit $?
