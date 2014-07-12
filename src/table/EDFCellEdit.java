package table;

import javax.swing.undo.AbstractUndoableEdit;

@SuppressWarnings("serial")
public class EDFCellEdit extends AbstractUndoableEdit {
    
	protected EDFTableModel tableModel;
    protected Object oldValue;
    protected Object newValue;
    protected int row;
    protected int column;    
    
    /**
     * Construct a new EDFCellEdit with specified cell, new and old values and a EDFTableModel
     * @param tableModel the table model to be used
     * @param oldValue old value of this edit
     * @param newValue new value of this edit
     * @param row the row of the table
     * @param column the column of the table
     */
    public EDFCellEdit(EDFTableModel tableModel, Object oldValue, Object newValue, int row, int column) {
        super();
        this.tableModel = tableModel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.row = row;
        this.column = column;
    }
    
    /**
     * Used by getUndoPresentationName and getRedoPresentationName to construct the strings they return
     * @see javax.swing.undo.AbstractUndoableEdit#getPresentationName()
     * @return this default implementation returns "".
     */
    @Override
    public String getPresentationName() {
        return "";
    }
    
    /**
     * Undo an edit to old value 
     * @see javax.swing.undo.AbstractUndoableEdit#undo()
     */
    @Override
    public void undo() {
        super.undo();
        tableModel.setValueAt(oldValue, row, column, false);
    }
    
    /**
     * Redoes an undo edit
     * @see javax.swing.undo.AbstractUndoableEdit#redo()
     */
    @Override
    public void redo() {
        super.redo();
        tableModel.setValueAt(newValue, row, column, false); 
    }
    
}
