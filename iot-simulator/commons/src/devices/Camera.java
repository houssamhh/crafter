package devices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.DataType;
import common.Generator;
import common.Location;

public class Camera extends IoTdevice {
	
	private static final Logger logger = LogManager.getLogger(Camera.class);
	
	Random random = new Random();

	public Camera(String clientId, String clientName, Location location, HashSet<DataType> dataTypesProvided,
			double period, double unavailabilityProbability, int unavailabilityDuration, HashMap<String, ArrayList<Integer>> visitors) {

		super(clientId, clientName, location, dataTypesProvided, period, unavailabilityProbability,
				unavailabilityDuration, visitors);
		this.deviceType = "periodic";
	}
	
	/**
	 * Method to emulate getting a video frame.
	 * 
	 * @return String: TODO get and send video frame
	 */
	public int readFrame() {
		//TODO check how to send video frames
		String frameNumber = String.valueOf(random.nextInt(2001));
		int padding = 4 - frameNumber.length();
		String filePath = "/mnt/frames/seq_00" + "0".repeat(padding) + frameNumber + ".jpg";
		File file = new File(filePath);
		byte[] fileContent = readFile(file);
		return fileContent.length;
	}
	
	private static byte[] readFile(File file) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			byte[] fileContent = new byte[(int) file.length()];
			inputStream.read(fileContent);
			inputStream.close();
			return fileContent;
		} catch (IOException e) {
			logger.fatal("Exception while reading frame", e.getCause());
		}
		return "empty".getBytes();
	}

	/**
	 * Method to publish a video frame.
	 */
	@Override
	public void generateMessage() {
		String message = Generator.generateMessage(readFrame());
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
