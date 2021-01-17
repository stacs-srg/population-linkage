#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


scp -i ~/.ssh/id_rsa_2 -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven/richard-connor/Metric-space-framework/1.0-SNAPSHOT/Metric-space-framework-1.0-20200423.084045-3-jar-with-dependencies.jar .
mvn -q install:install-file -Dfile=Metric-space-framework-1.0-20200423.084045-3-jar-with-dependencies.jar -DgroupId=richard-connor -DartifactId=Metric-space-framework -Dversion=1.0-SNAPSHOT -Dpackaging=jar
rm Metric-space-framework-1.0-20200423.084045-3-jar-with-dependencies.jar
