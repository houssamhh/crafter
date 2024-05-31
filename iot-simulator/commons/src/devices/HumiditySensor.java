package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class HumiditySensor extends IoTdevice {

	private static final Logger logger = LogManager.getLogger(HumiditySensor.class);
	
	int messageSize = 50000; //in bytes

	public HumiditySensor(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {
		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "periodic";
	}

	/**
	 * Method to emulate humidity reading.
	 * 
	 * @return
	 */
	public int readHumidity() {
		int currentHumidity = random.nextInt(50) + 30; // humidity fluctuates between 30% and 80%
		return currentHumidity;
	}

	/**
	 * Method to publish a humidity reading.
	 */
	@Override
	public void generateMessage() {
		for (String topic : publishingTopics) {
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
