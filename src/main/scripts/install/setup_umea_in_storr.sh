#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

export MAVEN_OPTS="-Xmx16G"

echo "This script assumes that the script put_umea_into_storr.sh has already been run from the data-umea project"

echo "Indexing records with STANDARDISED_ID in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.IndexOnStandardisedId" -e

echo "Establishing GT linkage in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.CreateGTLinks" -e
