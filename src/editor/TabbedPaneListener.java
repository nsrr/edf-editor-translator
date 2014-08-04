package editor;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A listener listens responsible for disabling or enabling different buttons according to 
 * different pane selected
 */
public class TabbedPaneListener implements ChangeListener {

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent arg0)  {
		BasicEDFPane pane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
        if (pane == null)
            return;

        if (MainWindow.getSelectedTabIndex() == 0 || MainWindow.getSelectedTabIndex() == 1 ) {
        	MainWindow.addRowButton.setEnabled(false);
        	MainWindow.removeRowButton.setEnabled(false);
        	MainWindow.templateAddRowItem.setEnabled(false);
        	MainWindow.templateRemoveRowItem.setEnabled(false);
        	MainWindow.editDiscardChangesItem.setEnabled(true);
        } else if(pane instanceof ESATemplatePane) {
        	MainWindow.addRowButton.setEnabled(true);
        	MainWindow.removeRowButton.setEnabled(true);
        	MainWindow.templateAddRowItem.setEnabled(true);
        	MainWindow.templateRemoveRowItem.setEnabled(true);
        	MainWindow.editDiscardChangesItem.setEnabled(false);
        }
	}
}
