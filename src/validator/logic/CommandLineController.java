package validator.logic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * TODO
 */
public class CommandLineController {

	public static void Start(String[] argv) {
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(OptionParser.options, argv);
			if (cmd != null) {
				//(1) Obtain the parameters
				String edf_dir = cmd.getOptionValue(OptionParser.OptionShort.edf.toString());
				String output  = cmd.getOptionValue(OptionParser.OptionShort.out.toString());
				
				//(2) Validate on EDF files
				ValidityController.conductValidity(edf_dir, output);
			}
		} catch (ParseException e) {
			System.out.println("-------------------------------");
			System.out.println("  => EDF validity:");
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
	public static enum OptionShort{validator, edf, out}
	public static Options options;
	static {
		options = new Options();
		
		options.addOption(OptionBuilder
				.withArgName("Validator").isRequired(true)
				.create(OptionShort.validator.toString())
				);
		
		options.addOption(OptionBuilder
				.withArgName("Input-directory of EDF files").hasArg().isRequired(true)
				.withDescription("Specify the input-directory of EDF files.")
				.create(OptionShort.edf.toString()));
		
		options.addOption(OptionBuilder
				.withArgName("Filename of EDF validity summary").hasArg().isRequired(true)
				.withDescription("Specify the filename of EDF validity summary.")
				.create(OptionShort.out.toString()));
	}
}
