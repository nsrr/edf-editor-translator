package editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A customized JList containing items of check boxes 
 */
@SuppressWarnings("serial")
public class CheckBoxList extends JList<JCheckBox> {
	
	////////////////////////////////////////////////////////////////////////////////////////
	// ListModel to ListModel<JCheckBox>; DefaultListModel to DefaultListModel<JCheckBox> //
	// JList to JList<JCheckBox>, AbstractListModel to AbstractListModel<ListItem<T>> 	  //
	// ListCellRenderer to ListCellRenderer<Object>, JList to JList<?>  				  //
	// AbstractListModel to AbstractListModel<ListItem<T>>								  //
	// by wei wang, 2014-7-15														      //
	////////////////////////////////////////////////////////////////////////////////////////

    protected static Border noFocusBorder = new EmptyBorder(1, 4, 1, 4);
    DefaultListModel<JCheckBox> model;

    /**
     * Default constructor for CheckBoxList
     */
    public CheckBoxList() {
        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkbox = (JCheckBox)getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    checkbox.setText(checkbox.getText());
                    revalidate();
                    repaint();
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    //Fangping, 08/23/2010
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g){
        //g.setColor(backgroundColor);
        //g.fillRect(0,0, getWidth(), getHeight());

        super.paintComponent(g);
    } 

    /**
     * Adds a check box to this list
     * @param checkBox the new check box to be added
     */
    public void addCheckbox(JCheckBox checkBox) {
		ListModel<JCheckBox> currentList = (ListModel<JCheckBox>)this.getModel();
        int currentSize = currentList.getSize();
        JCheckBox[] newList = new JCheckBox[currentSize + 1];
        for (int i = 0; i < currentSize; i++) {
            newList[i] = (JCheckBox)currentList.getElementAt(i);
        }
        newList[newList.length - 1] = checkBox;
        setListData(newList);
    }

    /**
     * Appends a check box to this list, same as method: {@link #addCheckbox(JCheckBox)}
     * @param checkBox the new check box to be appended
     */
    public void appendCheckbox(JCheckBox checkBox) {    	
        ListModel<JCheckBox> currentList = (ListModel<JCheckBox>)this.getModel();
        int currentSize = currentList.getSize();
        JCheckBox[] newList = new JCheckBox[currentSize + 1];
        for (int i = 0; i < currentSize; i++) {
            newList[i] = (JCheckBox)currentList.getElementAt(i);
        }
        newList[newList.length - 1] = checkBox;
        setListData(newList);
    }

    /**
     * Gets the check box at specific index
     * @param index the index
     * @return the check box at index 'index'
     */
    public JCheckBox getCheckBox(int index) {
        ListModel<JCheckBox> currentList = (ListModel<JCheckBox>)this.getModel();
        JCheckBox checkBox = (JCheckBox)currentList.getElementAt(index);

        return checkBox;
    }

    /**
     * Sets the specified check box selected
     * @param index the index of the check box to be set selected
     * @param selected true to select the sepcified check box
     */
    public void setCheckBoxSelected(int index, Boolean selected) {
    	ListModel<JCheckBox> currentList = (ListModel<JCheckBox>)this.getModel();
        JCheckBox checkBox = (JCheckBox)currentList.getElementAt(index);
        checkBox.setSelected(selected);
    }

    /**
     * Sets all check box selected according to the parameter
     * @param selected true to select all checkbox, false otherwise
     */
    public void SetAllCheckBoxSelected(Boolean selected) {
    	ListModel<JCheckBox> currentList = (ListModel<JCheckBox>)this.getModel();
        int size = currentList.getSize();
        for (int i = 0; i < size; i++) {
            setCheckBoxSelected(i, selected);
        }
    }

    /**
     * Returns a list of the index of the selected check box
     * @return the index list
     */
    public ArrayList<Integer> getSelectedCheckBox() {
        ArrayList<Integer> selectedIdx = new ArrayList<Integer>(10);
        @SuppressWarnings("unused")
		int looper = 0;
        int count = this.getModel().getSize();
        for (int i = 0; i < count; i++) {
            if (this.getCheckBox(i).isSelected()) {
                looper++;
                selectedIdx.add(new Integer(i));
            }
        }

        return selectedIdx;
    }

    /**
     * A cell renderer for this CheckBoxList
     */
    protected class CellRenderer implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox)value;
            // checkbox.setBackground(isSelected ? getSelectionBackground() :
            // getBackground());
            // checkbox.setForeground(isSelected ? getSelectionForeground() :
            // getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusable(true);
            checkbox.setFocusPainted(true);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            repaint();
            return checkbox;
        }
    }
    
    /**
     * The model of this CheckBoxList
     * @param <T> the data type
     */
    class CheckBoxListModel<T> extends AbstractListModel<ListItem<T>> {
    	
            private LinkedList<ListItem<T>> items = new LinkedList<ListItem<T>>(); 
     
            /**
             * Initializes a new CheckBoxListModel
             * @param items The items to add to the list
             */
            public CheckBoxListModel(LinkedHashMap<T, Boolean> items) {
                Iterator<T> iter = items.keySet().iterator();
                while(iter.hasNext()) {
                    T acc = iter.next();
                    this.items.add(new ListItem<T>(acc, items.get(acc)));
                }
            }
     
            /**
             * Returns the size of the list
             * @return The size of the list
             */
            public int getSize() {
                return items.size();
            }
     
            /**
             * Returns the ListItem at the specified index
             * @param index The index of the item to find
             * @return The ListItem at the specified index
             */
            public ListItem<T> getElementAt(int index) {
                return items.get(index);
            }
     
            /**
             * Returns the collection of items in the list
             * @return The collection of items in the list
             */
            @SuppressWarnings("unused")
			private LinkedHashMap<T, Boolean> getItems() {
                LinkedHashMap<T, Boolean> map = new LinkedHashMap<T, Boolean>();
                for(ListItem<T> item : items) {
                    map.put(item.dataItem, item.selected);
                }
                return map;
            }
     
            /**
             * Selects all items in the list
             */
            public void selectAll() {
                for(ListItem<T> item : items) {
                    item.selected = true;
                }
            }
     
            /**
             * Deselects all items in the list
             */
            public void selectNone() {
                for(ListItem<T> item : items) {
                    item.selected = false;
                }
            }
        }
    
    /**
     * A list item used as this check box list model data
     * @param <T> the type of data to be stored
     */
    class ListItem<T> {
        protected T dataItem;
        protected boolean selected;

        /**
         * Constructor using the data item and a boolean flag
         * @param dataItem the data to be stored
         * @param selected true indicates the item stored is selected
         */
        public ListItem(T dataItem, boolean selected) {
            this.dataItem = dataItem;
            this.selected = selected;
        }
    }
}
