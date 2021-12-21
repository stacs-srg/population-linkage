MCC02DG1QMML87:docker graham$ cat START_DB.sh

docker run -d --rm -p 7474:7474 -p 7687:7687 -v /mnt/encrypt/neo4j-docker/data:/data -v /mnt/encrypt/neo4j-docker/logs:/logs -e NEO4J_AUTH=none -e NEO4J_dbms_memory_heap_initial__size=100G -e NEO4J_dbms_memory_heap_max_size=100G -e NEO4J_dbms_memory_pagecache_size=100G -e NEO4J_dbms_jvm_additional=-XX:+ExitOnOutOfMemoryError --name neo4j-container neo4j

MCC02DG1QMML87:docker graham$ cat RESET_DB.sh

docker ps -q --filter ancestor="neo4j" | xargs -r docker stop
sudo rm -rf /mnt/encrypt/neo4j-docker/data /mnt/encrypt/neo4j-docker/logs

SCRIPT_DIRECTORY="`dirname $0`"
source ${SCRIPT_DIRECTORY}/START_DB.sh
