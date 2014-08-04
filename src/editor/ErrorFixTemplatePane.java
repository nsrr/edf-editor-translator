package editor;

import header.EIAHeader;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import validator.fix.ErrorFix;
import validator.fix.ErrorTypes;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The UI part of the "Fix EDF Header Errors" functionality 
 */
public class ErrorFixTemplatePane extends BasicEDFPane implements ActionListener{
	// JList to JList<String>, by wei wang, 2014-7-18

	private static final long serialVersionUID = 1L;

	private JPanel formPane;
	private JLabel err1_Label, err2_Label, err3_Label;
	private JCheckBox err1_Check, err2_Check, err3_Check;
	private JButton button;
	private static JList<String> edf_files;
	private ArrayList<String> selected_edf_files;
	private JScrollPane edf_sroll;

	private static ErrorFixTemplatePane _ErrorFixTemplatePane = null;

	/**
	 * Gets the ErrorFixTemplatePane using an EIA header and a master file
	 * @param eiaHeader not used in this implementation
	 * @param msFile the form configure file, not used in this implementation
	 * @return an ErrorFixTemplatePane
	 */
	public static ErrorFixTemplatePane getErrorFixTemplatePane(EIAHeader eiaHeader, File msFile) {
		
		if(_ErrorFixTemplatePane == null) {
			_ErrorFixTemplatePane = new ErrorFixTemplatePane(eiaHeader, msFile);
		}
		
		step5_loadEdfFileList();
		
		return _ErrorFixTemplatePane;
	}

	/**
	 * Constructor of this ErrorFixTemplatePane using an EIA header and a master file
	 * @param eiaHeader not used in this implementation
	 * @param msFile the master file of this pane. Not used in this implementation
	 */
	private ErrorFixTemplatePane(EIAHeader eiaHeader, File msFile) {

		super();
		this.setIsPrimaryTab(false);
		this.setMasterFile(msFile);

		step1_createGuiComponents();
		step2_createFormPane();
		step3_setupLayout();
		step4_loadFromConfigureFile(msFile);
	}

	/**
	 * Creates the related GUI components and 
	 * attach listeners to the list model and
	 * the Apply error-fixes button
	 */
	private void step1_createGuiComponents() {
		
		err1_Label = new JLabel("Swap Physical Max/Min");
		err2_Label = new JLabel("Empty Version Field");
		err3_Label = new JLabel("Invalid Date/time Separator");
		
		Font font = new Font(MainWindow.tabPane.getFont().getName(), MainWindow.tabPane.getFont().getStyle(),
				MainWindow.tabPane.getFont().getSize() + 2);
		
		err1_Label.setFont(font);
		err2_Label.setFont(font);
		err3_Label.setFont(font);
		
		err1_Check = new JCheckBox();
		err2_Check = new JCheckBox();
		err3_Check = new JCheckBox();
		
		err1_Check.setName(ErrorTypes.phyMaxMin.toString());
		err2_Check.setName(ErrorTypes.emptyVersion.toString());
		err3_Check.setName(ErrorTypes.InvalidDateTimeSeparator.toString());
		
		err2_Check.setEnabled(false);
		err3_Check.setEnabled(false);
		
		edf_files = new JList<String>();
		edf_files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ListSelectionModel listSelectionModel1 = edf_files.getSelectionModel();
		listSelectionModel1
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							selected_edf_files = new ArrayList<String>();
							@SuppressWarnings("deprecation")
							Object[] selected_Objects = edf_files.getSelectedValues();
							if (selected_Objects != null)
								for (Object object : selected_Objects)
									selected_edf_files.add(object.toString());
						}
					}
				});
		edf_sroll = new JScrollPane(edf_files);
		edf_sroll.setPreferredSize(new Dimension(500, 200));
		

		button = new JButton("Apply Error-fixes");
		button.addActionListener(this);
	}
	
	/**
	 * Creates the layout, default is error1("Swap Physical Max/Min") form
	 */
	private void step2_createFormPane() {

		String colSpec = "r:80dlu:n, 4dlu:n, 12dlu:n, 2dlu:n, f:150dlu:n, 4dlu:n, r:120dlu:n, f:p:g";
		String rowSpec = 
				"4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:35dlu:n, 4dlu:n";
		FormLayout layout = new FormLayout(colSpec, rowSpec);
		formPane = new JPanel(layout);
		formPane.setBorder(BorderFactory.createTitledBorder("Apply Error-fix on Invalid EDF Tables"));
		CellConstraints cc = new CellConstraints();

		formPane.add(err1_Check, cc.xy(1, 2));
		formPane.add(err1_Label, cc.xy(5, 2));

//		formPane.add(err2_Check, cc.xy(1, 4));
//		formPane.add(err2_Label, cc.xy(5, 4));
//		formPane.add(err3_Check, cc.xy(1, 6));
//		formPane.add(err3_Label, cc.xy(5, 6));

		formPane.add(edf_sroll, cc.xyw(2, 14, 6));
		formPane.add(button, cc.xyw(5, 16, 2));
	}

	/**
	 * Sets up the layout
	 */
	public void step3_setupLayout() {
		FormLayout layout = new FormLayout(
				"6dlu, f:max(400dlu;p):n, f:p:g, 10dlu:n",
				"c:p:n, 6dlu:n, c:min(35dlu;p):n, 6dlu:n, c:min(35dlu;p):n");
		this.setLayout(layout);
		CellConstraints cc = new CellConstraints();

		this.add(formPane, cc.xywh(2, 1, 2, 1));

		this.setBorder(BorderFactory.createEtchedBorder());
	}
	
	/**
	 * Loads form configure file, not implemented
	 * @param msFile the form configure file, to be implemented
	 */
	private void step4_loadFromConfigureFile(File msFile){
		
	}
	
	/**
	 * Loads source EDF files into local variable {@link ErrorFixTemplatePane#edf_files}
	 * , and display the working directory to console
	 */
	private static void step5_loadEdfFileList() {
		
		if (MainWindow.getWkEdfFiles() == null)
			return;
		
		int i = 0;
		String[] files = new String[MainWindow.getWkEdfFiles().size()];
		for (File file : MainWindow.getSrcEdfFiles()) {
			files[i++] = file.getAbsolutePath();
		}
		edf_files.setListData(files);
		
		if (MainWindow.workingDirectory != null) {
			System.out.println("Working Directory: " + MainWindow.workingDirectory.getAbsolutePath());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == button) {
			if (selected_edf_files != null) {
				
				ArrayList<ErrorTypes> errorTypeAL = new ArrayList<ErrorTypes>();
				
				Component[] components = formPane.getComponents();
				for (Component com : components){
					if (com instanceof JCheckBox){
						JCheckBox check = (JCheckBox)com;
						if (check.isSelected()){
							if (check.getName().equals(ErrorTypes.phyMaxMin.toString())) {
								errorTypeAL.add(ErrorTypes.phyMaxMin);
							} else if (check.getName().equals(ErrorTypes.emptyVersion.toString())) {
								errorTypeAL.add(ErrorTypes.emptyVersion);
							} else if (check.getName().equals(ErrorTypes.InvalidDateTimeSeparator.toString())) {
								errorTypeAL.add(ErrorTypes.InvalidDateTimeSeparator);
							}
						}
					}
				}
				
				ErrorFix.fixErrors(selected_edf_files, errorTypeAL);
			}
		}
	}

}
