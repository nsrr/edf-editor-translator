package translator.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import translator.logic.ExportFile;

/**
 * Uses for testing BasicTranslation
 * @author wei wang, 2014-8-21
 */
public class EmblaAnnotationTranslator extends BasicTranslation implements AnnotationTranslator {

	private Document xmlRoot; // = new DocumentImpl(); // xml root
	private Element scoredEvents; // parent element of <Event>
	
	/**
	 * Default constructor
	 */
	public EmblaAnnotationTranslator() {
		super();
		softwareVersion = "EmblaStub XML";
		xmlRoot = new DocumentImpl(); // xml root
	}
	
	public boolean read(String edfFile, String annotationFile, String mappingFile) {
		System.out.println("Inside EmblaStub read");
		boolean result = false;		
		this.edfFile = edfFile;
		this.xmlAnnotation = annotationFile;
		map = readMapFile(mappingFile);		
		document = resolveBOM(xmlAnnotation);
		// test
		if(document == null) {
			System.out.println("document is null");
		} else {
			System.out.println("document is not null");
		}
		result = recordEvents(document);		
		if(!result) {
			log("Cannot parse the events in the annotation file");
		}		
		return result;
	}

	/**
	 * Translates Embla annotation file using the mapping file and the corresponding EDF file
	 * @return true if successful
	 */
	public boolean translate() {
		boolean result = false;
		Element root = createEmptyDocument(softwareVersion);
		
		NodeList nodeList = document.getElementsByTagName("Event");
		System.out.println("Event size: " + nodeList.getLength()); // test
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
		result = true;
		System.out.println("DONE!");  // test: should be moved out of this method
		return result;
	}
	
	/**
	 * Serializes XML to output file
	 * @param output the xml output file
	 * @return true if the process succeed
	 */
	public boolean write(String outputFile) {
		output = outputFile;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            DOMSource source = new DOMSource(xmlRoot);
            StreamResult file = new StreamResult(new File(output));
            transformer.transform(source, file);
            // System.out.println("\nXML DOM Created Successfully..");
            log("XML DOM Created Successfully..");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	/**
	 * Parses event element and returns an parsed element
	 * @param scoredEventElement the event name in String
	 * @return the parsed element
	 */
	private Element parseEmblaXmlEvent(Element scoredEventElement) {
		
		// only DESAT type has more values to be processed, others are the same
		Element scoredEvent = null;
		String eventType = getElementByChildTag(scoredEventElement, "Type");
		// map[1] contains keySet with event name
		if(map[1].keySet().contains(eventType)) {
			scoredEvent = parseEventElement(scoredEventElement);
		} else {						
			// no mapping event name found
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
			log(info);
		}		

		return scoredEvent;
	}

	////////////////////////////////////////////////////
	////// Private utility methods start          //////
	////////////////////////////////////////////////////
	
	private Element createEmptyDocument(String softwareVersion) {
		Element root = xmlRoot.createElement("PSGAnnotation");
		Element software = xmlRoot.createElement("SoftwareVersion");
		software.appendChild(xmlRoot.createTextNode(softwareVersion));
		Element epoch = xmlRoot.createElement("EpochLength");
		epoch.appendChild(xmlRoot.createTextNode((String) map[0].get("EpochLength")) );
		root.appendChild(software);
		root.appendChild(epoch);
		
		scoredEvents = xmlRoot.createElement("ScoredEvents");
		recordStartDate(edfFile); // 
		String[] elmts = new String[3];//
		elmts[0] = "Recording Start Time";//
		elmts[1] = "0";//
		elmts[2] = timeStart[1];//
		Element timeElement = addElements(xmlRoot, elmts);
		Element clock = xmlRoot.createElement("ClockTime");
		clock.appendChild(xmlRoot.createTextNode(timeStart[0]));
		timeElement.appendChild(clock);
		scoredEvents.appendChild(timeElement);		
		return root;
	}
	
	private List<Element> getLocation(Element scoredEventElement) {
		// {eventConcept, duration, start}
		List<Element> list = new ArrayList<Element>();
		String eventType = getElementByChildTag(scoredEventElement, "Type");
		Element eventConcept = xmlRoot.createElement("EventConcept");		
		Element duration = xmlRoot.createElement("Duration");
		Element start = xmlRoot.createElement("Start");
		Node nameNode = xmlRoot.createTextNode(eventType);
		eventConcept.appendChild(nameNode);
		String startTime = getElementByChildTag(scoredEventElement, "StartTime");
		String relativeStart = getEventStartTime(timeStart[0], startTime);
		String stopTime = getElementByChildTag(scoredEventElement, "StopTime");
		String durationTime = getDurationInSeconds(startTime, stopTime);
		start.appendChild(xmlRoot.createTextNode(relativeStart));
		duration.appendChild(xmlRoot.createTextNode(durationTime));
		
		list.add(eventConcept);
		list.add(duration);
		list.add(start);
		
		return list;
	}
	
	private List<Element> getUserVariables(Element scoredEventElement) {
		List<Element> list = new ArrayList<Element>();
		String eventType = getElementByChildTag(scoredEventElement, "Type");
		if(eventType.equals("DESAT")) {
			Element spO2Nadir = xmlRoot.createElement("SpO2Nadir");
			Element spO2Baseline = xmlRoot.createElement("SpO2Baseline");
			String desatStartVal = getUserVariableValue(scoredEventElement, "Begin of desat");
			String desatEndVal = getUserVariableValue(scoredEventElement, "End of desat");
			spO2Nadir.appendChild(xmlRoot.createTextNode(desatEndVal));
			spO2Baseline.appendChild(xmlRoot.createTextNode(desatStartVal));
			list.add(spO2Nadir);
			list.add(spO2Baseline);			
		}
		return list;
	}
	
	private Element parseEventElement(Element scoredEventElement) {
		List<Element> locationList = getLocation(scoredEventElement);
		List<Element> userVariableList = getUserVariables(scoredEventElement);
		Element scoredEvent = null;
		if(xmlRoot != null) {
			scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
		} else {
			System.out.println("TEST: xmlRoot is null"); // test
			return null;
		}
		for(Element element : locationList)
			scoredEvent.appendChild(element);
		for(Element element : userVariableList)
			scoredEvent.appendChild(element);
		return scoredEvent;
	}
	
	/**
	 * Gets the value of the specified key from UserVariable parameter of this event
	 * @param scoredEventElement the scored event
	 * @param paramKey the key corresponding to the value needed
	 * @return the value corresponding to the key
	 */
	private String getUserVariableValue(Element scoredEventElement, String paramKey) {
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
	private String getElementByChildTag(Element parent, String childName) {		
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
	 * Gets the start time of an event, relative to the EDF recording time
	 * @param edfClockStart the start time recorded in the EDF file
	 * @param eventStart the start time of an event
	 * @return start time of format "xxxxx.x"
	 */
	public String getEventStartTime(String edfClockStart, String eventStart) {
		String format1 = "dd.mm.yy hh.mm.ss";
		String format2 = "yyyy-MM-dd'T'HH:mm:ss";
		String time;
		SimpleDateFormat sdf1 = new SimpleDateFormat(format1, Locale.US);
		SimpleDateFormat sdf2 = new SimpleDateFormat(format2, Locale.US);
		try {
			Date date1 = sdf1.parse(edfClockStart);
			Date date2 = sdf2.parse(eventStart);
//			System.out.println("Date 1: " + date1 + "\nDate 2 : " + date2); // test
			long diff = date2.getTime() - date1.getTime();  // milliseconds
			long timeStart = diff / 1000;
			
			String start_suf = eventStart.substring(20, 21);
			time = String.valueOf(timeStart);
			time += "." + start_suf;
			return time;
		} catch (ParseException e) {
			e.printStackTrace();
			log("Cannot parse duration");
			return "";
		}		
	}
	
	/**
	 * Gets the string representation of the duration
	 * @param start the start time
	 * @param end the end time
	 * @return duration
	 */
	private String getDurationInSeconds(String start, String end) {
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
	private boolean recordEvents(Document doc) {
		String eventName;
		Set<String> eventNames = new HashSet<String>();
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
	 * Appends elements of string format to the ScoredEvent element
	 * @param doc the Document to which the elements to be added
	 * @param elements elements to be added to the ScoredEvent element
	 * @return the ScoredEvent element
	 */
	private Element addElements(Document doc, String[] elements) {
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

	///////////////////////////////////
	//// Private utility methods end //
	//// Getters and Setters START   //
	///////////////////////////////////
	
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getXmlAnnotation() {
		return xmlAnnotation;
	}

	public void setXmlAnnotation(String xmlAnnotation) {
		this.xmlAnnotation = xmlAnnotation;
	}

	public String getEdfFile() {
		return edfFile;
	}

	public void setEdfFile(String edfFile) {
		this.edfFile = edfFile;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public HashMap<String, Object>[] getMap() {
		return map;
	}

	public void setMap(HashMap<String, Object>[] map) {
		this.map = map;
	}

	public Document getXmlRoot() {
		return xmlRoot;
	}

	public Element getScoredEvents() {
		return scoredEvents;
	}

	public String[] getTimeStart() {
		return timeStart;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Set<String> getEvents() {
		return events;
	}

	public void setEvents(Set<String> events) {
		this.events = events;
	}

	@Override
	public boolean write2JSON(String outputFile) {
		boolean result = false;
		String resultFinal = ExportFile.xml2json(xmlRoot);
		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
		    out.print(resultFinal);
		    result = true;
		} catch(Exception e) {
			// log result
		}
		return result;
	}
	
	//////////////////////////////////
	//// Getters and Setters END /////
	//////////////////////////////////

}
