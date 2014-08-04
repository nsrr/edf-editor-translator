package editor;

import header.EDFFileHeader;
import header.EIAHeader;
import header.ESAHeader;

import java.awt.BorderLayout;
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
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import listener.EDFFileFilter;
import net.miginfocom.swing.MigLayout;
import table.EIATable;

/**
 * A window that performs the applying template functionality
 */
@SuppressWarnings("serial")
public class ApplyTemplateListener extends JDialog {
	
	///////////////////////////////////////////////////////////////////
	// JComboBox to JComboBox<String>, removed one area of dead code //								
	// by wei wang, 2014-7-15										 //
	///////////////////////////////////////////////////////////////////
	
    private JButton applyButton;
    private JButton cancelButton; 
    private JButton selectAllButton; 
    private JButton unselectButton; 
    private CheckBoxList checkboxList; 
    private JRadioButton openedRadio; 
    private JRadioButton diskRadio;
    private JComboBox<String> templateFilesBox; 
    private JButton browseButton;
    private JTextField templateFileText; 
    
    private JPanel controlPanel;
    private JPanel edfPanel;
    private JPanel templatePanel;
    
    private final int dialogWidth = 500;
    private final int dialogHeight = 550;
    
    /*
     * a list of data related fields
     */
    private File templateFileselectFromDisk;
    
    private String templateType = "";
    private int choice = 0;
    private File templateFile;
    private ArrayList<File> selectedFiles = new ArrayList<File>();

    /**
     * Initializes this JDialog with a parent frame and a templateType string
     * @param frame the parent frame used
     * @param templateType the template type, eia or esa, to be applied
     */
    public ApplyTemplateListener(JFrame frame, String templateType) {
        super(frame, true); // modal
        this.templateType = templateType;
        // eia:0, esa:1
        choice = getTemplateChoice();
        
        String title;
        if (choice == 0)
            title = "Apply Identity Template";
        else
            title = "Apply Signal Template";
        
        this.setTitle(title);
        setLogo();
        this.setLocationRelativeTo(frame); 
        initUI();
        setDialogLayout();  
        // do not switch the order of visualize() and setDialogLayout();
        visualize(); 
    }

    /**
     * Initializes UI and registers a group of listeners
     */
    public void initUI() { // register a group of listener

        selectAllButton = new JButton("All");
        selectAllButton.addActionListener(new SelectButtonsListener());

        unselectButton = new JButton("None");
        unselectButton.addActionListener(new SelectButtonsListener());

        //fileCheckBox = new JCheckBox[200];

        templateFilesBox = new JComboBox<String>();

        templateFileText = new JTextField();  
        templateFileText.setEnabled(false);
        templateFileText.addMouseListener(new BrowseActionListener());
        checkboxList = createCheckBoxList();
        
        browseButton = new JButton("Browse...");
        browseButton.setEnabled(false);
        browseButton.addActionListener(new BrowseActionListener());
        
        diskRadio = new JRadioButton("From disk");
        diskRadio.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    browseButton.setEnabled(true);
                    templateFileText.setEnabled(true);
                    templateFilesBox.setEnabled(false);
                    repaint();
                }
            });
        
        openedRadio = new JRadioButton("From opened templates");
        openedRadio.setSelected(true);
        openedRadio.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    browseButton.setEnabled(false);
                    templateFileText.setEnabled(false);
                    templateFilesBox.setEnabled(true);
                    repaint();
                }
            });
        
        cancelButton = new JButton("Cancel");
        InputMap im = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = cancelButton.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
        am.put( "Cancel", new CancelAction());
        cancelButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        
        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ApplyButtonListener());
        
        templatePanel = createTemplatePanel(); 
        edfPanel = createEDFPanel();
        controlPanel = createControlPanel();
      
    }
    
    @SuppressWarnings("unused")
	private JScrollPane createScroller() {
        JScrollPane scroller = new JScrollPane(checkboxList);
        return scroller;
    }
    
    /**
     * Sets JDialog layout
     */
    public void setDialogLayout() {
        this.setLayout(new BorderLayout());
        this.getContentPane().add(templatePanel, BorderLayout.NORTH);
        this.getContentPane().add(edfPanel, BorderLayout.CENTER);
        this.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets the size and visualizes the UI
     */
    public void visualize() {   
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(dialogWidth, dialogHeight);
        this.setVisible(true);
        this.pack();
    }
    
    /**
     * Creates control panel
     * @return the control panel
     */
    public JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();        
        controlPanel.add(applyButton);
        controlPanel.add(cancelButton);        
        return controlPanel;
    }
    
    /**
     * Creates a JPanel to display selected EDF files list
     * @return a JPanel
     */
    public JPanel createEDFPanel() {
        JPanel edfFilesPanel = new JPanel();
        edfFilesPanel.setBorder(BorderFactory.createTitledBorder("Apply Template to Selected EDF Files"));
        MigLayout mig = new MigLayout("", "[left]5[]5[]5[400]",  "[]10[]");
        edfFilesPanel.setLayout(mig);
        edfFilesPanel.add(new JLabel("Select:"), "");
        edfFilesPanel.add(selectAllButton, "");
        edfFilesPanel.add(unselectButton, "wrap");        
        JScrollPane scroller = new JScrollPane(checkboxList);
        edfFilesPanel.add(scroller, "span, growx");   
        
        return edfFilesPanel;           
    }
    
    private CheckBoxList createCheckBoxList() {
        CheckBoxList boxList = new CheckBoxList();
        int nfiles;
        JCheckBox checks[]; // = new JCheckBox[nfiles];
        if (MainWindow.wkEdfFiles != null) {
            nfiles = MainWindow.wkEdfFiles.size();
            checks = new JCheckBox[nfiles];
            for (int i = 0; i < nfiles; i++) {
                File file = MainWindow.wkEdfFiles.get(i);
                String fileName = file.getName();
                checks[i] = new JCheckBox(fileName);
                checks[i].setToolTipText(file.getAbsolutePath());
                boxList.addCheckbox(checks[i]);
                checks[i].setSelected(true);
            }
        }
        
        return boxList;
    }
    
    private JPanel createTemplatePanel() {
        JPanel templatePanel = new JPanel();
        
        String title;
        if (choice == 0)
            title = "Select Identity Template Files";
        else
            title = "Select Signal Template Files";
        templatePanel.setBorder(BorderFactory.createTitledBorder(title));
        
        MigLayout mig = new MigLayout("", "[left]5[]5[400]",  "[]5[]10[]5[]");
        templatePanel.setLayout(mig);
        
        templatePanel.add(openedRadio, "span, growx, wrap");
        openedRadio.setSelected(true);
        
        if (choice == 0)
            setContentsOfEIATemplateFilesBox();
        else 
            setContentsOfESATemplateFilesBox();
        
        templatePanel.add(templateFilesBox, "span, growx, wrap");
        
        templatePanel.add(diskRadio, "");
        templatePanel.add(browseButton, "wrap");
        templatePanel.add(templateFileText, "span, growx");
        
        templateFileText.setEditable(false);
        
        ButtonGroup group = new ButtonGroup();
        group.add(diskRadio);
        group.add(openedRadio);      

        return templatePanel;
    }
    
    private void setContentsOfEIATemplateFilesBox() {
        if (MainWindow.EIATemplateFiles.isEmpty())
            return;
        for (File file: MainWindow.EIATemplateFiles) {
            String fileName = file.getAbsolutePath();
            templateFilesBox.addItem(fileName);
        }
    }
    
    private int getTemplateChoice() {
        if (templateType.equalsIgnoreCase("eia"))
            return 0;
        else 
            return 1;
    }
    
    private void setContentsOfESATemplateFilesBox() {
        if (MainWindow.ESATemplateFiles.isEmpty())
            return;
        
        for (File file: MainWindow.ESATemplateFiles) {
             String fileName = file.getAbsolutePath();
            templateFilesBox.addItem(fileName);
        }
    }

    /**
     * Initializes GUI and sets the layout
     * @param e an ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        if (MainWindow.wkEdfFiles == null)
            return; 
       
        initUI();
        visualize(); 
        setDialogLayout();  

        // do not switch the order of visualize() and setDialogLayout(); 
    }

    /**
     * Gets the selected template header
     * @param choice template type
     * @return either eia or esa template header, depending on the templateType
     */
    public Object getSelectedTemplateHeader(int choice) {

        // acquire the tempalte file name;
        File selection = null;
        Object currentPane;

        //case 1: diskRadio is enabled;
        if (diskRadio.isSelected()) {
            String fileName = templateFileText.getText();
            selection = new File(fileName);
            //initilaize tempalteFile, Fangping, 08/20/2010
            templateFile = selection;

            RandomAccessFile raf = null;
            EDFFileHeader header = null;
            try {
                raf = new RandomAccessFile(selection, "r");
                header = new EDFFileHeader(raf, selection, true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (choice == 0)
                return header.getEiaHeader();
            else
                return header.getEsaHeader();
        }

        // case 2: openedRadio is enabled
        if (openedRadio.isSelected()) {
            String fileName = (String)templateFilesBox.getSelectedItem();
            switch (choice) {
            case 0: // case 2.1: eia template to be applied
                //2.1.1 find the File for the assigned name
                label1:
                for (File file : MainWindow.EIATemplateFiles) {
                    if (file.getAbsolutePath().equals(fileName)) {
                        selection = file;
                        templateFile = selection;
                        break label1;
                    }
                }
                
                //2.1.2 construct the eia header
                EIATemplatePane eiaPane;
                EIAHeader eiaTemplateHeader;
                for (int i = 0; i < MainWindow.tabPane.getTabCount(); i++) {
                    currentPane = MainWindow.tabPane.getComponentAt(i);
                    if (currentPane instanceof EIATemplatePane) {
                        eiaPane = (EIATemplatePane)currentPane;
                        if (eiaPane.getMasterFile() == templateFile){
                            eiaTemplateHeader = eiaPane.eiaHeaderFromPreviewTable();
                            return eiaTemplateHeader;
                        }
                    }
                }
                break;

            case 1: // case 2.2: esa template to be applied
                //2.2.1 find the File corresponding to the choice
                label2:
                for (File file : MainWindow.ESATemplateFiles) {
                    if (file.getAbsolutePath().equals(fileName)) {
                        selection = file;
                        //initilaize the templateFile
                        templateFile = selection;
                        break label2;
                    }
                }

                // 2.2.2 construct the esa Header
                ESATemplatePane esaPane;
                ESAHeader esaTemplateHeader;
                for (int i = 0; i < MainWindow.tabPane.getTabCount(); i++) {
                    currentPane = MainWindow.tabPane.getComponentAt(i);
                    if (currentPane instanceof ESATemplatePane) {
                        esaPane = (ESATemplatePane)currentPane;
                        if (esaPane.getMasterFile() == templateFile) {
                            esaTemplateHeader = esaPane.esaHeaderFromEsaTemplateTable();
                            return esaTemplateHeader;
                        }
                    }
                }
                break;
            default:
                // do nothing so far
            }
        }

        //when nothing is found
        return null;
    }

    /**
     * A browse EIA or ESA template files listener
     */
    class BrowseActionListener extends MouseAdapter implements ActionListener {
        
        public void performActions() {
            String extName, description, chooserDialogTitle;
            if (choice == 0){
                extName = "eia";
                description = "EDF Identity Attribute Files (*.eia)";
                chooserDialogTitle = "Select EIA File";
            } else {
                extName = "esa";
                description = "EDF Signal Attribute Files (*.esa)";
                chooserDialogTitle = "Select ESA File";
            }
                           
            EDFFileFilter filter = new EDFFileFilter(new String[] { extName }, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);//Fangping, 08/19/2010
            chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //this selection mode guarrentees the unselection of directories.
            chooser.setDialogTitle(chooserDialogTitle);

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return;
            templateFileselectFromDisk = chooser.getSelectedFile();
            templateFileText.setText(templateFileselectFromDisk.getPath());              
        }
        
        /**
         * Actions performed when clicking the browse button
         */
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        /**
         * Mouse click the browse button and performs actions
         */
        public void mouseClicked(MouseEvent e) {
            performActions();
        }           
    }
    
    /**
     * An ActionListener used to select all EDF files on clicking "All" button
     */
    class SelectButtonsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            checkboxList.SetAllCheckBoxSelected(source.equals(selectAllButton));
            repaint(); /* this is demanded */          
        }
    }

    /**
     * An ActionListener used to apply template selected to selected EDF files
     */
    class ApplyButtonListener implements ActionListener {

        /**
         * Maps EIA Header to edf files and then update the header table
         * @param eiaTemplateHeader eia template header
         * @param indices the select files's indices
         */
		private void mapeiaHeaderToEDFFiles(EIAHeader eiaTemplateHeader, ArrayList<Integer> indices) {
            if (eiaTemplateHeader == null)
                return;
            
            selectedFiles = new ArrayList<File>();
            
            EIAHeader eiaHeader;
            EIATable etable = MainWindow.getIniEiaTable();
            int rowIndex;
            for (int i = 0; i < indices.size(); i++) {
                rowIndex = indices.get(i);
                // initialize the selectedFiles
               
                selectedFiles.add(MainWindow.wkEdfFiles.get(rowIndex)); 
               
                //do not remove the next line.
                //eiaHeader = MainWindow.srcEdfFileHeaders.get(idx).getEiaHeader();
                eiaHeader = new EIAHeader(etable, rowIndex);

                // Dead code: commented out by wei wang, 2014-7-15
//                if (eiaHeader == null) {
//                    System.out.println("!! edfFileHeader is null");
//                    return;
//                }
                Utility.mapEIAHeader(eiaHeader, eiaTemplateHeader);
                
                // renew the rowIndex-th row of the eia table
                etable.updateTableRow(eiaHeader, rowIndex);
            }   
        }
        
        /**
         *map esa header to edf files and then update all tables
         * @param esaTemplateHeader
         * @param indices
         * Fangping, 08/18/2010, 06:32pm
         */        
        private void mapesaHeaderToEDFFiles(ESAHeader esaTemplateHeader, ArrayList<Integer> indices) {
            if (esaTemplateHeader == null) {
                return;
            }
            
            selectedFiles = new ArrayList<File>();
            
            ESAHeader edfFileHeader;
            for (int i = 0; i < indices.size(); i++) {
                int idx = indices.get(i);
                edfFileHeader = new ESAHeader(MainWindow.iniEsaTables.get(idx), false);    
                //initialize the selectedFiles, Fangping, 08/20/2010
                selectedFiles.add(MainWindow.wkEdfFiles.get(idx));
                //modify the ESAFileHeader in accordance to esaTemplatHeader
                Utility.mapESAHeader(edfFileHeader, esaTemplateHeader);
                
                // renew the idx-th row of the eia table
                MainWindow.iniEsaTables.get(idx).updateTable(edfFileHeader);

            }
        }
        
        /**
         * Applies EIA or ESA template when clicking the "Apply" button
         */
        public void actionPerformed(ActionEvent e) {
            // acquire the selected edf files
            ArrayList<Integer> selectedindices = checkboxList.getSelectedCheckBox();

            switch (choice) {
            case 0: // case 1: apply eia template
                // acquire the template header;
                EIAHeader eiaTemplateHeader = (EIAHeader)getSelectedTemplateHeader(choice);
                // apply template to each edf file and update the eia header table
                mapeiaHeaderToEDFFiles(eiaTemplateHeader, selectedindices);               
                break;
            case 1:  // case 2: apply esa template
                ESAHeader esaTemplateHeader = (ESAHeader)getSelectedTemplateHeader(choice);   
               
                //esaHeader.printEsaHeaderToScreen();// start debugging here              
                mapesaHeaderToEDFFiles(esaTemplateHeader, selectedindices); 
                break;
            default:
                System.out.println("not supposed coming up");//do nothing
            }
            
            //print message to the console
            printMessageToConsole();
            System.out.println("!!*&*&*&");
            dispose();
            System.out.println("!!*&*&*&");
        }
    }//end of ApplyButtonListener
    
    private void printMessageToConsole() {
        String theme = "";
        int sz = selectedFiles.size();
        String grammar = (sz == 1)? " file ": " files ";
        Document doc = MainWindow.consolePane.getDocument();
        // customize the string theme
        switch(choice){
        case 0:
            theme = "Apply EIA template to " + sz + grammar;
            theme = theme + ". EIA template file is: " + templateFile.getAbsolutePath() + "\n";          
            break;
        case 1:
            theme = "Apply ESA template to " + sz + grammar;
            theme = theme + ". ESA template file is: " + templateFile.getAbsolutePath() + "\n";  
        }

        try {
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
        } catch (BadLocationException e) {
        }
        String content;
        for(File file: selectedFiles){
            content = "  apply to " +  file.getAbsolutePath() + "\n";
            try {
                doc.insertString(doc.getLength(), content, EDFInfoPane.content);
            } catch (BadLocationException e) {
            }
        }

        try {
            doc.insertString(doc.getLength(), "Applying template is done. \n", EDFInfoPane.theme);
        } catch (BadLocationException e) {
        }
    }
    
    /**
     * Cancels the actions
     */
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
}
