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
import argparse

def main(MAX_FIELD, MIN_FIELD, FILE, save):
    fig = plt.figure(figsize=(14, 8))
    threshold_total = 0
    fmeasure_total = 0

    if MAX_FIELD - MIN_FIELD == 5:
        axes = [plt.subplot2grid((3, 2), (0, 0)), plt.subplot2grid((3, 2), (0, 1)),
                plt.subplot2grid((3, 2), (1, 0)), plt.subplot2grid((3, 2), (1, 1)),
                plt.subplot2grid((3, 2), (2, 0), colspan=2)]
    elif MAX_FIELD - MIN_FIELD == 4:
        axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
                plt.subplot2grid((2, 2), (1, 0)), plt.subplot2grid((2, 2), (1, 1))]
    else:
        axes = [plt.subplot2grid((2, 2), (0, 0)), plt.subplot2grid((2, 2), (0, 1)),
                plt.subplot2grid((2, 2), (1, 0), colspan=2)]

    all_handles = []
    all_labels = []

    for i, N in enumerate(range(MAX_FIELD, MIN_FIELD, -1)):
        data = pd.read_csv(f'../../../../../../../../../{FILE}{N}.csv')

        ax1 = axes[i]
        l1 = ax1.plot(data['threshold'], data['recall'], label='Recall', color='b')
        l2 = ax1.plot(data['threshold'], data['precision'], label='Precision', color='g')
        l3 = ax1.plot(data['threshold'], data['fmeasure'], label='fmeasure', color='r')
        ax1.set_ylim([0, 1.05])
        ax1.set_ylabel('Quality Metrics\n& Open Patterns (Normalised)')
        ax1.set_xlabel('Threshold')

        if len(all_handles) == 0:
            handles, labels = ax1.get_legend_handles_labels()
            all_handles.extend(handles)
            all_labels.extend(labels)

        open_triangles_normalized = (data['total'] - data['total'].min()) / (data['total'].max() - data['total'].min())
        fnot_norm = (data['fnots'] - data['fnots'].min()) / (data['fnots'].max() - data['fnots'].min())
        fpots = data['total'] - data['fnots']
        fpot_norm = (fpots - fpots.min()) / (fpots.max() - fpots.min())

        not_zero = (fnot_norm > 0.05) | (fpot_norm > 0.05)
        valid_indices = np.where(not_zero)[0]
        intersection_index = valid_indices[np.argmin(np.abs(fnot_norm[valid_indices] - fpot_norm[valid_indices]))]
        intersection_threshold = data['threshold'].iloc[intersection_index]
        intersection_value = fnot_norm.iloc[intersection_index]
        # open_triangles_merged = (data['fnot'] + data['trianglesF']) / 2
        # open_triangles_merged = (open_triangles_merged - open_triangles_merged.min()) / (open_triangles_merged.max() - open_triangles_merged.min())

        ax2 = ax1.twinx()
        ax2.yaxis.set_visible(False)
        ax2.set_ylim([0, 1.05])
        l4 = ax2.plot(data['threshold'], open_triangles_normalized, label='Open Triangles Total', color='orange')
        l5 = ax2.plot(data['threshold'], fnot_norm, label='Number of FNOTs', color='purple')
        l6 = ax2.plot(data['threshold'], fpot_norm, label='Number of FPOTs', color='lime')

        l7 = ax2.axvline(x=intersection_threshold, color='black', linestyle='--', label='FPOT-FNOT Intersection')
        l8 = ax2.axvline(x=data['threshold'][data['fmeasure'].idxmax()], color='black', linestyle='-', label='Peak f-measure')

        if len(all_handles) == 3:
            handles, labels = ax2.get_legend_handles_labels()
            all_handles.extend(handles)
            all_labels.extend(labels)

        ax1.set_title(f'Threshold Analysis {FILE} {N} Fields')
        ax1.grid(True)

        print(f"Peak fmeasure threshold {N}: {data['threshold'][data['fmeasure'].idxmax()]}")
        print(f"Opptimal threshold estimate {N}: {intersection_threshold}")
        print(f"Difference {N}: {abs(intersection_threshold - data['threshold'][data['fmeasure'].idxmax()])}")
        threshold_total += abs(intersection_threshold - data['threshold'][data['fmeasure'].idxmax()])

        print(f"Peak F-measure {N}: {data['fmeasure'].max()}")
        print(f"Opptimal threshold fmeasure {N}: {data['fmeasure'][data['threshold'] == intersection_threshold].values[0]}")
        print(f"Difference {N}: {abs(data['fmeasure'].max() - data['fmeasure'][data['threshold'] == intersection_threshold].values[0])}")
        fmeasure_total += abs(data['fmeasure'].max() - data['fmeasure'][data['threshold'] == intersection_threshold].values[0])
        print("")

    print(f"Average threshold error: {threshold_total / (MAX_FIELD - MIN_FIELD)}")
    print(f"Average fmeasure error: {fmeasure_total / (MAX_FIELD - MIN_FIELD)}")
    fig.legend(handles=all_handles, labels=all_labels, loc='upper right', bbox_to_anchor=(1, 1), borderaxespad=1.5)
    plt.tight_layout(rect=[0, 0, 0.84, 1], pad=2)
    if save:
        plt.savefig('threshold_field_analysis_' + FILE)
    plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Threshold Analysis")
    parser.add_argument('--max', type=int, required=True, help="Maximum threshold field")
    parser.add_argument('--min', type=int, required=True, help="Minimum threshold field (1 below target)")
    parser.add_argument('--file', type=str, required=True, help="csv file name")
    parser.add_argument('--save', type=bool, required=True, help="save file")
    args = parser.parse_args()

    main(args.max, args.min - 1, args.file, args.save)
