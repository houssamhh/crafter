# Import helpers
import numpy as np
import random
import os
import math

# Import Gym
from gymnasium import Env
from gymnasium.spaces import MultiDiscrete

# Import Stable baselines
from stable_baselines3 import PPO
from stable_baselines3.common.evaluation import evaluate_policy
# from stable_baselines3.common.env_checker import check_env

import helpers as helper

qos_map = {'smart_lighting': 1000, 'security': 1000, 'visitor_guiding': 1000,
                 'maintenance': 1000, 'metaverse': 20}

qos_weights = {'smart_lighting': 1, 'security': 1, 'visitor_guiding': 1,
                 'maintenance': 1, 'metaverse': 5}

def calculateReward(response_time_means, bandwidth, cpu_allocation):
    reward = 0
    for app in response_time_means.keys():
        reward += ((qos_map[app] - response_time_means[app])/qos_map[app]) * qos_weights[app] - ((0.001 * bandwidth) - (cpu_allocation / 50000))
    if math.isnan(reward):
        raise ValueError("Reward is NaN: ")
    return reward


# map actions to possible bandwidth values
def mapBandwidthAction(action):
    action_to_bandwidth_map = {0: 20, 1: 40, 2: 60, 3: 80, 4: 100, 5: 150, 6: 200, 7: 250, 8: 300,
                               9: 350, 10: 400, 11: 450, 12: 500}
    return action_to_bandwidth_map[action]

def mapResourceAllocationAction(action):
    cpu_period = 50000
    resource_allocation = ((action + 1) / 10) * cpu_period
    return resource_allocation
    
    
class QosIotEnv(Env):
    
    day = ''
    timeperiod = ''
    
    index = 1
    
    def __init__(self):
        
        #  bandwidth: 20 Mbps --> 500 Mbps | cpu allocation: 10% --> 100% (increments of 10)
        self.action_space = MultiDiscrete([13, 10])        
        
        # congestion, resource consumption
        self.observation_space = MultiDiscrete([4, 4])
        
        self.state = MultiDiscrete([4, 4]).sample()
        
        # we can take 5 actions in one episode
        self.config_length = 12
        
    def step(self, action):
        bandwidth = mapBandwidthAction(action[0])
        cpu_allocation = mapResourceAllocationAction(action[1])       
        
        config = '{day}_{timeperiod}_{bandwidth}_{cpu}'.format(day=self.day, timeperiod=self.timeperiod, bandwidth=bandwidth, cpu=int(cpu_allocation))
        self.config_length -= 1
        
        response_time_means = helper.getResponseTimePerApplication(config)
        
        current_day = helper.mapDay(self.day)
        current_time = helper.mapTimeperiod(self.timeperiod)
        if current_time == 0:
            next_day = current_day
            next_time = current_time + 1
        else:
            next_day = (current_day + 1) % 6
            next_time = 0
        
        
        self.day = helper.decodeDay(next_day)
        self.timeperiod = helper.decodeTimeperiod(next_time)
        self.nb_visitors = helper.getNumberOfVisitors(self.day, self.timeperiod)
        
        config = '{day}_{timeperiod}_{bandwidth}_{cpu}'.format(day=self.day, timeperiod=self.timeperiod, bandwidth=bandwidth, cpu=int(cpu_allocation))
        
        traffic = helper.getAverageBandwidthNeeded(self.nb_visitors)
        congestion = helper.getLinkCongestion(traffic, bandwidth)
        resource_consumption = helper.getResourceConsumption(config)
        
        self.state = [helper.mapCongestion(congestion), helper.mapResourceConsumption(resource_consumption)]
        
        reward = calculateReward(response_time_means, bandwidth, cpu_allocation)
        
        if (self.config_length <= 0):
            done = True
        else:
            done = False
        
            
        info = {}
        truncated = False

        return self.state, reward, done, truncated, info
    
    def render(self):
        # Implement visualization if needed
        pass
    
    def reset(self, seed=None, options=None):
         resource_consumption = random.randint(0, 3)
         link_congestion = random.randint(0, 3)
         days = ['monday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
         times = ['morning', 'afternoon']
         self.day = random.choice(days)
         self.timeperiod = random.choice(times)
         self.nb_visitors = helper.getNumberOfVisitors(self.day, self.timeperiod)
         params = [link_congestion, resource_consumption]
         self.state = np.array(params)
         self.config_length = 12
         return self.state, {}
     
        
    
class ContextIotEnv(Env):
    
    day = ''
    timeperiod = ''
    
    def __init__(self):
        
        # First value: bandwidth, Second value: cpu allocation
        self.action_space = MultiDiscrete([13, 10])        
        
        # day, time, visitors
        self.observation_space = MultiDiscrete([6, 2, 12])
        
        self.state = MultiDiscrete([6, 2, 12]).sample()
        
        self.config_length = 12
        
    def step(self, action):
        bandwidth = mapBandwidthAction(action[0])
        cpu_allocation = mapResourceAllocationAction(action[1])
        
        config = '{day}_{timeperiod}_{bandwidth}_{cpu}'.format(day=self.day, timeperiod=self.timeperiod, bandwidth=bandwidth, cpu=int(cpu_allocation))
        self.config_length -= 1
        
        response_time_means = helper.getResponseTimePerApplication(config)
        
        
        # state transition: go to next timeperiod
        current_day = helper.mapDay(self.day)
        current_time = helper.mapTimeperiod(self.timeperiod)
        if current_time == 0:
            next_day = current_day
            next_time = current_time + 1
        else:
            next_day = (current_day + 1) % 6
            next_time = 0
        
        
        self.day = helper.decodeDay(next_day)
        self.timeperiod = helper.decodeTimeperiod(next_time)
        self.nb_visitors = helper.getNumberOfVisitors(self.day, self.timeperiod)
        self.state = [next_day, next_time, helper.encodeVisitors(self.nb_visitors)]
        
        reward = calculateReward(response_time_means, bandwidth, cpu_allocation)
        
        
        if (self.config_length <= 0):
            done = True
        else:
            done = False
            
        info = {}
        truncated = False
        
        
        return self.state, reward, done, truncated, info
    
    def render(self):
        pass
    
    def reset(self, seed=None, options=None):
         days = ['monday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
         times = ['morning', 'afternoon']
         self.day = random.choice(days)
         self.timeperiod = random.choice(times)
         self.nb_visitors = helper.getNumberOfVisitors(self.day, self.timeperiod)
         params = [helper.mapDay(self.day), helper.mapTimeperiod(self.timeperiod), helper.encodeVisitors(self.nb_visitors)]
         self.state = np.array(params)
         self.average_reward = 0
         self.config_length = 12
         return self.state, {}
     
        
class HybridIotEnv(Env):
    
    day = ''
    timeperiod = ''
    
    def __init__(self):
        
        self.action_space = MultiDiscrete([13, 10])        
        
        # day, time, visitors, link congesiton, resource_consumption
        self.observation_space = MultiDiscrete([6, 2, 12, 4, 4])
        
        self.state = MultiDiscrete([6, 2, 12, 4, 4]).sample()
        
        self.config_length = 12
        
    def step(self, action):
        bandwidth = mapBandwidthAction(action[0])
        cpu_allocation = mapResourceAllocationAction(action[1])
        
        config = '{day}_{timeperiod}_{bandwidth}_{cpu}'.format(day=self.day, timeperiod=self.timeperiod, bandwidth=bandwidth, cpu=int(cpu_allocation))
        self.config_length -= 1
        
        response_time_means = helper.getResponseTimePerApplication(config)
        
        traffic = helper.getAverageBandwidthNeeded(self.nb_visitors)
        congestion = helper.getLinkCongestion(traffic, bandwidth)
        resource_consumption = helper.getResourceConsumption(config)
        
        self.state = [helper.mapDay(self.day), helper.mapTimeperiod(self.timeperiod), helper.encodeVisitors(self.nb_visitors), helper.mapCongestion(congestion), helper.mapResourceConsumption(resource_consumption)]
    
        reward = calculateReward(response_time_means, bandwidth, cpu_allocation)
        
        
        if (self.config_length <= 0):
            done = True
        else:
            done = False
            
        info = {}
        truncated = False
        
        
        return self.state, reward, done, truncated, info
    
    def render(self):
        # Implement visualization if needed
        pass
    
    def reset(self, seed=None, options=None):
         resource_consumption = random.randint(0, 3)
         link_congestion = random.randint(0, 3)
         days = ['monday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
         times = ['morning', 'afternoon']
         self.day = random.choice(days)
         self.timeperiod = random.choice(times)
         self.nb_visitors = helper.getNumberOfVisitors(self.day, self.timeperiod)
         params = [helper.mapDay(self.day), helper.mapTimeperiod(self.timeperiod), helper.encodeVisitors(self.nb_visitors), link_congestion, resource_consumption]
         self.state = np.array(params)
         self.average_reward = 0
         self.config_length = 12
         return self.state, {}
     
        
class QosAgent():
    ep_len = 2048
    model_name = ''
    
        
    def train(self, model_name, nb_episodes):
        episodes = nb_episodes
        timesteps = episodes * self.ep_len
        self.model_name = model_name +'_' +str(int(episodes))
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        
        env = QosIotEnv()
        env.reset()
        model = PPO('MlpPolicy', env, verbose=2, tensorboard_log='./training/iot/tfboard/{}_board/'.format(self.model_name))
        print('\n\n\n**********Training Model*************\n\n\n')
        model.learn(total_timesteps=timesteps)
        print('\n\n\n**********Saving Model*************\n\n\n')
        model.save(model_save_path)
    
    def evaluate(self, nb_episodes):
        env = QosIotEnv()
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        print('\n\n\n**********Evaluating Model*************\n\n\n')
        model = PPO.load(model_save_path, env=env)
        print(evaluate_policy(model, env, n_eval_episodes=nb_episodes))
        
        
class ContextAgent():
    ep_len = 2048
    model_name = ''
    
        
    def train(self, model_name, nb_episodes):
        episodes = nb_episodes
        timesteps = episodes * self.ep_len
        self.model_name = model_name +'_' +str(int(episodes))
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        
        env = ContextIotEnv()
        env.reset()
        model = PPO('MlpPolicy', env, verbose=2, tensorboard_log='./training/iot/tfboard/{}_board/'.format(self.model_name))
        print('\n\n\n**********Training Model*************\n\n\n')
        model.learn(total_timesteps=timesteps)
        print('\n\n\n**********Saving Model*************\n\n\n')
        model.save(model_save_path)
    
    def evaluate(self, nb_episodes):
        env = ContextIotEnv()
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        print('\n\n\n**********Evaluating Model*************\n\n\n')
        model = PPO.load(model_save_path, env=env)
        print(evaluate_policy(model, env, n_eval_episodes=nb_episodes))
        
class HybridAgent():
    ep_len = 2048
    model_name = ''
    
        
    def train(self, model_name, nb_episodes):
        episodes = nb_episodes
        timesteps = episodes * self.ep_len
        self.model_name = model_name +'_' +str(int(episodes))
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        
        env = HybridIotEnv()
        env.reset()
        model = PPO('MlpPolicy', env, verbose=2, tensorboard_log='./training/iot/tfboard/{}_board/'.format(self.model_name))
        print('\n\n\n**********Training Model*************\n\n\n')
        model.learn(total_timesteps=timesteps)
        print('\n\n\n**********Saving Model*************\n\n\n')
        model.save(model_save_path)
    
    def evaluate(self, nb_episodes):
        env = HybridIotEnv()
        model_save_path = os.path.join('training', 'iot', 'models', self.model_name)
        print('\n\n\n**********Evaluating Model*************\n\n\n')
        model = PPO.load(model_save_path, env=env)
        print(evaluate_policy(model, env, n_eval_episodes=nb_episodes))
        