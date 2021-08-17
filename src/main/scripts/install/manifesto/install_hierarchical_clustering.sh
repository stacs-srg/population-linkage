#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

install_metric_space_framework.sh

# Hierarchical clustering from https://github.com/malger/Hierarchical-Clustering
# Downloaded to secure@manifesto: /home/secure/git/repositories/malger/Hierarchical-Clustering
# Manually copy target from /home/secure/git/repositories/malger/Hierarchical-Clustering/target to /home/secure/maven

scp -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven/hierarchicalclustering-1.0.jar .
mvn -q install:install-file -Dfile=hierarchicalclustering-1.0.jar -DgroupId=com.github.malger -DartifactId=hierarchicalclustering -Dversion=1.0 -Dpackaging=jar
rm hierarchicalclustering-1.0.jar

