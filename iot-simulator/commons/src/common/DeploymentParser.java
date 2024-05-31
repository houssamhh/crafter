package common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONObject;

import applications.Application;
import applications.Maintenance;
import applications.Metaverse;
import applications.Security;
import applications.SmartLighting;
import applications.VisitorGuiding;
import devices.AirQualitySensor;
import devices.Camera;
import devices.DoorSensor;
import devices.HumiditySensor;
import devices.IoTdevice;
import devices.LightSensor;
import devices.MotionSensor;
import devices.NoiseSensor;
import devices.ProximitySensor;
import devices.RFIDSensor;
import devices.TemperatureSensor;
import virtualsensors.EnvironmentalAnalysis;
import virtualsensors.PredictiveMaintenance;
import virtualsensors.QueueManagement;
import virtualsensors.VirtualOccupancy;
import virtualsensors.VirtualSecurity;
import virtualsensors.VirtualSensor;

public class DeploymentParser {

	public ComponentsConfig config;
	
	public DeploymentParser(Location location) {
		config = new ComponentsConfig();
		try {
			config.getConfig(location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DeploymentParser() {
		config = new ComponentsConfig();
	}
	
	public void parseJson(JSONObject jsonObject, Location location, HashMap<String, Device> devices,
			HashMap<String, VirtualSensor> virtualSensors, HashMap<String, Application> applications) {
		
		//Devices
		JSONObject devicesObject = jsonObject.getJSONObject("devices");
		Iterator<String> keys = devicesObject.keys();
		while(keys.hasNext()) {
			String sensorType = keys.next();
			int number = devicesObject.getInt(sensorType);
			if (sensorType.equals("airquality")) {
				for (int i = 0; i < number; i++) {
					AirQualitySensor sensor = createAirQualitySensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("door")) {
				for (int i = 0; i < number; i++) {
					DoorSensor sensor = createDoorSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if(sensorType.equals("humidity")) {
				for (int i = 0; i < number; i++) {
					HumiditySensor sensor = createHumiditySensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("light")) {
				for (int i = 0; i < number; i++) {
					LightSensor sensor = createLightSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("motion")) {
				for (int i = 0; i < number; i++) {
					MotionSensor sensor = createMotionSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("noise")) {
				for (int i = 0; i < number; i++) {
					NoiseSensor sensor = createNoiseSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("proximity")) {
				for (int i = 0; i < number; i++) {
					ProximitySensor sensor = createProximitySensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("rfid")) {
				for (int i = 0; i < number; i++) {
					RFIDSensor sensor = createRFIDSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("temperature")) {
				for (int i = 0; i < number; i++) {
					TemperatureSensor sensor = createTemperatureSensor(location);
					devices.put(sensor.clientId, sensor);
				}
			}
			else if (sensorType.equals("camera")) {
				for (int i = 0; i < number; i++) {
					Camera sensor = createCamera(location);
					devices.put(sensor.clientId, sensor);
				}
			}			
		}
		
		//Virtual Sensors
		JSONObject virtualsensorsObject = jsonObject.getJSONObject("virtualsensors");
		keys = virtualsensorsObject.keys();
		while(keys.hasNext()) {
			String virtualsensorType = keys.next();
			if (virtualsensorType.equals("virtual_occupancy")) {
				int number = virtualsensorsObject.getInt(virtualsensorType);
				for (int i = 0; i < number; i++) {
					VirtualOccupancy sensor = createVirtualOccupancySensor(location);
					virtualSensors.put(sensor.clientId, sensor);
				}
			}
			else if (virtualsensorType.equals("queue_management")) {
				int number = virtualsensorsObject.getInt(virtualsensorType);
				for (int i = 0; i < number; i++) {
					QueueManagement sensor = createQueueManagementSensor(location);
					virtualSensors.put(sensor.clientId, sensor);
				}
			}
			else if (virtualsensorType.equals("virtual_security")) {
				int number = virtualsensorsObject.getInt(virtualsensorType);
				for (int i = 0; i < number; i++) {
					VirtualSecurity sensor = createVirtualSecuritySensor(location);
					virtualSensors.put(sensor.clientId, sensor);
				}
			}
			else if (virtualsensorType.equals("predictive_maintenance")) {
				int number = virtualsensorsObject.getInt(virtualsensorType);
				for (int i = 0; i < number; i++) {
					PredictiveMaintenance sensor = createPredictiveMaintenanceSensor(location);
					virtualSensors.put(sensor.clientId, sensor);
				}
			}
			else if (virtualsensorType.equals("environmental_analysis")) {
				int number = virtualsensorsObject.getInt(virtualsensorType);
				for (int i = 0; i < number; i++) {
					EnvironmentalAnalysis sensor = createEnvironmentalAnalysisSensor(location);
					virtualSensors.put(sensor.clientId, sensor);
				}
			}
		}
		
		//Applications
		JSONObject applicationsObject = jsonObject.getJSONObject("applications");
		keys = applicationsObject.keys();
		while (keys.hasNext()) {
			String applicationType = keys.next();
			if (applicationType.equals("smart_lighting")) {
				int number = applicationsObject.getInt(applicationType);
				for (int i = 0; i < number; i++) {
					SmartLighting application = createSmartLightingApp(location);
					applications.put(application.clientId, application);
				}
			}
			else if (applicationType.equals("security")) {
				int number = applicationsObject.getInt(applicationType);
				for (int i = 0; i < number; i++) {
					Security application = createSecurityApp(location);
					applications.put(application.clientId, application);
				}
			}
			else if (applicationType.equals("visitor_guiding")) {
				int number = applicationsObject.getInt(applicationType);
				for (int i = 0; i < number; i++) {
					VisitorGuiding application = createVisitorGuidingApp(location);
					applications.put(application.clientId, application);
				}
			}
			else if (applicationType.equals("maintenance")) {
				int number = applicationsObject.getInt(applicationType);
				for (int i = 0; i < number; i++) {
					Maintenance application = createMaintenanceApp(location);
					applications.put(application.clientId, application);
				}
			}
			else if (applicationType.equals("metaverse")) {
				int number = applicationsObject.getInt(applicationType);
				for (int i = 0; i < number; i++) {
					Metaverse application = createMetaverseApp(location);
					applications.put(application.clientId, application);
				}
			}
		}		
	}
	
	/**
	 * Methods for creating devices
	 */
	public AirQualitySensor createAirQualitySensor(Location location) {
		IoTdevice model = config.devicesProperties.get("airquality");
		String id = MqttClient.generateClientId();
		AirQualitySensor sensor = new AirQualitySensor(id, "airquality" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public DoorSensor createDoorSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("door");
		DoorSensor sensor = new DoorSensor(id, "door" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public HumiditySensor createHumiditySensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("humidity");
		HumiditySensor sensor = new HumiditySensor(id, "humidity" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public LightSensor createLightSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("light");
		LightSensor sensor = new LightSensor(id, "light" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public MotionSensor createMotionSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("motion");
		MotionSensor sensor = new MotionSensor(id, "motion" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public NoiseSensor createNoiseSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("noise");
		NoiseSensor sensor = new NoiseSensor(id, "noise" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public ProximitySensor createProximitySensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("proximity");
		ProximitySensor sensor = new ProximitySensor(id, "proximity" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public RFIDSensor createRFIDSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("rfid");
		RFIDSensor sensor = new RFIDSensor(id, "rfid" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public TemperatureSensor createTemperatureSensor(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("temperature");
		TemperatureSensor sensor = new TemperatureSensor(id, "temperature" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	public Camera createCamera(Location location) {
		String id = MqttClient.generateClientId();
		IoTdevice model = config.devicesProperties.get("camera");
		Camera sensor = new Camera(id, "camera" + "-" + id, location, model.getDataTypesProvided(), model.initialSensingPeriod, 
				model.unavailabilityProbability, model.unavailabilityDuration, model.visitors);
		return sensor;
	}
	
	
	/**
	 * Methods for creating virtual sensors
	 */	
	public VirtualOccupancy createVirtualOccupancySensor(Location location) {
		String id = MqttClient.generateClientId();
		VirtualSensor model = config.virtualsensorsProperties.get("virtual_occupancy");
		VirtualOccupancy sensor = new VirtualOccupancy(id, "virtual_occupancy" + "-" + id, location, model.subscriptions, model.dataTypesProvided);
		return sensor;
	}
	
	public QueueManagement createQueueManagementSensor(Location location) {
		String id = MqttClient.generateClientId();
		VirtualSensor model = config.virtualsensorsProperties.get("queue_management");
		QueueManagement sensor = new QueueManagement(id, "queue_management" + "-" + id, location, model.subscriptions, model.dataTypesProvided);
		return sensor;
	}
	
	public VirtualSecurity createVirtualSecuritySensor(Location location) {
		String id = MqttClient.generateClientId();
		VirtualSensor model = config.virtualsensorsProperties.get("virtual_security");
		VirtualSecurity sensor = new VirtualSecurity(id, "virtual_security" + "-" + id, location, model.subscriptions, model.dataTypesProvided);
		return sensor;
	}
	
	public PredictiveMaintenance createPredictiveMaintenanceSensor(Location location) {
		String id = MqttClient.generateClientId();
		VirtualSensor model = config.virtualsensorsProperties.get("predictive_maintenance");
		PredictiveMaintenance sensor = new PredictiveMaintenance(id, "predictive_maintenance" + "-" + id, location, model.subscriptions, model.dataTypesProvided);
		return sensor;
	}
	
	public EnvironmentalAnalysis createEnvironmentalAnalysisSensor(Location location) {
		String id = MqttClient.generateClientId();
		VirtualSensor model = config.virtualsensorsProperties.get("environmental_analysis");
		EnvironmentalAnalysis sensor = new EnvironmentalAnalysis(id, "environemntal_analysis" + "-" + id, location, model.subscriptions, model.dataTypesProvided);
		return sensor;
	}
	
	/**
	 * Methods for creating applications
	 *
	 */
	public SmartLighting createSmartLightingApp(Location location) {
		String id = MqttClient.generateClientId();
		Application model = config.applicationsProperties.get("smart_lighting");
		SmartLighting application = new SmartLighting(id, "smart_lighting" + "-" + id, location, model.subscriptions);
		return application;
	}
	
	public Security createSecurityApp(Location location) {
		String id = MqttClient.generateClientId();
		Application model = config.applicationsProperties.get("security");
		Security application = new Security(id, "security" + "-" + id, location, model.subscriptions);
		return application;
	}
	
	public VisitorGuiding createVisitorGuidingApp(Location location) {
		String id = MqttClient.generateClientId();
		Application model = config.applicationsProperties.get("visitor_guiding");
		VisitorGuiding application = new VisitorGuiding(id, "visitor_guiding" + "-" + id, location, model.subscriptions);
		return application;
	}
	
	public Maintenance createMaintenanceApp(Location location) {
		String id = MqttClient.generateClientId();
		Application model = config.applicationsProperties.get("maintenance");
		Maintenance application = new Maintenance(id, "maintenance" + "-" + id, location, model.subscriptions);
		return application;
	}	
	
	public Metaverse createMetaverseApp(Location location) {
		String id = MqttClient.generateClientId();
		Application model = config.applicationsProperties.get("metaverse");
		Metaverse application = new Metaverse(id, "metaverse" + "-" + id, location, model.subscriptions);
		return application;
	}	

}
