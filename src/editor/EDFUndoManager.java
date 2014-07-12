package editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

@SuppressWarnings("serial")
public class EDFUndoManager extends UndoManager {
    protected Action undoAction;
    protected Action redoAction;

    /**
     * Initialize the EDFUndoManger
     */
    public EDFUndoManager() {
        this.undoAction = new EDFUndoAction(this);
        undoAction.putValue(Action.NAME, "Undo");

        this.redoAction = new EDFRedoAction(this);
        redoAction.putValue(Action.NAME, "Redo");

        synchronizeActions(); // to set initial names
    }

    /**
     * Get the undo action field
     * @return the undoAction field of this instance
     */
    public Action getUndoAction() {
        return undoAction;
    }

    /**
     * Get the redo action field
     * @return the redoAction field of this class
     */
    public Action getRedoAction() {
        return redoAction;
    }

    /**
     * Adds an UndoableEdit to this UndoManager, if it's possible
     * @see javax.swing.undo.UndoManager#addEdit(javax.swing.undo.UndoableEdit)
     */
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        try {
            return super.addEdit(anEdit);
        } finally {
            synchronizeActions();
        }
    }

    /**
     * Undoes the appropriate edits
     * @see javax.swing.undo.UndoManager#undoTo(javax.swing.undo.UndoableEdit)
     */
    @Override
    protected void undoTo(UndoableEdit edit) throws CannotUndoException {
        try {
            super.undoTo(edit);
        } finally {
            synchronizeActions();
        }
    }

    /** 
     * Redoes the appropriate edits
     * @see javax.swing.undo.UndoManager#redoTo(javax.swing.undo.UndoableEdit)
     */
    @Override
    protected void redoTo(UndoableEdit edit) throws CannotRedoException {
        try {
            super.redoTo(edit);
        } finally {
            synchronizeActions();
        }
    }

    /**
     * Set undoAction or redoAction according to current state
     */
    protected void synchronizeActions() {
        undoAction.setEnabled(canUndo());
        redoAction.setEnabled(canRedo());
    }
    
    class EDFUndoAction extends AbstractAction {
        protected final UndoManager manager;

        /**
         * Construct the EDFUndoAction using a UndoManager
         * @param manager the UndoManger used to construct this EDFUndoAction
         */
        public EDFUndoAction(UndoManager manager) {
            this.manager = manager;
        }

        /**
         * EDFUndoAction action
         */
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

        /**
         * Construct the EDFRedoAction using a UndoManager
         * @param manager the UndoManger used to construct this EDFRedoAction
         */
        public EDFRedoAction(UndoManager manager) {
            this.manager = manager;
        }
        
        /**
         * EDFRedoAction action
         */
        public void actionPerformed(ActionEvent e) {
            try {
                manager.redo();
            } catch (CannotRedoException ex) {
                ex.printStackTrace();
            }
        }
    }
}
