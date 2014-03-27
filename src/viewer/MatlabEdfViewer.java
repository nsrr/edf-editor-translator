package viewer;

import java.io.IOException;

import org.openide.util.Utilities;

public class MatlabEdfViewer {
	
	public static void main(String[] args){
		callEdfViewer();
	}
	
	public static void callEdfViewer(){

		switch (Utilities.getOperatingSystem()) {
			case Utilities.OS_LINUX:
				break;
			case Utilities.OS_MAC:
				break;
			case Utilities.OS_WIN95:
			case Utilities.OS_WIN98:
			case Utilities.OS_WIN2000:
			case Utilities.OS_WIN_OTHER:
			case Utilities.OS_WINNT:
			case Utilities.OS_WINVISTA:
				if (is64bit()) {
					callEdfViewer_MCR_R2013a_Win64();
				}
				break;
		}
		
	}
	
	private static void callEdfViewer_MCR_R2013a_Win64(){
		
    	try {
    		String loc = getViewerDirectory() + "SleepPortalViewer_R2013a(8.1)_Win64.exe";
			Runtime rt = Runtime.getRuntime();
			rt.exec(loc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static String getViewerDirectory(){
		return System.getProperty("user.dir") + "/viewers/";
	}
	
	private static boolean is64bit(){
		boolean is64bit = false;
		if (System.getProperty("os.name").contains("Windows")) {
		    is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
		    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}
		return is64bit;
	}

}
