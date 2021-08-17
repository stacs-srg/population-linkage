#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

# Assumes that manifesto/install_hierarchicalclusterng has been run on Manifesto

scp -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven/hierarchicalclustering-1.0.jar .
mvn -q install:install-file -Dfile=hierarchicalclustering-1.0.jar -DgroupId=com.github.malger -DartifactId=hierarchicalclustering -Dversion=1.0 -Dpackaging=jar
rm hierarchicalclustering-1.0.jar


