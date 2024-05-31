package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class MotionSensor extends IoTdevice {

	private static final Logger logger = LogManager.getLogger(MotionSensor.class);
	
	int messageSize = 50000; //in bytes

	public MotionSensor(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {

		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "event";
	}

	/**
	 * Method to publish motion reading.
	 */
	@Override
	public void generateMessage() {
		for (String topic : publishingTopics) {
			publishMessage(topic, Generator.generateMessage(messageSize));
		}
		try {
			double period = random.nextGaussian(sensingPeriod * 2, 3);
			Thread.sleep((long) period * 1000);
		} catch (InterruptedException e) {
			logger.error(clientId + ": exception while generating message");
			logger.error(e.getMessage());
		}
	}

	/**
	 * Method to emulate reading motion
	 * @return String: "true" or "false"
	 */
	public String getValue() {
		return (random.nextDouble() < 0.5 ? "true" : "false");
	}

}
