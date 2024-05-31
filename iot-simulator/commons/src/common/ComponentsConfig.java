package common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import applications.Application;
import devices.IoTdevice;
import virtualsensors.VirtualSensor;

public class ComponentsConfig {

	HashMap<String, IoTdevice> devicesProperties = new HashMap<String, IoTdevice>();
	HashMap<String, VirtualSensor> virtualsensorsProperties = new HashMap<String, VirtualSensor>();
	HashMap<String, Application> applicationsProperties = new HashMap<String, Application>();

	
	public void getConfig(Location location) throws IOException {

		String jsonString = new String(Files.readAllBytes(Paths.get("/mnt/config/componentsConfig.json")),
				StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(jsonString);

		//Parsing number of visitors
		String jsonFile = new String(Files.readAllBytes(Paths.get("/mnt/deployment/deployment.json")),
				StandardCharsets.UTF_8);
		JSONObject jObject = new JSONObject(jsonFile);
		JSONObject visitorsObjects = jObject.getJSONObject("visitors");
		JSONArray periodsArr = visitorsObjects.getJSONArray("periods");
		JSONArray nbpeopleArr = visitorsObjects.getJSONArray("nbpeople");
		ArrayList<Integer> periods = new ArrayList<Integer>();
		ArrayList<Integer> nbPeople = new ArrayList<Integer>();
		for (int i = 0; i < periodsArr.length(); i++) {
			periods.add((Integer) periodsArr.get(i));
		}
		for (int i = 0; i < nbpeopleArr.length(); i++) {
			nbPeople.add((Integer) nbpeopleArr.get(i));
		}	
		
		HashMap<String, ArrayList<Integer>> visitors = new HashMap<String, ArrayList<Integer>>();
		visitors.put("periods", periods);
		visitors.put("nbpeople", nbPeople);
		
				
		// Parsing devices
		JSONObject devicesObject = jsonObject.getJSONObject("devices");
		Iterator<String> keys = devicesObject.keys();
		while (keys.hasNext()) {
			String sensorType = keys.next();
			JSONObject properties = devicesObject.getJSONObject(sensorType);
			double sensingPeriod = properties.getDouble("sensingPeriod");
			JSONArray dataTypesArr = properties.getJSONArray("dataTypes");
			HashSet<DataType> dataTypesProvided = new HashSet<DataType>();
			for (int j = 0; j < dataTypesArr.length(); j++) {
				String dataType = (String) dataTypesArr.get(j);
				dataTypesProvided.add(DataType.valueOf(dataType.toUpperCase()));
			}
			double unavailabilityProbability = properties.getDouble("unavailabilityProbability");
			int unavailabilityDuration = properties.getInt("unavailabilityDuration");
			String deviceName = sensorType + "-model";
			IoTdevice device = new IoTdevice(deviceName, sensorType, location, dataTypesProvided, sensingPeriod,
                    unavailabilityProbability, unavailabilityDuration, visitors);
			devicesProperties.put(sensorType, device);
		}

		// Parsing virtual sensors
		JSONObject virtualsensorsObject = jsonObject.getJSONObject("virtualsensors");
		keys = virtualsensorsObject.keys();
		while (keys.hasNext()) {
			String sensorType = keys.next();
			JSONObject properties = virtualsensorsObject.getJSONObject(sensorType);
			JSONArray subscriptionsArr = properties.getJSONArray("subscriptions");
			JSONArray dataTypesProvidedArr = properties.getJSONArray("dataTypesProvided");
			
			HashSet<String> subscriptions = new HashSet<String>();
			for (int j = 0; j < subscriptionsArr.length(); j++) {
				String topic = (String) subscriptionsArr.get(j);
				subscriptions.add(topic);
			}
			
			HashSet<DataType> dataTypesProvided = new HashSet<DataType>();
			for (int j = 0; j < dataTypesProvidedArr.length(); j++) {
				String dataType = (String) dataTypesProvidedArr.get(j);
				dataTypesProvided.add(DataType.valueOf(dataType.toUpperCase()));
			}
			
			String deviceName = sensorType + "-model";
			VirtualSensor virtualSensor = new VirtualSensor(deviceName, sensorType, location, subscriptions, dataTypesProvided);
			virtualsensorsProperties.put(sensorType, virtualSensor);
		}

		// Parsing applications
		JSONObject applicationsObject = jsonObject.getJSONObject("applications");
		keys = applicationsObject.keys();
		while (keys.hasNext()) {
			HashSet<String> subscriptions = new HashSet<String>();
			String applicationType = keys.next();
			JSONObject properties = applicationsObject.getJSONObject(applicationType);
			JSONArray subscriptionsArr = properties.getJSONArray("subscriptions");
			for (int j = 0; j < subscriptionsArr.length(); j++) {
				String topic = (String) subscriptionsArr.get(j);
				subscriptions.add(topic);
			}

			String applicationName = applicationType + "-model";
			Application application = new Application(applicationName, applicationType, location, subscriptions);
			applicationsProperties.put(applicationType, application);
		}
			
	}
}
