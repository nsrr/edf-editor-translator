package translator.logic;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author wei wang, 2014-8-6
 */
public class EmblaTranslationTest {
	
	public static String mapFile = "/Users/wei/git/edf-editor-translator/eventmapping_embla.csv";
	public static String annotation = "/Users/wei/git/edf-editor-translator/10005_01202010.xml" ;
	public static String edf;
	public static String output = "/Users/wei/git/edf-editor-translator/output-test";
	

	public static void main(String[] args) {
		System.out.println("=================================================================");
//		testRecordingEvents();
//		testDuration("2010-01-20T23:01:30.711129", "2010-01-20T23:02:14.711129");
		testApneaCentral();
	}
	
	/**
	 * Tests for recording events in EmblaTranslation
	 */
	public static void testRecordingEvents() {
		EmblaTranslation et1 = new EmblaTranslation(mapFile, annotation, edf, output);
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
		EmblaTranslation et = new EmblaTranslation(mapFile, annotation, edf, output);
		Document doc = et.document;
		NodeList nodeList = doc.getElementsByTagName("Event");
		Node node = null;
		String apneaCentral = "APNEA-CENTRAL";
		for(int index = 0; index < nodeList.getLength(); index++) {
			node = nodeList.item(index);
			Element elem = (Element)node;
			if(apneaCentral.equals(et.getElementByChildTag(elem, "Type"))) {
				result = et.parseApneaCentral(elem);				
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
}
