package translator.utils;

import java.awt.Color;
import java.io.File;

import javax.swing.JFileChooser;

public class Keywords {
	
	public final static String translator_log = "translator_log.txt";
	
	final public static String key_edfname = "[EdfName]";
	final public static String key_vendor = "[Vendor]";
	final public static String key_date = "[Date]";
	final public static String key_time = "[Time]";
	
	public final static Color tabbedPane = new Color(255, 255, 255);
	public final static Color background = new Color(255, 255, 204);
	public final static Color status = new Color(225, 240, 250);
	
	public static JFileChooser fileFolderChooser = new JFileChooser();
	static{
		fileFolderChooser.setCurrentDirectory(new File("."));
	}
}
