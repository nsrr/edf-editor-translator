package table;

import editor.Main;
import editor.MainWindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.io.File;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public class EDFTable extends JTable {

    protected Boolean savedOnce = false; // usage: if the body should be copied in current save
    protected Boolean updateSinceLastSave = false; //false when ESA/EIA table is created; true when template file is created
    protected File masterFile = null; //usage: keep the master file of the table
    protected int masterFileIndex; // index of the master file in the master file array
    
    //represent the category of the table's master header
    //obsolete
    protected int masterHeaderCategory;
    JTextField jtf = new JTextField();
    private boolean edfValid = true; // if all fields conform to EDF specification
    
    public static final Color tableHeaderForegroundClr = new Color(212, 220, 225);
    public static final Color tableHeaderBackgroundClr = new Color(118, 146, 60);
    public static final Color tableOddRowClr = new Color(205, 221, 172);
    public static final Color tableEvenRowClr = new Color(234, 241, 221);
    public static final Color stripBackgroundClr = new Color(102, 78, 130);
    public static final Color stripForegroundClr = new Color(255, 255, 219);    
    public static final Color selectedCellColor = new Color(57, 105, 138);
    
    static final int PHYSICAL_MAXIMUM_INDEX = 4;
    static final int PHYSICAL_MINIMUM_INDEX = 3;   
    static final int DIGITAL_MAXIMUM_INDEX = 6;
    static final int DIGITAL_MINIMUM_INDEX = 5;
    
    static final int TEMPLATE_PHYSICAL_MAXIMUM_INDEX = 5;
    static final int TEMPLATE_PHYSICAL_MINIMUM_INDEX = 4;
    static final int TEMPLATE_DIGITAL_MAXIMUM_INDEX = 7;
    static final int TEMPLATE_DIGITAL_MINIMUM_INDEX = 6;
    
    static final String physical_maximum_nonnumber_error = "Physical Maximum field should contain scalar value. ";
    static final String physical_minimum_nonnumber_error = "Physical Minimum field should contain scalar value. ";
    static final String digital_order_error = "Digtial Maximum should be larger than Digital Minimum.";
    static final String digital_field_blank_error = "Digital field should contain integer value.";
    
    static final int START_DATE_INDEX = 4;
    static final String start_date_error = "Start date needs to be in the form of xx.xx.xx where x are integers";
    static final String altName = (Main.mac_os)? "Command": "Alt";
    
    //to store the indices of immuatable cells
    //protected int immutableFieldIndices[];

    public void setMasterFileIndex(int masterFileIndex) {
        this.masterFileIndex = masterFileIndex;
    }


    public int getMasterFileIndex() {
        return masterFileIndex;
    }

    public void setEdfValid(boolean valid) {
        this.edfValid = valid;
    }

    public boolean isEdfValid() {
        return edfValid;
    }
    
    
    public static class MasterHeaderCategory {
        public static final int EIA_WORKSET = 0;
        public static final int ESA_WORKSET = 1;
        public static final int EIA_TEMPLATE = 2;
        public static final int ESA_TEMPLATE = 3;
    }

    public EDFTable() {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        renderHeader();
    }

    public EDFTable(TableModel tableModel) {
        super(tableModel);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        this.getTableHeader().setReorderingAllowed(false);
    }

    public EDFTable(Boolean forEIATemplate) {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        if (forEIATemplate == true)
            new JTable(new EIATableModel(1));
        else
            new JTable(new ESATemplateTableModel(1));
    }

    public void customizeLook(String dataType) {
        this.getTableHeader().setFont(new Font("Dialog", Font.PLAIN, 16));
        this.setRowHeight((int)(this.getRowHeight() * 2.0));
        this.setCellSelectionEnabled(true);
        this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        if (dataType.equals("eia")) {
            for (int i = 0; i < 2; i++) {
                this.stripTableInColumn(i, stripBackgroundClr,
                                        stripForegroundClr, stripBackgroundClr,
                                        stripForegroundClr);
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               0.7));
            }
            
            for (int i = 2; i < 5; i++) {
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               1.5));
            }

            for (int i = 5; i < 11; i++) {
                this.stripTableInColumn(i, stripBackgroundClr,
                                        stripForegroundClr, stripBackgroundClr,
                                        stripForegroundClr);
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               0.7));
            }
        } else if (dataType.equals("esa"))
            this.stripTableInColumn(8, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
        else if (dataType.equals("esa_template"))
            this.stripTableInColumn(9, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
    }

    private void renderHeader() {
        int ncol = this.getColumnCount();
        for (int i = 0; i < ncol; i++) {
            TableColumn tc = this.getColumnModel().getColumn(i);
            tc.setHeaderRenderer(new EDFTableHeaderRenderer());
        }
    }

/*
 * this function has been reformed in Utility. They have the same functionality
 * Fangping, 09/28/2010
 */
    public void scrollToVisible(int rr, int cc) {
        if (!(getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)getParent();
        Rectangle rect = getCellRect(rr, cc, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }

    public void setSavedOnce(Boolean savedEver) {
        this.savedOnce = savedEver;
    }
    
    public boolean isTableColumnHidden(){
        return (getModel().getColumnCount() != getColumnModel().getColumnCount());
    }

    public Boolean getSavedOnce() {
        return savedOnce;
    }

    public void setUpdateSinceLastSave(Boolean updateSinceLastSave) {
        this.updateSinceLastSave = updateSinceLastSave;
    }

    public Boolean getUpdateSinceLastSave() {
        return updateSinceLastSave;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public File getMasterFile() {
/*         if (this.masterHeaderCategory == MasterHeaderCategory.EIA_WORKSET)
            return null; */
        return masterFile;
    }

    /**
     * obsolete method
     * @param masterHeaderCategory
     */
    public void setMasterHeaderCategory(int masterHeaderCategory) {
        this.masterHeaderCategory = masterHeaderCategory;
    }

    /**
     * obsolete method
     * @return
     */
    public int getMasterHeaderCategory() {
        return masterHeaderCategory;
    }

    /**
     * update all the four attributes.
     * @param saved saved once or not
     * @param updated updated or not since last save
     * @param masterFile master file of the table
     * @param categoryValue categroy of the table
     */
    public void setStatesAllInOne(Boolean saved, Boolean updated,
                                  File masterFile, int categoryValue,
                                  int indexofmasterFile) {
        this.setSavedOnce(saved);
        this.setUpdateSinceLastSave(updated);
        this.setMasterFile(masterFile);
        this.setMasterHeaderCategory(categoryValue);
        this.setMasterFileIndex(indexofmasterFile);
    }
    
    /*
     * used for both ESA and ESA template tables
     * Fangping, 09/29/2010
     */
    public ArrayList<Incompliance> parseESATable(){
        ArrayList<Incompliance> esaIncompliances = new ArrayList<Incompliance>();
        int errorSrcTypeIndex = -1;
        
        int nrow = this.getRowCount(); 
        int dig_max, dig_min;
        int phy_max, phy_min;
        String digmaxStr, digminStr; // keep the temp value of digital min/max value
        String phyminStr, phymaxStr;
        
        Incompliance incomp;
        String description;
        String fileName = this.getMasterFile().getAbsolutePath();
        String incomplianceType; 
        int rowIndex, columnIndex;

         //set up incompliance type
        if (this instanceof ESATable){
            phy_max = PHYSICAL_MAXIMUM_INDEX;
            phy_min = PHYSICAL_MINIMUM_INDEX;
            dig_max = DIGITAL_MAXIMUM_INDEX;
            dig_min = DIGITAL_MINIMUM_INDEX;
            errorSrcTypeIndex = Incompliance.index_incomp_src_esa;
            incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex]; //"ESA"       
        }
        else if (this instanceof ESATemplateTable){
            phy_max = TEMPLATE_PHYSICAL_MAXIMUM_INDEX;
            phy_min = TEMPLATE_PHYSICAL_MINIMUM_INDEX;
            dig_max = TEMPLATE_DIGITAL_MAXIMUM_INDEX;
            dig_min = TEMPLATE_DIGITAL_MINIMUM_INDEX;
            errorSrcTypeIndex = Incompliance.index_incomp_src_esatemplate;
            incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex]; //"ESA Template"
        }
        else
            return esaIncompliances; // at this time, esaIncompliances.size() = 0;
        
        for (int i = 0; i < nrow; i++) {
            phymaxStr = (String)this.getModel().getValueAt(i, phy_max) + "";
            phyminStr = (String)this.getModel().getValueAt(i, phy_min) + "";
            digmaxStr = (String)this.getModel().getValueAt(i, dig_max) + "";
            digminStr = (String)this.getModel().getValueAt(i, dig_min) + "";
            
            //1. verify phymax field
            boolean bPhymax = false;
            try{
                Double.parseDouble(phymaxStr);
                bPhymax = true;
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_esa_phymax;
                rowIndex = i;
                columnIndex = phy_max; 
                incomp = new Incompliance(incomplianceType, description, fileName, 
                                          rowIndex, columnIndex, errorSrcTypeIndex);
                esaIncompliances.add(incomp);
                //MainWindow.getAggregateIncompliances().add(incomp);
            }
            
            //2. verify phymin field
            boolean bPhymin = false;
            try{
                Double.parseDouble(phyminStr);
                bPhymin = true;
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_esa_phymin;
                rowIndex = i;
                columnIndex = phy_min;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, errorSrcTypeIndex);
                esaIncompliances.add(incomp);
                //MainWindow.getAggregateIncompliances().add(incomp);
            }
            
    		/************************************************************** 
    		 * The following codefix was made by Gang Shu on February 6, 2014
    		 * 
    		 * Bug:
    		 *    does not verify phymax > phymin
    		 **************************************************************/ 
            //3. verify phymax > phymin
            if (bPhymax && bPhymin){
            	if (Double.parseDouble(phymaxStr) <= Double.parseDouble(phyminStr)) {
                    setEdfValid(false);
                    description = Incompliance.error_esa_phymaxmin;
                    rowIndex = i;
                    columnIndex = phy_min; // might choose phy_max + 1                
                    incomp = new Incompliance(incomplianceType, description, 
                                              fileName,  rowIndex, columnIndex, errorSrcTypeIndex);    
                    esaIncompliances.add(incomp);
                    //MainWindow.getAggregateIncompliances().add(incomp);
                } 
            }
    		/************************************************************** 
    		 * The above codefix was made by Gang Shu on February 6, 2014
    		 **************************************************************/ 
            
            
           //3. verify digmax field
           boolean bDigmax = false;
           try{
        	   Integer.parseInt(digmaxStr);
        	   bDigmax = true;
           }
           catch(NumberFormatException e){
               setEdfValid(false);
               description = Incompliance.error_esa_digmax;
               rowIndex = i;
               columnIndex = dig_max;
               incomp = new Incompliance(incomplianceType, description, 
                                         fileName, rowIndex, columnIndex, errorSrcTypeIndex);
               esaIncompliances.add(incomp);
               //MainWindow.getAggregateIncompliances().add(incomp);
           }
           
            //4. verify digmin field    
           boolean bDigmin = false;       
            try{
                Integer.parseInt(digminStr);
                bDigmin = true;
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_esa_digmin;
                rowIndex = i;
                columnIndex = dig_min;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, errorSrcTypeIndex);
                esaIncompliances.add(incomp);
                //MainWindow.getAggregateIncompliances().add(incomp);
            }
            
            //5. verify digmax > digmin 
            if (bDigmax && bDigmin){
	            if (Integer.parseInt(digmaxStr) <= Integer.parseInt(digminStr)) {
	                setEdfValid(false);
	                description = Incompliance.error_esa_digmaxmin;
	                rowIndex = i;
	                columnIndex = dig_min; // might choose dig_max + 1                
	                incomp = new Incompliance(incomplianceType, description, 
	                                          fileName,  rowIndex, columnIndex, errorSrcTypeIndex);    
	                esaIncompliances.add(incomp);
	                //MainWindow.getAggregateIncompliances().add(incomp);
	            }
            }
        }
        
        //6. verify if labels are duplicate
        String alabel, blabel;
        boolean repeated;
        final int label_col_index = 0;
        for (int i = 0; i < nrow; i++){
            repeated = false;
            description = Incompliance.error_esa_label + (i + 1); 
            alabel = (String) getModel().getValueAt(i, label_col_index);
            for (int j = i + 1; j < nrow; j++){
                blabel = (String) getModel().getValueAt(j, label_col_index);
                if (alabel.equalsIgnoreCase(blabel)){
                    repeated = true;
                    description = description + ", " + (j + 1);
                }
            }
            
            if (repeated == true){
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName,  i, label_col_index, errorSrcTypeIndex); 
                esaIncompliances.add(incomp);
                //MainWindow.getAggregateIncompliances().add(incomp);
            }
        }  
        
        return esaIncompliances;
    }
    
      
    protected final int[] lowerbounds = {0, 0, 0};
    protected final int[] upperbounds = {31, 12, 99}; // 31 day, 12 month, 2099 years
     
    /**
     * parse EIA table.
     * this one can be used on both EIATable
     */
    public ArrayList<Incompliance> parseEIATable( ) {
        ArrayList<Incompliance> eiaIncompliances = new ArrayList<Incompliance>();
        //index always be Incompliance.index_incomp_src_eia        
        final int srcTypeIndex = Incompliance.index_incomp_src_eia;
        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
        
        int nrow = this.getRowCount();
            
        Incompliance incomp;
        String description;
        String fileName;
        int rowIndex, columnIndex;
        
        int segnumber = 3;
        int seg[] = new int[segnumber];
        String[] descriptions = {Incompliance.warning_eia_day, 
                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
        //int[] lowerbounds = {0, 0, 0};
        //int[] upperbounds = {31, 12, 99}; // 31 day, 12 month, 2099 years
        
       // boolean cellHiding = (getModel().getColumnCount() != getColumnModel().getColumnCount());
        boolean cellHiding = false;
        
        String sd;
        for (int i = 0; i < nrow; i++) {
            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
            fileName = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
            rowIndex = i;
            columnIndex = cellHiding? START_DATE_INDEX - 1: START_DATE_INDEX;
            String[] psd = sd.split("\\.");
            
            //1. check format of dd.mm.yy
            int psdlen = psd.length;
            if (psdlen != segnumber){
                setEdfValid(false);
                description = Incompliance.error_eia_format;
                incomp = new Incompliance( incomplianceType, description, 
                                           fileName, rowIndex, columnIndex, srcTypeIndex);
                //MainWindow.getAggregateIncompliances().add(incomp);
                eiaIncompliances.add(incomp);
                continue;
            } 
            
            //2. check each segment of startdate is number or not
            try{
                for (int j = 0; j < segnumber; j++)
                    seg[j] = Integer.parseInt(psd[j]);
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_eia_dmyint;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, srcTypeIndex);
                //MainWindow.getAggregateIncompliances().add(incomp);
                eiaIncompliances.add(incomp);
                continue;
            }
            
            //3. check segment valid
            for (int j = 0; j < psdlen; j++) {   
                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
                    setEdfValid(false);
                    description = descriptions[j];
                    incomp = new Incompliance(incomplianceType, description, 
                                              fileName, rowIndex, columnIndex, srcTypeIndex);
                    //MainWindow.getAggregateIncompliances().add(incomp);
                    eiaIncompliances.add(incomp);
                }
            }
        } 
        return eiaIncompliances;
    }
    
    public ArrayList<Incompliance> parseEIATemplateTable(){
        ArrayList<Incompliance> eiaTemplateIncomps = new ArrayList<Incompliance>();
        final int srcTypeIndex = Incompliance.index_incomp_src_eiatemplate;        
        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
        Incompliance incomp;
        String description;
        String fileName = this.getMasterFile().getPath();
        int nrow = 1;
        
        int rowIndex, columnIndex = 2;
        int segnumber = 3;
        int seg[] = new int[segnumber];
        String[] descriptions = {Incompliance.warning_eia_day, 
                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
        
       // boolean cellHiding = true;
        
        String sd;
        for (int i = 0; i < nrow; i++) {
            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
            fileName = this.getMasterFile().getAbsolutePath();
            rowIndex = i;
            String[] psd = sd.split("\\.");
            
            //1. check format of dd.mm.yy
            int psdlen = psd.length;
            if (psdlen != segnumber){
                setEdfValid(false);
                description = Incompliance.error_eia_format;
                incomp = new Incompliance( incomplianceType, description, 
                                           fileName, rowIndex, columnIndex, srcTypeIndex);
                eiaTemplateIncomps.add(incomp);
                continue;
            } 
            
            //2. check each segment of startdate is number or not
            try{
                for (int j = 0; j < segnumber; j++)
                    seg[j] = Integer.parseInt(psd[j]);
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_eia_dmyint;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, srcTypeIndex);
                  eiaTemplateIncomps.add(incomp);
                continue;
            }
            
            //3. check segment valid
            for (int j = 0; j < psdlen; j++) {   
                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
                    setEdfValid(false);
                    description = descriptions[j];
                    incomp = new Incompliance(incomplianceType, description, 
                                              fileName, rowIndex, columnIndex, srcTypeIndex);
                     eiaTemplateIncomps.add(incomp);
                }
            }
        }        
        
        return eiaTemplateIncomps;
    }
    
    //stub method
    //further implementation in EIATable and ESATable
    public void showImmutableFields(){
       ;
    }
    
    //stub method
    //further implementation in EIATable and ESATable
    public void hideImmutableFields(){
       ;
    }
    
    //Implement table cell tool tips.

    public String getToolTipText(MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        
        if (realColumnIndex == 1)
            tip = altName + "key + Mouse right-click to customize transducer type";
        else
            tip = super.getToolTipText(e);
        
        return tip;
    }
   
    class StripedTableCellRenderer implements TableCellRenderer {
        
        protected TableCellRenderer targetRenderer;
        protected Color evenBack;
        protected Color evenFore;
        protected Color oddBack;
        protected Color oddFore;
        
      public StripedTableCellRenderer(TableCellRenderer targetRenderer,
          Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
        this.targetRenderer = targetRenderer;
        this.evenBack = evenBack;
        this.evenFore = evenFore;
        this.oddBack = oddBack;
        this.oddFore = oddFore;
      }

      // Implementation of TableCellRenderer interface
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer renderer = targetRenderer;
        if (renderer == null) {
          renderer = table.getDefaultRenderer(table.getColumnClass(column));
        }

        Component comp = renderer.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);

        if (isSelected == false && hasFocus == false) {
          if ((row & 1) == 0) {
            comp.setBackground(evenBack != null ? evenBack : table
                .getBackground());
            comp.setForeground(evenFore != null ? evenFore : table
                .getForeground());
          } else {
            comp.setBackground(oddBack != null ? oddBack : table
                .getBackground());
            comp.setForeground(oddFore != null ? oddFore : table
                .getForeground());
          }
        }
        
        return comp;
      }
    }
    
    // Convenience method to apply this renderer to single column
    public void stripTableInColumn(int columnIndex, Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
      TableColumn tc = this.getColumnModel().getColumn(columnIndex);
      TableCellRenderer targetRenderer = tc.getCellRenderer();
      
      tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
          evenBack, evenFore, oddBack, oddFore));   
    }
    
    public void stripTable(Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
      StripedTableCellRenderer sharedInstance = null;
      int columns = this.getColumnCount();
      for (int i = 0; i < columns; i++) {
        TableColumn tc = this.getColumnModel().getColumn(i);
        TableCellRenderer targetRenderer = tc.getCellRenderer();
        if (targetRenderer != null) {
          tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
              evenBack, evenFore, oddBack, oddFore));
        } else {
          if (sharedInstance == null) {
            sharedInstance = new StripedTableCellRenderer(null,
                evenBack, evenFore, oddBack, oddFore);
          }
          tc.setCellRenderer(sharedInstance);
        }
      }
    }
    
    class EDFUndoableEditListener implements UndoableEditListener{
        public EDFUndoableEditListener(){
            super();
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            MainWindow.getUndoManager().addEdit(e.getEdit());
           if (MainWindow.getUndoManager().canUndo()) {
                MainWindow.getEditUndoItem().setEnabled(true);
                MainWindow.getUndoButton().setEnabled(true);
            } else {
                MainWindow.getEditUndoItem().setEnabled(false);
                MainWindow.getUndoButton().setEnabled(false);
            }

            if (MainWindow.getUndoManager().canRedo()) {
                MainWindow.getEditRedoItem().setEnabled(true);
                MainWindow.getRedoButton().setEnabled(true);
            } else {
                MainWindow.getEditRedoItem().setEnabled(false);
                MainWindow.getRedoButton().setEnabled(false);
            }
        }
    }
    

    private class EDFTableHeaderRenderer extends JLabel implements TableCellRenderer{

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, // column header value 
                                                       boolean isSelected, // always set to false
                                                       boolean hasFocus, // always set to false
                                                       int row /* row is always -1 */, int column) {
            
            setText(value.toString());
            this.setOpaque(false);
            this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            this.setForeground(tableHeaderForegroundClr);
            this.setBackground(tableHeaderBackgroundClr);
            
            return this;
        }
        
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String ptyName, Object oldValue, Object newValue){}
        public void firePropertyChange(String ptyName, boolean oldValue, boolean newValue){}
    }
}
