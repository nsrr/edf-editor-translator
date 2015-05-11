package translator.logic;

import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import translator.utils.Keywords;
import translator.utils.MyDate;
import translator.utils.Vendor;

/**
 * A controller that manages the annotation translation process
 */
public class AnnotationTranslatorClient {

	public static String translationErrors = "";
	public static JList<String> JList_Messages = null; // wei wang, change JList to generic JList<String>
	// wei wang, change DefaultListModel to DefaultListModel<String>
	public static DefaultListModel<String> ListModel_Messages = null;  
	
	public AbstractTranslatorFactory getVendor(String vendor) {
		// TODO
		return null;
	}
	
	/**
	 * Conducts translation from different vendor and log the result
	 * @param vendor vender name
	 * @param mapping_file vender-specific mapping file 
	 * @param edf_dir directory of containing edf file
	 * @param selected_Edf_files array of selected edf files
	 * @param annotation_dir directory containing the annotation
	 * @param stage_dir directory containing the stage file
	 * @param output_dir output directory
	 * @param outname output name before translation
	 * @return array of translated files
	 */
	public static ArrayList<String> conductTranslation(
			String vendor, String mapping_file, String edf_dir, ArrayList<String> selected_Edf_files, 
			String annotation_dir, String stage_dir, String output_dir, String outname) {

		mapping_file = separatorReplacer(mapping_file);
		edf_dir = separatorReplacer(edf_dir);
		annotation_dir = separatorReplacer(annotation_dir);
		stage_dir = separatorReplacer(stage_dir);
		output_dir = separatorReplacer(output_dir);

		ArrayList<String> successfulOutAL = new ArrayList<String>();		
		String annotation_file = null, stage_file = null, out_file_name = null;
		
		for (String edf_file : selected_Edf_files) {
			
			//(1) edf_file
			edf_file = separatorReplacer(edf_file);
			
			//(2) out_file
			String edf_file_prefix = edf_file.substring(0, edf_file.lastIndexOf(File.separator));
			String out_file_prefix = edf_file_prefix.replace(edf_dir, output_dir);
			
			String edf_file_postfix = edf_file.substring(edf_file.lastIndexOf(File.separator) + 1, edf_file.length());
			String basename = edf_file_postfix.replaceAll(".(?i)edf", "");
			String out_file_postfix = customize_out_file(outname, basename, vendor);
			
			out_file_name = separatorReplacer(out_file_prefix + File.separator + out_file_postfix);
			
			boolean bTranslation = false;
			AllVendorAnnotationTranslator converter = new AllVendorAnnotationTranslator();
			try {
				new File(out_file_name).getParentFile().mkdirs();
				if (vendor.equals(Vendor.Embla.toString())) {					
//					annotation_file = validate_file(annotation_dir, basename, ".txt"); // original version
					annotation_file = validate_file(annotation_dir, basename, ".xml");
					if ((new File(annotation_file)).exists()) {
						AbstractTranslatorFactory translator = new EmblaTranslatorFactory(); // 1.
						translator.read(edf_file, annotation_file, mapping_file); // 2.
						bTranslation = translator.translate(); // 3.
						translator.write2xml(out_file_name); // 4.
//						String jsonOut = separatorReplacer(out_file_prefix + File.separator + out_file_postfix + ".json"); 
//						translator.write2JSON(jsonOut); // can output as json file
					}
				} else if (vendor.equals(Vendor.Compumedics.toString())) {
					annotation_file = validate_file(annotation_dir, basename, ".xml");
					if ((new File(annotation_file)).exists()) {
						// original
//						bTranslation = converter.convertXML(annotation_file, edf_file, mapping_file, out_file_name); 
						// next four lines created by wei wang, 2014-8-21
						AbstractTranslatorFactory translator = new CompumedicsTranslatorFactory();
						// next three lines can be moved out of if statement
						translator.read(edf_file, annotation_file, mapping_file);
						bTranslation = translator.translate();
						translator.write2xml(out_file_name);
						// test: output json worked
//						String jsonOut = separatorReplacer(out_file_prefix + File.separator + out_file_postfix + ".json"); 						
//						translator.write2JSON(jsonOut);
					}
				} else if (vendor.equals(Vendor.Respironics.toString())) {
					annotation_file = validate_file(annotation_dir, basename, ".events.csv");
					stage_file = validate_file(stage_dir, basename, ".events.csv");
					if ((new File(annotation_file)).exists() && (new File(stage_file)).exists()) {
						bTranslation = converter.convertCSV(
								annotation_file, stage_file, edf_file, mapping_file, out_file_name);
					}
				} else if (vendor.equals(Vendor.Sandman.toString())) {
					annotation_file = validate_file(annotation_dir, basename, ".txt");
					if ((new File(annotation_file)).exists()) {
						bTranslation = converter.convertSandman(annotation_file, edf_file, mapping_file, out_file_name);
					}					
				}				

				if (bTranslation) {
					successfulOutAL.add(out_file_name);
				}
			} catch(Exception e) {
				bTranslation = false;
				e.printStackTrace();
			} finally {
				//(1) check existence of necessary files
				mapping_file = mapping_file == null ? "" : mapping_file;
				edf_file = edf_file == null ? "" : edf_file;
				annotation_file = annotation_file == null ? "" : annotation_file;
				stage_file = stage_file == null ? "" : stage_file;
				
				//(2) record transaction event into log history
				addElementIntoLog(" .....................", true);
				addElementIntoLog("  => Translating.. at " + MyDate.currentDateTime(), true);
				addElementIntoLog("   * Vendor:\t" + vendor, false);
				addElementIntoLog("   * Mapping:\t" + mapping_file + 
						"(" + ((new File(mapping_file)).exists() ? "Existed" : "Not exist!") + ")", false);
				addElementIntoLog("   * EDF file:\t" + edf_file + 
						"(" + ((new File(edf_file)).exists() ? "Existed" : "Not exist!") + ")", true);
				addElementIntoLog("   * Annotation:\t" + annotation_file + 
						"(" + ((new File(annotation_file)).exists() ? "Existed" : "Not exist!") + ")", true);
				if(vendor.equals(Vendor.Respironics.toString()))
				addElementIntoLog("   * Stage file:\t" + stage_file + "(" + 
				((new File(stage_file)).exists() ? "Existed" : "Not exist!") + ")", false);
				addElementIntoLog("   * Output file:\t" + out_file_name, true);
				addElementIntoLog("   * Translation:\t" + (bTranslation ? "Successful!" : "Failed!"), true);
				
				//(3) record transaction error into log history
				if (!translationErrors.equals("")) {
					addElementIntoLog("   * Errors:", true);
					addElementIntoLog(translationErrors, true);
				}
				translationErrors = "";
			}			
		}
		
		addElementIntoLog(" .....................", true);
		addElementIntoLog("  => The task finished!", true);
		addElementIntoLog("   * " + successfulOutAL.size() + " out of " + selected_Edf_files.size() + 
				" EDF files have been successfully translated.", true);
		
		return successfulOutAL;
	}
	
	/**
	 * Adds message into log file
	 * @param message the message to be logged
	 * @param showOnScreen if true, also show message on the console
	 */
	public static void addElementIntoLog(String message, boolean showOnScreen) {

		if (showOnScreen) {
			System.out.println(message);
			if (ListModel_Messages!=null)
				ListModel_Messages.addElement(message);
			if (JList_Messages!=null)
				JList_Messages.ensureIndexIsVisible(ListModel_Messages.getSize()-1);
		}
		
		BufferedWriter out = null;
		try {
			String filename = Keywords.translator_log;
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename, (new File(filename)).exists())));
			out.write(message + "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Formalizes the string with standard file separator
	 * @param oldString the string to be standardized
	 * @return the standardized string
	 */
	private static String separatorReplacer(String oldString) {
		String newString = oldString;
		if (newString != null) {
			newString = newString.replace("/", File.separator);
			newString = newString.replace("\\", File.separator);
			newString = newString.replace(File.separator + File.separator, File.separator);
		}
		return newString;
	}
	
	/**
	 * Customizes output file name using the specified base name and vendor name
	 * @param pattern output file pattern 
	 * @param basename base file name
	 * @param vendor vendor name
	 * @return the customized output file name
	 */
	public static String customize_out_file(String pattern, String basename, String vendor) {

		basename = basename == null ? "filename" : basename;

		String example = pattern;
		example = example.replace(Keywords.key_edfname, basename);
		example = vendor==null ? example : example.replace(Keywords.key_vendor, vendor);
		example = example.replace(Keywords.key_date, MyDate.currentDate());
		example = example.replace(Keywords.key_time, MyDate.currentTime());
		return example;
	}

	/**
	 * Validates the full path of a file consists of dir, basename and extension
	 * @param dir directory containing the file	
	 * @param basename the base name of the file(without extension)
	 * @param extension the extension of the file
	 * @return the full path of the validated file
	 */
	// wei wang: change 'validize_file' to 'validate_file'
	private static String validate_file(String dir, String basename, String extension) {

		if (dir == null || basename == null || extension == null)
			return null;

		String file = separatorReplacer(dir + File.separator + basename + extension.toUpperCase());

		if (!(new File(file)).exists()) {
			file = separatorReplacer(dir + File.separator + basename + extension);
//			if (!(new File(file)).exists())
//				file = null;
		}		
		return file;
	}

	/**
	 * Updates patterns corresponding to CheckBox event
	 * @param pattern the pattern to be updated 
	 * @param key section of the new pattern related to different event
	 * @param e the event generated by selecting or deselecting CheckBox items
	 * @return the updated pattern
	 */
	public static String updateOutputPattern(String pattern, String key, ItemEvent e) {

		final String str_xml = ".xml";
		// turn on case insensitive matching by (?i), wei wang
		pattern = pattern.replaceAll(".(?i)xml", str_xml);
		pattern = pattern.endsWith(str_xml) ? pattern : pattern + str_xml;
		pattern = pattern.substring(0, pattern.lastIndexOf(str_xml));
		pattern = pattern.contains(Keywords.key_edfname) 
				? pattern 
				: Keywords.key_edfname + pattern;

		if (e.getStateChange() == ItemEvent.SELECTED && !pattern.contains(key))
			pattern = pattern + "_" + key + str_xml;
		else if (e.getStateChange() == ItemEvent.DESELECTED && pattern.contains(key))
			pattern = pattern.replace(key, "") + str_xml;

		pattern = pattern.replace("]__", "]_");
		pattern = pattern.replace("__[", "_[");
		pattern = pattern.replace("][", "]_[");
		pattern = pattern.replace("]_.", "].");
		
		return pattern;
	}
}
