#!/bin/sh
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


echo please run this from project root = population-linkage


src/main/scripts/experiments/umea_birth_father_identity.sh 100
src/main/scripts/experiments/umea_birth_mother_identity.sh 100
src/main/scripts/experiments/umea_birth_death_identity.sh 100

src/main/scripts/experiments/umea_birth_sibling.sh 100
src/main/scripts/experiments/umea_death_sibling.sh 100

src/main/scripts/experiments/umea_birth_groom_identity.sh 100
src/main/scripts/experiments/umea_birth_bride_identity.sh 100

src/main/scripts/experiments/umea_groom_groom_sibling.sh 100
src/main/scripts/experiments/umea_bride_bride_sibling.sh 100
src/main/scripts/experiments/umea_bride_groom_sibling.sh 100
