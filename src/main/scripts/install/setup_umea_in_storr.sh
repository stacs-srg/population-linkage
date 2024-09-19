#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module data-umea.
#

export MAVEN_OPTS="-Xmx16G"

echo "Creating indices"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.helpers.CreateIndices" -e

echo "Loading event records"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.helpers.ImportUmeaRecordsToStore" -e
