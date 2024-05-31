package applications;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;

import common.Location;
import common.ResultsWriter;

public class SmartLighting extends Application {
	
	public SmartLighting(String clientId, String clientName, Location location, HashSet<String> subscriptions) {
		super(clientId, clientName, location, subscriptions);
		resultsWriter = new ResultsWriter(location);
		this.logger = LogManager.getLogger(SmartLighting.class);
	}
	
	

}
