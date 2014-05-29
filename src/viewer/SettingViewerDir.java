package viewer;

public class SettingViewerDir extends SettingParameters{

	private static final long serialVersionUID = 1L;
	private static SettingViewerDir sv = null;

	public static SettingViewerDir setViewerDir(){
		if (sv == null){
			sv = new SettingViewerDir();
		}
		sv.setVisible(true);
		return sv;
	}

	private SettingViewerDir() {
		super("Viewer_Dir");
	}
}