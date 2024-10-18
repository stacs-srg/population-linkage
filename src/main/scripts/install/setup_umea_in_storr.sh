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

export MAVEN_OPTS="-Xmx16G"

echo "Creating indices"
java -cp ~/data-umea-1.0-SNAPSHOT-jar-with-dependencies.jar uk.ac.standrews.cs.data.umea.store.CreateIndices

echo "Loading event records"
java -cp ~/data-umea-1.0-SNAPSHOT-jar-with-dependencies.jar uk.ac.standrews.cs.data.umea.store.ImportUmeaRecordsToStore
