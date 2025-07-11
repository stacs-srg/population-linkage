#!/bin/bash
#
# This script sets up port-forwarding for the container and calls the runall.sh
# runner script.
#

# Start traffic forwarding
./port_forwarding.sh

# Run all
./src/main/scripts/endtoend/runall.sh