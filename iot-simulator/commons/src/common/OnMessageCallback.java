package common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class OnMessageCallback implements MqttCallback {
	
	private static final Logger logger = LogManager.getLogger(OnMessageCallback.class);

//	private ObjectMapper objectMapper = new ObjectMapper();
	//TODO: check if we need ResultsWriter here
//	private ResultsWriter resultsWriter = new ResultsWriter();
	private String clientId;
	
	public OnMessageCallback(String clientId) {
		this.clientId = clientId;
	}
	public void connectionLost(Throwable cause) {
        // After the connection is lost, it usually reconnects here
        logger.fatal(clientId + " disconnected from broker!");
        logger.fatal(cause.getMessage() + cause.getStackTrace());
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
//    	Map<String, Object> messageAsObject = objectMapper.readValue(new String(message.getPayload()),
//				new TypeReference<>() {
//				});
//    	String payload = (String) messageAsObject.get("payload");
//		long timestamp = (long) messageAsObject.get("timestamp");
//		long latency = (System.currentTimeMillis() - timestamp);
//		Location location = Location.valueOf(topic.split("/")[0]);
//        resultsWriter.writeData(topic, clientId, location, payload, latency);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
//        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}