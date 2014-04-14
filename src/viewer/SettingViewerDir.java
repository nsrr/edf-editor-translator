package viewer;

public class SettingViewerDir extends SettingParameters{

	private static final long serialVersionUID = 1L;
	private static SettingViewerDir sv = null;
	
	public static SettingViewerDir setViewerDir(){
		
		if (sv==null)
			sv = new SettingViewerDir();
		
		sv.setVisible(true);
		
		return sv;
	}

	public static String getChosenDirectory(){
		
		if (sv!=null && sv.chosenDirectory!=null){
			String text = sv.chosenDirectory.getText();
			if (text.equals(""))
				return null;
			else
				return text;
		}
		else{
			return null;
		}
	}
	
	private SettingViewerDir() {
		super("Setting Directory of EDF Viewer");
	}
}
