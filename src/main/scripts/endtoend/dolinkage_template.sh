#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module population-linkage.
#
# population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with population-linkage. If not, see
# <http://www.gnu.org/licenses/>.
#

echo "in dir $PWD"

if [ "$#" -eq  "0" ]
then
    error "Wrong number of parameters supplied: should be called with a number of items to link of EVERYTHING, expect it to be called from dolinkage_all or dolinkage_subset"
    exit -1
fi

EXEC_ARGS="umea ${1}"

export MAVEN_OPTS="-Xmx16G"

echo "0. Perform indexing of GT and Relationships - may fail if already in Db"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.IndexRelationships" -e
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks.CreateGTIndices" -e
echo "1. Performing birth sibling bundling"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
echo "2. Performing birth own death linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthOwnDeathBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/deaths
echo "3. Performing birth bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthBrideOwnMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/brides
echo "4. Performing birth groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthGroomOwnMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/grooms
echo "5. Performing death groom own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathGroomOwnMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/groomdeath
echo "6. Performing death bride own marriage linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathBrideOwnMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/bridedeath
echo "7. Performing birth parents marriage (ID) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthParentsMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/birthparents
echo "8. Marriage sibling (GG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomGroomSiblingBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/grooms2
echo "9. Marriage sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideBrideSiblingBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/brides2
echo "10. Marriage sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideGroomSiblingBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/bridegroom2
echo "11. Death siblings (indirect) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
#$COPY #$COPYDIR/deathsiblings
echo "12. Birth-Death Sibling (BD) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthDeathSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/death-birth
echo "13. Birth-Bride Sibling (BB) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthBrideSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/birth-bride
echo "14. Birth-Groom Sibling (BG) linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthGroomSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/birth-bride
echo "15. Bride Bride Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideBrideIdentityBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/birth-bride-id
echo "16. Groom Groom Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomGroomIdentityBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/groom-groom-id
echo "17. Bride Marriage Parents Marriage Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.BrideMarriageParentsMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/bride-parents-marriage
echo "18. Groom Marriage Parents Marriage Identity linkage"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.GroomMarriageParentsMarriageBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/groom-parents-marriage
echo "19. Death Bride Sibling linkage DeathBrideSiblingBundleBuilder FINISHES FAST - FAILURE NO LINKS"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathBrideSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/death-bride-sibling
echo "20. Death Groom Sibling linkage" DeathBrideSiblingBundleBuilder
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathGroomSiblingBundleBuilder" -e -Dexec.args="${EXEC_ARGS}"
##$COPY #$COPYDIR/death-groom-sibling
