package translator.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

/**
 * @author wei wang, 2014-8-21
 */
public class CompumedicsAnnotationTranslator extends BasicTranslation implements AnnotationTranslator {
	
	private Document xmlRoot; // = new DocumentImpl(); // xml root
	private Element scoredEvents; // parent element of <Event>
	
	/**
	 * Default constructor
	 */
	public CompumedicsAnnotationTranslator() {
		super();
		softwareVersion = "Compumedics XML";
		xmlRoot = new DocumentImpl(); // xml root
	}

	@Override
	public boolean read(String edfFile, String annotationFile, String mappingFile) {
		System.out.println("Inside CompumedicsAnnotationTranslator read"); // test
		boolean result = false;		
		this.edfFile = edfFile;
		this.xmlAnnotation = annotationFile;
		map = readMapFile(mappingFile);		
		System.out.println("map: " + map);
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

	@Override
	public boolean translate() {
		boolean result = false;
		Element root = createEmptyDocument(softwareVersion);
		
		NodeList nodeList = document.getElementsByTagName("ScoredEvent");
		System.out.println("Event size: " + nodeList.getLength()); // test
		for(int index = 0; index < nodeList.getLength(); index++) {
			Element parsedElement = null;
			Node node = nodeList.item(index);  // for each <event> node
			Element elem = (Element)node;
			parsedElement = parseCompumedicsXmlEvent(elem);
			if(parsedElement == null) {
				log("Can't parse event: " + index + " " + getElementByChildTag(elem, "Type"));
			}
			scoredEvents.appendChild(parsedElement);
		}
		System.out.println("Parse ScoredEvent Success!");  // test
		
		root.appendChild(scoredEvents);
		xmlRoot.appendChild(root);
		result = true;
		System.out.println("DONE!");  // test: should be moved out of this method
		return result;
	}

	private Element parseCompumedicsXmlEvent(Element scoredEventElement) {
		// only DESAT type has more values to be processed, others are the same
		Element scoredEvent = null;
		String eventType = getElementByChildTag(scoredEventElement, "Name");
		System.out.println("map event size: " + map[1].keySet().size()); // test
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
			
			String startTime = getElementByChildTag(scoredEventElement, "Start");
			startElement.appendChild(xmlRoot.createTextNode(startTime));			
			String durationTime = getDuration(scoredEventElement);
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

	private Element parseEventElement(Element scoredEventElement) {
		List<Element> locationList = getLocation(scoredEventElement);
		if(locationList == null) {
			System.out.println("ERROR 1"); // test
		}
		List<Element> userVariableList = getUserVariables(scoredEventElement);
		if(userVariableList == null) {
			System.out.println("ERROR 2"); // test
		}
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

	private List<Element> getUserVariables(Element scoredEventElement) {
		List<Element> list = new ArrayList<Element>();
		String eventType = getElementByChildTag(scoredEventElement, "Name");
		if(eventType.equals("SpO2 desaturation")) {
			Element spO2Nadir = xmlRoot.createElement("SpO2Nadir");
			Element spO2Baseline = xmlRoot.createElement("SpO2Baseline");
			String desatStartVal = getElementByChildTag(scoredEventElement, "LowestSpO2");
			String desat = getElementByChildTag(scoredEventElement, "Desaturation");
			String desatEndVal = String.valueOf(Double.parseDouble(desatStartVal) + Double.parseDouble(desat));
			spO2Nadir.appendChild(xmlRoot.createTextNode(desatStartVal));
			spO2Baseline.appendChild(xmlRoot.createTextNode(desatEndVal));
			list.add(spO2Nadir);
			list.add(spO2Baseline);			
		}
		return list;
	}

	private List<Element> getLocation(Element scoredEventElement) {
		// {eventConcept, duration, start}
		List<Element> list = new ArrayList<Element>();
		String eventType = getElementByChildTag(scoredEventElement, "Name");
		Element eventConcept = xmlRoot.createElement("EventConcept");		
		Element duration = xmlRoot.createElement("Duration");
		Element start = xmlRoot.createElement("Start");
//		Node nameNode = xmlRoot.createTextNode(eventType); // bug-fixed: wei wang, 2014-8-26
		@SuppressWarnings("unchecked")
		Node nameNode = xmlRoot.createTextNode((String)((ArrayList<String>) map[1].get(eventType)).get(1));
		eventConcept.appendChild(nameNode);
		
		String startTime = getElementByChildTag(scoredEventElement, "Start");
		String durationTime = getElementByChildTag(scoredEventElement, "Duration");
		duration.appendChild(xmlRoot.createTextNode(durationTime));
		start.appendChild(xmlRoot.createTextNode(startTime));
			
		list.add(eventConcept);
		list.add(duration);
		list.add(start);
			
		return list;
	}

	private String getDuration(Element scoredEvent) {
		String duration;
		duration = getElementByChildTag(scoredEvent, "Duration");
		if(duration != null) {
			return duration;
		} else {
			log("Duration not found: " + getElementByChildTag(scoredEvent, "Name"));
			return "";
		}		
	}

	@Override
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
	 * Parses the Embla xml annotation file and generates the event names
	 * @param emblaXmlFile the Embla annotation file
	 * @return true if the process is successful
	 */
	private boolean recordEvents(Document doc) {
		String eventName;
		Set<String> eventNames = new HashSet<String>();
		NodeList nodeList = doc.getElementsByTagName("ScoredEvent");
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

	@Override
	public boolean write2JSON(String outputFile) {
		boolean result = false;
//		String resultFinal = ExportFile.xmlStr2jsonStr(ExportFile.xml2string(xmlRoot));
		String resultFinal = ExportFile.xml2json(xmlRoot);
		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
		    out.print(resultFinal);
		    result = true;
		} catch(Exception e) {
			// log result
		}
		return result;
	}

}
