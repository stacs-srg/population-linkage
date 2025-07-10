#!/usr/bin/env bash
#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module population-linkage.
#
# population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with population-linkage. If not, see
# <http://www.gnu.org/licenses/>.
#

. "$(dirname $0)"/../../java/uk/ac/standrews/cs/population_linkage/thresholdAnalysers/run_threshold_analysis.sh
. "$(dirname $0)"/dolinkage_template.sh EVERYTHING
. "$(dirname $0)"/../../java/uk/ac/standrews/cs/population_linkage/resolvers/run_resolvers.sh