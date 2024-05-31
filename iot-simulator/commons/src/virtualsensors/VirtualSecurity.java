package virtualsensors;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.DataType;
import common.Location;

public class VirtualSecurity extends VirtualSensor {

	private final static Logger logger = LogManager.getLogger(VirtualSecurity.class);

	// needed for emulating processing
	private Random random;
	private int currentTime;

	public VirtualSecurity(String clientId, String clientName, Location location, HashSet<String> dataNeeded,
			HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName, location, dataNeeded, dataTypesProvided);
		this.callback = new VirtualSecurityCallback();

		// needed for emulating processing
		this.random = new Random();
		this.currentTime = 0;
	}

	// needed for emulating processing
	private static class SecurityEvent {

		public SecurityEvent(String type, int time) {
		}
	}

	@Override
	public void processData(long timestamp) {
		int duration = 10000;
		for (currentTime = 0; currentTime < duration; currentTime++) {

			if (random.nextInt(100) < 80) {
				new SecurityEvent("Status Update", currentTime);
			}

			if (random.nextInt(1000) < 10) {
				new SecurityEvent("Unauthorized Access Attempt", currentTime);

			}

			if (random.nextInt(1000) < 5) {
				new SecurityEvent("Suspicious Activity", currentTime);
			}
		}
		sendData(timestamp);
	}

	@Override
	public void sendData(long timestamp) {
		for (String topic : publications) {
			String payload = "Virtual Security: " + (Math.random() < 0.5 ? "true" : "false");
			executor.submit(() -> publishMessage(topic, payload, timestamp));
		}
	}

	private class VirtualSecurityCallback implements MqttCallback {

		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public void connectionLost(Throwable cause) {
			logger.fatal(clientId + " Connection of Virtual Sensor Lost.");
			logger.fatal(cause.getMessage());
			cause.printStackTrace();
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			Map<String, Object> messageAsObject = objectMapper.readValue(new String(message.getPayload()),
					new TypeReference<>() {
					});
			long timestamp = (long) messageAsObject.get("timestamp");
			processData(timestamp);
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
		}
	}
}
