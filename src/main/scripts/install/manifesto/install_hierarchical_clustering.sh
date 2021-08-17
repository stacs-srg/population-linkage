#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

cd /home/secure/git/repositories/malger/Hierarchical-Clustering
git pull https://github.com/malger/Hierarchical-Clustering
mvn build
scp home/secure/git/repositories/malger/Hierarchical-Clustering/target/hierarchicalclustering-1.0.jar /home/secure/maven
