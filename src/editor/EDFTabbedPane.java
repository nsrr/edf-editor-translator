package editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.io.File;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

@SuppressWarnings("serial")
public class EDFTabbedPane extends JTabbedPane {
    
    Boolean primaryTabsOpened = false;
    
    /**
     * TODO
     */
    public EDFTabbedPane() {
        super();
        setdisplay();  
    }
    
    /**
     * TODO
     */
    private void setdisplay() {
        this.setPreferredSize(new Dimension(2*MainWindow.MAINWINDOW_DLG_WIDTH/3, 2*MainWindow.MAINWINDOW_DLG_WIDTH/5));
        this.setMaximumSize(new Dimension(2*MainWindow.MAINWINDOW_DLG_WIDTH/3, 2*MainWindow.MAINWINDOW_DLG_WIDTH/5));
        this.setOpaque(true);       
        //this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);       
        //customizeAdvancedUI();
        
    }
    
    /**
     * TODO
     */
    @SuppressWarnings("unused")
	private void customizeAdvancedUI(){
        //BasicTabbedPaneUI ui = (BasicTabbedPaneUI) this.getUI();      
        
        /*         UIManager.put("TabbedPane.lightHighlight", UIManager.get("TabbedPane.background") );
        UIManager.put("TabbedPane.darkShadow", UIManager.get("TabbedPane.background") );
        UIManager.put("TabbedPane.shadow", UIManager.get("TabbedPane.background") ); */
    }

    /**
     * TODO
     * @param tabPlacement
     * @return
     */
    protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * TODO
     * @param g
     * @param tabPlacement
     * @param selectedIndex
     */
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
    }

      
    /**
     * TODO
     * @param masterFileOfNode
     * @return
     */
    public int getTabIndexWithMasterFile(File masterFileOfNode) {
        return Utility.getTabIndexofMasterFile(masterFileOfNode);         
    }
    
    /**
     * TODO
     * @param mFile
     */
    public void setVisibleofTabWithMasterFile(File mFile) {
        int tabIndex = this.getTabIndexWithMasterFile(mFile);
        this.setSelectedIndex(tabIndex);
    }

    /**
     * TODO
     * @param primaryTabsOpened
     */
    public void setPrimaryTabsOpened(Boolean primaryTabsOpened) {
        this.primaryTabsOpened = primaryTabsOpened;
    }

    /**
     * TODO
     * @return
     */
    public Boolean isPrimaryTabsOpened() {
        return primaryTabsOpened;
    }
    
    //for gradient background
    /* (non-Javadoc)
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
