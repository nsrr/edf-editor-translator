package table;

import header.ESA;
import header.ESAChannel;
import header.ESAHeader;
import header.ESATemplateChannel;

import java.awt.Font;

import javax.swing.table.TableColumn;

/**
 * An ESATemplateTable used to create and costomize the ESATemplateTable
 */
@SuppressWarnings("serial")
public class ESATemplateTable extends EDFTable {
    
	static final int DIGITAL_MAXIMUM_INDEX = 6;
    static final int DIGITAL_MINIMUM_INDEX = 7;
    static final String digital_error_msg = "Digtial Maximum should be larger than Digital Minimum.";    
    
    private TableColumn[] allTableColumns;
    private final static int index_of_immutablie = 9;
    protected static final int[] immutableFieldIndices = {index_of_immutablie};
    
    /**
     * Default constructor
     */
    public ESATemplateTable() {
        super(new ESATemplateTableModel(1));
        customizeLook();
        this.setUpdateSinceLastSave(true);
    }

    /**
     * Creates ESATemplateTable using an ESAHeader
     * @param esaHeader the ESAHeader
     * @param forTemplateOpen if true, creates the ESATemplateTable, false creates ESATable
     */
    public ESATemplateTable(ESAHeader esaHeader, Boolean forTemplateOpen) {
        super(new ESATemplateTableModel(1));
        createESATable(esaHeader, forTemplateOpen);
        customizeLook();
        this.setUpdateSinceLastSave(true);
    }

    /**
     * Customizes the look of this table
     */
    public void customizeLook() {
    	this.stripTable(tableOddRowClr, null, tableEvenRowClr, null);
	    this.getTableHeader().setFont( new Font( "Dialog" , Font.PLAIN, 16));
	    // this.getTableHeader().setForeground(Color.black);
                
	    this.setRowHeight((int) (this.getRowHeight() * 2.0));
	    this.setCellSelectionEnabled(true);
	    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
	    this.stripTableInColumn(9, stripBackgroundClr, stripForegroundClr, stripBackgroundClr, stripForegroundClr);
    }  

    /**
     * Creates ESA table using an ESAHeader
     * @param esaHeader ESAHeader used to construct the ESATemplateTable
     * @param forTemplateOpen true if created as template
     */
    public void createESATable(ESAHeader esaHeader, boolean forTemplateOpen) {
        int nChannels = esaHeader.getNumberOfChannels();
        this.setModel(new ESATemplateTableModel(nChannels)); // this line is required; otherewise, getValueAt does not work
        
        if (forTemplateOpen) {        
            ESATemplateChannel esaChannel;
            String key;
            String aValue;
            for (int nrow = 0; nrow < nChannels; nrow++)
                for (int ncolumn = 0; ncolumn < ESA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++) {
                    esaChannel = esaHeader.getEsaTemplateChannelAt(nrow);
                    key = ESA.getESATemplateAttributes()[ncolumn];
                    aValue = (String)esaChannel.getSignalAttributeValueAt(key);
                    this.setValueAt(aValue, nrow, ncolumn); //map attribute values to cells
                }
        // this.stripTable(Color.lightGray, Color.white, null, null);
        } else {
            ESAChannel esaChannel;
            String key;
            String aValue;
            for (int nrow = 0; nrow < nChannels; nrow++) {
                for (int ncolumn = 0; ncolumn < ESA.NUMBER_OF_ATTRIBUTES; ncolumn++) {
                    esaChannel = esaHeader.getEsaChannelAt(nrow);
                    key = ESA.getESATemplateAttributes()[ncolumn];
                    aValue = (String)esaChannel.getSignalAttributeValueAt(key);
                    this.setValueAt(aValue, nrow, ncolumn); //map attribute values to cells
                }
            }
        //this.stripTable( stripBackgroundClr, stripForegroundClr,  stripBackgroundClr, stripForegroundClr);
        }
    }
    
    /**
     * Stores current columns into local fields
     */
    private void cacheColumns() {
        int ncol = getModel().getColumnCount();
         allTableColumns = new TableColumn[ncol];
         for (int i = 0; i < ncol; i++)
             allTableColumns[i] =  getColumnModel().getColumn(i);
     }
    
    /**
     * Shows immutable table fields of this ESA template table
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
     * Hides immutable fields of this ESA template table
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
}
