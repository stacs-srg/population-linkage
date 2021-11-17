#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

export NEOHOME=$(which neo4j)
export DIR="$(dirname "${NEOHOME}")"

cd "$DIR"/../var/neo4j

#rm -Rf data/databases/* data/transactions/*
ls -l

