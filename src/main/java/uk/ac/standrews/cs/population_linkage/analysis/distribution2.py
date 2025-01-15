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

df = pd.read_csv('../../../../../../../../../../birthbirthtri2.csv')

df['distance_diff'] = df['max_distance'] - df['average_distance']

true_sibling = df[df['has_GT_SIBLING'] == True]['distance_diff']
false_sibling = df[df['has_GT_SIBLING'] == False]['distance_diff']

plt.figure(figsize=(8, 4))

# Histogram for max - average distances in cluster
plt.hist(false_sibling, bins=20, alpha=0.5, label='False Positives', color='orange')
plt.hist(true_sibling, bins=20, alpha=0.5, label='False Negatives', color='blue')

plt.xlabel('Distance Difference (max - avg)')
plt.ylabel('Frequency')
plt.title('Histogram of Distance Difference by Error Type')
plt.legend(loc='upper right')

plt.tight_layout()
plt.show()

# Histogram for number of nodes in a cluster
plt.figure(figsize=(8, 4))
plt.hist(df['link_num'], bins=100, alpha=0.7, edgecolor='black', color='blue')

plt.xlabel('Number of open triangles')
plt.ylabel('Frequency')
plt.title('Number of Open Triangles per Cluster')

plt.tight_layout()
plt.show()
