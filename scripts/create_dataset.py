# -*- coding: utf-8 -*-
"""
Created on Wed Oct  2 14:44:09 2024

@author: houss
"""

import helpers

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor
from sklearn.metrics import mean_squared_error
import numpy as np

days = ['monday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
time_periods = ['morning', 'afternoon']
bandwidths = [20, 40, 60, 80, 100, 150, 200, 250, 300, 350, 400, 450, 500]
resources = [5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000]

days_map = {'monday': 0, 'wednesday': 1, 'thursday': 2, 'friday': 3, 'saturday': 4, 'sunday': 5}
timeperiods_map = {'morning': 0, 'afternoon': 1}

data = {
        'day': [],
        'time_period': [],
        'bandwidth': [],
        'resources': [],
        'smart_lighting': [],
        'security': [],
        'visitor_guiding': [],
        'maintenance': [],
        'metaverse': []
        }
i=0
for day in days:
    for time_period in time_periods:
        for bandwidth in bandwidths:
            for resource in resources:
                config = '{}_{}_{}_{}'.format(day, time_period, bandwidth, resource)
                response_times = helpers.getResponseTimePerApplication(config)
                data['day'].append(days_map[day])
                data['time_period'].append(timeperiods_map[time_period])
                data['bandwidth'].append(bandwidth)
                data['resources'].append(resource)
                data['smart_lighting'].append(response_times['smart_lighting'])
                data['security'].append(response_times['security'])
                data['visitor_guiding'].append(response_times['visitor_guiding'])
                data['maintenance'].append(response_times['maintenance'])
                data['metaverse'].append(response_times['metaverse'])
                
                
df = pd.DataFrame(data)                
df.to_csv('../RL/dataset_encoded.csv', index=False)