#!/bin/sh

#####################################################################################

MAVEN_HOST="manifesto.cs.st-andrews.ac.uk"
MAVEN_USER="secure"
MAVEN_REPO_PATH="/home/secure/maven"

PROJECT="data-skye"

#####################################################################################

SCRIPT_DIRECTORY="`dirname $0`"

source ${SCRIPT_DIRECTORY}/install_data_common.sh
