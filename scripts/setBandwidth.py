#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys

bandwidth = float(sys.argv[1].replace('[', '').replace(']', ''))
file_path = '/home/satrai/Desktop/houssam/experimental_framework_template.py'
with open(file_path, 'r') as file:
     data = file.read()
     data = data.replace('%bandwidth%', str(bandwidth))
        
output_file = file_path.replace('_template', '')
    
with open(output_file, 'w') as file:
    file.write(data)