#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import pandas as pd
from causallearn.search.ConstraintBased.PC import pc
from causallearn.utils.cit import kci
from causallearn.utils.GraphUtils import GraphUtils

from causallearn.utils.PCUtils.BackgroundKnowledge import BackgroundKnowledge

dataset_path = './causal-discovery/results/dataset.csv'
header=['day', 'time', 'location', 'number_of_visitors', 'bandwidth_allocation', 'resource_allocation', 'resource_consumption', 'response_time']
citest_cache_file = './causal-discovery/results/citest_cache_kci.json'
df = pd.read_csv(dataset_path, header=0)
data_np = df.to_numpy()

# without bk
cg = pc(data_np, 0.1, kci, uc_rule=1, uc_priority=1)
# cg.draw_pydot_graph(labels=header)
pyd = GraphUtils.to_pydot(cg.G, labels=header)
pyd.write_png('./causal-discovery/results/CG.png')

# with bk
cg_without_background_knowledge = pc(data_np, 0.1, kci, True, cache_path=citest_cache_file, uc_rule=0, uc_priority=0)
nodes = cg_without_background_knowledge.G.get_nodes()
bk = BackgroundKnowledge() 
# no edges directed edges from response_time
for i in range(1, 7):
    bk = bk.add_forbidden_by_node(nodes[7], nodes[i])
cg_with_background_knowledge = pc(data_np, 0.1, kci, True, cache_path='./causal-discovery/results/citest_cache_bk.json', background_knowledge=bk, uc_rule=1, uc_priority=4)
# cg_with_background_knowledge.draw_pydot_graph(labels=header)
pyd_bk = GraphUtils.to_pydot(cg_with_background_knowledge.G, labels=header)
pyd_bk.add_edge(nodes[3], nodes[7])
pyd_bk.write_png('./causal-discovery/results/CG_BK.png')