package translator.test;

import translator.logic.CommandLineController;
import translator.utils.Keywords;
import translator.utils.Vendor;

public class TestCommandLineController {

    
    public static void main(String[] args){
    	
    	String vendor = Vendor.Compumedics.toString();
		String mapping_file = "D:/Workspace/EDF_Editor/data/NSRR_eventmapping_compumedics_02032014.csv";
		String edf_dir = "D:/Workspace/EDF_Editor/data/input_files";
		String annotation_dir = "D:/Workspace/EDF_Editor/data/input_files";
		String stage_dir = null;
		String output_dir = "C:/Documents and Settings/Gang/Desktop/NF";
		String outname = Keywords.key_edfname + "_" + Keywords.key_vendor + "_" + Keywords.key_date + "_" + Keywords.key_time + ".xml";
		
		String[] argv = new String[]{
			"-translator",
			"-vendor", vendor,
			"-map", mapping_file,
			"-edf", edf_dir,
			"-ann", annotation_dir,
			"-stage", "",
			"-out", output_dir,
			"-name", outname
		};
		
		CommandLineController.Start(argv);
		System.exit(0);
    }

}
