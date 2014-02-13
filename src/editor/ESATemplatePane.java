package editor;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import header.ESAHeader;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import table.EDFTable;
import table.ESATemplateTable;


public class ESATemplatePane extends BasicEDFPane {

    ESATemplateTable esaTemplateTable;

    private static long uid = 0;
    private long pid;

    private JLabel filePathLabel;
    private JLabel warningLabel;
    private JPanel tablePane;
    
    private Font oldFont = MainWindow.tabPane.getFont();

    public ESATemplatePane() {
        super();
        incrementUid();
        pid = uid;

        esaTemplateTable = new ESATemplateTable();
        tablePane = this.createTablePane();
        createFilePathLabel();  

        setupLayout();        
        addListeners();
    }

    public ESATemplatePane(ESAHeader esaHeader, boolean istemplate) {
        super();
        incrementUid();

        if (esaHeader.getHostEdfFile() != null)
            masterFile = esaHeader.getHostEdfFile();
        esaTemplateTable = new ESATemplateTable(esaHeader, istemplate);

        tablePane = this.createTablePane();        
        createFilePathLabel();  
        
        setupLayout();        
        addListeners();
    }

    /**
     *  customerize the table's cell editor, renderer, and so on
     */
    public void addListeners() {
        esaTemplateTable.addMouseMotionListener(new CellMouseListener());
        esaTemplateTable.addMouseListener(new CellMouseClickedListener());
    }

    private void createFilePathLabel() {
        String filename;
        if (masterFile != null)
            filename = "Path: " + masterFile.getAbsolutePath();
        else
            filename = " ";
        filePathLabel = new JLabel(filename, JLabel.LEFT);
        warningLabel =
                new JLabel("Purple cells are uneditable", new ImageIcon(Main.class.getResource("/icon/Favorites-icon.png")),
                           JLabel.RIGHT);
        warningLabel.setForeground(EDFTable.stripBackgroundClr);
        warningLabel.setOpaque(true);      
        Font font = warningLabel.getFont();
        filePathLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 2));
        warningLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 2));

        repaint();
    }


    /**
     * @return table panel
     */
    public JPanel createTablePane() {
        FormLayout layout = new FormLayout("f:p:g:", "f:p:g");
        JPanel pane = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        esaTemplateTable.setAutoscrolls(true);        
        JScrollPane scroller = new JScrollPane(esaTemplateTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        editor.Utility.setRowHeader(esaTemplateTable);
        //scroller.setHorizontalScrollBar(new JScrollBar());
        scroller.setBorder(BorderFactory.createLineBorder(new Color(79, 136, 199)));

        pane.add(scroller, cc.xy(1, 1));

        return pane;
    }

    public void setupLayout() {
        FormLayout layout =
            new FormLayout("d:n, f:d:g, l:p:n, 4dlu:n, 6dlu:n", "15dlu:n, 2dlu:n, f:d:g, 6dlu:n, b:d:n");
        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();          
        createShowHideCheckBox();
        
        this.add(tablePane, cc.xywh(1, 3, 5, 1));
        this.add(filePathLabel, cc.xy(2, 1));
        this.add(allCellsShownCbox, cc.xy(3, 1));   
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
                esaTemplateTable.showImmutableFields();
            else
                esaTemplateTable.hideImmutableFields();                
        }        
    }
    
    private void setupCheckBoxStatus(boolean active){
        allCellsShownCbox.setSelected(active);
        setAllCellsShown(active);
    }


    public void setEsaTemplateTable(ESATemplateTable esaTemplateTable) {
        this.esaTemplateTable = esaTemplateTable;
    }

    public ESATemplateTable getEsaTemplateTable() {
        return esaTemplateTable;
    }

    public void setPid(long myPid) {
        pid = myPid;
    }

    public long getPid() {
        return pid;
    }

    public static void incrementUid() {
        uid++;
    }


    /**
     * @return ESAHeader which can be used when saving or applyint template
     * Fangping, 02/26/2010
     */
    public ESAHeader esaHeaderFromEsaTemplateTable() {
        ESAHeader esaHeader = new ESAHeader(esaTemplateTable, true);

        return esaHeader;
    }

    private class CellMouseClickedListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent arg0) {
            int sely = esaTemplateTable.getSelectedColumn();
            int selx = esaTemplateTable.getSelectedRow();
            int mouseMod = arg0.getModifiers();
            if (sely == 2 && mouseMod == 24) {
                new TransducerListener(new JFrame(), selx, 2,
                                       esaTemplateTable);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseExited(MouseEvent e) {            
            MainWindow.setCellContent("");
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
            String value = (String)table.getValueAt(rr, cc); // do not use getModel().getValueAt(rr, cc)
            if (value == null)
                value = "";
            String text = "Selected Value:  " + value;
            MainWindow.setCellContent(text);
        }
    }

}
