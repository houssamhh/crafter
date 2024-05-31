# -*- coding: utf-8 -*-

import pandas as pd

from stable_baselines3 import PPO
from stable_baselines3.common.evaluation import evaluate_policy

# from BandwidthAgent import QosIotEnv, ContextIotEnv, MixedIotEnv

from GlobalAgents import QosIotEnv, ContextIotEnv, HybridIotEnv

def getEpisode(timestep):
    return timestep / 2048

qos_agent_path = '../training/iot/evaluation/sotarl_combinedreward_100.csv'
context_agent_path = '../training/iot/evaluation/crafter_combinedreward_100.csv'
hybrid_agent_path = '../training/iot/evaluation/hybrid_combinedreward_100.csv'

df_qos = pd.read_csv(qos_agent_path)
timestep_column = df_qos['Step']
episode_column = timestep_column.apply(getEpisode)
df_qos['Step'] = episode_column

df_context = pd.read_csv(context_agent_path)
timestep_column = df_context['Step']
episode_column = timestep_column.apply(getEpisode)
df_context['Step'] = episode_column

df_hybrid = pd.read_csv(hybrid_agent_path)
timestep_column = df_hybrid['Step']
episode_column = timestep_column.apply(getEpisode)
df_hybrid['Step'] = episode_column

df_qos.rename(columns={'Step': 'Episode'}, inplace=True)
df_context.rename(columns={'Step': 'Episode'}, inplace=True)
df_hybrid.rename(columns={'Step': 'Episode'}, inplace=True)


markersize=2
ax = df_qos.plot(x='Episode', y='Value', label='SOTA-RL', ylabel='Reward value', figsize=(5, 3), color='maroon', marker='v', markersize=markersize)
df_context.plot(ax=ax, x='Episode', y='Value', label='CRAFTER', color='green', marker='s', markersize=markersize)
df_hybrid.plot(ax=ax, x='Episode', y='Value', label='Hybrid-RL', color='orange', marker='o', markersize=markersize)
ax.grid('on')
fig = ax.get_figure()  # Get the matplotlib figure object
# fig.savefig('../plots/training_reward.pdf', dpi=600, bbox_inches='tight')  # Save the figure


# qos_model_path = '../training/iot/models/ppo_global_qos'
# qos_env = QosIotEnv()
# print('\n**********Evaluating QoS Model*************\n')
# qos_model = PPO.load(qos_model_path, env=qos_env)
# print(evaluate_policy(qos_model, qos_env, n_eval_episodes=1000))


# context_model_path = '../training/iot/models/ppo_global_context'
# context_env = ContextIotEnv()
# print('\n**********Evaluating Context Model*************\n')
# context_model = PPO.load(context_model_path, env=context_env)
# print(evaluate_policy(context_model, context_env, n_eval_episodes=1000))

# hybrid_model_path = '../training/iot/models/ppo_global_hybrid'
# hybrid_env = HybridIotEnv()
# print('\n**********Evaluating Hybrid Model*************\n')
# hybrid_model_path = PPO.load(hybrid_model_path, env=hybrid_env)
# print(evaluate_policy(hybrid_model_path, hybrid_env, n_eval_episodes=1000))
