package table;

import header.EDFFileHeader;
import header.ESA;
import header.ESAChannel;
import header.ESAHeader;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ESATable extends EDFTable {
	
    private File hostFile; // keep the location of the source File for this header.
    private TableColumn[] allTableColumns;
    private final static int index_of_immutablie = 8;
    protected static final int[] immutableFieldIndices = { index_of_immutablie };

    /**
     * TODO
     * this constructor builds a table with 1 empty row
     */
    public ESATable() {
        super(new ESATableModel(1));  
        customizeLook("esa");
        this.setUpdateSinceLastSave(true);
    }
    
    /**
     * TODO
     * @param esa
     */
    public ESATable(ESATableModel esa) {
    	super(esa);
        customizeLook("esa");
    	this.setUpdateSinceLastSave(true);
    }
    
    /**
     * TODO
     * @param esaHeader
     * @param forTemplateOpen
     */
    public ESATable(ESAHeader esaHeader, Boolean forTemplateOpen) {
        super();
        
        if (forTemplateOpen == true) {
            createESATable(esaHeader);
            customizeLook("esa");
        }
        this.setUpdateSinceLastSave(true);
    }


    /**
     * TODO
     * @param edfHeader a group of edf files' headers
     */
    public ESATable(EDFFileHeader edfHeader, Boolean forTemplateOpen) {
        super();

        ESAHeader esaHeader = edfHeader.getEsaHeader();
        if (forTemplateOpen == true) {
            createESATable(esaHeader);
            customizeLook("esa");
        }
     }
    
//    @Override
//    public String getTooltTipText(MouseEvent e){
//        String tip = "Press Alt + Mouse right click for transducer customerization";
//        Point p = e.getPoint();
//
//        Locate the renderer under the event location
//        int hitColumnIndex = columnAtPoint(p);
//        int hitRowIndex = rowAtPoint(p);
//        
//        if (hitColumnIndex ==  1)
//            return tip;
//        
//        return null;
//    }
    
    /**
     * TODO
     * @param edfFileHeader
     */
    public void updateTable(ESAHeader edfFileHeader) {

    	TableModel model = this.getModel();
        int nrows = model.getRowCount();
        int ncols = model.getColumnCount();
        
        String value;
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                value = edfFileHeader.getValueAt(i, j);
                model.setValueAt(value, i, j);
            }
        }
        this.setUpdateSinceLastSave(true); // update the save status of the table
    }
    
    //Fangping, 10/04/2010
      /**
       * TODO
       */
      private void cacheColumns() {
    	  int ncol = getModel().getColumnCount();
    	  allTableColumns = new TableColumn[ncol];
    	  for (int i = 0; i < ncol; i++)
    		  allTableColumns[i] =  getColumnModel().getColumn(i);
      }
    
    //Fangping, 10/04/2010
    /**
     * TODO
     * @see table.EDFTable#showImmutableFields()
     */
    public void showImmutableFields() {
        for (int index: immutableFieldIndices) {
            addColumn(allTableColumns[index]);
            getColumnModel().moveColumn(getColumnCount() - 1, index);
        }
        validate();
    }
    
    /**
     * TODO
     * @see table.EDFTable#hideImmutableFields()
     */
    public void hideImmutableFields() {
        cacheColumns();
        int k = 0;
        for (int i : immutableFieldIndices) {
            TableColumn column = getColumnModel().getColumn(i - k);
            getColumnModel().removeColumn(column);
            k++;
        }
        validate();
    }
    
    /**
     * TODO
     * @param esaHeader ESA header
     * one header corresponds to one ESA Table
     * algorithm: map each attribute value of each channel to each cell
     */
    public void createESATable(ESAHeader esaHeader) {
        int nChannels = esaHeader.getNumberOfChannels();
        this.setModel(new  ESATableModel(nChannels)); //this line is required; otherwise, getValueAt does not work
        ESAChannel esaChannel;
        String key, aValue;
        for (int nrow = 0; nrow < nChannels; nrow++)
            for (int ncolumn = 0; ncolumn < ESA.NUMBER_OF_ATTRIBUTES; ncolumn++) {
                esaChannel = esaHeader.getEsaChannelAt(nrow);
                key = ESA.getESAAttributes()[ncolumn];
                aValue = (String) esaChannel.getSignalAttributeValueAt(key);
                //System.out.println(aValue);
                this.setValueAt(aValue, nrow, ncolumn); //map attribute values to cells
            }

        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null);
    }
    
    /**
     * TODO
     * @param sourceOfMasterFile
     */
    public void setSourceMasterFile(File sourceOfMasterFile) {
        this.hostFile = sourceOfMasterFile;
    }

    /**
     * TODO
     * @return
     */
    public File getSourceMasterFile() {
        return hostFile;
    }

    /**
     * TODO
     */
    class StripedTableCellRenderer extends JLabel implements TableCellRenderer {
        
		protected TableCellRenderer targetRenderer;
        protected Color evenBack;
        protected Color evenFore;
        protected Color oddBack;
        protected Color oddFore;

        /**
         * TODO
         * @param targetRenderer
         * @param evenBack
         * @param evenFore
         * @param oddBack
         * @param oddFore
         */
        public StripedTableCellRenderer(TableCellRenderer targetRenderer,
                                        Color evenBack, Color evenFore,
                                        Color oddBack, Color oddFore) {
            this.targetRenderer = targetRenderer;
            this.evenBack = evenBack;
            this.evenFore = evenFore;
            this.oddBack = oddBack;
            this.oddFore = oddFore;
        }

        // Implementation of TableCellRenderer interface
        /**
         * TODO
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            TableCellRenderer renderer = targetRenderer;
            if (renderer == null) {
                renderer = table.getDefaultRenderer(table.getColumnClass(column));
            }

            Component comp = renderer.getTableCellRendererComponent(table, value,
                                                       isSelected, hasFocus,
                                                       row, column);

            if (isSelected == false && hasFocus == false) {
                if ((row & 1) == 0) {
                    comp.setBackground(evenBack != null ? evenBack :
                                       table.getBackground());
                    comp.setForeground(evenFore != null ? evenFore :
                                       table.getForeground());
                } else {
                    comp.setBackground(oddBack != null ? oddBack :
                                       table.getBackground());
                    comp.setForeground(oddFore != null ? oddFore :
                                       table.getForeground());
                }
            }
            return comp;
        }
    }

    // Convenient method to apply this renderer to single column
    /**
     * TODO
     * @see table.EDFTable#stripTableInColumn(int, java.awt.Color, java.awt.Color, java.awt.Color, java.awt.Color)
     */
    public void stripTableInColumn(int columnIndex, Color evenBack,
                                   Color evenFore, Color oddBack,
                                   Color oddFore) {
        TableColumn tc = this.getColumnModel().getColumn(columnIndex);
        TableCellRenderer targetRenderer = tc.getCellRenderer();

        tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
                                                        evenBack, evenFore,
                                                        oddBack, oddFore));
    }

    /**
     * TODO
     * @see table.EDFTable#stripTable(java.awt.Color, java.awt.Color, java.awt.Color, java.awt.Color)
     */
    public void stripTable(Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
        StripedTableCellRenderer sharedInstance = null;
        int columns = this.getColumnCount();
        for (int i = 0; i < columns; i++) {
            TableColumn tc = this.getColumnModel().getColumn(i);
            TableCellRenderer targetRenderer = tc.getCellRenderer();
            if (targetRenderer != null) {
                tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
                                                                evenBack,
                                                                evenFore,
                                                                oddBack,
                                                                oddFore));
            } else {
                if (sharedInstance == null) {
                    sharedInstance =
                            new StripedTableCellRenderer(null, evenBack,
                                                         evenFore, oddBack,
                                                         oddFore);
                }
                tc.setCellRenderer(sharedInstance);
            }
        }
    }

}
