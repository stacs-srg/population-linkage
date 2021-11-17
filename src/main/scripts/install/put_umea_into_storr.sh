#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

export MAVEN_OPTS="-Xmx16G"

echo "install_umea_data must be run before this script to load packages"

echo "1. Loading event records into Storr"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.data.umea.ImportUmeaRecordsToStore" -e

echo "2. Indexing records with STANDARDISED_ID in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.IndexOnStandardisedId" -e

echo "3. Indexing records with indices for GT linkage in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.Index" -e

echo "4. Establishing GT linkage in Neo4J"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.EstablishGTLinks" -e

