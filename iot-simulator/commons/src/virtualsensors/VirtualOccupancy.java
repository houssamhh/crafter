package virtualsensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import common.DataType;
import common.Location;

public class VirtualOccupancy extends VirtualSensor {

	private final static Logger logger = LogManager.getLogger(VirtualOccupancy.class);

	public VirtualOccupancy(String clientId, String clientName, Location location, HashSet<String> dataNeeded,
			HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName, location, dataNeeded, dataTypesProvided);
		this.callback = new VirtualOccupancyCallback();
	}

	@Override
	public void processData(long timestamp) {
		String scriptPath = "/mnt/mall-dataset/detect.sh";
		ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath);
//		processBuilder.redirectErrorStream(true);
		Process process;
		try {
			process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			reader.readLine();
//	        while ((output = reader.readLine()) != null) {
//			    System.out.println(output);
//	        }
			sendData(timestamp);
		} catch (IOException e) {
			logger.fatal(clientId, "processBuilder error", e.getCause());
		}
	}

	@Override
	public void sendData(long timestamp) {
		for (String topic : publications) {
			String payload = "Virtual Occupancy: " + random.nextInt(10);
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
//			TODO: check if we need to log these
			long timestamp = (long) messageAsObject.get("timestamp");
//			long latency = (System.currentTimeMillis() - timestamp);
//			String payload = (String) messageAsObject.get("payload");
			processData(timestamp);
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
		}
	}
}
