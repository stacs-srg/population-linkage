#!/bin/sh
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

MAX_HEAP_GB="100"
RECORDS_TO_BE_CHECKED="25000"
NUMBER_OF_RUNS="1"
OUTPUT_DIR="full-experiment"      # Blank for project root directory.

"$(dirname $0)"/umea_birth_father_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
"$(dirname $0)"/umea_birth_mother_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#"$(dirname $0)"/umea_birth_death_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#
#"$(dirname $0)"/umea_birth_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#"$(dirname $0)"/umea_death_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#
#"$(dirname $0)"/umea_birth_groom_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#"$(dirname $0)"/umea_birth_bride_identity.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#
#"$(dirname $0)"/umea_groom_groom_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#"$(dirname $0)"/umea_bride_bride_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
#"$(dirname $0)"/umea_bride_groom_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS} ${OUTPUT_DIR}
