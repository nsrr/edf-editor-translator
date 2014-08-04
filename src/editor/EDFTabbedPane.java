package editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.io.File;

import javax.swing.JTabbedPane;

/**
 * An EDF tabbed pane for holding different header table
 */
@SuppressWarnings("serial")
public class EDFTabbedPane extends JTabbedPane {
    
    Boolean primaryTabsOpened = false;
    
    /**
     * Default constructor for EDFTabbedPane
     */
    public EDFTabbedPane() {
        super();
        setdisplay();  
    }
    
    /**
     * Customizes the look of this tabbed pane
     */
    private void setdisplay() {
        this.setPreferredSize(new Dimension(2*MainWindow.MAINWINDOW_DLG_WIDTH/3, 2*MainWindow.MAINWINDOW_DLG_WIDTH/5));
        this.setMaximumSize(new Dimension(2*MainWindow.MAINWINDOW_DLG_WIDTH/3, 2*MainWindow.MAINWINDOW_DLG_WIDTH/5));
        this.setOpaque(true);       
        //this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);       
        //customizeAdvancedUI();        
    }
    
    /**
     * @deprecated
     */
    @SuppressWarnings("unused")
	private void customizeAdvancedUI(){
        //BasicTabbedPaneUI ui = (BasicTabbedPaneUI) this.getUI();      
        
//        UIManager.put("TabbedPane.lightHighlight", UIManager.get("TabbedPane.background") );
//        UIManager.put("TabbedPane.darkShadow", UIManager.get("TabbedPane.background") );
//        UIManager.put("TabbedPane.shadow", UIManager.get("TabbedPane.background") );
    }

    /**
     * Returns insets of the content border
     * @param tabPlacement tab placement indicator
     * @return Insets of the tabbed-pane content border
     */
    protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Paints content border using tab placement indicator and selected tab index
     * @param g the Graphics context in which to paint
     * @param tabPlacement tab placement indicator
     * @param selectedIndex selected tab index
     */
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}

      
    /**
     * Returns the index of this tab with a master file
     * @param masterFileOfNode the master file of this tab
     * @return the index of this tab
     */
    public int getTabIndexWithMasterFile(File masterFileOfNode) {
        return Utility.getTabIndexofMasterFile(masterFileOfNode);         
    }
    
    /**
     * Sets the selected tab index using the master file of this tab
     * @param mFile the master file of this tab
     */
    public void setVisibleOfTabWithMasterFile(File mFile) {
        int tabIndex = this.getTabIndexWithMasterFile(mFile);
        this.setSelectedIndex(tabIndex);
    }

    /**
     * Sets the primary tab opened status
     * @param primaryTabsOpened true to set the primary tab is opened
     */
    public void setPrimaryTabsOpened(Boolean primaryTabsOpened) {
        this.primaryTabsOpened = primaryTabsOpened;
    }

    /**
     * Tests if the primary tab is opened
     * @return true if the primary tab is opened
     */
    public Boolean isPrimaryTabsOpened() {
        return primaryTabsOpened;
    }
    
    /**
     * For gradient background
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (!isOpaque()) {
            super.paintComponent(g);
            return;
        }
        
        int width = this.getWidth();
        int height = this.getHeight();
        Color color1 =  new Color(79, 100, 150); //new Color(74, 214, 241); // new Color(74, 214, 241);
        Color color2 = color1.brighter();
        Graphics2D g2 = (Graphics2D)g;
        
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);        
        
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }
}
