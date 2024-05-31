#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import subprocess
from distutils.dir_util import copy_tree
import pandas as pd
import sys


visitors = {'monday': {'morning': 1, 'afternoon': 3},
            'wednesday': {'morning': 2, 'afternoon': 3},
            'thursday': {'morning': 3, 'afternoon': 5},
            'friday': {'morning': 7, 'afternoon': 9},
            'saturday': {'morning': 10, 'afternoon': 13},
            'sunday': {'morning': 12, 'afternoon': 11}
            }


visitors_distribution = {'B': 0.35, 'V': 0.15, 'C': 0.05, 'G': 0.15, 'S': 0.1, 'D': 0.05, 'E': 0.15,
                         'edge1': 1, 'edge2': 1, 'metaverse': 1}
    
# returns total number of visitors        
def setNumberOfVisitors(day, timeperiod):
    deployments_dir = './deployments'
    for subdir, dirs, file in os.walk(deployments_dir):
        for d in dirs:
            deployment_template = deployments_dir+ '/'+ d + '/deployment-template.json'
            input_file = open(deployment_template, 'r')
            data = input_file.read()
            nbpeople = round(visitors[day][timeperiod] * 7 * visitors_distribution[d])
            if nbpeople == 0:
                nbpeople=1   # avoid having empty locations
            data = data.replace('%nbpeople%', '[{nb}, {nb}, {nb}]'.format(nb=nbpeople))
            output_file = deployment_template.replace('-template', '')
            output = open(output_file, 'w')
            output.write(data)
            input_file.close()
            output.close()
    return visitors[day][timeperiod] * 7
    
def setNetworkParams(bandwidth, quota):
    file = open('./experimental_framework_template.py')
    data = file.read()
    data = data.replace('%bw%', str(float(bandwidth)))
    data = data.replace('%cpu_quota%', str(int(quota)))
    output = open('./experimental_framework.py', 'w')
    output.write(data)
    file.close()
    output.close()

def saveResults(exp_id):
    from_dir = './results'
    to_dir = './training/dataset/'+exp_id
    copy_tree(from_dir, to_dir)
    
    
if __name__ == '__main__':
    log_file_path = './training/dataset-generation.log'
    
    input_file = sys.argv[1]
    df = pd.read_csv(input_file)

    for row in df.iterrows():
        day = row[1]['day']
        timeperiod = row[1]['time']
        bandwidth = row[1]['bandwidth']
        cpu = row[1]['cpu']
        
        setNumberOfVisitors(day, timeperiod)
        quota = cpu
        setNetworkParams(bandwidth, quota)
        exp_id = day + '_' + timeperiod + '_' + str(bandwidth) + '_' + str(int(cpu))
        log_file = open(log_file_path, 'a')
        log_file.write('Running experiment: ' + exp_id + '\n')
        log_file.close()
        mininet_command = './scripts/runMininet.sh'
        subprocess.run([mininet_command], stdout=subprocess.DEVNULL, text=True, check=True)
        
        saveResults(exp_id)