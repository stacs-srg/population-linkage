#!/bin/bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

source "$(dirname "$0")"/../../set_heap_size.sh

mvn exec:java -q -e -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthBrideIdentity" -Dexec.args="$2 $3 $4"
