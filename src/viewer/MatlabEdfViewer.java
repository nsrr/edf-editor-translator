package viewer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import org.openide.util.Utilities;

import editor.MainWindow;


public class MatlabEdfViewer {

	public static void main(String[] args){
		callEdfViewer();
	}
	
	public static void callEdfViewer(){
		
		//(1) set Edf and Xml (if any)
		if (MainWindow.masterFile == null){
			JOptionPane.showMessageDialog(null, "No EDF file for EDF-Viewer is selected from the task tree.");
			return;
		}
		else{
			String EdfFilePath = MainWindow.masterFile.getParentFile().getAbsolutePath() + File.separator;
			String EdfFileName = MainWindow.masterFile.getName();
			String XmlFilePath = EdfFilePath;////////////////////
			String XmlFileName = EdfFileName.replaceAll("\\.[eE][dD][fF]$", "_MIMI.xml");
	
			if (!(new File(XmlFilePath + XmlFileName)).exists()){
				XmlFilePath = "";
				XmlFileName = "";
			} 
			
			//(2) set ViewerDir, ViewerApp, and MrcDir 
			ViewerAppEnvir.setting();
			
//			McrDirEnvir.mcrDir = "C:\\Program Files\\MATLAB\\MATLAB Compiler Runtime\\v82\\runtime\\win64";
//			ViewerDirEnvir.viewerDir = "C:\\Users\\gxs213\\git\\edf-editor-translator\\viewers";
			
			//(3) run EdfViewer
			callEdfViewer(EdfFilePath, EdfFileName, XmlFilePath, XmlFileName);
		}

	}
	
	private static void callEdfViewer(String EdfFilePath, String EdfFileName, String XmlFilePath, String XmlFileName){

		String mcrDir = SettingMcrDir.getChosenDirectory();
		String viewerDir = SettingViewerDir.getChosenDirectory();
		String viewerApp = ViewerAppEnvir.viewerApp;
		
		if (mcrDir==null){
			JOptionPane.showMessageDialog(null, "Please first set the diectory of MATLAB Compiler Runtime (MCR)!");
			SettingMcrDir.setMcrDir();
			return;
		}
		if (viewerDir==null){
			JOptionPane.showMessageDialog(null, "Please first set the diectory of MATLAB viewers!");
			SettingViewerDir.setViewerDir();
			return;
		}
		if (viewerApp==null){
			JOptionPane.showMessageDialog(null, "No viewer compiled for this operating system exists.");
			return;
		}
		if (EdfFilePath==null || EdfFileName==null){
			JOptionPane.showMessageDialog(null, "Error occurs in EDF settings.");
			return;
		}
		if (XmlFilePath==null || XmlFileName==null){
			JOptionPane.showMessageDialog(null, "Error occurs in Annotation settings.");
			return;
		}
		
		callEdfViewer(mcrDir, viewerDir, viewerApp, EdfFilePath, EdfFileName, XmlFilePath, XmlFileName);
	}
	
	private static class ViewerAppEnvir{
		static String operatingSystem = null;
		static String viewerApp = null;
		
		@SuppressWarnings("deprecation")
		static void setting(){
			int os = Utilities.getOperatingSystem();
			boolean is64bit = is64bit();
			if (os != Utilities.OS_WINDOWS_MASK){
				if (is64bit){
					viewerApp = "SleepPortalViewerR2013bWin64.exe";
					operatingSystem = "Win_64bit";
				}
				else{
					viewerApp = "SleepPortalViewerR2013bWin32.exe";
					operatingSystem = "Win_32bit";
				}
			}
			else if (os == Utilities.OS_MAC){
				if (is64bit){
					operatingSystem = "Mac_64bit";
				}
				else{
					operatingSystem = "Mac_32bit";
				}
			}
			else if (os == Utilities.OS_LINUX){
				if (is64bit){
					operatingSystem = "Linux_64bit";
				}
				else{
					operatingSystem = "Linux_32bit";
				}
			}
		}
		
		static boolean is64bit(){
			boolean is64bit = false;
			if (System.getProperty("os.name").contains("Windows")) {
			    is64bit = (System.getenv("ProgramFiles(x86)") != null);
			} else {
			    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
			}
			return is64bit;
		}
	}
	
	
	private static void callEdfViewer(String mcrDir, String viewerDir, String viewerApp, String EdfFilePath, String EdfFileName, String XmlFilePath, String XmlFileName){

		/**
		 * (1) Examine existence of paths and files
		 */
		//mcrDir
		if (!(new File(mcrDir)).exists()){
			JOptionPane.showMessageDialog(null, "Diectory of MATLAB Compiler Runtime (MCR) does not exist. Please check it!");
			return;
		}
		//viewerDir
		if (!(new File(viewerDir)).exists()){
			JOptionPane.showMessageDialog(null, "Diectory of ViewerDir does not exist. Please check it!");
			return;
		}
		//viewer
		String viewer = viewerDir + File.separator + viewerApp;
		viewer.replace("\\", File.separator);
		viewer.replace("/", File.separator);
		viewer.replace(File.separator + File.separator, File.separator);
		if (!(new File(viewer)).exists()){
			JOptionPane.showMessageDialog(null, "Matlab Viewer App does not exist. Please check it!");
			return;
		}
		//EdfFilePath
		if (!(new File(EdfFilePath)).exists()){
			JOptionPane.showMessageDialog(null, "EDF file does not exist. Please check it!");
			return;
		}
		//EdfFile
		String EdfFile = EdfFilePath + File.separator + EdfFileName;
		EdfFile.replace("\\", File.separator);
		EdfFile.replace("/", File.separator);
		EdfFile.replace(File.separator + File.separator, File.separator);
		if (!(new File(EdfFile)).exists()){
			JOptionPane.showMessageDialog(null, "EDF file does not exist. Please check it!");
			return;
		}
		//XmlFilePath & XmlFile
		if (!(XmlFilePath.equals("") && XmlFileName.equals(""))){
			//XmlFilePath
			if (!(new File(XmlFilePath)).exists()){
				JOptionPane.showMessageDialog(null, "Annotation file does not exist. Please check it!");
				return;
			}
			//XmlFile
			String XmlFile = XmlFilePath + File.separator + XmlFileName;
			XmlFile.replace("\\", File.separator);
			XmlFile.replace("/", File.separator);
			XmlFile.replace(File.separator + File.separator, File.separator);
			if (!(new File(XmlFile)).exists()){
				JOptionPane.showMessageDialog(null, "Xml file does not exist. Please check it!");
				return;
			}	
		}

		/**
		 * (2) Prepare BATCH commands
		 */
		//mcrDir
		mcrDir = formatDir(mcrDir);
		mcrDir = mcrDir.replace(File.pathSeparator, "\\");
		//viewerDir
		viewerDir = formatDir(viewerDir);
		viewerDir = viewerDir.replace(File.pathSeparator, "\\");		
		//EdfFilePath
		EdfFilePath = formatDir(EdfFilePath);
		EdfFilePath = EdfFilePath + File.separator;
		EdfFilePath = EdfFilePath.replace(File.separator + File.separator, File.separator);
		EdfFilePath = EdfFilePath.replace("/", "\\\\");
		EdfFilePath = EdfFilePath.replace("\\", "\\\\");
		EdfFilePath = "\"" + EdfFilePath + "\"";
		//EdfFileName
		EdfFileName = "\"" + EdfFileName + "\"";
		//XmlFilePath
		XmlFilePath = formatDir(XmlFilePath);
		XmlFilePath = XmlFilePath + File.separator;
		XmlFilePath = XmlFilePath.replace(File.separator + File.separator, File.separator);
		XmlFilePath = XmlFilePath.replace("/", "\\\\");
		XmlFilePath = XmlFilePath.replace("\\", "\\\\");
		XmlFilePath = "\"" + XmlFilePath + "\"";
		//XmlFileName
		XmlFileName = "\"" + XmlFileName + "\"";
		
		/**
		 * (3) Run BATCH commands
		 */
		PrintWriter stdin = null;
		try {
			Process p = Runtime.getRuntime().exec("cmd");
			new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
			new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
			stdin = new PrintWriter(p.getOutputStream());
			stdin.println("set PATH=" + mcrDir);
			stdin.println("chdir " + viewerDir);
			stdin.print(viewerApp + " ");
			stdin.print(EdfFilePath + " ");
			stdin.print(EdfFileName + " ");
			stdin.print(XmlFilePath + " ");
			stdin.print(XmlFileName + " ");
			stdin.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if (stdin!=null){
				stdin.close();
			}
		}
		
		/**
		 * (4) Check BATCH commands
		 */
		System.out.println();
		System.out.println("===========================");
		System.out.println("Invoking Matlab EDF-Viewer");
		System.out.println("MCR Dir: " + mcrDir);
		System.out.println("Viewer Dir: " + viewerDir);
		System.out.println("Viewer App: " + viewerApp);
		System.out.println("EdfFilePath: " + EdfFilePath);
		System.out.println("EdfFileName: " + EdfFileName);
		System.out.println("XmlFilePath: " + XmlFilePath);
		System.out.println("XmlFileName: " + XmlFileName);
		System.out.println("Operating System: " + ViewerAppEnvir.operatingSystem);
	}
	
	private static class SyncPipe implements Runnable {
		public SyncPipe(InputStream istrm, OutputStream ostrm) {
			istrm_ = istrm;
			ostrm_ = ostrm;
		}

		public void run() {
			try {
				final byte[] buffer = new byte[1024];
				for (int length = 0; (length = istrm_.read(buffer)) != -1;) {
					ostrm_.write(buffer, 0, length);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private final OutputStream ostrm_;
		private final InputStream istrm_;
	}
	
	
	private static String formatDir(String path){
		String str = path;
		str = str.replace("\\", File.separator);
		str = str.replace("/", File.separator);
		str = str.replace(File.separator + File.separator, File.separator);
		return str;
	}
}


