package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class DoorSensor extends IoTdevice {

	private static final Logger logger = LogManager.getLogger(DoorSensor.class);
	
	int messageSize = 50000; //in bytes

	public DoorSensor(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {
		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "event";
	}

	/**
	 * Method to publish a temperature reading.
	 */
	@Override
	public void generateMessage() {
		while (true) {
			for (String topic : publishingTopics)
				publishMessage(topic, Generator.generateMessage(messageSize));
			try {
				Thread.sleep((long) sensingPeriod * 1000);
			} catch (InterruptedException e) {
				logger.error(clientId + ": exception while generating message");
				logger.error(e.getMessage());
			}
		}
	}

	public String getValue() {
		return (random.nextDouble() < 0.75 ? "false" : "true");
	}

}
