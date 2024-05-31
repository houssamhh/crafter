# -*- coding: utf-8 -*-


import pandas as pd
import matplotlib.pyplot as plt
import math
import os

import helpers as helper


from stable_baselines3 import PPO
from GlobalAgents import QosIotEnv, ContextIotEnv, HybridIotEnv

# qos_model_path ='.../training/iot/models/ppo_global_qos'
qos_model_path ='..\\training\\iot\\models\\sotarl_lowerpenalty_100'
qos_env = QosIotEnv()
qos_model = PPO.load(qos_model_path, env=qos_env)

# context_model_path = '.../training/iot/models/ppo_global_context'
context_model_path = '..\\training\\iot\\models\\crafter_lowerpenalty_100'
context_env = ContextIotEnv()
context_model = PPO.load(context_model_path, env=context_env)


hybrid_model_path = '../training/iot/models/hybrid_lowerpenalty_100'
hybrid_env = HybridIotEnv()
hybrid_model = PPO.load(hybrid_model_path, env=hybrid_env)


subscriptions = {'smart_lighting': ['lighting'], 'security': ['security'], 'visitor_guiding': ['occupancy', 'queueing'],
                 'maintenance': ['maintenance'], 'metaverse': ['lighting', 'noise', 'rfid']}

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

def getApplicationResponseTime(config, application):
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
    return response_times_dict[application]


initial_bandwidth = 100
initial_cpu_allocation = 15000

days = ['monday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
timeperiods = ['morning', 'afternoon']

app='maintenance'
baseline_latency = []
rl_latency = []
cd_latency = []
hybrid_latency = []
for day in days:
    for timeperiod in timeperiods:
        visitors = helper.getNumberOfVisitors(day, timeperiod)
        link_congestion = helper.getLinkCongestion(helper.getAverageBandwidthNeeded(visitors), initial_bandwidth)
        resource_consumption = helper.getResourceConsumption('{}_{}_{}_{}'.format(day, timeperiod, str(initial_bandwidth), str(initial_cpu_allocation)))
        
        baseline_config = '{}_{}_{}_{}'.format(day, timeperiod, str(initial_bandwidth), str(initial_cpu_allocation))
        baseline_latency.append(getApplicationResponseTime(baseline_config, app))
        
        
        rl_actions = qos_model.predict([helper.mapCongestion(link_congestion), helper.mapResourceConsumption(resource_consumption)])
        rl_bandwidth = helper.mapBandwidthAction(rl_actions[0][0])
        rl_cpu_allocation = int(helper.mapResourceAllocationAction(rl_actions[0][1]))
        rl_config = '{}_{}_{}_{}'.format(day, timeperiod, str(rl_bandwidth), str(rl_cpu_allocation))
        rl_latency.append(getApplicationResponseTime(rl_config, app))
        
        
        cd_actions = context_model.predict([helper.mapDay(day), helper.mapTimeperiod(timeperiod), helper.encodeVisitors(visitors)], deterministic=False)
        cd_bandwidth = helper.mapBandwidthAction(cd_actions[0][0])
        cd_cpu_allocation = int(helper.mapResourceAllocationAction(cd_actions[0][1]))
        cd_config = '{}_{}_{}_{}'.format(day, timeperiod, str(cd_bandwidth), str(cd_cpu_allocation))
        print(cd_config, cd_actions)
        cd_latency.append(getApplicationResponseTime(cd_config, app))
        
        hybrid_actions = hybrid_model.predict([helper.mapDay(day), helper.mapTimeperiod(timeperiod), helper.encodeVisitors(visitors), helper.mapCongestion(link_congestion), helper.mapResourceConsumption(resource_consumption)])
        hybrid_bandwidth = helper.mapBandwidthAction(hybrid_actions[0][0])
        hybrid_cpu_allocation = int(helper.mapResourceAllocationAction(hybrid_actions[0][1]))
        hybrid_config = '{}_{}_{}_{}'.format(day, timeperiod, str(hybrid_bandwidth), str(hybrid_cpu_allocation))
        hybrid_latency.append(getApplicationResponseTime(hybrid_config, app))


# labels = ['M_M', 'M_A', 'W_M', 'W_A', 'T_M', 'T_A', 'F_M', 'F_A', 'S_M', 'S_A', 'D_M', 'D_A']
labels = ['Monday-AM', 'Monday-PM', 'Wednesday-AM', 'Wednesday-PM', 'Thursday-AM', 'Thursday-PM',
          'Friday-AM', 'Friday-PM', 'Saturday-AM', 'Saturday-PM', 'Sunday-AM', 'Sunday-PM']

qos = {'metaverse': 20, 'smart_lighting': 500, 'maintenance': 2000, 'visitor_guiding': 500, 'security': 1000}

markersize=4
plt.plot(labels, baseline_latency, label='No adaptation', marker='+', markersize=markersize, color='blue')
plt.axhline(y=qos[app], linestyle='--', label='QoS requirements', color='red')
plt.plot(labels, rl_latency, label='SOTA-RL', marker = 'v', markersize=markersize, color='maroon')
plt.plot(labels, cd_latency, label='CRAFTER', marker='s', markersize=markersize, color='green')
plt.plot(labels, hybrid_latency, label='Hybrid-RL', marker='o', markersize=markersize, color='orange')
plt.xlabel('Period')
plt.ylabel('Average Response Time (ms)')
plt.xticks(rotation=35, ha='right')
plt.legend()
plt.grid(visible=True)
# plt.ylim(0, 1000)
plot_name = '../plots/response_times_plot_adaptation_{}.pdf'.format(app)
plt.savefig(plot_name, dpi=600, bbox_inches='tight') 
plt.show()
        
# labels = ['M_M', 'M_A', 'W_M', 'W_A', 'T_M', 'T_A', 'F_M', 'F_A', 'S_M', 'S_A', 'D_M', 'D_A']

# plt.plot(labels, latency_metaverse, label='metaverse')
# plt.plot(labels, latency_guiding, label='guiding')
# plt.plot(labels, latency_lighting, label='lighting')
# plt.plot(labels, latency_maintenance, label='maintenance')
# plt.plot(labels, latency_security, label='security')
# plt.legend()
# plt.savefig('response_times_plot_cd.png')
