package translator.test;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import translator.logic.EmblaAnnotationTranslator;
import translator.logic.EmblaAnnotationTranslator;
import translator.logic.EmblaTranslation;

/**
 * To test the functionality of the EmblaTranslation class
 * @author wei wang, 2014-8-6
 */
public class TestEmblaAnnotationTranslation {
	
	public static String mappingFile = "/Users/wei/git/edf-editor-translator/resource-wei/eventmapping_embla_test.csv";
	public static String annotation = "/Users/wei/git/edf-editor-translator/resource-wei/10005_01202010.xml" ;
	public static String edf = "/Users/wei/git/edf-editor-translator/resource-wei/100022.EDF";
	public static String output = "/Users/wei/git/edf-editor-translator/output-wei/100022_Embla_2014-08-10-4.xml";	

	public static void main(String[] args) {
		System.out.println("=================================================================");
//		testRecordingEvents();
//		testDuration("2010-01-20T23:01:30.711129", "2010-01-20T23:02:14.711129");
//		testApneaCentral();
//		testEmblaTranslation();
//		testReadingMap();
//		testUserVariable("Begin of desat");
//		testUserVariable("End of desat");
		test();
	}
	
	public static void test() {
		EmblaAnnotationTranslator et = new EmblaAnnotationTranslator();
		et.read(edf, annotation, mappingFile);
		et.translate();
		et.write(output);
	}
	
	public static void testEmblaTranslation() {
		EmblaTranslation et = new EmblaTranslation(mappingFile, annotation, edf, output);
		et.translate();
	}
	
	/**
	 * Tests for recording events in EmblaTranslation
	 */
	public static void testRecordingEvents() {
		EmblaTranslation et1 = new EmblaTranslation(mappingFile, annotation, edf, output);
		ArrayList<String> events = et1.getEvents();
		System.out.println("Event size: " + events.size());
		for(int i = 0; i < events.size(); i++) {
			if(i % 5 == 0 && i != 0) {
				System.out.println();
			}
			System.out.print(events.get(i) + "; ");
		}
		System.out.println();
		System.out.println("=================================================================");
	}
	
	public static void testDuration(String start, String end) {
		EmblaTranslation et2 = new EmblaTranslation();
		String duration = et2.getDurationInSeconds(start, end);
		System.out.println("Duration in seconds: " + duration);
		System.out.println("=================================================================");
		System.out.println();
	}
	
	public static void testApneaCentral() {
		Element result = null;
		EmblaTranslation et = new EmblaTranslation(mappingFile, annotation, edf, output);
		Document doc = et.document;
		NodeList nodeList = doc.getElementsByTagName("Event");
		Node node = null;
		String apneaCentral = "APNEA-CENTRAL";
		System.out.println("APNEA-CENTRAL:");
		for(int index = 0; index < nodeList.getLength(); index++) {
			node = nodeList.item(index);
			Element elem = (Element)node;
			if(apneaCentral.equals(et.getElementByChildTag(elem, "Type"))) {
				result = et.parseEmblaXmlEvent(elem);				
			}
		}
		if(result != null) {
			String start = et.getElementByChildTag(result, "Start");
			String duration = et.getElementByChildTag(result, "Duration");
			System.out.println("Start: " + start + "\nDuration: " + duration);
		} else {
			System.out.println("Test failed");
		}
		System.out.println("=================================================================");
	}
	
	public static void testReadingMap() {
		HashMap<String,Object>[] testMap = null;
		HashMap<String,Object> events = null;
		EmblaTranslation et = new EmblaTranslation(mappingFile, annotation, edf, output);
		testMap = et.map;
		events = testMap[1];
		for(String key : events.keySet()) {
			System.out.print(key + "; ");
		}
	}
	
	public static void testUserVariable(String key) {
		EmblaTranslation et = new EmblaTranslation(mappingFile, annotation, edf, output);
		Document doc = et.document;
		NodeList nodeList = doc.getElementsByTagName("Event");
		Node node = null;
		String desat = "DESAT";
		Element resultElem = null;
		System.out.println("DESAT:");		
		for(int index = 0; index < nodeList.getLength(); index++) {
			node = nodeList.item(index);
			Element elem = (Element)node;
			String type = et.getElementByChildTag(elem, "Type");			
			if(desat.equals(type)) {
//				System.out.println("================================================");							
				resultElem = et.parseEmblaXmlEvent(elem);				
				if(resultElem != null) {
					String finalValue = et.getUserVariableValue(elem, key);
					System.out.println("Key = " + key + ": Value = " + finalValue);
				} else {
					System.out.println("Test failed");
				}
//				break;
			}
		}
	}
}
