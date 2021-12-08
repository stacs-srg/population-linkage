#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

export MAVEN_OPTS="-Xmx16G"

echo "This script assumes script install_umea_data has already been run to install into the local maven repository"

echo "1. Loading event records into Storr"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.data.umea.store.ImportUmeaRecordsToStore" -e

echo "2. Indexing records with STANDARDISED_ID in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.IndexOnStandardisedId" -e

echo "3. Establishing GT linkage in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.CreateGTLinks" -e
