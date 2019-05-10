#!/bin/sh

echo please run this from project root = population-linkage


src/main/scripts/experiments/umea_birth_father.sh 100
src/main/scripts/experiments/umea_birth_mother.sh 100
src/main/scripts/experiments/umea_birth_death.sh 100

src/main/scripts/experiments/umea_birth_sibling.sh 100
src/main/scripts/experiments/umea_death_sibling.sh 100

src/main/scripts/experiments/umea_groom_birth.sh 100
src/main/scripts/experiments/umea_bride_birth.sh 100

src/main/scripts/experiments/umea_groom_groom_sibling.sh 100
src/main/scripts/experiments/umea_bride_bride_sibling.sh 100
src/main/scripts/experiments/umea_bride_groom_sibling.sh 100
src/main/scripts/experiments/umea_groom_bride_sibling.sh 100

src/main/scripts/experiments/umea_birth_father_filtered.sh 100








