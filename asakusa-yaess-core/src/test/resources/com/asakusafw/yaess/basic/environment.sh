#!/bin/sh

_OUTPUT="$0.out"
touch "$_OUTPUT"

printenv >> "$_OUTPUT"
cat "$_OUTPUT"
