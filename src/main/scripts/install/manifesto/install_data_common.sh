#!/bin/sh
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


cp ${MAVEN_REPO_PATH}/uk/ac/standrews/cs/${PROJECT}/1.0-SNAPSHOT/maven-metadata.xml .
BUILD_NUMBER=$(sed -ne '/buildNumber/{s/.*<buildNumber>\(.*\)<\/buildNumber>.*/\1/p;q;}' < maven-metadata.xml)
TIME_STAMP=$(sed -ne '/timestamp/{s/.*<timestamp>\(.*\)<\/timestamp>.*/\1/p;q;}' < maven-metadata.xml)
JAR_FILE=${PROJECT}-1.0-${TIME_STAMP}-${BUILD_NUMBER}-jar-with-dependencies.jar
cp ${MAVEN_REPO_PATH}/uk/ac/standrews/cs/${PROJECT}/1.0-SNAPSHOT/${JAR_FILE} .

mvn -q install:install-file -Dfile=${JAR_FILE} -DgroupId=uk.ac.standrews.cs -DartifactId=${PROJECT} -Dversion=1.0-SNAPSHOT -Dpackaging=jar
rm ${JAR_FILE}
rm maven-metadata.xml
