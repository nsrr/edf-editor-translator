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

import validator.fix.ValidateEDF;

@SuppressWarnings("serial")
public class EDFTable extends JTable {
	
	protected Boolean savedOnce = false; // usage: if the body should be copied in current save
	// TODO: what is updateSinceLastSave: updated or not since last save
    protected Boolean updateSinceLastSave = false; // false when ESA/EIA table is created; true when template file is created
    protected File masterFile = null; // usage: keep the master file of the table
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
    
    static final String physical_maximum_nonnumber_error = "Physical Maximum field should contain scalar value. ";
    static final String physical_minimum_nonnumber_error = "Physical Minimum field should contain scalar value. ";
    static final String digital_order_error = "Digtial Maximum should be larger than Digital Minimum.";
    static final String digital_field_blank_error = "Digital field should contain integer value.";
    
    static final String start_date_error = "Start date needs to be in the form of xx.xx.xx where x are integers";
    static final String altName = (Main.mac_os)? "Command": "Alt";
    
    // to store the indices of immutable cells
    // protected int immutableFieldIndices[];

    /**
     * set the index of the master file in the master file array
     * @param masterFileIndex the master file index
     */
    public void setMasterFileIndex(int masterFileIndex) {
        this.masterFileIndex = masterFileIndex;
    }

    /**
     * get the index of the master file in the master file array
     * @return the master file index in the master file array
     */
    public int getMasterFileIndex() {
        return masterFileIndex;
    }

    /**
     * Set the validity of EDF file fields according to the EDF specification
     * @param valid true if all fields conform to EDF specification
     */
    public void setEdfValid(boolean valid) {
        this.edfValid = valid;
    }

    /**
     * Return the validity of EDF file fields of this table
     * @return true if the EDF file fields are valid
     */
    public boolean isEdfValid() {
        return edfValid;
    }

    //  wei wang commented 2014-6-28    
    /**
     * Master header category class consists of category fields
     */
    public static class MasterHeaderCategory {
        public static final int EIA_WORKSET = 0;
        public static final int ESA_WORKSET = 1;
        public static final int EIA_TEMPLATE = 2;
        public static final int ESA_TEMPLATE = 3;
    }

    /**
     * Default EDFTable constructor
     */
    public EDFTable() {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        renderHeader();
    }

    /**
     * Construct EDFTable using a TableModel
     * @param tableModel the TableModel used
     */
    public EDFTable(TableModel tableModel) {
        super(tableModel);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        this.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * Create EDFTable using isEIATemplate indicator
     * @param isEIATemplate if true, construct EDFTable using EIATableModel,
     * 		 				false, construct EDFTable using ESATemplateTableModel
     */
    public EDFTable(Boolean isEIATemplate) {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        if (isEIATemplate == true)
            new JTable(new EIATableModel(1));
        else
            new JTable(new ESATemplateTableModel(1));
    }

    /**
     * Set the customized look from different data type
     * @param dataType the data type that responsible for different look of this table
     */
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
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() * 0.7));
            }
            
            for (int i = 2; i < 5; i++) {
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() * 1.5));
            }

            for (int i = 5; i < 11; i++) {
                this.stripTableInColumn(i, stripBackgroundClr,
                                        stripForegroundClr, stripBackgroundClr,
                                        stripForegroundClr);
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() * 0.7));
            }
        } else if (dataType.equals("esa"))
            this.stripTableInColumn(8, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
        else if (dataType.equals("esa_template"))
            this.stripTableInColumn(9, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
    }

    /**
     * Set the header renderer using EDFTableHeaderRenderer
     */
    private void renderHeader() {
        int ncol = this.getColumnCount();
        for (int i = 0; i < ncol; i++) {
            TableColumn tc = this.getColumnModel().getColumn(i);
            tc.setHeaderRenderer(new EDFTableHeaderRenderer());
        }
    }

    /**
     * Scroll the specified cell to be visible
     * this function has been reformed in Utility. They have the same functionality
     * Fangping, 09/28/2010
     * @param rr row of the cell
     * @param cc column of the cell
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
    
    /** 
     * Returns <code>false</code> to indicate that the height of the viewport does
     * not determine the height of the table
     * @return false if the height of the viewport does not determine the height of
     * the table
     * @see javax.swing.JTable#getScrollableTracksViewportHeight()
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Set the savedOnce status
     * @param savedEver true if this EDFTable has been saved once
     */
    public void setSavedOnce(Boolean savedEver) {
        this.savedOnce = savedEver;
    }
    
    /**
     * Test if some of the table columns has been hidden
     * @return true if some columns has been hidden
     */
    public boolean isTableColumnHidden() {
        return (getModel().getColumnCount() != getColumnModel().getColumnCount());
    }

    /**
     * Returns the status of savedOnce
     * @return true if this table has been saved once
     */
    public Boolean getSavedOnce() {
        return savedOnce;
    }

    /**
     * Set updateSinceLastSave status
     * @param updateSinceLastSave true if updated since last save
     */
    public void setUpdateSinceLastSave(Boolean updateSinceLastSave) {
        this.updateSinceLastSave = updateSinceLastSave;
    }

    /**
     * Get updateSinceLastSave status
     * @return true if template file is created, false if ESA/EIA table is created
     */
    public Boolean getUpdateSinceLastSave() {
        return updateSinceLastSave;
    }

    /**
     * Set the master file of this table
     * @param masterFile the host file
     */
    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    /**
     * Returns the master file of this table
     * @return the master file
     */
    public File getMasterFile() {
//         if (this.masterHeaderCategory == MasterHeaderCategory.EIA_WORKSET)
//            return null;
        return masterFile;
    }

    /**
     * obsolete method
     * @deprecated
     * @param masterHeaderCategory the category of this master file header
     */
    public void setMasterHeaderCategory(int masterHeaderCategory) {
        this.masterHeaderCategory = masterHeaderCategory;
    }

    /**
     * obsolete method
     * Get the masterHeaderCategory
     * @deprecated
     * @return masterHeaderCategory in integer
     */
    public int getMasterHeaderCategory() {
        return masterHeaderCategory;
    }

    /**
     * Updates all the four attributes
     * @param saved saved once or not
     * @param updated updated or not since last save
     * @param masterFile master file of the table
     * @param categoryValue category of the table
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
    
    /**
     * TODO
     * used for both ESA and ESA template tables
     * Fangping, 09/29/2010
     * Validation improvement for ESA tables
     * Gang Shu, 02/20/2014
     */
	public ArrayList<Incompliance> parseESATable() {
		
		String wrkFileName = this.getMasterFile().getAbsolutePath();
		String srcFileName = "";
		for (int i = 0; i < MainWindow.getWkEdfFiles().size(); i++){
			File f = MainWindow.getWkEdfFiles().get(i);
			if (f.getAbsolutePath().equals(wrkFileName)){
				srcFileName = MainWindow.getSrcEdfFiles().get(i).getAbsolutePath();
				break;
			}
		}
		
		return ValidateEDF.parseESATable(this, srcFileName);
	}

	/**
	 * TODO
	 * @return
	 */
    public ArrayList<Incompliance> parseEIATable() {
    	
    	int nrow = this.getRowCount();
    	String[] fileList = new String[nrow];
        for (int i = 0; i < nrow; i++) {
        	String fileName = MainWindow.getSrcEdfFiles().get(i).getAbsolutePath();
        	fileList[i] = fileName;
        }
        
        return ValidateEDF.parseEIATable((EIATable)this, fileList);
    }
    
    /**
     * TODO
     * @return
     */
    public ArrayList<Incompliance> parseEIATemplateTable() {    	
    	return ValidateEDF.parseEIATemplateTable(this);
    }
    
    protected final int[] lowerbounds = {0, 0, 0};
    protected final int[] upperbounds = {31, 12, 99}; // 31 day, 12 month, 2099 years
     
//    public ArrayList<Incompliance> parseEIATemplateTable(){
//        ArrayList<Incompliance> eiaTemplateIncomps = new ArrayList<Incompliance>();
//        final int srcTypeIndex = Incompliance.index_incomp_src_eiatemplate;        
//        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
//        Incompliance incomp;
//        String description;
//        String fileName = this.getMasterFile().getPath();
//        int nrow = 1;
//        
//        int rowIndex, columnIndex = 2;
//        int segnumber = 3;
//        int seg[] = new int[segnumber];
//        String[] descriptions = {Incompliance.warning_eia_day, 
//                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
//        
//       // boolean cellHiding = true;
//        
//        String sd;
//        for (int i = 0; i < nrow; i++) {
//            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
//            fileName = this.getMasterFile().getAbsolutePath();
//            rowIndex = i;
//            String[] psd = sd.split("\\.");
//            
//            //1. check format of dd.mm.yy
//            int psdlen = psd.length;
//            if (psdlen != segnumber){
//                setEdfValid(false);
//                description = Incompliance.error_eia_dateformat;
//                incomp = new Incompliance( incomplianceType, description, 
//                                           fileName, rowIndex, columnIndex, srcTypeIndex);
//                eiaTemplateIncomps.add(incomp);
//                continue;
//            } 
//            
//            //2. check each segment of startdate is number or not
//            try{
//                for (int j = 0; j < segnumber; j++)
//                    seg[j] = Integer.parseInt(psd[j]);
//            }
//            catch(NumberFormatException e){
//                setEdfValid(false);
//                description = Incompliance.error_eia_dmyint;
//                incomp = new Incompliance(incomplianceType, description, 
//                                          fileName, rowIndex, columnIndex, srcTypeIndex);
//                  eiaTemplateIncomps.add(incomp);
//                continue;
//            }
//            
//            //3. check segment valid
//            for (int j = 0; j < psdlen; j++) {   
//                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
//                    setEdfValid(false);
//                    description = descriptions[j];
//                    incomp = new Incompliance(incomplianceType, description, 
//                                              fileName, rowIndex, columnIndex, srcTypeIndex);
//                     eiaTemplateIncomps.add(incomp);
//                }
//            }
//        }        
//        
//        return eiaTemplateIncomps;
//    }

    /**
     * Stub method
     * Further implementation in EIATable and ESATable
     */
    public void showImmutableFields() {;}
    
    /**
     * Stub method
     * Further implementation in EIATable and ESATable
     */
    public void hideImmutableFields() {;}
    
    /**
     * Implementation of table cell tool tips
     * @return a string of table cell tool tip
     */
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
   
    /**
     * A customized TableCellRenderer
     */
    class StripedTableCellRenderer implements TableCellRenderer {
        
        protected TableCellRenderer targetRenderer;
        protected Color evenBack;
        protected Color evenFore;
        protected Color oddBack;
        protected Color oddFore;
        
        /**
         * Construct a StripedTableCellRenderer using a TableCellRenderer and some attributes
         * @param targetRenderer the TableCellRenderer to be used
         * @param evenBack even background color
         * @param evenFore even foreground color
         * @param oddBack odd background color
         * @param oddFore odd foreground color
         */
        public StripedTableCellRenderer(TableCellRenderer targetRenderer,
        		Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
        	this.targetRenderer = targetRenderer;
        	this.evenBack = evenBack;
        	this.evenFore = evenFore;
        	this.oddBack = oddBack;
        	this.oddFore = oddFore;
        }

        /**
         * Implementation of TableCellRenderer interface
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
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
    
    /**
     * Convenience method to apply this renderer to single column
     * @param columnIndex the column index
     * @param evenBack even background color
     * @param evenFore even foreground color
     * @param oddBack odd background color
     * @param oddFore odd foreground color
     */
    public void stripTableInColumn(int columnIndex, Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
      TableColumn tc = this.getColumnModel().getColumn(columnIndex);
      TableCellRenderer targetRenderer = tc.getCellRenderer();
      
      tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
          evenBack, evenFore, oddBack, oddFore));   
    }
    
    /**
     * Render table using different color theme
     * @param evenBack even background color
     * @param evenFore even foreground color
     * @param oddBack odd background color
     * @param oddFore odd foreground color
     */
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
    
    /**
     * An EDF UndoableEditListener implementation listening to undoable event
     */
    class EDFUndoableEditListener implements UndoableEditListener {
        public EDFUndoableEditListener() {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
         */
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

    /**
     * A EDF table header renderer 
     */
    private class EDFTableHeaderRenderer extends JLabel implements TableCellRenderer {

        /* (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
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
