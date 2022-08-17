#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

REPO_ROOT=/Users/al/repos/github

# Install Umea data

/bin/bash $REPO_ROOT/data-umea/src/main/scripts/install/setup_umea_in_storr.sh

# Install the Ground truth 

/bin/bash  "$(dirname $0)"/establish_gt.sh

# Do the linkage

/bin/bash  "$(dirname $0)"/../endtoend/dolinkage_all.sh

