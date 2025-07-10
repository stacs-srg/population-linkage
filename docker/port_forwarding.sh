#!/bin/bash
#
# This script sets up traffic-forwarding to redirect traffic intended for a
# localhost Neo4J server to the same ports on another host.
#

HOST=${NEO4J_HOST:-umea-neo4j}

# Forward ports to umea-neo4j container
socat TCP-LISTEN:7474,fork TCP:$HOST:7474 &
socat TCP-LISTEN:7687,fork TCP:$HOST:7687 &