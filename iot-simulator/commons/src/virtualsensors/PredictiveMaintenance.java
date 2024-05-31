package virtualsensors;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.DataType;
import common.Location;

public class PredictiveMaintenance extends VirtualSensor {

	private final static Logger logger = LogManager.getLogger(PredictiveMaintenance.class);

	// needed for emulating processing
	private Random random;
	private int currentTime;

	public PredictiveMaintenance(String clientId, String clientName, Location location, HashSet<String> dataNeeded,
			HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName, location, dataNeeded, dataTypesProvided);
		this.callback = new PredictiveMaintenanceCallback();

		// needed for emulating processing
		this.random = new Random();
		this.currentTime = 0;
	}

	@Override
	public void processData(long timestamp) {
		int duration = 5;
		double[] artifactCondition = new double[(int) duration];

		artifactCondition[0] = 100.0;

		for (currentTime = 1; currentTime < duration; currentTime++) {

			double degradationRate = random.nextDouble() * 2;
			artifactCondition[currentTime] = artifactCondition[currentTime - 1] - degradationRate;

			if (random.nextInt(1000) < 5) {
				double suddenDegradation = random.nextDouble() * 30;
				artifactCondition[currentTime] -= suddenDegradation;
			}
			if (currentTime >= 5) {
				SimpleRegression regression = new SimpleRegression();
				for (int i = currentTime - 5; i < currentTime; i++) {
					regression.addData(i, artifactCondition[i]);
				}

				double predictedCondition = regression.predict(currentTime);
				if (predictedCondition <= 20.0) {

					artifactCondition[currentTime] = 100.0;
				}
			}
		}
		sendData(timestamp);
	}

	@Override
	public void sendData(long timestamp) {
		for (String topic : publications) {
			String payload = "Predictive Mainatenace: ok";
			executor.submit(() -> publishMessage(topic, payload, timestamp));
		}
	}

	private class PredictiveMaintenanceCallback implements MqttCallback {

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
