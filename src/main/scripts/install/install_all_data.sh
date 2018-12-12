#!/usr/bin/env bash

SCRIPT_DIRECTORY="`dirname $0`"

${SCRIPT_DIRECTORY}/install_kilmarnock_data.sh
${SCRIPT_DIRECTORY}/install_skye_data.sh
${SCRIPT_DIRECTORY}/install_umea_data.sh
