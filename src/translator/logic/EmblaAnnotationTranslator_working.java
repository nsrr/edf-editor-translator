package translator.logic;

import javax.swing.text.html.parser.Element;

interface AnnotationTranslatorDe {
	public
		boolean read(String edfFile, String annFile, String mappingFile);
		boolean validate();
		String[] listEvents();
		Element translateEvent(String event);
		boolean write(String translatedAnnFile);
}

public class EmblaAnnotationTranslator_working {
//	private
//		Element scoredEvents;
//		File edfFile, annFile;
//
//		validateEDFFile() {
//			// has valid edf header 
//			
//		}
//
//		validateAnnFile() {
//			// is a valid xml file (check header)
//			
//			//if valid file
//			//initialize()
//			
//		}
//		
//		void initialize() {
//			//setup the scoredEvents tree
//			
//		}
//		
//		Element[] parseLocation(Element event) {
//			// elements - start time, end time, duration
//		}
//		
//		Element[] parseUserVariables(Element event) {
//			// example for desat element there are 2 user variables
//		//	if ("desat") {}
//		//	if ("position") {}
//		//	if ("another_event") {}
//		}
//				
//		void buildEvent(Element event) {
//			// get Location
//			// get UserVariables
//			
//			// build the translated event 
//			Element translatedEvent;
//			
//			// write to the element tree
//			writeEvent(translatedEvent);
//			
//		}
//		
//		void writeEvent(Element translatedEvent) {
//			scoredEvents.add(translatedEvent);			
//		}
//		
//			
//	public 
//		read(String edfFile, String annFile, String mappingFile) {
//			// check if the edfFile, annFile, mappingFile Locations are valid
//			
//			// if valid open files for reading 
//			// File edfFile = new 
//			// File annFile = new 
//			// File mappingFile = new 
//			
//		}
//		
//		boolean validate() {
//			// validate EDF
//			// validate XML			
//		}
//				
//		String[] listEvents() {
//			// read list of events from xml file			
//		}
//		
//		Element translateEvent(String event) {
//			// get Location
//			// read xml file and get the element 
//			parseLocation(element)
//
//			// get UserVariables
//			parseUserVariables()
//			
//			// build the translated event 
//			Element translatedEvent;
//			
//			// write to the element tree
//			writeEvent(translatedEvent);
//			
//		}
//		
//		boolean write(String translatedAnnFile) {
//			// check o/p file location
//			// if not validate o/p file location, write contents to default location
//			
//			// write the element tree to o/p location
//			
//			// activity log
//		}
		
}



// calling class
// EmblaAnnotationTranslator e;
// e.read(edf, ann, map);
// e.validate();
// s = e.listEvents(); // "arousal", "hypopnea", "apnea", "arousal", "desat"
// for (i=0; i<s.length; i++)
// {
//		e.translateEvent(s[i]);
// }
// write(output);

