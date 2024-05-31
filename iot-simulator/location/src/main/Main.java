package main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import applications.Application;
import common.DeploymentParser;
import common.Device;
import common.Location;
import virtualsensors.VirtualSensor;


public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);
	
	public static void main(String[] args) throws IOException {
		
		String locationStr = args[0];
		String deploymentFile = "/mnt/deployment/deployment.json";
		String resultsFile = "/mnt/results/" + locationStr + "/results.csv";
		String resourcesMonitoringFile = "/mnt/results/" + locationStr + "/resources_monitoring.csv";
		String devicesResultsFile = "/mnt/results/" + locationStr + "/results_devices.csv";
		
		File file = new File(resultsFile);
		if (file.exists())
			file.delete();
		
		File devicesFile = new File(devicesResultsFile);
		if (devicesFile.exists())
			devicesFile.delete();
		
		File resourcesFile = new File(resourcesMonitoringFile);
		if (resourcesFile.exists())
			resourcesFile.delete();
		
		String jsonString = new String(Files.readAllBytes(Paths.get(deploymentFile)), StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(jsonString);
		Location location = Location.valueOf(locationStr.toUpperCase());
		

			
		logger.info("Starting " + location + " components");
		
		HashMap<String, Device> devices = new HashMap<String, Device>();
		HashMap<String, VirtualSensor> virtualSensors = new HashMap<String, VirtualSensor>();
		HashMap<String, Application> applications = new HashMap<String, Application>();
		
		DeploymentParser parser = new DeploymentParser(location);
		
		parser.parseJson(jsonObject, location, devices, virtualSensors, applications);
		
		for (Device device : devices.values()) {
			logger.info("Activating " + device.getId());
			device.activate();
		}
			
		
		for (VirtualSensor virtualSensor : virtualSensors.values()) {
			logger.info("Activating " + virtualSensor.getId());
			virtualSensor.activate();
		}
			
		
		for (Application app : applications.values()) {
			logger.info("Activating " + app.getId());
			app.activate();
		}
		
		Thread thread = new Thread() {
			public void run() {
				ResourcesMonitor.collectAndSaveSystemInfo(resourcesMonitoringFile);
			}
		};
		thread.start();
		
	}
}
