package validator.logic;

import java.io.File;

import editor.NewTaskListener;

public class ValidityController {
	
	public static void conductValidity(String edf_dir, String output) {

		edf_dir = separatorReplacer(edf_dir);
		output = separatorReplacer(output);
		
		new NewTaskListener(edf_dir, output);
	}
	
	private static String separatorReplacer(String oldString){
		String newString = oldString;
		if (newString!=null){
			newString = newString.replace("/", File.separator);
			newString = newString.replace("\\", File.separator);
			newString = newString.replace(File.separator + File.separator, File.separator);
		}
		return newString;
	}

}
