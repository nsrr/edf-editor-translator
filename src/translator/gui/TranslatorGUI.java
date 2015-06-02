package translator.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import translator.logic.AnnotationTranslatorClient;
import translator.utils.Keywords;
import translator.utils.MyDate;
import translator.utils.Vendor;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

//remove warnings:
// wei wang: JComboBox -> JComboBox<String>; JList -> JList<String>; DefaultListModel -> DefaultListModel<String>
/**
 * The GUI of EDF Annotation Translator
 */
@SuppressWarnings("serial")
public class TranslatorGUI extends JPanel implements ActionListener, ItemListener {
	
	// ComboBox of Vendors
	private String vendor_Selected = null;
	
	// Input-File of Mapping file:
	private JTextField JTextField_MappingFile;
	private JButton JButton_MappingFile;
	
	// Input-Directory of EDF files:
	private JTextField JTextField_EdfDirectory;
	private JButton JButton_EdfDirectory;
	private ArrayList<String> selected_Edf_files = null;

	// Input-Directory of Annotation files:
	private JTextField JTextField_AnnotationDirectory;
	private JButton JButton_AnnotationDirectory;
	
	// Input-Directory of Stage files:
	private JTextField JTextField_StageDirectory;
	private JButton JButton_StageDirectory;
	
	// Output-Directory of Output files:
	private JTextField JTextField_OutputDirectory;
	private JButton JButton_OutputDirectory;
	
	// Pattern design of Output filename:
	private Checkbox Checkbox_Vendor;
	private Checkbox Checkbox_Date;
	private Checkbox Checkbox_Time;
	private JTextField JTextField_OutputPattern;
	private JLabel JLabel_OutputExample;
	
	// List of EDF-files and Output-files
	private JList<String> JList_Edf_files, JList_Out_files;
	
	// Translate Button
	private JButton JButton_Translate;
	
	// List of Messages/Status
	private JList<String> JList_Messages = null;
	private DefaultListModel<String> ListModel_Messages = null;
	
	/**
	 * Initialize translator GUI
	 */
	public TranslatorGUI() {
		
		/* (1) Background */
		this.setLayout(new GridBagLayout());
		this.setBackground(Keywords.background);
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (2) "Selection of Vendor" */ //TODO: added Embla new xml format
		String[] vendorList = new String[] { 
				"----- Select a vender from the dropdown menu -----                                                                                       ", 
				Vendor.Embla.toString(), Vendor.Compumedics.toString(),
				Vendor.Embla_EDFBrowser.toString(), // Add this line for translating annotation to EDFBrowser version; TODO
				Vendor.Compumedics_EDFBrowser.toString(), // Add this line for translating annotation to EDFBrowser version; TODO
				Vendor.Respironics.toString(), Vendor.Sandman.toString()
		};
		JComboBox<String> JComboBox_vendor = new JComboBox<String>(vendorList);  
		JComboBox_vendor.addActionListener (
			new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    @SuppressWarnings("unchecked")
					JComboBox<String> jComboBox = (JComboBox<String>)e.getSource();
                    boolean enable;
                    if (jComboBox.getSelectedIndex() == 0) {
                    	enable = false;
                    	vendor_Selected = null;
                    } else {
                    	enable = true;
                    	vendor_Selected = (String)jComboBox.getSelectedItem();
                    }
                    enableComponents(enable);

                    String pattern = JTextField_OutputPattern.getText();
                    String example = AnnotationTranslatorClient.customize_out_file(pattern, null, vendor_Selected);
                    JLabel_OutputExample.setText("<html><font color='blue'>" + example + "</font></html>");
                }
            }
        );
		LayoutManager.addFirstField(new JLabel("Vendor:"), this);
		LayoutManager.addMiddleField(JComboBox_vendor, this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (3) "Selection of Mapping file" */
		LayoutManager.addFirstField(new JLabel("Select a mapping file:"), this);
		JTextField_MappingFile = new JTextField();
		LayoutManager.addMiddleField(JTextField_MappingFile, this);
		JButton_MappingFile = new JButton("Browse");
		JButton_MappingFile.addActionListener(this);
		LayoutManager.addLastField(JButton_MappingFile, this);
		
		/* (4) "Input-Directory of EDF files" */
		LayoutManager.addFirstField(new JLabel("Select input-directory of EDF files:"), this);
		JTextField_EdfDirectory = new JTextField();
		LayoutManager.addMiddleField(JTextField_EdfDirectory, this);
		JButton_EdfDirectory = new JButton("Browse");
		JButton_EdfDirectory.addActionListener(this);
		LayoutManager.addLastField(JButton_EdfDirectory, this);
		
		/* (5) "Input-Directory of Annotation files" */
		LayoutManager.addFirstField(new JLabel("Select input-directory of annotation files:"), this);
		JTextField_AnnotationDirectory = new JTextField();
		LayoutManager.addMiddleField(JTextField_AnnotationDirectory, this);
		JButton_AnnotationDirectory = new JButton("Browse");
		JButton_AnnotationDirectory.addActionListener(this);
		LayoutManager.addLastField(JButton_AnnotationDirectory, this);

		/* (6) "Input-Directory of Annotation files" */
		LayoutManager.addFirstField(new JLabel("Select input-directory of stage files:"), this);
		JTextField_StageDirectory = new JTextField();
		LayoutManager.addMiddleField(JTextField_StageDirectory, this);
		JButton_StageDirectory = new JButton("Browse");
		JButton_StageDirectory.addActionListener(this);
		LayoutManager.addLastField(JButton_StageDirectory, this);
		
		/* (7) "Output-Directory of resulting files" */
		LayoutManager.addFirstField(new JLabel("Select output-directory of translated files:"), this);
		JTextField_OutputDirectory = new JTextField();
		LayoutManager.addMiddleField(JTextField_OutputDirectory, this);
		JButton_OutputDirectory = new JButton("Browse");
		JButton_OutputDirectory.addActionListener(this);
		LayoutManager.addLastField(JButton_OutputDirectory, this);
		
		/* (8) "Output-filename Pattern Design" */
		//(8.1) Design of Pattern Filename
		Checkbox_Vendor = new Checkbox("Vendor", null, false);
		Checkbox_Date = new Checkbox("Date", null, false);
		Checkbox_Time = new Checkbox("Time", null, false);
		Checkbox_Vendor.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			String pattern = AnnotationTranslatorClient.updateOutputPattern(JTextField_OutputPattern.getText(), Keywords.key_vendor, e);
			JTextField_OutputPattern.setText(pattern);	
		}});
		Checkbox_Date.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			String pattern = AnnotationTranslatorClient.updateOutputPattern(JTextField_OutputPattern.getText(), Keywords.key_date, e);
			JTextField_OutputPattern.setText(pattern);	
		}});
		Checkbox_Time.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			String pattern = AnnotationTranslatorClient.updateOutputPattern(JTextField_OutputPattern.getText(), Keywords.key_time, e);
			JTextField_OutputPattern.setText(pattern);	
		}});
		
		JTextField_OutputPattern = new JTextField("", 30);
		JTextField_OutputPattern.setText(Keywords.key_edfname + ".xml");
		JTextField_OutputPattern.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {update();}
				public void removeUpdate(DocumentEvent e) {update();}
				public void insertUpdate(DocumentEvent e) {update();}
				private void update() {
                	String pattern = JTextField_OutputPattern.getText();
                	String example = AnnotationTranslatorClient.customize_out_file(pattern, null, vendor_Selected);
                	JLabel_OutputExample.setText("<html><font color='blue'>" + example + "</font></html>");
				}
		});
		
		JPanel jPanel1 = new JPanel(new GridBagLayout());
		jPanel1.setBackground(Keywords.background);
		jPanel1.add(JTextField_OutputPattern);
		jPanel1.add(new JLabel("        "));
		jPanel1.add(Checkbox_Vendor);
		jPanel1.add(new JLabel("  "));
		jPanel1.add(Checkbox_Date);
		jPanel1.add(new JLabel("  "));
		jPanel1.add(Checkbox_Time);
		LayoutManager.addFirstField(new JLabel("Customize Output Filename:"), this);
		LayoutManager.addMiddleField(jPanel1, this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		//(8.2) Review of Example Filename
		JLabel_OutputExample = new JLabel("");
    	String pattern = JTextField_OutputPattern.getText();
    	String example = AnnotationTranslatorClient.customize_out_file(pattern, null, vendor_Selected);
    	JLabel_OutputExample.setText("<html><font color='blue'>" + example + "</font></html>");
		
		JPanel jPanel2 = new JPanel();
		jPanel2.add(new JLabel("Example Filename: "));
		jPanel2.add(JLabel_OutputExample);
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(jPanel2, this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (9) JPanel of "List of EDF files" and "List of Output files" */
		JPanel JPanel_TwoLists = new JPanel();
		JPanel_TwoLists.setLayout(new GridBagLayout());
		JPanel_TwoLists.setBackground(Keywords.background);
		LayoutManager.addItemList(JPanel_TwoLists, this);
		
		/* (10) "List of EDF files" */
		LayoutManager.addFirstField(new JLabel("List of EDF files:"), JPanel_TwoLists);
		JList_Edf_files = new JList<String>();
		JList_Edf_files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ListSelectionModel listSelectionModel1 = JList_Edf_files.getSelectionModel();
        listSelectionModel1.addListSelectionListener(new ListSelectionListener (){
        	public void valueChanged(ListSelectionEvent e) {
        		if (e.getValueIsAdjusting()){
        			selected_Edf_files = new ArrayList<String>();
        			@SuppressWarnings("deprecation")
					Object [] selected_Objects = JList_Edf_files.getSelectedValues(); // this should change to getSelectedValuesList()
        			if (selected_Objects != null)
        				for(Object object : selected_Objects)
        					selected_Edf_files.add(object.toString());
        		}
        	}
        });
		JScrollPane JSroll_Edf_files = new JScrollPane(JList_Edf_files);
		JSroll_Edf_files.setPreferredSize(new Dimension(200,200));
		LayoutManager.addItemList(JSroll_Edf_files, JPanel_TwoLists);
		
		/* (11) "List of Output files" */
		LayoutManager.addFirstField(new JLabel("List of Output files:"), JPanel_TwoLists);
		JList_Out_files = new JList<String>();
		JList_Out_files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel listSelectionModel2 = JList_Out_files.getSelectionModel();
		listSelectionModel2.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		if (e.getValueIsAdjusting()) {
        			String output_file = (String) JList_Out_files.getSelectedValue();
        			if (!SubWindowGUI.existViewer(output_file)){
        				int i0 = output_file.lastIndexOf(File.separator);
        				int i1 = i0 < 0 ? i0 : i0 + 1;
        				int i2 = output_file.length();
        				if (i1 < i2){
        					String name = output_file.substring(i1, i2);
        					SubWindowGUI.addViewerTab(name, output_file);
        				}
        			}
        		}
        	}
        });
		JScrollPane JSroll_Out_files = new JScrollPane(JList_Out_files);
		JSroll_Out_files.setPreferredSize(new Dimension(200,200));
		LayoutManager.addItemList(JSroll_Out_files, JPanel_TwoLists);
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (12) "Execute Translation Button" */
		JButton_Translate = new JButton("Translate");
		JButton_Translate.addActionListener(this);
		LayoutManager.addFirstField(JButton_Translate, this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);

		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel("Quick Log Viewer:"), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (13) JPanel of "Status/Messages" */
		JPanel JPanel_Status = new JPanel();
		JPanel_Status.setLayout(new GridBagLayout());
		JPanel_Status.setBackground(Keywords.background);
		LayoutManager.addItemList(JPanel_Status, this);
		
		/* (14) "Status" */
		ListModel_Messages = new DefaultListModel<String>();
		JList_Messages = new JList<String>(ListModel_Messages);
		JScrollPane JSroll_Messages = new JScrollPane(JList_Messages);
		JSroll_Messages.setPreferredSize(new Dimension(200,100));
		JList_Messages.setBackground(Keywords.status);
		LayoutManager.addItemList(JSroll_Messages, JPanel_Status);
		
		AnnotationTranslatorClient.ListModel_Messages = ListModel_Messages;
		AnnotationTranslatorClient.JList_Messages = JList_Messages;
		
		/* (15) Disable all fields before having vendor selected */
		enableComponents(false);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		JFileChooser select = new JFileChooser();
		select.setCurrentDirectory(Keywords.fileFolderChooser.getCurrentDirectory());
		
		if (e.getSource() == JButton_MappingFile) {
			select.setDialogTitle("Select a mapping file");
			select.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = 
					new FileNameExtensionFilter(
							"Mapping file (*.csv)", 
							new String[] {"CSV", "csv", "Csv"}
					);
			select.setFileFilter(filter);
			if (select.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String file = select.getSelectedFile().getAbsolutePath();
				JTextField_MappingFile.setText(file);
			}
		} else if (e.getSource() == JButton_EdfDirectory) {
			select.setDialogTitle("Select EDF files");
			select.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			select.setAcceptAllFileFilterUsed(false);
			if (select.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String pathname = select.getSelectedFile().getAbsolutePath();
				JTextField_EdfDirectory.setText(pathname);
				Collection<File> fileCollection = FileUtils.listFiles(
						new File(pathname), new String[]{"edf", "Edf", "eDf", "edF", "EDf", "EdF", "eDF", "EDF"}, true);
				if (fileCollection != null) {
					String[] files = new String[fileCollection.size()];
					int i = 0;
					for (File file : fileCollection)
						files[i++] = file.getAbsolutePath();
					JList_Edf_files.setListData(files);
					selected_Edf_files = null;
				}
				//The following code is to give suggested paths
				//Code Starts
				if (JTextField_AnnotationDirectory.getText().equals("")) {
					JTextField_AnnotationDirectory.setText(pathname);
				}
				if (JTextField_StageDirectory.getText().equals("")) {
					if (vendor_Selected!=null && vendor_Selected == Vendor.Respironics.toString()){
			    		JTextField_StageDirectory.setEnabled(true);
			    		JTextField_StageDirectory.setText(pathname);
			    		JButton_StageDirectory.setEnabled(true);
			    	}
				}
				//Code Ends
			}
		} else if (e.getSource() == JButton_AnnotationDirectory) {
			select.setDialogTitle("Select annotation files");
			select.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			select.setAcceptAllFileFilterUsed(false);
			if (select.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				String pathname = select.getSelectedFile().getAbsolutePath();
				JTextField_AnnotationDirectory.setText(pathname);
			}
		} else if (e.getSource() == JButton_StageDirectory) {
			select.setDialogTitle("Select stage files");
			select.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			select.setAcceptAllFileFilterUsed(false);
			if (select.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				String pathname = select.getSelectedFile().getAbsolutePath();
				JTextField_StageDirectory.setText(pathname);
			}
		} else if (e.getSource() == JButton_OutputDirectory) {
			select.setDialogTitle("Select output-directory of translated files");
			select.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			select.setAcceptAllFileFilterUsed(false);
			if (select.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				String pathname = select.getSelectedFile().getAbsolutePath();
				JTextField_OutputDirectory.setText(pathname);
			}
		} else if (e.getSource() == JButton_Translate) {
			
			AnnotationTranslatorClient.addElementIntoLog("", true);
			AnnotationTranslatorClient.addElementIntoLog("=====================================================================", true);
			AnnotationTranslatorClient.addElementIntoLog(" + User started a new task at " + MyDate.currentDateTime(), true);
			
			ArrayList<String> errorMessages = validatePrerequisites();

			if (errorMessages.size() > 0) {
				for (String message : errorMessages) {
					AnnotationTranslatorClient.addElementIntoLog("    * " + message, false);
				}
				AnnotationTranslatorClient.addElementIntoLog(" + The task failed. Please check the above error messages!", false);
			} else {
				String vendor = vendor_Selected;
				String mapping_file = JTextField_MappingFile.getText();
				String edf_dir = JTextField_EdfDirectory.getText();
				String annotation_dir = JTextField_AnnotationDirectory.getText();
				String stage_dir = JTextField_StageDirectory.getText();
				String output_dir = JTextField_OutputDirectory.getText();
				String outname = JTextField_OutputPattern.getText();
				
				ArrayList<String> successfulOutAL = AnnotationTranslatorClient.conductTranslation(
								vendor, 
								mapping_file, 
								edf_dir, 
								selected_Edf_files, 
								annotation_dir, 
								stage_dir, 
								output_dir, 
								outname
				);

				String[] outputFiles = successfulOutAL.toArray(new String[successfulOutAL.size()]);
				JList_Out_files.setListData(outputFiles);
			}
		}
		Keywords.fileFolderChooser = select;
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		String pattern = JTextField_OutputPattern.getText();
    	String example = AnnotationTranslatorClient.customize_out_file(pattern, null, vendor_Selected);
    	JLabel_OutputExample.setText("<html><font color='blue'>" + example + "</font></html>");
	}
	
	/**
	 * Validate related fields. If error occurs, output them into the error log
	 * @return ArrayList containing the error messages
	 */
	private ArrayList<String> validatePrerequisites() {
		
		String mapping_file = JTextField_MappingFile.getText();
		String edf_dir = JTextField_EdfDirectory.getText();
		String annotation_dir = JTextField_AnnotationDirectory.getText();
		String stage_dir = JTextField_StageDirectory.getText();
		String output_dir = JTextField_OutputDirectory.getText();
		
		File File_MappingFile = new File(mapping_file);
		File File_EdfDirectory = new File(edf_dir);
		File File_AnnotationDirectory = new File(annotation_dir);
		File File_StageDirectory = new File(stage_dir);
		File File_OutputDirectory = new File(output_dir);
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		System.out.println("Inside validatePrerequisites function...");  // ww test
		if (vendor_Selected==null)
			errorMessages.add("Please select a vendor from the dropdown menu;");
		
		System.out.println("mapping_file: " + mapping_file); // test ww
		if (mapping_file.equals(""))
			errorMessages.add("Please select a mapping file;");
		if (edf_dir.equals(""))
			errorMessages.add("Please select the input-directory of EDF files;");
		if (annotation_dir.equals(""))
			errorMessages.add("Please select the input-directory of Annotation files;");
		if (vendor_Selected == Vendor.Respironics.toString() && stage_dir.equals(""))
			errorMessages.add("Please select the input-directory of Stage files;");
		if (output_dir.equals(""))
			errorMessages.add("Please select the output-directory of resulting files;");
		
		if (!mapping_file.equals("") && !File_MappingFile.exists())
			errorMessages.add("The chosen mapping file no longer exists;");
		if (!edf_dir.equals("") && !File_EdfDirectory.exists())
			errorMessages.add("The chosen input-directory of EDF files no longer exists;");
		if (!annotation_dir.equals("") && !File_AnnotationDirectory.exists())
			errorMessages.add("The chosen input-directory of Annotation files no longer exists;");
		if (vendor_Selected == Vendor.Respironics.toString() && !stage_dir.equals("") && !File_StageDirectory.exists())
			errorMessages.add("The chosen input-directory of Stage files no longer exists;");
		if (!output_dir.equals("") && !File_OutputDirectory.exists())
			errorMessages.add("The chosen output-directory of Resulting files no longer exists;");
		
		if (selected_Edf_files==null || selected_Edf_files.size()==0)
			errorMessages.add("No EDF files were selected;");
		
		return errorMessages;
	}
	
	/**
	 * Enable components corresponding to the selected vendor
	 * @param enable true to enable specified components related to the selected vendor
	 */
	private void enableComponents(boolean enable) {
		JTextField_MappingFile.setEnabled(enable);
		JTextField_EdfDirectory.setEnabled(enable);
		JTextField_AnnotationDirectory.setEnabled(enable);
        JTextField_StageDirectory.setEnabled(enable);
        JTextField_OutputDirectory.setEnabled(enable);
        JButton_MappingFile.setEnabled(enable);
        JButton_EdfDirectory.setEnabled(enable);
        JButton_AnnotationDirectory.setEnabled(enable);
        JButton_StageDirectory.setEnabled(enable);
        JButton_OutputDirectory.setEnabled(enable);
        Checkbox_Vendor.setEnabled(enable);
    	Checkbox_Date.setEnabled(enable);
    	Checkbox_Time.setEnabled(enable);
    	JTextField_OutputPattern.setEnabled(enable);
        JList_Edf_files.setEnabled(enable);
		JList_Out_files.setEnabled(enable);
        JButton_Translate.setEnabled(enable);
    	
    	if (vendor_Selected!=null && vendor_Selected != Vendor.Respironics.toString()) {
    		JTextField_StageDirectory.setEnabled(false);
    		JTextField_StageDirectory.setText("");
    		JButton_StageDirectory.setEnabled(false);
    	}
	}	
}
