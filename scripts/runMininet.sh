#! /bin/bash

if [ -f "results/mylog.log" ]; then
	sudo rm "results/mylog.log"
fi

sudo mn -c
sudo python3 experimental_framework.py
