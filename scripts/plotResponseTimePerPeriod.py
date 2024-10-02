# -*- coding: utf-8 -*-


import pandas as pd
import matplotlib.pyplot as plt
import math
import os

import helpers as helper


from stable_baselines3 import PPO
from RLAgents import QosIotEnv, ContextIotEnv, HybridIotEnv

# qos_model_path ='.../training/iot/models/ppo_global_qos'
qos_model_path ='..\\RL\\iot\\models\\sotarl'
qos_env = QosIotEnv()
qos_model = PPO.load(qos_model_path, env=qos_env)

# context_model_path = '.../training/iot/models/ppo_global_context'
context_model_path = '..\\RL\\iot\\models\\crafter'
context_env = ContextIotEnv()
context_model = PPO.load(context_model_path, env=context_env)


hybrid_model_path = '../RL/iot/models/hybrid'
hybrid_env = HybridIotEnv()
hybrid_model = PPO.load(hybrid_model_path, env=hybrid_env)


subscriptions = {'smart_lighting': ['lighting'], 'security': ['security'], 'visitor_guiding': ['occupancy', 'queueing'],
                 'maintenance': ['maintenance'], 'metaverse': ['lighting', 'noise', 'rfid']}

def getResponseTimePerApplication(config):
    response_times_dict = dict()
    file_path = '../RL/dataset/{}/metaverse/results.csv'.format(config)
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
    file_path = '../RL/dataset/{}/metaverse/results.csv'.format(config)
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



# Decision trees data:
dt_smart_lighting = [24.37335676444832, 19.01378237960799, 19.53047773680003, 19.710940562901552, 18.90887226491652, 31.415592220458944, 30.816788482798277, 37.41545483043536, 36.48318324400406, 47.747922994612104, 49.28584571128964, 48.88921605962312]
dt_security = [29.929944888496113, 33.87433336203824, 30.077004250130535, 30.9980383287724, 33.326954776670135, 3038.704190437791, 2910.9725135022454, 2842.881977011697, 2630.683682704002, 4308.984031798919, 3669.747965128901, 3813.38694846783]
dt_visitor_guiding = [998.3899829476304, 1340.1031339263463, 1290.96465247611, 1369.3037795005857, 1424.296082303264, 1369.7089103949877, 1450.993889271122, 2963.4186967938936, 2815.6733044804946, 4054.3904377208605, 3528.902450609036, 3646.5285862695227]
dt_maintenance = [76.92204626292033, 80.3154810736496, 76.07730366495205, 81.64603550445968, 81.60856669940218, 130.66334803488272, 293.8441791997866, 4846.5839138163465, 4701.583167878466, 5259.908298009089, 5305.524195716672, 4822.3802606287945]
dt_metaverse = [24.267419664660416, 19.494508051008083, 19.554737756626626, 19.638775678904253, 19.056766448379296, 30.351898858654504, 29.905341095111616, 35.898996323533076, 35.305441850724954, 46.31410374030574, 47.91539262297275, 47.86714319820812]

ga_smart_lighting = [13.048850574712644, 11.080459770114942, 14.698863636363637, 13.69942196531792, 13.790229885057473, 15.40625, 14.07183908045977, 18.374269005847957, 51.94857142857143, 17.28488372093023, 25.26453488372093, 15.817142857142857]
ga_security = [13.556410256410256, 17.182456140350876, 14.383505154639176, 20.02233676975945, 16.681081081081082, 507.6779463243874, 1348.27065351418, 2106.8686087990486, 272.56028368794324, 413.9670588235294, 2051.029733959311, 1051.179104477612]
ga_visitor_guiding = [69.22535211267606, 74.8984375, 93.67857142857144, 81.4535519125683, 83.02673796791444, 79.39893617021276, 71.75, 290.35403726708074, 183.8963210702341, 240.6598240469208, 204.3860759493671, 172.7202572347267]
ga_maintenance = [25.378006872852232, 22.31873479318735, 20.8764367816092, 20.91646191646192, 26.34567901234568, 24.373382624768947, 19.40066225165563, 1582.4634146341464, 5624.054988216811, 3418.32424677188, 663.3333333333334, 1972.2384868421047]
ga_metaverse = [14.22992700729927, 13.007155635062611, 15.82661996497373, 14.418647166361977, 17.527927927927927, 16.635231316725978, 15.4348623853211, 18.640535372848948, 49.1957773512476, 19.71669793621013, 26.30916030534351, 16.81679389312977]

if app =='smart_lighting':
    decision_tree_latency = dt_smart_lighting
elif app == 'maintenance':
    decision_tree_latency = dt_maintenance
elif app == 'visitor_guiding':
    decision_tree_latency = dt_visitor_guiding
elif app == 'security':
    decision_tree_latency = dt_security
elif app == 'metaverse':
    decision_tree_latency = dt_metaverse
    
if app =='smart_lighting':
    genetic_algo_latency = ga_smart_lighting
elif app == 'maintenance':
    genetic_algo_latency = ga_maintenance
elif app == 'visitor_guiding':
    genetic_algo_latency = ga_visitor_guiding
elif app == 'security':
    genetic_algo_latency = ga_security
elif app == 'metaverse':
    genetic_algo_latency = ga_metaverse
# labels = ['M_M', 'M_A', 'W_M', 'W_A', 'T_M', 'T_A', 'F_M', 'F_A', 'S_M', 'S_A', 'D_M', 'D_A']
labels = ['Monday-AM', 'Monday-PM', 'Wednesday-AM', 'Wednesday-PM', 'Thursday-AM', 'Thursday-PM',
          'Friday-AM', 'Friday-PM', 'Saturday-AM', 'Saturday-PM', 'Sunday-AM', 'Sunday-PM']

qos = {'metaverse': 20, 'smart_lighting': 500, 'maintenance': 2000, 'visitor_guiding': 500, 'security': 1000}


markersize=5
plt.plot(labels, baseline_latency, label='No adaptation', marker='+', markersize=markersize, color='blue')
plt.axhline(y=qos[app], linestyle='--', label='QoS requirements', color='red')
plt.plot(labels, decision_tree_latency, label='Decision Tree', marker='*', markersize=markersize, color='grey')
plt.plot(labels, genetic_algo_latency, label='Genetic Algorithm', marker='X', markersize=markersize, color='purple')
plt.plot(labels, rl_latency, label='SOTA-RL', marker = 'v', markersize=markersize, color='maroon')
plt.plot(labels, cd_latency, label='CRAFTER-Context Only', marker='s', markersize=markersize, color='green')
plt.plot(labels, hybrid_latency, label='CRAFTER-Mixed', marker='o', markersize=markersize, color='orange')


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
