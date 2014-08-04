package table;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Use a JTable as a renderer for row numbers of a given main table.
 * This table must be added to the row header of the scrollpane that
 * contains the main table.
 */
@SuppressWarnings("serial")
public class LineNumberTable extends JTable implements ChangeListener,  PropertyChangeListener {

	private JTable main;

    /**
     * Constructs a LineNumberTable using a table
     * @param table the main table to operate on 
     */
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

    /**
     * @see javax.swing.JTable#addNotify()
     */
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

    /**
     *  Delegates method to main table
     */
    @Override
    public int getRowCount() {
        return main.getRowCount();
    }

    /**
     * @see javax.swing.JTable#getRowHeight(int)
     */
    @Override
    public int getRowHeight(int row) {
        return main.getRowHeight(row);
    }

    /**
	 * This table does not use any data from the main TableModel,
	 * so just return a value based on the row parameter.
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column) {
        //System.out.println("here, row = " + row + ", column = " + column);
        return Integer.toString(row + 1);
    }

    /**
	 *  Don't edit data in the main TableModel by mistake
     * @see javax.swing.JTable#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

//      Implement the ChangeListener
//      scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);  seems not necessary, since
//      scrollPane.getVerticalScrollBar().getValue() is always equal to viewport.getViewPosition().y

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
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
    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
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
     *  Borrows the renderer from JDK1.4.2 table header
     */
    private static class RowNumberRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		@SuppressWarnings("unused")
		final static Color bkColor = new Color(246, 207, 134);
        @SuppressWarnings("unused")
		final static Color fgColor = Color.BLACK;

        /**
         * Default RowNumberRenderer constructor
         */
        public RowNumberRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        /**
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
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
