package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class LightSensor extends IoTdevice {
	
	private static final Logger logger = LogManager.getLogger(LightSensor.class);
	
	int messageSize = 50000; //in bytes
	
	public LightSensor(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {
		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "periodic";
	}
	
	/**
	 * Method to emulate reading luminosity level.
	 */
	public int readLuminosity() {
		int currentLuminosity = random.nextInt(1000);
		return currentLuminosity;
	}
		
	/**
	 * Method to publish a luminosity reading reading.
	 */
	@Override
	public void generateMessage() {
		String message = Generator.generateMessage(messageSize);
		for (String topic : publishingTopics) {
			publishMessage(topic, message);
		}

		try {
			Thread.sleep((long) sensingPeriod * 1000);
		} catch (InterruptedException e) {
			logger.error(clientId + ": exception while generating message");
			logger.error(e.getMessage());
		}	
	}
}
