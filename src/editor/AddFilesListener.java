package editor;

import header.EDFFileHeader;
import header.ESAHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import listener.EDFFileFilter;
import table.EDFTable;
import table.EIATable;
import table.ESATable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A JDialog that has the functionality of adding new files and updating the UI
 */
public class AddFilesListener extends JDialog {

	private static final long serialVersionUID = 1L;
	private static JButton browseButton;
    private static JButton finishButton;
    private static JButton cancelButton;

    private final static int dialogWidth = 500;
    private final static int dialogHeight = 500;
    private final static String extName = "edf";
    private final static String description = "EDF Files (*.edf)";
    private final static Dimension favoriteSize = new Dimension(80, 28);
    private static final Color alertColor = new Color(255, 240, 188);
    private final static String srctip = "Select one or mulitple EDF source files";
    private final static String helptip = "To add new files to current task, select source EDF files";
    @SuppressWarnings("unused")
	private final static String alerttip = " EDF files have been selected.";

    /*
     * workFiles is used to store the new file names for working
     * it facilitate renaming, message printing, etc.
     * it is initialized in renewFileRecords.
     * Fangping, 08/20/2010
     */ 
    protected ArrayList<File> sourceFiles;
    private File selectedFiles[]; // alias of sourceFiles; //Fangping, 08/23/2010
    private File sourceDirectory;
    
    //Fangping, 08/23/2010
    // list of selected to add yet already in the working directory, 
    // so that they cannot be added finally
    private ArrayList<Integer> multiaddedFileIndices;     
    private static CheckBoxList checkBoxList = new CheckBoxList();
   
    private static JPanel tipPanel;
    @SuppressWarnings("unused")
	private static JLabel tipLabel = new JLabel();
    // private static JLabel notificationLabel = new JLabel("", JLabel.LEFT);
    private JPanel edfPanel;
    @SuppressWarnings("unused")
	private JPanel selectionPane; // contains both browsePanel and edfPanel
    private JPanel controlPanel;
    protected static JTextField sourceFilesDirField = new JTextField();
    protected static JLabel selectedFilesLabel = new JLabel();
    protected static JLabel selectNoteLabel = new JLabel("Select one or more Files: ", JLabel.LEFT);
    
    static {
        Font oldFont = selectNoteLabel.getFont();
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() + 2);
        selectNoteLabel.setFont(newFont);
        selectNoteLabel.setFont(newFont);
        selectNoteLabel.setPreferredSize(favoriteSize);
        selectNoteLabel.setMinimumSize(favoriteSize);
        
        sourceFilesDirField.setPreferredSize(favoriteSize);
        sourceFilesDirField.setMinimumSize(favoriteSize);
        sourceFilesDirField.setHorizontalAlignment(JTextField.LEFT);
        
        selectedFilesLabel.setPreferredSize(favoriteSize);
        selectedFilesLabel.setMinimumSize(favoriteSize);
        selectedFilesLabel.setHorizontalAlignment(JLabel.CENTER);
        selectedFilesLabel.setVisible(false);
        selectedFilesLabel.setBackground(alertColor);
        selectedFilesLabel.setOpaque(true);
    }
    
    /**
     * Adds a mouse listener for dir field
     * @param frame parent window frame
     */       
    public AddFilesListener(JFrame frame) {
        super(frame, true); // modal
        this.setLocationRelativeTo(frame);
        
        initUI();
        setDialogLayout();
        visualize();
     }

    private void initUI() {
        this.setSize(new Dimension(dialogWidth, dialogHeight));
        
        buildSourceFileDirField();
        //buildSelectedFilesLabel();   
        buildFileBrowseButton();
        buildFinishButton();
        buildCancelButton();      
    }

    private void visualize() {
        this.setTitle("Add Files");
        setLogo();
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
    }

    private void setDialogLayout() {
        
        buildEdfPanel();
        buildTipPanel();
        buildControlPanel();
        
        this.add(tipPanel, BorderLayout.NORTH);
        this.add(edfPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
        
    }

    /**
     * Creates the selection panel of the window
     * @param title the title of this window
     * @param textField a JTextField for displaying the source file directory
     * @param button browse file button
     * @return the JPanel
     */
    public JPanel createSelectionPanel(String title, JTextField textField, JButton button) {
    	// used for GridLayout
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
        selectionPanel.add(containerPanel); // second layer

        JPanel secLayerPanel = new JPanel();
        if (layer == 3) {
            secLayerPanel.add(selectedFilesLabel);
            selectionPanel.add(secLayerPanel);
        }

        return selectionPanel;
    }

    /**
     * Creates the source file directory field and customizes it
     */
    public void buildSourceFileDirField() {
        sourceFilesDirField = new JTextField();
        // sourceFilesDirField.setPreferredSize(new Dimension(350, 25));
        // sourceFilesDirField.setMinimumSize(new Dimension(350, 25));
        // sourceFilesDirField.setHorizontalAlignment(JTextField.LEFT);
        sourceFilesDirField.setBackground(Color.white);
        sourceFilesDirField.setPreferredSize(favoriteSize);
        sourceFilesDirField.setMinimumSize(favoriteSize);
        sourceFilesDirField.setEditable(false);
        // sourceFilesDirField.setEnabled(true);
        sourceFilesDirField.addMouseListener(new BrowseSourceFilesButtonListenser());
    }

    private void buildFileBrowseButton() {
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(new BrowseSourceFilesButtonListenser());
    }

    private void buildFinishButton() {
        finishButton = new JButton("Finish");
        finishButton.setEnabled(false); //Fangping, 08/19/2010
        finishButton.addActionListener(new FinishButtonListener());
    }

    private void buildCancelButton() {
        cancelButton = new JButton("Cancel");
        InputMap im = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = cancelButton.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
        am.put( "Cancel", new CancelAction());        
        cancelButton.addActionListener(new CancelButtonListener());
    }
    
    /**
     * Builds the control panel that containing finish and cancel button
     */
    public void buildControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        controlPanel.setPreferredSize(new Dimension(dialogWidth, 40));

        controlPanel.add(finishButton);
        controlPanel.add(cancelButton);
    }
    
    /**
     * Builds the tip panel
     */
    public void buildTipPanel() {
        tipPanel = new JPanel();
        tipPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        tipPanel.setPreferredSize(new Dimension(dialogWidth, 40));

        JLabel tipLabel = new JLabel(helptip);
        tipLabel.setHorizontalAlignment(JLabel.LEADING);
        tipPanel.add(tipLabel);
        tipPanel.setBackground(alertColor);
        tipPanel.setBorder(BorderFactory.createEtchedBorder());
    }

    private class BrowseSourceFilesButtonListenser extends MouseAdapter implements ActionListener {
        public void performActions() {        
            selectedFiles = selectSourceFiles();
            if (selectedFiles.length == 0)
                return;
            
            sourceFiles = selectedFilesArrayToArayList();  
            getMultiaddedFileIndexList();
            // evaluate source directory;
            sourceDirectory = sourceFiles.get(0).getParentFile();
            showResultAreas(true);                       
        }
        
        private File[] selectSourceFiles() {
            EDFFileFilter filter =
                new EDFFileFilter(new String[] { extName }, description);
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
            chooser.setAcceptAllFileFilterUsed(false);//Fangping, 08/19/2010
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //this selection mode guarrentees the unselection of directories.
            chooser.setDialogTitle("Select EDF Files");
            
            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return null;
                      
            return chooser.getSelectedFiles();
        }

        private ArrayList<File> selectedFilesArrayToArayList() {
            int nfiles = selectedFiles.length;
            ArrayList<File> sourceFiles = new ArrayList<File>(nfiles);
            for (int i = 0; i < nfiles; i++)              
                sourceFiles.add(i, selectedFiles[i]);
            
            return sourceFiles;
        }

        private String getPromptMsg() {
            String text;
            int nfiles = sourceFiles.size();
            if (nfiles == 1)
                text = nfiles + " EDF file has been selected.";
            else 
                text = nfiles + " EDF files have been selected.";
            
            text = text + " Please verify your selection.";
            
            return text;
        }
        
        private void showResultAreas(boolean affirmative) {
            sourceFilesDirField.setText(sourceDirectory.toString());      
            updateCheckBoxList();
            activateBoxAndNotificationArea(affirmative, getPromptMsg());            
            finishButton.setEnabled(affirmative);  
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        /**
         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent e) {
             performActions();
        }
    }
    
    /**
     * A listener for FinishButton
     */
    private class FinishButtonListener implements ActionListener {
        private ArrayList<File> finalAddedFiles = new ArrayList<File>();
        private ArrayList<File> workFiles;

        private void performActions() {            
            if (sourceFiles == null || sourceFiles.size() == 0) {
                printNoFileAddedMsgToConsole();
                dispose();
                return;
            }
            
            acquiredFinalAddedFiles();
            
            // all added files have been loaded in the task
            if (finalAddedFiles.size() == 0)
                return;
            renewFileRecords();

            yieldNewEDFHeaders();
            yieldEiaTable();
            yieldEsaTables();
            updatePrimaryTabs();
            updateTaskTreeWkfileNodes();
            printMessageToConsole();

            MainWindow.tabPane.setSelectedIndex(0);
            MainWindow.taskinfoEdtPane.outputTaskInfoWithHtml();

            dispose();
        }
        
        // removed already opened files
        private void acquiredFinalAddedFiles() {
            finalAddedFiles = sourceFiles;
                
            ArrayList<Integer> rlist = reverseOrderOf(multiaddedFileIndices);
            for (Integer index: rlist) {
                finalAddedFiles.remove(index.intValue());
            }
        }
        
        /**
         * Helper of acquiredFinalAddedFiles();
         * reverse elements in the olist to facilitate remove opearation
         */
        private ArrayList<Integer> reverseOrderOf(ArrayList<Integer> olist) {
            if (olist == null)
                return null;
            
            int size = olist.size();
            if (size == 0)
                return olist;
            
            ArrayList<Integer> nlist = new ArrayList<Integer>(size);
            int temp;
            for (int i = 0; i < size; i++){
                temp = olist.get(size - i - 1);
                nlist.add(new Integer(temp));
            }
            
            return nlist;  
        }
        
        /**
         * Re-registers a new group of EDF files
         */
        private void renewFileRecords() {           
            MainWindow.addSrcEdfFiles(sourceFiles);
            File wkDirectory = MainWindow.getWorkingDirectory();           
            workFiles = new ArrayList<File>();
            
            String fullPath = "";
            String fileName, dirName = "";
            File clone;
            @SuppressWarnings("unused")
			File dirFile = null;
            if (wkDirectory != null){//!dir mode
                dirName = wkDirectory.getAbsolutePath();
                dirFile = new File(dirName);
            }
            
            for (File file: finalAddedFiles) {
                if (wkDirectory == null) {
                    fullPath = file.getAbsolutePath();
                    clone = new File(fullPath);
                } else {
                    fileName = file.getName();
                    fullPath = dirName + "/" + fileName;
                    clone = new File(fullPath);
                    //parse name collision for clone
                    clone = Utility.parseSingleFileNameCollision(clone, MainWindow.wkEdfFiles);
                    MainWindow.wkEdfFiles.add(clone);
                }     
                workFiles.add(clone);
            }           
        }
        
        /**
         * Creates the EIA and ESA headers;
         */
        private void yieldNewEDFHeaders() {
            if (finalAddedFiles == null || finalAddedFiles.size() == 0)
                return;
            ArrayList<EDFFileHeader> headers = new ArrayList<EDFFileHeader>();

            //read each file to build headers            
            for (File currentFile: finalAddedFiles) {
                try {
                    RandomAccessFile raf = new RandomAccessFile(currentFile, "r");
                    // following is dead code. removed by wei wang, 2014-7-15
//                    if (raf == null)
//                        return;
                    headers.add(new EDFFileHeader(raf, currentFile, false));
                } catch (IOException f) {
                    JOptionPane.showMessageDialog(null, 
                    		"File invalid file: wrong format or empty file. ", "Data read error", JOptionPane.ERROR_MESSAGE);
                }
            }       
            
            MainWindow.addSrcEdfFileHeaders(headers); 
            MainWindow.addDupEdfFileHeaders(headers);
        }
               

        /**
         * Builds the EIA Table
         */
        @SuppressWarnings("deprecation")
		private void yieldEiaTable() {
            int numberOfOpenedFiles = MainWindow.getSrcEdfFileHeaders().size(); 
            MainWindow.iniEiaTable = new EIATable(MainWindow.getSrcEdfFileHeaders(), numberOfOpenedFiles);
            MainWindow.iniEiaTable.setUpdateSinceLastSave(true); //the initial update status should be true
            MainWindow.iniEiaTable.setSavedOnce(false);

            MainWindow.iniEiaTable.setMasterHeaderCategory(EDFTable.MasterHeaderCategory.EIA_WORKSET); //obsolete line
        }
        
        /**
         * Constructs esa Tables
         * one esa header corresponds to one esa table
         */
        private void yieldEsaTables() {
//        	algorithm is:
//          1. acquire the eiaHeader of the current file;
//          2. construct the ESA table one channel after another;
//          3. update the status.
            int sz = MainWindow.srcEdfFileHeaders.size();
            ArrayList<ESATable> esaTables = new ArrayList<ESATable>(sz);
            
            System.out.println("sz = " + sz);
            for (int i = 0; i < sz; i++) {
                ESAHeader esaHeader = MainWindow.srcEdfFileHeaders.get(i).getEsaHeader();//1.
                esaTables.add(i, new ESATable(esaHeader, true));//2.
                // configure the status 
                Boolean savedOnce = false; // start of 3.
                Boolean updateSinceLastSave = true;
                File workingFile = MainWindow.wkEdfFiles.get(i);
                int category = EDFTable.MasterHeaderCategory.ESA_WORKSET;                
                esaTables.get(i).setStatesAllInOne(savedOnce, updateSinceLastSave, workingFile, category, i);//end of 4.
                esaTables.get(i).setSourceMasterFile(MainWindow.getSrcEdfFiles().get(i));// set source file  
            }
            
            MainWindow.setIniEsaTables(esaTables);
            MainWindow.setDupEsaTables(esaTables);
        }
        
                 
        /**
         * Updates/replaces nodes under workingDir node
         * passed test.  
         * @author Fangping 02/23/10, 10:55pm
         */
        private void updateTaskTreeWkfileNodes() {
            TaskTree tree = MainWindow.taskTree;
            tree.removeNodeGroupAt(MainWindow.workingDirNode);
            MainWindow.workingDirNode.setUserObject("EDF Files" + " ( " + MainWindow.wkEdfFiles.size() + " files )");
            tree.addNodeGroupAt(MainWindow.workingDirNode, MainWindow.wkEdfFiles);
                                //MainWindow.srcEdfFiles);                     
        }
        
        private void updatePrimaryESATab() {
            if ( MainWindow.tabPane.isPrimaryTabsOpened()) {
                MainWindow.tabPane.removeTabAt(1);
            }
            
            String ft = "Signal Attributes - " + MainWindow.getWkEdfFiles().get(0).getName();
                       
            ESATable esaTable = MainWindow.getIniEsaTables().get(0);
            WorkingTablePane pane = new WorkingTablePane(esaTable);
            pane.setMasterFile(finalAddedFiles.get(0)); // register master file
            MainWindow.tabPane.insertTab(ft, null, pane, null, 1);
            MainWindow.tabPane.setToolTipTextAt(1, "Signal Attributes of EDF Files");
            
            //since primary tabs have been dispalyed, set primaryTabsOpened true.
            MainWindow.tabPane.setPrimaryTabsOpened(true);   
        }
        
        private void updatePrimaryEIATab() {
            if ( MainWindow.tabPane.isPrimaryTabsOpened()){
                MainWindow.tabPane.removeTabAt(0);               
            }
            
            String ft = "Identity attributes";
            
/*             String ft = "<html><body leftmargin=10 topmargin=10 marginwidth=10" +
                "marginheight=10>De-identify</body></html>"; */
                       
            EIATable eiaTable = MainWindow.getIniEiaTable();
            WorkingTablePane pane = new WorkingTablePane(eiaTable);
            MainWindow.tabPane.insertTab(ft, null, pane, null, 0);
            MainWindow.tabPane.setToolTipTextAt(0, "Deidentify Attributes of EDF Files");
            
            //MainWindow.tabPane.setPrimaryTabsOpened(true);    
        }
        
        /**
         * Updates primary tabs
         */
        private void updatePrimaryTabs(){
            updatePrimaryEIATab();
            updatePrimaryESATab();
        }
               
        /**
         * Prints messages to console when finished adding files
         */
        public void printMessageToConsole() {
            int sz = sourceFiles.size();
            
            //print the theme
            String srcdir = " from directory " + sourceFiles.get(0).getParentFile().getAbsolutePath();
            Document doc = MainWindow.consolePane.getDocument();
            String grammar = (sz == 1)? " file " : " files ";   
            String theme = Utility.currentTimeToString() + ": ";
            theme = theme + "Adding " + sz + grammar + srcdir + " to current task. \n";
            try{
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);      
            }
            catch(Exception e){;}
            
            //print the action content
            
            String srcFileName, workFileName;
            for (int i = 0; i < sz; i++) {
                srcFileName = sourceFiles.get(i).getAbsolutePath();
                workFileName = workFiles.get(i).getAbsolutePath();
                try{
                    doc.insertString(doc.getLength(), "  adding \" " + srcFileName + "\" to \"" + workFileName + "\"\n", EDFInfoPane.content);
                }
                catch(Exception e){;}
            }

            try {
                doc.insertString(doc.getLength(), "Adding files is done. \n", EDFInfoPane.theme);
            } catch (BadLocationException e) {;}  
        }
        
        /**
         * Prints information about sourcefiles and workfiles to the console window.
         */
        public void printNoFileAddedMsgToConsole() {
            String theme = Utility.currentTimeToString() + ": ";
            theme = theme + "No file added, since ALL selected files have existed in current task. \n";
            Document doc = MainWindow.consolePane.getDocument();
            try {
                doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
            } catch (BadLocationException e) {;}
        }

        public void actionPerformed(ActionEvent e) {
            performActions();
        }   
    } //end of FinishButtonListener class
    
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
    
    /**
     * Cancels adding file action
     */
    @SuppressWarnings("serial")
	class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            dispose();
        }
    }
    
    private void setLogo() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/icon/mimilogo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setIconImage(image);
    }
    
    //Fangping:
    private ArrayList<Integer> geteMultiTbaFilesIndices(File[] tbaFiles,
                            /*to be added list*/ArrayList<File> aaList) /*already added list*/ {
        if (tbaFiles == null)
            return null;
        
        ArrayList<Integer> indices = new ArrayList<Integer>();

        String ofname, nfname;

        for (int i = 0; i < tbaFiles.length; i ++ )
            for (File ofile : aaList) {
                nfname = tbaFiles[i].getAbsolutePath();
                ofname = ofile.getAbsolutePath();
                if (nfname.equalsIgnoreCase(ofname)) {
                    indices.add(new Integer(i));
                    break;
                }
            } 
        return indices;
    }

    private void getMultiaddedFileIndexList() {
        multiaddedFileIndices = geteMultiTbaFilesIndices(selectedFiles, MainWindow.srcEdfFiles);
    }

    private void updateCheckBoxList() {
        int nfiles = selectedFiles.length;
        if (nfiles == 0)
            return;
        
        JCheckBox checks[] = new JCheckBox[nfiles];
        //checkBoxList = new CheckBoxList();//renew checkBoxList to clean up
        String fileName;
        for (int i = 0; i < nfiles; i++) {
            fileName = selectedFiles[i].getName();
            checks[i] = new JCheckBox(fileName);
            checks[i].setToolTipText(selectedFiles[i].getAbsolutePath());
            checks[i].setSelected(true);
        }
        
        checkBoxList.setListData(checks);
        checkBoxList.setEnabled(false); 
        //TODO: disable editing of the checklist
        
        if (multiaddedFileIndices != null) {
            int index;
            for (int i = 0; i < multiaddedFileIndices.size(); i++) {
                index = multiaddedFileIndices.get(i);
                checks[index].setEnabled(false);
            }
        }   
    }

    private void buildEdfPanel(){
        
        //6 cols, 6 rows
        FormLayout layout = new FormLayout("4dlu:n, f:p:g, 4dlu:n, l:p:n, f:p:g, 4dlu:n"/*cols*/, 
                                           "6dlu:n, p:n, 6dlu:n, p:n, 6dlu:n, p:n, 6dlu:n, f:80dlu:g, 6dlu"/*rows*/);
        edfPanel = new JPanel(layout);
        
        CellConstraints cc = new CellConstraints();
        JScrollPane scroller = new JScrollPane(checkBoxList);
        
        edfPanel.add(selectNoteLabel, cc.xy(2, 2));
        edfPanel.add(browseButton, cc.xy(4, 2));        
        edfPanel.add(sourceFilesDirField, cc.xywh(2, 4, 4, 1));        
        edfPanel.add(selectedFilesLabel, cc.xywh(2, 6, 4, 1));        
        edfPanel.add(scroller, cc.xywh(2, 8, 4, 1)); 
        
        edfPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        activateBoxAndNotificationArea(false, "");
    }

    private void activateBoxAndNotificationArea(boolean visible, String notetext) {
        selectedFilesLabel.setVisible(visible);
        checkBoxList.setVisible(visible);
        if (visible == true && notetext != ""){
            selectedFilesLabel.setText(notetext);
        }
    }

}//end of AddFilesListener class