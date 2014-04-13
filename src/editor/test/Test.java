package editor.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class Test {
	
	public static void main(String[]args) throws IOException{
//		main1(new String[]{""});
//		main2(new String[]{""});
//		main3(new String[]{""});
		main4(new String[]{""});
	}
	
	public static void main2(String[]args) throws IOException{
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(new String[]{"C:\\Documents and Settings\\Gang\\git\\edf-editor-translator\\viewers\\SleepPortalViewer_R2013b(8.2)_Win32.bat"});
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

			// kick them off
			errorGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void main3(String[]args) throws IOException{
		final String dosCommand = "cmd /c dir /s";
		final String location = "C:\\WINDOWS";
		try {
			final Process process = Runtime.getRuntime().exec(
					dosCommand + " " + location);
			final InputStream in = process.getInputStream();
			int ch;
			while ((ch = in.read()) != -1) {
				System.out.print((char) ch);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main4(String[]args) throws IOException{
		try {
//			String cmd = "\"C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/SleepPortalViewer_R2013b(8.2)_Win32.bat\"";
			String[] cmd = new String[]{
					"\"C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/SleepPortalViewer_R2013b(8.2)_Win32.bat\""
			};
			Process proc = Runtime.getRuntime().exec(cmd);
			
//			String[] cmd = new String[]{
//				"\"C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/SleepPortalViewer_R2013b(8.2)_Win32.exe\"",
//				"D:\\ABC\\",
//				"ABC_012345.edf",
//				"D:\\ABC\\",
//				"ABC_012345_MIMI.xml"
//			};
//			String[] vir = new String[]{
//					"PATH=C:/Program Files/MATLAB/R2013b/runtime/win32"
//			};
//			Process proc = Runtime.getRuntime().exec(cmd, vir);
			
//			String[] cmdarray = new String[]{
//					"\"C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/SleepPortalViewer_R2013b(8.2)_Win32.exe\"",
//					"D:\\ABC\\",
//					"ABC_012345.edf",
//					"D:\\ABC\\",
//					"ABC_012345_MIMI.xml"
//			};
//			System.out.println(Arrays.toString(cmdarray));
//			String[] envp = new String[]{
//				"PATH=C:\\Program Files\\MATLAB\\R2013b\\runtime\\win32\\"
//			};
//			File dir = new File("C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/");
//			Process proc = Runtime.getRuntime().exec(cmdarray,envp,dir);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main1(String[]args) throws IOException{
		String[] cmd;
		cmd = new String[] {
//				"C:\\Documents and Settings\\Gang\\git\\edf-editor-translator\\viewers\\SleepPortalViewer_R2013b(8.2)_Win32.bat"
				"C:/Documents and Settings/Gang/git/edf-editor-translator/viewers/SleepPortalViewer_R2013b(8.2)_Win32.bat"
			};
		
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			StringBuffer output = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		System.out.println(Arrays.toString(cmd));
//
//		Process proc = Runtime.getRuntime().exec(cmd);
//        java.io.InputStream is = proc.getInputStream();
//        java.io.InputStream es = proc.getErrorStream();
//        java.util.Scanner isS = new java.util.Scanner(is).useDelimiter("\\A");
//        java.util.Scanner esS = new java.util.Scanner(es).useDelimiter("\\A");
//        if (isS.hasNext()) {
//            System.out.println(isS.next());
//        }
//        if (esS.hasNext()) {
//            System.out.println(esS.next());
//        }
        
        
        
        
	}

}

class StreamGobbler extends Thread {
	InputStream is;
	String type;
	OutputStream os;

	StreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	StreamGobbler(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}

	public void run() {
		try {
			PrintWriter pw = null;
			if (os != null)
				pw = new PrintWriter(os);

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (pw != null)
					pw.println(line);
				System.out.println(type + ">" + line);
			}
			if (pw != null)
				pw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}