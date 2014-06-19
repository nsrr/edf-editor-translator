package editor.test;

import editor.Main;
import translator.utils.Keywords;
import translator.utils.Vendor;

public class TestMain {

	public static void main(String[] args) throws Exception {
		
		testMainGUI();
		testTranslator();
		testValidator();
	}
	
	private static void testMainGUI() throws Exception {
		
		Main.main(new String[]{});
	}
	
	private static void testValidator() throws Exception {
		String edf_dir = "D:/Workspace/EDF_Editor/data/input_files";
		String output = "C:/Documents and Settings/Gang/Desktop/NF/1.txt";

		String[] argv = new String[]{
			"-validator",
			"-edf", edf_dir,
			"-out", output
		};
		
		Main.main(argv);
	}
	
	private static void testTranslator() throws Exception {
		String vendor = Vendor.Compumedics.toString();
		String mapping_file = "D:/Workspace/EDF_Editor/data/NSRR_eventmapping_compumedics_02032014.csv";
		String edf_dir = "D:/Workspace/EDF_Editor/data/input_files";
		String annotation_dir = "D:/Workspace/EDF_Editor/data/input_files";
		String stage_dir = "";
		String output_dir = "C:/Documents and Settings/Gang/Desktop/NF";
		String outname = Keywords.key_edfname + "_" + Keywords.key_vendor + "_" + Keywords.key_date + "_" + Keywords.key_time + ".xml";
		
		String[] argv = new String[] {
			"-translator",
			"-vendor", vendor,
			"-map", mapping_file,
			"-edf", edf_dir,
			"-ann", annotation_dir,
			"-stage", stage_dir,
			"-out", output_dir,
			"-name", outname
		};
		
		Main.main(argv);
	}
}
