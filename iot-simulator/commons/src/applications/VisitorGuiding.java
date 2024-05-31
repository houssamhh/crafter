package applications;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;

import common.Location;
import common.ResultsWriter;

public class VisitorGuiding extends Application {

	public VisitorGuiding(String clientId, String clientName, Location location, HashSet<String> subscriptions) {
		super(clientId, clientName, location, subscriptions);
		resultsWriter = new ResultsWriter(location);
		this.logger = LogManager.getLogger(VisitorGuiding.class);
	}
}
