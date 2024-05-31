# -*- coding: utf-8 -*-

import math
import json
import pandas as pd

visitors = {'monday': {'morning': 1, 'afternoon': 3},
            'wednesday': {'morning': 2, 'afternoon': 3},
            'thursday': {'morning': 3, 'afternoon': 5},
            'friday': {'morning': 7, 'afternoon': 9},
            'saturday': {'morning': 10, 'afternoon': 13},
            'sunday': {'morning': 12, 'afternoon': 11}
            }

visitors_distribution = {'B': 0.35, 'V': 0.15, 'C': 0.05, 'G': 0.15, 'S': 0.1, 'D': 0.05, 'E': 0.15,
                         'edge1': 1, 'edge2': 1, 'metaverse': 1}

message_size = 50000
frame_size = 43000
locations = ['B', 'C', 'D', 'E', 'G', 'S', 'V']
rates = {'airquality': 0.05, 'door': 0.1, 'humidity': 0.05, 'light': 0.2, 'motion': 0.1,
         'noise': 0.2, 'proximity': 0.1, 'rfid': 0.1, 'temperature': 0.05, 'camera': 0.5}

days_map = {'monday': 0, 'wednesday': 1, 'thursday': 2, 'friday': 3, 'saturday': 4, 'sunday': 5}
timeperiods_map = {'morning': 0, 'afternoon': 1}

virtual_sensors_deployment_map = {'edge1': ['queueing', 'security'], 'edge2': ['occupancy', 'maintenance', 'environment']}

subscriptions = {'smart_lighting': ['lighting'], 'security': ['security'], 'visitor_guiding': ['occupancy', 'queueing'],
                 'maintenance': ['maintenance'], 'metaverse': ['lighting', 'noise', 'rfid']}


action_to_bandwidth_map = {0: 20, 1: 40, 2: 60, 3: 80, 4: 100, 5: 150, 6: 200, 7: 250, 8: 300, 9: 350, 10: 400, 11: 450, 12: 500}    

def mapBandwidthAction(action):
    return action_to_bandwidth_map[action]

def mapResourceAllocationAction(action):
    cpu_period = 50000
    resource_allocation = ((action + 1) / 10) * cpu_period
    return resource_allocation

def mapDay(day):
    return days_map[day]

def mapTimeperiod(timeperiod):
    return timeperiods_map[timeperiod]

def decodeDay(day):
    return list(days_map.keys())[list(days_map.values()).index(day)]

def decodeTimeperiod(timeperiod):
    return list(timeperiods_map.keys())[list(timeperiods_map.values()).index(timeperiod)]

def getNumberOfVisitors(day, timeperiod):
    return visitors[day][timeperiod] * 7

def getNumberOfVisitorsInLocation(day, timeperiod, location):
    return visitors[day][timeperiod] * 7 * visitors_distribution[location]

def encodeVisitors(nb_visitors):
    return math.floor(nb_visitors/10)

def getLinkCongestion(traffic, bandwidth):
    return (traffic / bandwidth) * 100

def mapCongestion(congestion):
    if congestion < 30:
        return 0
    elif congestion >= 30 and congestion < 50:
        return 1
    elif congestion <= 50 and congestion < 80:
        return 2
    else:
        return 3
    

def getResourceConsumption(config):
    cpu_consumption_file_1 = '../training/dataset/' + config + '/edge1/resources_monitoring.csv'
    df = pd.read_csv(cpu_consumption_file_1, names=['timestamp', 'cpu', 'mem', 'disk'])
    cpu_consumption_mean_1 = df.loc[:, 'cpu'].mean()
    cpu_consumption_file_2 = '../training/dataset/' + config + '/edge2/resources_monitoring.csv'
    df = pd.read_csv(cpu_consumption_file_2, names=['timestamp', 'cpu', 'mem', 'disk'])
    cpu_consumption_mean_2 = df.loc[:, 'cpu'].mean()
    cpu_consumption_mean = ((cpu_consumption_mean_1 + cpu_consumption_mean_2) / 2) * 100  # in percent
    return cpu_consumption_mean  
    
def mapResourceConsumption(consumption):
    if (consumption < 30):
        return 0
    elif (consumption >= 30 and consumption < 50):
        return 1
    elif (consumption >= 50 and consumption > 70):
        return 2
    else:
        return 3

    
def initializeDicts():
    response_times_dict = {}
    congestion_dict = {}
    resource_consumption_dict = {}
    
    for i in range (1, 7):
        response_times_dict[i] = dict()
        congestion_dict[i] = dict()
        resource_consumption_dict[i] = dict()
        
    for i in range (1, 7):
        for j in range (1, 3):
            response_times_dict[i][j] = dict()
            congestion_dict[i][j] = dict()
            resource_consumption_dict[i][j] = dict()
            
    for i in range (1, 7):
        for j in range (1, 3):
            for k in range (10, 60, 10):
                response_times_dict[i][j][k] = dict()
                congestion_dict[i][j][k] = dict()
                resource_consumption_dict[i][j][k] = dict()

    for i in range (1, 7):
        for j in range (1, 3):
            for k in range (10, 60, 10):
                for l in range (25, 125, 25):
                    response_times_dict[i][j][k][l] = dict()
                    congestion_dict[i][j][k][l] = dict()
                    resource_consumption_dict[i][j][k][l] = dict()
                    
    response_times_file ='../training/output.csv'
    df = pd.read_csv(response_times_file)
    for row in df.iterrows():
        day = int(row[1]['day'])
        time = int(row[1]['time'])
        # location = str(int(row[1]['location']))
        # visitors = str(int(row[1]['visitors']))
        bandwidth = int(row[1]['bandwidth'])
        cpu = int(row[1]['cpu'])
        congestion = float(row[1]['congestion'])
        resource_consumption = float(row[1]['resource_consumption'])
        response_time = int(row[1]['response_time'])
        response_times_dict[day][time][bandwidth][cpu] = response_time
        congestion_dict[day][time][bandwidth][cpu] = congestion
        resource_consumption_dict[day][time][bandwidth][cpu] = resource_consumption
        
    return response_times_dict, congestion_dict, resource_consumption_dict

def getAverageResponseTime(config):
    response_times_file ='../training/dataset/{}/metaverse/results.csv'.format(config)
    df = pd.read_csv(response_times_file, names=['timestamp', 'sensorid', 'observation', 'location', 'latency'])
    response_time_mean = df.loc[:, 'latency'].mean()
    return response_time_mean         

    
def getAverageBandwidthNeeded(nb_visitors):
    traffic = {}
    for l in locations:
        traffic[l] = 0
           
    nb_messages = {}
    for l in locations:
        nb_messages[l] = 0
        
    for loc in locations:
        file = '../deployments/{l}/deployment.json'.format(l=loc)
        f = open(file)
        data = json.load(f)
            
        for i in data['devices']:
            device = i
            if (device == 'camera'):
                traffic[loc] += (data['devices'][device] * rates['camera'] * frame_size * 8) / 1000000
                nb_messages[loc] += data['devices'][device] * rates['camera']
            elif (device == 'door' or device == 'motion' or device == 'proximity' or device == 'rfid'):
                traffic[loc] += (data['devices'][device] * rates[device] * frame_size * 8 * nb_visitors) / 1000000
                nb_messages[loc] += data['devices'][device] * rates[device] * nb_visitors
            else:
                traffic[loc] += (data['devices'][device] * rates[device] * message_size * 8) / 1000000
                nb_messages[loc] += data['devices'][device] * rates[device]
            
        f.close()
        
    average_traffic = 0
    for key, value in traffic.items():
        average_traffic += value
    return average_traffic/7
    

def getLocationResponseTime(config, location):
    file_path = '../training/dataset/{}/metaverse/results.csv'.format(config)
    df = pd.read_csv(file_path, names=['timestamp', 'sensorid', 'observation', 'location', 'latency'])
    filtered_df = df[df['observation'].str.contains('topic/{}'.format(location.lower()))]
    average_response_time = filtered_df['latency'].mean()
    return average_response_time

def getResponseTimePerApplication(config):
    response_times_dict = dict()
    file_path = '../training/dataset/{}/metaverse/results.csv'.format(config)
    df = pd.read_csv(file_path, names=['timestamp', 'sensorid', 'observation', 'location', 'latency'])
    for app, topics in subscriptions.items():
        pattern = '|'.join(topics)
        filtered_df = df[df['observation'].str.contains('/{}'.format(pattern))]
        average_response_time = filtered_df['latency'].mean()
        if math.isnan(average_response_time):
            average_response_time = 2e4
        response_times_dict[app] = average_response_time
    return response_times_dict
        
        
# returns bandwidth required for a specific location
def getLocationTraffic(location, total_visitors):
    traffic = 0  
    nb_visitors = total_visitors * visitors_distribution[location]
    file = '../deployments/{l}/deployment.json'.format(l=location)
    f = open(file)
    data = json.load(f)
        
    for i in data['devices']:
        device = i
        if (device == 'camera'):
            traffic += (data['devices'][device] * rates['camera'] * frame_size * 8) / 1000000
        elif (device == 'door' or device == 'motion' or device == 'proximity' or device == 'rfid'):
            traffic += (data['devices'][device] * rates[device] * frame_size * 8 * nb_visitors) / 1000000
        else:
            traffic+= (data['devices'][device] * rates[device] * message_size * 8) / 1000000
        
    f.close()

    return traffic


def getNodeResponseTime(config, node):
    topics = virtual_sensors_deployment_map[node.lower()]
    pattern = '|'.join(topics)
    file_path = '../training/dataset/{}/metaverse/results.csv'.format(config)
    df = pd.read_csv(file_path, names=['timestamp', 'sensorid', 'observation', 'location', 'bandwidth', 'latency'])
    filtered_df = df[df['observation'].str.contains('topic/{}'.format(pattern))]
    average_response_time = filtered_df['latency'].mean()
    return average_response_time

def getNodeResourceConsumption(config, node):
    cpu_consumption_file = '../training/dataset/{}/{}/resources_monitoring.csv'.format(config, node)
    df = pd.read_csv(cpu_consumption_file, names=['timestamp', 'cpu', 'mem', 'disk'])
    resource_consumption = df.loc[:, 'cpu'].mean()
    return resource_consumption
