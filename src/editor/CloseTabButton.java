package editor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * CloseTabButton acts as a button to close the tab
 */
@SuppressWarnings("serial")
class CloseTabButton extends JPanel implements ActionListener {
    
  private JTabbedPane pane;
  JButton btClose;
  
  /**
   * Initialize CloseTabButton with a specific JTabbedPane and its index
   * @param pane the JTabbedPane to be closed
   * @param index the tab index where the component should be set
   */
  public CloseTabButton(JTabbedPane pane, int index) {
      
    FormLayout layout = new FormLayout("f:p:g, f:8dlu:n", "f:p:g");
    this.setLayout(layout);     
    CellConstraints cc = new CellConstraints();
    
    this.pane = pane;
    setOpaque(false);
    this.add(new JLabel(pane.getTitleAt(index), pane.getIconAt(index), JLabel.RIGHT), cc.xy(1, 1));
    setCloseButton();  
    add(btClose, cc.xy(2, 1));
    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
    this.setOpaque(false);
    this.setFocusable(false);
    
    pane.setTabComponentAt(index, this);
  }
  
  /**
   * Initialize the close button for closing a tab
   */
  public void setCloseButton() {
      Icon closeIcon = new CloseIcon(); 
      btClose = new JButton(closeIcon);
      btClose.setPreferredSize(new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight()));
      btClose.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
      //btClose.setFocusPainted(false);
      btClose.setBounds(2,2,10,10);
      btClose.setContentAreaFilled(false);
      btClose.addActionListener(this);
  }
  
  	/**
  	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
  	 */
    public void actionPerformed(ActionEvent e) {
      int index = pane.indexOfTabComponent(this);  
      // do not use pane.getSelectedPane() which is wrong
      JPanel templatePane = (JPanel) pane.getComponentAt(index);    
      long uid;
      File tempFile;
      int tempType;
      if (templatePane instanceof EIATemplatePane){
          EIATemplatePane temp = (EIATemplatePane) templatePane;
          uid = temp.getPid();
          tempFile = temp.getMasterFile();
          tempType = 0;
        
          removeNodeFromTree(MainWindow.taskTree, tempType + 1, uid);
          removeTabAt(index);
          unregisterTemplateFile(tempFile, tempType);
      } else if (templatePane instanceof ESATemplatePane) {
          ESATemplatePane temp = (ESATemplatePane) templatePane;
          uid = temp.getPid();
          tempFile = temp.getMasterFile();
          tempType = 1;
        
          removeNodeFromTree(MainWindow.taskTree, tempType + 1, uid);
          removeTabAt(index);
          unregisterTemplateFile(tempFile, tempType);
      } else {
          ErrorFixTemplatePane temp = (ErrorFixTemplatePane) templatePane;
          tempFile = temp.getMasterFile();
        
          removeTabAt(index);
      } 
    }

    /**
     * Removes a tab provided by its index
     * @param index the tab index where the tab should be removed
     */
    private void removeTabAt(int index) {
        if (index != -1) {
            pane.remove(index);
        }
    }
    
    /**
     * Unregisters the template file opened
     * @param tempFile the template file opened
     * @param tempType the type of the template file
     */
    private void unregisterTemplateFile(File tempFile, int tempType) {
        if (tempType == 0)
            MainWindow.EIATemplateFiles.remove(tempFile);
        else
            MainWindow.ESATemplateFiles.remove(tempFile);
    }
    
//  private void removeNodeFromTree(TaskTree tree, String nodeName){
//      tree.removeFileNode(nodeName);
//  }
    
    /**
     * Removes a node from a given TaskTree, the node is denoted by its uid
     * @param tree the tree from which to delete a specific node
     * @param parentIndex the node's parent index
     * @param uid the node's uid
     */
    private void removeNodeFromTree(TaskTree tree, int parentIndex, long uid) {
        tree.removeFileNode(parentIndex, uid);    
    }   
}
