package editor;

import header.EIAHeader;
import header.ESAHeader;
import table.EIATable;
import table.ESATable;
import table.ESATemplateTable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import javax.swing.text.MutableAttributeSet;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import listener.EDFFileFilter;

import table.EDFTableModel;
import table.EIATemplateTable;


public class SaveListener  implements ActionListener { //extends SwingWorker<Void, Void>
    private final static int SAVE = 0;
    private final static int SAVE_AS = 1;
    private final static int SAVE_ALL = 2;
    
    JProgressBar progressBar;
    private int saveOption;
    private Document doc= MainWindow.consolePane.getDocument();
    private String theme;
    private MutableAttributeSet mas = EDFInfoPane.theme;
    private final static String msg_fail = "Failed! \n";
    private String msg_done = "Completed. \n";
    
    //for Save As
    //private boolean nameAltered = false;
    private int redIndex;
    private ArrayList<File> siblingFiles;
    private int fileType;
    private static final int type_edf = 0;
    private static final int type_eia = 1;
    private static final int type_esa = 2;
    
    private final static String edfExtName = "edf";
    private final static String eiaExtName = "eia";
    private final static String esaExtName = "esa";
    private final static String edfDescription = "EDF File(*.edf, *.EDF)";
    private final static String eiaDescription = "EIA File(*.eia, *.EIA)";
    private final static String esaDescription = "EDF File(*.esa, *.ESA)";
    private final static String saveas_title = "Save As";
    private final static String dots = "......";
    
    private final static int extname_len = 4; //".eia", ".esa", ".edf"'s length
    private File oldFile;
    
    private File oldSrcFile = null;
    private File freshFile = null;
    private BasicEDFPane selectedPane = null;
    private boolean signalBodySaved;
    private boolean identityHeaderSaved;
    
    

    public SaveListener(String choice) {
        saveOption = choiceToInt(choice);
        signalBodySaved = false;
        identityHeaderSaved = false;
    }
    
    public void actionPerformed(ActionEvent e) {
        performActions();
    }
    
    private void performActions(){
        SaveTask task = new SaveTask(saveOption);
        task.execute();
    }

    private int choiceToInt(String choice) {
        if (choice.equalsIgnoreCase("Save"))
            return SAVE;
        else if (choice.equalsIgnoreCase("SaveAs"))
            return SAVE_AS;
        else if (choice.equalsIgnoreCase("SaveAll"))
            return SAVE_ALL;
        else {
            return -1;
        }
    }

    private BasicEDFPane getCurrentPane() {
        return (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
    }
    
    public int getIndexInWkFiles(File masterFile){
        int i = 0;
        ArrayList<File> files = MainWindow.getWkEdfFiles();
        while (i < files.size() && !(files.get(i).getAbsolutePath().equalsIgnoreCase(masterFile.getAbsolutePath()))){
            i++;
        }
        return i;
    }


    /**
     * save the master file of the current pane.
     */
    public void saveFileInCurrentPane() {
        BasicEDFPane pane = getCurrentPane();
        if (pane == null)
            return;
        
        int tabIndex = MainWindow.tabPane.getSelectedIndex();

        if (pane.isIsPrimaryTab()) 
            saveHeadersInPrimaryTab(pane, tabIndex); //done: message to console window
        else if (pane instanceof ESATemplatePane){
            saveESATemplateTable((ESATemplatePane) pane); //done: message to console window
        }
        else if (pane instanceof EIATemplatePane){
            saveEIATemplateTable((EIATemplatePane) pane); //done: message to console window
        }
        else
            System.out.println("pane initialization error");     
    }
    
    public void saveEIATemplateTable(EIATemplatePane pane){ 
        EIATemplateTable previewTable = pane.getPreviewTable();
        EIAHeader header = EIAHeader.getEIAHeaderFromPreviewTable(previewTable);
        File tempFile = pane.getMasterFile();
        
        if (tempFile == null)
            return;
        
        String timestr = Utility.currentTimeToString();
        String path = tempFile.getAbsolutePath();
        theme = timestr + ": Saving EIA template header " + path + dots;
        printMessageToConsole();        

        if (!header.saveToXml(tempFile.getPath())){
            theme = msg_fail;
            printMessageToConsole();
        }
        
        if (freshFile != null) //this is for save as
            previewTable.setMasterFile(freshFile);
        
        theme = msg_done;
        printMessageToConsole();
    }
      
      //obsolete, 10/13/2010
/*     public void saveEIATemplateTable(EIATemplatePane pane){ 
        EIATemplateTable previewTable = pane.getPreviewTable();
        EIAHeader header = EIAHeader.getEIAHeaderFromPreviewTable(previewTable);
        File tempFile = pane.getMasterFile();
        
        if (tempFile == null)
            return;
        
        String timestr = Utility.currentTimeToString();
        String path = tempFile.getAbsolutePath();
        theme = timestr + ": Saving EIA template header " + path + dots;
        printMessageToConsole();
        
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(tempFile, "rw");
            header.writeEiaHeader(raf);
        } catch (Exception e) {
            theme = msg_fail;
            printMessageToConsole();
            e.printStackTrace();
        }
        
        if (freshFile != null) //this is for save as
            previewTable.setMasterFile(freshFile);
        
        theme = msg_done;
        printMessageToConsole();
    } */
    
    public void saveESATemplateTable(ESATemplatePane pane) {
        ESATemplateTable esaTable = pane.getEsaTemplateTable();
        File tempFile = pane.getMasterFile();
        
        String timestr = Utility.currentTimeToString();
        String filename = tempFile.getAbsolutePath();
        theme = timestr + ": Saving ESA template header to " + filename + dots;
        printMessageToConsole();
        
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFile, "rw");
            ESAHeader esaHeader = new ESAHeader(esaTable, true);
            esaHeader.saveToDisk(raf, tempFile, false, true);
        } catch (Exception ex) {
            String message = ex.getMessage();
            JOptionPane.showMessageDialog(null, message, "File Not Found", JOptionPane.ERROR_MESSAGE);
            theme = msg_fail;
            printMessageToConsole();
            ex.printStackTrace();
        }
        
        if (freshFile != null)//for saveas use
            esaTable.setMasterFile(freshFile);

        theme = msg_done;
        printMessageToConsole();       
        esaTable.setUpdateSinceLastSave(false);
    }
            
    

    /**
     * Save header(s) in primary tab
     * @param Pane
     * @param tabIndex 
     */
    public void saveHeadersInPrimaryTab(BasicEDFPane Pane, int tabIndex){
        
        WorkingTablePane currentPane = (WorkingTablePane) Pane;
        
        if (tabIndex == 0) { // EIA File
            EIATable eiaTable = (EIATable) currentPane.getEdfTable();
            String timestr = Utility.currentTimeToString();
            theme = timestr + ": Saving updates on identity headers" + dots;
            printMessageToConsole();
            try {
                saveEiaWkTable(eiaTable);
            } catch (IOException e) {
                theme = msg_fail;
                printMessageToConsole();
                e.printStackTrace();
            }
            
            //print saving message to console window
            theme = msg_done;
            printMessageToConsole();

            return;
        }
        
        if (tabIndex == 1) { // save ESA File
            File masterFile = currentPane.getMasterFile();
            ESATable activeEsaTable = (ESATable) currentPane.getEdfTable();
            String timestr = Utility.currentTimeToString();
            String path = masterFile.getAbsolutePath();
            theme = timestr + ": Saving " + path  + dots;
            printMessageToConsole();
            
            try {
                saveESAWorkingTable(activeEsaTable, masterFile);
            } catch (IOException e) {
                theme = msg_fail;
                printMessageToConsole();
                e.printStackTrace();
            }
            theme = msg_done;
            printMessageToConsole();
        }
    }

    /**
     * Save file headers in Eia working table.
     * @param eiaTable
     * @throws IOException
     */
    public void saveEiaWkTable(EIATable eiaTable) throws IOException {
        int nfiles = eiaTable.getRowCount();

        // copy the body first for the first time of saving
        if (eiaTable.getSavedOnce() == false) {
            try {
                File sourceFile, workFile;
                for (int i = 0; i < nfiles; i++) {
                    sourceFile = MainWindow.getSrcEdfFiles().get(i);
                    workFile = MainWindow.getWkEdfFiles().get(i);
                                       
                    if ((sourceFile != workFile) && MainWindow.iniEsaTables.get(i).getSavedOnce() == false){
                        MainWindow.getSaveProgressBar().setVisible(true);
                        Utility.copyEDFFile(sourceFile, workFile);
                        MainWindow.getSaveProgressBar().setVisible(false);
                        MainWindow.iniEsaTables.get(i).setSavedOnce(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } 
            
            eiaTable.setSavedOnce(true);
        }

        
        for (int i = 0; i < nfiles; i++) {
            EIAHeader eiaHeader = new EIAHeader(eiaTable, i);
            File wkFile = MainWindow.getWkEdfFiles().get(i);
            
            RandomAccessFile raf = null;
            raf = new RandomAccessFile(wkFile, "rw");
            eiaHeader.saveToDisk(raf, wkFile);
            raf.close();
        }
        
        eiaTable.setUpdateSinceLastSave(false);
    }
    
 
    public void saveESAWorkingTable(ESATable esaTable, File masterFile) throws IOException {
        // if had never saved, copy the signal body
        String timestr = Utility.currentTimeToString();
        String path = masterFile.getAbsolutePath();
        theme = timestr + ": Save to " + path + dots;
        printMessageToConsole();
        
        if (esaTable.getSavedOnce() == false){// ? move out this?
            int index = getIndexInWkFiles(masterFile);
            File srcFile = MainWindow.getSrcEdfFiles().get(index);
            if (srcFile != masterFile){
                MainWindow.getSaveProgressBar().setVisible(true);
                try{                    
                    Utility.copyEDFFile(srcFile, masterFile); // set cursor for this
                }
                catch(Exception e){
                    theme = msg_fail;
                    MainWindow.getSaveProgressBar().setVisible(false);
                    printMessageToConsole();
                    e.printStackTrace();
                }
 
            }
            esaTable.setSavedOnce(true);
        }
        
        // if no update after last save, do nothing
/*         if (esaTable.getUpdateSinceLastSave() == true)
            return; */
        
        //write the header to the file
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(masterFile, "rw");
            ESAHeader esaHeader = new ESAHeader(esaTable, false);
            //esaHeader.screenPrintEsaHeader();//only for debug
            esaHeader.saveToDisk(raf, masterFile, true, false);
        } catch (FileNotFoundException fnfe) {
            String message = fnfe.getMessage();
            JOptionPane.showMessageDialog(null, message, "File Not Found", JOptionPane.ERROR_MESSAGE);
            theme = msg_fail;
            printMessageToConsole();
            fnfe.printStackTrace();
        }
        
        theme = msg_done;
        printMessageToConsole();
        esaTable.setUpdateSinceLastSave(false);
    }


    /**
     * save the master file of the current pane as.
     */
    public void saveFileInCurrentPaneAs() {
        selectedPane = getCurrentPane();
        if (selectedPane == null)
            return;
        
        oldFile = selectedPane.getMasterFile();
        int tabIndex = MainWindow.tabPane.getSelectedIndex();
        
        // save nothing and prompt user to select a valid tab
        if ((selectedPane instanceof WorkingTablePane) && tabIndex == 0) {
            showWarngingMsgForSaveAs();
            //saveHeadersInPrimaryTabAs(pane, tabIndex);
            return;
        }
        
        //save an EDF file As
        if ((selectedPane instanceof WorkingTablePane) && (tabIndex == 1)){
            fileType = type_edf;
            siblingFiles = MainWindow.wkEdfFiles;
        }
        
        if (selectedPane instanceof EIATemplatePane){
            fileType = type_eia;
            siblingFiles = MainWindow.EIATemplateFiles;
        }
        
        if (selectedPane instanceof ESATemplatePane){
            fileType = type_esa;
            siblingFiles = MainWindow.ESATemplateFiles;
        }
        
        //redIndex is used to retrieve row in tables
        redIndex = retrieveRedIndex(oldFile, siblingFiles);
        if (fileType == type_edf)
            oldSrcFile = MainWindow.srcEdfFiles.get(redIndex);
        
        boolean saveSuccessed = saveOnPaneMasterFileIAs();
        
        if (saveSuccessed){
            updateFileList();
            updateTaskTree();
            updateTabbedPane();
            updateEIATable();
        }
        
        printMsgWhenSaveASDone(saveSuccessed);                 
        
    }
    
    private void printMsgWhenSaveASDone(boolean success){
        theme = (success)? msg_done: msg_fail;
        printMessageToConsole();
    }
    
    private void formatMsgForSaveAS(){
        String oldname = oldFile.getAbsolutePath();
        String newname = freshFile.getAbsolutePath();
        String timestr = Utility.currentTimeToString();
        theme = timestr + ": Saving "  + oldname + " as " + newname + dots; 
    }
    
    private void updateEIATable(){
        if (fileType == type_edf && MainWindow.iniEiaTable != null){
            EDFTableModel model = (EDFTableModel)MainWindow.iniEiaTable.getModel();
            String fileName = freshFile.getName();
            int truncLen = fileName.length() - ".edf".length();
            model.setValueAt(fileName.substring(0, truncLen), redIndex, 0);
        }
    }
    
    private void updateTabbedPane(){
         EDFTabbedPane tabbedPane = MainWindow.tabPane;
         int tabIndex = tabbedPane.getSelectedIndex();
        
        if (fileType == type_edf) {
            WorkingTablePane tempPane = (WorkingTablePane)tabbedPane.getComponentAt(tabIndex);
            tempPane.setMasterFile(freshFile);
            tempPane.setTextToFilePathLabel(freshFile.getPath());
            //tabbedPane.insertTab("Signal Header", null, tempPane, freshFile.getAbsolutePath(), tabIndex);  
            tabbedPane.setToolTipTextAt(tabIndex, freshFile.getPath());
            
            //currentPane.setMasterFile(freshFile);
            return;
        }
        
        ImageIcon icon = null;
        if (fileType == type_eia){
            icon = MainWindow.eiaTemplateTabIcon;
        }
        else if (fileType == type_esa){
            icon = MainWindow.esaTemplateTabIcon;
        }
        
        tabbedPane.insertTab(freshFile.getName(), icon, selectedPane, freshFile.getAbsolutePath(), tabIndex); //?     
        new CloseTabButton(tabbedPane, tabIndex);
        tabbedPane.setToolTipTextAt(tabIndex, freshFile.getPath());
        
        selectedPane.setMasterFile(freshFile);
        tabbedPane.setSelectedIndex(tabIndex);
    }
    
    private void updateTaskTree(){
        EDFTreeNode parentNode = getParentNode();
        EDFTreeNode redNode = (EDFTreeNode)parentNode.getChildAt(redIndex);
        DefaultTreeModel model = (DefaultTreeModel)MainWindow.taskTree.getModel();
        model.removeNodeFromParent(redNode);
        EDFTreeNode newNode = new EDFTreeNode(freshFile);
        model.insertNodeInto(newNode, parentNode, redIndex);

        TreePath path = new TreePath(newNode.getPath());
        MainWindow.taskTree.scrollPathToVisible(path);
        MainWindow.taskTree.setSelectionPath(path);
    }
    
    private EDFTreeNode getParentNode(){
        switch(fileType){
        case type_edf:
            return MainWindow.taskTree.getEdfRootNode();
        case type_eia:
            return MainWindow.taskTree.getEiaRootNode();
        case type_esa:
            return MainWindow.taskTree.getEsaRootNode();
        default:
            return null;
        }
    }
    
    private void updateFileList(){
            siblingFiles.set(redIndex, freshFile);
    }
    
    private void showWarngingMsgForSaveAs(){
        String msg = "Oops. No file saved. Please select a file first.";
        String title = "Save As";
        int option = JOptionPane.CANCEL_OPTION;
        int msgType = JOptionPane.ERROR_MESSAGE;
        JOptionPane.showConfirmDialog(null, msg, title, option, msgType);
    }
    
    private boolean saveOnPaneMasterFileIAs(){
        try {
            freshFile = acquireFreshFile();
        } catch (IOException e) { return false;}
        
        if (freshFile == null){
            return false;
        }
        
        formatMsgForSaveAS();
        printMessageToConsole();       

        return writeIntoFreshFile(freshFile, fileType);
    }
    
    private boolean writeIntoFreshFile(File newFile, int typeOfFile){
        if (typeOfFile == type_eia){
            selectedPane.setMasterFile(newFile);
            saveEIATemplateTable((EIATemplatePane) selectedPane);
            return true;
        }
        
        if (typeOfFile == type_esa){
            selectedPane.setMasterFile(newFile);
            saveESATemplateTable((ESATemplatePane) selectedPane);
            return true;            
        }

        if (typeOfFile == type_edf) {
            try {
                writeTableInEIAPaneToDisk(newFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
                }
            try {
                writeTableInESAPaneToDisk(newFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
                }
            return true;
        }      
        
        return false;
    }    
    
    private void  writeTableInEIAPaneToDisk(File newFile) throws IOException {
        //WorkingTablePane spane = (WorkingTablePane)selectedPane;
        WorkingTablePane spane = (WorkingTablePane)MainWindow.tabPane.getComponentAt(0);
        EIATable eiaTable = (EIATable)spane.getEdfTable();
        
        if (!signalBodySaved){
            saveSignalBody(newFile); 
        }
        EIAHeader eiaHeader = new EIAHeader(eiaTable, redIndex);
        RandomAccessFile raf = null;
        raf = new RandomAccessFile(newFile, "rw");
        eiaHeader.saveToDisk(raf, newFile);
        raf.close();
        
        identityHeaderSaved = true;   
    }
    
    private void saveSignalBody(File newFile) throws IOException {
        //copy the signal from oldSrcFile, instead of oldFile which may contains only headers
        MainWindow.getSaveProgressBar().setVisible(true);
        Utility.copyEDFFile(oldSrcFile, newFile);
        MainWindow.getSaveProgressBar().setVisible(false);
        signalBodySaved = true;
    }
    
    private void writeTableInESAPaneToDisk(File newFile) throws IOException {
        WorkingTablePane spane = (WorkingTablePane)selectedPane;
        ESATable esaTable = (ESATable)spane.getEdfTable();
        if (!signalBodySaved){
            saveSignalBody(newFile);
        }
        signalBodySaved = false;
               
        RandomAccessFile raf = null;
        raf = new RandomAccessFile(newFile, "rw");      
        ESAHeader esaHeader = new ESAHeader(esaTable, false);
        esaHeader.saveToDisk(raf, newFile, identityHeaderSaved, false);
        raf.close();
        
        esaTable.setSavedOnce(true);
        spane.setMasterFile(newFile);
        esaTable.setUpdateSelectionOnSort(true);
    }
    

    private int retrieveRedIndex(File file, ArrayList<File> files){
        int red = -1;
        String statName = file.getAbsolutePath();
        String tempFileName;
        for (int i= 0; i < files.size(); i++){
            tempFileName = files.get(i).getAbsolutePath();
            if (statName.equalsIgnoreCase(tempFileName)){
                red = i;
                break;
            }
        }
        return red;
    }

    private File acquireFreshFile() throws IOException {
        String oldName = oldFile.getName();
        int sz = oldName.length();
        String oldNameWithoutExt = oldName.substring(0, sz - extname_len);
        String ExtName = oldName.substring(sz - extname_len, sz);//duplicate the ext name
        
        String msg = "Give a new name for " + oldFile.getPath();
        msg += ".\n Do not specify the path or any extension name of .edf, .eia, or .esa";
        String input = "";
        String iniValue = oldNameWithoutExt;
        File newFile;
        String dirName = oldFile.getParentFile().getAbsolutePath() + "/";

        do {
            input = (String)JOptionPane.showInputDialog(null, msg, saveas_title, 0, null, null, iniValue);
            //1. no name or no name change, then just leave from renaming
            if (input == null || input.equalsIgnoreCase(oldNameWithoutExt)) {
                return null;
            }
            
            String tempName = dirName + input + ExtName;
            newFile = new File(tempName);
            String tip;
            //2. test name Collided with sibling files
            boolean nameCollidedWithSiblingFiles = Utility.isFileNameCollided(newFile, siblingFiles, redIndex); 
            //3. test name collided with on-disk files
           boolean  nameColliedeWithOnDiskFiles = !newFile.createNewFile();
            //give more chance of trying naming if the name is the same as some file in the list
            if (nameCollidedWithSiblingFiles){
                tip = "Oops! the name \"" + newFile.getName() + "\" is already in use. Please try another name.";
                JOptionPane.showMessageDialog(null, tip, saveas_title, JOptionPane.ERROR_MESSAGE);
                    continue;
            }
            //otherwise, prompt overrite is file on disk already has the name
            if (!nameCollidedWithSiblingFiles && nameColliedeWithOnDiskFiles) {
                tip = "A file with the name " + newFile.getName() + " alreay exists in the directory. Want to overwrite it? ";
                int option = JOptionPane.showConfirmDialog(null, tip, saveas_title, JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.WARNING_MESSAGE);
                // cancel means user do not want a new name
                if (option == JOptionPane.CANCEL_OPTION) {
                    return null;
                } else {//need to save the file
                     return newFile;
                }
            }
            
            if (!nameCollidedWithSiblingFiles && !nameColliedeWithOnDiskFiles){
                newFile.delete();
                return newFile;
            }         
        } while (true);
    }
    
       
    
    /**
     * the core method for Save As
     * @param pane
     * @param tabIndex
     * obsolete method
     */
    private void saveHeadersInPrimaryTabAs(BasicEDFPane pane, int tabIndex){
        WorkingTablePane  currentPane = (WorkingTablePane ) pane;
        
        if (tabIndex == 0){ //do the same as "save"
            theme = Utility.currentTimeToString() + ": Saving all updates on identity headers......";
            printMessageToConsole();
            
            EIATable eiaTable = (EIATable) currentPane.getEdfTable();
            try {
                saveEiaWkTable(eiaTable);
            } catch (IOException e) {
                theme = msg_fail;
                printMessageToConsole();
                e.printStackTrace();
            }

            theme = msg_done;
            printMessageToConsole();
            return;
        }

        if (tabIndex == 1) {
            File masterFile = currentPane.getMasterFile();
            
            ESATable activeEsaTable = (ESATable)currentPane.getEdfTable();
            File newFile = getNewFileName(masterFile);
                       
            if (newFile == null) // no file if cancel;
                return;
            theme = Utility.currentTimeToString() + ": Saving update on signal header of " + newFile.getAbsolutePath() + "......";
            // if the file remain the same, just save the header
            printMessageToConsole();
            if (newFile.getAbsolutePath().equals(masterFile.getAbsolutePath())){ 
                try {
                    saveESAWorkingTable(activeEsaTable, masterFile);
                } catch (IOException e) {
                    theme = msg_fail;
                    printMessageToConsole();
                    e.printStackTrace();
                }
                
                theme = msg_done;
                printMessageToConsole();
                return;
            }
            
            /*
             * force the new file is created and writted on disk,
             * including the new header and the signal body
             */
            int index = getIndexInWkFiles(masterFile);
            //index is to be replaced by idx as defined following
            //Fangping, 08/20/2010
            int idx = MainWindow.wkEdfFiles.indexOf(masterFile);
            
            masterFile = newFile;
            MainWindow.wkEdfFiles.set(index, newFile);
            activeEsaTable.setSavedOnce(false);
            activeEsaTable.setUpdateSinceLastSave(true);

            try {
                saveESAWorkingTable(activeEsaTable, masterFile);
            } catch (IOException e) {
                theme = msg_fail;
                printMessageToConsole();
                e.printStackTrace();
            }
            
               theme = msg_done;
               printMessageToConsole();
               
            /*
             *the following two lines are necessary
             */
            currentPane.setMasterFile(newFile);
            activeEsaTable.setMasterFile(newFile);
        }
    }
    
    /** 
     * serve for save the esa template table as
     */
    private void saveESATemplateTableAs(ESATemplatePane pane){
        ESATemplateTable esaTable = pane.getEsaTemplateTable();
        File oldFile = pane.getMasterFile();
        File newFile = getNewFileName(oldFile);
                
        MainWindow.ESATemplateFiles.remove(oldFile);
        MainWindow.ESATemplateFiles.add(newFile);
        oldFile = newFile;
        
        esaTable.setUpdateSinceLastSave(true);
        pane.setMasterFile(oldFile);
        esaTable.setMasterFile(oldFile);
       
        saveESATemplateTable(pane);           
    }
    
/*     private void saveEIATemplateTableAs(EIATemplatePane pane){
        File oldFile = pane.getMasterFile();
        File newFile = getNewFileName(oldFile);

        oldFile = newFile;
        
        pane.setMasterFile(oldFile);
        
        saveEIATemplateTable(pane);       
    } */


    private File getNewFileName(File oldFile) {
        File newFile;
        //customize the file filter
        EDFFileFilter filter = null;
        if (oldFile.getName().contains(".edf"))
            filter = new EDFFileFilter(new String[] {"edf"}, "EDF Files(*.edf)");
        else if (oldFile.getName().contains(".eia"))
            filter = new EDFFileFilter(new String[] {"eia"}, "EDF Files(*.eia)");
        else if (oldFile.getName().contains(".esa"))
            filter = new EDFFileFilter(new String[] {"esa"}, "EDF Files(*.esa)");

        JFileChooser chooser = new JFileChooser(oldFile);
        chooser.setAcceptAllFileFilterUsed(false); //Fangping, 08/19/2010
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Save As");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setSelectedFile(oldFile);

        int opt = chooser.showSaveDialog(new JFrame());
        if (opt == JFileChooser.CANCEL_OPTION)
            return null;
        newFile = chooser.getSelectedFile();

        return newFile;
    }


    /**
     * save all files, including EDF files and all template files.
     */
    public void saveAllFilesInAllPanes() {
        int ntabs = MainWindow.tabPane.getTabCount();
        if (ntabs < 1)
            return;
        
        for (int i = ntabs - 1; i >= 0; i--) {          
            BasicEDFPane pane = (BasicEDFPane) MainWindow.tabPane.getComponentAt(i);
            
            if (pane instanceof ESATemplatePane){
                saveESATemplateTable((ESATemplatePane) pane);
            }
            
            if (pane instanceof EIATemplatePane){
                saveEIATemplateTable((EIATemplatePane) pane);
            }
            
            if (pane.isIsPrimaryTab() == true){
                saveAllHeadersInPrimaryTabs((WorkingTablePane) pane, i);
            }          
        }
    }


    public void saveAllHeadersInPrimaryTabs(WorkingTablePane pane, int tabIndex){
        if (tabIndex == 0) { // save all EIA headers
            EIATable eiaTable = (EIATable)pane.getEdfTable();
            try {
                saveEiaWkTable(eiaTable);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (tabIndex == 1) { // save all ESA Header
            //new Task
            ESATable esaTable;
            File masterFile;
            
            for (int i = 0; i < MainWindow.iniEsaTables.size(); i++) {
                esaTable = MainWindow.iniEsaTables.get(i);
                masterFile = MainWindow.getWkEdfFiles().get(i);
                try {
                    saveESAWorkingTable(esaTable, masterFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected void createProgressbar(){
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        MainWindow.statusBars.add(progressBar);
        
        progressBar.setVisible(true);       
    }
    
    
    protected class SaveTask extends SwingWorker<Void, Void>{        
        private int saveOptionIndex;
        
        public SaveTask(int index){
            saveOptionIndex = index;
        }

        protected Void doInBackground() {
            MainWindow.tabPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            //MainWindow.getSaveProgressBar().setVisible(true);
            //createProgressbar();
            
            switch (saveOptionIndex) {
            case SAVE:
                saveFileInCurrentPane(); 
                break;
            case SAVE_ALL:
                saveAllFilesInAllPanes(); 
                break;
            case SAVE_AS:
                saveFileInCurrentPaneAs(); 
                break;
            default:
                // do nothing
            }
           
            return null;
        }
        
        public void done(){
            MainWindow.getSaveProgressBar().setVisible(false);
            Toolkit.getDefaultToolkit().beep();
            MainWindow.tabPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void printMessageToConsole(){
        try {
            doc.insertString(doc.getLength(), theme, mas);
        } catch (BadLocationException e) {; }
    }
        

} // end of SaveMenuItemListener
