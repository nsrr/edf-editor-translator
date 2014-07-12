package editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import table.ESATable;


/**
 *  MouseListener is implemented to support popup menu
 */
@SuppressWarnings("serial")
public class TaskTree extends JTree implements TreeSelectionListener, MouseListener {
	
    private static JPopupMenu rootNodePopup = new JPopupMenu();
    private static JPopupMenu leafNodePopup = new JPopupMenu();
    private static JMenuItem newTaskMenu = new JMenuItem("New task");
    private static JMenuItem closeTaskMenu = new JMenuItem("Close task");
    private static JMenuItem renameMenu = new JMenuItem("Rename file");
    private static JMenuItem excludeMenu = new JMenuItem("Exclude file from task");
    private static JMenuItem deleteMenu = new JMenuItem("Delete file");
    private static JMenuItem addFilesMenu = new JMenuItem("Add files");
    //private JMenuItem compareMenu = new JMenuItem("compare with source file");
    private static JMenuItem newEiaTemplateMenu =  new JMenuItem("New identity template");
    private static JMenuItem openEiaTemplateMenu = new JMenuItem("Open identity template");
    private static JMenuItem extractEiaTemplateMenu = new JMenuItem("Extract identity template");
    private static JMenuItem applyEiaTemplateMenu = new JMenuItem("Apply identity template");
    private static JMenuItem newEsaTemplateMenu =  new JMenuItem("New signal template");
    private static JMenuItem openEsaTemplateMenu = new JMenuItem("Open signal template");
    private static JMenuItem extractEsaTemplateMenu = new JMenuItem("Extract signal template");
    private static JMenuItem applyEsaTemplateMenu = new JMenuItem("Apply signal template");
    
    @SuppressWarnings("unused")
	private static JMenuItem saveFileMenu = new JMenuItem("Save File");
    @SuppressWarnings("unused")
	private static JMenuItem saveFileAsMenu = new JMenuItem("Save File As...");
    @SuppressWarnings("unused")
	private static JMenuItem saveAllFilesMenu = new JMenuItem("Save All Files");
    
    private EDFTreeNode rootNode;
    private EDFTreeNode edfRootNode;
    private EDFTreeNode eiaRootNode;
    private EDFTreeNode esaRootNode;
    
    private TreePath rootPath;
    private TreePath edfRootPath;
    private TreePath eiaRootPath;
    private TreePath esaRootPath;
    
    //customize image icon
    public static final ImageIcon edfRootClosedIcon = new ImageIcon(MainWindow.class.getResource("/icon/TreeRoot-closed.png"));
    public static final ImageIcon edfRootOpenIcon = new ImageIcon(MainWindow.class.getResource("/icon/TreeRoot-open.png"));
    public static final ImageIcon edfLeafIcon = new ImageIcon(MainWindow.class.getResource("/icon/Edfleaf.png"));
    public static final ImageIcon eiaLeafIcon = new ImageIcon(MainWindow.class.getResource("/icon/Eialeaf.png"));
    public static final ImageIcon esaLeafIcon = new ImageIcon(MainWindow.class.getResource("/icon/Esaleaf.png"));
    
    //attach listeners
    static {        
        newTaskMenu.addActionListener(new MainWindow.SelectFilesListener());
        closeTaskMenu.addActionListener(new MainWindow.CloseTaskItemListener());
        addFilesMenu.addActionListener(new MainWindow.AddFilesAdaptor());
        
        renameMenu.addActionListener(new RenameFileListener());
        excludeMenu.addActionListener(new ExcludeFileListener(ExcludeFileListener.REMOVE));
        deleteMenu.addActionListener(new ExcludeFileListener(ExcludeFileListener.DELETE));
        
        newEiaTemplateMenu.addActionListener(new MainWindow.NewEIATemplateListener());
        openEiaTemplateMenu.addActionListener(new MainWindow.OpenEIATemplateListener());
        extractEiaTemplateMenu.addActionListener(new MainWindow.ExtractEIATemplateListener());
        applyEiaTemplateMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (MainWindow.wkEdfFiles == null)
                    return;
                new ApplyTemplateListener(new JFrame(), "eia");
            }
        });
        newEsaTemplateMenu.addActionListener(new MainWindow.NewESATemplateListener());
        openEsaTemplateMenu.addActionListener(new MainWindow.OpenESATemplateListener());
        extractEsaTemplateMenu.addActionListener(new MainWindow.ExtractESATemplateListener());
        applyEsaTemplateMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (MainWindow.wkEdfFiles == null)
                    return;
                new ApplyTemplateListener(new JFrame(), "esa");
            }
        });     
        
/*         saveFileMenu.addActionListener(new SaveListener("Save"));
        saveFileAsMenu.addActionListener(new SaveListener("SaveAs"));
        saveAllFilesMenu.addActionListener(new SaveListener("SaveAll")); */
    }
    
    /**
     * TODO	 
     * @param rootNode
     * @param workingEDFsNode
     * @param eiaTemplateNode
     * @param esaTemplateNode
     */
    public TaskTree(EDFTreeNode rootNode,
                    EDFTreeNode workingEDFsNode,
                    EDFTreeNode eiaTemplateNode,
                    EDFTreeNode esaTemplateNode) {
        
        super(rootNode);       
       
        initialzeTreeNodes(rootNode, workingEDFsNode, eiaTemplateNode, esaTemplateNode);
        setlaf();   
    }
    
    /**
     * TODO
     * @param rootNode
     * @param workingEDFsNode
     * @param eiaTemplateNode
     * @param esaTemplateNode
     */
    private void initialzeTreeNodes(EDFTreeNode rootNode,
                    EDFTreeNode workingEDFsNode,
                    EDFTreeNode eiaTemplateNode,
                    EDFTreeNode esaTemplateNode) {
        this.rootNode = rootNode;
        this.edfRootNode = workingEDFsNode;
        this.eiaRootNode = eiaTemplateNode;
        this.esaRootNode = esaTemplateNode;
        
        this.rootNode.add(workingEDFsNode);
        this.rootNode.add(eiaTemplateNode);
        this.rootNode.add(esaTemplateNode);
        
        rootPath = new TreePath(this.rootNode.getPath());
        edfRootPath = new TreePath(this.edfRootNode.getPath());
        eiaRootPath = new TreePath(this.eiaRootNode.getPath());
        esaRootPath = new TreePath(this.esaRootNode.getPath());         
    }
    
    /**
     * TODO
     */
    private void setlaf() {
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.getModel().addTreeModelListener(new  EDFTreeModelListener());
        this.setShowsRootHandles(true);
        this.putClientProperty("JTree.lineStyle", "Horizontal");
        this.addTreeSelectionListener(this);
        this.addMouseListener(this);  
        this.setEditable(false);
        this.setCellRenderer(new EDFTaskTreeCellRenderer());
        //expand layer 1 nodes
        this.setExpandedState(rootPath, true);

    }
    
    /**
     * TODO
     */
    private void buildRootNodePopup() {
        rootNodePopup = new JPopupMenu();
        rootNodePopup.add(newTaskMenu);
        rootNodePopup.add(closeTaskMenu);
        rootNodePopup.add(addFilesMenu);
        rootNodePopup.addSeparator();
/*         rootNodePopup.add(saveAllFilesMenu); 
        saveAllFilesMenu.addActionListener(new SaveListener("SaveAll"));
        rootNodePopup.addSeparator() ;*/
        rootNodePopup.add(newEiaTemplateMenu);
        rootNodePopup.add(openEiaTemplateMenu);
        rootNodePopup.add(extractEiaTemplateMenu);
        rootNodePopup.add(applyEiaTemplateMenu);
        rootNodePopup.addSeparator();
        rootNodePopup.add(newEsaTemplateMenu);
        rootNodePopup.add(openEsaTemplateMenu);
        rootNodePopup.add(extractEsaTemplateMenu);
        rootNodePopup.add(applyEsaTemplateMenu);
        
        rootNodePopup.setLightWeightPopupEnabled(true);
        
    }
    
    /**
     * TODO
     */
    private void buildLeafNodePopup() {
        leafNodePopup = new JPopupMenu();
        leafNodePopup.add(excludeMenu);
        leafNodePopup.add(deleteMenu);
        leafNodePopup.add(renameMenu);
        //fileNodePopup.add(addFilesMenu);
        //popup.add(compareMenu);
/*         leafNodePopup.addSeparator();
        
        leafNodePopup.add(saveFileMenu);
        saveFileMenu.addActionListener(new SaveListener("Save"));
        leafNodePopup.add(saveFileAsMenu);
        saveFileAsMenu.addActionListener(new SaveListener("SaveAs")); */
        leafNodePopup.addSeparator();
        leafNodePopup.add(applyEiaTemplateMenu);
        leafNodePopup.add(applyEsaTemplateMenu);
        leafNodePopup.setLightWeightPopupEnabled(true);
    }
    
    /** 
     * TODO
     * this is to override the isPathEditable() method in
     * JTree so that only tree pathes with permission can be
     * edited
     */ 
    public boolean isPathEditable(TreePath treePath) {
         if (treePath.toString().equals(rootPath.toString()))
            return false;
         
        if (treePath.toString().equals(edfRootPath.toString()))
           return false;
        
        if (treePath.toString().equals(eiaRootPath.toString()))
           return false;
        
        if (treePath.toString().equals(esaRootPath.toString()))
           return false;

        return true;
    }
    

    /**
     * TODO
     * 1. add file nodes to the parent node;
     * 2. expand and display the node
     * @param parentNode
     * @param files
     */
    public void addNodeGroupAt(EDFTreeNode parentNode,
                               ArrayList<File> files) {
        DefaultTreeModel model = (DefaultTreeModel) this.getModel();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //System.out.println("node file: " + file.getPath());
            EDFTreeNode fileNode = new EDFTreeNode(file);
            model.insertNodeInto(fileNode, parentNode, i);
         }      
                       
        this.expandPath(getPathForRow(1)); // expand all these tree nodes under "EDF working files"
        TreePath path = this.getPathForRow(2); // the first opened edf is at row 2
        this.setSelectionPath(path); //highlight the line on the first EDF file
        this.scrollPathToVisible(path);       
    }
    
    /**
     * TODO
     * @param parentNode
     */
    public void removeNodeGroupAt(EDFTreeNode parentNode) {
       
        DefaultTreeModel model = (DefaultTreeModel) this.getModel(); 
        while (parentNode.getChildCount() != 0){
            MutableTreeNode node = (MutableTreeNode) parentNode.getFirstChild();
            model.removeNodeFromParent(node);
        }
    }
    
    /**
     * TODO 
     * this one will be merged with the next one.
     * @param rootChild
     * @param file
     */
    public void addFileNodeAt(int rootChild, File file) {
   
        EDFTreeNode fileNode = new EDFTreeNode(file);
         
        DefaultTreeModel model = (DefaultTreeModel) this.getModel();
        TreeNode root = (TreeNode) model.getRoot();
        TreeNode eiaRoot = root.getChildAt(rootChild); 

        if (eiaRoot == null) return;
        TreePath path = this.getNextMatch(eiaRoot.toString(), 0, Position.Bias.Forward);
        MutableTreeNode parentNode = (MutableTreeNode) path.getLastPathComponent();    
        
        model.insertNodeInto(fileNode, parentNode, parentNode.getChildCount());
        this.expandPath(path);
        
        TreePath pathOfTheNewNode = new TreePath(fileNode.getPath());
        this.scrollPathToVisible(pathOfTheNewNode);
        this.setSelectionPath(pathOfTheNewNode);    
    }
    
    /**
     * TODO
     * @param rootChild
     * @param file
     * @param uid
     */
    public void addFileNodeAt(int rootChild, File file, long uid) {
    
        EDFTreeNode fileNode = new EDFTreeNode(file);
        fileNode.setUid(uid);
         
        DefaultTreeModel model = (DefaultTreeModel) this.getModel();
        TreeNode root = (TreeNode) model.getRoot();
        TreeNode eiaRoot = root.getChildAt(rootChild); 

        if (eiaRoot == null) return;
        TreePath path = this.getNextMatch(eiaRoot.toString(), 0, Position.Bias.Forward);
        MutableTreeNode parentNode = (MutableTreeNode) path.getLastPathComponent();    
        
        model.insertNodeInto(fileNode, parentNode, parentNode.getChildCount());
        this.expandPath(path);
        
        TreePath pathOfTheNewNode = new TreePath(fileNode.getPath());
        this.scrollPathToVisible(pathOfTheNewNode);
        this.setSelectionPath(pathOfTheNewNode);       
    }

    /**
     * TODO
     * @param e tree selection event
     * Algorithm:
     * 1. aquire the index of the selected file
     * 2. show the table in the tabbed pane
     * 3. update the active ESA table in the tabbed pane
     */
    public void valueChanged(TreeSelectionEvent e) {
        EDFTreeNode selectedNode = (EDFTreeNode)this.getLastSelectedPathComponent();
        if (selectedNode == null)
            return;
        EDFTreeNode parentNode = (EDFTreeNode)selectedNode.getParent();
        if (parentNode == null)
            return;
        
        /*
         * this can be done by directly compare MainWindow.rootNote, etc
         * Fangping, 08/25/2010
         */
        //Object parentNodeInfo = parentNode.getUserObject();
        Object selectedNodeInfo = selectedNode.getUserObject();
        
        File masterFile = null;
        
        if (parentNode == MainWindow.workingDirNode) {
            int nodeIndex = parentNode.getIndex(selectedNode);
            setESATableVisible(nodeIndex); 
            masterFile = MainWindow.srcEdfFiles.get(nodeIndex);
            MainWindow.masterFile = masterFile;
            this.outputFileInfoPane(masterFile);
            //setTextInfoPane(masterFile);
            return;
        }
        
        if (parentNode == MainWindow.eiaTemplateFilesNode) {
            int nodeIndex = parentNode.getIndex(selectedNode);
            masterFile = MainWindow.EIATemplateFiles.get(nodeIndex); 
            MainWindow.tabPane.setVisibleofTabWithMasterFile(masterFile);
            if (masterFile !=  null)
                this.outputFileInfoPane(masterFile);
                 //setTextInfoPane(masterFile);

            return;
        }
        
        if (parentNode == MainWindow.esaTemplateFilesNode) {
            int nodeIndex = parentNode.getIndex(selectedNode);
            masterFile = MainWindow.ESATemplateFiles.get(nodeIndex);
            MainWindow.tabPane.setVisibleofTabWithMasterFile(masterFile);
 
            if (masterFile !=  null)
                outputFileInfoPane(masterFile);
                //setTextInfoPane(masterFile);

            return;
        }        
        
       // this.setStatusBarText("");
    }
    
    /**
     * TODO
     * @param file
     */
    private void outputFileInfoPane(File file) {
        MainWindow.fileinfoEdtPane.outputFileInfoWithHtml(file);
    }

   /*  private void setTextInfoPane(File file) {
        EDFInfoPane edtpane = MainWindow.fileinfoEdtPane;
        edtpane.setContentType("text/html");
        String name = "", size = "", date= "";
        if (file != null) {
            name = file.getAbsolutePath();
            size = formatFileSize(file.length());
            date = formatFileDate(file.lastModified());
        }
        
        String list = htmlformat(name, size, date);      
        edtpane.setText(EDFInfoPane.finfoTitle + list);
    }
    
    private String htmlformat(String name, String size, String date){
        String nameht = "<tr><th> Path: " + "</th><td>" + name + "</td></tr>";
        String sizeht = "<tr><th> Size: " + "</th><td>" + size + "</td></tr>";
        String dateht = "<tr><th> Date: " + "</th><td>" + date + "</td></tr>";
        
        return "<table width = 250>" + nameht + sizeht + dateht + "</table>";
    }
    
    private String formatFileSize(long size){
        String lenstr = "";
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(',');
        
        df.setDecimalFormatSymbols(dfs);
        lenstr = df.format(toMegaBytes(size)) + "M bytes";
        
        return lenstr;
    }
    
    private long toMegaBytes(long size){
        int mb = 1000*1000;
        return (size < 1)? 1: (size/mb);
    }
    
    private String formatFileDate(long date){        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");      
        String datestr = sdf.format(date);       
        
        return datestr;
    } */
    
    /**
     * TODO
     * @param tree
     * @param nodes
     * @return
     */
    private TreePath findByNodes(JTree tree, Object[] nodes) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return findTreePath(tree, new TreePath(root), nodes, 0, false);
    }
    
    /**
     * TODO
     * @param tree
     * @param names
     * @return
     */
    private TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return findTreePath(tree, new TreePath(root), names, 0, false);
    }

    /**
     * TODO
     * @param tree
     * @param parent
     * @param nodes
     * @param depth
     * @param byName
     * @return
     */
    private TreePath findTreePath(JTree tree, TreePath parent, Object[] nodes,
                           int depth, boolean byName) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        Object obj = node; // If by name, convert node to a string
        if (byName) {
            obj = obj.toString();
        } 
        if (obj.equals(nodes[depth])) { 
            if (depth == nodes.length - 1) {
                return parent;
            }
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result =
                        findTreePath(tree, path, nodes, depth + 1, byName);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }      
        return null;
    }
    
    /**
     * TODO
     * @param parent
     * @param userObject
     * @return
     */
    public int getIndexOfNodeByName(EDFTreeNode parent, Object userObject) {
        int index = 0;
        
        for (Enumeration e = parent.children(); e.hasMoreElements();) {
            if (userObject.equals(((EDFTreeNode)e.nextElement()).getUserObject()))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * TODO
     * @param index
     */
    public void setESATableVisible(int index) {
        ArrayList<ESATable> esaTables = MainWindow.iniEsaTables;
          
        ESATable active = esaTables.get(index);
        if (MainWindow.tabPane.isPrimaryTabsOpened()) {
            MainWindow.tabPane.remove(1);
            MainWindow.tabPane.setPrimaryTabsOpened(true);
        }
        
        String ft = "Signal Header"; 
       
        WorkingTablePane pane = new WorkingTablePane(active);
        File mfile = MainWindow.wkEdfFiles.get(index);
        pane.setMasterFile(mfile); 
        pane.setTextToFilePathLabel(mfile.getPath());
        active.setMasterFile(mfile);
        
        MainWindow.tabPane.insertTab(ft, null, pane, null, 1);
        MainWindow.tabPane.setSelectedIndex(1);         
        MainWindow.tabPane.setToolTipTextAt(1,  mfile.getPath()); 
              
        MainWindow.setActiveESATableInTabPane(active);
    }

      
    /**
     * TODO
     * @param nodeName
     */
    public void removeFileNode(String nodeName) {
               
        TreePath path = this.getNextMatch(nodeName, 0, Position.Bias.Forward);
        EDFTreeNode redNode = (EDFTreeNode) path.getLastPathComponent();
        EDFTreeNode parentNode = (EDFTreeNode) redNode.getParent();
   
        DefaultTreeModel treeModel = (DefaultTreeModel) this.getModel();
        
          if (parentNode != null) {
              treeModel.removeNodeFromParent(redNode);
        }        
    }
    
    /**
     * TODO
     * @param parentIndex
     * @param uid
     */
    public void removeFileNode(int parentIndex, long uid) {
        DefaultTreeModel model = (DefaultTreeModel) this.getModel();  
        TreeNode root = (TreeNode) model.getRoot();
        
        EDFTreeNode parentNode = (EDFTreeNode) root.getChildAt(parentIndex);
        
        EDFTreeNode redNode = null;
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            redNode = (EDFTreeNode) parentNode.getChildAt(i);
            if (redNode.getUid() ==  uid)
                break;
        }
        model.removeNodeFromParent(redNode);
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) { //e.getButton() to identify left or right click

        int selRow = this.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = this.getPathForLocation(e.getX(), e.getY());

        BasicEDFPane pane =
            (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
        if (pane == null)
            return;

        if (pane instanceof ESATemplatePane) {
            MainWindow.addRowButton.setEnabled(true);
            MainWindow.removeRowButton.setEnabled(true);
        } else {
            MainWindow.addRowButton.setEnabled(false);
            MainWindow.removeRowButton.setEnabled(false);
        }
    }
    
    /**
     * TODO
     * This is for pop up menu
     * @param e MourseEvent
     */
    public void mousePressed(MouseEvent e) {
    	int row = this.getRowForLocation(e.getX(), e.getY());
        if (row == -1)
            return;
        this.setSelectionRow(row);
        TreePath path = this.getSelectionPath();
        EDFTreeNode node = (EDFTreeNode)path.getLastPathComponent();
        if (e.isPopupTrigger()){
            if (node == rootNode || node == edfRootNode || node == eiaRootNode || node == esaRootNode){
                buildRootNodePopup();
                rootNodePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            } else {
                buildLeafNodePopup();
                leafNodePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        int row = this.getRowForLocation(e.getX(), e.getY());
        if (row == -1)
            return;
        this.setSelectionRow(row);
        TreePath path = this.getSelectionPath();
        EDFTreeNode node = (EDFTreeNode)path.getLastPathComponent();
        if (e.isPopupTrigger()){
            if (node == rootNode || node == edfRootNode || node == eiaRootNode || node == esaRootNode){
                buildRootNodePopup();
                rootNodePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            } else {
                buildLeafNodePopup();
                leafNodePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            }
        }
    }
    
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public EDFTreeNode getEiaRootNode() {
        return eiaRootNode;
    }

    public EDFTreeNode getEsaRootNode() {
        return esaRootNode;
    }

    public void setEdfRootNode(EDFTreeNode edfRootNode) {
        this.edfRootNode = edfRootNode;
    }

    public EDFTreeNode getEdfRootNode() {
        return edfRootNode;
    }

    public void setEsaRootNode(EDFTreeNode esaRootNode) {
        this.esaRootNode = esaRootNode;
    }

//    void valueForPathChanged(TreePath path, Object newValue) {
//
//    }
    
    /**
     * TODO
     * this is used to support file name change
     */
    private class EDFTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            
        }

        public void treeNodesInserted(TreeModelEvent e) {
            //System.out.println("2");
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            //System.out.println("3");
            
        }

        public void treeStructureChanged(TreeModelEvent e) {
            //System.out.println("4");
        }
    }
    
    /**
     * TODO
     * class to customize tree layout
     * Fangping, 08/22/2010
     */
    class EDFTaskTreeCellRenderer extends DefaultTreeCellRenderer {
        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf, int row,
                                                      boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                               leaf, row, hasFocus);
            
            // customize expanded node here
            setClosedIcon(edfRootClosedIcon);
            setOpenIcon(edfRootOpenIcon);
            
            if (!leaf) 
                //setClosedIcon(); 
                //sertOpenIcon();
                return this;
            
            EDFTreeNode node = (EDFTreeNode) value;
            File file = node.getHostFile();
            
            switch (getLeafNodeType(value)){
            case 1: // for edf file node
                setIcon(edfLeafIcon);
                setToolTipText(file.getPath());
                break;
            case 2: // for eia file node
                setIcon(eiaLeafIcon);
                setToolTipText(file.getPath());
                break;
            case 3: // for esa file node
                setIcon(esaLeafIcon);
                setToolTipText(file.getPath());
                break;
            default:
                setIcon(edfRootClosedIcon);
                break;              
            }
            
            //setToolTipText(value.toString());
            
            return this;
    
        }
         
        /*
         * 1 <-> edf; 2<->eia; 3 <-> esa
         * Fangping, 08/22/2010
         */
        /**
         * TODO
         * @param value
         * @return
         */
        private int getLeafNodeType(Object value){
            
            EDFTreeNode node = (EDFTreeNode) value;
            EDFTreeNode pnode = (EDFTreeNode) node.getParent();
            
            if (pnode == edfRootNode)
                return 1;
            if (pnode == eiaRootNode)
                return 2;
            if (pnode == esaRootNode)
                return 3;
            if (node == rootNode)
                return 4;
            if (node == edfRootNode)
                return 5;
            if (node == eiaRootNode)
                return 6;
            if (node == esaRootNode)
                return 7;
                    
            return -1;            
        }
    }

}//end of TaskTree class
