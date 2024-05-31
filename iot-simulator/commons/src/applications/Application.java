package applications;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.BrokerClient;
import common.Configuration;
import common.Location;
import common.ResultsWriter;

public class Application extends BrokerClient {

	public Logger logger = LogManager.getLogger(Application.class);

	private String brokerUrl = Configuration.brokerUrl;
	public HashSet<String> subscriptions = new HashSet<String>();

	private Location location;
	protected ResultsWriter resultsWriter;

	protected MqttCallback callback = new ApplicationCallback();

	public Application(String clientId, String clientName, Location location, HashSet<String> subscriptions) {
		super(clientId, clientName);
		this.location = location;
		resultsWriter = new ResultsWriter(location);
		this.subscriptions = subscriptions;
	}

	public void activate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					connect(brokerUrl, callback);
					subscribeToTopics(subscriptions);
					logger.info(clientName + " activated! Connected to broker on " + brokerUrl);
				} catch (IOException e) {
					logger.fatal(e.getClass() + ": " + e.getStackTrace() + ": " + e.getCause(), e);
				}

			}
		}).start();
	}

	public void subscribeToTopics(HashSet<String> subscriptions) {
		for (String topic : subscriptions) {
			subscribe(topic);
			logger.info("[SUB] " + clientId + "subscribed to " + topic);
		}
	}

	public String getId() {
		return clientId;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		String str = "{ \"id\": \"" + getId() + "\" }";
		return str;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Application other = (Application) obj;
		return (clientId == other.clientId);
	}

	private class ApplicationCallback implements MqttCallback {

		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public void connectionLost(Throwable cause) {
			logger.fatal(clientId + " disconnected from broker!");
			logger.fatal(cause.toString());
			logger.fatal(cause.getMessage());
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) {
			Map<String, Object> messageAsObject;
			try {
				messageAsObject = objectMapper.readValue(new String(message.getPayload()), new TypeReference<>() {
				});
//				String payload = (String) messageAsObject.get("payload");
				long timestamp = (long) messageAsObject.get("timestamp");
				long latency = (System.currentTimeMillis() - timestamp);
				resultsWriter.writeData(topic, clientId, getLocation(), latency);
			} catch (JsonParseException e) {
				logger.fatal(clientId + " JsonParserException: " + e.getOriginalMessage() + " -- " + e.getCause());
				e.printStackTrace();
			} catch (JsonMappingException e) {
				logger.fatal(clientId + " JsonMappingException: " + e.getOriginalMessage() + " -- " + e.getCause());
				e.printStackTrace();
			} catch (IOException e) {
				logger.fatal(clientId + " IOException: " + e.getMessage() + " -- " + e.getCause());
				e.printStackTrace();
			}

		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			System.out.println("Delivery complete ----" + token.isComplete());
		}

	}

}
