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

fig = plt.figure(figsize=(10, 8))

MAX_FIELD = 4
MIN_FIELD = 1

# axes = [plt.subplot2grid((3, 2), (0, 0)), plt.subplot2grid((3, 2), (0, 1)),
#         plt.subplot2grid((3, 2), (1, 0)), plt.subplot2grid((3, 2), (1, 1)),
#         plt.subplot2grid((3, 2), (2, 0), colspan=2)]

# axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
#         plt.subplot2grid((2, 2), (1, 0)), plt.subplot2grid((2, 2), (1, 1))]

axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
        plt.subplot2grid((2, 2), (1, 0), colspan=2)]

for i, N in enumerate(range(MAX_FIELD, MIN_FIELD, -1)):
    data = pd.read_csv(f'../../../../../../../../../deathdeath{N}.csv')

    ax1 = axes[i]
    ax1.plot(data['threshold'], data['recall'], label='Recall', color='b')
    ax1.plot(data['threshold'], data['precision'], label='Precision', color='g')
    # ax1.plot(data['threshold'], data['fmeasure'], label='fmeasure', color='r')

    ax1.set_xlabel('Threshold')
    ax1.set_ylabel('Metrics', color='black')
    ax1.set_ylim(0.0, 1.01)

    ax1.legend(loc='upper left')

    ax2 = ax1.twinx()
    ax2.plot(data['threshold'], data['triangles'], label='Open Triangles', color='orange')

    ax2.set_ylabel('Open Triangles')

    ax1.set_title(f'Threshold Analysis Birth-Death ID {N} Fields')
    ax1.grid(True)

plt.tight_layout()
# plt.savefig('threshold_field_analysis_dd.png')
plt.show()