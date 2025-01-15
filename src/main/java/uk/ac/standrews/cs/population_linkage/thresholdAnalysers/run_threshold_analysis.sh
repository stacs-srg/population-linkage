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

echo "1. Running Birth-Birth Sibling Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthBirthSib"

echo "2. Running Birth-Death ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthDeathID"

echo "3. Running Death-Death Sibling Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisDeathDeathSib"

echo "4. Running Birth-Marriage Groom ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthGroomID"

echo "5. Running Birth-Marriage Bride ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthBrideID"

echo "6. Running Marriage-Marriage Groom Parents ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisGroomMarriageParentsMarriage"

echo "7. Running Marriage-Marriage Bride Parents ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBrideMarriageParentsMarriage"

echo "8. Running Birth-Marriage Parents ID Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthParentsMarriage"

echo "9. Running Birth-Death Sibling Threshold Analysis"
mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.thresholdAnalysers.ThresholdTrianglesAnalysisBirthDeathSib"
