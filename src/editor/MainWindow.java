package editor;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import header.EDFFileHeader;
import header.EIAHeader;
import header.ESAHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import javax.xml.parsers.ParserConfigurationException;

import listener.EDFFileFilter;

import org.xml.sax.SAXException;

import table.EDFTable;
import table.EIATable;
import table.EIATemplateTable;
import table.ESATable;
import table.ESATableModel;
import table.ESATemplateTable;
import table.ESATemplateTableModel;
import table.ErrorListTable;
import table.Incompliance;
import translator.gui.SubWindowGUI;
import validator.fix.ErrorFix;
import validator.fix.ErrorTypes;

//import jEDF.JEDF.JEDFMainWindow;


public class MainWindow extends JFrame implements WindowListener {

	public static String log = "log.txt";
	
    /////////////////////////////////////////////////////////////////////////
    //////////////// START of menu and menu items list //////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    //File menu items
    private JMenuItem fileNewTaskItem;
    protected static JMenuItem fileCloseTaskItem;
    protected static JMenuItem fileAddFilesItem;
    protected static JMenuItem fileExcludeFileItem;
    protected static JMenuItem fileDeleteFileItem;

    
    protected static JMenuItem fileSaveItem;
    protected static JMenuItem fileSaveAsItem;
    protected static JMenuItem fileRenameItem;
    protected static JMenuItem fileSaveAllItem;
    protected static JMenuItem fileExportItem;
    protected static JMenuItem filePrintItem;
    //private JMenuItem fileCloseAllItem;
    protected static JMenuItem fileExitItem;

    //Edit menu items
    protected static JMenuItem editUndoItem;
    protected static JMenuItem editRedoItem;
    protected static JMenuItem editCutItem;
    protected static JMenuItem editCopyItem;
    protected static JMenuItem editPasteItem;
    protected static JMenuItem editDiscardChangesItem;

    //template menu items
    protected static JMenuItem templateNewEIAItem;
    protected static JMenuItem templateOpenEIAItem;
    protected static JMenuItem templateImportEIAItem;

    protected static JMenuItem templateNewESAItem;
    protected static JMenuItem templateOpenESAItem;
    protected static JMenuItem templateImportESAItem;

    protected static JMenuItem toolFixTableErrorItem;
    
    protected static JMenuItem templateAddRowItem;
    protected static JMenuItem templateRemoveRowItem;
    
    //Tools Menu Items
    protected static JMenuItem toolApplyEIATemplateItem;
    protected static JMenuItem toolApplyESATemplateItem;
    protected static JMenuItem toolValidateTableItem;
    protected static JMenuItem annotConverterItem;
    protected static JMenuItem jEDFToolItem;
    protected static JMenuItem EDFViewerToolItem;

    // Help menu items
    protected static JMenuItem helpAboutItem;
    protected static JMenuItem helpManualItem;
    protected static JMenuItem helpMimiItem;
    protected static JMenuItem helpEdfItem;
    protected static HelpBroker hb;
    protected static HelpSet hs;

    ///////////////////////////////////////////////////////////////////////////////
    //////////////// END of menu and menu items list //////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////
    //////////////// START of ToolBar buttons   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /*
     * ToolBar buttons
     */
    protected static JButton newTaskButton;
    //Fangping, 08/09/2010
    protected static JButton addFilesButton;
     
    protected static JButton saveButton;
    protected static JButton saveAllButton;
    protected static JButton printButton;
    //protected static JButton exportButton;

    protected static JButton undoButton;
    protected static JButton redoButton;
    protected static JButton cutButton;
    protected static JButton copyButton;
    protected static JButton pasteButton;
    protected static JButton applyEIATemplateButton;
    protected static JButton applyESATemplateButton;
    protected static JButton addRowButton;
    protected static JButton removeRowButton;
    protected static JButton verifyBtn;
    protected static JButton priorMatchBtn;
    protected static JButton nextMatchBtn;
    protected static JTextField searchTf;
    protected static JLabel searchCount;;
    protected static JButton searchPreviousBtn;
    protected static JButton searchNextBtn;
    protected static JButton helpButton;
    
    protected static Point searchCursor; // store the table search cursor
    //this table stores the latest searched table
    protected static EDFTable latestSearchedTable = new EDFTable();
    ///////////////////////////////////////////////////////////////////////////////
    //////////////// END of ToolBar buttons   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    ///////////////// START of static member zone ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final String logo = "PhysioMIMI EDF Header Editor";

    protected static JLabel leftStatusBar;
    protected static JLabel middleStatusBar;
    protected static JProgressBar saveProgressBar;
    protected static JLabel rightStatusBar;
    protected static JPanel statusBars = new JPanel();

    protected static EDFTabbedPane tabPane; // for tabList
    protected static JTabbedPane taskTabPane = new JTabbedPane(JTabbedPane.TOP);
    protected static JTabbedPane consoleTabPane = new JTabbedPane(JTabbedPane.BOTTOM);
    
    protected static String sourceDirectoryText; // source diretory
    protected static File workingDirectory; // work directory

    protected static EIATable iniEiaTable = null; // initial EIA table
    protected static EIATable dupEiaTable = null; // clone iniEiaTable
    
    protected static EDFUndoManager undoManager = new EDFUndoManager();

    // store currently active ESA table
    protected static ESATable activeESATableInTabPane = null; 
    
    protected static ArrayList<File> srcEdfFiles; // selected Files
    protected static ArrayList<File> wkEdfFiles; // the file in myWork directory
    protected static ArrayList<ESATable> iniEsaTables = null; // initial EIA table
    protected static ArrayList<ESATable> dupEsaTables = null; // clone iniEsaTable
    protected static ArrayList<EDFFileHeader> srcEdfFileHeaders = null;
    protected static ArrayList<EDFFileHeader> dupEdfFileHeaders = null; // clone srcEdfFileHeaders
    protected static ErrorListTable errorListTable = new ErrorListTable();
    
    /*
     * five cases of incompliances: esa, esatemplate, eia, eiatemplate, and the aggregate of these
     * When aggregate these four types of incompliances, the order had better be: (1) esa, (2) esaTemplate;
     * (3) eia, (4) eiaTemplate. In this way, insertion, deletion and searching in the aggregation can 
     * run more efficiently.
     * Fangping, 10/11/2010    
     */    
    //protected static ArrayList<Incompliance> aggregateIncompliances = new ArrayList<Incompliance>();
    protected static ArrayList<Incompliance> esaIncompliances = new ArrayList<Incompliance>();
    protected static ArrayList<Incompliance> esaTemplateIncompliances = new ArrayList<Incompliance>();
    protected static ArrayList<Incompliance> eiaIncompliances = new ArrayList<Incompliance>();
    protected static ArrayList<Incompliance> eiaTemplateIncompliances = new ArrayList<Incompliance>();
    
    
    /* keep track of opened eia templates */
    protected static ArrayList<File> EIATemplateFiles = new ArrayList<File>(30); 
    /* keep track of opened esa templates */
    protected static ArrayList<File> ESATemplateFiles = new ArrayList<File>(30); 
    /* keep track of opened error-fix templates */
    protected static ArrayList<File> ErrorFixTemplateFiles = new ArrayList<File>(30); 

    protected static EDFTreeNode rootNode = new EDFTreeNode("Task"); // root
    protected static EDFTreeNode workingDirNode = new EDFTreeNode("EDF Files"); // root.getChildAtRow(0)
    protected static EDFTreeNode eiaTemplateFilesNode = new EDFTreeNode("Identity Templates"); // root.getChildAtRow(1)
    protected  static EDFTreeNode esaTemplateFilesNode = new EDFTreeNode("Signal Templates"); // root.getChildAtRow(2)
    protected static TaskTree taskTree = new TaskTree(rootNode, workingDirNode, eiaTemplateFilesNode, esaTemplateFilesNode);

    private static int snOfNewEIATemplateFile = 0;
    private static int snOfNewESATemplateFile = 0;
    
    //Fangping, 08/09/2010
    //main window width and height
    public static final int MAINWINDOW_DLG_WIDTH = 1000;
    public static final int MAINWINDOW_DLG_HEIGHT = 750;
    
    //Fangping, 08/13/2010
    //show selected file's information, like size, modified day, and/or source file name
    // located at the left-bottom of the frame
    protected static EDFInfoPane fileinfoEdtPane = new EDFInfoPane(EDFInfoPane.FINFO);
    protected static EDFInfoPane taskinfoEdtPane = new EDFInfoPane(EDFInfoPane.FINFO);
    //log/message Pane, located at the right-bottom of the frame
    protected static EDFInfoPane consolePane = new EDFInfoPane(EDFInfoPane.LOG);
    

    public static final ImageIcon fileNewIcon = new ImageIcon(MainWindow.class.getResource("/icon/Folder.png"));
    public static final ImageIcon fileAddFilesIcon = new ImageIcon(MainWindow.class.getResource("/icon/new doc.png"));
    public static final ImageIcon fileRenameIcon = new ImageIcon(MainWindow.class.getResource("/icon/Rename.png"));
    public static final ImageIcon fileDeleteIcon = new ImageIcon(MainWindow.class.getResource("/icon/Delete.png"));
    public static final ImageIcon fileSaveIcon = new ImageIcon(MainWindow.class.getResource("/icon/Save.png"));
    public static final ImageIcon fileSaveAllIcon = new ImageIcon(MainWindow.class.getResource("/icon/SaveAll.png"));
    public static final ImageIcon fileExportIcon = new ImageIcon(MainWindow.class.getResource("/icon/Export.png"));
    public static final ImageIcon filePrintIcon = new ImageIcon(MainWindow.class.getResource("/icon/Print.png"));
    public static final ImageIcon fileExitIcon = new ImageIcon(MainWindow.class.getResource("/icon/Exit.png"));
    public static final ImageIcon editUndoIcon = new ImageIcon(MainWindow.class.getResource("/icon/Undo.png"));
    public static final ImageIcon editRedoIcon = new ImageIcon(MainWindow.class.getResource("/icon/Redo.png"));
    public static final ImageIcon editCopyIcon = new ImageIcon(MainWindow.class.getResource("/icon/Copy.png"));
    public static final ImageIcon editCutIcon = new ImageIcon(MainWindow.class.getResource("/icon/Cut.png"));
    public static final ImageIcon editPasteIcon = new ImageIcon(MainWindow.class.getResource("/icon/Paste.png"));
    /* public static final ImageIcon editCopyIcon = new ImageIcon(MainWindow.class.getResource("/icon/Copy.png")); */
    public static final ImageIcon editAddRowIcon = new ImageIcon(MainWindow.class.getResource("/icon/Add.png"));
    public static final ImageIcon editRemoveRowIcon = new ImageIcon(MainWindow.class.getResource("/icon/Remove.png"));
    public static final ImageIcon templateNewEiaIcon = new ImageIcon(MainWindow.class.getResource("/icon/eia.png"));
    public static final ImageIcon templateNewEsaIcon = new ImageIcon(MainWindow.class.getResource("/icon/esa.png"));
    public static final ImageIcon templateOpenEiaIcon = new ImageIcon(MainWindow.class.getResource("/icon/Open.png"));
    public static final ImageIcon templateOpenEsaIcon =  new ImageIcon(MainWindow.class.getResource("/icon/Open.png"));
    public static final ImageIcon templateExtractEiaIcon = new ImageIcon(MainWindow.class.getResource("/icon/import.png"));
    public static final ImageIcon templateExtractEsaIcon = new ImageIcon(MainWindow.class.getResource("/icon/import.png"));
    public static final ImageIcon toolsApplyEiaIcon = new ImageIcon(MainWindow.class.getResource("/icon/apply-eia.png"));
    public static final ImageIcon toolsApplyEsaIcon = new ImageIcon(MainWindow.class.getResource("/icon/apply-esa.png")); 
    public static final ImageIcon toolsValidateIcon = new ImageIcon(Main.class.getResource("/icon/Preview.png"));
    public static final ImageIcon toolsErrorFixIcon = new ImageIcon(MainWindow.class.getResource("/icon/apply-eia.png"));
    public static final ImageIcon helpAboutIcon = new ImageIcon(MainWindow.class.getResource("/icon/ProductManual.png"));
    public static final ImageIcon helpHowToUseIcon = new ImageIcon(MainWindow.class.getResource("/icon/Help.png"));
    public static final ImageIcon helpResourceSiteIcon = new ImageIcon(MainWindow.class.getResource("/icon/network-icon.png"));
    public static final ImageIcon helpEDFHomeIcon = new ImageIcon(MainWindow.class.getResource("/icon/edfplus_icon.png"));
    public static final ImageIcon helpIntensiveIcon =new ImageIcon(MainWindow.class.getResource("/icon/help.gif"));
    
    public static final ImageIcon searchIcon = new ImageIcon(Main.class.getResource("/icon/Search.png"));
    public static final ImageIcon nextIcon =  new ImageIcon(Main.class.getResource("/icon/Next-icon.png"));
    public static final ImageIcon previousIcon = new ImageIcon(Main.class.getResource("/icon/Previous-icon.png"));

    public static final ImageIcon eiaTemplateTabIcon = new ImageIcon(MainWindow.class.getResource("/icon/Eialeaf.png"));
    public static final ImageIcon esaTemplateTabIcon = new ImageIcon(MainWindow.class.getResource("/icon/Esaleaf.png"));
    public static final ImageIcon errorFixTemplateTabIcon = new ImageIcon(MainWindow.class.getResource("/icon/Eialeaf.png"));
    public static final ImageIcon eiaTabIcon = new ImageIcon(MainWindow.class.getResource("/icon/Edfleaf.png"));
    public static final ImageIcon esaTabIcon = new ImageIcon(MainWindow.class.getResource("/icon/Edfleaf.png"));
    public static final ImageIcon messageIcon = new ImageIcon(MainWindow.class.getResource("/icon/Message.png"));
    public static final ImageIcon errorIcon = new ImageIcon(MainWindow.class.getResource("/icon/Warning1.png")); //("/icon/Warning.png"));
    public static final ImageIcon warningIcon = new ImageIcon(MainWindow.class.getResource("/icon/Warning1.png"));
    public static final ImageIcon fileInfoIcon = new ImageIcon(MainWindow.class.getResource("/icon/FileInfo.png"));
    public static final ImageIcon taskInfoIcon = new ImageIcon(MainWindow.class.getResource("/icon/TaskInfo.png"));
    
    protected static int writeMode;
    public static final int overwrite_mode = 0;
    public static final int duplicate_mode = 1;    
    protected static int selectionMode;
    public static final int selection_mode_by_dir = 0;
    public static final int selection_mode_by_file = 1;

    
    public static final String version_text = "Version 1.5.4";
    
    //for search
    protected static int currentInd, totalCount;
    
    
    
    static{
        fileinfoEdtPane.setEditable(false);
        fileinfoEdtPane.setPreferredSize(new Dimension(220, 2*MAINWINDOW_DLG_HEIGHT/5));
        fileinfoEdtPane.setMaximumSize(new Dimension(220, 2*MAINWINDOW_DLG_HEIGHT/5));
        
        taskinfoEdtPane.setEditable(false);
        taskinfoEdtPane.setPreferredSize(new Dimension(220, 2*MAINWINDOW_DLG_HEIGHT/5));
        taskinfoEdtPane.setMaximumSize(new Dimension(220, 2*MAINWINDOW_DLG_HEIGHT/5));
        
        consolePane.setEditable(false);
        consolePane.setPreferredSize(new Dimension(MAINWINDOW_DLG_WIDTH - 220, 2*MAINWINDOW_DLG_HEIGHT/5));
        //logMsgEdtPane.setMinimumSize(new Dimension(MAINWINDOW_DLG_WIDTH - 220, 1*MAINWINDOW_DLG_HEIGHT/3));
        consolePane.setMaximumSize(new Dimension(MAINWINDOW_DLG_WIDTH - 220, 2*MAINWINDOW_DLG_HEIGHT/5));

        ToolTipManager.sharedInstance().registerComponent(taskTree);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////// END of static member zone ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public MainWindow() {
		super("PhysioMIMI EDF Header Editor");
		setupHelpFiles();
		createMainPane();
		createStatusBar();

		addWindowListener(this);
		showGUI();
		ErrorListTable.setIcon(errorListTable,0);
    }
    
  //Zendrix Ng 9/10/2010

    private void setupHelpFiles() {
        /*String helpHS = "/helpfiles/Master.hs";
        HelpSet hs;
        hb = null;
        //ClassLoader cl = Main.class.getClassLoader(); //commented by Fangping
        try {
            //URL hsURL = HelpSet.findHelpSet(cl, helpHS); //commented by Fangping
            URL hsURL = MainWindow.class.getResource(helpHS);
            hs = new HelpSet(null, hsURL);
            hb = hs.createHelpBroker();
            hb.setCurrentID("intro");
        } catch (Exception ee) {
            System.out.println("HelpSet " + ee.getMessage());
            System.out.println("HelpSet " + helpHS + " not found");
        }*/
        
        //Zenrix: please comment the above segment and uncomment this for your use
        
    	String helpHS = "Master.hs";
        HelpSet hs;
        hb = null;
        ClassLoader cl = Main.class.getClassLoader(); //commented by Fangping
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS); //commented by Fangping
            //URL hsURL = MainWindow.class.getResource(helpHS);
            hs = new HelpSet(null, hsURL);
            hb = hs.createHelpBroker();
            hb.setCurrentID("intro");
        } catch (Exception ee) {
            System.out.println("HelpSet " + ee.getMessage());
            System.out.println("HelpSet " + helpHS + " not found");
        } 
    }

    private void createMainPane() {
                
        setJMenuBar(createMenuBar());
        boolean active = false;
        activateMenuItems(active);
        add(createToolBar(), BorderLayout.NORTH);
        activateToolBarItems(active);
        
        createTabPane();
        add(createMainSplitPane(), BorderLayout.CENTER);

        setSize(MAINWINDOW_DLG_WIDTH, MAINWINDOW_DLG_HEIGHT);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLogo();
    }

    private void createTabPane() {
        tabPane = new EDFTabbedPane();
        tabPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    BasicEDFPane pane =
                        (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
                    if (pane == null)
                        return;
                    int index = MainWindow.getSelectedTabIndex();
                    if ( index == 0 || index == 1) 
                        activateButtons(false);                    
                    else if (pane instanceof ESATemplatePane) 
                        activateButtons(true);
                }
                
                private void activateButtons(boolean active){
                    MainWindow.addRowButton.setEnabled(active);
                    MainWindow.removeRowButton.setEnabled(active);
                    MainWindow.templateAddRowItem.setEnabled(active);
                    MainWindow.templateRemoveRowItem.setEnabled(active);
                    MainWindow.editDiscardChangesItem.setEnabled(!active);
                }
            });
    }

    private JPanel createTreePanel() {
        JPanel treePanel = new JPanel(new BorderLayout());
        JLabel navLabel = createTreePaneTitleLabel();
        treePanel.add(navLabel, BorderLayout.NORTH);
        treePanel.add(taskTree, BorderLayout.CENTER);    
        treePanel.setOpaque(true);
        
        //context sensitive help
        hb.enableHelpKey(treePanel, "overview.tasknav", null);
        
        return treePanel;
    }
    
    private JLabel createTreePaneTitleLabel(){
        JLabel navLabel = new JLabel("Task Navigator", JLabel.LEFT);
        navLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        navLabel.setOpaque(true);
        navLabel.setBackground(new Color(79, 100, 150)); //175, 203, 231));
        navLabel.setPreferredSize(new Dimension(30, 25));
        navLabel.setBorder(BorderFactory.createEtchedBorder());
        navLabel.setForeground(Color.white);
        
        return navLabel;
    }
    
    public static final int spliting_loation = 3 * MAINWINDOW_DLG_HEIGHT/5;
    public static final float splitting_resize_rate = 0.85f;
    public static final Color consoleTabPaneColor = new Color(240, 240, 200);
    
    protected void createTaskTabPane(){
        taskTabPane.addTab("Source File ", fileInfoIcon, new JScrollPane(fileinfoEdtPane));
        taskTabPane.addTab("Task Summary ", fileInfoIcon, new JScrollPane(taskinfoEdtPane));
        taskTabPane.setOpaque(true);
        taskTabPane.setBackground(statusBarColor.darker());
        //context sensitive help
        hb.enableHelpKey(fileinfoEdtPane, "overview.fileinfo", null);
        hb.enableHelpKey(taskinfoEdtPane, "overview.taskinfo", null);
    }
    
    // create leftPanel to contain 
    private JSplitPane createLeftPanel(){       
        JPanel upPane = createTreePanel();
        
        createTaskTabPane();
        
        //JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(upPane), fileinfoEdtPane);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(upPane), taskTabPane);
        splitter.setDividerLocation(spliting_loation - 40);
        splitter.setOneTouchExpandable(false);
        splitter.setResizeWeight(splitting_resize_rate);
        
        return splitter;
    }
    
    private void createConsoleTabPane(){
        consoleTabPane.setOpaque(true);
        //consoleTabPane.setBackground(consoleTabPaneColor.darker());
    }
    
    private JSplitPane createRightPanel(){
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setTopComponent(MainWindow.tabPane);
        createConsoleTabPane();
        
        //JTabbedPane consoleTabPane = new JTabbedPane(JTabbedPane.BOTTOM);
        //consoleTabPane.setUI(new PlasticTabbedPaneUI());
        consoleTabPane.addTab("Messages ", messageIcon, new JScrollPane(consolePane));
        consoleTabPane.addTab("Incompliances ", warningIcon, new JScrollPane(errorListTable)); 
        splitter.setBottomComponent(consoleTabPane);
        
        //splitter.setBottomComponent(new JScrollPane(consolePane));
        
        splitter.setOneTouchExpandable(false);
        splitter.setDividerSize(5);
        splitter.setDividerLocation(spliting_loation); 
        splitter.setResizeWeight(splitting_resize_rate);
        
      //context sensitive help
        hb.enableHelpKey(MainWindow.tabPane, "overview.workarea", null);
        hb.enableHelpKey(consolePane, "overview.logging", null);
        hb.enableHelpKey(errorListTable, "overview.incomp", null);
        return splitter;
    }
       

    private JSplitPane createMainSplitPane(){
        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel()); // tabPane);
        
        splitter.setOneTouchExpandable(false);
        splitter.setDividerSize(5);
        splitter.setDividerLocation(260); 
        splitter.setResizeWeight(0);
       
        return splitter;
    }
       
    /*
     * customize GUI
     */
    protected static final Color  statusBarColor = new Color(79, 136, 199);
    protected static final Color textColor = Color.white;
    protected static final Font barFont = new Font(Font.SERIF, Font.TRUETYPE_FONT, 13);
    private void createStatusBar() {
        createLeftStatusBar();
        createMidStatusBar();
        createSaveProgressBar();
        createRightStatusBar();     

        String layoutStr1 = "2dlu:n, l:50dlu:g(0.5), 2dlu:n, c:50dlu:g, 2dlu:n, f:80dlu:g, 2dlu:n, r:30dlu:g(0.5), 2dlu:n";
        String layoutStr2 = "f:14dlu:n";
        FormLayout layout = new FormLayout(layoutStr1, layoutStr2);
        statusBars = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        statusBars.add(leftStatusBar, cc.xy(2, 1));
        statusBars.add(middleStatusBar, cc.xy(4, 1));
        statusBars.add(saveProgressBar, cc.xy(6, 1));
        statusBars.add(rightStatusBar, cc.xy(8, 1));
        
        statusBars.setBackground(statusBarColor);
        statusBars.setForeground(textColor);
        
        statusBars.setOpaque(true);
        statusBars.setBorder(BorderFactory.createEtchedBorder());
        
        statusBars.validate();    
        this.add(statusBars, BorderLayout.SOUTH);
    }
    
    private void createLeftStatusBar(){
        String start_text = "Start New Task...";
        leftStatusBar = new JLabel(start_text, fileNewIcon, JLabel.LEFT);
        leftStatusBar.setFont(barFont);
        leftStatusBar.setToolTipText("Click to start a new task");
        leftStatusBar.setBackground(statusBarColor);
        leftStatusBar.setForeground(textColor);
        leftStatusBar.setOpaque(true);
        leftStatusBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leftStatusBar.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e) {
                    new NewTask_for_ValidityCommandLine(new JFrame());
                }
            });
    }
    
    private void createMidStatusBar(){
        middleStatusBar = new JLabel(" ", JLabel.CENTER);
        middleStatusBar.setOpaque(true);
        middleStatusBar.setBackground(statusBarColor);
        middleStatusBar.setForeground(textColor);
        middleStatusBar.setFont(barFont);
    }
    
    private void createSaveProgressBar(){
        saveProgressBar = new JProgressBar();
        saveProgressBar.setOpaque(true);
        saveProgressBar.setBackground(statusBarColor);
        saveProgressBar.setForeground(textColor);
        saveProgressBar.setFont(barFont);
        saveProgressBar.setVisible(false);
        saveProgressBar.setIndeterminate(true);
    }
    
    private void createRightStatusBar(){
        rightStatusBar = new JLabel(" ", JLabel.RIGHT);
        rightStatusBar.setOpaque(true);
        rightStatusBar.setBackground(statusBarColor);
        rightStatusBar.setForeground(textColor);
        rightStatusBar.setFont(barFont);
    }


    public void showGUI() {
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
        setVisible(true);
        
        
    }

    public static EDFInfoPane getConsolePane() {
        return consolePane;
    }

    public static void setTabPane(EDFTabbedPane tabPane) {
        MainWindow.tabPane = tabPane;
    }

    public static EDFTabbedPane getTabPane() {
        return tabPane;
    }

/*     public static void setAggregateIncompliances(ArrayList<Incompliance> incompliances) {
        MainWindow.aggregateIncompliances = incompliances;
    } */

/*     public static ArrayList<Incompliance> getAggregateIncompliances() {
        return aggregateIncompliances;
    } */

    public static void setErrorListTable(ErrorListTable errorListTable) {
        MainWindow.errorListTable = errorListTable;
    }

    public static ErrorListTable getErrorListTable() {
        return errorListTable;
    }

    public static void setConsolePane(EDFInfoPane consolePane) {
        MainWindow.consolePane = consolePane;
    }

    public static void setWriteMode(int writeMode) {
        MainWindow.writeMode = writeMode;
    }

    public static int getWriteMode() {
        return writeMode;
    }

    public static void setSelectionMode(int slectionMode) {
        MainWindow.selectionMode = slectionMode;
    }

    public static int getSelectionMode() {
        return selectionMode;
    }

    public static void setSourceDirectoryText(String sourceDirectoryText) {
        MainWindow.sourceDirectoryText = sourceDirectoryText;
    }

    public static String getSourceDirectoryText() {
        return sourceDirectoryText;
    }

    public static void setSaveProgressBar(JProgressBar saveProgressBar) {
        MainWindow.saveProgressBar = saveProgressBar;
    }

    public static JProgressBar getSaveProgressBar() {
        return saveProgressBar;
    }

    public static void setEiaTemplateIncompliances(ArrayList<Incompliance> eiaTemplateIncompliances) {
        MainWindow.eiaTemplateIncompliances = eiaTemplateIncompliances;
    }

    public static ArrayList<Incompliance> getEiaTemplateIncompliances() {
        return eiaTemplateIncompliances;
    }

    public static void setEsaIncompliances(ArrayList<Incompliance> esaIncompliances) {
        MainWindow.esaIncompliances = esaIncompliances;
    }

    public static ArrayList<Incompliance> getEsaIncompliances() {
        return esaIncompliances;
    }

    public static void setEsaTemplateIncompliances(ArrayList<Incompliance> esaTemplateIncompliances) {
        MainWindow.esaTemplateIncompliances = esaTemplateIncompliances;
    }

    public static ArrayList<Incompliance> getEsaTemplateIncompliances() {
        return esaTemplateIncompliances;
    }

    public static void setEiaIncompliances(ArrayList<Incompliance> eiaIncompliances) {
        MainWindow.eiaIncompliances = eiaIncompliances;
    }

    public static ArrayList<Incompliance> getEiaIncompliances() {
        return eiaIncompliances;
    }

    /**
     * @return the menu bar of the main window
     */
    
    private class EDFMenuBar extends JMenuBar{
        EDFMenuBar(){
            super();
            setPreferredSize(new Dimension(30, 30));   
            setOpaque(true);
        }
        
        @Override
        protected void paintComponent(Graphics g){
            if (!isOpaque()){
                super.paintComponent(g);
                return;
            }
            
            int width = this.getWidth();
            int height = this.getHeight();
            Color color1 = new Color(225, 240, 250);
            Color color2 = color1.brighter();
            Graphics2D g2 = (Graphics2D)g;
                       
            GradientPaint gp = new GradientPaint(0, 0, color1, width, 0, color2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, width, height);        
            
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        }
    }
    
    public EDFMenuBar createMenuBar() {
        EDFMenuBar menuBar = new EDFMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu templateMenu = createTemplateMenu();
        JMenu toolsMenu = createToolsMenu();
        JMenu helpMenu = createHelpMenu();
        //append menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(templateMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
      //context sensitive help
        hb.enableHelpKey(menuBar, "overview.menubar", null);

        return menuBar;
    }

    /**
     * @return the File menu
     */
    public JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        //create File menu items
        createFileNewTaskItem(this);
        createFileCloseTaskItem();
        
        createFileRenameItem();
        createFileAddFilesItem();
        createFileExcludeFileItem();
        createFileDeleteFileItem();
        
        createFileSaveItem();
        createFileSaveAsItem();
        
        createFileSaveAllItem();
        //createFileImportItem();
        createFileExportItem();
        createFilePrintItem();
        //createFileCloseAllItem();
        createFileExitItem();

        //append File menu items
        fileMenu.add(fileNewTaskItem);
        fileMenu.add(fileAddFilesItem);
        fileMenu.add(fileCloseTaskItem);
        fileMenu.addSeparator();
        
        fileMenu.add(fileRenameItem);
        fileMenu.add(fileExcludeFileItem);
        fileMenu.add(fileDeleteFileItem);        
        fileMenu.addSeparator();
        
        fileMenu.add(fileSaveItem);
        fileMenu.add(fileSaveAsItem);
        fileMenu.add(fileSaveAllItem);
        fileMenu.addSeparator();
        //fileMenu.add(fileImportItem);
        fileMenu.add(fileExportItem);
        fileMenu.addSeparator();
        fileMenu.add(filePrintItem);
        fileMenu.addSeparator();
        //fileMenu.add(fileCloseAllItem);
        //fileMenu.addSeparator();
        fileMenu.add(fileExitItem);
        
      //context sensitive help
        //hb.enableHelpOnButton(fileMenu, "menubar.file", null);
        CSH.setHelpIDString(fileMenu, "menubar.file");

        return fileMenu;
    }

    public void createFileNewTaskItem(final JFrame frame) {
        fileNewTaskItem = new JMenuItem("New Task...");
        fileNewTaskItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                                  Event.CTRL_MASK));
        fileNewTaskItem.setIcon(fileNewIcon);
        fileNewTaskItem.addActionListener(new SelectFilesListener());
    }
    

    public void createFileCloseTaskItem(){
        fileCloseTaskItem = new JMenuItem("Close Task");
        fileCloseTaskItem.addActionListener(new CloseTaskItemListener());
    }
    
  protected static class CloseTaskItemListener implements ActionListener{
        private final boolean active = false;
        
        public void actionPerformed(ActionEvent e) {
            //do nothing if there is no task at all
            if (tabPane == null || tabPane.getTabCount() == 0 || !(tabPane.getComponentAt(0) instanceof WorkingTablePane))
                return;
            
            if (!affirmClose())
                return;
            setTaskTreeToNull();
            closePrimaryTabs();
            setCoreDataStructuresToNull();
            clearFileInfoPane();
            clearTaskInfoPane();
            clearConsolePane();
            clearErrorListTable();
            activateMenuItems(active);
            activateToolBarItems(active);
            
        }
        
        private boolean affirmClose(){
            String prompt = "<html><center> Please make sure to have your task saved before closing current task." +
                            "<p><p> Click on Yes to confirm, No to cancel.<html>";
           return Utility.defaultNoOptionPane(null, prompt, "Close Task", JOptionPane.YES_NO_OPTION);           
        }
        
        private static void clearFileInfoPane(){
            fileinfoEdtPane.setText("");
        }
        
        private static void clearTaskInfoPane(){
            taskinfoEdtPane.setText("");
        }
        
        private static void clearConsolePane(){
            Document doc = consolePane.getDocument();
            consolePane.setText("");
            String theme = Utility.currentTimeToString() + ": Task closed. \n";
            try {
                doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
            } catch (BadLocationException e) {;}
            //jump to the console pane
            consoleTabPane.setSelectedIndex(0);
        }
    }
    
    /*
     * Fangping, 08/22/2010
     */
    public void createFileAddFilesItem() {
    	fileAddFilesItem = new JMenuItem("Add Files...");
        fileAddFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                                                  Event.CTRL_MASK));
        fileAddFilesItem.setIcon(fileAddFilesIcon);
    	fileAddFilesItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {new AddFilesListener(new JFrame());}});
    	fileAddFilesItem.setEnabled(false);
        // selectFilesListener is specified next to createSelectFilesBtn();
    }
    
    /*
     * Fangping, 08/22/2010
     */
    public void createFileExcludeFileItem(){
        fileExcludeFileItem = new JMenuItem("Exclude File");
        int cmd = ExcludeFileListener.REMOVE;  
        fileExcludeFileItem.addActionListener(new ExcludeFileListener(cmd));
    }
    
    public void createFileDeleteFileItem(){
        fileDeleteFileItem = new JMenuItem("Delete File");
        fileDeleteFileItem.setIcon(fileDeleteIcon);
        int cmd = ExcludeFileListener.DELETE;
        fileDeleteFileItem.addActionListener(new ExcludeFileListener(cmd));
    }
    
    
    /**
     * save, save as, and save all use the same listener which implements actions for different
     * options. Ths listener is defined nex to createFileSaveAllItem();
     */
    private void createFileSaveItem() {
        fileSaveItem = new JMenuItem("Save", 'S');
        fileSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                           Event.CTRL_MASK));
        fileSaveItem.setIcon(fileSaveIcon);
        fileSaveItem.addActionListener(new SaveListener("Save"));
    }

    private void createFileSaveAsItem() {
        fileSaveAsItem = new JMenuItem("Save As...");
        fileSaveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                                                           Event.CTRL_MASK));
        fileSaveAsItem.setIcon(fileSaveIcon);
        fileSaveAsItem.addActionListener(new SaveListener("SaveAs"));
    }

    private void createFileSaveAllItem() {
        fileSaveAllItem = new JMenuItem("Save All", 'A');
        fileSaveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                                                              Event.CTRL_MASK));
        fileSaveAllItem.setIcon(fileSaveAllIcon);
        fileSaveAllItem.addActionListener(new SaveListener("SaveAll"));
    }    
        
    /*
     * Fangping, 08/22/2010
     */
    private void createFileRenameItem() {
        fileRenameItem = new JMenuItem("Rename File");
        fileRenameItem.setAccelerator(KeyStroke.getKeyStroke("F2"));
        fileRenameItem.setIcon(fileRenameIcon);
        fileRenameItem.addActionListener(new RenameFileListener());
    }

    private void createFileExportItem() {
        fileExportItem = new JMenuItem("Export");
        fileExportItem.setIcon(fileExportIcon);
        fileExportItem.addActionListener(new ExportTaskToFileListener());
    }
    
    protected static class ExportTaskToFileListener implements ActionListener{
        Document doc = consolePane.getDocument();
        String theme;
        String eiaFileName;
        String esaFileName;
        
        private void printThemeToConsole(){
            try {
                doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
            } catch (BadLocationException f) {;}
        }
        
        public void actionPerformed(ActionEvent e) {
            if (srcEdfFiles == null)
                return;  // Fangping, 02/28/2010
            
            ArrayList<EDFFileHeader>  srcHeader = srcEdfFileHeaders; 
            ArrayList<File> srcFiles = srcEdfFiles;
        
            theme = "Export current task ......";
            printThemeToConsole();
            
            eiaFileName = Utility.writeCSVEIA(srcHeader, srcFiles);
            esaFileName = Utility.writeCSVESA(srcHeader, srcFiles);
         
            //Fangping, 08/20/2010
            String str = "\nReports exported to " + workingDirectory.toString();
            
            //Fangping, 08/20/2010
            theme = "Done. \n";
            printThemeToConsole();
            confirmFinished();
        }
        
        private void confirmFinished(){
            String message = "Export task to files: \n" + eiaFileName + "\n" + esaFileName + "\nis done.";
            message = message + "\nDo you want to open them now?";
            String title = "Export Task";
            boolean reply = Utility.NoThanksOptionPane(null, message, title, JOptionPane.YES_OPTION);
            if (reply == true){
                Utility.editFile(new File(eiaFileName));
                Utility.editFile(new File(esaFileName));
            }
            
        }
    }
    

    private void createFilePrintItem() {
        filePrintItem = new JMenuItem("Print", 'P');
        filePrintItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                            Event.CTRL_MASK));
        filePrintItem.setIcon(filePrintIcon);
        filePrintItem.addActionListener(new PrintListener());
    }
    
    /*
     * print listener for print
     * adapted from http://java.sun.com/developer/onlineTraining/Programming/JDCBook/advprint.html#pe
     */
    private class PrintListener implements ActionListener, Printable{
        
        JTable wTable = null;
        
        public void actionPerformed(ActionEvent e) {
            if (!MainWindow.tabPane.isPrimaryTabsOpened())
                return;   
            WorkingTablePane wPane = (WorkingTablePane) MainWindow.tabPane.getSelectedComponent();
            wTable = wPane.getEdfTable();            
                     
            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setPrintable(this);
            pjob.printDialog();
            try{
                pjob.print();
            }
            catch(Exception pe){
                ;// do nothing yet
            }
        }        
        
        public int print(Graphics graphics, PageFormat pageFormat,
                         int pageIndex) {
            
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setColor(Color.black);
            int fontHeight = g2.getFontMetrics().getHeight();
            int fontDescent = g2.getFontMetrics().getDescent();
            
            //leave room for page number
            double pageHeight = pageFormat.getImageableHeight() - fontHeight;
            double pageWidth = pageFormat.getImageableWidth();
            double tableWidth = (double) wTable.getColumnModel().getTotalColumnWidth();
            double scale = 1;
            if (tableWidth >= pageWidth)
                scale = pageWidth/tableWidth;
            
            //set on-page header height and table width
            double headerHeightOnPage = wTable.getTableHeader().getHeight() * scale;
            double tableWidthOnPage = tableWidth * scale;
            
            double oneRowHeight = (wTable.getRowHeight() + wTable.getRowMargin()) * scale;
            int numRowsOnPage = (int)((pageHeight - headerHeightOnPage)/oneRowHeight);
            double pageHeightForTable = oneRowHeight * numRowsOnPage;
            int totalNumPages = (int) Math.ceil(((double)wTable.getRowCount())/numRowsOnPage);
            
            if (pageIndex >= totalNumPages)
                return Printable.NO_SUCH_PAGE;
            
            //print the footer with format: "page #"
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.drawString("Page: " + (pageIndex+1), (int) pageWidth/2-35, (int) (pageHeight+fontHeight-fontDescent));
            
            g2.translate(0f, headerHeightOnPage);
            g2.translate(0f, -pageIndex * pageHeightForTable);
            
            //if this peice of the table is smaller than the size available, clip to the appropriate bounds
            if (pageIndex + 1 == totalNumPages){
                int lastRowPrinted = numRowsOnPage * pageIndex;
                int numRowsLeft = wTable.getRowCount() - lastRowPrinted;
                g2.setClip(0, (int) (pageHeightForTable * pageIndex), 
                           (int) Math.ceil(tableWidthOnPage),
                           (int) Math.ceil(oneRowHeight * numRowsLeft));
            }
            //else clip to the entire area available.
            else{
                g2.setClip(0, (int)(pageHeightForTable*pageIndex), 
                          (int) Math.ceil(tableWidthOnPage),
                           (int) Math.ceil(pageHeightForTable)); 
            }
            
            g2.scale(scale, scale);
            wTable.paint(g2);
            g2.scale(1/scale, 1/scale);
            g2.translate(0f, pageIndex*pageHeightForTable);
            g2.translate(0f, -headerHeightOnPage);
            
            g2.setClip(0, 0, (int) Math.ceil(tableWidthOnPage), 
                      (int)Math.ceil(headerHeightOnPage));
            g2.scale(scale,scale);
            wTable.getTableHeader().paint(g2);
                    //paint header at top

            return Printable.PAGE_EXISTS;           
 
        }
    }
    
    private void createFileExitItem() {
        fileExitItem = new JMenuItem("Exit", 'X');
        fileExitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                           Event.CTRL_MASK));
        fileExitItem.setIcon(fileExitIcon);
        fileExitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                        gracefulCloseWindow();                                                    
                }
            });
    }



    private void createEditUndoItem() {
        editUndoItem = new JMenuItem("Undo", 'U');
        editUndoItem.setAction(undoManager.getUndoAction());
        editUndoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                                           Event.CTRL_MASK));
        editUndoItem.setIcon(editUndoIcon);
    }

    private void createEditRedoItem() {
        editRedoItem = new JMenuItem("Redo", 'R');
        editRedoItem.setAction(undoManager.getRedoAction());
        editRedoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
        editRedoItem.setIcon(editRedoIcon);
        //editRedoItem.setEnabled(true);
        //editRedoItem.addActionListener(redoListener);
    }

    private void createEditCutItem() {
        editCutItem = new JMenuItem("Cut", 'T');
        editCutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
        editCutItem.addActionListener(new CPCAdapter("Cut"));
        editCutItem.setIcon(editCutIcon);
    }

    private void createEditCopyItem() {
        editCopyItem = new JMenuItem("Copy", 'C');
        editCopyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                           Event.CTRL_MASK));
        editCopyItem.addActionListener(new CPCAdapter("Copy"));
        editCopyItem.setIcon(editCopyIcon);
    }

    private void createEditPasteItem() {
        editPasteItem = new JMenuItem("Paste");
        editPasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                            Event.CTRL_MASK));
        editPasteItem.addActionListener(new CPCAdapter("Paste"));
        editPasteItem.setIcon(editPasteIcon);
    }
    
    private void createEditDiscardChangesItem() {
    	editDiscardChangesItem = new JMenuItem("Discard Changes");
    	editDiscardChangesItem.addActionListener(new DiscardChangesListener());
    }


    private void createToolsApplyEIATemplateItem() {
        toolApplyEIATemplateItem = new JMenuItem("Apply Identity Template");
        toolApplyEIATemplateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                                                                       Event.CTRL_MASK));
        toolApplyEIATemplateItem.setIcon(toolsApplyEiaIcon);
        //editApplyEIATemplateItem.addActionListener(new ApplyTemplateListener(this, "eia"));
        toolApplyEIATemplateItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (MainWindow.wkEdfFiles == null)
                        return;
                    new ApplyTemplateListener(new JFrame(), "eia");
                }
            });
    }


    private void createToolsApplyESATemplateItem() {
        toolApplyESATemplateItem = new JMenuItem("Apply Signal Template");
        toolApplyESATemplateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,
                                                                       Event.CTRL_MASK));
        toolApplyESATemplateItem.setIcon(toolsApplyEsaIcon);
        //editApplyESATemplateItem.addActionListener(new ApplyTemplateListener(this, "esa"));
        toolApplyESATemplateItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (MainWindow.wkEdfFiles == null)
                        return;
                    new ApplyTemplateListener(new JFrame(), "esa");
                }
            });
    }
    
    private void createToolsValidateTableItem(){
    	//TODO
        toolValidateTableItem = new JMenuItem("Find Table Errors");
        toolValidateTableItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
        toolValidateTableItem.setIcon(toolsValidateIcon);
        toolValidateTableItem.addActionListener(new VerifyHeaderListener());
    }

    private void createToolsFixTableErrors(){
    	toolFixTableErrorItem = new JMenuItem("Fix Table Errors");
    	toolFixTableErrorItem.setIcon(toolsErrorFixIcon);
    	toolFixTableErrorItem.addActionListener(new NewErrorFixTemplateListener());
    }
    
    private void createJEDFToolItem(){
        jEDFToolItem = new JMenuItem("JEDF Tool");
        jEDFToolItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                	try
                    {
                    	Runtime rt = Runtime.getRuntime();
                    	Process p = rt.exec("java -jar jEDF.jar");
                    }
                    catch (Exception exc)
                    {
                    	exc.printStackTrace();
                    }
                }
            });
    }
    
    private void createEDFViewerToolItem(){
        EDFViewerToolItem = new JMenuItem("EDFViewer Tool");
        EDFViewerToolItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    try
                    {
                    	Runtime rt = Runtime.getRuntime();
                    	Process p = rt.exec("EDFViewer.exe");
                    }
                    catch (Exception exc)
                    {
                    	exc.printStackTrace();
                    }
                }
            });
    }


    /**
     * @return the Edit menu
     */
    public JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        //initialize Edit menu items
        createEditUndoItem();
        createEditRedoItem();
        createEditCutItem();
        createEditCopyItem();
        createEditPasteItem();
        createEditDiscardChangesItem();
        createTemplateAddRowItem();
        createTemplateRemoveRowItem();

        // append Edit menu items
        editMenu.add(editUndoItem);
        editMenu.add(editRedoItem);
        editMenu.add(editDiscardChangesItem);
        editMenu.addSeparator();
        editMenu.add(editCutItem);
        editMenu.add(editCopyItem);
        editMenu.add(editPasteItem);        
        editMenu.addSeparator();        
        editMenu.add(templateAddRowItem);
        editMenu.add(templateRemoveRowItem);
        
/*         editMenu.add(editApplyEIATemplateItem);
        editMenu.add(editApplyESATemplateItem); */
        
      //context sensitive help
        //hb.enableHelpOnButton(editMenu, "menubar.edit", null);
        CSH.setHelpIDString(editMenu, "menubar.edit");

        return editMenu;
    }

    /**
     * @return the Template menu
     */
    public JMenu createTemplateMenu() {
        JMenu templateMenu = new JMenu("Template");
        templateMenu.setMnemonic('T');

        createTemplateNewEIAItem();
        createTemplateNewESAItem();
        createTemplateOpenEIAItem();
        createTemplateOpenESAItem();
        createTemplateImportEIAItem();
        createTemplateImportESAItem();
        createToolsFixTableErrors();
        createToolsApplyEIATemplateItem();
        createToolsApplyESATemplateItem();
/*         createTemplateAddRowItem();
        createTemplateRemoveRowItem(); */

        templateMenu.add(templateNewEIAItem);
        templateMenu.add(templateOpenEIAItem);
        templateMenu.add(templateImportEIAItem);
        templateMenu.addSeparator();
        templateMenu.add(templateNewESAItem);
        templateMenu.add(templateOpenESAItem);
        templateMenu.add(templateImportESAItem);
        templateMenu.addSeparator();
        templateMenu.add(toolApplyEIATemplateItem);
        templateMenu.add(toolApplyESATemplateItem);
        
        //context sensitive help
        //hb.enableHelpOnButton(templateMenu, "menubar.template", null);
        CSH.setHelpIDString(templateMenu, "menubar.template");
        
        return templateMenu;
    }


    private void createTemplateNewEIAItem() {
        templateNewEIAItem = new JMenuItem("New Identity Template");
        templateNewEIAItem.setIcon(templateNewEiaIcon);
        templateNewEIAItem.setToolTipText("create from blank form");
        templateNewEIAItem.addActionListener(new NewEIATemplateListener());
    }

    public static void setUndoManager(EDFUndoManager undoManager) {
        MainWindow.undoManager = undoManager;
    }

    public static UndoManager getUndoManager() {
        return undoManager;
    }

    public static JButton getUndoButton() {
        return undoButton;
    }


    public static JButton getRedoButton() {
        return redoButton;
    }

    public static JMenuItem getEditUndoItem() {
        return editUndoItem;
    }

    public static JMenuItem getEditRedoItem() {
        return editRedoItem;
    }


    /**
     * TODO
     * The following code:
     * Feature Improvement by Gang Shu on Feb. 24, 2014
     */
	protected static class NewErrorFixTemplateListener implements ActionListener {

		private File templateFile = new File("ErrorFix.fix");

		public void actionPerformed(ActionEvent e) {

			ErrorFixTemplatePane templatePane = ErrorFixTemplatePane .getErrorFixTemplatePane(null, templateFile);

			tabPane.addTab(templateFile.getName(), toolsErrorFixIcon, templatePane, "Error Fix");
			int index = MainWindow.tabPane.getTabCount() - 1;
			new CloseTabButton(MainWindow.tabPane, index);
			MainWindow.tabPane.setSelectedIndex(index);
		}
	} //end of NewErrorFixTemplateListener
    /**
     * The above code:
     * Feature Improvement by Gang Shu on Feb. 24, 2014
     */
    
    protected static class NewEIATemplateListener implements ActionListener {

        private final String eiaFileNamePrefix = "untitled";
        private File templateFile;
        private long pid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        public void performActions(){
            if (!generateFileName())
                return;
            
            if (templateFile == null)
                return;
            
            updateEiaTemplateFileList();  
            yieldEiaTemplateTab();
            yieldEiaTemplateTreeNode();
            
            incrementSNofNewEIATemplateFile();
            updateEiaTemplatingBtns();
        }
        
        private boolean generateFileName() {
            String tempFileName;
            int serialNumber = snOfNewEIATemplateFile;
            String directoryPath = null;
            if (MainWindow.getWorkingDirectory() != null)
                directoryPath = MainWindow.getWorkingDirectory().getAbsolutePath();
            else
                try {
                    directoryPath = new File(".").getCanonicalPath();
                } catch (IOException e) { e.printStackTrace();}

            if (directoryPath == null)
                return false;
            
            String tempFullPath;
            File tempFile = null;
            boolean notYetOnDisk = false;
            while(true){
                tempFileName = generateTempName(serialNumber);                
                tempFullPath = directoryPath + "/" + tempFileName;
                tempFile = new File(tempFullPath);
                //1. see if alreay on disk
                try {
                    notYetOnDisk = tempFile.createNewFile();
                } catch (IOException e) {;}
                if (!notYetOnDisk){
                    serialNumber++;
                    continue;
                }   
                //2. go on to see if already in eiaTemplateFiles
                else {
                    tempFile.delete();     
                    if (Utility.isFileNameCollided(tempFile, EIATemplateFiles)){
                        serialNumber++;
                        continue;
                    }
                    else{
                        templateFile = tempFile;
                         return true;
                    }               
                }
            }
        }
        
        private String generateTempName(int serialNumber){
            String tempName = null;
            if (serialNumber == 0)
                tempName = eiaFileNamePrefix + ".eia";
            else
                tempName = eiaFileNamePrefix + "(" + serialNumber + ").eia";
            
            return tempName;
        }
        

        private void yieldEiaTemplateTab() {
            String fileName = templateFile.getAbsolutePath();
            //master file is set as templateFile
            EIATemplatePane templatePane = new EIATemplatePane(null, templateFile);
            pid = templatePane.getPid();
            
            tabPane.addTab(templateFile.getName(), eiaTemplateTabIcon, templatePane, fileName);
            int index = MainWindow.tabPane.getTabCount() - 1;
            new CloseTabButton(MainWindow.tabPane, index);
            MainWindow.tabPane.setSelectedIndex(index);           

          //context sensitive help
            hb.enableHelpKey(templatePane, "hFileATemp", null);
        }

        private void yieldEiaTemplateTreeNode() {
            MainWindow.taskTree.addFileNodeAt(1, templateFile, pid);
        }
        
        private void incrementSNofNewEIATemplateFile(){
            snOfNewEIATemplateFile++;
        }
        
        public void updateEiaTemplateFileList(){
            EIATemplateFiles.add(templateFile);
        }
        
        private void updateEiaTemplatingBtns(){
            fileRenameItem.setEnabled(true);
        }

    } //end of NewEIATemplateListener


    private void createTemplateNewESAItem() {
        templateNewESAItem = new JMenuItem("New Signal Template");
        templateNewESAItem.setIcon(templateNewEsaIcon);
        templateNewESAItem.setToolTipText("create from a blank form");

        templateNewESAItem.addActionListener(new NewESATemplateListener());
    }

    protected static class NewESATemplateListener implements ActionListener {

        private final String esaFileNamePrefix = "untitled";
        private File templateFile;
        private long pid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        public void performActions(){
            if (!generateFileName())
                return;
            
            if (templateFile == null)
                return;
            
            updateEsaTemplateFileList();
            yieldEsaTemplateTab();
            yieldEsaTemplateTreeNode();

            updateEsaTemplatingBtns(true);
            incrementSNofNewESATemplateFile();
        }
        
        private boolean generateFileName() {
            String tempFileName;
            int serialNumber = snOfNewESATemplateFile;
            String directoryPath = null;
            if (MainWindow.getWorkingDirectory() != null)
                directoryPath = MainWindow.getWorkingDirectory().getAbsolutePath();
            else
                try {
                    directoryPath = new File(".").getCanonicalPath();
                } catch (IOException e) { e.printStackTrace();}

            if (directoryPath == null)
                return false;
            
            String tempFullPath;
            File tempFile = null;
            boolean notYetOnDisk = false;
            while(true){
                tempFileName = generateTempName(serialNumber);                
                tempFullPath = directoryPath + "/" + tempFileName;
                tempFile = new File(tempFullPath);
                //1. see if alreay on disk
                try {
                    notYetOnDisk = tempFile.createNewFile();
                } catch (IOException e) {;}
                if (!notYetOnDisk){
                    serialNumber++;
                    continue;
                }   
                //2. go on to see if already in esaTemplateFiles
                else {
                    tempFile.delete();     
                    if (Utility.isFileNameCollided(tempFile, ESATemplateFiles)){
                        serialNumber++;
                        continue;
                    }
                    else{
                        templateFile = tempFile;
                        return true;
                    }               
                }
            }
        }
        
        private String generateTempName(int serialNumber){
            String tempName = null;
            if (serialNumber == 0)
                tempName = esaFileNamePrefix + ".esa";
            else
                tempName = esaFileNamePrefix + "(" + serialNumber + ").esa";
            
            return tempName;
        }
        
        private void yieldEsaTemplateTab() {
            ESATemplatePane templatePane = new ESATemplatePane();            
            String fileName = templateFile.getAbsolutePath();
            tabPane.addTab(templateFile.getName(), esaTemplateTabIcon, templatePane, fileName);
            int index = MainWindow.tabPane.getTabCount() - 1;
            new CloseTabButton(MainWindow.tabPane, index);
            MainWindow.tabPane.setSelectedIndex(index);
            MainWindow.tabPane.setToolTipTextAt(index, fileName);

            pid = templatePane.getPid();
            templatePane.setMasterFile(templateFile);  
            templatePane.getEsaTemplateTable().setMasterFile(templateFile);
            
          //context sensitive help
            hb.enableHelpKey(templatePane, "hSignalATemp", null);
        }

        private void yieldEsaTemplateTreeNode() {
            MainWindow.taskTree.addFileNodeAt(2, templateFile, pid);
        }

        private void updateEsaTemplateFileList(){
            ESATemplateFiles.add(templateFile);
        }
        
        private void updateEsaTemplatingBtns(boolean active){
            fileRenameItem.setEnabled(active);
            addRowButton.setEnabled(active);
            removeRowButton.setEnabled(active);            
        }
        
        private void incrementSNofNewESATemplateFile(){
            snOfNewESATemplateFile++;
        }
    }// end of NewESATemplateListener


    private void createTemplateOpenEIAItem() {
        templateOpenEIAItem = new JMenuItem("Open Identity Template");
        templateOpenEIAItem.setIcon(templateOpenEiaIcon);
        templateOpenEIAItem.addActionListener(new OpenEIATemplateListener());
    }

    protected static class OpenEIATemplateListener implements ActionListener {
        private final String extName = "eia";
        private final String description = "EDF Identity Attribute Files(*.eia)";


        private File openedEIAFile;
        private boolean alreadyOpened;
        private EIAHeader eiaHeader = null;
        private long uid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        private void performActions(){
            if (!selectTemplateFile())
                return;
            
            if (openedEIAFile == null)
                return;
            
            if (alreadyOpened){
                selectTabWhenAlreadyOpened();
                return;
            }
            
            yieldEIAHeader();
            yieldEIATemplateTab();
            yieldEiaTemplateTreeNode();
            
            updateMenuToolBarItems(true);
            
        }

        private boolean selectTemplateFile() {
            EDFFileFilter filter =
                new EDFFileFilter(new String[] { extName }, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Open Identity Template");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return false;
            
            openedEIAFile = chooser.getSelectedFile(); 
            //need to process the alreadyOpened case
            alreadyOpened = Utility.isFileNameCollided(openedEIAFile, EIATemplateFiles);
            return true;
        }
        
        private void selectTabWhenAlreadyOpened(){
            int index = getIndexOfOpenedFile();  
            selectTaskTreeNode(index);
            selectTabWithIndex();
        }
        
        private int getIndexOfOpenedFile(){
            File file;
            for (int index = 0; index < EIATemplateFiles.size(); index++){
                file = EIATemplateFiles.get(index);
                if (openedEIAFile.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath()))
                    return index;
            }
            
            return -1;
        }        
        
        private void selectTaskTreeNode(int index) {            
            EDFTreeNode parentNode = MainWindow.taskTree.getEiaRootNode();
            EDFTreeNode greenNode = (EDFTreeNode) parentNode.getChildAt(index);

            TreePath path = new TreePath(greenNode.getPath());
            MainWindow.taskTree.scrollPathToVisible(path);
            MainWindow.taskTree.setSelectionPath(path);
        }
        
        private void selectTabWithIndex(){
            BasicEDFPane pane;
            String name = openedEIAFile.getAbsolutePath();
            for (int i = 0; i < tabPane.getTabCount(); i++){
                pane = (BasicEDFPane)tabPane.getComponentAt(i);
                if ((pane instanceof EIATemplatePane) && pane.getMasterFile().getAbsolutePath().equalsIgnoreCase(name)){
                    tabPane.setSelectedIndex(i);
                    return;
                }     
            }
        }
        
        private void yieldEIAHeader() {
            try {
                eiaHeader = EIAHeader.retrieveEIAHeaderFromXml(openedEIAFile.getPath());
            } 
            catch (ParserConfigurationException e) {;} 
            catch (SAXException e) {;} 
            catch (IOException e) {;}
        }
        
/*         private void yieldEIAHeader() {
            RandomAccessFile ras = null;
            try {
                ras = new RandomAccessFile(openedEIAFile, "rw");
            } catch (FileNotFoundException f) {
                f.printStackTrace();
            }

            try {
                eiaHeader = new EIAHeader(ras, openedEIAFile);
            } catch (IOException f) {
                f.printStackTrace();
            }
        } */

        private void yieldEIATemplateTab() {
            //master file is set as opendedEIAFile
            EIATemplatePane templatePane = new EIATemplatePane(eiaHeader, openedEIAFile);

            String fileName = openedEIAFile.getAbsolutePath();
            
            tabPane.addTab(openedEIAFile.getName(), eiaTemplateTabIcon, templatePane, fileName);
            int index = MainWindow.tabPane.getTabCount() - 1;
            new CloseTabButton(MainWindow.tabPane, index);
            MainWindow.tabPane.setSelectedIndex(index);
            uid = templatePane.getPid();
 
            EIATemplateFiles.add(openedEIAFile);
            
          //context sensitive help
            hb.enableHelpKey(templatePane, "hFileATemp", null);

        }

        private void yieldEiaTemplateTreeNode() {
            MainWindow.taskTree.addFileNodeAt(1, openedEIAFile, uid);
        }
        
        private void updateMenuToolBarItems(boolean active){
            fileRenameItem.setEnabled(active);
        }
    } // end of OpenEIATemplateListener


    private void createTemplateOpenESAItem() {
        templateOpenESAItem = new JMenuItem("Open Signal Template");
        templateOpenESAItem.setIcon(templateOpenEsaIcon);
        templateOpenESAItem.addActionListener(new OpenESATemplateListener());
    }

    protected static class OpenESATemplateListener implements ActionListener {
        private final String extName = "esa";
        private final String description = "EDF Signal Attribute Files(*.esa)";

        private File openedESAFile;
        private ESAHeader esaHeader = null;
        private boolean alreadyOpened;
        private long uid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        private void performActions(){
            if (!selectTemplateFile())
                return;
            
            if (openedESAFile == null)
                return;

            if (alreadyOpened) {
                selectTabWhenAlreadyOpened();
                return;
            }
            
            yieldESAHeader();
            yieldESATemplateTab();
            yieldESATemplateTreeNode();
            
            updateMenuToolBarItems(true);

        }

        private boolean selectTemplateFile() {
            EDFFileFilter filter =
                new EDFFileFilter(new String[] { extName }, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);//Fangping, 08/19/2010
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Open Signal Template");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return false;
            
            openedESAFile = chooser.getSelectedFile();
            //need to process the alreay opened case
            alreadyOpened = Utility.isFileNameCollided(openedESAFile, ESATemplateFiles);
            return true;
        }
        
        private void selectTabWhenAlreadyOpened(){
            int index = getIndexOfOpenedFile();
            selectTaskTreeNode(index);
            selectTabWithIndex();            
        }
        
        private int getIndexOfOpenedFile(){
            File file;
            for (int index = 0; index < ESATemplateFiles.size(); index++){
                file = ESATemplateFiles.get(index);
                if (openedESAFile.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath()))
                    return index;
            }
            
            return -1;
        }

        private void selectTaskTreeNode(int index) {
            EDFTreeNode parentNode = MainWindow.taskTree.getEsaRootNode();
            EDFTreeNode greenNode = (EDFTreeNode)parentNode.getChildAt(index);

            TreePath path = new TreePath(greenNode.getPath());
            MainWindow.taskTree.scrollPathToVisible(path);
            MainWindow.taskTree.setSelectionPath(path);
        }

        private void selectTabWithIndex() {
            BasicEDFPane pane;
            String name = openedESAFile.getAbsolutePath();
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                pane = (BasicEDFPane)tabPane.getComponentAt(i);
                if ((pane instanceof ESATemplatePane) && pane.getMasterFile().getAbsolutePath().equalsIgnoreCase(name)) {
                    tabPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        private void yieldESAHeader() {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(openedESAFile, "rw");
            } catch (FileNotFoundException f) {
                f.printStackTrace();
            }

            EDFFileHeader edfHeader = null;
            edfHeader = new EDFFileHeader(raf, openedESAFile, true);

            esaHeader = edfHeader.getEsaHeader();
        }

        private void yieldESATemplateTab() {
            ESATemplatePane templatePane = new ESATemplatePane(esaHeader, true);
            templatePane.setMasterFile(openedESAFile);
            
            String fileName = openedESAFile.getAbsolutePath();
            tabPane.addTab(openedESAFile.getName(), esaTemplateTabIcon, templatePane, fileName);
            int index = MainWindow.tabPane.getTabCount() - 1;
            new CloseTabButton(MainWindow.tabPane, index);
            MainWindow.tabPane.setSelectedIndex(index);

            uid = templatePane.getPid();
            templatePane.setMasterFile(openedESAFile);
            ESATemplateFiles.add(openedESAFile);
            
          //context sensitive help
            hb.enableHelpKey(templatePane, "hSignalATemp", null);
        }

        private void yieldESATemplateTreeNode() {
            MainWindow.taskTree.addFileNodeAt(2, openedESAFile, uid);
        }
        
        private void updateMenuToolBarItems(boolean active){
            fileRenameItem.setEnabled(active);
            addRowButton.setEnabled(active);
            removeRowButton.setEnabled(active);            
        }    
    } //end of TemplateOpenESAListener

    private void createTemplateImportEIAItem() {
        templateImportEIAItem = new JMenuItem("Extract Identity Template from EDF File");
        templateImportEIAItem.setIcon(templateExtractEiaIcon);
        templateImportEIAItem.setToolTipText("Extract Identity Template from EDF File");
        templateImportEIAItem.addActionListener(new ExtractEIATemplateListener());
    }
    
    protected static class ExtractEIATemplateListener implements ActionListener {
        private final String extName = "edf";
        private final String description = "EDF File(*.edf)";
        
        protected final static int ext_len = ".eia".length();

        private File openedEDFFile;
        private File newOpenedEDFFile;
        private String directoryPath;
        private EIAHeader eiaHeader = null;
        private long uid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        private void performActions(){
            if (!generateFileName())
                return;
            
            if (openedEDFFile == null)
                return;
            
            updateEiaTemplateFileList();
            yieldEIAHeader();
            yieldEIATemplateTab();
            yieldEiaTemplateTreeNode();
            
            upateEiaTemplatingBtns(true);
        }

        private boolean generateFileName() {
            openedEDFFile = extractCandidateFileName();
            if (openedEDFFile == null)
                return false;

            try {
                return yieldOutputPath();
            } catch (IOException e) {;}
            return true;
        }
        
        private File extractCandidateFileName(){
            EDFFileFilter filter = new EDFFileFilter(new String[] { extName }, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Extract Identity Template from EDF File");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return null;
            File candFile = chooser.getSelectedFile();

            if (MainWindow.getWorkingDirectory() != null) {
                directoryPath = MainWindow.getWorkingDirectory().getAbsolutePath();
            } else {
                directoryPath = candFile.getParent();
            }
            return candFile;
        }
        
       
        private boolean yieldOutputPath() throws IOException {
            String tempPath = removeExtName(openedEDFFile.getName()); 
            boolean notYetOnDisk = false;
            boolean notYetInTemplateList;
            File tempFile = null;
            while(true){
                tempFile = new File(synthesizeName(tempPath));
                //1. test if already on the disk
                notYetOnDisk = tempFile.createNewFile();
                if (notYetOnDisk)
                    tempFile.delete();
                //2. test if already in the templateFileList;
                if (! Utility.isFileNameCollided(tempFile, EIATemplateFiles))
                    notYetInTemplateList = true;
                else 
                    notYetInTemplateList = false;
                
                //3. go on asking for naming if collision detected
                if (notYetOnDisk && notYetInTemplateList){
                    newOpenedEDFFile = tempFile;
                    break;
                }else{
                    tempPath = getStrFromInputDialog(tempPath);
                    //4. cancel/null value lead to no extraction done
                    if (tempPath == null)
                        return false;
                }
            }
            
            return true;
        }
        
        private String synthesizeName(String fileName){
            return directoryPath + "/" + fileName +  ".eia"; 
        }
        
        private String removeExtName(String fileName){
            int sz = fileName.length();
            return fileName.substring(0, sz - ext_len);
        }
        
        private String getStrFromInputDialog(String fileName){
            String msg = "Invalid file name. Please give another name. ";
            msg += "\n Do not specify the folder name or any extension name of .edf, .eia, or .esa";
            String title = "Specify Name for Extracting Identity Template";
            return (String)JOptionPane.showInputDialog(null, msg, title, 0, null, null, fileName);
        }
        
        //////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////

        private void yieldEIAHeader() {
            RandomAccessFile ras = null;
            try {
                ras = new RandomAccessFile(openedEDFFile, "rw");
            } catch (FileNotFoundException f) {
                f.printStackTrace();
            }

            try {
                eiaHeader = new EIAHeader(ras, openedEDFFile);
            } catch (IOException f) {
                f.printStackTrace();
            }
            
            eiaHeader.setHostEdfFile(newOpenedEDFFile);
        }

        private void yieldEIATemplateTab() {
            EIATemplatePane templatePane = new EIATemplatePane(eiaHeader, newOpenedEDFFile);
            String fileName = newOpenedEDFFile.getAbsolutePath();
            
            tabPane.addTab(newOpenedEDFFile.getName(), eiaTemplateTabIcon, templatePane, fileName);
            int index = MainWindow.tabPane.getTabCount() - 1;
            new CloseTabButton(MainWindow.tabPane, index);
            tabPane.setSelectedIndex(index);

            uid = templatePane.getPid();

            //context sensitive help
            hb.enableHelpKey(templatePane, "hFileATemp", null);
        }

        private void yieldEiaTemplateTreeNode() {
            MainWindow.taskTree.addFileNodeAt(1, newOpenedEDFFile, uid);
        }
        
        private void updateEiaTemplateFileList(){
            EIATemplateFiles.add(newOpenedEDFFile);
        }
        
        private void upateEiaTemplatingBtns(boolean active){
            fileRenameItem.setEnabled(active);
        }
        
    } // end of ExtractEIATemplateListener
    

    private void createTemplateImportESAItem() {
        templateImportESAItem = new JMenuItem("Extract Signal Template from EDF File");
        templateImportESAItem.setIcon(templateExtractEiaIcon);
        templateImportESAItem.addActionListener(new ExtractESATemplateListener());
    }
    
    protected static class ExtractESATemplateListener implements ActionListener {
        private final String extName = "edf";
        private final String description = "EDF File(*.edf)";
        
        protected final static int ext_len = ".esa".length();

        private File openedEDFFile;
        private File newOpenedEDFFile;
        private String directoryPath;
        private ESAHeader esaHeader = null;
        private long uid;
        
        public void actionPerformed(ActionEvent e) {
            performActions();
        }
        
        private void performActions(){
            if (!generateFileName())
                return;
            
            if (openedEDFFile == null)
                return;
            
            updateEsaTemplateFileList();
            yieldESAHeader();
            yieldESATemplateTab();
            yieldESATemplateTreeNode();
            addRowButton.setEnabled(true);
            removeRowButton.setEnabled(true);
            
            updateEsaTemplatingBtns(true);
        }
        
        private boolean generateFileName() {
            openedEDFFile = extractCandidateFileName();
            if (openedEDFFile == null)
                return false;

            try {
                return yieldOutputPath();
            } catch (IOException e) {;}
            return true;
        }

        private File extractCandidateFileName() {
            EDFFileFilter filter = new EDFFileFilter(new String[] {extName}, description);

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Extract Signal Template from EDF File");

            int option = chooser.showOpenDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return null;
            File candFile = chooser.getSelectedFile();

            if (MainWindow.getWorkingDirectory() != null) {
                directoryPath = MainWindow.getWorkingDirectory().getAbsolutePath();
            } else {
                directoryPath = candFile.getParent();
            }
            return candFile;
        }
        
        private boolean yieldOutputPath() throws IOException {
            String tempPath = removeExtName(openedEDFFile.getName()); 
            boolean notYetOnDisk = false;
            boolean notYetInTemplateList;
            File tempFile = null;
            while(true){
                tempFile = new File(synthesizeName(tempPath));
                
                //1. test if already on the disk
                notYetOnDisk = tempFile.createNewFile();
                if (notYetOnDisk)
                    tempFile.delete();
                
                //2. test if already in the templateFileList;
                if (! Utility.isFileNameCollided(tempFile, ESATemplateFiles))
                    notYetInTemplateList = true;
                else 
                    notYetInTemplateList = false;
                
                //3. go on asking for naming if collision detected
                if (notYetOnDisk && notYetInTemplateList){
                    newOpenedEDFFile = tempFile;
                    break;
                }else{
                    tempPath = getStrFromInputDialog(tempPath);
                    //4. cancel/null value lead to no extraction done
                    if (tempPath == null)
                        return false;
                }
            }
            
            return true;
        }

        private String synthesizeName(String fileName) {
            return directoryPath + "/" + fileName + ".esa";
        }

        private String removeExtName(String fileName) {
            int sz = fileName.length();
            return fileName.substring(0, sz - ext_len);
        }

        private String getStrFromInputDialog(String fileName) {
            String msg = "Invalid file name. Please give another name. ";
            msg += "\n Do not specify the folder name or any extension name of .edf, .eia, or .esa";
            String title = "Specify Name for Extracting Signal Template";
            return (String)JOptionPane.showInputDialog(null, msg, title, 0, null, null, fileName);
        }
        
        private void updateEsaTemplateFileList(){
            ESATemplateFiles.add(newOpenedEDFFile);
        }

        private void yieldESAHeader() {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(openedEDFFile, "rw");
            } catch (FileNotFoundException f) {
                f.printStackTrace();
            }

            EDFFileHeader edfHeader = null;
            edfHeader = new EDFFileHeader(raf, openedEDFFile, false);

            esaHeader = edfHeader.getEsaHeader();
            esaHeader.setHostEdfFile(newOpenedEDFFile);
        }

        private void yieldESATemplateTab() {
            ESATemplatePane templatePane = new ESATemplatePane(esaHeader, false);            
            String fileName = newOpenedEDFFile.getAbsolutePath();
            
            tabPane.addTab(newOpenedEDFFile.getName(), esaTemplateTabIcon, templatePane, fileName);  
            int index = tabPane.getTabCount() - 1;
            
            new CloseTabButton(tabPane, tabPane.getTabCount() - 1);
            MainWindow.tabPane.setSelectedIndex(index);
                    
            uid = templatePane.getPid();
            templatePane.setMasterFile(newOpenedEDFFile);
  
            
          //context sensitive help
            hb.enableHelpKey(templatePane, "hSignalATemp", null);
        }

        private void yieldESATemplateTreeNode() {
             MainWindow.taskTree.addFileNodeAt(2, newOpenedEDFFile, uid);
        }
        
        private void updateEsaTemplatingBtns(boolean active){
            fileRenameItem.setEnabled(active);
        }
  
    } //end of ExtractESATemplateListener
    
    

    private void createTemplateAddRowItem() {
        templateAddRowItem = new JMenuItem("Add row");
        templateAddRowItem.setIcon(editAddRowIcon);
        templateAddRowItem.addActionListener(new TemplateAddRowListener());
    }

    private class TemplateAddRowListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (MainWindow.tabPane.getSelectedComponent() == null)
                return;
            
            BasicEDFPane splitPane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
            
            if (!(splitPane instanceof ESATemplatePane)) 
                return;
            ESATemplatePane contentPane = (ESATemplatePane) splitPane;
            int nrow = 0;
            ESATemplateTable table = contentPane.getEsaTemplateTable();
            ESATemplateTableModel model = (ESATemplateTableModel)table.getModel();
            Object row[] = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };
            model.addRow(row);
            nrow = model.getRowCount();
            
            String rowstring = Utility.currentTimeToString() + ": row " + nrow + " is inserted.";
            contentPane.appendToLog(rowstring, "edit");
        }
    }

    private void createTemplateRemoveRowItem() {
        templateRemoveRowItem = new JMenuItem("Remove row");
        templateRemoveRowItem.setIcon(editRemoveRowIcon);
        templateRemoveRowItem.addActionListener(new TemplateRemoveRowListener());
    }

    private class TemplateRemoveRowListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
 
            if (MainWindow.tabPane.getSelectedComponent() == null)
                return;
            
            BasicEDFPane splitPane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
            
            if (!(splitPane instanceof ESATemplatePane))
                return;
            ESATemplatePane contentPane = (ESATemplatePane)splitPane;
            int toBeRemovedRow = 0;
            ESATemplateTable table = contentPane.getEsaTemplateTable();
            toBeRemovedRow = table.getSelectedRow();
                
            if (toBeRemovedRow == -1){ // no row in the table
                splitPane.appendToLog("No row removed. select a row to remove it.", "error");
                return;
            }

            ESATableModel model = (ESATableModel)table.getModel();
            model.removeRow(toBeRemovedRow);
            
            
            String logText = Utility.currentTimeToString() + ": row " + (toBeRemovedRow+1) + " is removed.";
            splitPane.appendToLog(logText, "edit");
        }
    }
    
    /**
     * @return the Help Menu
     */
    private JMenu createToolsMenu()
    {
    	JMenu toolsMenu = new JMenu("Tools");
    	toolsMenu.setMnemonic('T');
    	
        createToolsValidateTableItem();
        createAnnotationConverterItem();
        createJEDFToolItem();
        createEDFViewerToolItem();
        
        toolsMenu.add(toolValidateTableItem);
        toolsMenu.add(toolFixTableErrorItem);
        toolsMenu.addSeparator();
    	
    	toolsMenu.add(annotConverterItem);
        toolsMenu.add(jEDFToolItem);
        toolsMenu.add(EDFViewerToolItem);
        
        //context sensitive help
        //hb.enableHelpOnButton(toolsMenu, "menubar.tools", null);
        CSH.setHelpIDString(toolsMenu, "menubar.tools");
    	
    	return toolsMenu;
    }
    
    private void createAnnotationConverterItem()
    {
    	annotConverterItem = new JMenuItem("EDF Annotation Translator");
    	annotConverterItem.addActionListener(new ActionListener() {
    		
                public void actionPerformed(ActionEvent e) {
                	try
                    {
                		/************************************************************** 
                		 * The following code was hidden by Gang Shu on January 14, 2014
                		 **************************************************************/ 
//                    	Runtime rt = Runtime.getRuntime();
//                    	Process p = rt.exec("java -jar Annotation.jar");
//                    	InputStream in = p.getInputStream() ;
//                    	 OutputStream out = p.getOutputStream ();
//                    	 InputStream err = p.getErrorStream() ;
//                    	 
//                    	//do whatever you want
//                    	 //some more code
//                    	 
//                    	 //p.destroy() ;

                		/************************************************************** 
                		 * The above code was added by Gang Shu on January 14, 2014
                		 **************************************************************/ 
                		SubWindowGUI.getInstance();
                    }
                    catch (Exception exc)
                    {
                    	exc.printStackTrace();
                    }
                }
                	
            });
    }


    /**
     * @return the Help Menu
     */
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        createHelpAboutItem();
        createHelpManualItem();
        createHelpMimiItem();
        createHelpEdfItem();

        helpMenu.add(helpAboutItem);
        helpMenu.add(helpManualItem);
        helpMenu.addSeparator();
        helpMenu.add(helpMimiItem);
        helpMenu.add(helpEdfItem);
        
      //context sensitive help
        //hb.enableHelpOnButton(helpMenu, "menubar.help", null);
        CSH.setHelpIDString(helpMenu, "menubar.help");

        return helpMenu;
    }

    private void createHelpAboutItem() {
        helpAboutItem = new JMenuItem("About EDF Header Editor");
        helpAboutItem.setIcon(helpAboutIcon);
        helpAboutItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) { 
                    new AboutListener(new JFrame());
                }
            });
    }
    
    protected static class AboutListener extends JDialog {  
        static final int helpDialogWidth = 520;
        static final int helpDialogHeight = 440;
        JButton okBtn;        
         AboutListener(JFrame frame){
            super(frame, true); // modal
            this.setLocationRelativeTo(frame);
            initUI();
        }

        private void initUI() {
            this.setSize(new Dimension(helpDialogWidth, helpDialogHeight));
            this.setTitle("About PhysioMIMI EDF Header Editor");
            this.setLogo();
            addGUIComponents();
            this.pack();
            this.setVisible(true);
        }
        
        private void addGUIComponents(){
            JPanel mainpane = createMainPanel();
            JPanel okPanel = createOkPanel();  
            setLayout(new BorderLayout());
            this.add(mainpane, BorderLayout.CENTER);
            this.add(okPanel, BorderLayout.SOUTH);
        }
        
        private JPanel createMainPanel(){
            JPanel mainpane =  new JPanel(customizeLayout());
            CellConstraints cc = new CellConstraints();
            
            JLabel logoPanel = createLogoLabel();
            JEditorPane copyrightPane = createCopyrightPane();
            
            mainpane.add(logoPanel, cc.xywh(1, 1, 3, 1));
            mainpane.add(copyrightPane, cc.xy(2, 2));
            
            return mainpane;   
        }
        
        private FormLayout customizeLayout(){
            String layoutStr1 = "2dlu:n, f:p:g, 2dlu:n";
            String layoutStr2 = "f:p:g, f:p:g";
            FormLayout layout = new FormLayout(layoutStr1, layoutStr2);
            
            return layout;
        }
        
         private JPanel createOkPanel(){       
             JPanel okPanel = new JPanel();
/*              okPanel.setMinimumSize(new Dimension(helpDialogWidth, 1*helpDialogHeight/6));
             okPanel.setPreferredSize(new Dimension(helpDialogWidth, 1*helpDialogHeight/6)); */
             
             createeOkBtn();
             okPanel.add(okBtn);
             
             return okPanel;
         }
         
        private void createeOkBtn(){
             okBtn = new JButton("OK");
             InputMap im =
                 okBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             ActionMap am = okBtn.getActionMap();
             im.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
             am.put("Cancel", new AbstractAction(){
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });    
             okBtn.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
         }
         
         private JLabel createLogoLabel(){
             URL url = this.getClass().getClassLoader().getResource("icon/mimilogo128.png");
             ImageIcon image =  new ImageIcon(url, "MIMI Logo "); 
             JLabel imLabel = new JLabel("  ", image, JLabel.CENTER);
             
             imLabel.setPreferredSize(new Dimension(helpDialogWidth, 3*helpDialogHeight/6));
             imLabel.setMaximumSize(new Dimension(helpDialogWidth, 3*helpDialogHeight/6));
            
             //logoPanel.add(imLabel, BorderLayout.CENTER);    
             imLabel.setOpaque(false);
             
             return imLabel;
         }

        private JEditorPane createCopyrightPane() {
            JEditorPane textPane = new JEditorPane();
            textPane.setContentType("text/html");            
            textPane.setText(makeHTMLTable());
           
            textPane.setPreferredSize(new Dimension(helpDialogWidth, 1*helpDialogHeight/6));
            textPane.setMaximumSize(new Dimension(helpDialogWidth, 1*helpDialogHeight/6));
            textPane.setBorder(null);
            textPane.setEditable(false);
            
            return textPane;
        }
        
        private String makeHTMLTable(){
            StringWriter sout = new StringWriter();
            PrintWriter out = new PrintWriter(sout);
            String text1 = "EDF Header Editor", text2 = version_text;
            String text = format(text1, text2);
            text1 = "Developing members";
            text2 = "Nathan Johnson; Fangping Huang; Zendrix Ng; Van Anh Tran; Catherine Jayapandian";
            text = text + format(text1, text2);
            text = appendHeadTail(text);
            
            out.println(text);
            out.close();

            return sout.toString();
        }
        
        
        private String appendHeadTail(String text){
            return "<TABLE width=550>" + text + "</TABLE>";
        }
        
        private String format(String text1, String text2){
            return "<TR><TH align = left>" + text1 + "</TH><TD>" + text2 + "</TD></TR>\n";
        }
        
         
        private void setLogo(){
            BufferedImage image = null;
            try {
                image = ImageIO.read(this.getClass().getResource("/icon/mimilogo.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setIconImage(image);
        }
    }

    private void createHelpManualItem() {
        helpManualItem = new JMenuItem("How to Use");
        helpManualItem.setIcon(helpHowToUseIcon);
        
        //context sensitive help
        hb.enableHelpOnButton(helpManualItem, "intro", null);
    }
    
    protected static String MIMI = "http://mimi.case.edu/";
    private void createHelpMimiItem(){
        helpMimiItem = new JMenuItem("PhysioMIMI Resource Sites");
        helpMimiItem.setIcon(helpResourceSiteIcon);
        helpMimiItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    Utility.openURL(MIMI);
                }
            });
    }
    
    protected static String EDF_SITE = "http://www.edfplus.info/";
    private void createHelpEdfItem(){
        helpEdfItem = new JMenuItem("EDF and EDFPlus Home");
        helpEdfItem.setIcon(helpEDFHomeIcon);
        helpEdfItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    Utility.openURL(EDF_SITE);
                }
            }); 
    }

    private class EDFToolBar extends JToolBar{
        EDFToolBar(){
            super();
            setPreferredSize(new Dimension(32, 32));
            setFloatable(false);
            setBorderPainted(true);
            setOpaque(true);
        }
        
        @Override
        protected void paintComponent(Graphics g){
            if (!isOpaque()){
                super.paintComponent(g);
                return;
            }
            
            int width = this.getWidth();
            int height = this.getHeight();
            Color color1 = new Color(240, 240, 200);
            Color color2 = color1.brighter();
            Graphics2D g2 = (Graphics2D)g;
            
            GradientPaint gp = new GradientPaint(0, 0, color1, width, 0, color2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, width, height);        
            
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        }
    }

    private EDFToolBar createToolBar() {
        EDFToolBar toolBar = new EDFToolBar();
        
      //context sensitive help
        hb.enableHelpKey(toolBar, "overview.toolbar", null);
                
        createNewTaskBtn();
        createAddFilesBtn();
        toolBar.add(newTaskButton);
        toolBar.add(addFilesButton);
        toolBar.addSeparator(new Dimension(10, 20));
        
        createSaveBtn();
        createSaveAllBtn();
        createPrintBtn();
        //createExportBtn();
        createAddRowBtn();
        createRemoveRowBtn();
        createVerifyBtn();
        createSearchCountLabel();
        createSearchTf();
        createSearchPreviousBtn();
        createSearchNextBtn();

        toolBar.add(saveButton);
        toolBar.add(saveAllButton);
        toolBar.add(printButton);
        
        toolBar.addSeparator(new Dimension(10, 20));
        
        createCopyBtn();       
        createCutBtn();
        createPasteBtn();
        toolBar.add(copyButton);
        toolBar.add(cutButton); 
        toolBar.add(pasteButton);
        
        toolBar.addSeparator(new Dimension(10, 20));
        
        createUndoBtn();
        createRedoBtn();
        toolBar.add(undoButton);
        toolBar.add(redoButton);        
        
        toolBar.addSeparator(new Dimension(10, 20));
        
        createApplyEIATemplateBtn();
        createApplyESATemplateBtn();
        toolBar.add(applyEIATemplateButton);
        toolBar.add(applyESATemplateButton);
        toolBar.addSeparator(new Dimension(10, 20)); 
               
        toolBar.add(verifyBtn);
        
        toolBar.addSeparator(new Dimension(10, 20));
        toolBar.add(addRowButton);
        toolBar.add(removeRowButton); 
        
      //Context sensitive help Zendrix 9/10/2010
        createHelpBtn();
        toolBar.addSeparator(new Dimension(10, 20));
        toolBar.add(helpButton);
        
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(searchCount);
        toolBar.add(searchTf);
        //leave some space for searchTf to the rightmost border
        toolBar.add(searchNextBtn);
        toolBar.add(searchPreviousBtn);
        toolBar.add(new JLabel("     "));
                                          
        return toolBar;
    }
    
   private void createNewTaskBtn() {
        newTaskButton = new JButton(fileNewIcon);
        newTaskButton.setBorderPainted(false);
        newTaskButton.setToolTipText("Select EDF files");
        newTaskButton.addActionListener(new SelectFilesListener());
        
      //context sensitive help
        //hb.enableHelpOnButton(newTaskButton, "selection", null);
        CSH.setHelpIDString(newTaskButton, "selection");
    }


     protected static class SelectFilesListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (MainWindow.wkEdfFiles != null && MainWindow.wkEdfFiles.size() > 0) {
                String prompt = "Current task is to be closed. Click on Yes to confirm, or No to cancel.";
                int input =
                    JOptionPane.showConfirmDialog(null, prompt, "Open EDF Files",
                                                  JOptionPane.YES_NO_OPTION);
                if (input != 0)
                    return; // do nothing
            }
            new NewTask_for_ValidityCommandLine(new JFrame());
        }
    } 
    
    private void createAddFilesBtn(){
        addFilesButton = new JButton(fileAddFilesIcon);
        addFilesButton.setBorderPainted(false);
        //addFilesButton.setEnabled(false);
        addFilesButton.setToolTipText("add files into current task");
        addFilesButton.addActionListener(new AddFilesAdaptor());
        
      //context sensitive help
        //hb.enableHelpOnButton(addFilesButton, "toolbar.add", null);
        CSH.setHelpIDString(addFilesButton, "toolbar.add");
    }
    
    protected static class AddFilesAdaptor implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            //when there is no task, do nothing
            if (tabPane == null || tabPane.getTabCount() == 0 || !(tabPane.getComponentAt(0) instanceof WorkingTablePane))
                return;
            new AddFilesListener(new JFrame());
        }
    }

    private void createSaveBtn() {
       /*  ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/Save.png")); */
        saveButton = new JButton(fileSaveIcon);
        saveButton.setBorderPainted(false);
        saveButton.setToolTipText("save");
        saveButton.addActionListener(new SaveListener("save"));
        
      //context sensitive help
        //hb.enableHelpOnButton(saveButton, "toolbar.save", null);
        CSH.setHelpIDString(saveButton, "toolbar.save");
    }

    private void createSaveAllBtn() {
        /* ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/SaveAll.png")); */
        saveAllButton = new JButton(fileSaveAllIcon);
        saveAllButton.setBorderPainted(false);
        saveAllButton.setToolTipText("save all");
        
      //context sensitive help
        //hb.enableHelpOnButton(saveAllButton, "toolbar.saveall", null);
        CSH.setHelpIDString(saveAllButton, "toolbar.saveall");
    }

    private void createPrintBtn() {
/*         ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/Print.png")); */
        printButton = new JButton(filePrintIcon);
        printButton.setBorderPainted(false);
        printButton.setToolTipText("print header files");
        printButton.setEnabled(true);
        printButton.addActionListener(new PrintListener());
        
      //context sensitive help
        //hb.enableHelpOnButton(printButton, "toolbar.print", null);
        CSH.setHelpIDString(printButton, "toolbar.print");
    }

/*     private void createExportBtn() {
        ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/Print.png"));
        exportButton = new JButton(imageIcon);
        exportButton.setBorderPainted(false);
        exportButton.setToolTipText("");
    } */

    private void createUndoBtn() {
        undoButton = new JButton();
        undoButton.setAction(undoManager.getUndoAction());
        undoButton.setIcon(editUndoIcon); 
        undoButton.setText("");
        undoButton.setBorderPainted(false);
        undoButton.setToolTipText("undo");
       
        //context sensitive help
        //hb.enableHelpOnButton(undoButton, "toolbar.undo", null);
        CSH.setHelpIDString(undoButton, "toolbar.undo");
        
    }

    private void createRedoBtn() {
        redoButton = new JButton();
        redoButton.setAction(undoManager.getRedoAction());
        redoButton.setIcon(editRedoIcon); 
        redoButton.setText("");
        redoButton.setBorderPainted(false);
        redoButton.setToolTipText("redo");
        
      //context sensitive help
        //hb.enableHelpOnButton(redoButton, "toolbar.redo", null);
        CSH.setHelpIDString(redoButton, "toolbar.redo");
    }
    
    private void createCopyBtn() {           
        copyButton = new JButton(editCopyIcon);
        copyButton.setBorderPainted(false);
        copyButton.addActionListener(new CPCAdapter("Copy"));
        copyButton.setToolTipText("copy");
        
      //context sensitive help
        //hb.enableHelpOnButton(copyButton, "toolbar.copy", null);
        CSH.setHelpIDString(copyButton, "toolbar.copy");
    }

    private void createCutBtn() {
        cutButton = new JButton(editCutIcon);
        cutButton.setBorderPainted(false);
        cutButton.addActionListener(new CPCAdapter("Cut"));
        cutButton.setToolTipText("cut");
        
      //context sensitive help
        //hb.enableHelpOnButton(cutButton, "toolbar.cut", null);
        CSH.setHelpIDString(cutButton, "toolbar.cut");
    }


    private void createPasteBtn() {
        pasteButton = new JButton(editPasteIcon);
        pasteButton.setBorderPainted(false);
        pasteButton.addActionListener(new CPCAdapter("Paste"));
        pasteButton.setToolTipText("paste");
        
      //context sensitive help
        //hb.enableHelpOnButton(pasteButton, "toolbar.paste", null);
        CSH.setHelpIDString(pasteButton, "toolbar.paste");
    }

    private void createApplyEIATemplateBtn() {
        ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/apply-eia.png"));
        applyEIATemplateButton = new JButton(imageIcon);
        applyEIATemplateButton.setBorderPainted(false);
        //applyEIATemplateButton.setText("eia");
        Font font = new Font("Lucide Grande", Font.PLAIN, 11);
        //Font font = new Font(smallSystemFont);
        applyEIATemplateButton.setForeground(new Color(227, 108, 10));
        applyEIATemplateButton.setFont(font);
        //applyTemplateButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        applyEIATemplateButton.setHorizontalTextPosition(SwingConstants.CENTER);
       // applyTemplateButton.putClientProperty("Quaqua.Button.style", "toolBarTab");
        applyEIATemplateButton.setToolTipText("apply identity template");
        
      //context sensitive help
        //hb.enableHelpOnButton(applyEIATemplateButton, "toolbar.hFileA", null);
        CSH.setHelpIDString(applyEIATemplateButton, "toolbar.hFileA");
        applyEIATemplateButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (MainWindow.wkEdfFiles == null)
                        return;
                    BasicEDFPane pane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
                    if (pane == null)
                        return;
                    
                    new ApplyTemplateListener(new JFrame(), "eia");                    
                }
            });
    }
    
    private JButton createApplyESATemplateBtn() {
        ImageIcon imageIcon =
            new ImageIcon(MainWindow.class.getResource("/icon/apply-esa.png"));
        applyESATemplateButton = new JButton(imageIcon);
        applyESATemplateButton.setBorderPainted(false);
        //applyESATemplateButton.setText("esa");
        //Font font = new Font("Lucide Grande", Font.PLAIN, 11);
        Font font = new Font("Lucide Grande", Font.PLAIN, 11);
        applyESATemplateButton.setForeground(new Color(227, 108, 10));
        applyESATemplateButton.setFont(font);
        //applyESATemplateButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        applyESATemplateButton.setHorizontalTextPosition(SwingConstants.CENTER);
       // applyTemplateButton.putClientProperty("Quaqua.Button.style", "toolBarTab");
        applyESATemplateButton.setToolTipText("apply signal template");
        
      //context sensitive help
        //hb.enableHelpOnButton(applyESATemplateButton, "toolbar.hSignalA", null);
        CSH.setHelpIDString(applyESATemplateButton, "toolbar.hSignalA");
        applyESATemplateButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (MainWindow.wkEdfFiles == null)
                        return;
                    BasicEDFPane pane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
                    if (pane == null)
                        return;  
                    new ApplyTemplateListener(new JFrame(), "esa");
                }
            });
        
        return applyESATemplateButton;
    }
    

    private void createAddRowBtn() {
        addRowButton = new JButton(editAddRowIcon);
        addRowButton.setBorderPainted(false);
        addRowButton.setToolTipText("add row");
        addRowButton.addActionListener(new TemplateAddRowListener());
        //addRowButton.setEnabled(false);
        
      //context sensitive help
        //hb.enableHelpOnButton(addRowButton, "toolbar.addRow", null);
        CSH.setHelpIDString(addRowButton, "toolbar.addRow");
    }

    private void createRemoveRowBtn() {
        removeRowButton = new JButton(editRemoveRowIcon);
        removeRowButton.setBorderPainted(false);
        removeRowButton.setToolTipText("remove row");
        removeRowButton.addActionListener(new TemplateRemoveRowListener());
        //removeRowButton.setEnabled(false);
        
      //context sensitive help
        //hb.enableHelpOnButton(removeRowButton, "toolbar.removeRow", null);
        CSH.setHelpIDString(removeRowButton, "toolbar.removeRow");
    }
    
    private void createHelpBtn(){ 
    	helpButton = new JButton(helpIntensiveIcon);
    	helpButton.setToolTipText("Help Button");
    	helpButton.addActionListener(new CSH.DisplayHelpAfterTracking(hb));
    	//context sensitive help
        //hb.enableHelpOnButton(helpButton, "toolbar.help", null);
        CSH.setHelpIDString(helpButton, "toolbar.help");
    }


    ////////////////////////////////////////////////////////////////////////////////
    //////////////// start of window listener method ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////
    /*
     * close the window in a graceful way
     * Fangping, 08/04/2010
     */
    private void gracefulCloseWindow(){
        // non-zero number of tabs means prompt to save work
        if (tabPane.getTabCount() == 0){
            System.exit(0);
            return;
        }
        int option = new JOptionPane().showConfirmDialog(null, "Save Files before exit ? ",
                                                         "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        switch(option){
        case 0:
            new SaveListener("SaveAll").saveAllFilesInAllPanes();
        case 1:
            System.exit(0);
        case 2:
            //do nothing
        default:
            //do nothing;
        } 
    }
    
    public void windowClosing(WindowEvent e) {
        gracefulCloseWindow();
    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowClosed(WindowEvent e) { 
        
    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {
  
    }
    
    private void setLogo(){
        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/icon/mimilogo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setIconImage(image);
    }
    
    public static void setCellContent(String text){
        middleStatusBar.setText(text);        
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////// end of window listener method /////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////
    /////////////////// START of getter and setter ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * @param activeESATable current active ESA table in the ESA tab pane.
     * can only be initialized by the New TaskListener
     * can only be updated by the TaskTree Listener
     *
     */
    public static void setActiveESATableInTabPane(ESATable activeESATable) {
        MainWindow.activeESATableInTabPane = activeESATable;
    }

    public static ESATable getActiveESATableInTabPane() {
        return activeESATableInTabPane;
    }

    public static void setWkEdfFiles(ArrayList<File> wkEdfFiles) {
        MainWindow.wkEdfFiles = wkEdfFiles;
    }
    
    public static void addWkEdfFiles(ArrayList<File> wkEdfFiles)
    {
    	int size = MainWindow.wkEdfFiles.size() + wkEdfFiles.size();
    	ArrayList<File> tempFiles = new ArrayList<File>(size);
    	for(int i = 0; i < MainWindow.wkEdfFiles.size(); i++)
    	{
    		tempFiles.add(i, MainWindow.wkEdfFiles.get(i));
    	}
    	for(int i = MainWindow.wkEdfFiles.size(), j=0; i < size; i++, j++)
    	{
    		tempFiles.add(i, wkEdfFiles.get(j));
    	}
        MainWindow.wkEdfFiles = tempFiles;
    }

    public static ArrayList<File> getWkEdfFiles() {
        return wkEdfFiles;
    }

    public static void setWorkingDirectory(File workingDirectory) {
        MainWindow.workingDirectory = workingDirectory;
    }

    public static File getWorkingDirectory() {
        return workingDirectory;
    }

    public static void setSrcEdfFiles(ArrayList<File> mySrcEdfFiles) {
        srcEdfFiles = mySrcEdfFiles;
    }
    
    /*
     * coded by Zendrix
     * Fangping, 08/19/2010
     */
    public static void addSrcEdfFiles(ArrayList<File> mySrcEdfFiles) {
        
        //Fangping, 08/19/2010
        int sz = mySrcEdfFiles.size();
        for (int i = 0; i < sz; i++)
            srcEdfFiles.add(mySrcEdfFiles.get(i));        
    }

    public static ArrayList<File> getSrcEdfFiles() {
        return srcEdfFiles;
    }
    
    public static void setSrcEdfFileHeaders(ArrayList<EDFFileHeader> srcEdfFileHeaders) {
        MainWindow.srcEdfFileHeaders = srcEdfFileHeaders;
    }
    
    /*
     * zendrix code
     * Fangping, 08/17/2010
     */
    public static void addSrcEdfFileHeaders(ArrayList<EDFFileHeader> FileHeaders) {        
        
        //commented by Fangping, 08/19/2010
        int size = MainWindow.srcEdfFileHeaders.size() + FileHeaders.size();
    	ArrayList<EDFFileHeader> tempHeaders = new ArrayList<EDFFileHeader>(size);
    	for(int i = 0; i < MainWindow.srcEdfFileHeaders.size(); i++)
    	{
    		tempHeaders.add(i, MainWindow.srcEdfFileHeaders.get(i));
    	}
    	for(int i = MainWindow.srcEdfFileHeaders.size(), j=0; i < size; i++, j++)
    	{
    		tempHeaders.add(i, FileHeaders.get(j));
    	}
        MainWindow.srcEdfFileHeaders = tempHeaders; 
    } 
    
    /*
     * Fangping, 08/17/2010
     */
    public static void setDupEdfFileHeaders(ArrayList<EDFFileHeader> srcEdfFileHeaders) {
        MainWindow.dupEdfFileHeaders = srcEdfFileHeaders;
    } 
    
    /*
     * Fangping, 08/17/2010
     */
    public static void addDupEdfFileHeaders(ArrayList<EDFFileHeader> FileHeaders) {
    /*     for (EDFFileHeader header: FileHeaders)
            dupEdfFileHeaders.add(header); */
        // Commnented by Fangping, 08/19/2010
      	int size = MainWindow.dupEdfFileHeaders.size() + FileHeaders.size();
    	ArrayList<EDFFileHeader> tempHeaders = new ArrayList<EDFFileHeader>(size);
    	for(int i = 0; i < MainWindow.dupEdfFileHeaders.size(); i++)
    	{
            tempHeaders.add(i, MainWindow.dupEdfFileHeaders.get(i)); 
    	}
    	for(int i = dupEdfFileHeaders.size(), j=0; i < size; i++, j++)
    	{
            tempHeaders.add(i, FileHeaders.get(j));
    	}
       dupEdfFileHeaders = tempHeaders;  
    }

    /*
     * Fangping, 08/17/2010
     */
    public static ArrayList<EDFFileHeader> getSrcEdfFileHeaders() {
        return srcEdfFileHeaders;
    }
    
     public static ArrayList<EDFFileHeader> getDupEdfFileHeaders() {
        return dupEdfFileHeaders;
    } 

    /*
     * Fangping, 08/17/2010
     */
    public static void setIniEsaTables(ArrayList<ESATable> iniEsaTables) {
        MainWindow.iniEsaTables = iniEsaTables;
    }
     
    /*
     * Fangping, 08/17/2010
     * should clone the iniEsaTables, not copy
     * Fangping, 08/20/2010
     */
     public static void setDupEsaTables(ArrayList<ESATable> iniEsaTables) {
    	MainWindow.dupEsaTables = iniEsaTables;
    } 
    
    /*
     * Fangping, 08/17/2010
     */
    public static void setIniEsaTable(ESATable iniEsaTable, int index) {
    	MainWindow.iniEsaTables.set(index, iniEsaTable);
    }

    public static ArrayList<ESATable> getIniEsaTables() {
        return iniEsaTables;
    }

    public static void setIniEiaTable(EIATable iniEiaTable) {
        MainWindow.iniEiaTable = iniEiaTable;
    }
    
    public static EIATable getIniEiaTable(){
        return iniEiaTable;
    }
    
    /*
     * this one is wrong. 
     * keep it for later correction.
     */
    public static void setDupEiaTable(EIATable iniEiaTable) {
    	MainWindow.dupEiaTable = iniEiaTable;
    } 
   
    public static int getSelectedTabIndex(){
        return tabPane.getSelectedIndex();
    }
    
    public static int getSelectedEDFIndex(){
    	return taskTree.getSelectionRows()[0];
    }
    
    /*
     * called when the window is initialized or when current task is closed
     */
    private static void activateMenuItems(boolean active){
        fileCloseTaskItem.setEnabled(active);
        fileAddFilesItem.setEnabled(active);             

        fileExcludeFileItem.setEnabled(active);
        fileDeleteFileItem.setEnabled(active);
        fileRenameItem.setEnabled(active);
        toolApplyEIATemplateItem.setEnabled(active);
        toolApplyESATemplateItem.setEnabled(active);
    }
    
    private static void activateToolBarItems(boolean active){
        addFilesButton.setEnabled(active);  
        applyEIATemplateButton.setEnabled(active);
        applyESATemplateButton.setEnabled(active);
        addRowButton.setEnabled(active);
        removeRowButton.setEnabled(active);
    }
    
    /**
     * should remove only the primaryTabs
     */
    private static void closePrimaryTabs(){
        tabPane.remove(0);// close the Identity Tab
        tabPane.remove(0);//close the Signal Tab
        tabPane.setPrimaryTabsOpened(false);
    }
    
    protected static void setTaskTreeToNull(){
        taskTree.removeNodeGroupAt(MainWindow.workingDirNode);
        MainWindow.workingDirNode.setUserObject("EDF Files");
    }
    
    /*
     * aggregate all four thype of incompliances into one array list.
     */
    public static ArrayList<Incompliance> aggregateIncompliances(){
        ArrayList<Incompliance> inserted = new ArrayList<Incompliance>();
        //do not mix the adding order       
        insertIncompliances(inserted, eiaIncompliances);
        insertIncompliances(inserted, esaIncompliances);
        insertIncompliances(inserted, eiaTemplateIncompliances);
        insertIncompliances(inserted, esaTemplateIncompliances);
        
        return inserted;
    }
    
    /*
     * The following three methods operates on incompliances
     */
    //insert incomps into aggregateIncompliances
    protected static void insertIncompliances(ArrayList<Incompliance> inserted, ArrayList<Incompliance> insertees){
        for (Incompliance incomp: insertees)
            inserted.add(incomp);
    }
    
   
    protected static void clearErrorListTable(){
        errorListTable.blankOut();        
        ErrorListTable.setIcon(errorListTable, 0);
    }
    
    protected static void setCoreDataStructuresToNull(){
        srcEdfFiles.clear();
        wkEdfFiles.clear();
        iniEiaTable = null;
        dupEiaTable = null;
        iniEsaTables.clear();
        dupEsaTables.clear();
        srcEdfFileHeaders.clear();
        dupEdfFileHeaders.clear();
        eiaIncompliances.clear();
        esaIncompliances.clear();
        //aggregateIncompliances.clear(); 
    }
    
    protected static int indexOfSelectedNode(){
        TreePath path = taskTree.getSelectionPath();
        if (path == null)
            return -1;
        TreeNode selectedNode = (TreeNode) path.getLastPathComponent();
        TreePath parentPath = path.getParentPath();
        if (path == null)
            return -1;
        TreeNode parentNode = (TreeNode) parentPath.getLastPathComponent();
              
        return parentNode.getIndex(selectedNode);
    }
    
    private static void createVerifyBtn(){
        verifyBtn = new JButton(toolsValidateIcon);
        verifyBtn.setToolTipText("verify all headers in current task");
        verifyBtn.addActionListener(new VerifyHeaderListener());
        //context sensitive help
        CSH.setHelpIDString(verifyBtn, "toolbar.verify");
    }
    
    public static class VerifyHeaderListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            try {
                performActions();
            } catch (BadLocationException f) {
                f.printStackTrace();
            }
        }
                
        public void performActions() throws BadLocationException {
            Document doc = MainWindow.consolePane.getDocument();
            String theme;
            
            theme = Utility.currentTimeToString() + ". Parse file headers.\n";
            doc.insertString(doc.getLength(), theme, EDFInfoPane.getTheme());
            ArrayList<Incompliance> aggregateIncompliances = verifyHeaders();
            NewTask_for_ValidityCommandLine.generateInvalidReport(aggregateIncompliances);
        }
        /*
         * obsolete
         */
        void verifyWorkingFilesHeader() throws BadLocationException {
            if (iniEsaTables == null || iniEsaTables.size() == 0)
                return;
            
            int sz = iniEsaTables.size();
            
            Document doc = MainWindow.consolePane.getDocument();
            String theme;
            EDFTable table;
                      
            for (int i = 0; i < sz; i++){
                theme = "\tScanning " + MainWindow.wkEdfFiles.get(i);
                doc.insertString(doc.getLength(), theme, EDFInfoPane.getTheme());

                table = iniEsaTables.get(i);
                table.parseESATable();
                
                if (!table.isEdfValid())
                    theme = "incompliances detected. \n";
                else
                    theme = ". No incompliance detected. \n";                
                doc.insertString(doc.getLength(), theme, EDFInfoPane.getTheme());            
            }
        }
        
        //obsolete
        void verifyTemplateFilesHeader() throws BadLocationException {
            if (ESATemplateFiles == null || ESATemplateFiles.size() == 0)
                return;       
            
            int sz = MainWindow.tabPane.getTabCount();
            ESATemplateTable table;
            ESATemplatePane pane;
            Document doc = MainWindow.consolePane.getDocument();
            String theme;
            int ntempFiles = 0;
            for (int i = 0; i < sz; i++){
                Component comp = MainWindow.tabPane.getComponentAt(i);;
                if (!(comp instanceof ESATemplatePane))
                    continue;
                
                theme = "\tScanning " + MainWindow.ESATemplateFiles.get(ntempFiles);
                ntempFiles++;
                doc.insertString(doc.getLength(), theme, EDFInfoPane.getTheme());

                pane = (ESATemplatePane) comp;                 
                table = pane.getEsaTemplateTable();
    
                table.parseESATable();
                if (!table.isEdfValid())
                    theme = "Incompliances detected. \n";
                else
                    theme = ". No incompliance detected. \n";                
                doc.insertString(doc.getLength(), theme, EDFInfoPane.getTheme());                    
            }
        }
        
        public ArrayList<Incompliance> verifyHeaders(){
        	/**
        	 * [Validation] Validation of EDF Header
        	 */
            cleanupIncompliances();
            parseEIATable();
            parseESATables();
            parseEIATempalteTables();
            parseESATemplateTables();
            ArrayList<Incompliance> aggregateIncompliances = outputValidationToErrorListTable();
            return aggregateIncompliances;
        }
        
        public void cleanupIncompliances(){
            MainWindow.getEiaIncompliances().clear();
            MainWindow.getEsaIncompliances().clear();
            MainWindow.getEiaTemplateIncompliances().clear();
            MainWindow.getEsaTemplateIncompliances().clear();
            MainWindow.errorListTable.blankOut();
        }
        
        public void parseEIATempalteTables(){
            if (tabPane == null)
                return;
            ArrayList<Incompliance> temps = new ArrayList<Incompliance>();
            for (int i = 0; i < tabPane.getTabCount(); i++){
                if (tabPane.getComponentAt(i) instanceof EIATemplatePane){
                    EIATemplatePane tpane = (EIATemplatePane) tabPane.getComponentAt(i);
                    EIATemplateTable ttable = tpane.getPreviewTable();
                    temps = ttable.parseEIATemplateTable();
                    for (Incompliance incomp: temps)
                        MainWindow.getEiaTemplateIncompliances().add(incomp);
                }
            }
        }
        
        public void parseEIATable(){
            if (iniEiaTable == null)
                return;
            MainWindow.setEiaIncompliances(iniEiaTable.parseEIATable());
            
        }
        
        public void parseESATables(){
            if (iniEsaTables == null || iniEsaTables.size() == 0)
                return;
            
            ArrayList<Incompliance> temps = new ArrayList<Incompliance>();
            for (int i = 0; i < iniEsaTables.size(); i++){
                temps = iniEsaTables.get(i).parseESATable();
                for (Incompliance incomp: temps)
                    MainWindow.getEiaIncompliances().add(incomp);
            }
        }
        
        public void parseESATemplateTables(){
            if (tabPane == null)
                return;
            ArrayList<Incompliance> temps = new ArrayList<Incompliance>();
            for (int i = 0; i < tabPane.getTabCount(); i++){
                if (tabPane.getComponentAt(i) instanceof ESATemplatePane){
                    ESATemplatePane tpane = (ESATemplatePane) tabPane.getComponentAt(i);
                    ESATemplateTable ttable = tpane.getEsaTemplateTable();
                    temps = ttable.parseESATable();
                    for (Incompliance incomp: temps)
                        MainWindow.getEsaTemplateIncompliances().add(incomp);
                }
            }
        }

        
        private ArrayList<Incompliance> outputValidationToErrorListTable(){
        	
            ErrorListTable errorTable = MainWindow.getErrorListTable();
           
            ArrayList<Incompliance> aggregateIncompliances = MainWindow.aggregateIncompliances();
//            NewTask_for_ValidityCommandLine.generateInvalidReport(aggregateIncompliances);
            
            int count = aggregateIncompliances.size();
            outputMessage(count);
  
            if (count != 0){
                errorTable.yieldTableFrom(aggregateIncompliances);
                MainWindow.consoleTabPane.remove(1);
                MainWindow.consoleTabPane.insertTab("Error List", null, new JScrollPane(errorTable), null, 1);
                MainWindow.consoleTabPane.setSelectedIndex(1);
            }
            
    		/************************************************************** 
    		 * The following codefix was made by Gang Shu on February 6, 2014
    		 * 
    		 * Bug:
    		 *    "Error number" on the message list does not updated
    		 *    after fixing error and re-running table validation
    		 **************************************************************/ 
            ErrorListTable.setIcon(errorTable, count);
    		/************************************************************** 
    		 * The above codefix was made by Gang Shu on February 6, 2014
    		 **************************************************************/
            
            return aggregateIncompliances;
        }
        
        private void outputMessage(int count){
            String theme;
            if (count == 0){
                theme = "All files in current task are EDF+ compliant.\n";
                printTheme(theme);
            }
            else{
                String placeholder = (count == 1)? " Incmnpliance ":  " Incompliances ";
                theme = "Detected " + count + placeholder + "to EDF+ spcecification. ";
                theme = theme + "Check the error list table for detail. \n";
                printTheme(theme);
            }
        }
        
        
        private void printTheme(String theme){
            Document doc = consolePane.getDocument();
            try {
                doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
            } catch (BadLocationException e) {; }
        }
        
        
        
        void printTitle(File file){
            String theme = "scanning file " + file.getAbsolutePath() + "\n";
            Document doc = MainWindow.consolePane.getDocument();
            
            try {
                doc.insertString(doc.getLength(), theme,
                                 EDFInfoPane.getTheme());
            } catch (BadLocationException e) {;}
        }        
    } //end of VerifyHeaderListener
    
    private void createSearchCountLabel()
    {
    	searchCount = new JLabel("", JLabel.CENTER);
    }
    
    private void createSearchTf() {
               
        searchTf = new JTextField("search in current table");
        searchTf.setPreferredSize(new Dimension(180, 30));
        searchTf.setMinimumSize(new Dimension(180, 30));
        
        searchTf.setMargin(new Insets(0, 20, 0, 0));

        JButton searchBtn = createSearchIconBtn();    
        
        Font oldfont = searchTf.getFont();
        int fsize = oldfont.getSize();
        int fstyle = oldfont.ITALIC;
        String fname = oldfont.getName();
        searchTf.setFont(new Font(fname, fstyle, fsize));
        searchTf.setForeground(Color.gray);
        
        SearchListeners listener = new SearchListeners();
        searchTf.addMouseListener(listener);
        searchTf.addKeyListener(listener);
       
        searchTf.add(searchBtn);
    }
    
  //////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
   class SearchListeners extends MouseAdapter implements ActionListener, KeyListener{
        private int selectedTabindex;
        private EDFTable currentSearchedTable;
        private BasicEDFPane pane;
        private boolean primaryTabOpenned = false;
        private Font oldfont = MainWindow.taskTree.getFont();
        
        public SearchListeners(){
            super();
        }
        
        public void acquireTable(){
            pane = (BasicEDFPane) tabPane.getSelectedComponent();
            selectedTabindex = tabPane.getSelectedIndex();
            
            if (pane instanceof EIATemplatePane){
                currentSearchedTable = null;
                return;
            }
            
            //this few lines could be simplified if BasicEDFPane provide a common
            //interface of getTable();
            //so at the code-cleaning stage, this should be improved.
            //Fangping, 08/29/2010
            if (pane instanceof WorkingTablePane){
                primaryTabOpenned = true;
                currentSearchedTable = ((WorkingTablePane)pane).getEdfTable();    
                return;
            }
            
            if (pane instanceof ESATemplatePane){
                currentSearchedTable = ((ESATemplatePane)pane).getEsaTemplateTable();
                return;
            }         
        }  
        
        public void showMatchingCells()
        {
        	//((EDFTable)currentSearchedTable).clearMatrix();
        	totalCount = 0;
        	if (currentSearchedTable == null)
                return;
            int nrow = currentSearchedTable.getRowCount();
            int ncol = currentSearchedTable.getColumnCount();
            String textInSearchTf = searchTf.getText().trim().toLowerCase();
            String textInCell;
            
            for (int i = 0; i < ncol; i++)
                for (int j = 0; j < nrow; j++){
                    if (currentSearchedTable.getValueAt(j, i) == null)
                        continue;
                    textInCell = (String)currentSearchedTable.getValueAt(j, i);
                    if (textInCell.toLowerCase().contains(textInSearchTf)){ 
                        totalCount++;
                        //((EDFTable)currentSearchedTable).setSearchMatrix(j, i);
                    }   
                }
            System.out.println("\"" + textInSearchTf.trim() + "\"");
            if(textInSearchTf.trim().equals(""))
            {
            	totalCount = 0;
            }
        }
             
        public void searchTableStartFrom(int rowIndex, int colIndex){
            if (currentSearchedTable == null)
                return;
            int nrow = currentSearchedTable.getRowCount();
            int ncol = currentSearchedTable.getColumnCount();
            String textInSearchTf = searchTf.getText().trim().toLowerCase();
            String textInCell;
            
            if (rowIndex == nrow){
                Toolkit.getDefaultToolkit().beep();
                return;
            }            
           
           //since not a retangular shape search, so the search has to be separated to two parts.
            for (int j = rowIndex + 1; j < nrow; j++){
                    if (currentSearchedTable.getValueAt(j, colIndex) == null)
                        continue;
                    textInCell = (String)currentSearchedTable.getValueAt(j, colIndex);
                    if (textInCell.toLowerCase().contains(textInSearchTf)){ //the first match
                        searchCursor = new Point(j, colIndex);
                        currentSearchedTable.getSelectionModel().setSelectionInterval(j, j);
                        currentSearchedTable.getColumnModel().getSelectionModel().setSelectionInterval(colIndex, colIndex); 
                        currentSearchedTable.scrollToVisible(j, colIndex);
                        return;
                    }   
            }
            
            for (int i = colIndex + 1; i < ncol; i++){
                for (int j = 0; j < nrow; j++){
                    if (currentSearchedTable.getValueAt(j, i) == null)
                        continue;
                    textInCell = (String)currentSearchedTable.getValueAt(j, i);
                    if (textInCell.toLowerCase().contains(textInSearchTf)){ //the first match
                        searchCursor = new Point(j, i);
                        currentSearchedTable.getSelectionModel().setSelectionInterval(j, j);
                        currentSearchedTable.getColumnModel().getSelectionModel().setSelectionInterval(i, i); 
                        currentSearchedTable.scrollToVisible(j, i);
                        return;
                    }   
                }
             }
   
                Toolkit.getDefaultToolkit().beep();
        }
        
        
        public void reverseSearchTableStartFrom(int rowIndex, int colIndex){
            if (currentSearchedTable == null)
                return;
            
            String textInSearchTf = searchTf.getText().trim().toLowerCase();
            String textInCell;
            
            if (rowIndex  == 0){
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            //since not a retangular shape search, so the search has to be separated to two parts.
            for (int j = rowIndex - 1; j >= 0; j--){
                if (currentSearchedTable.getValueAt(j, colIndex) == null)
                    continue;
                textInCell = (String)currentSearchedTable.getValueAt(j, colIndex);
                if (textInCell.toLowerCase().contains(textInSearchTf)){ //the first match
                    searchCursor = new Point(j, colIndex);
                    currentSearchedTable.getSelectionModel().setSelectionInterval(j, j);
                    currentSearchedTable.getColumnModel().getSelectionModel().setSelectionInterval(colIndex, colIndex); 
                    currentSearchedTable.scrollToVisible(j, colIndex);
                    return;
                }      
            }
           
            for (int i = colIndex - 1; i >= 0; i--)
                for (int j = rowIndex - 1; j >= 0; j--){
                    if (currentSearchedTable.getValueAt(j, i) == null)
                        continue;
                    textInCell = (String)currentSearchedTable.getValueAt(j, i);
                    if (textInCell.toLowerCase().contains(textInSearchTf)){ //the first match
                        searchCursor = new Point(j, i);
                        currentSearchedTable.getSelectionModel().setSelectionInterval(j, j);
                        currentSearchedTable.getColumnModel().getSelectionModel().setSelectionInterval(i, i); 
                        currentSearchedTable.scrollToVisible(j, i);
                        return;
                    }      
                }
                Toolkit.getDefaultToolkit().beep();
        }
        
        public void keyTyped(KeyEvent e) {/*do nothing*/}
        public void keyPressed(KeyEvent e) {/*do nothing*/}

        public void keyReleased(KeyEvent e) {
        	showMatchingCells();
        	if(totalCount > 0)
        		currentInd = 1;
        	else
        		currentInd = 0;
        	searchCount.setText(currentInd + " of " + totalCount);
            searchTableStartFrom(-1, 0);
        }

        public void mouseClicked(MouseEvent e) {
            JTextField findtf;
            if (!(e.getSource() instanceof JTextField)) {
                return;
            }
            
            findtf = (JTextField)e.getSource();
            findtf.setText("");
            findtf.setFont(oldfont);
            findtf.setForeground(Color.black);

            if (tabPane.getSelectedComponent() == null)
                return;
            acquireTable();
            latestSearchedTable = currentSearchedTable;
        }
        
        public void actionPerformed(ActionEvent e){  
            acquireTable();
            
            if (latestSearchedTable != currentSearchedTable){
                latestSearchedTable = currentSearchedTable;
                searchCursor = new Point(0, 0);
                searchTableStartFrom(0, 0);
                return;
            }            
            
            if (e.getSource() == MainWindow.searchNextBtn){ 
            	currentInd+=1;
            	if(currentInd > totalCount)
            	{
            		currentInd = totalCount;
            	}
            	searchCount.setText(currentInd + " of " + totalCount);
                int rr = searchCursor.x, cc = searchCursor.y;
                searchTableStartFrom(rr, cc);
                return;
            }
            
            if (e.getSource() == MainWindow.searchPreviousBtn){
            	currentInd-=1;
            	if(currentInd < 1)
            	{
            		if(totalCount == 0)
            			currentInd = 0;
            		else
            			currentInd = 1;
            	}
            	searchCount.setText(currentInd + " of " + totalCount);
                int rr = searchCursor.x, cc = searchCursor.y;
                reverseSearchTableStartFrom(rr, cc);
                return;
            }
        }   
    }//end of searchListeners
    
   private static JButton createSearchIconBtn(){
       JButton searchBtn = new JButton(searchIcon);
       searchBtn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
       searchBtn.setContentAreaFilled(false);
       searchBtn.setFocusPainted(false);
       searchBtn.setBounds(2, 6, 20, 20);
       
       return searchBtn;
   }
    
    
    private void createSearchPreviousBtn(){
        searchPreviousBtn = new JButton("Previous", previousIcon);
        searchPreviousBtn.setBorderPainted(false);
        searchPreviousBtn.setFocusPainted(true);
        searchPreviousBtn.addActionListener(new SearchListeners()); //(new MoveSearchAroundListener("previous"));
    }
    
    private void createSearchNextBtn(){
        searchNextBtn = new JButton("Next", nextIcon);
        searchNextBtn.setBorderPainted(false);
        searchNextBtn.setFocusPainted(true);
       // searchNextBtn.setContentAreaFilled(true);
        searchNextBtn.addActionListener(new SearchListeners());//(new MoveSearchAroundListener("next"));
    }
    

    
} //end of the class 




