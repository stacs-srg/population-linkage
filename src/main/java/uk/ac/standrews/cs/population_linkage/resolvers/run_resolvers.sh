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

echo "1. Running Birth-Birth Sibling Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.BirthBirthOpenTriangleResolver" -Dexec.args="umea EVERYTHING"

echo "2. Running Birth-Death Sibling Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.BirthDeathOpenTriangleResolver" -Dexec.args="umea EVERYTHING"

echo "3. Running Death-Death Sibling Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.DeathDeathOpenTriangleResolver" -Dexec.args="umea EVERYTHING"

echo "4. Running Birth-Death ID Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.BirthDeathIDGraphOpenTriangleResolver" -Dexec.args="umea"

echo "5. Running Birth-Marriage ID Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.BirthMarriageIDOpenTriangleResolver" -Dexec.args="umea"

echo "6. Running Birth-Marriage Parents ID Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.BirthParentsMarriageIDOpenTriangleResolver" -Dexec.args="umea"

echo "7. Running Marriage-Marriage Parents ID Open Triangle Resolver"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.resolvers.MarriageParentsMarriageIDOpenTriangleResolver" -Dexec.args="umea"
