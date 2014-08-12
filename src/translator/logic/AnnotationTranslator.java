package translator.logic;

import org.w3c.dom.Document;

public interface AnnotationTranslator {
	boolean read(String edfFile, String annotationFile, String mappingFile);
	Document translate();
	boolean write(String outputFile);
}
