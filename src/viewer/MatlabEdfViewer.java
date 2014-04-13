package viewer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.openide.util.Utilities;

import editor.MainWindow;


public class MatlabEdfViewer {
	
	public static void main(String[] args){
		callEdfViewer();
	}
	
	public static void callEdfViewer(){
		
		if (ViewerLocations.viewer_dir == null){
			JOptionPane.showMessageDialog(null, "Please first set where Matlab EDF-Viewers locate at.");
			SetViewerLocation.main();
			return;
		}
		
		if (MainWindow.masterFile == null){
			JOptionPane.showMessageDialog(null, "No EDF file for EDF-Viewer is selected from the task tree.");
			return;
		}
		
		if (MainWindow.masterFile != null){
			String EdfFilePath = MainWindow.masterFile.getParentFile().getAbsolutePath() + File.separator;
			String EdfFileName = MainWindow.masterFile.getName();
			String XmlFilePath = EdfFilePath;////////////////////
			String XmlFileName = EdfFileName.replaceAll("\\.[eE][dD][fF]$", "_MIMI.xml");
//			EdfFilePath = "D:\\ABC\\";
//			EdfFileName = "ABC_012345.edf";
//			XmlFilePath = "D:\\ABC\\";
//			XmlFileName = "ABC_012345_MIMI.xml";
//			callEdfViewer(EdfFilePath, EdfFileName, XmlFilePath, XmlFileName);
			
			if (!(new File(XmlFilePath + XmlFileName)).exists()){
				XmlFilePath = "";
				XmlFileName = "";
			} 
			
			callEdfViewer(EdfFilePath, EdfFileName, XmlFilePath, XmlFileName);
		}

	}
	
	@SuppressWarnings("deprecation")
	public static void callEdfViewer(String EdfFilePath, String EdfFileName, String XmlFilePath, String XmlFileName){
		
		/*
		 * Format parameters
		 */
		EdfFilePath = formatPath(EdfFilePath);
		EdfFileName = formatPath(EdfFileName);
		XmlFilePath = formatPath(XmlFilePath);
		XmlFileName = formatPath(XmlFileName);
		
		EdfFilePath = EdfFilePath.replace(File.separator, "\\");
		EdfFileName = EdfFileName.replace(File.separator, "\\");
		XmlFilePath = XmlFilePath.replace(File.separator, "\\");
		XmlFileName = XmlFileName.replace(File.separator, "\\");
		
		/*
		 * Determine the location of viewer according to 
		 * 		local operating system and 32/64-bit versions.
		 */
		int os = Utilities.getOperatingSystem();
		boolean is64bit = is64bit();
		String operatingSystem = null;
		
		String loc_viewer = null;
		
		if (os != Utilities.OS_WINDOWS_MASK){
			operatingSystem = "Win_" + (is64bit ? "64bit" : "32bit");
			if (is64bit)
				loc_viewer = ViewerLocations.EdfViewer_MCR_R2013a_Win64();
			else
				loc_viewer = ViewerLocations.EdfViewer_MCR_R2013b_Win32();
		}
		else if (os == Utilities.OS_MAC){
			operatingSystem = "Mac_" + (is64bit ? "64bit" : "32bit");
		}
		else if (os == Utilities.OS_LINUX){
			operatingSystem = "Linux_" + (is64bit ? "64bit" : "32bit");
		}
		
		/*
		 * Invoke the EdfViewer if there exists its corresponding compiled viewer.
		 */
		boolean bExist = false;
		boolean bRun = false;
		if (loc_viewer != null){
			File viewer = new File(loc_viewer);
			if (viewer.exists()){
				bExist = true;
				String[] cmd;
				try {
					if (os != Utilities.OS_WINDOWS_MASK){
//						cmd = new String[] {
//							"cmd/c",
//							"set PATH=C:\\Program Files\\MATLAB\\R2013b\\runtime\\win32",
//							"\"" + loc_viewer + "\" \"" + EdfFilePath + "\" \"" + EdfFileName + "\" \"" + XmlFilePath + "\" \"" + XmlFileName + "\""
//							};
						cmd = new String[] {
								"\"" + loc_viewer.replace(".exe", ".bat") + "\""
							};
					}
					else{
						cmd = new String[] {
							loc_viewer,
							"\"" + EdfFilePath + "\"",
							"\"" + EdfFileName + "\"",
							"\"" + XmlFilePath + "\"",
							"\"" + XmlFileName + "\""
							};
					}
					
					System.out.println(Arrays.toString(cmd));
					Runtime rt = Runtime.getRuntime();
		    		rt.exec(cmd);
					bRun = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("===========================");
		System.out.println("Invoke Matlab EDF-Viewer fr");
		System.out.println("EdfFilePath: " + EdfFilePath);
		System.out.println("EdfFileName: " + EdfFileName);
		System.out.println("XmlFilePath: " + XmlFilePath);
		System.out.println("XmlFileName: " + XmlFileName);
		System.out.println("");
		System.out.println("Operating-System: " + operatingSystem);
		System.out.println("Edf-viewer: " + loc_viewer);
		System.out.println("Exist? " + bExist);
		System.out.println("Run? " + bRun);
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
	
	public static String formatPath(String path){
		String str = path;
		str = str.replace("\\", File.separator);
		str = str.replace("/", File.separator);
		str = str.replace(File.separator + File.separator, File.separator);
		return str;
	}

}

class ViewerLocations{
	
	public static String viewer_dir = null;
	
	private static String getViewerDirectory(){
		return MatlabEdfViewer.formatPath(viewer_dir);
	}
	
	public static String EdfViewer_MCR_R2013a_Win64(){
		return getViewerDirectory() + "SleepPortalViewer_R2013a(8.1)_Win64.exe";
	}
	
	public static String EdfViewer_MCR_R2013b_Win32(){
		return getViewerDirectory() + "SleepPortalViewer_R2013b(8.2)_Win32.exe";
	}
	
	public static String EdfViewer_MCR_R2013b_Mac64(){
		return getViewerDirectory() + "SleepPortalViewer_R2013b(8.2)_Mac64.exe";
	}
}


