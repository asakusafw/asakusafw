#!/bin/bash
#
# Copyright 2011-2016 Asakusa Framework Team.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
BATCH_RUNTIME_JAR="$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar"
PLUGIN_CONF="$ASAKUSA_HOME/core/conf/asakusa-resources.xml"

LIBJAR=$FLOW_JARNAME

for i in "$ASAKUSA_HOME/core/lib/"*.jar
do
    LIBJAR="$LIBJAR","$i"
done

# FIXME support <batch-archive>/usr/lib

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

if [ "$HADOOP_CMD" = "" ]
then
    if [ "$HADOOP_HOME" != "" ]
    then
        HADOOP_CMD="$HADOOP_HOME/bin/hadoop"
        unset HADOOP_HOME
    else
        HADOOP_CMD="$(which hadoop)"
        _RET=$?
        if [ $_RET -ne 0 ]
        then
            echo 'hadoop command is not found' 1>&2
            exit 1
        fi
    fi
fi

if [ ! -x "$HADOOP_CMD" ]
then
    echo "$HADOOP_CMD is not executable" 1>&2
    exit 1
fi

cd $HOME

RUNCMD="$HADOOP_CMD jar $BATCH_RUNTIME_JAR $TOOL_LAUNCHER_CLASSNAME $STAGE_CLIENT_CLASSNAME -conf $PLUGIN_CONF -libjars $LIBJAR" 

echo "[COMMAND] $RUNCMD" "$@" $DPROP
$RUNCMD "$@" $DPROP
exit $?
