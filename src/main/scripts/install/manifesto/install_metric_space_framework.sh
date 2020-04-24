#!/bin/sh
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

mvn -q install:install-file -Dfile=/home/secure/maven/richard-connor/Metric-space-framework/1.0-SNAPSHOT/Metric-space-framework-1.0-20181123.085901-1-jar-with-dependencies.jar -DgroupId=richard-connor -DartifactId=Metric-space-framework -Dversion=1.0-SNAPSHOT -Dpackaging=jar
