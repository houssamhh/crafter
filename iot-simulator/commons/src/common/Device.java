package common;

public interface Device {
	
	public String getId();
	
//	public HashMap<Location, HashSet<DataType>> getDataTypesProvided();
//	
//	public ProviderType getProviderType();
//	
//	public String getIp();
//	
//	public int getPort();
	
	public Location getLocation();
	
	public void activate();
	public void connectToBroker();
	
	@Override
	public boolean equals(Object object);
	
//	@Override
//	public String toString();
}
