package editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class EDFUndoManager extends UndoManager {
    protected Action undoAction;
    protected Action redoAction;

    public EDFUndoManager() {
        this.undoAction = new EDFUndoAction(this);
        undoAction.putValue(Action.NAME, "Undo");

        this.redoAction = new EDFRedoAction(this);
        redoAction.putValue(Action.NAME, "Redo");

        synchronizeActions(); // to set initial names
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        try {
            return super.addEdit(anEdit);
        } finally {
            synchronizeActions();
        }
    }

    @Override
    protected void undoTo(UndoableEdit edit) throws CannotUndoException {
        try {
            super.undoTo(edit);
        } finally {
            synchronizeActions();
        }
    }

    @Override
    protected void redoTo(UndoableEdit edit) throws CannotRedoException {
        try {
            super.redoTo(edit);
        } finally {
            synchronizeActions();
        }
    }

    protected void synchronizeActions() {
        //System.out.println("canUndo() = " + canUndo());
        undoAction.setEnabled(canUndo());
        //undoAction.putValue(Action.NAME, getUndoPresentationName());
        //System.out.println("canRedo() = " + canRedo());
        redoAction.setEnabled(canRedo());
        //redoAction.putValue(Action.NAME, getRedoPresentationName());
    }
    
    class EDFUndoAction extends AbstractAction {
        protected final UndoManager manager;

        public EDFUndoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                manager.undo();
            } catch (CannotUndoException ex) {
                ex.printStackTrace();
            }
        }
    }

    class EDFRedoAction extends AbstractAction {
        protected final UndoManager manager;

        public EDFRedoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                manager.redo();
            } catch (CannotRedoException ex) {
                ex.printStackTrace();
            }
        }

    }
}


