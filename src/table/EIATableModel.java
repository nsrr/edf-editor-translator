package table;

import header.EIA;
import header.EIAHeader;

import javax.swing.table.DefaultTableModel;


public class EIATableModel extends EDFTableModel {

    public EIATableModel() {
        super();
    }

    /**
     * @param rows the total number of rows to be displayed
     * primary constructor for EIATableModel
     */
    public EIATableModel(int rows) {
        super(EIA.getEIAAttributes(), rows);
    }

    /**
     * @return
     * an EIAHeader Image of the current TableModel
     */
    public EIAHeader toEIAHeader() {
        //TODO: transform table data to EIA header
        return new EIAHeader();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
        case 1:
            return false;
        case 2:
        case 3:
        case 4:
        //case 5:
            return true;
        default:
            return false;
        }
    }

    /**
     * Usage: set the class of each column
     * @param col the column index
     * @return the class of the indexed column
     */
    public Class<?> getColumnClass(int col) {
        switch (col) {
        case 0:
            return String.class;
        case 1:
            return Integer.class;
        case 2:
        case 3:
        case 4:
        case 5:
            return String.class;
        case 6:
        case 7:
        case 8:
        case 9:
        case 10:
            return Integer.class;
        }

        throw new AssertionError("invalid column");
    }

}
