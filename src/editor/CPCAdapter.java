package editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import table.EDFTable;

/**
 * This class is to implement Cut/Copy/Paste and Redo/Undo functionalities.
 */
public class CPCAdapter implements ActionListener {
    @SuppressWarnings("unused")
	private String operationCode;
    private int codeIndex;
    // system buffer for data transfer
    private Clipboard systemBuf = Toolkit.getDefaultToolkit().getSystemClipboard(); 
    private StringSelection strSel = null;

    protected final static String COPY = "Copy";
    private final static String CUT = "Cut";
    private final static String PASTE = "Paste";
    EDFTable dataTable = null;

    /**
     * Construct this CPCAdapter using COPY, CUT, or PASTE instruction
     * @param code the mode to initialize this CPCAdapter
     */
    public CPCAdapter(String code) {
        super();
        operationCode = code;
        if (code.equalsIgnoreCase(COPY))
            codeIndex = 0;
        else if (code.equalsIgnoreCase(CUT))
            codeIndex = 1;
        else if (code.equalsIgnoreCase(PASTE))
            codeIndex = 2;
        else
            System.out.println("wrong specfication of operation");
    }

    /**
     * Get the table from current selected tabbed pane 
     */
    public void acquireDataTable() {
        if (MainWindow.tabPane.getSelectedComponent() == null)
            return;

        BasicEDFPane splitPane = (BasicEDFPane)MainWindow.tabPane.getSelectedComponent();
        if (splitPane instanceof WorkingTablePane) {
            WorkingTablePane pane = (WorkingTablePane)splitPane;
            dataTable = pane.getEdfTable();
        } else if (splitPane instanceof ESATemplatePane) {
            ESATemplatePane pane = (ESATemplatePane)splitPane;
            dataTable = pane.getEsaTemplateTable();
        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
    	
        acquireDataTable();
        if (dataTable == null)
            return;
        if (dataTable.getSelectedColumnCount() != 1 || dataTable.getSelectedRowCount() != 1)
            return;

        switch (codeIndex) {
        case 0:
            copyOperation();
            break;
        case 1:
            cutOperation();
            break;
        case 2:
            pasteOperation();
            break;
        default:
            // do nothing
        }
    }

    /**
     * Copy operation
     */
    public void copyOperation() {
        String strBuf = "";
        int rr = dataTable.getSelectedRow();
        int cc = dataTable.getSelectedColumn();
        strBuf = (String)dataTable.getValueAt(rr, cc);
        strSel = new StringSelection(strBuf);
        systemBuf.setContents(strSel, strSel);
        System.out.println(systemBuf);
    }

    /**
     * Cut operation
     */
    public void cutOperation() {
        String strBuf = "";
        int rr = dataTable.getSelectedRow();
        int cc = dataTable.getSelectedColumn();
        strBuf = (String)dataTable.getValueAt(rr, cc);
        dataTable.setValueAt("", rr, cc);
        strSel = new StringSelection(strBuf);
        systemBuf.setContents(strSel, strSel);
        System.out.println(systemBuf);
    }

    /**
     * Paste operation
     */
    public void pasteOperation() {
        int rr = dataTable.getSelectedRow();
        int cc = dataTable.getSelectedColumn();

        String buffer = null;
        try {
            buffer = (String)(systemBuf.getContents(this).getTransferData(DataFlavor.stringFlavor));
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (buffer == null)
            return;
        dataTable.setValueAt(buffer, rr, cc);
    }
}
