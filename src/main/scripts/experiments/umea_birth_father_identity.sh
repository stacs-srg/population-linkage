#!/bin/bash

# shellcheck disable=SC1090
source "$(dirname $0)"/set_heap_size.sh

mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthFatherIdentity" -e
