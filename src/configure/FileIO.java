package configure;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * TODO
 */
public class FileIO {

	final static String sperator = "\t";

	/**
	 * Write out the output file specified by the file name
	 * @param outFile the file name of the output file
	 * @param hashmap the hash map which stores the data
	 * @param append if true, the bytes will be written to the end of the file
	 * rather than the beginning 
	 */
	public static synchronized void write(String outFile, HashMap<String, String> hashmap, boolean append) {

		BufferedWriter out = null;
		try {
			createAbsolutePath(outFile);
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile, append)));

			if (hashmap!=null){
				Iterator<Entry<String, String>> iterator = hashmap.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String, String> entry = iterator.next();
					String key = entry.getKey();
					String value = entry.getValue();
					out.write(key + sperator + value + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read in a file specified by a String. Put the content into a map
	 * @param filename the file to be read in
	 * @return a HashMap contains the data read in from the file
	 */
	public static synchronized HashMap<String, String> read(String filename) {

		HashMap<String, String> hashmap = null;

		if (filename != null) {
			hashmap = new HashMap<String, String>();
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(filename)));
				String inline;
				while ((inline = in.readLine()) != null){
					String[] items = inline.split(sperator);
					if (items.length == 2)
						hashmap.put(items[0], items[1]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return hashmap;
	}
	
	/**
	 * Determine whether a file existed or not
	 * @param filename the file path string
	 * @return true if the file path exist
	 */
	public static synchronized boolean isExist(String filename) {
		try {
			File f = new File(filename);
			return f.exists();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Create an absolute path from a string.
	 * @param absPath a String that specifies the absolute path.
	 */
	public static void createAbsolutePath(String absPath) {
		try {
			String tarfileDir = absPath.substring(0, absPath.lastIndexOf("/"));
			File f = new File(tarfileDir);
			if (!f.exists()) {
				f.mkdirs();
			}
		} catch (Exception e) {
			e.getStackTrace();
		}

	}
}