# -*- coding: utf-8 -*-
"""
Created on Wed Oct  2 17:11:48 2024

@author: houss
"""

import random
import numpy as np
import pandas as pd
from deap import base, creator, tools, algorithms


days = [0,1,2,3,4,5]
time_periods = [0,1]
bandwidths = [20, 40, 60, 80, 100, 150, 200, 250, 300, 350, 400, 450, 500]
resources = [5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000]


filtered_df = pd.DataFrame()
# Define the decision variables (bandwidth, resources)
def create_individual():
    return [random.choice(bandwidths), random.choice(resources)]

# Register toolbox functions
toolbox = base.Toolbox()
toolbox.register("individual", tools.initIterate, creator.Individual, create_individual)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

# Define evaluation function
def evaluate(individual):
    # Select the rows from the dataframe based on the individual's decision variables (bandwidth, resources)
    bandwidth, resources = individual
    selected_row = filtered_df.iloc[(filtered_df['bandwidth'] - bandwidth).abs().argsort()[:1]]  # Closest match based on bandwidth

    # Return the corresponding smart_lighting, security, visitor_guiding, maintenance, metaverse values
    return selected_row['smart_lighting'].values[0], selected_row['security'].values[0], selected_row['visitor_guiding'].values[0], selected_row['maintenance'].values[0], selected_row['metaverse'].values[0]
    # return selected_row['bandwidth'].values[0], selected_row['resources'].values[0], selected_row['smart_lighting'].values[0], selected_row['security'].values[0], selected_row['visitor_guiding'].values[0], selected_row['maintenance'].values[0], selected_row['metaverse'].values[0]

def genetic_algo(day, time):
    global filtered_df
    df = pd.read_csv('../RL/dataset_encoded.csv')
    filtered_df = df[(df['day'] == day) & (df['time_period'] == time)]
    
    
    # creator.create("FitnessMin", base.Fitness, weights=(-0.01, -0.01, -1.0, -1.0, -1.0, -1.0, -3.0))
    creator.create("FitnessMin", base.Fitness, weights=(-1.0, -1.0, -1.0, -1.0, -1.0))
    creator.create("Individual", list, fitness=creator.FitnessMin)
    toolbox.register("mate", tools.cxBlend, alpha=0.5)
    toolbox.register("mutate", tools.mutGaussian, mu=0, sigma=1, indpb=0.1)
    toolbox.register("select", tools.selNSGA2)
    toolbox.register("evaluate", evaluate)
    
    # Genetic Algorithm parameters
    population_size = 50
    num_generations = 70
    mutation_prob = 0.2
    crossover_prob = 0.5
    
    # Create the population
    population = toolbox.population(n=population_size)
    
    # Run the genetic algorithm
    algorithms.eaMuPlusLambda(population, toolbox, mu=population_size, lambda_=population_size, cxpb=crossover_prob, mutpb=mutation_prob, ngen=num_generations, verbose=True)
    
    # Get the best individual
    best_individual = tools.selBest(population, k=10)[0]
    # print(tools.selBest(population, k=10))
    # print("Best individual (bandwidth, resources):", best_individual)
    # print("Best fitness (smart_lighting, security, visitor_guiding, maintenance, metaverse):", evaluate(best_individual))
    return evaluate(best_individual)

smart_lighting = []
security = []
visitor_guiding = []
maintenance = []
metaverse = []
for day in days:
    for time in time_periods:
        response_times = genetic_algo(day, time)
        smart_lighting.append(response_times[0])
        security.append(response_times[1])
        visitor_guiding.append(response_times[2])
        maintenance.append(response_times[3])
        metaverse.append(response_times[4])
        
        
print(smart_lighting)
print(security)
print(visitor_guiding)
print(maintenance)
print(metaverse)
        