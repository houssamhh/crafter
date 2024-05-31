package virtualsensors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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

public class QueueManagement extends VirtualSensor {

	private final static Logger logger = LogManager.getLogger(QueueManagement.class);

	// Needed for emulating processing

	private static class Visitor {
		private int serviceTime;

		public Visitor(int arrivalTime, int serviceTime) {
		}

		public int getServiceTime() {
			return serviceTime;
		}
	}

	private Queue<Visitor> queue;
	private Random random;
	private int currentTime;

	public QueueManagement(String clientId, String clientName, Location location, HashSet<String> dataNeeded,
			HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName, location, dataNeeded, dataTypesProvided);
		this.callback = new VirtualOccupancyCallback();

		// needed for emulating processing
		this.queue = new LinkedList<>();
		this.random = new Random();
		this.currentTime = 0;
	}

	@Override
	public void processData(long timestamp) {
		int duration = 10000;
		int visitorServiceEndTime = 0;

		for (currentTime = 0; currentTime < duration; currentTime++) {
			if (random.nextInt(5) == 0) {
				int serviceTime = random.nextInt(10) + 1;
				Visitor newVisitor = new Visitor(currentTime, serviceTime);
				queue.add(newVisitor);
			}

			if (currentTime >= visitorServiceEndTime && !queue.isEmpty()) {
				Visitor visitor = queue.poll();
				visitorServiceEndTime = currentTime + visitor.getServiceTime();
			}
		}
		sendData(timestamp);
	}

	@Override
	public void sendData(long timestamp) {
		for (String topic : publications) {
			String payload = "Queue Management dummy data";
			executor.submit(() -> publishMessage(topic, payload, timestamp));
		}
	}

	private class VirtualOccupancyCallback implements MqttCallback {

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
