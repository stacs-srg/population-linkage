#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


#####################################################################################

MAVEN_HOST="manifesto.cs.st-andrews.ac.uk"
MAVEN_USER="secure"
MAVEN_REPO_PATH="/home/secure/maven"

PROJECT="data-umea"

#####################################################################################

SCRIPT_DIRECTORY="`dirname $0`"

source ${SCRIPT_DIRECTORY}/install_data_common.sh
