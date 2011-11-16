#!/bin/sh

_OUTPUT="$0.out"
touch "$_OUTPUT"

for line in "$@"
do
    echo "$line" >> "$_OUTPUT"
done

cat "$_OUTPUT"
