package viewer;

/**
 * Setting viewer directory class. Responsible for setting up the viewer path
 */
@SuppressWarnings("serial")
public class SettingViewerDir extends SettingParameters {

	private static SettingViewerDir sv = null;

	/**
	 * Create a new JFrame, if not exist,  for setting up viewer's path
	 * @return SettingViewerDir
	 */
	public static SettingViewerDir setViewerDir() {
		if (sv == null) {
			sv = new SettingViewerDir();
		}
		sv.setVisible(true);
		return sv;
	}

	/**
	 * private constructor for this class 
	 * only accessed from {@link #setViewerDir() setViewerDir} method
	 */
	private SettingViewerDir() {
		super("Viewer_Dir");
	}
}