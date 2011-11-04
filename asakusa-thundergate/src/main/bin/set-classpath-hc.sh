#!/bin/bash
#rem クラスパスを設定するコマンド
#rem 引数（バッチID,ジョブフローID）

BULK_LOADER_CLASSPATH=$ASAKUSA_HOME/bulkloader/conf
BULK_LOADER_CLASSPATH=$BULK_LOADER_CLASSPATH:$ASAKUSA_HOME/core/lib/*
BULK_LOADER_CLASSPATH=$BULK_LOADER_CLASSPATH:$ASAKUSA_HOME/bulkloader/lib/*

if [ -e "$ASAKUSA_HOME/batchapps/$1/lib/jobflow-$2.jar" ]
then
    BULK_LOADER_CLASSPATH=$BULK_LOADER_CLASSPATH:$ASAKUSA_HOME/batchapps/$1/lib/jobflow-$2.jar
fi

export BULK_LOADER_CLASSPATH
