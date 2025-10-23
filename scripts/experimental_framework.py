#!/usr/bin/python

import time
import os

from mininet.net import Containernet
from mininet.node import Controller
from mininet.cli import CLI
from mininet.link import TCLink
from mininet.log import info, error, setLogLevel

setLogLevel('info')


network = Containernet(controller=Controller)

cwd = os.getcwd()

info('*** Adding controller\n')
network.addController('c0')

info('*** Adding docker containers\n')

##-------Broker-------

brokerDocker = network.addDocker(
	'broker',
	ip='10.0.0.240',
	dimage='emqx-new:latest',		
	dcmd="/opt/emqx/bin/emqx foreground",
	ports=[1883, 18083, 8883, 8083, 8084, 8780, 9900],
	port_bindings={1883: 1883, 18083: 18083, 8883: 8883, 8083: 8083, 8084: 8084, 8780: 8780, 9900:9900}
)

nodeE = network.addDocker(
	'nodeE',
	ip='10.0.0.241',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/E:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeB = network.addDocker(
	'nodeB',
	ip='10.0.0.242',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/B:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeC = network.addDocker(
	'nodeC',
	ip='10.0.0.243',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/C:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeD = network.addDocker(
	'nodeD',
	ip='10.0.0.244',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/D:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeG = network.addDocker(
	'nodeG',
	ip='10.0.0.245',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/G:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeS = network.addDocker(
	'nodeS',
	ip='10.0.0.246',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/S:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

nodeV = network.addDocker(
	'nodeV',
	ip='10.0.0.249',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/V:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset/frames:/mnt/frames:rw"],
	ports_binding={1883:1883, 9900:9900}
)

edge1 = network.addDocker(
	'edge1',
	ip='10.0.0.250',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/edge1:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset:/mnt/mall-dataset:rw"],
	ports_binding={1883:1883, 9900:9900},
	cpu_period=50000,
	cpu_quota=50000
)
edge2 = network.addDocker(
	'edge2',
	ip='10.0.0.247',
	dimage="location:latest",
	ports=[1883, 9900],	
	volumes=[cwd + "/deployments/edge2:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw", cwd + "/mall-dataset:/mnt/mall-dataset:rw"],			
	ports_binding={1883: 1883, 9900:9900},
	cpu_period=50000,
	cpu_quota=50000
)

metaverseDocker = network.addDocker(
	'metaverse',
	ip='10.0.0.248',		
	dimage="location:latest",
	ports=[1883, 9900],
	volumes=[cwd + "/deployments/metaverse:/mnt/deployment:rw", cwd + "/deployments/componentsConfig.json:/mnt/config/componentsConfig.json:rw", cwd + "/results:/mnt/results:rw"],
	ports_binding={1883:1883, 9900:9900}
)

info('*** Adding switches\n')
edge1Switch = network.addSwitch('s6')
edge2Switch = network.addSwitch('s7')
metaverseSwitch = network.addSwitch('s8')
brokerSwitch = network.addSwitch('s9')

info('*** Creating links\n')
##Connecting hosts to switches
network.addLink(edge1, edge1Switch)
network.addLink(edge2, edge2Switch)
network.addLink(metaverseDocker, metaverseSwitch)
network.addLink(brokerDocker, brokerSwitch)

##Connecting switches to broker's switch
network.addLink(nodeE, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeB, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeC, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeD, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeG, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeS, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeV, brokerSwitch, cls=TCLink, bw=500.0)
network.addLink(edge1Switch, brokerSwitch, cls=TCLink, bw=1000.0)
network.addLink(edge2Switch, brokerSwitch, cls=TCLink, bw=1000.0)
network.addLink(metaverseSwitch, brokerSwitch, cls=TCLink, bw=1000)

##Connecting switches to metaverse switch
network.addLink(nodeE, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeB, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeC, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeD, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeG, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeS, metaverseSwitch, cls=TCLink, bw=500.0)
network.addLink(nodeV, metaverseSwitch, cls=TCLink, bw=500.0)


info('*** Starting network\n')
network.start()

info('*** Testing connectivity\n')
network.pingAll([])

info('*** Setting up experiment \n')

time.sleep(1)

info('\n*** Launching "nodeE" Docker... \n')
nodeE.cmd('java -jar location.jar E&')

info('\n*** Launching "nodeB" Docker... \n')
nodeB.cmd('java -jar location.jar B &')

info('\n*** Launching "nodeC" Docker... \n')
nodeC.cmd('java -jar location.jar C &')

info('\n*** Launching "nodeD" Docker... \n')
nodeD.cmd('java -jar location.jar D &')

info('\n*** Launching "nodeG" Docker... \n')
nodeG.cmd('java -jar location.jar G &')

info('\n*** Launching "nodeS" Docker... \n')
nodeS.cmd('java -jar location.jar S &')

info('\n*** Launching "nodeV" Docker... \n')
nodeV.cmd('java -jar location.jar V &')

info('\n*** Launching "edge1" Docker... \n')
edge1.cmd('java -jar location.jar edge1 &')

info('\n*** Launching "edge2" Docker... \n')
edge2.cmd('java -jar location.jar edge2 &')

info('\n*** Launching "metaverse" Docker... \n')
metaverseDocker.cmd('java -jar location.jar metaverse &')

info('\n*** Starting network \n')
# info('*** Running CLI\n')
# CLI(network)

time.sleep(60)
info('*** Stopping network')
network.stop()

