package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultsWriter {

	String fileName;
	String devicesFileName;
	
	Logger logger = LogManager.getLogger(ResultsWriter.class);
	
	
	public ResultsWriter(Location location) {
		String locationStr = location.toString().toLowerCase();
		fileName = "/mnt/results/" + locationStr + "/results.csv";
		devicesFileName = "/mnt/results/" + locationStr + "/results_devices.csv";
	}
	public void writeData(String topic, String clientId, Location location, String payload, long latency) {		
		String timestamp = String.valueOf(System.currentTimeMillis());
		StringBuffer sb = new StringBuffer(timestamp + "," + clientId + "," + topic + "," + location + "," + payload + "," + String.valueOf(latency) + "\n");
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File(fileName), true));
			bf.write(sb.toString());
			bf.flush();
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeData(String topic, String clientId, Location location, long latency) {		
		String timestamp = String.valueOf(System.currentTimeMillis());
		StringBuffer sb = new StringBuffer(timestamp + "," + clientId + "," + topic + "," + location + "," + String.valueOf(latency) + "\n");
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File(fileName), true));
			bf.write(sb.toString());
			bf.flush();
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeDataDevices(String clientId, Location location, String status) {		
		String timestamp = String.valueOf(System.currentTimeMillis());
		StringBuffer sb = new StringBuffer(timestamp + "," + clientId + "," + location + "," + status + "\n");
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File(devicesFileName), true));
			bf.write(sb.toString());
			bf.flush();
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
