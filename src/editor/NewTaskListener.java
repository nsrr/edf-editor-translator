package editor;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import header.EDFFileHeader;
import header.ESAHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.imageio.ImageIO;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.commons.io.FileUtils;

import listener.EDFFileFilter;
import table.EDFTable;
import table.EIATable;
import table.ESATable;
import table.ErrorListTable;
import table.Incompliance;
import translator.utils.Keywords;
import translator.utils.MyDate;


public class NewTaskListener extends JDialog {

    protected static JTextField srcFilesDirField;
    protected static JTextField workingDirField;
    protected static JLabel selectedFileCountLabel;
    private static JLabel choosebyLabel = new JLabel("Choose by");
    private static JLabel overwriteLabel = new JLabel("Overwrite source files?");

    private JButton srcFilesBrowseBtn; // added by Fangping, 08/03/2010
    private JButton ooutputDirBrowseBtn;
    private JButton finishButton;
    private JButton cancelButton;

    private JRadioButton fileRadio;
    private JRadioButton dirRadio;
    private JRadioButton yesOverwriteRadio;
    private JRadioButton noOverwriteRadio;

    private final int dialogWidth = 600;
    private final int dialogHeight = 450;
    private final String extName = "edf";
    private final String description = "EDF Files (*.edf)";

    private final static String srctip = "Select one or mulitple EDF source files";
    private final static String srcDtip = "Select a source directory of EDF files";
    private final static String wktip = "Specify a directory to store working files";
    
    public final static String defaultDirectoryName = "Physiomimi Work";

    protected ArrayList<File> sourceFiles;
    protected File sourceDirectory;
    private File workingDirectory;
    
    private boolean dirMode = true;
    private boolean overwriteMode = false;
    
    //Fangping, 08/20/2010
    private Document doc = MainWindow.consolePane.getDocument();
    private String theme, content;
    
    private static final Color alertColor = new Color(255, 240, 188);
    private static final Dimension favoriteDim = new Dimension(80, 28);
    
    //good place to initilaize some static elements
    //Fangping, 08/23/2010
    static{
        Font oldFont = choosebyLabel.getFont();
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize());
        choosebyLabel.setFont(newFont);
        overwriteLabel.setFont(newFont);
    }

    public NewTaskListener(JFrame frame) {
        super(frame, true); // modal
        this.setLocationRelativeTo(frame);

        initUI();
        setDialogLayout();
        visualize();
    }
    
	/************************************************************** 
	 * The below feature improvement was made by Gang Shu on February 7, 2014
	 **************************************************************/
    private static void addElementIntoLog(String message, boolean showOnScreen, String outfile){
    	
    	if (showOnScreen){
			System.out.println(message);
    	}
    	
		BufferedWriter out = null;
		try {
			new File(outfile).getParentFile().mkdirs();
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile, (new File(outfile)).exists())));
			out.write(message + "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
    
	public NewTaskListener(String edf_dir, String output) {
		
		super(new JFrame(), false);
		
		File f = new File(edf_dir);
		if (f.exists()) {
			// (1) search for the edf files contained in chosen folder.
			sourceFiles = new ArrayList<File>();
			@SuppressWarnings("unchecked")
			Collection<File> fileCollection = FileUtils.listFiles(f, new String[]{"edf", "Edf", "eDf", "edF", "EDf", "EdF", "eDF", "EDF"}, true);
			addElementIntoLog("===============================================================", true, output);
			addElementIntoLog("  => User start a EDF validation at " + MyDate.currentDateTime(), true, output);
			addElementIntoLog("  *  A list of EDF files:", true, output);
			if (fileCollection != null){
				int i = 0;
				for (File file : fileCollection){
					addElementIntoLog("     " + (++i) + ": " + file.getAbsolutePath(), true, output);
					sourceFiles.add(file);
				}
			}
			
			// (2) simulate GUI operation on an invisible GUI.
			MainWindow mainWindow = new MainWindow();
			MainWindow.wkEdfFiles = sourceFiles;
			mainWindow.setVisible(false);

			initUI();
			setVisible(false);
			FinishButtonListener finishButtonListener = new FinishButtonListener();
			finishButtonListener.createWorkingDirectory();
			finishButtonListener.yieldNewEDFHeaders();
			finishButtonListener.yieldEiaTable();
			finishButtonListener.yieldEsaTables();

			// (3) validate the chosen EDF files.
			(new MainWindow.VerifyHeaderListener()).verifyHeaders();
			ArrayList<Incompliance> aggregateIncompliances = MainWindow.aggregateIncompliances();
			
			addElementIntoLog("  *  The number of errors: " + aggregateIncompliances.size(), true, output);
			
			for (int i = 0; i < aggregateIncompliances.size(); i++) {
				Incompliance error = aggregateIncompliances.get(i);
				String message = "";
				message += "------" + "\r\n";
				message += "Error #" + (i+1) + ": " + error.getFileName();
				message += " (Row: " + error.getRowIndex() + ", Col: " + error.getColumnIndex() + ")\r\n";
				message += "Description: " + error.getDescription();
				addElementIntoLog(message, true, output);
			}
		}
		
	}
	/************************************************************** 
	 * The above feature improvement was made by Gang Shu on February 7, 2014
	 **************************************************************/

    private void initUI() {
        this.setSize(new Dimension(dialogWidth, dialogHeight));
        

        srcFilesDirField = createTextField();
        srcFilesDirField.setEditable(false);
        srcFilesDirField.addMouseListener(new BrowseSourceFilesBtnListener());

        workingDirField = createTextField();
        workingDirField.setEditable(true);
        workingDirField.addMouseListener(new BrowseWorkingDirButtonListener());
        
        buildSelectedFileCountLabel();

        srcFilesBrowseBtn = new JButton("Browse...");
        srcFilesBrowseBtn.addActionListener(new BrowseSourceFilesBtnListener());

        ooutputDirBrowseBtn = new JButton("Browse...");
        ooutputDirBrowseBtn.addActionListener(new BrowseWorkingDirButtonListener());
        

        finishButton = new JButton("Finish");
        finishButton.addActionListener(new FinishButtonListener());

        cancelButton = new JButton("Cancel");
        InputMap im =
            cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = cancelButton.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
        am.put("Cancel", new CancelAction());

        cancelButton.addActionListener(new CancelButtonListener());

        fileRadio = new JRadioButton("File Selections");
        fileRadio.setSelected(false);
        fileRadio.setToolTipText("select one or multiple EDF source files");
        fileRadio.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if (dirMode == true) {
                        enableOutputDirPanel(false);
                        resetActiveAreas();
                        dirMode = false;
                    }
                }
            });

        dirRadio = new JRadioButton("Directory"); 
        dirRadio.setSelected(true);
        dirRadio.setToolTipText("select a source directory of EDF files");
        dirRadio.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if (dirMode == false) 
                        enableOutputDirPanel(false);
                        resetActiveAreas();  
                        dirMode = true;
                  }
              });

        yesOverwriteRadio = new JRadioButton("Yes");
        yesOverwriteRadio.setToolTipText("overwrite source files");
        yesOverwriteRadio.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ooutputDirBrowseBtn.setEnabled(false);
                    workingDirField.setEnabled(false);
                    overwriteMode = true;
                    MainWindow.setWriteMode(MainWindow.overwrite_mode);
                    repaint();
                }
            });

        // noOverrideRadio = new JRadioButton("Save Changes to New Directory");
        noOverwriteRadio = new JRadioButton("No");
        noOverwriteRadio.setToolTipText("duplicate source files to new directory");
        noOverwriteRadio.setSelected(true);
        noOverwriteRadio.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ooutputDirBrowseBtn.setEnabled(true);
                    workingDirField.setEnabled(true);
                    if (workingDirectory != null)
                        workingDirField.setText(workingDirectory.toString());
                    overwriteMode = false;
                    MainWindow.setWriteMode(MainWindow.duplicate_mode);
                    
                    repaint();
                }
            });
        enableOutputDirPanel(false);
        finishButton.setEnabled(false);
        //disable finish button until source and output have been chosen. -- Fangping, 08/03/2010
      }
    
    private void clearselectedFileCountLabel(){
        selectedFileCountLabel.setText("");
        selectedFileCountLabel.setEnabled(false);
    }

    private void visualize() {
        this.setTitle("Select EDF Files");
        this.setLocationRelativeTo(null);	//by Gang Shu
        setLogo();
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
    }


    
    private JPanel createSelectionPanel() {
        FormLayout layout = new FormLayout("0dlu:n, pref:grow, 2dlu, pref:grow, 2dlu, pref:grow, 40dlu, 4dlu, min",
                                      "pref:grow, 6dlu, pref:grow, 6dlu, pref:grow");
        JPanel sPanel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();

/*         JLabel note = new JLabel("Choose  by: ");
        Font oldFont = note.getFont();
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() + 2);
        note.setFont(newFont); */
        sPanel.add(choosebyLabel, cc.xy(2, 1));
        sPanel.add(dirRadio, cc.xy(4, 1));
        sPanel.add(fileRadio, cc.xy(6, 1));
        sPanel.add(srcFilesDirField, cc.xyw(2, 3, 6));
        sPanel.add(srcFilesBrowseBtn, cc.xy(9, 3));
        sPanel.add(selectedFileCountLabel, cc.xyw(2, 5, 6));

        ButtonGroup selectGroup = new ButtonGroup();
        selectGroup.add(fileRadio);
        selectGroup.add(dirRadio);

        sPanel.setBorder(BorderFactory.createTitledBorder("1. Choose source files"));

        return sPanel;
    }


    public JPanel createSelectionPanel(String title, JTextField textField,
                                       JButton button) {
        int layer;
        if (title == srctip)
            layer = 3;
        else
            layer = 2;

        JPanel selectionPanel = new JPanel(new GridLayout(layer, 1));
        JLabel titleLabel = new JLabel(title);
        selectionPanel.add(titleLabel); // layer 1

        JPanel containerPanel = new JPanel();
        containerPanel.add(textField);
        containerPanel.add(button);
        selectionPanel.add(containerPanel); // layer 2

        JPanel secLayerPanel = new JPanel();
        if (layer == 3) {

            secLayerPanel.add(selectedFileCountLabel);
            selectionPanel.add(secLayerPanel);
        }

        return selectionPanel;
    }


    private JPanel createOutputPanel() {
        JPanel oPanel =
            new JPanel(new FormLayout("0dlu, pref:grow, 2dlu, pref:grow, 2dlu, pref:grow, 40dlu, 4dlu, min",
                                      "pref:grow, 6dlu, pref:grow, 6dlu"));
        CellConstraints cc = new CellConstraints();

        oPanel.add(overwriteLabel, cc.xy(2, 1));
        oPanel.add(yesOverwriteRadio, cc.xy(4, 1));
        oPanel.add(noOverwriteRadio, cc.xy(6, 1));
        oPanel.add(workingDirField, cc.xyw(2, 3, 6));
        oPanel.add(ooutputDirBrowseBtn, cc.xy(9, 3));

        ButtonGroup workingDirGroup = new ButtonGroup();
        workingDirGroup.add(yesOverwriteRadio);
        workingDirGroup.add(noOverwriteRadio);

        oPanel.setBorder(BorderFactory.createTitledBorder("2. Select output directory"));

        return oPanel;

    }

    /*
     * Fangping, 08/03/2010
     */

    private void setDialogLayout() {
        JPanel sourcePanel = createSelectionPanel();
        JPanel outputPanel = createOutputPanel();
        JPanel controlPanel = createControlPanel();
        
        FormLayout layout = new FormLayout("2dlu, pref:grow, 2dlu", "6dlu, pref, 10dlu:grow, pref");
        JPanel specPanel = new JPanel(layout);
        specPanel.setBorder(BorderFactory.createTitledBorder(" "));
        CellConstraints cc = new CellConstraints();
        specPanel.add(sourcePanel, cc.xy(2, 2));
        specPanel.add(outputPanel, cc.xy(2, 4));

        specPanel.setPreferredSize(new Dimension(dialogWidth, 100));
        specPanel.setMaximumSize(new Dimension(dialogWidth, 100));

        this.getContentPane().add(createTipPanel(), BorderLayout.NORTH);
        this.getContentPane().add(specPanel, BorderLayout.CENTER);
        this.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }


    /*
     * create a text field to dispaly the directory path
     */

    public JTextField createTextField() {
        JTextField text = new JTextField();
        text.setPreferredSize(favoriteDim);
        text.setMinimumSize(favoriteDim);
        text.setHorizontalAlignment(JTextField.LEFT);
        text.setEditable(true);
        text.setBackground(Color.white);
        text.setEditable(false);

        text.setInputVerifier(new InputVerifier() {
                public boolean verify(JComponent input) {
                    return false;
                }
            });

        return text;
    }
    
    private void buildSelectedFileCountLabel(){
        selectedFileCountLabel = new JLabel("", JLabel.CENTER);
        selectedFileCountLabel.setBackground(alertColor);
        selectedFileCountLabel.setOpaque(true);
        selectedFileCountLabel.setVisible(false);       
        selectedFileCountLabel.setPreferredSize(new Dimension(80, 25));
        selectedFileCountLabel.setMinimumSize(new Dimension(80, 25));
        selectedFileCountLabel.setForeground(Color.black);
    }

    /*
     * create the "finish-cancel" pannel at the bottom of the dialog
    */

    public JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        controlPanel.setPreferredSize(new Dimension(dialogWidth, 40));

        controlPanel.add(finishButton);
        controlPanel.add(cancelButton);

        return controlPanel;
    }

    /*
     * create the tip panel atop the dialog
     */

    public JPanel createTipPanel() {
        JPanel tipPanel = new JPanel();
        tipPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        tipPanel.setPreferredSize(new Dimension(dialogWidth, 40));

        JLabel tipLabel =
            new JLabel("Select EDF source directory/files and output directory");
        tipLabel.setHorizontalAlignment(JLabel.LEADING);
        tipPanel.add(tipLabel);
        tipPanel.setBackground(alertColor);
        tipPanel.setBorder(BorderFactory.createEtchedBorder());

        return tipPanel;
    }

    /**
     * Action listern for srcFilesBrowseBtn
     * Algorithm is:
     * (1) select files according to the selection mode;
     * (2) update the srcFilesDirField and outputDirField
     * (3) enable the output directory panle
     */
    private class BrowseSourceFilesBtnListener extends MouseAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        private void performActions(){
            if (dirRadio.isSelected()){
                browseBySourceDir();
                MainWindow.setSelectionMode(MainWindow.selection_mode_by_dir);
            }
            else{
                browseBySourcFile();
                MainWindow.setSelectionMode(MainWindow.selection_mode_by_file);
            }
        }
        
        private void browseBySourceDir() {
        	
        	EDFFileFilter filter =  new EDFFileFilter(new String[] { "" }, "(Directory)");
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(Keywords.fileFolderChooser.getCurrentDirectory());
            chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //this selection mode guarrentees the selection of directories.
            chooser.setDialogTitle("Select Source Directory for EDF Files");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return;
            // evaluate source directory;
            sourceDirectory = chooser.getSelectedFile();
            Keywords.fileFolderChooser = chooser;
        	
            File[] dirList = sourceDirectory.listFiles();
            
            ArrayList<File> listOfFiles = new ArrayList<File>();
            ArrayList<File> listOfDirectories = new ArrayList<File>();
            for (int i = 0; i < dirList.length; i++) {
                if (dirList[i].isFile()) {
                    String str = dirList[i].getName().toLowerCase();
                    if (str.endsWith(".edf")) {
                        listOfFiles.add(dirList[i]);
                    }
                }
                if (dirList[i].isDirectory()) {
                    listOfDirectories.add(dirList[i]);
                }
            }
            
            for (int i = 0; i < listOfDirectories.size(); i++) {
                dirList = listOfDirectories.get(i).listFiles();
                for (int j = 0; j < dirList.length; j++) {
                    if (dirList[j].isFile()) {
                        String str = dirList[j].getName().toLowerCase();
                        if (str.endsWith(".edf")) {
                            listOfFiles.add(dirList[j]);
                        }
                    }
                }
            }

            sourceFiles = new ArrayList<File>(listOfFiles.size());
            for (int i = 0; i < listOfFiles.size(); i++) {
                sourceFiles.add(listOfFiles.get(i));
            }

             /* 
              * need to check if there is any name collision.
             * Fangping, 08/16/2010
             */
            File tempWkDirectory = Utility.parseDirNameCollision(sourceDirectory, defaultDirectoryName);            
            workingDirField.setText(tempWkDirectory.getAbsolutePath()); //(workingDirectory.toString());
            
            activateSelectedCounterLabel(true);
            srcFilesDirField.setText(sourceDirectory.toString());
            enableOutputDirPanel(true);
            finishButton.setEnabled(true);
        }


        private void activateSelectedCounterLabel(boolean active) {
            String text = sourceFiles.size() + " EDF files are selected. Files may have been renamed.";
            selectedFileCountLabel.setText(text);
            selectedFileCountLabel.setVisible(active);
            selectedFileCountLabel.setEnabled(active);
        }


        private void browseBySourcFile() {            
            enableOutputDirPanel(false);
            finishButton.setEnabled(false);
            
            EDFFileFilter filter =
                new EDFFileFilter(new String[] {extName}, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(Keywords.fileFolderChooser.getCurrentDirectory());
            chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //this selection mode guarrentees the unselection of directories.
            chooser.setDialogTitle("Select EDF Files");
            Keywords.fileFolderChooser = chooser;

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return;
            File[] choosedFiles = chooser.getSelectedFiles();
            int sz = choosedFiles.length;
            sourceFiles = new ArrayList<File>(sz);
            
            for (int i = 0; i < choosedFiles.length; i++){
                   sourceFiles.add(choosedFiles[i]);  
            }
            
            // evaluate source directory;
            sourceDirectory = sourceFiles.get(0).getParentFile();
            selectedFileCountLabel.setText("(" + sourceFiles.size() +
                                           " EDF files are selected)");
            //selectedFileCountLabel.setHorizontalAlignment(JLabel.CENTER);
            selectedFileCountLabel.setVisible(true);

            srcFilesDirField.setText(sourceDirectory.toString());
            
            File tempWkDirectory = Utility.parseDirNameCollision(sourceDirectory, defaultDirectoryName);            
            workingDirField.setText(tempWkDirectory.getAbsolutePath()); 
            
            //workingDirField.setText(workingDirectory.toString());
            enableOutputDirPanel(true);
            finishButton.setEnabled(true);
        }


        
        /*
         * do exactly the same as actionperformed
         */
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (dirRadio.isSelected())
                    browseBySourceDir();
                else
                    browseBySourcFile();
            }
        }
    }
    
    public void enableOutputDirPanel(boolean abool){
        yesOverwriteRadio.setEnabled(abool);
        noOverwriteRadio.setEnabled(abool);
        noOverwriteRadio.setSelected(abool);
        ooutputDirBrowseBtn.setEnabled(abool);
        workingDirField.setEnabled(abool);           
    }
    
 
    private class BrowseWorkingDirButtonListener extends MouseAdapter implements ActionListener {
        //only valid when overwrite = false, that is, nooverwrite == false
        private void selectDir() {
            JFileChooser chooser = new JFileChooser();
            EDFFileFilter filter =
                new EDFFileFilter(new String[] { "" }, "(Directory)");
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //this selection mode guarantees the selection of directories.
            chooser.setDialogTitle("Select Directory to Store Task");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return;
            File workingDirectory = chooser.getSelectedFile();
            //workingDirectory = Utility.checkDirNameCollision(workingDirectory, "");
            //System.out.println(workingDirectory.toString());
            workingDirField.setText(workingDirectory.toString());
        }

        public void actionPerformed(ActionEvent e) {
            if (noOverwriteRadio.isSelected()){
                selectDir();
            }
        }

        public void mouseClicked(MouseEvent e) { // handle double click
            if (yesOverwriteRadio.isSelected())
                return;
            if (e.getClickCount() == 2)
                selectDir();
        }
    }

    private class FinishButtonListener implements ActionListener {        
        public void actionPerformed(ActionEvent e) {
            performActions();            
        }
        
        public void performActions(){
            if (sourceFiles != null) {
                createWorkingDirectory();
                renewFileRecords();
                yieldNewEDFHeaders();
                yieldEiaTable();
                yieldEsaTables();
                updatePrimaryTabs();
                updateTaskTreeWkfileNodes();
                
                printMessageToConsole();
                printMessageToInfopane();
                //cleanupErorListTable();                
                                             
                boolean active = true;
                activateMenuItems(active);
                activateToolBarItems(active);
                displayMainTab(active);
                
                parseTaskFiles();

                dispose();
            }
        }
        

        /*
         * create the workingDirectory
         * since workingDirectory is created only if this dir does not exist, we
         * need not worry about this method may destroy other files already there.
         * Fangping, 08/21/2010
         */
        public void createWorkingDirectory() {
            //create only not at overwrite mode.
            if (!overwriteMode) {
                String dirName = workingDirField.getText().trim();
                workingDirectory = new File(dirName);
                workingDirectory.mkdir();
            }
            else
                workingDirectory = null;
        }

        /**
         * re-register a new group of EDF files
         * name collision check Fangping, 08/20/2010
         */
        private void renewFileRecords() {                  
            File wkDirectory = workingDirectory;
            
            ArrayList<File> WkEdfFiles = new ArrayList<File>(); 
                       
            if (!overwriteMode)
                WkEdfFiles = Utility.copyFilestoDirectory(sourceFiles, workingDirectory);       
            else{
                wkDirectory = null; // this line is redundant
                for (File file : sourceFiles)
                    WkEdfFiles.add(file);
            }
            
            MainWindow.setWorkingDirectory(workingDirectory);
            MainWindow.setSourceDirectoryText(sourceFiles.get(0).getParent());
            MainWindow.setSrcEdfFiles(sourceFiles);
            MainWindow.setWkEdfFiles(WkEdfFiles); 
        }

        /**
         * create the EIA and ESA headers;
         */
        private void yieldNewEDFHeaders() {
            int numberOfOpenedFiles = sourceFiles.size();
            ArrayList<EDFFileHeader> headers = new ArrayList<EDFFileHeader>(numberOfOpenedFiles);
            ArrayList<EDFFileHeader> dupHeaders = new ArrayList<EDFFileHeader>(numberOfOpenedFiles);
            MainWindow.srcEdfFileHeaders = new ArrayList<EDFFileHeader>(numberOfOpenedFiles);
            
            //read each file to build headers
           
            File currentFile;
            for (int i = 0; i < numberOfOpenedFiles; i++) {
                 currentFile = sourceFiles.get(i);
                try {
                    RandomAccessFile raf =
                        new RandomAccessFile(currentFile, "r");
                    if (raf == null)
                        return;
                    headers.add(i, new EDFFileHeader(raf, currentFile, false));
                    raf = new RandomAccessFile(currentFile, "r");
                    dupHeaders.add(i, new EDFFileHeader(raf, currentFile, false));
                } catch (IOException f) {
                    JOptionPane.showMessageDialog(null,
                                                  "File invalid: wrong format or empty. ",
                                                  "Data read error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
            MainWindow.setSrcEdfFileHeaders(headers);
            MainWindow.setDupEdfFileHeaders(dupHeaders);
        }


        /**
         * build the eia Table
         */
        private void yieldEiaTable() {
            int numberOfOpenedFiles = sourceFiles.size();
            MainWindow.iniEiaTable =
                    new EIATable(MainWindow.getSrcEdfFileHeaders(),
                                 numberOfOpenedFiles);
            MainWindow.iniEiaTable.setUpdateSinceLastSave(true); //the initial update status should be true
            MainWindow.iniEiaTable.setSavedOnce(false);

            MainWindow.iniEiaTable.setMasterHeaderCategory(EDFTable.MasterHeaderCategory.EIA_WORKSET); //obsolete line
            //MainWindow.setDupEiaTable(MainWindow.iniEiaTable);
        }
        
        /**
         * construct esa Tables
         * one esa header corresponds to one esa table
         * algorithm is:
         * 1. acquire the eiaHeader of the current file;
         * 2. construct the ESA table one channel after another;
         * 3. update the status.
         */
        private void yieldEsaTables() {
            int numberOfOpenedFiles = sourceFiles.size();
            ArrayList<ESATable> esaTables = new ArrayList<ESATable>(numberOfOpenedFiles);
            
            ESAHeader esaHeader;
            ESATable table;
            for (int i = 0; i < numberOfOpenedFiles; i++) {
                esaHeader = MainWindow.srcEdfFileHeaders.get(i).getEsaHeader(); //1.
                table = new ESATable(esaHeader, true); 
                esaTables.add(i, table); //2.
                // configure the status
                Boolean savedOnce = false; // start of 3.
                Boolean updateSinceLastSave = true;
                File workingFile = MainWindow.getWkEdfFiles().get(i);
                int cat = EDFTable.MasterHeaderCategory.ESA_WORKSET;
                table.setStatesAllInOne(savedOnce, updateSinceLastSave, workingFile, cat, i); //end of 4.
                table.setSourceMasterFile(sourceFiles.get(i)); // set source file
            }

            MainWindow.setIniEsaTables(esaTables);
            MainWindow.setDupEsaTables(esaTables);
        }
        
        
        void printEsaHeader(ESAHeader header){
            
        }

        /**
         * update/replace nodes under workingDir node
         * passed test. -- Fangping 02/23/10, 10:55pm
         */
        private void updateTaskTreeWkfileNodes() {
            TaskTree tree = MainWindow.taskTree;
            tree.removeNodeGroupAt(MainWindow.workingDirNode);
            MainWindow.workingDirNode.setUserObject("EDF Files" + " ( " + MainWindow.wkEdfFiles.size() + " files )");
            tree.addNodeGroupAt(MainWindow.workingDirNode, MainWindow.wkEdfFiles);
        }

        private void updatePrimaryESATab() {
            if (MainWindow.tabPane.isPrimaryTabsOpened()) {
                MainWindow.tabPane.removeTabAt(1);
            }

            String ft = "Signal Header"; // 

            ESATable esaTable = MainWindow.getIniEsaTables().get(0);
            WorkingTablePane pane = new WorkingTablePane(esaTable);
            pane.setMasterFile(sourceFiles.get(0)); // register master file
            MainWindow.tabPane.insertTab(ft, null, pane, null, 1);
            MainWindow.tabPane.setToolTipTextAt(1, "signal headers of EDF files");

            MainWindow.tabPane.setPrimaryTabsOpened(true);
        }

        private void updatePrimaryEIATab() {
            if (MainWindow.tabPane.isPrimaryTabsOpened()) {
                MainWindow.tabPane.removeTabAt(0);
            }
            
            String ft = "Identity Headers";

            EIATable eiaTable = MainWindow.getIniEiaTable();
            WorkingTablePane pane = new WorkingTablePane(eiaTable);
            
            MainWindow.tabPane.insertTab(ft, null, pane, null, 0);
            MainWindow.tabPane.setToolTipTextAt(0, "identity attribute headers of EDF files");

            //MainWindow.tabPane.setPrimaryTabsOpened(true);
        }

        /**
         * update primary tabs
         */
        private void updatePrimaryTabs() {
            updatePrimaryEIATab();
            updatePrimaryESATab();
        }
        
        private void printMessageToConsole(){
            int sz = MainWindow.srcEdfFiles.size();
            MainWindow.consolePane.setText("");
            String srcdir = MainWindow.srcEdfFiles.get(0).getParentFile().getAbsolutePath();
            String wkdir =  MainWindow.wkEdfFiles.get(0).getParentFile().getAbsolutePath();
            
            theme = Utility.currentTimeToString() + ": New task created. ";
            //for overwrite mode
            if (overwriteMode){
                theme = theme + "Source directory is: " + srcdir + ". OVERRIDE mode. \n";
                EDFInfoPane.printMessageHeader(theme);
                printTheme();
                String srcName; 
                for (int i = 0; i < sz; i++){
                    srcName = MainWindow.srcEdfFiles.get(i).getAbsolutePath();
                    content = "\tloading \"" + srcName + "\" \n";
                    printContent();
                }
                
                theme = "Loading completed. Current task contains " + sz + " EDF files. \n";
                printTheme();
                
                MainWindow.rightStatusBar.setText("Task Write Mode: OVERWRITE");
                return;
            }
                        
            //for dupliate mode
            
            theme = theme + "Source directory is: " + srcdir + ". Working directory is: " + wkdir + ".\n";
            //EDFInfoPane.printMessageHeader(theme);
            printTheme();
            String srcName, outputName;            
            for (int i = 0; i < sz; i++){
                srcName = MainWindow.srcEdfFiles.get(i).getAbsolutePath();
                outputName = MainWindow.wkEdfFiles.get(i).getAbsolutePath();
                content = "\tloading \"" + srcName + "\" to \"" + outputName + "\" \n";
                printContent();
            }
            
            theme = "Loading completed. Current task contains " + sz + " EDF files. \n";
            printTheme();
            
            MainWindow.rightStatusBar.setText("Task Write Mode: DUPLICATE");
        }
        
        private void printMessageToInfopane(){
            MainWindow.taskinfoEdtPane.outputTaskInfoWithHtml();
            MainWindow.taskTabPane.setSelectedIndex(1);
        }
        
        private void parseTaskFiles(){
            //cleanupErrorListTable();
            //must clear, otherwise, the data might displayed twice
            MainWindow.getEiaIncompliances().clear(); 
            MainWindow.getEsaIncompliances().clear();
            //update eia and esa incompliances
            validateEiaTable();
            validateEsaTable();
            outputValidationToErrorListTable(); 
            switchToErrorListTable();
        }
        
       
/*         public void cleanupErrorListTable(){
            MainWindow.getAggregateIncompliances().clear();
            ErrorListTable errorlist = MainWindow.getErrorListTable();
            errorlist = new ErrorListTable();
        } */
        
        public void validateEiaTable(){
            EIATable table = MainWindow.getIniEiaTable();
            MainWindow.setEiaIncompliances(table.parseEIATable());
        }
        
        public void validateEsaTable(){
            ArrayList<ESATable> tables = MainWindow.getIniEsaTables();
            ArrayList<Incompliance> incomps = new ArrayList<Incompliance>();
            ArrayList<Incompliance> inserted = new ArrayList<Incompliance>();
            for (int i = 0; i < tables.size(); i++){
                incomps = tables.get(i).parseESATable();
                for (Incompliance incomp: incomps)
                    inserted.add(incomp);
            }
            MainWindow.setEsaIncompliances(inserted);       
        }
        
        private void outputValidationToErrorListTable(){
            ErrorListTable errorTable = MainWindow.getErrorListTable();
            errorTable.blankOut();            
            ArrayList<Incompliance> incompliances = MainWindow.aggregateIncompliances();
            
            int count = incompliances.size();
            outputMessage(count);
                        
            if (count != 0)
                errorTable.yieldTableFrom(incompliances);
            
            ErrorListTable.setIcon(errorTable, count);
        }
        
        private void switchToErrorListTable(){
            if (MainWindow.getEiaIncompliances().size()!= 0 || 
                    MainWindow.getEiaIncompliances().size()!= 0)
                MainWindow.consoleTabPane.setSelectedIndex(1);
        }
        
        private void outputMessage(int count){
            if (count == 0){
                theme = "No incompliance found.\n";
                printTheme();
            }
            else{
                String placeholder = (count == 1)? " Incmnpliance ":  " Incompliances ";
                theme = count + placeholder + "Found. ";
                theme = theme + "Check the error list table for detail. \n";
                printTheme();
            }            
        }
        
        /*
         * corresponding to setup initial status of system in MainWindow
         */
        private void activateMenuItems(boolean active){
            MainWindow.fileCloseTaskItem.setEnabled(active);
            MainWindow.fileAddFilesItem.setEnabled(active);             

            MainWindow.fileExcludeFileItem.setEnabled(active);
            MainWindow.fileDeleteFileItem.setEnabled(active);
            MainWindow.fileRenameItem.setEnabled(active);
            MainWindow.toolApplyEIATemplateItem.setEnabled(active);
            MainWindow.toolApplyESATemplateItem.setEnabled(active);         
        }
        
        private void activateToolBarItems(boolean active){
            MainWindow.addFilesButton.setEnabled(active);  
            MainWindow.applyEIATemplateButton.setEnabled(active);
            MainWindow.applyESATemplateButton.setEnabled(active);
        }
        
        private void displayMainTab(boolean active){
            if (active)
                MainWindow.tabPane.setSelectedIndex(0);
        }
                
        

    } //end of FinishButtonListener class

    private class CancelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            dispose();
        }
    }

    private void setLogo() {
        BufferedImage image = null;
        try {
            image =
                    ImageIO.read(this.getClass().getResource("/icon/mimilogo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setIconImage(image);
    }
    
    private void printTheme(){
        try {
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
        } catch (BadLocationException e) {; }
    }
    
    private void printContent(){
            try {
                doc.insertString(doc.getLength(), content, EDFInfoPane.content);
            } catch (BadLocationException e) {; }
        
        }
    
    /*
     * work for switch of modes between dir and file selection.
     * Fangping, 08/23/2010
     */
    private void resetActiveAreas(){
        srcFilesDirField.setText("");
        enableOutputDirPanel(false);
        selectedFileCountLabel.setText("");
        selectedFileCountLabel.setEnabled(false);
        finishButton.setEnabled(false);
    }
    
}//end of NewTaskListener class






