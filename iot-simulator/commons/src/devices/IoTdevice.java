package devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.BrokerClient;
import common.Configuration;
import common.DataType;
import common.Device;
import common.Location;
import common.ResultsWriter;

/**
 * Class for modelling a generic IoT device
 * You can inherit from it to define IoT devices with specific parameters and specific data generation functions (with the generateMessages function)
 */
public class IoTdevice extends BrokerClient implements Device {

	private static final Logger logger = LogManager.getLogger(IoTdevice.class);
	
	private Location location;
	
	public double initialSensingPeriod;
	public double sensingPeriod;
	
	public double unavailabilityProbability;
	public int unavailabilityDuration;
	
	public HashMap<String, ArrayList<Integer>> visitors = new HashMap<String, ArrayList<Integer>>();
	ArrayList<Integer> periods = new ArrayList<Integer>();
	ArrayList<Integer> nbPeople = new ArrayList<Integer>();
	double peopleCount;
	
	private String brokerUrl = Configuration.brokerUrl;
	
	public ResultsWriter resultsWriter;
	public Random random = new Random();
	
	public String deviceType = "";
	
	
	private HashSet<DataType> dataTypesProvided;
	public HashSet<String> publishingTopics = new HashSet<String>();
	
	public IoTdevice(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided, double sensingPeriod,
			double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {
		super(clientId, clientName);
		this.location = location;
		this.initialSensingPeriod = sensingPeriod;
		this.unavailabilityProbability = unavailabilityProbability;
		this.unavailabilityDuration = unavailabilityDuration;
		this.visitors = visitors;
		periods = visitors.get("periods");
		nbPeople = visitors.get("nbpeople");
		this.resultsWriter = new ResultsWriter(location);
		
		for (DataType dataType : dataTypesProvided) {
			String topic = "topic/" + location.toString().toLowerCase() + "/" + dataType.toString().toLowerCase();
			this.publishingTopics.add(topic);
		}
		
		this.dataTypesProvided = new HashSet<DataType>(dataTypesProvided);
	}

	/**
	 * Method to activate and start the device. It contains two threads:
	 * - t1 is responsible for calling the method for generating messages, and according to the unavailability probability, calling a function
	 * 	 to emulate the unavailability for a specific duration.
	 * - t2 is responsible for changing the period according to which the device generates data based on the number of visitors.
	 */
	public void activate() {
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				connectToBroker();
				logger.info(clientName + " activated! Connected to broker on " + brokerUrl);
				peopleCount = nbPeople.get(0);
				if(deviceType.equals("event"))
					sensingPeriod = initialSensingPeriod / peopleCount;
				else
					sensingPeriod = initialSensingPeriod;
				logger.info("Period 1: number of people = " + peopleCount + " -- initial sensing period = " + initialSensingPeriod + " -- sensing period = " + sensingPeriod);
				while(true) {
					if (random.nextDouble() < unavailabilityProbability)
						deviceUnavailable(unavailabilityDuration);
					else
						generateMessage();
				}
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				int index = 0;
				long startTime = System.currentTimeMillis();
				while (true) {
					long curTime = System.currentTimeMillis();
					if ((curTime - startTime) >= periods.get(index)*1000 && index < nbPeople.size() - 1) {
						index++;
						startTime = System.currentTimeMillis();
						peopleCount = nbPeople.get(index);
						sensingPeriod = initialSensingPeriod / peopleCount;
						String loggingMessage = "[INFO] Period " + (index+1) + ": number of people = " + peopleCount + " -- sensing period = " + sensingPeriod;
						logger.info(loggingMessage);
					}
					if (index >= nbPeople.size())
						break;
				}
			}
		});
		
		t1.start();
		
		if (deviceType.equals("event"))
			t2.start();
	}
	
	/**
	 * Connect to message broker.
	 */
	public void connectToBroker() {
		super.connect(brokerUrl);
	}
	
	/**
	 * This method is to be defined by children classes.
	 */
	public void generateMessage() {
	}
	
	/**
	 * This method emulates the unavailability of a device by making the thread sleep for a specific duration.
	 * @param duration: duration for which the device is unavailable
	 */
	public void deviceUnavailable(int duration) {
		int time = 0;
		while (time < duration) {
			resultsWriter.writeDataDevices(clientId, location, "unavailable");
			time += sensingPeriod;
			try {
				Thread.sleep((long) sensingPeriod * 1000);
			}
			catch (InterruptedException e) {
				logger.error(clientId + ": exception occured in deviceUnavailable method");
				logger.error(e.getMessage());
			}
		}
	}
	
	public String getId() {
		return clientId;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public HashSet<DataType> getDataTypesProvided() {
		return dataTypesProvided;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(clientId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IoTdevice other = (IoTdevice) obj;
		return clientId == other.clientId;
	}	
}
