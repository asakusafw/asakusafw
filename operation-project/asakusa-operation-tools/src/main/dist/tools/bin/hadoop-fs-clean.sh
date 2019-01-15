#!/bin/bash
#
# Copyright 2011-2019 Asakusa Framework Team.
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

usage() {
    cat 1>&2 <<EOF
hadoop-fs-clean - Remove old contents on Hadoop File System

Usage:
    hadoop-fs-clean.sh -k <days> [-r] [-s] <path> [<path> [...]]

Parameters:
    -h, -help
        Print this message
    -k <days>, -keep-days <days>
        Keep contents which are modified in <days> days.
        The command will remove contents only which are modified after the last <days>.
    -r, -recursive
        Cleanup directories and their contents recursively.
    -s, -dry-run
        Do not actually remove contents.
    path
        Cleanup target path expressions.
    --
        Separate options and paths.

Examples:
    hadoop-fs-clean.sh -k 1 hadoopwork/*
    hadoop-fs-clean.sh -k 0 -r hdfs://localhost:8020/usr/asakusa/hadoopwork/*
    hadoop-fs-clean.sh -k 10 -r -s file:///tmp/asakusa/logs
EOF
}

import() {
    _SCRIPT="$1"
    if [ -e "$_SCRIPT" ]
    then
        . "$_SCRIPT"
    else
        echo "$_SCRIPT is not found" 1>&2
        exit 1
    fi
}

if [ "$1" = "-h" -o "$1" = "-help" ]
then
    usage
    exit 0
fi

if [ $# -lt 3 ]
then
    usage
    exit 2
fi

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"

cd

import "${ASAKUSA_HOME:?ASAKUSA_HOME is not defined}/hadoop/libexec/configure-hadoop.sh"
import "$_ROOT/conf/env.sh"

if [ "$_HADOOP_CMD" != "" ]
then
    exec "$_HADOOP_CMD" jar "$_ROOT/lib/asakusa-operation-tools.jar" \
        "com.asakusafw.operation.tools.hadoop.fs.Clean" \
        "$@"
else
    _CLASSPATH=("$_ROOT/lib/asakusa-operation-tools.jar")
    _CLASSPATH+=("${_HADOOP_EMBED_CLASSPATH[@]}")
    _CLASSPATH+=("$_ROOT/lib/slf4j-simple.jar")
    import "$ASAKUSA_HOME/core/libexec/configure-java.sh"
    exec "$_JAVA_CMD" $JAVA_OPTS \
        -classpath "$(IFS=:; echo "${_CLASSPATH[*]}")" \
        "com.asakusafw.operation.tools.hadoop.fs.Clean" \
        "$@"
fi
