package translator.test;

import translator.logic.CompumedicsTranslatorFactory;

public class TestCompumedicsTranslatorFactory {
	public static String mapping = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/CHAT-mapping-2014-fall.csv";
	public static String annotation = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/100022.xml";
	public static String edf = "/Users/wei/git/edf-editor-translator/resource-wei/Compumedics/100022.EDF";
	public static String output = "/Users/wei/git/edf-editor-translator/output-wei/100022_Compumedics_2014-10-14.xml";	

	public static void main(String[] args) {
		testCompumedicsTranslation();
	}
	
	public static void testCompumedicsTranslation() {
		CompumedicsTranslatorFactory et = new CompumedicsTranslatorFactory();
		et.read(edf, annotation, mapping);
		et.translate();
		et.write2xml(output);
	}
}
