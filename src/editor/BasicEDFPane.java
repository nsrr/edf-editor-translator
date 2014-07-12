package editor;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BasicEDFPane extends JPanel {
    
    protected File masterFile = null;

    private boolean updatedSinceLastSave = true;
    private boolean isPrimaryTab = false;
    
    protected int paneType;
    protected JCheckBox allCellsShownCbox; 

//  denote immutable cells hided or not. 
//  has the same value as cellsShownCbox's selection status

    protected boolean allCellsShown;
    protected static final int type_eiapane = 0;
    protected static final int type_esapane = 1;
    protected static final int type_eiatemplatePane = 2;
    protected static final int type_esatemplatePane = 3;

    public BasicEDFPane() {
    }

    /**
     * TODO
     * append log to log area. All subclassed should use this to append content.
     * @param content the logging content
     * @param logType
     */
   public void appendToLog(String content, String logType) {   
       return; 
    }

   /**
    * TODO
    * @param updatedSinceLastSave
    */
   public void setUpdatedSinceLastSave(boolean updatedSinceLastSave) {
        this.updatedSinceLastSave = updatedSinceLastSave;
    }

    /**
     * TODO
     * @return
     */
    public boolean isUpdatedSinceLastSave() {
        return updatedSinceLastSave;
    }

    /**
     * TODO
     * @param isPrimaryTab
     */
    public void setIsPrimaryTab(boolean isPrimaryTab) {
        this.isPrimaryTab = isPrimaryTab;
    }

    /**
     * TODO
     * @return
     */
    public boolean isIsPrimaryTab() {
        return isPrimaryTab;
    }

    /**
     * TODO
     * @param masterFile
     */
    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    /**
     * TODO
     * @return
     */
    public File getMasterFile() {
        return masterFile;
    }

    /**
     * TODO
     * @param paneType
     */
    public void setPaneType(int paneType) {
        this.paneType = paneType;
    }

    /**
     * TODO
     * @return
     */
    public int getPaneType() {
        return paneType;
    }


    /**
     * TODO
     * @param allCellsShown
     */
    public void setAllCellsShown(boolean allCellsShown) {
        this.allCellsShown = allCellsShown;
    }

    /**
     * TODO
     * @return
     */
    public boolean isAllCellsShown() {
        return allCellsShown;
    }
}
