package common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class BrokerClient {
	
	private static final Logger logger = LogManager.getLogger(BrokerClient.class);

	protected String clientId;
	protected String clientName;
	protected String clientPwd;
	protected MqttClient mqttClient;
	
	protected static final String DEFAULT_MESSAGE = "{ \"timestamp\": ${TIMESTAMP}, \"payload\": \"${PAYLOAD}\" }";
	
	public BrokerClient(String clientId, String clientName) {
		if (clientId == null) { throw new IllegalArgumentException("MQTT Client ID cannot be null"); }
		if (clientId.isBlank()) { throw new IllegalArgumentException("MQTT Client ID cannot be blank"); }
		if (clientName == null) { throw new IllegalArgumentException("MQTT Client name cannot be null"); }
		if (clientName.isBlank()) { throw new IllegalArgumentException("MQTT Client name cannot be blank"); }
		
		this.clientId = clientId;
		this.clientName = clientName;
		this.clientPwd = Generator.generatePassword(20);
	}
	
	/*
	 * connect function with custom callback
	 */
	public void connect(String brokerUrl, MqttCallback callback) throws IOException{
		try {
			mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName("admin");
			connOpts.setPassword("public".toCharArray());
			connOpts.setCleanSession(true);
			connOpts.setMaxInflight(30000);	
			connOpts.setAutomaticReconnect(true);
			connOpts.setKeepAliveInterval(1000);
			mqttClient.setCallback(callback);
			mqttClient.connect(connOpts);
		} catch (MqttException e) {
			logger.fatal(clientId + " couldn't connect to broker on " + brokerUrl + " -- " + e.getCause() + " -- ");
			e.printStackTrace();
		}
	}
	
	/*
	 * connect function with callback that only prints messages, topics, and qos
	 */
	public void connect(String brokerUrl){
		try {
			mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName("admin");
			connOpts.setPassword("public".toCharArray());
			connOpts.setCleanSession(true);
			connOpts.setMaxInflight(30000);	
			connOpts.setAutomaticReconnect(true);
			connOpts.setKeepAliveInterval(1000);	
			mqttClient.setCallback(new OnMessageCallback(clientId));
			mqttClient.connect(connOpts);
		} catch (MqttException e) {
			logger.fatal(clientId + "couldn't connect to broker on " + brokerUrl + " -- " + e.getCause() + " -- " + e.getStackTrace().toString());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.fatal(pw.toString());
		}
	}
	
	//used by devices
	public void publishMessage(String topic, String message) {
//		String message = DEFAULT_MESSAGE.replace("${PAYLOAD}", value);
//		message = message.replace("${TIMESTAMP}", String.valueOf(System.currentTimeMillis()));
		MqttMessage mqttMessage = new MqttMessage(message.getBytes());
		mqttMessage.setQos(0);
		try {
			mqttClient.publish(topic, mqttMessage);
		} catch (MqttException e) {
			logger.info("Exception when publishing message");
			logger.info(e.getCause());
			e.printStackTrace();
		}	
	}
	
	//used by virtual sensors to conserve the original timestamp
	public void publishMessage(String topic, String value, long timestamp) {
		String message = DEFAULT_MESSAGE.replace("${PAYLOAD}", value);
		message = message.replace("${TIMESTAMP}", String.valueOf(timestamp));
		MqttMessage mqttMessage = new MqttMessage(message.getBytes());
		mqttMessage.setQos(0);
		try {
			mqttClient.publish(topic, mqttMessage);
		} catch (MqttException e) {
			logger.info("Exception when publishing message");
			logger.info(e.getCause());
			e.printStackTrace();
		}	
	}
	
	
	
	/**
	 * disconnect the client from the broker.
	 */
	public void disconnect() {
		try {
			mqttClient.disconnect();
			System.out.print("disconnected");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * close connection with the broker.
	 */
	public void closeConnection() {
		try {
			mqttClient.close();
			
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribe(String topic) {
		try {
			mqttClient.subscribe(topic, 0);
		} catch (MqttException e) {
			logger.fatal(clientId + " couldn't subscribe to " + topic + " -- " + e.getCause() + " -- " + e.getStackTrace().toString());
			e.printStackTrace();
		}
	}
	
	public void unsubscribe(String topic) {
		MqttMessage message = new MqttMessage("unsubscribe".getBytes());
		try {
			mqttClient.publish(topic,  message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return boolean that represents connection state with the broker.
	 */
	public boolean isConnectedTo() {
		return mqttClient.isConnected();
	}

	public String getClientId() {
		return clientId;
	}
	
	

	public MqttClient getMqttClient() {
		return mqttClient;
	}

	public void setMqttClient(MqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}

	@Override
	public String toString() {
		return "BrokerClient [clientId=" + clientId + ", clientName=" + clientName + 
				", mqttClient=" + (mqttClient != null ? mqttClient.getServerURI() : null) + "]";
	}
}
