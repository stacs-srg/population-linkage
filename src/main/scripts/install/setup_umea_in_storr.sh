#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

export MAVEN_OPTS="-Xmx16G"

echo "This script assumes that the script setup_umea_in_storr.sh has already been run from the data-umea project"

echo "Establishing ground truth linkage in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.CreateGTLinks" -e
