import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv('../../../../../../../../../birthbirthtri2.csv')

df['distance_diff'] = df['max_distance'] - df['average_distance']

true_sibling = df[df['has_GT_SIBLING'] == True]['distance_diff']
false_sibling = df[df['has_GT_SIBLING'] == False]['distance_diff']

plt.hist(true_sibling, bins=10, alpha=0.5, label='True Sibling', color='blue')
plt.hist(false_sibling, bins=10, alpha=0.5, label='False Sibling', color='orange')

plt.xlabel('Distance Difference (max - avg)')
plt.ylabel('Frequency')
plt.title('Histogram of Distance Difference by Sibling Status')
plt.legend(loc='upper right')

plt.show()
