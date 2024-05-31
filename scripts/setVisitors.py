#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import random
import sys

day = int(sys.argv[1])
time = int(sys.argv[2])

if (day == 4 or day == 5 or day == 6):
    max_visitors = 30
elif (time == 0):
    max_visitors = 15
elif (time == 1):
    max_visitors = 20
elif (time == 2):
    max_visitors = 25
    
locations = ['entrance', 'hall1', 'hall2', 'hall3', 'shop', 'rest']

periods, visitors = [], []
[periods.append(random.randint(20, 120)) for x in range (0, 3)]
print(str(periods))

[visitors.append(random.randint(5, max_visitors)) for x in range (0, 3)]
print(str(visitors))

for location in locations:
    file_path = 'deployments/' + location + '/componentsConfig-template.json'
    with open(file_path) as file:
        data = file.read()
        data = data.replace('%periods%', str(periods))
        data = data.replace('%visitors%', str(visitors))
        
    output_file = file_path.replace('-template', '')
    
    with open(output_file, 'w') as file:
        file.write(data)