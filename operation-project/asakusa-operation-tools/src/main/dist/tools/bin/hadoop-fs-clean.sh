#!/bin/sh
#
# Copyright 2011-2012 Asakusa Framework Team.
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
        echo "$_SCRIPT is not found" 2>&1
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

_TL_ROOT="$(dirname $0)/.."
import "$_TL_ROOT/conf/env.sh"
import "$_TL_ROOT/libexec/configure-hadoop-cmd.sh"
import "$_TL_ROOT/libexec/configure-classpath.sh"

_TL_CLASS="com.asakusafw.operation.tools.hadoop.fs.Clean"

export HADOOP_CLASSPATH="$_TL_CLASSPATH"
"$HADOOP_CMD" "$_TL_CLASS" "$@"
_RET=$?
exit $_RET
