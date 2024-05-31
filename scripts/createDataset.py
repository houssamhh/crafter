#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import csv
import pandas as pd

visitors = {'monday': {'morning': 1, 'afternoon': 2},
            'wednesday': {'morning': 1, 'afternoon': 2},
            'thursday': {'morning': 2, 'afternoon': 4},
            'friday': {'morning': 4, 'afternoon': 7},
            'saturday': {'morning': 7, 'afternoon': 9},
            'sunday': {'morning': 7, 'afternoon': 8}
            }

visitors_distribution = {'B': 0.35, 'V': 0.15, 'C': 0.05, 'G': 0.15, 'S': 0.1, 'D': 0.05, 'E': 0.15,
                         'edge1': 1, 'edge2': 1, 'metaverse': 1}

locations = ['B', 'C', 'D', 'E', 'G', 'S', 'V']
results_dir = './training/dataset/'
dataset_path = './causal-discovery/results/dataset.csv'

day_encoding = {'monday': 1, 'wednesday': 2, 'thursday': 3, 'friday': 4, 'saturday': 5, 'sunday': 6}
time_encoding = {'morning': 0, 'afternoon': 1}
location_encoding = {'B': 0, 'C': 1, 'D': 2, 'E': 3, 'G': 4, 'S': 5, 'V': 6}

def encodeDay(day):
    return day_encoding[day]

def encodeTime(timeperiod):
    return time_encoding[timeperiod]

def encodeLocation(location):
    return location_encoding[location]

data = []
for dirs in os.listdir(results_dir):
    if os.path.isdir(results_dir + dirs):
        config = dirs
        day, timeperiod, bandwidth, cpu = config.split('_')
        
        ## getting average bandwidth
        response_times_file = results_dir + config + '/metaverse/results.csv'
        df = pd.read_csv(response_times_file, names=['timestamp', 'sensorid', 'observation', 'location', 'latency'])
        response_time_mean = df.loc[:, 'latency'].mean()
        
        ## getting average cpu consumption for both edge nodes
        cpu_consumption_file_1 = results_dir + config + '/edge1/resources_monitoring.csv'
        df = pd.read_csv(cpu_consumption_file_1, names=['timestamp', 'cpu', 'mem', 'disk'])
        cpu_consumption_mean_1 = df.loc[:, 'cpu'].mean()
        cpu_consumption_file_2 = results_dir + config + '/edge2/resources_monitoring.csv'
        df = pd.read_csv(cpu_consumption_file_2, names=['timestamp', 'cpu', 'mem', 'disk'])
        cpu_consumption_mean_2 = df.loc[:, 'cpu'].mean()
        cpu_consumption_mean = ((cpu_consumption_mean_1 + cpu_consumption_mean_2) / 2) * 100 # in percent
        
        for loc in locations:
            people = str(visitors[day][timeperiod] * 7 * visitors_distribution[loc])
            day_encoded = str(encodeDay(day))
            timeperiod_encoded = str(encodeTime(timeperiod))
            loc_encoded = str(encodeLocation(loc))
            metrics = [day_encoded, timeperiod_encoded, loc_encoded, people, str(bandwidth), str(cpu), str(cpu_consumption_mean), str(response_time_mean)]
            data.append(metrics)
        
header = ['day', 'time', 'location', 'visitors', 'bandwidth', 'cpu', 'resource_consumption', 'response_time']
with open(dataset_path, 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(header)
    writer.writerows(data)
        