# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats

# Parameters for the first normal distribution
mean1 = 35.6069534060359
std_dev1 = 2.334939480024699

# Parameters for the second normal distribution
mean2 = 44.63040479008109
std_dev2 = 2.2120062646125156

mean3 = 46.89349507421255
std_dev3 = 15.702882827583213

# Generate x values
x = np.linspace(0, 70, 1000)

# Generate y values for the first normal distribution
y1 = stats.norm.pdf(x, mean1, std_dev1)

# Generate y values for the second normal distribution
y2 = stats.norm.pdf(x, mean2, std_dev2)

y3 = stats.norm.pdf(x, mean3, std_dev3)

# Plot the first normal distribution
plt.plot(x, y1, label='SOTA-RL', color='maroon')

# Plot the second normal distribution
plt.plot(x, y2, label='CRAFTER', color='green')

plt.plot(x, y3, label='Hybrid-RL', color='orange')

# Add labels and title
plt.xlabel('Reward Value')
plt.ylabel('Probability Density')
# plt.title('Distribution of evaluation reward')
plt.legend()
plt.grid(visible=True)

# Show the plot
plt.savefig('../plots/evaluation_reward.pdf', dpi=600, bbox_inches='tight') 
plt.show()
