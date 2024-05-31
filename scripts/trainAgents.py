# -*- coding: utf-8 -*-
from GlobalAgents import QosAgent, ContextAgent, HybridAgent
from GlobalAgents import QosIotEnv, ContextIotEnv, HybridIotEnv


from stable_baselines3 import PPO
from stable_baselines3.common.evaluation import evaluate_policy

import time

# import optuna



file = open('./training/iot/training-log.log', 'w')
file.write('Training qos agent ...\n')
file.close()
start = time.time()
qosAgent = QosAgent()
qosAgent.train('sotarl_lowerpenalty', 100)
end = time.time()
file = open('./training/iot/training-log.log', 'a')
file.write('Training time: {} \n'.format(str(start - end)))
file.close()


file = open('./training/iot/training-log.log', 'a')
file.write('Training context agent ...\n')
file.close()
start = time.time()
contextAgent = ContextAgent()
contextAgent.train('crafter_lowerpenalty', 100)
end = time.time()
file = open('./training/iot/training-log.log', 'a')
file.write('Training time: {} \n'.format(str(start - end)))
file.close()

file = open('./training/iot/training-log.log', 'a')
file.write('Training hybrid agent ...\n')
file.close()
start = time.time()
hybridAgent = HybridAgent()
hybridAgent.train('hybrid_lowerpenalty', 100)
end = time.time()
file = open('./training/iot/training-log.log', 'a')
file.write('Training time: {} \n'.format(str(start - end)))
file.close()


qos_model_path = './training/iot/models/sotarl_lowerpenalty_100'
qos_env = QosIotEnv()
print('\n**********Evaluating QoS Model*************\n')
qos_model = PPO.load(qos_model_path, env=qos_env)
print(evaluate_policy(qos_model, qos_env, n_eval_episodes=100, deterministic=False))


context_model_path = './training/iot/models/crafter_lowerpenalty_100'
context_env = ContextIotEnv()
print('\n**********Evaluating Context Model*************\n')
context_model = PPO.load(context_model_path, env=context_env)
print(evaluate_policy(context_model, context_env, n_eval_episodes=100, deterministic=False))

hybrid_model_path = './training/iot/models/hybrid_lowerpenalty_100'
hybrid_env = HybridIotEnv()
print('\n**********Evaluating Hybrid Model*************\n')
hybrid_model_path = PPO.load(hybrid_model_path, env=hybrid_env)
print(evaluate_policy(hybrid_model_path, hybrid_env, n_eval_episodes=100, deterministic=False))
