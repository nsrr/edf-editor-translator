package table;

import javax.swing.undo.AbstractUndoableEdit;

public class EDFCellEdit extends AbstractUndoableEdit{
    
    protected EDFTableModel tableModel;
    protected Object oldValue;
    protected Object newValue;
    protected int row;
    protected int column;    
    
    public EDFCellEdit(EDFTableModel tableModel, Object oldValue, Object newValue, int row, int column) {
        super();
        this.tableModel = tableModel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.row = row;
        this.column = column;
    }
    
    @Override
    public String getPresentationName(){
        return "";
    }
    
    
    @Override
    public void undo(){
        super.undo();
        tableModel.setValueAt(oldValue, row, column, false);
    }
    
    
    @Override
    public void redo(){
        super.redo();
        tableModel.setValueAt(newValue, row, column, false); 
    }
    
}
