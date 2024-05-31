package virtualsensors;

import java.util.ArrayDeque;
import java.util.HashSet;
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

public class EnvironmentalAnalysis extends VirtualSensor {

	private final static Logger logger = LogManager.getLogger(EnvironmentalAnalysis.class);

	// needed for emulating processing
	private Random random;
	private int currentTime;
	private Queue<Double> temperatureQueue;
	private int windowSize;
	private double bandwidth;
	private double[][] weights;

	public EnvironmentalAnalysis(String clientId, String clientName, Location location, HashSet<String> dataNeeded,
			HashSet<DataType> dataTypesProvided) {
		super(clientId, clientName, location, dataNeeded, dataTypesProvided);
		this.callback = new EnvironmentalAnalysisCallback();

		// needed for emulating processing
		this.random = new Random();
		this.currentTime = 0;
		this.temperatureQueue = new ArrayDeque<>();
		this.windowSize = 5; // Adjust window size as needed
		this.bandwidth = 0.5; // Adjust bandwidth as needed
		this.weights = precomputeWeights(windowSize, bandwidth);
	}

	@Override
	public void processData(long timestamp) {
		int duration = 10000;
		for (currentTime = 0; currentTime < duration; currentTime++) {
			double temperature = random.nextDouble() * 40;
			double humidity = random.nextDouble() * 100;
			double airQuality = random.nextDouble() * 500;
			if (temperature > 30) {
			}
			if (humidity > 70) {
			}
			if (airQuality > 300) {
			}

			temperatureQueue.offer(temperature);
			if (temperatureQueue.size() > windowSize) {
				temperatureQueue.poll();
			}
			predictNextTemperature();
		}
		sendData(timestamp);
	}

	@Override
	public void sendData(long timestamp) {
		for (String topic : publications) {
			String payload = "Environmental Analysis: " + random.nextInt(10);
			executor.submit(() -> publishMessage(topic, payload, timestamp));
		}
	}

	// needed for emulating processing
	private double predictNextTemperature() {
		double[] x = new double[temperatureQueue.size()];
		double[] y = new double[temperatureQueue.size()];
		int i = 0;
		for (Double temp : temperatureQueue) {
			x[i] = i;
			y[i] = temp;
			i++;
		}
		double sum = 0;
		double weightSum = 0;
		for (i = 0; i < x.length; i++) {
			double weight = weights[x.length - 1][i];
			sum += weight * y[i];
			weightSum += weight;
		}
		return sum / weightSum;
	}

	private double[][] precomputeWeights(int windowSize, double bandwidth) {
		double[][] weights = new double[windowSize][windowSize];
		for (int i = 0; i < windowSize; i++) {
			for (int j = 0; j < windowSize; j++) {
				double distance = Math.abs(i - j);
				weights[i][j] = tricube(distance / bandwidth);
			}
		}
		return weights;
	}

	private double tricube(double x) {
		if (Math.abs(x) >= 1) {
			return 0;
		}
		double tmp = 1 - x * x * x;
		return tmp * tmp * tmp;
	}

	private class EnvironmentalAnalysisCallback implements MqttCallback {

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
			processData(timestamp);
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
		}
	}

}
