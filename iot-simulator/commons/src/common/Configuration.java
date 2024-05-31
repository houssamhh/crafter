package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	
	public static final String brokerUrl;
	public static final String entranceIp;
	public static final String hall1Ip;
	public static final String hall2Ip;
	public static final String hall3Ip;
	public static final String restIp;
	public static final String shopIp;
	public static final String controllerIp;
	public static final int controllerPort;
	
	public static final double bandwidth;
	
	static {
		Properties props = new Properties();
		String fileName = "resources/config.properties";
		try (FileInputStream fis = new FileInputStream(fileName)) {
			props.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		brokerUrl = props.getProperty("brokerUrl");
		entranceIp = props.getProperty("entranceIp");
		hall1Ip = props.getProperty("hall1Ip");
		hall2Ip = props.getProperty("hall2Ip");
		hall3Ip = props.getProperty("hall3Ip");
		restIp = props.getProperty("restIp");
		shopIp = props.getProperty("shopIp");
		controllerIp = props.getProperty("controllerIp");
		controllerPort = Integer.valueOf(props.getProperty("controllerPort"));
		bandwidth = Double.valueOf(props.getProperty("bandwidth"));
		
	}

}
