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

import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv('../../../../../../../../../birthbirthhalfplus.csv')

fig, ax1 = plt.subplots(figsize=(10, 6))

ax1.plot(data['threshold'], data['recall'], label='Recall', color='b')
ax1.plot(data['threshold'], data['precision'], label='Precision', color='g')
#ax1.plot(data['threshold'], data['fmeasure'], label='F1 Score', color='r')

ax1.set_xlabel('Threshold')
ax1.set_ylabel('Metrics', color='black')
ax1.set_ylim(0, 1.1)

ax1.legend(loc='upper left')

ax2 = ax1.twinx()

ax2.plot(data['threshold'], data['triangles'], label='Triangles', color='orange')

ax2.set_ylabel('Triangles')

ax2.legend(loc='upper right')

plt.title('Threshold Analysis Birth-Birth Sibling +1')
plt.grid(True)

plt.show()