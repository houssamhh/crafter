package virtualsensors;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import common.BrokerClient;
import common.Configuration;
import common.DataType;
import common.Location;

public class VirtualSensor extends BrokerClient {
	
	private static final Logger logger = LogManager.getLogger(VirtualSensor.class);
	
	protected ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public HashSet<String> subscriptions;
	public HashSet<DataType> dataTypesProvided;
	protected HashSet<String> publications = new HashSet<String>();
	
	protected MqttCallback callback;
	protected Random random = new Random();
	
	public String brokerUrl = Configuration.brokerUrl;
	
	public VirtualSensor(String clientId, String clientName, Location location, 
			HashSet<String> dataNeeded, HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName);
		this.subscriptions = new HashSet<String>(dataNeeded);
		this.dataTypesProvided = new HashSet<DataType>(dataTypesProvided);
		
		for (DataType dataType : dataTypesProvided) {
			String topic = "topic/" + dataType.toString().toLowerCase();
			publications.add(topic);
		}
	}
	
	public void activate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					connect(brokerUrl, callback);
					for (String topic : subscriptions) {
						subscribe(topic);
						logger.info(clientId + "subscribed to " + topic);
					}
					logger.info(clientId + " activated! Connected to broker on " + brokerUrl);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void processData() {
		//this function is to be defined by children classes
		return;
	}
	
	public void processData(long timestamp) {
		//this function is to be defined by children classes
		return;
	}
	
	public void sendData() {
		//this function is to be defined by children classes
	}
	
	public void sendData(long timestamp) {
		//this function is to be defined by children classes
	}
	
	public String getId() {
        return clientId;
    }
	
	
	
}
