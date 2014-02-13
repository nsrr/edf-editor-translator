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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

/*
 * Works as the header of EDF tables
 * copiously adapted from http://www.jguru.com/faq/view.jsp?EID=87579
 * Fangping Huang, 08/11/2010
 */
public class TableRowHeader extends JList {
    private JTable table;

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


class TableRowHeaderModel extends AbstractListModel {
    private JTable table;

    public TableRowHeaderModel(JTable table) {
        this.table = table;
    }

    public int getSize() {
        return table.getRowCount();
    }

    public Object getElementAt(int index) {
        return null;
    }
}


class RowHeaderRenderer extends JLabel implements ListCellRenderer {
    private JTable table;
    private Border selectedBorder;
    private Border normalBorder;
    private Font selectedFont;
    private Font normalFont;
    //background color after selected
    private Color slbkColor = new Color(255, 213, 141);    
    private Color bkColor;

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
