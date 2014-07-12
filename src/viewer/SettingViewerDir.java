package viewer;

@SuppressWarnings("serial")
public class SettingViewerDir extends SettingParameters {

	private static SettingViewerDir sv = null;

	/**
	 * TODO
	 * @return
	 */
	public static SettingViewerDir setViewerDir() {
		if (sv == null) {
			sv = new SettingViewerDir();
		}
		sv.setVisible(true);
		return sv;
	}

	/**
	 * TODO
	 */
	private SettingViewerDir() {
		super("Viewer_Dir");
	}
}