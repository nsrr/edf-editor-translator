package configure;

import java.util.HashMap;

/**
 * TODO
 */
public class ConfigureManager {

	final static String configurationFilename = "configuration.txt";

	/**
	 * Add if the configuration file specified does not existed
	 * @param key the key of a configuration entry
	 * @param value the value of a configuration entry
	 */
	public static void addOrUpdateConfiguration(String key, String value){

		HashMap<String, String> hashmap;

		if (FileIO.isExist(configurationFilename)){
			hashmap = FileIO.read(configurationFilename);
		} else {
			hashmap = new HashMap<String, String>();
		}

		hashmap.put(key, value);
		FileIO.write(configurationFilename, hashmap, false);
	}

	/**
	 * Get the configuration value
	 * @param key a key of the configuration entry
	 * @return the value of the configuration entry specified by the key
	 */
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