package viewer;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/**
 * Setting MCR directory class. Responsible for setting up the MCR(MATLAB Compiler Runtime) path
 */
@SuppressWarnings("serial")
public class SettingMcrDir extends SettingParameters {

	private static SettingMcrDir sm = null;

	/**
	 * Create a new JFrame, if not exist,  for setting up MCR path
	 * @return SettingMcrDir
	 */
	public static SettingMcrDir setMcrDir() {
		if (sm == null) {
			sm = new SettingMcrDir();
		}
		sm.setVisible(true);
		return sm;
	}

	/**
	 * Setting up MCR path if this is the first time
	 */
	private SettingMcrDir() {
		super("MCR_Dir");
		if (bfirst) {
			bfirst = false;
			String mcr_dir = getSystemMcrDir();
			if (mcr_dir != null) {
				chosenDirectory.setText(mcr_dir);
				chosenDirectory.setForeground(Color.BLUE);
				chosenDirectory.setFont(new Font("Serif", Font.BOLD, 14));
			}
		}
	}

	private static boolean bfirst = true;

	/**
	 * Search and return the MCR path from the system environment
	 * @return the MCR path if exist, null other wise
	 */
	public static String getSystemMcrDir() {
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()){
			if (envName.equalsIgnoreCase("path")) {
//				System.out.println(env.get(envName));
				String[] vars = env.get(envName).split(";");
				for (String var : vars) {
					String regex = "^.*[mM][aA][tT][lL][aA][bB].*$";
					if (var.matches(regex)) {
						if (var.endsWith("\\"))
							var = var.substring(0, var.length() - 1);
						return var;
					}
				}
				break;
			}
		}
		return null;
	}
}