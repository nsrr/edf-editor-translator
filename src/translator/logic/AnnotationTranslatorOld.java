package translator.logic;


/**
 * This interface is used by each of the vendors who will be implementing the translation process
 * @deprecated
 * @author wei wang
 */
public interface AnnotationTranslatorOld {
	/**
	 * Reads the path of the EDF file, the annotation file and the mapping file
	 * @param edfFile the path of the EDF file
	 * @param annotationFile the path of the annotation file
	 * @param mappingFile the path of the mapping file
	 * @return true if this process is successful
	 */
	boolean read(String edfFile, String annotationFile, String mappingFile);
	
	/**
	 * Does the translation process
	 * @return true if successful
	 */
	boolean translate();
	
	/**
	 * Writes the result to the path indicated by the argument
	 * @param outputFile the output path
	 * @return true if this process is done successful
	 */
	boolean write(String outputFile);
	
	/**
	 * Writes the result to JSON file
	 * @param outputFile the output file path
	 * @return true if this process is done successful
	 */
	boolean write2JSON(String outputFile);
}
