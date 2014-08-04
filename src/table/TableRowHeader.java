package table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

/**
 * Works as the header of EDF tables
 * copiously adapted from http://www.jguru.com/faq/view.jsp?EID=87579
 * Fangping Huang, 08/11/2010
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class TableRowHeader extends JList {
	private JTable table;

    /**
     * Constructs the TableRowHeader using a specific JTable
     * @param table the JTable used to construct TableRowHeader
     */
    @SuppressWarnings("unchecked")
	public TableRowHeader(JTable table) {
        super(new TableRowHeaderModel(table));
        this.table = table;
        setFixedCellHeight(table.getRowHeight());
        setFixedCellWidth(preferredHeaderWidth());
        setCellRenderer(new RowHeaderRenderer(table));
        setSelectionModel(table.getSelectionModel());
        
        this.setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Returns the bounds of the specified range of items in JList
     * coordinates. Returns null if index isn't valid.
     * @param index0 the index of the first JList cell in the range
     * @param index1 the index of the last JList cell in the range
     * @return the bounds of the indexed cells in pixels
     */
    public Rectangle getCellBounds(int index0, int index1) {
        Rectangle rect0 = table.getCellRect(index0, 0, true);
        Rectangle rect1 = table.getCellRect(index1, 0, true);
        int y, height;
        if (rect0.y < rect1.y) {
            y = rect0.y;
            height = rect1.y + rect1.height - y;
        } else {
            y = rect1.y;
            height = rect0.y + rect0.height - y;
        }
        return new Rectangle(0, y, getFixedCellWidth(), height);
    }
    // assume that row header width should be big enough to display row number Integer.MAX_VALUE completely

    /**
     * Gets the preferred header width
     * @return the preferred header width
     */
    private int preferredHeaderWidth() {
        JLabel longestRowLabel = new JLabel("0000");
        JTableHeader header = table.getTableHeader();
        //longestRowLabel.setBorder(header.getBorder());
        //UIManager.getBorder("TableHeader.cellBorder"));
        longestRowLabel.setBorder( BorderFactory.createEtchedBorder());
        longestRowLabel.setHorizontalAlignment(JLabel.CENTER);
        
        Font font = header.getFont();
        int fontSize = font.getSize() - 2;
        font = new Font(font.getName(), font.getStyle(), fontSize);
        longestRowLabel.setFont(font);
        
        return longestRowLabel.getPreferredSize().width;
    }
}


/**
 * Model of this TableRowHeader
 */
@SuppressWarnings({ "serial", "rawtypes" })
class TableRowHeaderModel extends AbstractListModel {

	private JTable table;

    /**
     * Constructs this model using a JTable	
     * @param table
     */
    public TableRowHeaderModel(JTable table) {
        this.table = table;
    }

    /**
     * Gets the size of this header
     * @return the number of fields in this header
     */
    public int getSize() {
        return table.getRowCount();
    }

    /**
     * Gets the element at index <code>index</code>
     * @return the element at index <code>index</code>
     */
    public Object getElementAt(int index) {
        return null;
    }
}


/**
 * A cell renderer for the table header
 */
@SuppressWarnings({ "serial", "rawtypes" })
class RowHeaderRenderer extends JLabel implements ListCellRenderer {

	private JTable table;
    @SuppressWarnings("unused")
	private Border selectedBorder;
    @SuppressWarnings("unused")
	private Border normalBorder;
    private Font selectedFont;
    private Font normalFont;
    //background color after selected
    private Color slbkColor = new Color(255, 213, 141);    
    private Color bkColor;

    /**
     * Construct a RowHeaderRenderer
     * @param table the target table
     */
    RowHeaderRenderer(JTable table) {
        this.table = table;
        //normalBorder = UIManager.getBorder("TableHeader.cellBorder");

        selectedBorder = BorderFactory.createEtchedBorder();
        final JTableHeader header = table.getTableHeader();
        bkColor = header.getBackground();
        normalFont = header.getFont();
        selectedFont =
                normalFont.deriveFont(normalFont.getStyle() | Font.BOLD);
        setForeground(header.getForeground());
        setBackground(bkColor);
        setOpaque(true);
        setHorizontalAlignment(CENTER);
    }

    /**
     * Returns a component that has been configured to display the specified value
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        if (table.getSelectionModel().isSelectedIndex(index)) {
            setFont(selectedFont);
            setBackground(slbkColor);
            //setBorder(selectedBorder);
        } else {
            setFont(normalFont);
            setBackground(bkColor);
            //setBorder(normalBorder);
        }
        
        String label = String.valueOf(index + 1);
        setText(label);
        
        return this;
    }
}
