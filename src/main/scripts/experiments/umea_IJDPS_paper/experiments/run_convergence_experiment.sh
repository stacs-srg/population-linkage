#!/bin/sh
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

MAX_HEAP_GB="100"
RECORDS_TO_BE_CHECKED="-1"   # All records.
NUMBER_OF_RUNS="10"

"$(dirname $0)"/umea_birth_sibling.sh ${MAX_HEAP_GB} ${RECORDS_TO_BE_CHECKED} ${NUMBER_OF_RUNS}
