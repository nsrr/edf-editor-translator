package translator.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Base class for all annotation translation, which provides basic attributes and 
 * functionalities of an translation procedure
 * @author wei wang, 2014-8-21
 */
public class BasicTranslation {
	
	protected String softwareVersion;
	protected String xmlAnnotation; // annotation file path
	protected String edfFile;  // EDF file path
	protected String output; // output file path: initialized in write() method
	protected Document document;	// document is the result of resolving BOM	
	protected Set<String> events;
	protected HashMap<String,Object>[] map;
	protected String[] timeStart;
	
	/**
	 * Reads mapping file and saves as an array of HashMap
	 * Can be put in a higher hierarchy
	 * @param mapFile the mapping file name
	 * @return  the mapping in form of HashMap
	 */
	public static HashMap<String,Object>[] readMapFile(String mapFile) {
		// System.out.println("Read map file...");  // for test
		@SuppressWarnings("unchecked")
		// HashMap[] map = new HashMap[3]; // original
		HashMap<String,Object>[] map = (HashMap<String,Object>[]) Array.newInstance(HashMap.class, 3);
		// HashMap map = new HashMap();
		try {
			BufferedReader input =  new BufferedReader(new FileReader(mapFile));
			try {
				String line = input.readLine();
				HashMap<String,Object> epoch = new HashMap<String,Object>();
				HashMap<String,Object> events = new HashMap<String,Object>();
				HashMap<String,Object> stages = new HashMap<String,Object>();
				while ((line = input.readLine()) != null) {
					String[] data = line.split(",");
					// process events
					if (data[0].compareTo("EpochLength") != 0 && data[0].compareTo("Sleep Staging") != 0) {
						// values: {EventType, EventConcept, Note}
						ArrayList<String> values = new ArrayList<String>(3);
						values.add(data[0]);
						values.add(data[2]);
						if (data.length >= 4) {
							values.add(data[3]);
						}
						// events {event, event_type && event_concept}
						events.put(data[1], values);
					} else if (data[0].compareTo("EpochLength") == 0) {
						// System.out.println(data[0]);
						epoch.put(data[0], data[2]);
					} else {
						// stages {event, event_concept}
						stages.put(data[1], data[2]);
					}
				}	
				// System.out.println(map[2].values().size());
				map[0] = epoch;
				map[1] = events;
				map[2] = stages;
			} finally {
				input.close();
			}
		} catch(IOException e) {
			e.printStackTrace();			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log(errors.toString());
		}
		return map;
	}
	
	/**
	 * Resolves BOM and stores the result document
	 * @param xmlAnnotationFile the xml annotation file
	 * @return true if this operation successful
	 */
	public Document resolveBOM(String xmlAnnotationFile) {
		Document doc = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(xmlAnnotationFile));
			@SuppressWarnings("resource")
			BOMInputStream bomInputStream = new BOMInputStream(inputStream);
			ByteOrderMark bom = bomInputStream.getBOM();
			String charsetName = bom == null ? "UTF-8" : bom.getCharsetName();
			inputStream.close();
		
			inputStream = new FileInputStream(new File(xmlAnnotationFile));
			Reader reader = new InputStreamReader(inputStream, charsetName);
			InputSource is = new InputSource(reader);
			is.setEncoding(charsetName);
		
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
		} catch(Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log(errors.toString());
		} finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return doc;
	}
	
	/**
	 * Logs messages.
	 * @param message the message to be logged
	 */
	public static void log(String message) {
		TranslationController.translationErrors += message;
	}
	
	/**
	 * Records the start date from the EDF file
	 * Can be put in a higher hierarchy
	 * @param edfFile the EDF file name
	 * @return the start date and duration, first string is start date and the second is duration
	 */
	public String[] recordStartDate(String edfFile) {
		String[] startDate = new String[2];
		try {
			RandomAccessFile edfFileRead = new RandomAccessFile(new File(edfFile), "r");
			edfFileRead.seek(168);
			char[] date = new char[8];
			for (int i = 0; i < 8; i++) {
				date[i] = (char)edfFileRead.readByte();
			}
			
			// edf.read(date);
			char[] time = new char[8];
			for (int i = 0; i < 8; i++) {
				time[i] = (char)edfFileRead.readByte();
			}
			edfFileRead.seek(236);
		
			char[] numRec = new char[8];
			for (int i = 0; i < 8; i++) {
				numRec[i] = (char)edfFileRead.readByte();
				//System.out.println(dur[i]);
			}
			char[] durRec = new char[8];
			for (int i = 0; i < 8; i++) {
				durRec[i] = (char)edfFileRead.readByte();
				//System.out.println(dur[i]);
			}
			
			// long numRec = edf.readLong();
			// long durRec = edf.readLong();
			long duration = Long.parseLong(String.valueOf(durRec).trim()) * 
					Long.parseLong(String.valueOf(numRec).trim());
			// long duration = 0;
			// edf.read(time);
			startDate[0] = String.valueOf(date) + " " + String.valueOf(time);
			startDate[1] = String.valueOf(duration);
			edfFileRead.close();
		} catch (Exception e) {
			e.printStackTrace();			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log(errors.toString());
		}
		timeStart = startDate;
		return startDate;
	}
}
