/**
 * this class serves two-fold purposes:
 * <ul>
 * <li> create a panel to tabbed pane which contains table and a log TextArea
 * <li> customize the table. For example, designate cell editor, render and constraints.
 * </ul>
 */

package editor;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import table.EDFTable;
import table.EIATable;
import table.ESATable;


public class WorkingTablePane extends BasicEDFPane {

    private EDFTable edfTable;

    private JLabel filePathLabel;
    private  JLabel warningLabel;
    private static JPopupMenu customerizeTypePopup;
    private static JMenuItem customizeItem;
    
    private static final String path_prefix = "Path: ";    
    
    //private JPopupMenu editorPopup;
    //private JPopupMenu tablePopup;
    
    private Font oldFont = MainWindow.tabPane.getFont();

    static {
        customerizeTypePopup = new JPopupMenu();
        customizeItem = new JMenuItem("Customerize type");
        customerizeTypePopup.add(customizeItem);
    }
    
    public WorkingTablePane(EDFTable table) {
        this.setIsPrimaryTab(true);
        
        edfTable = table;
        edfTable.setAutoscrolls(true);        
        setupTable(); 
        setupPaneType();
        addListeners();        
        setupLayout();
    }
    
    public void setupPaneType(){
        if (edfTable instanceof EIATable){
            setPaneType(type_eiapane);
            return;
        }
        
        if (edfTable instanceof ESATable){
            setPaneType(type_esapane);
            return;
        }
    }
    
    void addListeners(){       
        edfTable.getCellEditor(1, 1).addCellEditorListener(new CellEditorListeners());
        customizeItem.addActionListener(new CellMouseClickedListener());
    }

    /**
     *  customerize the table's cell editor, renderer, and so on
     */
    public void setupTable() {
        //if (edfTable instanceof ESATable)
        MouseListener mList[] = edfTable.getMouseListeners();
        if (mList.length < 3) {
            edfTable.addMouseMotionListener(new CellMouseListener());
            edfTable.addMouseListener(new CellMouseClickedListener());
        }
    }

    /**
     * @return table panel
     */
    private void setupLayout() {         
        FormLayout layout = new FormLayout("d:n, f:d:g, l:p:n, 4dlu:n, 6dlu:n", "2dlu, 15dlu:n, 2dlu:n, f:d:g, 6dlu:n, b:d:n");
        
        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();
               
        JScrollPane scroller =
            new JScrollPane(edfTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setBorder(BorderFactory.createLineBorder(new Color(79, 136, 199)));
        editor.Utility.setRowHeader(edfTable);
        this.add(scroller, cc.xywh(1, 4, 5, 1));
        
        createBtPane();
        createShowHideCheckBox();
        this.add(filePathLabel, cc.xy(2, 2));
        this.add(allCellsShownCbox, cc.xy(3, 2));        
    }
    
    private void createShowHideCheckBox(){
        JCheckBox cbox;
        cbox = new JCheckBox("Show read-only cells");
        Font cboxFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize()+ 2);
        cbox.setFont(cboxFont);
        cbox.addActionListener(new showHideListener());
        
        allCellsShownCbox = cbox;
        setupCheckBoxStatus(true);
    }
    
    private void setupCheckBoxStatus(boolean active){
        allCellsShownCbox.setSelected(active);
        setAllCellsShown(active);
    }
    
    class showHideListener implements ActionListener{
        private boolean selected;
        JCheckBox cbox;
        public void actionPerformed(ActionEvent e) {
            cbox = (JCheckBox)e.getSource();
            selected = cbox.isSelected();
            performActions();
        }
        
        private void performActions(){
            if (selected)
                edfTable.showImmutableFields();
            else
                edfTable.hideImmutableFields();                
        }        
    }

     
    private void createBtPane(){
        String filename;
        if (edfTable instanceof ESATable) {
            File file = edfTable.getMasterFile();
            filename = path_prefix + file.getAbsolutePath();            
        }
        else
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

    public String currentTimeToString() {
        Date time = new Date();
        DateFormat df = new SimpleDateFormat("h:mm a");
        return df.format(time);
    }

    public void setEdfTable(EDFTable edfTable) {
        this.edfTable = edfTable;
    }

    public EDFTable getEdfTable() {
        return edfTable;
    }

    public void setTextToFilePathLabel(String text) {
        filePathLabel.setText(path_prefix + text);
    }

    public JLabel getFilePathLabel() {
        return filePathLabel;
    }

    public void setFilePathLabel(JLabel filePathLabel) {
        this.filePathLabel = filePathLabel;
    }

    private class CellMouseClickedListener implements MouseListener, ActionListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (edfTable instanceof ESATable) {
                int sely = edfTable.getSelectedColumn();
                int selx = edfTable.getSelectedRow();
                 
                int mouseMod = e.getModifiers();
                if (mouseMod == 24 && sely == 1) 
                    new TransducerListener(new JFrame(), selx, sely, edfTable);
                    //customerizeTypePopup.show((JComponent)e.getSource(), e.getX(), e.getY());
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            int sely = edfTable.getSelectedColumn();
            int selx = edfTable.getSelectedRow();
            new TransducerListener(new JFrame(), selx, sely, edfTable);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            MainWindow.setCellContent("");
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }


        @Override
        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // TODO Auto-generated method stub

        }


    }

    private class CellMouseListener implements MouseMotionListener {

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

        public void mouseMoved(MouseEvent e) {
            EDFTable table = (EDFTable)e.getSource();
            int rr = table.rowAtPoint(e.getPoint());
            int cc = table.columnAtPoint(e.getPoint());
            String text = "Selected Value:  " + (String)table.getValueAt(rr, cc);
            MainWindow.setCellContent(text);
        }
    }
    
    /*
     * TODO: 09/28/2010
     */
    private class CellEditorListeners implements KeyListener, CellEditorListener{

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
        //TODO:
        public void editingStopped(ChangeEvent e) {
/*             DefaultCellEditor editor = (DefaultCellEditor)e.getSource();
            MainWindow.consolePane.outputMessage((String)editor.getCellEditorValue()); */
        }

        public void editingCanceled(ChangeEvent e) {
            //TODO: system stub
        }
    }
    
    

}

