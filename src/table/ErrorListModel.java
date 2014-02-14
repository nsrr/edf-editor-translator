package table;

import javax.swing.table.DefaultTableModel;

public class ErrorListModel extends DefaultTableModel {
    public ErrorListModel() {
        super(ErrorListTable.columnNames, ErrorListTable.maxViewableRows);
    }

    public ErrorListModel(int nrows) {
        super(ErrorListTable.columnNames, nrows);
    }

    public ErrorListModel(Object[] columnNames, int nrow) {
        super(columnNames, nrow);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
