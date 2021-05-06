#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

cd /Users/al/repos/github/population-linkage

COPY=/Users/al/repos/github/population-linkage/src/main/scripts/neo4j/COPY_DB.sh

COPYDIR=/Users/al/Desktop/NEOCOPY/

export MAVEN_OPTS="-Xmx16G"

#echo "1. Loading records from Storr"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.LoadNeo4JVitalEventsFromStorr" -e
#$COPY $COPYDIR/records
#echo "2. Indexing records in Neo4J"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.Index" -e
echo "3. Performing birth sibling bundling"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthSiblingBundleBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/siblings
echo "4. Performing birth sibling bundling then BM (birth-parents-marriage) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthSiblingBundleThenParentsBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/parents
echo "5. Performing birth own death linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthOwnDeathBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/deaths
echo "6. Performing birth bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthBrideOwnMarriageBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/brides
echo "7. Performing birth groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthGroomOwnMarriageBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/grooms
echo "8. Performing death groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathGroomOwnMarriageBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/groomdeath
echo "9. Performing death bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathBrideOwnMarriageBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/bridedeath
echo "10. Performing birth parents marriage (ID) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthParentsMarriageBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/birthparents
echo "11. Marriage sibling (GG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomGroomSiblingBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/grooms2
echo "12. Marriage sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideBrideiblingBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/brides2
echo "13. Marriage sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideGroomSiblingBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/bridegroom2
echo "14. Marriage sibling (GB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomBrideSiblingBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/groombride2
echo "14. Death siblings (indirect) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathSiblingBundleBuilder" -e -Dexec.args="umea umea_results"
$COPY $COPYDIR/deathsiblings
