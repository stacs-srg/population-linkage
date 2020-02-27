#!/bin/sh

echo please run this from project root = population-linkage

if [[ -n "$1" ]];
then
    export MAVEN_OPTS="-Xmx"$1"G"
    echo Setting heap size: $1GB
fi


mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.UmeaGroomGroomSiblingViability" -e

mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.UmeaBrideBrideSiblingViability" -e

mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.UmeaBrideGroomSiblingViability" -e

mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.population_linkage.groundTruth.UmeaGroomBrideSiblingViability" -e

