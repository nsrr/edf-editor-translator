package configure;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class FileIO {
	
	final static String sperator = "\t";
	
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
	
	public static synchronized boolean isExist(String filename) {
		try {
			File f = new File(filename);
			return f.exists();
		} catch (Exception e) {
			return false;
		}
	}

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
