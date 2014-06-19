package translator.logic;

import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import translator.utils.Keywords;
import translator.utils.MyDate;
import translator.utils.Vendor;

public class TranslationController {

	public static String translationErrors = "";
	public static JList JList_Messages = null;
	public static DefaultListModel ListModel_Messages = null;
	
	public static ArrayList<String> conductTranslation(
			String vendor, String mapping_file, String edf_dir, ArrayList<String> selected_Edf_files, 
			String annotation_dir, String stage_dir, String output_dir, String outname) {

		mapping_file = separatorReplacer(mapping_file);
		edf_dir = separatorReplacer(edf_dir);
		annotation_dir = separatorReplacer(annotation_dir);
		stage_dir = separatorReplacer(stage_dir);
		output_dir = separatorReplacer(output_dir);

		ArrayList<String> successfulOutAL = new ArrayList<String>();
		
		String annotation_file = null, stage_file = null, out_file = null;
		
		for (String edf_file : selected_Edf_files) {
			
			//(1) edf_file
			edf_file = separatorReplacer(edf_file);
			
			//(2) out_file
			String edf_file_prefix = edf_file.substring(0, edf_file.lastIndexOf(File.separator));
			String out_file_prefix = edf_file_prefix.replace(edf_dir, output_dir);
			
			String edf_file_postfix = edf_file.substring(edf_file.lastIndexOf(File.separator) + 1, edf_file.length());
			String basename = edf_file_postfix.replaceAll(".(?i)edf", "");
			String out_file_postfix = customize_out_file(outname, basename, vendor);
			
			out_file = separatorReplacer(out_file_prefix + File.separator + out_file_postfix);
			
			boolean bTranslation = false;
			AnnotationConverter converter = new AnnotationConverter();
			try {
				new File(out_file).getParentFile().mkdirs();
				if (vendor.equals(Vendor.Embla.toString())) {
					annotation_file = validize_file(annotation_dir, basename, ".txt");
					if ((new File(annotation_file)).exists()) {
						bTranslation = converter.convertTXT(annotation_file, mapping_file, out_file);	
					}
				} else if (vendor.equals(Vendor.Compumedics.toString())) {
					annotation_file = validize_file(annotation_dir, basename, ".xml");
					if ((new File(annotation_file)).exists()) {
						bTranslation = converter.convertXML(annotation_file, edf_file, mapping_file, out_file);
					}
				} else if (vendor.equals(Vendor.Respironics.toString())) {
					annotation_file = validize_file(annotation_dir, basename, ".events.csv");
					stage_file = validize_file(stage_dir, basename, ".events.csv");
					if ((new File(annotation_file)).exists() && (new File(stage_file)).exists()) {
						bTranslation = converter.convertCSV(annotation_file, stage_file, edf_file, mapping_file, out_file);
					}
				} else if (vendor.equals(Vendor.Sandman.toString())) {
					annotation_file = validize_file(annotation_dir, basename, ".txt");
					if ((new File(annotation_file)).exists()) {
						bTranslation = converter.convertSandman(annotation_file, edf_file, mapping_file, out_file);
					}
				}
				
				if (bTranslation) {
					successfulOutAL.add(out_file);
				}
			}
			catch(Exception e) {
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
				addElementIntoLog("   * Mapping:\t" + mapping_file + "(" + ((new File(mapping_file)).exists() ? "Existed" : "Not exist!") + ")", false);
				addElementIntoLog("   * EDF file:\t" + edf_file + "(" + ((new File(edf_file)).exists() ? "Existed" : "Not exist!") + ")", true);
				addElementIntoLog("   * Annotation:\t" + annotation_file + "(" + ((new File(annotation_file)).exists() ? "Existed" : "Not exist!") + ")", true);
				if (vendor.equals(Vendor.Respironics.toString()))
				addElementIntoLog("   * Stage file:\t" + stage_file + "(" + ((new File(stage_file)).exists() ? "Existed" : "Not exist!") + ")", false);
				addElementIntoLog("   * Output file:\t" + out_file, true);
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
		addElementIntoLog("   * " + successfulOutAL.size() + " out of " + selected_Edf_files.size() + " EDF files have been successfully translated.", true);
		
		return successfulOutAL;
	}
	
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
	
	private static String separatorReplacer(String oldString) {
		String newString = oldString;
		if (newString!=null) {
			newString = newString.replace("/", File.separator);
			newString = newString.replace("\\", File.separator);
			newString = newString.replace(File.separator + File.separator, File.separator);
		}
		return newString;
	}
	
	public static String customize_out_file(String pattern, String basename, String vendor) {
		
		basename = basename == null ? "filename" : basename;
		
		String example = pattern;
		example = example.replace(Keywords.key_edfname, basename);
		example = vendor==null ? example : example.replace(Keywords.key_vendor, vendor);
		example = example.replace(Keywords.key_date, MyDate.currentDate());
		example = example.replace(Keywords.key_time, MyDate.currentTime());
		return example;
	}
	
	private static String validize_file(String dir, String basename, String extension) {
		
		if (dir == null || basename == null || extension == null)
			return null;
		
		String file = separatorReplacer(dir + File.separator + basename + extension.toUpperCase());
		
		if (!(new File(file)).exists()){
			file = separatorReplacer(dir + File.separator + basename + extension);
//			if (!(new File(file)).exists())
//				file = null;
		}
		
		return file;
	}
	
	public static String updateOutputPattern(String pattern, String key, ItemEvent e) {
		
		final String str_xml = ".xml";
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
