# -*- coding: utf-8 -*-
"""
Created on Wed Oct  2 15:15:39 2024

@author: houss
"""

import helpers

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor
from sklearn.metrics import mean_squared_error
import numpy as np

df = pd.read_csv('../RL/dataset_encoded.csv')

# Split data into features (X) and target (y)
X = df[['day', 'time_period']]
y = df[['smart_lighting', 'security', 'visitor_guiding', 'maintenance', 'metaverse']]

# Split dataset into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Initialize the Decision Tree Regressor
regressor = DecisionTreeRegressor(max_depth=2, random_state=42)

# Train the model
regressor.fit(X_train, y_train)

# Predict the response time for the test set
y_pred = regressor.predict(X_test)

# Evaluate the model
mse = mean_squared_error(y_test, y_pred)
print(f"Mean Squared Error: {mse}")

days = [0,1,2,3,4,5]
time_periods = [0,1]
bandwidths = [20, 40, 60, 80, 100, 150, 200, 250, 300, 350, 400, 450, 500]
resources = [5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000]

# Find the configuration that minimizes the response time
# Create a grid of possible configurations
grid = {
    'day': days,
    'time_period': time_periods
}

# print(regressor.predict([[5, 1]]))

dt_smart_lighting, dt_security, dt_visitor_guiding, dt_maintenance, dt_metaverse = [], [], [], [], []
for day in days:
    for time in time_periods:
        results = regressor.predict([[day, time]])
        # print('{}, {}'.format(day, time), results[0][1])
        dt_smart_lighting.append(results[0][0])
        dt_security.append(results[0][1])
        dt_visitor_guiding.append(results[0][2])
        dt_maintenance.append(results[0][3])
        dt_metaverse.append(results[0][4])
        

print(dt_smart_lighting)
print(dt_security)
print(dt_visitor_guiding)
print(dt_maintenance)
print(dt_metaverse)
        

# Generate all possible combinations of configurations
# from itertools import product
# configurations = list(product(grid['day'], grid['time_period']))

# # Convert to DataFrame
# config_df = pd.DataFrame(configurations, columns=['day', 'time_period'])
# print(config_df)

# # Predict response times for all configurations
# predicted_response_times = regressor.predict(config_df)

# # Find the configuration with the minimum predicted response time
# min_index = np.argmin(predicted_response_times)
# best_configuration = config_df.iloc[min_index]
# best_response_time = predicted_response_times[min_index]

# print(f"Best Configuration: \n{best_configuration}")
# print(f"Predicted Response Time: {best_response_time}")
