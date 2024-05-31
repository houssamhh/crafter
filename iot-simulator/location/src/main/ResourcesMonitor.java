package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
//import com.sun.management.OperatingSystemMXBean;
import java.lang.management.OperatingSystemMXBean;

public class ResourcesMonitor {

	
	
	public static void collectAndSaveSystemInfo(String resourcesMonitoringFile) {
		long interval = 5000;
		
		while (true) { 
			// Get CPU usage
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			double cpuUsage = osBean.getSystemLoadAverage();
			
	        
			//Get memory usage
			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			long usedMemory = totalMemory - freeMemory;
			
			
			//Get free disk space
			File diskPartition = new File("/");
			long freeSpace = diskPartition.getFreeSpace();
			
			// Save collected information
			String timestamp = String.valueOf(System.currentTimeMillis());
			StringBuffer sb = new StringBuffer(timestamp + "," + cpuUsage + "," + freeMemory + "," + freeSpace + "\n");
			try {
				BufferedWriter bf = new BufferedWriter(new FileWriter(new File(resourcesMonitoringFile), true));
				bf.write(sb.toString());
				bf.flush();
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        try {
	        	Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//not used
	public static StringBuilder getCpuUsage() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		String cmd = "awk '{u=$2+$4; t=$2+$4+$5; if (NR==1){u1=u; t1=t;} else print ($2+$4-u1) * 100 / (t-t1) \"%\"; }' <(grep 'cpu ' /proc/stat) <(sleep 1;grep 'cpu ' /proc/stat)";
		processBuilder.command("bash", "-c", cmd);
			try {

				Process process = processBuilder.start();

				StringBuilder output = new StringBuilder();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line + "\n");
				}

				int exitVal = process.waitFor();
				if (exitVal == 0) {
					return output;
				} else {
					return new StringBuilder("-1");
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return new StringBuilder("-1");

	}
}
