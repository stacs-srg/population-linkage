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
from scipy.optimize import curve_fit
from scipy.stats import spearmanr

fig = plt.figure(figsize=(14, 8))

MAX_FIELD = 6
MIN_FIELD = 2 #1 under
FILE = "birthmarriage"


if FILE == "birthmarriage":
    axes = [plt.subplot2grid((3, 2), (0, 0)), plt.subplot2grid((3, 2), (0, 1)),
            plt.subplot2grid((3, 2), (1, 0)), plt.subplot2grid((3, 2), (1, 1)),
            plt.subplot2grid((3, 2), (2, 0), colspan=2)]
elif FILE == "birthdeathID" or FILE == "groomID":
    MAX_FIELD = 6
    MIN_FIELD = 2
    axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
            plt.subplot2grid((2, 2), (1, 0)), plt.subplot2grid((2, 2), (1, 1))]
else:
    axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
            plt.subplot2grid((2, 2), (1, 0), colspan=2)]

all_handles = []
all_labels = []

def quadratic_func(x, a, b, c):
    return a * x**2 + b * x + c

def exponential_func(x, a, b):
    return a * np.exp(b * x)

for i, N in enumerate(range(MAX_FIELD, MIN_FIELD, -1)):
    data = pd.read_csv(f'../../../../../../../../../{FILE}{N}.csv')

    ax1 = axes[i]
    l1 = ax1.plot(data['threshold'], data['recall'], label='Recall', color='b')
    l2 = ax1.plot(data['threshold'], data['precision'], label='Precision', color='g')
    l3 = ax1.plot(data['threshold'], data['fmeasure'], label='fmeasure', color='r')

    if len(all_handles) == 0:
        handles, labels = ax1.get_legend_handles_labels()
        all_handles.extend(handles)
        all_labels.extend(labels)

    GRADIENT = 0

    def walker(gradient):
        for i in range(int(len(gradient) / 2), len(gradient) - 1):
            if round(gradient[i],3) < GRADIENT + 0.1 and round(gradient[i + 1],3) >= GRADIENT - 0.1:
                return i
        return None

    open_triangles_normalized = (data['triangles'] - data['triangles'].min()) / (data['triangles'].max() - data['triangles'].min())
    open_triangles_smooth = open_triangles_normalized.rolling(window=5, min_periods=1).mean()
    open_triangles_gradient = np.gradient(open_triangles_smooth, data['threshold'])
    # optimal_threshold = walker(open_triangles_gradient)

    # ax1.set_xlabel('Threshold')
    # ax1.set_ylabel('Metrics', color='black')
    # ax1.set_ylim(0.0, 1.01)

    # ax1.legend(loc='upper left')

    # data['triangle_diff'] = open_triangles_normalized.diff()
    # average_diff = data['triangle_diff'].mean()
    # sharp_increase_index = data[data['triangle_diff'] > 2 * average_diff].index
    # if not sharp_increase_index.empty:
    #     optimal_index_sharp = sharp_increase_index[0] - 1

    # data['triangle_diff'] = data['triangles_smoothed'].diff()

    # data['triangle_acceleration'] = data['triangle_diff'].diff()
    # acceleration_start_threshold = data['triangle_acceleration'].mean() * 3.5
    # acceleration_start_indices = data[data['triangle_acceleration'] > acceleration_start_threshold].index
    # optimal_index = data['triangle_acceleration'].idxmax()
    # valid_acceleration_indices = data[data['triangle_acceleration'] < acceleration_threshold].index
    # optimal_index_begin_growth = acceleration_start_indices[0] - 1
    # optimal_threshold = data.loc[optimal_index, 'threshold']

    params, _ = curve_fit(exponential_func, data['threshold'], open_triangles_normalized, maxfev=10000)
    a, b = params

    fitted_curve = exponential_func(data['threshold'], a, b)
    # fitted_curve_gradient = np.gradient(fitted_curve, data['threshold'])
    optimal_threshold = walker(open_triangles_gradient)

    ax2 = ax1.twinx()
    # ax2.plot(data['threshold'], data['squares'], label='Open Triangles', color='orange')
    l4 = ax2.plot(data['threshold'], open_triangles_normalized, label='Open Triangles', color='orange')
    # l5 = ax2.plot(data['threshold'], fitted_curve, '--', label='Best fit', color='purple')
    # ax2.plot(data['threshold'], open_triangles_gradient, label='Open Triangles Dif', color='purple')
    # ax2.plot(data['threshold'][optimal_threshold], open_triangles_normalized[optimal_threshold], 'o', color='orange', label='Max Open Triangles Gradient')
    l6 = ax2.axvline(x=data.loc[optimal_threshold, 'threshold'], color='black', linestyle='--', linewidth=2, label='Optimal Threshold')
    # l7 = ax2.axvline(x=data.loc[optimal_index_begin_growth, 'threshold'], color='black', linestyle='--', label='Start of growth')
    # l8 = ax2.axvline(x=data.loc[optimal_index_sharp, 'threshold'], color='pink', linestyle='--', label='Max growth')
    # ax2.plot(data['threshold'], data['squares'], label='Open Squares', color='orange')
    # ax2.plot(data['threshold'], data['strict_squares'], label='Open Squares (Strict)', color='purple')

    if len(all_handles) == 3:
        handles, labels = ax2.get_legend_handles_labels()
        all_handles.extend(handles)
        all_labels.extend(labels)

    ax2.set_ylabel('Open Triangles')

    # ax2.legend(loc='lower left')

    ax1.set_title(f'Threshold Analysis {FILE} {N} Fields')
    ax1.grid(True)
    correlation, p_value = spearmanr(data['fmeasure'], data['triangles'])
    print(f"Spearman Correlation {N} fields: {correlation}")
    print(f"P-value {N} fields: {p_value}")
    print(f"Optimal Threshold for field {N}: {data['threshold'][optimal_threshold]}")
    print(f"Peak fmeasure threshold {N}: {data['threshold'][data['fmeasure'].idxmax()]}")

# fig.legend(loc='center left', bbox_to_anchor=(1, 0.5))
fig.legend(handles=all_handles, labels=all_labels, loc='center right', bbox_to_anchor=(1, 0.5))

plt.tight_layout(rect=[0, 0, 0.9, 1])
# plt.savefig('threshold_field_analysis_groomsq.png')
plt.show()