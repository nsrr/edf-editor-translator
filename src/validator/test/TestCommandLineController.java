package validator.test;

import validator.logic.CommandLineController;

public class TestCommandLineController {

	public static void main(String[] args) {

//		String edf_dir = "D:/Workspace/EDF_Editor/data/input_files";
//		String output = "C:/Documents and Settings/Gang/Desktop/NF/1.txt";
		
		String edf_dir = "D:/Workspace2/test";
		String output = "D:/Workspace2/test/1.txt";

		String[] argv = new String[] {
			"-validator",
			"-edf", edf_dir,
			"-out", output
		};
		
		CommandLineController.Start(argv);
		System.exit(0);
	}
}