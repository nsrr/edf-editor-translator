package translator.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
 * A translator factory that translates ARES annotation file and generate
 * specified output
 * 
 * @author wei wang, 2014-8-21
 */
public class ARESTranslatorFactory extends AbstractTranslatorFactory {

  private Document xmlRoot; // = new DocumentImpl(); // xml root
  private Element scoredEvents; // parent element of <Event>

  public ARESTranslatorFactory() {
    super();
    System.out
        .println("=================================================================");
    System.out.println("Ares Factory:"); // test
    softwareVersion = "ARES";
    xmlRoot = new DocumentImpl(); // xml root
  }

  @Override
  public boolean read(String edfFile, String annotationFile, String mappingFile) {
    boolean result = false;
    this.edfFile = edfFile;
    this.xmlAnnotation = annotationFile;
    map = readMapFile(mappingFile);
    initLocalVariables(edfFile); // initialize local variables
    document = resolveBOM(xmlAnnotation);
    result = recordEvents(document);
    if (!result) {
      log("Cannot parse the events in the annotation file");
    }
    return result;
  }

  @Override
  public boolean translate() {
    System.out.println("Signal Label in EDF: ");
    for (String signal : signalLabels) {
      System.out.print(" \"" + signal + "\"");
    }
    System.out.println();
    boolean result = false;
    Element root = createEmptyDocument(softwareVersion);

    NodeList nodeList = document.getElementsByTagName("stripe");
    for (int index = 0; index < nodeList.getLength(); index++) {
      Element parsedElement = null;
      Node node = nodeList.item(index); // for each <event> node
      Element elem = (Element) node;
      parsedElement = parseARESxmlEvent(elem);
      if (parsedElement == null) {
        log("Can't parse event: " + index + " "
            + getElementByChildTag(elem, "Type"));
      }
      scoredEvents.appendChild(parsedElement);
    }
    // Parse staging:
    // for (Element elem : parseStaging())
    // scoredEvents.appendChild(elem);

    root.appendChild(scoredEvents);
    xmlRoot.appendChild(root);
    result = true;
    return result;
  }

  private Element parseARESxmlEvent(Element scoredEventElement) {
    // only DESAT type has more values to be processed, others are the same
    Element scoredEvent = null;
    // String eventType = getElementByChildTag(scoredEventElement, "Name");
    String code = getElementByChildTag(scoredEventElement, "code");
    String attribute = getElementByChildTag(scoredEventElement, "attribute");
    String subattribute = getElementByChildTag(scoredEventElement,
        "subattribute");
    if(attribute == null || attribute == "" || attribute.length() == 0){
      attribute = "99";
    }
    if(subattribute == null || subattribute == "" || subattribute.length() == 0){
      subattribute = "99";
    }

    String eventKey = code + attribute + subattribute;
    // map[1] contains keySet with event name
    if (attribute != null && map[1].keySet().contains(eventKey)) {
      scoredEvent = parseEventElement(scoredEventElement);
    } else {
      // no mapping event name found
      scoredEvent = xmlRoot.createElement("ScoredEvent");
      Element eventConcept = xmlRoot.createElement("EventConcept");
      Element eventCategory = xmlRoot.createElement("EventType");
      Element startElement = xmlRoot.createElement("Start");
      Element durationElement = xmlRoot.createElement("Duration");
      Element notesElement = xmlRoot.createElement("Notes");

      eventConcept.appendChild(xmlRoot.createTextNode("Technician Notes|Technician Notes"));
      notesElement.appendChild(xmlRoot.createTextNode(code));
      eventCategory.appendChild(xmlRoot.createTextNode("Technician Notes|Technician Notes"));

      String startTime = "0";
      String durationTime = "0";
      startElement.appendChild(xmlRoot.createTextNode(startTime));
      durationElement.appendChild(xmlRoot.createTextNode(durationTime));

      scoredEvent.appendChild(eventCategory);
      scoredEvent.appendChild(eventConcept);
      scoredEvent.appendChild(startElement);
      scoredEvent.appendChild(durationElement);
      scoredEvent.appendChild(notesElement);
      
      String info = xmlAnnotation + "," + code + "," + startTime;
//      log(info);
      addElementIntoLog(">>> Missing event code: " + code, true);
    }

    return scoredEvent;
  }

  private Element parseEventElement(Element scoredEventElement) {
    List<Element> locationList = getLocation(scoredEventElement);
    if (locationList == null) {
      log("ERROR: location error");
    }
    Element scoredEvent = null;
    if (xmlRoot != null) {
      scoredEvent = xmlRoot.createElementNS(null, "ScoredEvent");
    } else {
      log("ERROR: root element is null");
      return null;
    }
    for (Element element : locationList)
      scoredEvent.appendChild(element);
    return scoredEvent;
  }

  /**
   * TODO: Check the start/stop time format: seconds/milliseconds?
   * @param scoredEventElement
   * @return
   */
  private List<Element> getLocation(Element scoredEventElement) {
    // {eventConcept, duration, start}
    String code = getElementByChildTag(scoredEventElement, "code");
    String attribute = getElementByChildTag(scoredEventElement, "attribute");
    String subattribute = getElementByChildTag(scoredEventElement,
        "subattribute");
    if(attribute == null || attribute == "" || attribute.length() == 0){
      attribute = "99";
    }
    if(subattribute == null || subattribute == "" || subattribute.length() == 0){
      subattribute = "99";
    }
    String eventKey = code + attribute + subattribute;

//    System.out.println("KEY: " + eventKey + "map contains key? " + map[1].containsKey(eventKey));
    List<Element> list = new ArrayList<Element>();
    @SuppressWarnings({ "unused", "unchecked" })
    String eventType = ((ArrayList<String>) map[1].get(eventKey)).get(0);
    String annLocation = (String) map[4].get(eventKey);
    Element eventCategory = xmlRoot.createElement("EventType");
    Element eventConcept = xmlRoot.createElement("EventConcept");
    Element duration = xmlRoot.createElement("Duration");
    Element start = xmlRoot.createElement("Start");
    Element input = xmlRoot.createElement("SignalLocation");
    @SuppressWarnings("unchecked")
    String cddname = (String) ((ArrayList<String>) map[1]
        .get(eventKey)).get(1);
    @SuppressWarnings("unchecked")
    String aresname = (String) ((ArrayList<String>) map[1].get(eventKey)).get(0);
    Node nameNode = xmlRoot.createTextNode(cddname + "|" + aresname);
    String categoryStr = map[3].get(eventKey) == null ? "" : (String) map[3]
        .get(eventKey);
    Node categoryNode = xmlRoot.createTextNode(categoryStr + "|" + categoryStr);

    String signalLocation = getSignalLocationFromEvent(scoredEventElement,
        annLocation);
    Node inputNode = xmlRoot.createTextNode(signalLocation);
    eventCategory.appendChild(categoryNode);
    eventConcept.appendChild(nameNode);
    input.appendChild(inputNode);

    String startTime = getElementByChildTag(scoredEventElement, "starttime");
//    String stopTime = getElementByChildTag(scoredEventElement, "stoptime"); //TODO
//    String durationTime = String.valueOf(Integer.valueOf(startTime)
//        - Integer.valueOf(stopTime));
    duration.appendChild(xmlRoot.createTextNode(getDuration(scoredEventElement)));
    start.appendChild(xmlRoot.createTextNode(startTime));

    list.add(eventCategory);
    list.add(eventConcept);
    list.add(start);
    list.add(duration);
    list.add(input);

    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * translator.logic.AbstractTranslatorFactory#getSignalLocationFromEvent(org
   * .w3c.dom.Element, java.lang.String)
   */
  
  public String getSignalLocationFromEvent(Element scoredEvent,
      String annLocation) {
    String attributeStr = getElementByChildTag(scoredEvent, "attribute");
    String subattributeStr = getElementByChildTag(scoredEvent, "subattribute");
    
    if (attributeStr == null || subattributeStr == null) {
      return "";
    }
    int code = Integer.valueOf(getElementByChildTag(scoredEvent, "code"));
    int attribute = Integer.valueOf(attributeStr);
    int subattribute = Integer.valueOf(subattributeStr);
//    String eventKey = code + attribute + subattribute;
    AresItemKey aresKey = new AresItemKey(code, attribute, subattribute);

    String result = signalLabels[0]; // initialize to the first EDF signal

    String defaultSignal = (String) map[4].get(aresKey.toString());
    List<String> edfSignals = Arrays.asList(signalLabels);

    if (edfSignals.contains(defaultSignal)) {
      result = annLocation;
    }

    return result;
  }

  private String getDuration(Element scoredEvent) {
    String startTime = getElementByChildTag(scoredEvent, "starttime");
    String stopTime = getElementByChildTag(scoredEvent, "stoptime");
    String durationTime = "Invalid Duration";
    try{
    	int duration = Integer.valueOf(stopTime) - Integer.valueOf(startTime);
    	duration /= 1000; // Assume duration is in millisecond format
    	if (duration > 0) {
    	    durationTime = String.valueOf(duration);
    	}
    }catch(Exception e){
    	durationTime = null;
    }
    
//    System.out.print("Start Time: " + startTime + ", Stop Time:" + stopTime + " ");
//    System.out.println("Duration: " + durationTime);

    if (durationTime != null) {
      return durationTime;
    } else {
      log("Duration not found: " + getElementByChildTag(scoredEvent, "Name"));
      return "";
    }
  }

  @Override
  public boolean write2xml(String outputFile) {
    System.out.println("   >>> Inside CompumedicsAnnotationTranslator write"); // test
    output = outputFile;
    try {
      Transformer transformer = TransformerFactory.newInstance()
          .newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(xmlRoot);
      StreamResult file = new StreamResult(new File(output));
      transformer.transform(source, file);
      log("XML DOM Created Successfully..");
      System.out.println("   [Write done]");
      System.out
          .println("=================================================================");
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
    String epochLength = (String) map[0].get("EpochLength");
    if (epochLength != null) {
      epoch.appendChild(xmlRoot.createTextNode(epochLength));
    } else {
      epoch.appendChild(xmlRoot.createTextNode(""));
    }
    root.appendChild(software);
    root.appendChild(epoch);

    scoredEvents = xmlRoot.createElement("ScoredEvents");
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
   * Parses the Embla xml annotation file and generates the event names
   * 
   * @param emblaXmlFile
   *          the Embla annotation file
   * @return true if the process is successful
   */
  private boolean recordEvents(Document doc) {
    String eventName;
    Set<String> eventNames = new HashSet<String>();
    NodeList nodeList = doc.getElementsByTagName("ScoredEvent");
    if (nodeList == null) {
      log("Cannot find EventType");
      return false;
    }
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.hasChildNodes()) {
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
   * 
   * @param doc
   *          the Document to which the elements to be added
   * @param elements
   *          elements to be added to the ScoredEvent element
   * @return the ScoredEvent element
   */
  private Element addElements(Document doc, String[] elements) {
    Element eventElement = doc.createElement("ScoredEvent");
    Element nameElement = doc.createElement("EventConcept");
    Element typeElement = doc.createElement("EventType");
    typeElement.appendChild(doc.createTextNode(""));
    nameElement.appendChild(doc.createTextNode(elements[0]));
    eventElement.appendChild(typeElement);
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
    String resultFinal = FormatedWriter.xml2json(xmlRoot);
    try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
      out.print(resultFinal);
      result = true;
    } catch (Exception e) {
      // log result
    }
    return result;
  }

  /**
   * Reads mapping file and saves as an array of HashMap Can be put in a higher
   * hierarchy.
   * 
   * @param mapFile
   *          the mapping file name
   * @return the mapping in form of HashMap
   */
  public HashMap<String, Object>[] readMapFile(String mapFile) {
    @SuppressWarnings("unchecked")
    // [Item:
    HashMap<String, Object>[] map = (HashMap<String, Object>[]) Array
        .newInstance(HashMap.class, 5);
    try {
      BufferedReader input = new BufferedReader(new FileReader(mapFile));
      try {
        String line = input.readLine();
        HashMap<String, Object> epoch = new HashMap<String, Object>();
        HashMap<String, Object> events = new HashMap<String, Object>();
        HashMap<String, Object> stages = new HashMap<String, Object>();
        HashMap<String, Object> categories = new HashMap<String, Object>();
        HashMap<String, Object> signalLocation = new HashMap<String, Object>(); // /

        while ((line = input.readLine()) != null) {
          String[] data = line.split(",");
          if (data == null || data[0] == "" || data[0].length() == 0 || data[0] == null) {
            break;
          }

          String eventTypeLowerCase = data[0].toLowerCase();
          String eventCategory = data[0];
          String eventName = data[1].trim();
          String defaultSignal = "";

          // construct are item key
          int code = Integer.valueOf(data[5]);;
          
          int attribute;
          try{
          	attribute = Integer.valueOf(data[6]);
          }catch(NumberFormatException e){
          	attribute = 99;  
          }
          
          int subattribute;
          try{
        	  subattribute = Integer.valueOf(data[7]);
          }catch(NumberFormatException e){
        	  subattribute = 99;
          }
          AresItemKey aresKey = new AresItemKey(code, attribute, subattribute);

          // process events
          if (!eventTypeLowerCase.contains("epochlength")
              && !eventTypeLowerCase.contains("stages")) {

            // Process signal column in mapping file
            if (data.length >= 5) {
              defaultSignal = data[4].trim();
            }

            // values = {aresName: String, cddName: String, note: String}
            ArrayList<String> values = new ArrayList<String>(3);
            values.add(data[3]);
            values.add(eventName);
            if (data.length > 8) {
              values.add(data[8]); // add Notes
            }
            // events {event, event_type && event_concept}
            events.put(aresKey.toString(), values);
            categories.put(aresKey.toString(), eventCategory);
            signalLocation.put(aresKey.toString(), defaultSignal);
          }
          // Dated process for epoch
          else if (data[0].compareTo("EpochLength") == 0) {
            // System.out.println(data[0]);
            epoch.put(data[0], data[2]);
          } else {
            // stages {event, event_concept}
            stages.put(aresKey.toString(), eventName);
            categories.put(aresKey.toString(), eventCategory);
          }
        }
        // System.out.println(map[2].values().size());
        map[0] = epoch;
        map[1] = events;
        map[2] = stages;
        map[3] = categories;
        map[4] = signalLocation;
      } finally {
        input.close();
      }
    } 
    catch (IOException e) {
      e.printStackTrace();
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      log(errors.toString());
    }
    return map;
  }
}

class AresItemKey {

  private int code;
  private int attribute;
  private int subattribute;

  public AresItemKey(int code, int attribute, int subattribute) {
    this.code = code;
    this.attribute = attribute;
    this.subattribute = subattribute;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    String result = String.valueOf(code);
    result += String.valueOf(attribute);
    result += String.valueOf(subattribute);
    return Integer.valueOf(result);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    try {
      AresItemKey anotherItem = (AresItemKey) obj;
      if (anotherItem.hashCode() == this.hashCode()) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return String.valueOf(this.hashCode());
  }

  public void setCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setAttribute(int attribute) {
    this.attribute = attribute;
  }

  public int getAttribute() {
    return attribute;
  }

  public void setSubattribute(int subattribute) {
    this.subattribute = subattribute;
  }

  public int getSubattribute() {
    return subattribute;
  }
}