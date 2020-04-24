#!/usr/bin/env bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


SCRIPT_DIRECTORY="`dirname $0`"

${SCRIPT_DIRECTORY}/install_kilmarnock_data.sh
${SCRIPT_DIRECTORY}/install_skye_data.sh
${SCRIPT_DIRECTORY}/install_umea_data.sh
