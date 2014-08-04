package editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import table.EDFTable;
import table.EIATable;
import table.ESATable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class serves two-fold purposes:
 * <ul>
 * <li> create a panel to tabbed pane which contains table and a log TextArea
 * <li> customize the table. For example, designate cell editor, render and constraints.
 * </ul>
 */
@SuppressWarnings("serial")
public class WorkingTablePane extends BasicEDFPane {

    private EDFTable edfTable;
    private JLabel filePathLabel;
    private JLabel warningLabel;
    private static JPopupMenu customerizeTypePopup;
    private static JMenuItem customizeItem;

    private static final String path_prefix = "Path: "; 
    private Font oldFont = MainWindow.tabPane.getFont();

    static {
        customerizeTypePopup = new JPopupMenu();
        customizeItem = new JMenuItem("Customerize type");
        customerizeTypePopup.add(customizeItem);
    }

    /**
     * Initializes the WorkingTablePane using an EDFTable
     * @param table the EDFTable
     */
    public WorkingTablePane(EDFTable table) {
        this.setIsPrimaryTab(true);

        edfTable = table;
        edfTable.setAutoscrolls(true);        
        setupTable(); 
        setupPaneType();
        addListeners();        
        setupLayout();
    }

    /**
     * Sets up this pane type, the type is defined in BasicEDFPane
     * @see BasicEDFPane#type_eiapane
     */
    public void setupPaneType() {
        if (edfTable instanceof EIATable) {
            setPaneType(type_eiapane);
            return;
        }
        
        if (edfTable instanceof ESATable) {
            setPaneType(type_esapane);
            return;
        }
    }
    
    /**
     * Adds listeners to cell
     */
    void addListeners() {       
        edfTable.getCellEditor(1, 1).addCellEditorListener(new CellEditorListeners());
        customizeItem.addActionListener(new CellMouseClickedListener());
    }

    /**
     * Customerizes the table's cell editor, renderer, and so on
     */
    public void setupTable() {
        // if (edfTable instanceof ESATable)
        MouseListener mList[] = edfTable.getMouseListeners();
        if (mList.length < 3) {
            edfTable.addMouseMotionListener(new CellMouseListener());
            edfTable.addMouseListener(new CellMouseClickedListener());
        }
    }

    /**
     * Sets up layout
     */
    private void setupLayout() {         
        FormLayout layout = new FormLayout("d:n, f:d:g, l:p:n, 4dlu:n, 6dlu:n", "2dlu, 15dlu:n, 2dlu:n, f:d:g, 6dlu:n, b:d:n");
        
        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();
               
        JScrollPane scroller = new JScrollPane(edfTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setBorder(BorderFactory.createLineBorder(new Color(79, 136, 199)));
        editor.Utility.setRowHeader(edfTable);
        this.add(scroller, cc.xywh(1, 4, 5, 1));
        
        createBtPane();
        createShowHideCheckBox();
        this.add(filePathLabel, cc.xy(2, 2));
        this.add(allCellsShownCbox, cc.xy(3, 2));        
    }
    
    /**
     * Creates the show hide check box
     */
    private void createShowHideCheckBox() {
        JCheckBox cbox;
        cbox = new JCheckBox("Show read-only cells");
        Font cboxFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize()+ 2);
        cbox.setFont(cboxFont);
        cbox.addActionListener(new showHideListener());
        
        allCellsShownCbox = cbox;
        setupCheckBoxStatus(true);
    }
    
    /**
     * Sets the all cells shown status of the check box	
     * @param active true to show all cells
     */
    private void setupCheckBoxStatus(boolean active) {
        allCellsShownCbox.setSelected(active);
        setAllCellsShown(active);
    }
    
    /**
     * Show/hide immutable fields according to check box status
     */
    class showHideListener implements ActionListener {
        private boolean selected;
        JCheckBox cbox;
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            cbox = (JCheckBox)e.getSource();
            selected = cbox.isSelected();
            performActions();
        }
        
        /**
         * Perform actions based on the check box selection status
         */
        private void performActions() {
            if (selected)
                edfTable.showImmutableFields();
            else
                edfTable.hideImmutableFields();                
        }        
    }
     
    /**
     * Creates the labels of file name and warning messages
     */
    private void createBtPane() {
        String filename;
        if (edfTable instanceof ESATable) {
            File file = edfTable.getMasterFile();
            filename = path_prefix + file.getAbsolutePath();            
        } else
            filename = " ";
        filePathLabel = new JLabel(filename, JLabel.LEFT); 
        warningLabel = new JLabel( "Purple cells are uneditable", new ImageIcon(Main.class.getResource("/icon/Favorites-icon.png")), JLabel.RIGHT);
        warningLabel.setForeground(EDFTable.stripBackgroundClr);
        warningLabel.setOpaque(true);
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() + 2);
        filePathLabel.setFont(newFont);
        warningLabel.setFont(newFont);

        repaint();
    }

    /**
     * Returns the current time as string
     * @return the current time
     */
    public String currentTimeToString() {
        Date time = new Date();
        DateFormat df = new SimpleDateFormat("h:mm a");
        return df.format(time);
    }

    /**
     * Sets the EDF table
     * @param edfTable the EDF table
     */
    public void setEdfTable(EDFTable edfTable) {
        this.edfTable = edfTable;
    }

    /**
     * Returns the EDF table
     * @return the EDF table
     */
    public EDFTable getEdfTable() {
        return edfTable;
    }

    /**
     * Adds text to file path text string
     * @param text the text to be added
     */
    public void setTextToFilePathLabel(String text) {
        filePathLabel.setText(path_prefix + text);
    }

    /**
     * Returns the label which contains file path
     * @return the {@code JLable} contains the file path
     */
    public JLabel getFilePathLabel() {
        return filePathLabel;
    }

    /**
     * Sets the label containing the file path
     * @param filePathLabel the file label
     */
    public void setFilePathLabel(JLabel filePathLabel) {
        this.filePathLabel = filePathLabel;
    }

    /**
     * Handles mouse clicked event
     */
    private class CellMouseClickedListener implements MouseListener, ActionListener {

        /**
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (edfTable instanceof ESATable) {            	
                int sely = edfTable.getSelectedColumn();
                int selx = edfTable.getSelectedRow();
                 
                int mouseMod = e.getModifiers();
                if (mouseMod == 24 && sely == 1)  // what is 24? might be alt + mouse_button_1
                    new TransducerListener(new JFrame(), selx, sely, edfTable);
                    //customerizeTypePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            }
        }
        
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int sely = edfTable.getSelectedColumn();
            int selx = edfTable.getSelectedRow();
            new TransducerListener(new JFrame(), selx, sely, edfTable);
        }
        
        /**
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited(MouseEvent e) {
            MainWindow.setCellContent("");
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}
    }

    /**
     * Handles mouse drag and move event
     */
    private class CellMouseListener implements MouseMotionListener {

        /**
         * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        public void mouseDragged(MouseEvent e) {
            EDFTable table = (EDFTable)e.getSource();
            int nrows = table.getSelectedRowCount();
            int ncols = table.getSelectedColumnCount();

            String tr = (nrows > 1) ? " rows" : " row";
            String tc = (ncols > 1) ? " columns" : " column";
            String vb = (ncols == 1 && nrows == 1) ? " is" : " are";

            String text =
                nrows + " " + tr + " and " + ncols + tc + vb + " Selected.";
            MainWindow.setCellContent(text);
        }

        /**
         * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
         */
        public void mouseMoved(MouseEvent e) {
            EDFTable table = (EDFTable)e.getSource();
            int rr = table.rowAtPoint(e.getPoint());
            int cc = table.columnAtPoint(e.getPoint());
            String text = "Selected Value:  " + (String)table.getValueAt(rr, cc);
            MainWindow.setCellContent(text);
        }
    }
    
    /**
     * TODO: 09/28/2010
     */
    private class CellEditorListeners implements KeyListener, CellEditorListener {

        public void keyTyped(KeyEvent e) {
            //System.out.println("I am pressed. 1");
        }

        public void keyPressed(KeyEvent e) {
           // System.out.println("I am pressed. 2 ");
        }

        public void keyReleased(KeyEvent e) {
            //System.out.println("I am pressed. 3");
        }
        
        //this is the right place to update the cell list table when editing is finished
        public void editingStopped(ChangeEvent e) {
//            DefaultCellEditor editor = (DefaultCellEditor)e.getSource();
//            MainWindow.consolePane.outputMessage((String)editor.getCellEditorValue());
        }

        public void editingCanceled(ChangeEvent e) {}
    }
}
