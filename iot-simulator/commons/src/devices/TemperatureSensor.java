package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class TemperatureSensor extends IoTdevice {

	private static final Logger logger = LogManager.getLogger(TemperatureSensor.class);
	
	int messageSize = 50000; //in bytes

	public TemperatureSensor(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {

		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "event";
	}

	/**
	 * Method to emulate the reading of temperature.
	 * 
	 * @return int: temperature reading
	 */
	public int readTemperature() {
		int currentTemperature = random.nextInt(15) + 15; // temperature fluctuates between 15 and 30. This can be
															// changed later to provide more realistic changes
		return currentTemperature;
	}

	/**
	 * Method to publish a temperature reading.
	 */
	@Override
	public void generateMessage() {
		for (String topic : publishingTopics) {
//				resultsWriter.writeDataDevices(clientId, getLocation(), "ok");
			publishMessage(topic, Generator.generateMessage(messageSize));
		}

		try {
			Thread.sleep((long) sensingPeriod * 1000);
		} catch (InterruptedException e) {
			logger.error(clientId + ": exception while generating message");
			logger.error(e.getMessage());
		}
	}
}
