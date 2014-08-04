package table;

import header.EIA;
import header.EIAHeader;

/**
 * EIATableModel has facility methods that can test the status of cells
 */
@SuppressWarnings("serial")
public class EIATableModel extends EDFTableModel {

    public EIATableModel() {
        super();
    }

    /**
     * Constructs an EIATableModel by providing a row number
     * @param nrows the total number of rows to be displayed
     */
    public EIATableModel(int nrows) {
        super(EIA.getEIAAttributes(), nrows);
    }

    /**
     * An EIAHeader Image of the current TableModel
     * @return an EIA header of the current EIATableModel
     */
    public EIAHeader toEIAHeader() {
        // TODO: transform table data to EIA header
        return new EIAHeader();
    }

    /**
     * Test whether a cell in the table is editable
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
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
     * Get the class of each column
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
