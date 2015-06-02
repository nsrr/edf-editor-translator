package translator.test;

import translator.logic.AbstractTranslatorFactory;
import translator.logic.CompumedicsTranslatorFactory;
import translator.logic.EmblaTranslatorFactory;
import translator.logic.NewVendorTranslatorFactory;

/**
 * Used for testing
 * @author wei wang, 2014-10-16
 */
public class TranslationClinet {
	
	public static String cmapping = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/CHAT-mapping-2014-fall.csv";
	public static String cannotation = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/100022.xml";
	public static String edf = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/100022.EDF";
	public static String coutput = "/Users/wei/git/edf-editor-translator/output-wei/Compumedics01.xml";
	
	public static String emapping = "/Users/wei/git/edf-editor-translator/resource-wei/EmblaNew/eventmapping_embla_test.csv";
	public static String eannotation = "/Users/wei/git/edf-editor-translator/resource-wei/EmblaNew/10007_01262010s1.xml" ;
	public static String eoutput = "/Users/wei/git/edf-editor-translator/output-wei/Embla01.xml";
	
	public static void main(String[] args) {		
		run();
	}
	
	public static void run() {
		AbstractTranslatorFactory ctranslator = getVendor("Compumedics");
		ctranslator.read(edf, cannotation, cmapping);
		ctranslator.translate();
		ctranslator.write2xml(coutput);
		
		System.out.println();
		
		AbstractTranslatorFactory etranslator = getVendor("Embla");
		etranslator.read(edf, cannotation, cmapping);
		etranslator.translate();
		etranslator.write2xml(coutput);
		
		System.out.println();
		
		AbstractTranslatorFactory ntranslator = getVendor("NewVendor");
		ntranslator.read(edf, cannotation, cmapping);
		ntranslator.translate();
		ntranslator.write2xml(coutput);
	}
	
	public static AbstractTranslatorFactory getVendor(String vendor) {
		if(vendor.toLowerCase().equals("embla")) {
			return new EmblaTranslatorFactory();
		} else if(vendor.toLowerCase().equals("compumedics")) {
			return new CompumedicsTranslatorFactory();
		} else if(vendor.toLowerCase().equals("newvendor")) {
			return new NewVendorTranslatorFactory();
		} else {
			return null;
		}
	}
}
