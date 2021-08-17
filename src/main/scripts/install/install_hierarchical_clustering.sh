#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

# Hierarchical clustering manually installed on Manifesto
# downloaded from https://github.com/malger/Hierarchical-Clustering
# downloaded to secure@manifesto: /home/secure/git/repositories/malger/Hierarchical-Clustering
# Manually copy target from /home/secure/git/repositories/malger/Hierarchical-Clustering/target to /home/secure/maven

# Operations on Manifesto:
# cd /home/secure/git/repositories/malger/Hierarchical-Clustering
# git pull https://github.com/malger/Hierarchical-Clustering
# mvn build
# scp home/secure/git/repositories/malger/Hierarchical-Clustering/target/hierarchicalclustering-1.0.jar /home/secure/maven

# Run this script on client machine

scp -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven/hierarchicalclustering-1.0.jar .
mvn -q install:install-file -Dfile=hierarchicalclustering-1.0.jar -DgroupId=com.github.malger -DartifactId=hierarchicalclustering -Dversion=1.0 -Dpackaging=jar
rm hierarchicalclustering-1.0.jar

