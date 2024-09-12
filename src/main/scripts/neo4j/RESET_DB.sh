#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

neo4j stop
rm -rf /opt/homebrew/Cellar/neo4j@4.4.10/4.4.10/libexec/data/
neo4j-admin set-initial-password password
neo4j start
sleep 5
neo4j status
