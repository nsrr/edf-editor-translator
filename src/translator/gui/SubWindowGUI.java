package translator.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.*;

import translator.utils.Keywords;

/**
 * A GUI to show the EDF Annotation Translator and related log/file viewer
 */
@SuppressWarnings("serial")
public class SubWindowGUI extends JFrame {
	
	private static HashMap<String, Integer> map__filename_with_tabIndex = new HashMap<String, Integer>();
	private static JTabbedPane jtabbedPane;
	
	private static SubWindowGUI _instance = null;
	
	/**
	 * Static factory method of returning a new SubWindowGUI instance if not exist
	 * @return a SubWindowGUI instance 
	 */
	public static SubWindowGUI getInstance() {
		if (_instance == null) {
			_instance = new SubWindowGUI();
			_instance.setTitle("EDF Annotation Translator");

    		Dimension dim1 = Toolkit.getDefaultToolkit().getScreenSize();
    		Dimension dim2 = new Dimension(1100,800);
    		if (dim1.width > dim2.width || dim1.height > dim2.height){
    			_instance.setSize(dim2);
    			_instance.setMinimumSize(dim2);
    		} else {
    			_instance.setSize(dim1);
    			_instance.setMinimumSize(dim1);
    		}
    		
    		//Set JFrame to appear centered, regardless of the Monitor resolution
    		int frame_x = (dim1.width - _instance.getSize().width) / 2;
    		int frame_y = (dim1.height - _instance.getSize().height) / 2;
    		_instance.setLocation(frame_x, frame_y);
		}
		_instance.setVisible(true);
		_instance.setResizable(true);
		return _instance;
	}
	
	/**
	 * Default SubWindowGUI constructor
	 */
	private SubWindowGUI() {
		
		jtabbedPane = new JTabbedPane();
		jtabbedPane.setBackground(Keywords.tabbedPane);
		jtabbedPane.setPreferredSize(new Dimension(1200, 900));
		jtabbedPane.addTab("EDF Converter", new TranslatorGUI());
		jtabbedPane.addTab("Complete Log Viewer", new QuickViewerGUI(Keywords.translator_log, false));
		this.add(jtabbedPane);
		this.setResizable(false);
	}
	
	/**
	 * Return <code>true</code> if this SubWindowGUI contains the specified file viewer
	 * @param filename the file name to be tested
	 * @return true if the file exists
	 */
	public static boolean existViewer(String filename) {
		return map__filename_with_tabIndex.containsKey(filename);
	}
	
	/**
	 * Add a file viewer tab using file name and tab name
	 * @param tabName the tab name
	 * @param filename the file name
	 */
	public static void addViewerTab(String tabName, String filename) {
		map__filename_with_tabIndex.put(filename, 1);
		jtabbedPane.addTab(tabName, new QuickViewerGUI(filename, true));
	}
	
	/**
	 * Remove a viewer tab
	 * @param tab the tab to be removed
	 */
	public static void removeViewerTab(QuickViewerGUI tab) {
		map__filename_with_tabIndex.remove(tab.filename);
		jtabbedPane.remove(tab);
	}
}
