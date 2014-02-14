package editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import table.EIATable;
import table.EIATableModel;


public class ExcludeFileListener implements ActionListener{
    protected static final int REMOVE = 0;
    protected static final int DELETE = 1; 
    protected static final String command_remove = "remove";
    protected static final String command_delete = "delete";
    
    private int commandType;
    private File redFile;
    private int redIndex; //used by both file and node operations
    private TreeNode redNode;
   
    
    public ExcludeFileListener(int cmd) {
        super();
        commandType = cmd;
    }
    
    /*
     * Algorithm:
     * (1) pop up warning dialog, with affirmation
     * (2) remove item from iniEsaTables, dupEsaTables, srcEdfFileHeaders, dupEdffileHeaders, 
     *     srcEdfFiles, wkEdfFiles
     * (3) update the task tree;
     * (4) remove the item from EIA primary tab
     * (5) delete the file if commandType = DELETE;
     */    
    public void actionPerformed(ActionEvent e) {
        performActions();
    }

    private void performActions() {
        redFile = retrieveRedFile();
        if (redFile == null)
            return;
        updateESAtables();
        updateESAFileHeaders();
        updateSrcWkFiles();
        updateEIATable();
        updateTaskTree();
        
        if (MainWindow.wkEdfFiles.size() != 0)
            MainWindow.taskinfoEdtPane.outputTaskInfoWithHtml();
        else
            
        
        if (commandType == REMOVE) {
            printDoneMsgToConsole();
            return;
        }
        if (deleteFile() == true)
            printDoneMsgToConsole();
        else
            printErrorMsgToConsole();
    }

    private boolean isAffirmed(File file) {
        String message = "<html>Do you want to remove the selected file  from current task? <p> <center>" + 
                        file.getAbsolutePath() + "</center><p>"
            + "<p><h4>Note: you can restore the file from command \" Add Files\".</h4> </html>";
        String title = "Exclude File from Task";
        int option = JOptionPane.YES_NO_OPTION;     
        int reply = JOptionPane.showConfirmDialog(null, message, title, option);
        
        boolean affirmative = (reply == JOptionPane.YES_OPTION)? true: false;    
        return affirmative;              
    }
    
    private void setRedIndex(int index){
        redIndex = index;
    }
    
    
    private File retrieveRedFile(){
        TreePath path = MainWindow.taskTree.getSelectionPath();
        if (path == null)
            return null;
        redNode = (TreeNode) path.getLastPathComponent();
        if (redNode.getParent() == null || redNode.getParent() != MainWindow.workingDirNode)
            return null;
        int index =  MainWindow.workingDirNode.getIndex(redNode);

        File tbdFile = MainWindow.wkEdfFiles.get(index);
        if (!isAffirmed(tbdFile))
            return null;
        else{
            setRedIndex(index);
            return tbdFile;    
        }
    }
    
    private void updateESAtables(){
        MainWindow.iniEsaTables.remove(redIndex);
        //MainWindow.dupEsaTables.remove(redIndex);// do not use it unless clone() is implemented
    }
    
    private void updateESAFileHeaders(){
        MainWindow.srcEdfFileHeaders.remove(redIndex);
        //MainWindow.dupEdfFileHeaders.remove(redIndex); // do not use it unless clone() is implemented
    }

    private void updateEIATable() {
        EIATable table = MainWindow.iniEiaTable;
        EIATableModel model = (EIATableModel)table.getModel();
        model.removeRow(redIndex);
        if (table.getRowCount() == 0) {
        
            /* MainWindow.tabPane.remove(0);
            MainWindow.tabPane.insertTab("Identity attributes", null,
                                         new BasicEDFPane(),
                                         "no file in current task", 0); */
        }
    }
    
    private void updateSrcWkFiles(){
        MainWindow.wkEdfFiles.remove(redIndex);
        MainWindow.srcEdfFiles.remove(redIndex);
    }
    
    private void updateTaskTree(){
        DefaultTreeModel model = (DefaultTreeModel) MainWindow.taskTree.getModel();      
        DefaultMutableTreeNode priorNode = (DefaultMutableTreeNode)MainWindow.workingDirNode.getChildBefore(redNode);
        DefaultMutableTreeNode afterNode =  (DefaultMutableTreeNode)MainWindow.workingDirNode.getChildAfter(redNode); 
        model.removeNodeFromParent((MutableTreeNode)redNode);
        if (priorNode != null)
            MainWindow.taskTree.setSelectionPath(new TreePath(priorNode.getPath()));
        else if (afterNode != null)
            MainWindow.taskTree.setSelectionPath(new TreePath(afterNode.getPath()));
        else{
            MainWindow.tabPane.remove(1);
            MainWindow.tabPane.insertTab("Signal Header", null, new BasicEDFPane(), "no file in current task", 1);
        }
        
        MainWindow.workingDirNode.setUserObject("EDF Files" + " ( " + MainWindow.wkEdfFiles.size() + " files )");
            
    }
    
        
    private void printDoneMsgToConsole(){
        Document doc = MainWindow.consolePane.getDocument();
        String theme = Utility.currentTimeToString() + ": ";
        
        if (commandType == REMOVE){
            theme = theme + redFile.getName() + " has been removed from current task. \n";
        }
        else{
            theme = theme + redFile.getName() + " has been deleted.\n";
        }

        try {
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
        } catch (BadLocationException e) {;}
    }
    
    private void printErrorMsgToConsole(){
        //just in case of misuse of this method
        if (commandType == 0)
            return;
        
        Document doc = MainWindow.consolePane.getDocument();
        String theme = Utility.currentTimeToString() + ": ";

        theme = " fail to delete " + redFile.getName() + ". \n";
        try {
            doc.insertString(doc.getLength(), theme, EDFInfoPane.theme);
        } catch (BadLocationException e) {;}
    }
    
    private boolean deleteFile(){
        return redFile.delete();    
    }


}
