package configure;

import java.util.HashMap;

public class ConfigureManager {
	
	final static String configurationFilename = "configuration.txt";
	
	public static void addOrUpdateConfiguration(String key, String value){
		
		HashMap<String, String> hashmap;
		
		if (FileIO.isExist(configurationFilename)){
			hashmap = FileIO.read(configurationFilename);
		}
		else{
			hashmap = new HashMap<String, String>();
		}
		
		hashmap.put(key, value);
		FileIO.write(configurationFilename, hashmap, false);
	}
	
	public static String retrieveConfiguration(String key){
		
		if (FileIO.isExist(configurationFilename)){
			HashMap<String, String> hashmap = FileIO.read(configurationFilename);
			if (hashmap.containsKey(key)){
				return hashmap.get(key);
			}
		}
		
		return null;
	}
}

