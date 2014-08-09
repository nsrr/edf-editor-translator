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
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

public class EmblaTranslation {
	// algorithm:
	// 1. read mapping file
	// 2. create xml header: PSGAnnotation, SoftwareVersion, Embla, EpochLength...
	// 3. Resolve BOM
	// 4. loop through and process events

	private String format = "xml"; // "txt" or "xml", in lower case
	
	private String xmlAnnotation; // annotation file path
	private String edfFile;  // EDF file path
	private String output; // output file path
	private String mapFile; // map file path
	
	public Document document;	// document is the result of resolving BOM	
	private ArrayList<String> events;
	private HashMap<String,Object>[] map;
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
		this.mapFile = mapFile;
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
		this.mapFile = mapFile;
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
		// 1. Parsing
		//    (a) Creates meta data
		// 	  (b) Creates first ScoredEvent: Recording start time from EDF file
		//    (c) Creates the rest ScoredEvents
		boolean result = true;
		if(!format.equals("xml")) {
			result = false;
			return result;
		}
		
//		xmlRoot = new DocumentImpl();
		Element root = xmlRoot.createElement("PSGAnnotation");
		Element software = xmlRoot.createElement("SoftwareVersion");
		software.appendChild(xmlRoot.createTextNode("Embla xml"));
		Element epoch = xmlRoot.createElement("EpochLength");
		epoch.appendChild(xmlRoot.createTextNode((String) map[0].get("EpochLength")) );
		root.appendChild(software);
		root.appendChild(epoch);															// 1(a) end
		
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
		scoredEvents.appendChild(timeElement);												// 1(b) end
		
		Document doc = document;
		NodeList nodeList = doc.getElementsByTagName("Event");
			
		for(int index = 0; index < nodeList.getLength(); index++) {
			Element parsedElement = null;
			Node node = nodeList.item(index);  // for each <event> node
			Element elem = (Element)node;
			parsedElement = parseEmblaXmlEvent(elem);
			if(parsedElement == null) {
				log("Can't parse event: " + index + " " + getElementByChildTag(elem, "Type"));
			}
			scoredEvents.appendChild(parsedElement);
		}
		
		root.appendChild(scoredEvents);
		xmlRoot.appendChild(root);
		saveXML(xmlRoot, output);
		return result;
	}

	/**
	 * TODO
	 * Parses event element and returns an parsed element
	 * @param scoredEvent the event name in String
	 * @return the parsed element
	 */
	private Element parseEmblaXmlEvent(Element scoredEvent) {
		
		// only DESAT type has more values to be processed, others are the same
		Element elem = null;
		String eventType = getElementByChildTag(scoredEvent, "Type");
		
		// map[1] contains keySet with event name
		if(map[1].keySet().contains(eventType)) {
			
			// creates and appends ScoredEvent>EventConcept element
			elem = xmlRoot.createElementNS(null, "ScoredEvent");
			Element name = xmlRoot.createElement("EventConcept");
			@SuppressWarnings("unchecked")
			String eventName = ((ArrayList<String>)map[1].get(eventType)).get(1);
			Node nameNode = xmlRoot.createTextNode(eventName);
			name.appendChild(nameNode);
			elem.appendChild(name);

			Element subEvent = null;
			// use enum for improvement
			if(eventName.equals("APNEA")) {
				subEvent = parseApnea(scoredEvent);
			} else if(eventName.equals("APNEA-CENTRAL")) {
				subEvent = parseApneaCentral(scoredEvent);
			} else if(eventName.equals("APNEA-MIXED")) {
				subEvent = parseApneaMixed(scoredEvent);
			} else if(eventName.equals("APNEA-OBSTRUCTIVE")) {
				subEvent = parseApneaObstructive(scoredEvent);
			} else if(eventName.equals("DESAT")) {
				subEvent = parseDesaturationEvent(scoredEvent);
			} else if(eventName.equals("HYPOPNEA")) {
				subEvent = parseHypopnea(scoredEvent);
			} else if(eventName.equals("LIGHTS-OFF")) {
				subEvent = parseLightsOff(scoredEvent);
			} else if(eventName.equals("LIGHTS-ON")) {
				subEvent = parseLightsOn(scoredEvent);
			} else {
				subEvent = null;
			}				
			return subEvent;
		} else {
			// no mapping event name found
			Element eventNode = xmlRoot.createElement("ScoredEvent");
			Element nameNode = xmlRoot.createElement("EventConcept");
			Element startNode = xmlRoot.createElement("Starttime");
			Element durationNode = xmlRoot.createElement("Duration");
			Element notesNode = xmlRoot.createElement("Notes");
				
			nameNode.appendChild(xmlRoot.createTextNode("Technician Notes"));
			notesNode.appendChild(xmlRoot.createTextNode(eventType));
			NodeList durationList = scoredEvent.getElementsByTagName("Duration"); // TODO compute duration
			Element lstNmElmnt = (Element) durationList.item(0);
			NodeList lstNm = lstNmElmnt.getChildNodes();

			@SuppressWarnings("unused")
			Element duration = xmlRoot.createElement("Duration");
			Node durationN = xmlRoot.createTextNode((String) ((Node) lstNm.item(0)).getNodeValue());
			durationNode.appendChild(durationN);
			//e.appendChild(duration);
			//System.out.println("\t<Duration>" + ((Node) lstNm.item(0)).getNodeValue() + "</Duration>" );
			NodeList startTimeList = scoredEvent.getElementsByTagName("StartTime");
			Element startElmnt = (Element) startTimeList.item(0);
			NodeList start = startElmnt.getChildNodes();
			double starttime= Double.parseDouble(((Node) start.item(0)).getNodeValue()); // TODO should alter
			//Element startEt = xml.createElement("Start");
			Node startN = xmlRoot.createTextNode(Double.toString(starttime));
			startNode.appendChild(startN);
					
			eventNode.appendChild(nameNode);
			eventNode.appendChild(startNode);
			eventNode.appendChild(durationNode);
			eventNode.appendChild(notesNode);
					
			scoredEvents.appendChild(eventNode);
			String info = xmlAnnotation + "," + eventType + "," + Double.toString(starttime) ;
			this.log(info);
		}
		
		Element eventElement = null;
		System.out.println(">>> inside parseEmblaXmlEvent()");
		boolean result = false;

		if(result) {
			System.out.println("Adds the event element to the tree...");
		}

		return eventElement;
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
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseApnea(Element scoredEventElement) {
		return null;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseApneaCentral(Element scoredEventElement) {
		String eventType = "APNEA-CENTRAL";
		Element scoredEvent = null;
		Element eventConcept = null;		
		Element duration = null;
		Element start = null;
		Node nameNode = null;
		@SuppressWarnings("unchecked")
		String eventName = ((ArrayList<String>)map[1].get(eventType)).get(1); // can be modified
		if(xmlRoot != null) {
			scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
			eventConcept = xmlRoot.createElement("EventConcept");		
			duration = xmlRoot.createElement("Duration");
			start = xmlRoot.createElement("Start");
			nameNode = xmlRoot.createTextNode(eventName);	
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
		
		return scoredEvent;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseApneaMixed(Element scoredEventElement) {
		return null;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseApneaObstructive(Element scoredEventElement) {
		Element apnea_obstrusive = null;
		System.out.println("--- >>> Inside parrseAPNEA_O()");
		return apnea_obstrusive;
	}

	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseDesaturationEvent(Element scoredEventElement) {
		Element desatElement = null;
		System.out.println("--- >>>Inside parseDesaturation()");
		return desatElement;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseHypopnea(Element scoredEventElement) {
		return null;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseLightsOff(Element scoredEventElement) {
		return null;
	}
	
	/**
	 * TODO
	 * @param scoredEventElement the scored event element to be tranlated
	 * @return the translated scored event element
	 */
	public Element parseLightsOn(Element scoredEventElement) {
		return null;
	}
	// more...
	
	///////////////////////////////////////////////////
	////// Parses each event of this vendor END ///////
	///////////////////////////////////////////////////

	/**
	 * Gets the text content of the <code>childName</code> node from a parent element
	 * @param parent an scored event element
	 * @param childName the child name
	 * @return the text content in the child node
	 */
	public String getElementByChildTag(Element parent, String childName) {
//		NodeList list = parent.getElementsByTagName(childName);
//		Element e = (Element) list.item(0);
//		NodeList children = e.getChildNodes();
//		String elementName = ((Node)children.item(0)).getNodeValue(); // first Type child value
//		return elementName;
		
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
	public static String getText(Element element) {
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
		System.out.println("Read map file...");  // for test TODO
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
			long duration = Long.parseLong(String.valueOf(durRec).trim()) * Long.parseLong(String.valueOf(numRec).trim());
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
			// System.out.println(errors.toString());
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
