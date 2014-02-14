package table;

import header.ESA;
import header.ESAChannel;
import header.ESAHeader;

import javax.swing.table.DefaultTableModel;

public class ESATableModel extends EDFTableModel{
    public ESATableModel() {
        super();
    }
    
    public ESATableModel(int rows){
        super(ESA.getESAAttributes(), rows);
    }
    
    public ESATableModel(Object[] columnNames, int nrow)
    {
    	super(columnNames, nrow);
    }

    /**
     * @return
     * get the ESAHeader image of the table model
     */
    public ESAHeader toESAHeader(){
        //TODO: transform table data to ESA header
        return new ESAHeader();
    }

/*     /**
     * @param rowIndex the index of the row
     * @return the ESA channel generated from the row
     * usage: tableModel.toESAChannel(1)
     */
    public ESAChannel toESAChannel(int rowIndex){
        return new ESAChannel();
    }


    /**
     * Usage: control edibility of cell.
     * @param rowIndex
     * @param columnIndex
     * @return
     * TODO:
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex){
        case 8: 
            return false;
        default:            
            return true;
        }
    }

    /**
     * Usage: set the class of each column
     * @param col
     * @return
     * TODO:
     */
/*     public Class<?> getColumnClass(int col) {
        switch (col) {
        case 0:
        case 1:            
        case 2:
            return String.class;
        case 3:
        case 4:
        case 5:
        case 6:           
        case 7:
        case 8:
        case 9:
            return Integer.class;            
        }

        throw new AssertionError("invalid column");
    } */

    
}
