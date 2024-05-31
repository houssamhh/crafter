package applications;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;

import common.Location;
import common.ResultsWriter;

public class Metaverse extends Application{

	public Metaverse(String clientId, String clientName, Location location, HashSet<String> subscriptions) {
		super(clientId, clientName, location, subscriptions);
		resultsWriter = new ResultsWriter(location);
		this.logger = LogManager.getLogger(Metaverse.class);
	}
}
