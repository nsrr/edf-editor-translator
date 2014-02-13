package editor;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JPanel;


public class BasicEDFPane extends JPanel {
    
    protected File masterFile = null;

    private boolean updatedSinceLastSave = true;
    private boolean isPrimaryTab = false;
    
    protected int paneType;
    protected JCheckBox allCellsShownCbox; 
    /*
     * denote immutable cells hided or not. 
     * has the same value as cellsShownCbox's selection status
     */ 
    protected boolean allCellsShown;
    protected static final int type_eiapane = 0;
    protected static final int type_esapane = 1;
    protected static final int type_eiatemplatePane = 2;
    protected static final int type_esatemplatePane = 3;

    public BasicEDFPane() {
    }

    /**
     * append log to log area. All subclassed should use this to append content.
     * @param content the logging content
     * @param logType
     */
   public void appendToLog(String content, String logType) {   
       return; 
    }

   public void setUpdatedSinceLastSave(boolean updatedSinceLastSave) {
        this.updatedSinceLastSave = updatedSinceLastSave;
    }

    public boolean isUpdatedSinceLastSave() {
        return updatedSinceLastSave;
    }


    public void setIsPrimaryTab(boolean isPrimaryTab) {
        this.isPrimaryTab = isPrimaryTab;
    }

    public boolean isIsPrimaryTab() {
        return isPrimaryTab;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public File getMasterFile() {
        return masterFile;
    }

    public void setPaneType(int paneType) {
        this.paneType = paneType;
    }

    public int getPaneType() {
        return paneType;
    }


    public void setAllCellsShown(boolean allCellsShown) {
        this.allCellsShown = allCellsShown;
    }

    public boolean isAllCellsShown() {
        return allCellsShown;
    }
}
