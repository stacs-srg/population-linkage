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


scp -q ${MAVEN_USER}@${MAVEN_HOST}:${MAVEN_REPO_PATH}/uk/ac/standrews/cs/${PROJECT}/1.0-SNAPSHOT/maven-metadata.xml .
BUILD_NUMBER=$(sed -ne '/buildNumber/{s/.*<buildNumber>\(.*\)<\/buildNumber>.*/\1/p;q;}' < maven-metadata.xml)
TIME_STAMP=$(sed -ne '/timestamp/{s/.*<timestamp>\(.*\)<\/timestamp>.*/\1/p;q;}' < maven-metadata.xml)
JAR_FILE=${PROJECT}-1.0-${TIME_STAMP}-${BUILD_NUMBER}.jar
scp -q ${MAVEN_USER}@${MAVEN_HOST}:${MAVEN_REPO_PATH}/uk/ac/standrews/cs/${PROJECT}/1.0-SNAPSHOT/${JAR_FILE} .

mvn -q install:install-file -Dfile=${JAR_FILE} -DgroupId=uk.ac.standrews.cs -DartifactId=${PROJECT} -Dversion=1.0-SNAPSHOT -Dpackaging=jar
rm ${JAR_FILE}
rm maven-metadata.xml
