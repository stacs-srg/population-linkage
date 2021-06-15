#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

cd /Users/al/repos/github/population-linkage

COPY=/Users/al/repos/github/population-linkage/src/main/scripts/neo4j/COPY_DB.sh

COPYDIR=/Users/al/Desktop/NEOCOPY/

export MAVEN_OPTS="-Xmx16G"

echo "1. Load records from Storr - not used"
#echo "1. Loading records from Storr"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.LoadNeo4JVitalEventsFromStorr" -e
#$COPY $COPYDIR/records
#echo "2. Indexing records in Neo4J"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.Index" -e
#echo "3. Performing birth sibling bundling"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
#$COPY $COPYDIR/siblings

echo "5. Performing birth own death linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthOwnDeathBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/deaths
echo "6. Performing birth bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthBrideOwnMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/brides
echo "7. Performing birth groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthGroomOwnMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/grooms
echo "8. Performing death groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathGroomOwnMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/groomdeath
echo "9. Performing death bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathBrideOwnMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/bridedeath
echo "10. Performing birth parents marriage (ID) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthParentsMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/birthparents
echo "11. Marriage sibling (GG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomGroomSiblingBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/grooms2
echo "12. Marriage sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideBrideSiblingBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/brides2
echo "13. Marriage sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideGroomSiblingBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/bridegroom2
echo "14. Marriage sibling (GB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomBrideSiblingBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/groombride2
echo "15. Death siblings (indirect) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/deathsiblings

echo "16. Performing birth sibling bundling then BM (birth-parents-marriage) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthSiblingBundleThenParentsBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/parents

echo "17. Birth-Death Sibling (BD) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthDeathSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/death-birth

echo "18. Birth-Bride Sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthBrideSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/birth-bride

echo "19. Birth-Groom Sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BirthGroomSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/birth-bride

echo "20. Bride Bride Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideBrideIdentityBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/birth-bride-id

echo "21. Groom Groom Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomGroomIdentityBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/groom-groom-id

echo "22. Bride Marriage Parents Marriage Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.BrideMarriageParentsMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/bride-parents-marriage

echo "22. Groom Marriage Parents Marriage Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.GroomMarriageParentsMarriageBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/groom-parents-marriage

echo "23. Death Bride Sibling linkage" DeathBrideSiblingBundleBuilder
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathBrideSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/death-bride-sibling

echo "24. Death Groom Sibling linkage" DeathBrideSiblingBundleBuilder
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathGroomSiblingBundleBuilder" -e -Dexec.args="Umea umea_results"
$COPY $COPYDIR/death-groom-sibling
