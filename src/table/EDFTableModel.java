package table;

import editor.MainWindow;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;

public class EDFTableModel extends DefaultTableModel {
    public EDFTableModel() {
        super();
        this.addUndoableEditListener(MainWindow.getUndoManager());
    }
    
    public EDFTableModel(Object[] columnNames, int nrow){
        super(columnNames, nrow);
        this.addUndoableEditListener(MainWindow.getUndoManager());   
    }

    public EDFTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        this.addUndoableEditListener(MainWindow.getUndoManager());
    }

    @Override
    public void setValueAt(Object value, int row, int column){
        setValueAt(value, row, column, true);
    } 
    
    public void setValueAt(Object value, int row, int column, boolean undoable)
        {
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

        public void addUndoableEditListener(UndoableEditListener listener){
            listenerList.add(UndoableEditListener.class, listener);
        }
}
