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

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv("../../../../../../../../../../birthbirthtri.csv")

sibling_data = data[data['is_sibling'] == True]['distance_sum']
non_sibling_data = data[data['is_sibling'] == False]['distance_sum']

fig, ax = plt.subplots(2, 2, figsize=(12, 8))

max_freq = max(
    max(np.histogram(sibling_data, bins=30)[0]),
    max(np.histogram(non_sibling_data, bins=30)[0])
)

# Histogram for siblings
ax[0, 0].hist(sibling_data, bins=30, color='blue', edgecolor='black', alpha=0.7)
ax[0, 0].set_title('Histogram of Sum of Distances (False Negatives)')
ax[0, 0].set_xlabel('Sum of Distances')
ax[0, 0].set_ylabel('Frequency')

# Histogram for non-siblings
ax[0, 1].hist(non_sibling_data, bins=30, color='orange', edgecolor='black', alpha=0.7)
ax[0, 1].set_title('Histogram of Sum of Distances (False Positives)')
ax[0, 1].set_xlabel('Sum of Distances')
ax[0, 1].set_ylabel('Frequency')

# Box plot for siblings
ax[1, 0].boxplot(sibling_data, vert=False, patch_artist=True, boxprops=dict(facecolor='blue', color='black'))
ax[1, 0].set_title('Box Plot of Sum of Distances (False Negatives)')
ax[1, 0].set_xlabel('Sum of Distances')

# Box plot for non-siblings
ax[1, 1].boxplot(non_sibling_data, vert=False, patch_artist=True, boxprops=dict(facecolor='orange', color='black'))
ax[1, 1].set_title('Box Plot of Sum of Distances (False Positives)')
ax[1, 1].set_xlabel('Sum of Distances')

plt.tight_layout()
plt.show()