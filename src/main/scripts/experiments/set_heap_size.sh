#!/bin/bash

if [[ -n "$1" ]];
then
    export MAVEN_OPTS="-Xmx$1G"
fi
