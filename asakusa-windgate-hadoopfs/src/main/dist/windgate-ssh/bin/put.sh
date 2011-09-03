#!/bin/sh

_WG_ROOT="$(dirname $0)/.."

. "$_WG_ROOT/conf/env.sh"

if [ "$HADOOP_HOME" = "" -o ! -d "$HADOOP_HOME" ]
then
    echo '$HADOOP_HOME'" is invalid: $HADOOP_HOME" 1>&2
    exit 1
fi

_WG_CLASSPATH="$_WG_ROOT/conf"
if [ -d "$_WG_ROOT/lib" ]
then
    for f in $(ls "$_WG_ROOT/lib/")
    do
        _WG_CLASSPATH="$_WG_CLASSPATH:$_WG_ROOT/lib/$f"
    done
fi

_WG_CLASS="com.asakusafw.windgate.hadoopfs.ssh.WindGateHadoopPut"

echo "$_WG_CLASS" 1>&2
for f in $*
do
    echo "$f" 1>&2
done

export HADOOP_CLASSPATH="$_WG_CLASSPATH"
"$HADOOP_HOME/bin/hadoop" "$_WG_CLASS" $*

_WG_RET=$?
if [ $_WG_RET -ne 0 ]
then
    echo "$_WG_CLASS failed with exit code: $_WG_RET" 1>&2
    for f in $*
    do
        echo "$f" 1>&2
    done
    exit $_WG_RET
fi
