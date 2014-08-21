package translator.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Older version of EmblaAnnotationTranslator
 * should be removed later
 * @author wei wang, 2014-8-21
 */
public class EmblaTranslation {
	// Old-version
	// Available events:
	// *APNEA;
	// *APNEA-OBSTRUCTIVE;
	// APNEA-MIXED; (no occurrence)
	// *APNEA-CENTRAL;
	// *DESAT;
	// *HYPOPNEA;
	// LIGHTS-OFF; (no occurrence)
	// LIGHTS-ON; (no occurrence)
	
	// algorithm:
	// 1. read mapping file
	// 2. create xml header: PSGAnnotation, SoftwareVersion, Embla, EpochLength...
	// 3. Resolve BOM
	// 4. loop through and process events

	private String format = "xml"; // "txt" or "xml", in lower case
	
	private String xmlAnnotation; // annotation file path
	private String edfFile;  // EDF file path
	private String output; // output file path
//	private String mapFile; // map file path
	
	public Document document;	// document is the result of resolving BOM	
	private ArrayList<String> events;
	public HashMap<String,Object>[] map;
	private Document xmlRoot = new DocumentImpl();; // xml root
	private Element scoredEvents; // parent element of <Event>
	private String[] timeStart;
	
	/**
	 * Default constructor
	 */
	public EmblaTranslation() {
		super();
	}

	/**
	 * Standardizes the xml annotation and stores as a Document, then records event types(Mostly used)
	 * @param mapFile mapping file path
	 * @param xmlAnnotation the Embla xml annotation file
	 * @param edfFile EDF file path
	 * @param output output file path
	 */
	public EmblaTranslation(String mapFile, String xmlAnnotation, String edfFile, String output) {
//		this.mapFile = mapFile;
		this.xmlAnnotation = xmlAnnotation;
		this.edfFile = edfFile;
		this.output = output;
		boolean result = false;
		document = resolveBOM(xmlAnnotation);
		result = recordEvents(document);
		map = readMapFile(mapFile);
		if(!result) {
			log("Cannot parse the events in the annotation file");
		}
	}
	
	/**
	 * Standardizes the annotation and stores as a Document, then records event types.
	 * Used for later support
	 * @param mapFile mapping file path
	 * @param format .txt file or .xml file 
	 * @param xmlAnnotation the Embla xml annotation file
	 * @param edfFile EDF file path
	 * @param output output file path
	 */
	public EmblaTranslation(String mapFile, String format, String xmlAnnotation, String edfFile, String output) {
//		this.mapFile = mapFile;
		this.edfFile = edfFile;
		this.output = output;
		this.xmlAnnotation = xmlAnnotation;
		boolean result = false;
		this.format = format;
		document = resolveBOM(xmlAnnotation);
		result = recordEvents(document);
		map = readMapFile(mapFile);
		if(!result) {
			log("Cannot parse the events in the annotation file");
		}
	}

	/**
	 * Translates Embla annotation file using the mapping file and the corresponding EDF file
	 * @return true if the process is successful
	 */
	public boolean translate() {
		// Algorithm:
		//  (a) Creates meta data
		//  (b) Creates first ScoredEvent: Recording start time from EDF file
		//  (c) Creates the rest ScoredEvents
		//  (d) Save xml to output file
		boolean result = true;
		if(!format.equals("xml")) {
			result = false;
			return result;
		}
		
		Element root = xmlRoot.createElement("PSGAnnotation");
		Element software = xmlRoot.createElement("SoftwareVersion");
		software.appendChild(xmlRoot.createTextNode("Embla xml"));
		Element epoch = xmlRoot.createElement("EpochLength");
		epoch.appendChild(xmlRoot.createTextNode((String) map[0].get("EpochLength")) );
		root.appendChild(software);
		root.appendChild(epoch);															// (a) end
		
		scoredEvents = xmlRoot.createElement("ScoredEvents");
		
		// stores 'String[2]' into 'String[] timeStart'
		recordStartDate(edfFile);
		String[] elmts = new String[3];
		elmts[0] = "Recording Start Time";
		elmts[1] = "0";
		elmts[2] = timeStart[1];
		Element timeElement = addElements(xmlRoot, elmts);
		Element clock = xmlRoot.createElement("ClockTime");
		clock.appendChild(xmlRoot.createTextNode(timeStart[0]));
		timeElement.appendChild(clock);
		scoredEvents.appendChild(timeElement);												// (b) end
		
		Document doc = document;
		NodeList nodeList = doc.getElementsByTagName("Event");
			
		for(int index = 0; index < nodeList.getLength(); index++) {
			Element parsedElement = null;
			Node node = nodeList.item(index);  // for each <event> node
			Element elem = (Element)node;
			// heart:
			parsedElement = parseEmblaXmlEvent(elem);
			if(parsedElement == null) {
				log("Can't parse event: " + index + " " + getElementByChildTag(elem, "Type"));
				// for test
				System.out.println("Can't parse event: " + index + " " + getElementByChildTag(elem, "Type"));
			}
			scoredEvents.appendChild(parsedElement);
		}
		
		root.appendChild(scoredEvents);
		xmlRoot.appendChild(root);															// (c) end
		saveXML(xmlRoot, output);															// (d) end
		System.out.println("DONE!");  // test
		return result;
	}

//	/**
//	 * Parses event element and returns an parsed element
//	 * @param scoredEventElement the event name in String
//	 * @return the parsed element
//	 */
//	private Element parseEmblaXmlEvent(Element scoredEventElement) {
//		
//		// only DESAT type has more values to be processed, others are the same
//		Element scoredEvent = null;
//		boolean result = false;
//		String eventType = getElementByChildTag(scoredEventElement, "Type");
//		// map[1] contains keySet with event name
//		if(map[1].keySet().contains(eventType)) {
//			result = true;
//			// use enum for improvement
//			if(eventType.equals("APNEA")) {
//				scoredEvent = parseApnea(scoredEventElement);
//			} else if(eventType.equals("APNEA-CENTRAL")) {
//				scoredEvent = parseApneaCentral(scoredEventElement);
//			} else if(eventType.equals("APNEA-MIXED")) {
//				scoredEvent = parseApneaMixed(scoredEventElement);
//			} else if(eventType.equals("APNEA-OBSTRUCTIVE")) {
//				scoredEvent = parseApneaObstructive(scoredEventElement);
//			} else if(eventType.equals("DESAT")) {
//				scoredEvent = parseDesaturationEvent(scoredEventElement);
//			} else if(eventType.equals("HYPOPNEA")) {
//				scoredEvent = parseHypopnea(scoredEventElement);
//			} else if(eventType.equals("LIGHTS-OFF")) {
//				scoredEvent = parseLightsOff(scoredEventElement);
//			} else if(eventType.equals("LIGHTS-ON")) {
//				scoredEvent = parseLightsOn(scoredEventElement);
//			} else {
//				scoredEvent = null; // create default scored event element
//			}
//		} else {						
//			// no mapping event name found
//			result = false;
//			scoredEvent = xmlRoot.createElement("ScoredEvent");
//			Element eventConcept = xmlRoot.createElement("EventConcept");
//			Element startElement = xmlRoot.createElement("Starttime");
//			Element durationElement = xmlRoot.createElement("Duration");
//			Element notesElement = xmlRoot.createElement("Notes");
//				
//			eventConcept.appendChild(xmlRoot.createTextNode("Technician Notes"));
//			notesElement.appendChild(xmlRoot.createTextNode(eventType));
//			
//			String startTime = getElementByChildTag(scoredEventElement, "StartTime");
//			String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
//			String durationTime = getDurationInSeconds(startTime, stopTime);
//			durationElement.appendChild(xmlRoot.createTextNode(durationTime));
//					
//			scoredEvent.appendChild(eventConcept);
//			scoredEvent.appendChild(startElement);
//			scoredEvent.appendChild(durationElement);
//			scoredEvent.appendChild(notesElement);
//			String info = xmlAnnotation + "," + eventType + "," + startTime ;
//			this.log(info);
//		}		
//		if(result) {
////			System.out.println("Has mapping element");
//			// if true silence
//		} else {
//			System.out.println("Does not have mapping element");
//		}
//
//		return scoredEvent;
//	}
	
	/**
	 * Parses event element and returns an parsed element
	 * @param scoredEventElement the event name in String
	 * @return the parsed element
	 */
	public Element parseEmblaXmlEvent(Element scoredEventElement) {
		
		// only DESAT type has more values to be processed, others are the same
		Element scoredEvent = null;
		boolean result = false;
		String eventType = getElementByChildTag(scoredEventElement, "Type");
		// map[1] contains keySet with event name
		if(map[1].keySet().contains(eventType)) {
			scoredEvent = parseEventElement(scoredEventElement);
			result = true;
		} else {						
			// no mapping event name found
			result = false;
			scoredEvent = xmlRoot.createElement("ScoredEvent");
			Element eventConcept = xmlRoot.createElement("EventConcept");
			Element startElement = xmlRoot.createElement("Starttime");
			Element durationElement = xmlRoot.createElement("Duration");
			Element notesElement = xmlRoot.createElement("Notes");
				
			eventConcept.appendChild(xmlRoot.createTextNode("Technician Notes"));
			notesElement.appendChild(xmlRoot.createTextNode(eventType));
			
			String startTime = getElementByChildTag(scoredEventElement, "StartTime");
			String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
			String durationTime = getDurationInSeconds(startTime, stopTime);
			durationElement.appendChild(xmlRoot.createTextNode(durationTime));
					
			scoredEvent.appendChild(eventConcept);
			scoredEvent.appendChild(startElement);
			scoredEvent.appendChild(durationElement);
			scoredEvent.appendChild(notesElement);
			String info = xmlAnnotation + "," + eventType + "," + startTime ;
			this.log(info);
		}		
		if(result) {
//			System.out.println("Has mapping element");
			// if true silence
		} else {
			System.out.println("Does not have mapping element");
		}

		return scoredEvent;
	}

	/**
	 * For later implementation
	 * @param event the txt event to be parsed
	 * @return true if parsing is successful
	 */
	@SuppressWarnings("unused")
	private boolean parseEmblaTxtEvent(String event) {
		return true;
	}

	////////////////////////////////////////////////////
	////// Parses each event of this vendor START //////
	////////////////////////////////////////////////////
	
	private Element parseEventElement(Element scoredEventElement) {
		String eventType = getElementByChildTag(scoredEventElement, "Type");
		Element scoredEvent = null;
		Element eventConcept = null;		
		Element duration = null;
		Element start = null;
		Node nameNode = null;
		if(xmlRoot != null) {
			scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
			eventConcept = xmlRoot.createElement("EventConcept");		
			duration = xmlRoot.createElement("Duration");
			start = xmlRoot.createElement("Start");
			nameNode = xmlRoot.createTextNode(eventType);	
		} else {
			System.out.println("TEST: xmlRoot is null"); // test
		}
		// creates and appends ScoredEvent>EventConcept element			
		eventConcept.appendChild(nameNode);
		scoredEvent.appendChild(eventConcept);
		
		// create ScoredEvent>Duration
		String startTime = getElementByChildTag(scoredEventElement, "StartTime");
		String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
		String durationTime = getDurationInSeconds(startTime, stopTime);
		duration.appendChild(xmlRoot.createTextNode(durationTime));
		scoredEvent.appendChild(duration);
		// create ScoredEvent>Start
		start.appendChild(xmlRoot.createTextNode(startTime));
		scoredEvent.appendChild(start);

		if(eventType.equals("DESAT")) {
			Element spO2Nadir = xmlRoot.createElement("SpO2Nadir");
			Element spO2Baseline = xmlRoot.createElement("SpO2Baseline");
			String desatStartVal = getUserVariableValue(scoredEventElement, "Begin of desat");
			String desatEndVal = getUserVariableValue(scoredEventElement, "End of desat");
//			System.out.println("========"); // test
//			System.out.println(desatStartVal + ": " + desatEndVal); // test
			spO2Nadir.appendChild(xmlRoot.createTextNode(desatStartVal));
			spO2Baseline.appendChild(xmlRoot.createTextNode(desatEndVal));
			scoredEvent.appendChild(spO2Nadir);
			scoredEvent.appendChild(spO2Baseline);
		}
		
		return scoredEvent;
	}
	
//	/**
//	 * Parses APNEA-CENTRAL event
//	 * @param scoredEventElement the scored event element to be tranlated
//	 * @return the translated scored event element
//	 */
//	public Element parseApneaCentral(Element scoredEventElement) {
//		String eventType = "APNEA-CENTRAL";
//		Element scoredEvent = null;
//		Element eventConcept = null;		
//		Element duration = null;
//		Element start = null;
//		Node nameNode = null;
//		if(xmlRoot != null) {
//			scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
//			eventConcept = xmlRoot.createElement("EventConcept");		
//			duration = xmlRoot.createElement("Duration");
//			start = xmlRoot.createElement("Start");
//			nameNode = xmlRoot.createTextNode(eventType);	
//		} else {
//			System.out.println("TEST: xmlRoot is null"); // test
//		}
//		// creates and appends ScoredEvent>EventConcept element			
//		eventConcept.appendChild(nameNode);
//		scoredEvent.appendChild(eventConcept);
//		
//		// create ScoredEvent>Duration
//		String startTime = getElementByChildTag(scoredEventElement, "StartTime");
//		String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
//		String durationTime = getDurationInSeconds(startTime, stopTime);
//		duration.appendChild(xmlRoot.createTextNode(durationTime));
//		scoredEvent.appendChild(duration);
//		// create ScoredEvent>Start
//		start.appendChild(xmlRoot.createTextNode(startTime));
//		scoredEvent.appendChild(start);
//		
//		return scoredEvent;
//	}

//	/**
//	 * Parses DESAT event
//	 * @param scoredEventElement the scored event element to be tranlated
//	 * @return the translated scored event element
//	 */
//	public Element parseDesaturationEvent(Element scoredEventElement) {
//		String eventType = "DESAT";
//		Element spO2Nadir = null;
//		Element spO2Baseline = null;
//		Element scoredEvent = null;
//		Element eventConcept = null;		
//		Element duration = null;
//		Element start = null;
//		Node nameNode = null;
//		if(xmlRoot != null) {
//			scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
//			eventConcept = xmlRoot.createElement("EventConcept");		
//			duration = xmlRoot.createElement("Duration");
//			start = xmlRoot.createElement("Start");
//			nameNode = xmlRoot.createTextNode(eventType);
//			spO2Nadir = xmlRoot.createElement("SpO2Nadir");
//			spO2Baseline = xmlRoot.createElement("SpO2Baseline");
//		} else {
//			System.out.println("TEST: xmlRoot is null"); // test
//		}
//		eventConcept.appendChild(nameNode);
//		scoredEvent.appendChild(eventConcept);
//		
//		// create ScoredEvent>Duration
//		String startTime = getElementByChildTag(scoredEventElement, "StartTime");
//		String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
//		String durationTime = getDurationInSeconds(startTime, stopTime);
//		duration.appendChild(xmlRoot.createTextNode(durationTime));
//		scoredEvent.appendChild(duration);
//		// create ScoredEvent>Start
//		start.appendChild(xmlRoot.createTextNode(startTime));
//		scoredEvent.appendChild(start);
//		
//		// Get the parameter that contains SpO2Baseline and SpO2Nadir
//		
//		NodeList desatParamsList = document.getElementsByTagName("Parameters");
//		Element desatParams = (Element)desatParamsList.item(0);		
//		NodeList desatParamList = desatParams.getElementsByTagName("Parameter");
//		Element userVarElement = null;
//		for(int i = 0; i < desatParamList.getLength(); i++) {
//			Element userValElement = (Element)desatParamList.item(i);
//			NodeList keys = userValElement.getElementsByTagName("Key");
//			Element firstKeyElement = (Element)keys.item(0);
//			String keyvalue = getText(firstKeyElement);
//			if("UserVariables".equals(keyvalue)) {
//				userVarElement = userValElement;
//			}
//		}
//		NodeList values = userVarElement.getElementsByTagName("Value");
//		Element value = (Element)values.item(0);
//		NodeList paramsList = value.getElementsByTagName("Parameters");
//		Element parameters = (Element)paramsList.item(0);
//		NodeList finalParam = parameters.getElementsByTagName("Parameter");
//		
//		String desatStart = "Begin of desat";
//		String desatEnd = "End of desat";
//		String desatStartVal = "";
//		String desatEndVal = "";
//		for(int index = 0; index < finalParam.getLength(); index++) {
//			Element parent = (Element)finalParam.item(index);
//			String keyVal = getElementByChildTag(parent, "Key");
//			if(keyVal.equals(desatStart)) {
//				desatEndVal = getElementByChildTag(parent, "Value");
//			} else if(keyVal.equals(desatEnd)) {
//				desatStartVal = getElementByChildTag(parent, "Value");
//			}
//		}
//		spO2Nadir.appendChild(xmlRoot.createTextNode(desatStartVal));
//		spO2Baseline.appendChild(xmlRoot.createTextNode(desatEndVal));
//		scoredEvent.appendChild(spO2Nadir);
//		scoredEvent.appendChild(spO2Baseline);
//		
//		return scoredEvent;
//	}	
	
	///////////////////////////////////////////////////
	////// Parses each event of this vendor END ///////
	///////////////////////////////////////////////////
	
	/**
	 * Gets the value of the specified key from UserVariable parameter of this event
	 * @param scoredEventElement the scored event
	 * @param paramKey the key corresponding to the value needed
	 * @return the value corresponding to the key
	 */
	public String getUserVariableValue(Element scoredEventElement, String paramKey) {
		NodeList rootParamsList = scoredEventElement.getElementsByTagName("Parameters");
		Element rootParams = (Element)rootParamsList.item(0);		
		NodeList rootParamList = rootParams.getElementsByTagName("Parameter");
		Element userVarElement = null;
		for(int i = 0; i < rootParamList.getLength(); i++) {
			Element userValElement = (Element)rootParamList.item(i);
			NodeList keys = userValElement.getElementsByTagName("Key");
			Element firstKeyElement = (Element)keys.item(0);
			String keyvalue = getText(firstKeyElement);
			if("UserVariables".equals(keyvalue)) {
				userVarElement = userValElement;
			}
		}
		NodeList values = userVarElement.getElementsByTagName("Value");
		Element value = (Element)values.item(0);
		NodeList paramsList = value.getElementsByTagName("Parameters");
		Element parameters = (Element)paramsList.item(0);
		NodeList finalParamList = parameters.getElementsByTagName("Parameter");

		String resultValue = "";
		for(int index = 0; index < finalParamList.getLength(); index++) {
			Element parent = (Element)finalParamList.item(index);
			String keyVal = getElementByChildTag(parent, "Key");
			if(keyVal.equals(paramKey)) {
				resultValue = getElementByChildTag(parent, "Value");
			}
		}
		return resultValue;
	}

	/**
	 * Gets the text content of the <code>childName</code> node from a parent element
	 * @param parent an scored event element
	 * @param childName the child name
	 * @return the text content in the child node
	 */
	public String getElementByChildTag(Element parent, String childName) {		
		NodeList list = parent.getElementsByTagName(childName);
	    if (list.getLength() > 1) {
	      throw new IllegalStateException("Multiple child elements with name " + childName);
	    } else if (list.getLength() == 0) {
	      return null;
	    }
	    Element child = (Element) list.item(0);
	    return getText(child);
	}
	
	/**
	 * Gets the text content of an element
	 * @param element the element to extract from
	 * @return the text content of this element
	 */
	private static String getText(Element element) {
		StringBuffer buf = new StringBuffer();
	    NodeList list = element.getChildNodes();
	    boolean found = false;
	    for (int i = 0; i < list.getLength(); i++) {
	      Node node = list.item(i);
	      if (node.getNodeType() == Node.TEXT_NODE) {
	        buf.append(node.getNodeValue());
	        found = true;
	      }
	    }
	    return found ? buf.toString() : null;
	}
	
	/**
	 * Reads mapping file and saves as an array of HashMap
	 * Can be put in a higher hierarchy
	 * @param mapFile the mapping file name
	 * @return  the mapping in form of HashMap
	 */
	public HashMap<String,Object>[] readMapFile(String mapFile) {
//		System.out.println("Read map file...");  // for test
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
			// System.out.println(errors.toString());
		}
		return map;
	}

	/**
	 * Records the start date from the EDF fiel
	 * Can be put in a higher hierarchy
	 * @param edfFile the EDF file name
	 * @return the start date and duration, first string is start date and the second is duration
	 */
	public String[] recordStartDate(String edfFile) {
		String[] startDate = new String[2];
		@SuppressWarnings("unused")
		SimpleDateFormat df = new SimpleDateFormat("mm.dd.yyyy hh.mm.ss");
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
	
	/**
	 * Gets the string representation of the duration
	 * @param start the start time
	 * @param end the end time
	 * @return duration
	 */
	public String getDurationInSeconds(String start, String end) {
		// SimpleDataFormat did not handle microseconds well, so I wrote the code to handle it
		// by wei wang, 2014-8-9
		String format = "yyyy-MM-dd'T'HH:mm:ss";
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		try {
			Date startDate = sdf.parse(start);
			Date endDate = sdf.parse(end);
			long diff = endDate.getTime() - startDate.getTime(); // in milliseconds
			long duration = diff / 1000;			
			
			String start_suf = start.substring(20);
			String end_suf = end.substring(20);
			int s = Integer.valueOf(start_suf);
			int e = Integer.valueOf(end_suf);
			int res = 0;
			String finalDuration = "";
			if(e < s) {
				res = e + 1000000 - s;
				duration -= 1;				
			} else {
				res = e - s;
			}
			long finalRes = Math.round(res * 1.0 / 100000);
			finalDuration = duration + "." + String.valueOf(finalRes);
			result = String.valueOf(finalDuration);
		} catch (ParseException e) {
			e.printStackTrace();
			log("Cannot parse duration");
		}
		return result;
	}
	
	/**
	 * Parses the Embla xml annotation file and generates the event names
	 * @param emblaXmlFile the Embla annotation file
	 * @return true if the process is successful
	 */
	public boolean recordEvents(Document doc) {
		String eventName;
		ArrayList<String> eventNames = new ArrayList<String>();
		NodeList nodeList = doc.getElementsByTagName("EventType");
		if(nodeList == null) {
			log("Cannot find EventType");
			return false;
		}
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node.hasChildNodes()) {
				Node last = node.getLastChild();
				eventName = last.getNodeValue();
				eventNames.add(eventName);
			}
		}
		events = eventNames;
		return true;
	}
	
	/**
	 * Serializes xml file from a Document
	 * @param xml source xml Document file
	 * @param filename output xml file name
	 */
	@SuppressWarnings("deprecation")
	public void saveXML(Document xml, String filename) {
		try {
			String tarfileDir = filename.substring(0, filename.lastIndexOf(File.separator));
			File f1 = new File(tarfileDir);
			if (!f1.exists()){
				f1.mkdirs();
			}
			
			File f2 = new File(filename);
			FileOutputStream fos = new FileOutputStream(filename, f2.exists());
			// XERCES 1 or 2 additionnal classes.
			OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
			of.setIndent(1);
			of.setIndenting(true);
			//of.setDoctype(null,"users.dtd");
			XMLSerializer serializer = new XMLSerializer(fos, of);
			// As a DOM Serializer
			serializer.asDOMSerializer();
			serializer.serialize( xml.getDocumentElement() );
			//System.out.println(outfile);
			fos.close();
		} catch(IOException e) {
			e.printStackTrace();			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log(errors.toString());
			// System.out.println(errors.toString());
		}
	}
	
	/**
	 * Appends elements of string format to the ScoredEvent element
	 * @param doc the Document to which the elements to be added
	 * @param elements elements to be added to the ScoredEvent element
	 * @return the ScoredEvent element
	 */
	public Element addElements(Document doc, String[] elements) {
		Element eventElement = doc.createElement("ScoredEvent");
		Element nameElement = doc.createElement("EventConcept");
		nameElement.appendChild(doc.createTextNode(elements[0]));
		eventElement.appendChild(nameElement);
		Element startElement = doc.createElement("Start");
		startElement.appendChild(doc.createTextNode(elements[1]));
		eventElement.appendChild(startElement);
		Element durationElement = doc.createElement("Duration");
		durationElement.appendChild(doc.createTextNode(elements[2]));
		eventElement.appendChild(durationElement);
		return eventElement;
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
			// System.out.println(errors.toString());
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
	 * Logs messages
	 * Can be put in a higher hierarchy
	 * @param message the message to be logged
	 */
	public void log(String message) {
		TranslationController.translationErrors += message;
	}

	///////////////////////////////////
	//// Getters and Setters START ////
	///////////////////////////////////

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public ArrayList<String> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<String> events) {
		this.events = events;
	}
	
	//////////////////////////////////
	//// Getters and Setters END /////
	//////////////////////////////////
}
