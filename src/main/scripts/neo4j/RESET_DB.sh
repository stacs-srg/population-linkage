#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

neo4j stop
rm -r "$(dirname "$(which neo4j)")"/../var/neo4j/data/databases/*
rm -r "$(dirname "$(which neo4j)")"/../var/neo4j/data/transactions/*
neo4j-admin set-initial-password password
neo4j start
sleep 5
neo4j status
