#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

neo4j stop
neo4j-admin load --verbose  --from=$1   --force
neo4j start
neo4j status
