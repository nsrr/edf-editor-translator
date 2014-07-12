package table;

import editor.MainWindow;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class EDFTableModel extends DefaultTableModel {
	
	/**
	 * Construct this EDFTableModel with a undo manager
	 */
    public EDFTableModel() {
        super();
        this.addUndoableEditListener(MainWindow.getUndoManager());
    }
    
    /**
     * Construct a EDFTableModel with as many columns as there are
     * elements in <code>columnNames</code> and <code>rowCount</code> 
     * of <code>null</code> object values.  Each column's name will 
     * be taken from the <code>columnNames</code> array.
     * @param columnNames <code>array</code> containing the names of
     *                    the new columns; if this is <code>null</code>
     *                    then the model has no columns
     * @param nrow the number of rows the table holds
     */
    public EDFTableModel(Object[] columnNames, int nrow) {
        super(columnNames, nrow);
        this.addUndoableEditListener(MainWindow.getUndoManager());   
    }

    /**
     * Constructs a <code>EDFTableModel</code> and initializes the table
     * by passing <code>data</code> and <code>columnNames</code>
     * to the <code>setDataVector</code>
     * method. The first index in the <code>Object[][]</code> array is
     * the row index and the second is the column index.
     * @param data           the data of the table
     * @param columnNames    the names of the columns
     */
    public EDFTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        this.addUndoableEditListener(MainWindow.getUndoManager());
    }

    /**
     * Sets the object value for the cell at column and row. aValue is the new value
     * @value the new value 
     * @row the row whose value is to be changed
     * @column the column whose value is to be changed
     * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
        setValueAt(value, row, column, true);
    } 
    
    /**
     * Specify the value to be set and undoable support
     * @param value the new value 
     * @param row the row whose value is to be changed
     * @param column the column whose value is to be changed
     * @param undoable true if this value support undoable event
     */
    public void setValueAt(Object value, int row, int column, boolean undoable) {
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null){
            super.setValueAt(value, row, column);
            return;
        }

        Object oldValue = getValueAt(row, column);
        super.setValueAt(value, row, column);
        EDFCellEdit cellEdit = new EDFCellEdit(this, oldValue, value, row, column);
        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
        for (UndoableEditListener listener : listeners)
            listener.undoableEditHappened(editEvent);
    }

    /**
     * Add a UndoableEditListener to this model
     * @param listener the UndoableEditListener
     */
    public void addUndoableEditListener(UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }
}
