package validator.logic;

import java.io.File;

import editor.NewTask_for_ValidityCommandLine;

public class ValidityController {
	
	/**
	 * Conducts validation using EDF file directory and the output directory
	 * @param edf_dir the EDF file directory
	 * @param output the output directory 
	 */
	public static void conductValidity(String edf_dir, String output) {

		edf_dir = separatorReplacer(edf_dir);
		output = separatorReplacer(output);
		
		new NewTask_for_ValidityCommandLine(edf_dir, output);
	}
	
	/**
	 * Formalizes the file separator for the string 
	 * @param oldString the string to be formalized
	 * @return the formalized string
	 */
	private static String separatorReplacer(String oldString) {
		String newString = oldString;
		if (newString!=null) {
			newString = newString.replace("/", File.separator);
			newString = newString.replace("\\", File.separator);
			newString = newString.replace(File.separator + File.separator, File.separator);
		}
		return newString;
	}
}
