#!/bin/sh
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

MAX_HEAP_GB="100"
RECORDS_TO_BE_CHECKED="25000"
NUMBER_OF_RUNS="1"
# Sets results directory to be unique
OUTPUT_DIR="results/IJDPS_paper/$(date '+%Y%m%d_%H%M%S')"

# Create results directory if it doesn't already exist
mkdir -p $OUTPUT_DIR

"$(dirname $0)"/umea_birth_father_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_birth_mother_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_birth_death_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}

"$(dirname $0)"/umea_birth_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_death_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}

"$(dirname $0)"/umea_birth_groom_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_birth_bride_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}

"$(dirname $0)"/umea_groom_groom_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_bride_bride_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_bride_groom_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
