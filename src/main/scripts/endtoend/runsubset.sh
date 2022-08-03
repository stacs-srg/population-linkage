#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

cd /Users/al/repos/github/population-linkage

echo "in dir $PWD"

COPY=/Users/al/repos/github/population-linkage/src/main/scripts/neo4j/COPY_DB.sh

COPYDIR=/Users/al/Desktop/NEOCOPY/

export MAVEN_OPTS="-Xmx16G"

echo "1. No longer Load records from Storr - not used: now in data-umea"
#echo "1. Loading records from Storr"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.LoadNeo4JVitalEventsFromStorr" -e
##$COPY #$COPYDIR/records
echo "2. No longer Indexing records in Neo4J - not used: now in data-umea"
#echo "2. Indexing records in Neo4J"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.graph.util.Index" -e
echo "3. Performing birth sibling bundling"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
#$COPY #$COPYDIR/siblings
#echo "No longer - Performing birth sibling bundling then BM (birth-parents-marriage) linkage file doesn't exist!"
##echo "4. Performing birth sibling bundling then BM (birth-parents-marriage) linkage"
##mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleThenParentsBuilder" -e -Dexec.args="umea EVERYTHING"
#$COPY #$COPYDIR/parents
echo "5. Performing birth own death linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthOwnDeathBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/deaths
echo "6. Performing birth bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthBrideOwnMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/brides
echo "7. Performing birth groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthGroomOwnMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/grooms
echo "8. Performing death groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathGroomOwnMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/groomdeath
echo "9. Performing death bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathBrideOwnMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/bridedeath
echo "10. Performing birth parents marriage (ID) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthParentsMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/birthparents
echo "11. Marriage sibling (GG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomGroomSiblingBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/grooms2
echo "12. Marriage sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideBrideSiblingBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/brides2
echo "13. Marriage sibling (BG) linkage THIS ONE FAILS!"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideGroomSiblingBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/bridegroom2
echo "15. Death siblings (indirect) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
#$COPY #$COPYDIR/deathsiblings
#echo "16. Birth-Death Sibling (BD) linkage"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthDeathSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/death-birth
#
echo "17. Birth-Bride Sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthBrideSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/birth-bride
#
echo "18. Birth-Groom Sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthGroomSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/birth-bride
#
#echo "19. Bride Bride Identity linkage"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideBrideIdentityBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/birth-bride-id
#
#echo "20. Groom Groom Identity linkage"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomGroomIdentityBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/groom-groom-id
#
#echo "21. Bride Marriage Parents Marriage Identity linkage"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideMarriageParentsMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/bride-parents-marriage
#
#echo "22. Groom Marriage Parents Marriage Identity linkage"
#mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomMarriageParentsMarriageBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/groom-parents-marriage
#
echo "23. Death Bride Sibling linkage DeathBrideSiblingBundleBuilder FINISHES FAST - FAILURE NO LINKS"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathBrideSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/death-bride-sibling
#
echo "24. Death Groom Sibling linkage" DeathBrideSiblingBundleBuilder
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathGroomSiblingBundleBuilder" -e -Dexec.args="umea EVERYTHING"
##$COPY #$COPYDIR/death-groom-sibling
