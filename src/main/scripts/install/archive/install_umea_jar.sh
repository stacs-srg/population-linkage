#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

mvn -q install:install-file -Dfile=$1 -DgroupId=uk.ac.standrews.cs -DartifactId=umea-data -Dversion=1.0-SNAPSHOT -Dpackaging=jar
