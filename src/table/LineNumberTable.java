package table;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*
 *  Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scrollpane that
 *  contains the main table.
 */
public class LineNumberTable extends JTable implements ChangeListener,
                                                       PropertyChangeListener {
    private JTable main;

    public LineNumberTable(JTable table) {
        main = table;
        main.addPropertyChangeListener(this);

        setFocusable(false);
        setAutoCreateColumnsFromModel(false);
        //setModel( main.getModel() );
        //setSelectionModel( main.getSelectionModel() );

        TableColumn column = new TableColumn();
        column.setHeaderValue(" ");
        addColumn(column);
        column.setCellRenderer(new RowNumberRenderer());

        getColumnModel().getColumn(0).setPreferredWidth(30);
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Component c = getParent();

        //  Keep scrolling of the row table in sync with the main table.

        if (c instanceof JViewport) {
            JViewport viewport = (JViewport)c;
            viewport.addChangeListener(this);
            validate();
            repaint();
        }
    }

    /*
    *  Delegate method to main table
    */

    @Override
    public int getRowCount() {
        return main.getRowCount();
    }

    @Override
    public int getRowHeight(int row) {
        return main.getRowHeight(row);
    }

    /*
	 *  This table does not use any data from the main TableModel,
	 *  so just return a value based on the row parameter.
	 */

    @Override
    public Object getValueAt(int row, int column) {
        //System.out.println("here, row = " + row + ", column = " + column);
        return Integer.toString(row + 1);
    }

    /*
	 *  Don't edit data in the main TableModel by mistake
	 */

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }


    //  Implement the ChangeListener
    /*
     *  scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);  seems not necessary, since
     *  scrollPane.getVerticalScrollBar().getValue() is always equal to viewport.getViewPosition().y
     */

    public void stateChanged(ChangeEvent e) {
        //  Keep the scrolling of the row table in sync with main table
        JViewport viewport = (JViewport)e.getSource();
        if (viewport.getParent() instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)viewport.getParent();
            //System.out.println("scrollPane.getVerticalScrollBar().getValue =" + scrollPane.getVerticalScrollBar().getValue());
            scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
            //System.out.println("viewport.getViewPosition().y =" + viewport.getViewPosition().y);
        }
    }

    //  Implement the PropertyChangeListener

    public void propertyChange(PropertyChangeEvent e) {
        //  Keep the row table in sync with the main table

        if ("selectionModel".equals(e.getPropertyName())) {
            setSelectionModel(main.getSelectionModel());
        }

        if ("model".equals(e.getPropertyName())) {
            setModel(main.getModel());
        }

    }

    /**
     *  Borrow the renderer from JDK1.4.2 table header
     */

    private static class RowNumberRenderer extends DefaultTableCellRenderer {
        final static Color bkColor = new Color(246, 207, 134);
        final static Color fgColor = Color.BLACK;

        public RowNumberRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected) {
                //System.out.println("value = " + value + ", row = " + row);
                //setFont(getFont().deriveFont(Font.BOLD));
            }
            //Fangping, 08/11/2010
            setText(Integer.toString(row + 1));
            //setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            validate();
            repaint();

            return this;
        }
    }
}
