#!/bin/bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


if [[ -n "$1" ]];
then
    export MAVEN_OPTS="-Xmx$1G"
fi
