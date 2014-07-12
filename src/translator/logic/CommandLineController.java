package translator.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import translator.utils.Keywords;

/**
 * TODO
 */
public class CommandLineController {
    
	/**
	 * TODO
	 * @param argv
	 */
	public static void Start(String[] argv) {

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(OptionParser.options, argv);
			if (cmd != null) {
				//(1) Obtain the parameters
				String vendor = cmd.getOptionValue(OptionParser.OptionShort.vendor.toString());
				String mapping_file = cmd.getOptionValue(OptionParser.OptionShort.map.toString());
				String edf_dir = cmd.getOptionValue(OptionParser.OptionShort.edf.toString());
				String annotation_dir = cmd.getOptionValue(OptionParser.OptionShort.ann.toString());
				String stage_dir = cmd.getOptionValue(OptionParser.OptionShort.stage.toString());
				String output_dir = cmd.getOptionValue(OptionParser.OptionShort.out.toString());
				String outname = cmd.getOptionValue(OptionParser.OptionShort.name.toString());
				outname = outname != null ? outname : Keywords.key_edfname + ".xml";
				
				//(2) Find EDF files in the chosen folder and its nested sub-folders
				ArrayList<String> selected_Edf_files = new ArrayList<String>();
				File f = new File(edf_dir);
				if (f.exists()) {
					Collection<File> fileCollection = FileUtils.listFiles(
							f, new String[]{"edf", "Edf", "eDf", "edF", "EDf", "EdF", "eDF", "EDF"}, true);
					if (fileCollection != null)
						for (File file : fileCollection)
							selected_Edf_files.add(file.getAbsolutePath());
				}
				
				//(3) Perform translation
				TranslationController.conductTranslation(vendor, mapping_file, edf_dir, selected_Edf_files, annotation_dir, stage_dir, output_dir, outname);
			}
		} catch (ParseException e) {
			System.out.println("-------------------------------");
			System.out.println("  => EDF Annotation Translator:");
			System.out.println("     Parsing options failed.");
			System.out.println("     Reason: " + e.getMessage());
		}
		
	}
}

/**
 * TODO
 */
@SuppressWarnings("static-access")
class OptionParser {
	public static enum OptionShort{translator, vendor, map, edf, ann, stage, out, name, help}
	public static Options options;
	
	static {
		options = new Options();
		
		options.addOption(OptionBuilder
				.withArgName("Translator").isRequired(true)
				.create(OptionShort.translator.toString())
				);
		
		options.addOption(OptionBuilder
				.withArgName("Vendor").hasArg().isRequired(true)
				.withDescription("Specify a Vendor.")
				.create(OptionShort.vendor.toString())
				);
		
		options.addOption(OptionBuilder
				.withArgName("Mapping file").hasArg().isRequired(true)
				.withDescription("Specify a Mapping file.")
				.create(OptionShort.map.toString()));
		
		options.addOption(OptionBuilder
				.withArgName("Input-directory of EDF files").hasArg().isRequired(true)
				.withDescription("Specify the input-directory of EDF files.")
				.create(OptionShort.edf.toString()));

		options.addOption(OptionBuilder
				.withArgName("Input-directory of Annotation files").hasArg().isRequired(true)
				.withDescription("Specify the input-directory of Annotation files.")
				.create(OptionShort.ann.toString()));
		
		options.addOption(OptionBuilder
				.withArgName("Input-directory of Stage files").hasArg()
				.withDescription("Specify the input-directory of Stage files.")
				.create(OptionShort.stage.toString()));
		
		options.addOption(OptionBuilder
				.withArgName("Output-directory of Translated files").hasArg().isRequired(true)
				.withDescription("Specify the output-directory of Translated files.")
				.create(OptionShort.out.toString()));
		
		options.addOption(OptionBuilder
				.withArgName("Output filename design").hasArg()
				.withDescription("Design the name of translated output files.")
				.create(OptionShort.name.toString()));
	}
}
