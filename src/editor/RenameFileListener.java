package editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import table.EDFTableModel;


public class RenameFileListener implements ActionListener {

    private final static int edf_type = 0;
    private final static int eia_type = 1;
    private final static int esa_type = 2;
    private final static int IMMUTE = -1;

    private File oldFile;
    private File freshFile;
    private String freshName;
    boolean renamed = false;
    private int redIndex;
    private EDFTreeNode redNode;
    private int nodeType;
    private ArrayList<File> siblingFiles;

    public RenameFileListener() {
        super();
    }

    public void actionPerformed(ActionEvent e) {
        performActions();
    }

    private void performActions() {
        if (!validateNodeType() || nodeType == IMMUTE)
            return;
        
        retrieveOldeFileName();
        retrieveFreshName(); //get the new VALID name
        if (!renamed)
            return;
        //update data
        updateOnDiskName();
        updateRecordInFileList();
        //update view
        updateCurrentTabPane();
        updateTaskTreeNodeName();
        updateEIATable();
        printMsgToConsole();
    }

    private boolean validateNodeType() {
        TreePath path = MainWindow.taskTree.getSelectionPath();
        if (path == null)
            return false;
        redNode = (EDFTreeNode)path.getLastPathComponent();
        if (redNode.getParent() == MainWindow.workingDirNode)
            nodeType = edf_type;
        else if (redNode.getParent() == MainWindow.eiaTemplateFilesNode)
            nodeType = eia_type;
        else if (redNode.getParent() == MainWindow.esaTemplateFilesNode)
            nodeType = esa_type;
        else
            nodeType = IMMUTE;

        return true;
    }

    /*
     * this method can be improved for oldFile retrieve part.
     */

    private void retrieveOldeFileName() {
        if (nodeType == edf_type) {
            redIndex = MainWindow.workingDirNode.getIndex(redNode);
            oldFile = MainWindow.wkEdfFiles.get(redIndex);
            siblingFiles = MainWindow.wkEdfFiles;
            //oldFile = redNode.getHostFile(); // this is better
            return;
        }

        if (nodeType == eia_type) {
            redIndex = MainWindow.eiaTemplateFilesNode.getIndex(redNode);
            oldFile = redNode.getHostFile();
            siblingFiles = MainWindow.EIATemplateFiles;
            return;
        }

        if (nodeType == esa_type) {
            redIndex = MainWindow.esaTemplateFilesNode.getIndex(redNode);
            oldFile = redNode.getHostFile();
            siblingFiles = MainWindow.ESATemplateFiles;
        }
    }
    
    private void retrieveFreshName() {
        String oldName = oldFile.getName();
        int sz = oldName.length();
        int extname_len = 4; // length of .edf, .eia, .esa
        String oldNameWithoutExt = oldName.substring(0, sz - extname_len);
        String Ext = oldName.substring(sz - extname_len, sz);

        String msg = "Rename " + oldFile.getName() + ".\n Do not specify the folder name or any extension name of .edf, .eia, or .esa";
        String title = "Rename File";

        String input = "";
        String iniValue = oldNameWithoutExt;
        File newFile;
        String dirName = oldFile.getParentFile().getAbsolutePath() + "/";
        do {
            input = (String)JOptionPane.showInputDialog(null, msg, title, 0, null, null, iniValue);
            //no name or no name change, then just leave from renaming
            if (input == null || input.equalsIgnoreCase(oldNameWithoutExt)) {
                newFile = oldFile;
                renamed = false;
                break;
            }
            
            String temp = dirName + input + Ext;
            newFile = new File(temp);
            String msgstr = "Oops! " + newFile.getName() + " is not valid or has been in use";
            try {
                if (!newFile.createNewFile()) {
                    JOptionPane.showMessageDialog(null, msgstr, title, JOptionPane.ERROR_MESSAGE);
                    continue;
                } else {
                    newFile.delete(); //need to improve, Fangping, 08/26/2010
                    if (!Utility.isFileNameCollided(newFile, siblingFiles, redIndex)) {
                        freshFile = newFile;
                        freshName = newFile.getName();
                        renamed = true;
                        break;
                    }
                }
            } catch (IOException e) {
                ;
            }
        } while (true);
    }

    private void updateOnDiskName() {
        if (oldFile.exists())
            oldFile.renameTo(freshFile);
    }

    private void updateTaskTreeNodeName() {
        DefaultTreeModel model = (DefaultTreeModel)MainWindow.taskTree.getModel();
        EDFTreeNode parentNode = (EDFTreeNode)redNode.getParent();
        model.removeNodeFromParent(redNode);
        EDFTreeNode newNode = new EDFTreeNode(freshFile);
        model.insertNodeInto(newNode, parentNode, redIndex);

        TreePath path = new TreePath(newNode.getPath());
        MainWindow.taskTree.scrollPathToVisible(path);
        MainWindow.taskTree.setSelectionPath(path);
    }

    private void updateRecordInFileList() {
        switch (nodeType) {
        case edf_type:
            MainWindow.wkEdfFiles.set(redIndex, freshFile); 
            break;
        case eia_type:
            MainWindow.EIATemplateFiles.set(redIndex, freshFile);
            break;
        case esa_type:
            MainWindow.ESATemplateFiles.set(redIndex, freshFile);
            break;
        default:
            //do nothing;
        }
    }

    private void updateCurrentTabPane() {       
        EDFTabbedPane tabbedPane = MainWindow.tabPane;
        int tabCount = tabbedPane.getTabCount();
        int tabIndex = -1;

       
        // retrieve index of current tab
        BasicEDFPane tempane;
        for (int i = 0; i < tabCount; i++){
            tempane = (BasicEDFPane)tabbedPane.getComponentAt(i);//           
            if (oldFile == tempane.getMasterFile()){
                tabIndex = i;
                break;
            }
        }
        
        //tabbedPane.removeTabAt(tabIndex);

        BasicEDFPane currentPane = (BasicEDFPane)tabbedPane.getComponentAt(tabIndex);
        
        if (currentPane instanceof WorkingTablePane && tabIndex == 1) {
            WorkingTablePane temp = (WorkingTablePane)currentPane;
            temp.setMasterFile(freshFile);
            temp.setTextToFilePathLabel(freshFile.getPath());
            //tabbedPane.insertTab("Signal Header", null, temp, freshFile.getAbsolutePath(), tabIndex);             
            tabbedPane.setToolTipTextAt(tabIndex, freshFile.getPath());
            
            //currentPane.setMasterFile(freshFile);
            return;
        }
        
        ImageIcon icon = null;
        if (currentPane instanceof EIATemplatePane){
            icon = MainWindow.eiaTemplateTabIcon;
        }
        else if (currentPane instanceof ESATemplatePane){
            icon = MainWindow.esaTemplateTabIcon;
        }
        
        tabbedPane.insertTab(freshFile.getName(), icon, currentPane, freshFile.getAbsolutePath(), tabIndex); //?     
        new CloseTabButton(tabbedPane, tabIndex);
        tabbedPane.setToolTipTextAt(tabIndex, freshFile.getPath());
        
       currentPane.setMasterFile(freshFile);   
       tabbedPane.setSelectedIndex(tabIndex);            
    }
    
    
    private void updateEIATable() {
        if (nodeType == edf_type && MainWindow.iniEiaTable != null){
            EDFTableModel model = (EDFTableModel)MainWindow.iniEiaTable.getModel();
            String fileName = freshFile.getName();
            int truncLen = fileName.length() - ".edf".length();
            model.setValueAt(fileName.substring(0, truncLen), redIndex, 0);
        }
    }

    private void printMsgToConsole() {
        String timeStr = Utility.currentTimeToString() + ": ";
        String oldname = oldFile.getAbsolutePath();
        String freshname = freshFile.getAbsolutePath();
        String theme = timeStr + oldname + " is renamed to " + freshname + "\n";

        Document doc = MainWindow.consolePane.getDocument();

        try {
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
        } catch (BadLocationException e) {
            ;
        }
    }

}//End of the listener
