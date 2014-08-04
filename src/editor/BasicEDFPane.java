package editor;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * A Base pane for the working area, to be extended for different tab type
 */
@SuppressWarnings("serial")
public class BasicEDFPane extends JPanel {
    
    protected File masterFile = null;
    private boolean updatedSinceLastSave = true;
    private boolean isPrimaryTab = false;
    
    protected int paneType;
    protected JCheckBox allCellsShownCbox; 

//  denote immutable cells hidden or not. 
//  has the same value as cellsShownCbox's selection status
    protected boolean allCellsShown;
    protected static final int type_eiapane = 0;
    protected static final int type_esapane = 1;
    protected static final int type_eiatemplatePane = 2;
    protected static final int type_esatemplatePane = 3;

    /**
     * Default constructor
     */
    public BasicEDFPane() {}

    /**
     * Appends log to log area. All subclasses should use this to append content.
     * @param content the logging content
     * @param logType log type
     */
   public void appendToLog(String content, String logType) {   
       return; 
    }

   /**
    * Sets if it has been updated since last save
    * @param updatedSinceLastSave true if updated since last save
    */
   public void setUpdatedSinceLastSave(boolean updatedSinceLastSave) {
        this.updatedSinceLastSave = updatedSinceLastSave;
    }

    /**
     * Return true or false indicating whether updated since last save
     * @return true if updated since last save
     */
    public boolean isUpdatedSinceLastSave() {
        return updatedSinceLastSave;
    }

    /**
     * Sets if this pane should be primary tab
     * @param isPrimaryTab true this pane should be in primary tab
     */
    public void setIsPrimaryTab(boolean isPrimaryTab) {
        this.isPrimaryTab = isPrimaryTab;
    }

    /**
     * Tests whether this pane is the primary tab
     * @return true if this is the primary tab
     */
    public boolean isIsPrimaryTab() {
        return isPrimaryTab;
    }

    /**
     * Sets master file of this pane
     * @param masterFile the file associated with this pane
     */
    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    /**
     * Returns the master file of this pane
     * @return the master file
     */
    public File getMasterFile() {
        return masterFile;
    }

    /**
     * Sets the pane type
     * @param paneType the type of this pane to set 
     * @see #paneType
     */
    public void setPaneType(int paneType) {
        this.paneType = paneType;
    }

    /**
     * Returns the pane type
     * @return the pane type
     * @see #paneType
     */
    public int getPaneType() {
        return paneType;
    }

    /**
     * Sets whether all cells should be seen
     * @param allCellsShown true if all cells should be seen
     */
    public void setAllCellsShown(boolean allCellsShown) {
        this.allCellsShown = allCellsShown;
    }

    /**
     * Tests if all cells is shown
     * @return true all cells from the table in the pane can be seen
     */
    public boolean isAllCellsShown() {
        return allCellsShown;
    }
}
