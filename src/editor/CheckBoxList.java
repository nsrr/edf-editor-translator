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


public class CheckBoxList extends JList {

    protected static Border noFocusBorder = new EmptyBorder(1, 4, 1, 4);
    DefaultListModel model;

    public CheckBoxList() {
        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {
                        JCheckBox checkbox =
                            (JCheckBox)getModel().getElementAt(index);
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
    @Override
    protected void paintComponent(Graphics g){
        //g.setColor(backgroundColor);
        //g.fillRect(0,0, getWidth(), getHeight());

        super.paintComponent(g);
    } 

    public void addCheckbox(JCheckBox checkBox) {
        ListModel currentList = this.getModel();
        int currentSize = currentList.getSize();
        JCheckBox[] newList = new JCheckBox[currentSize + 1];
        for (int i = 0; i < currentSize; i++) {
            newList[i] = (JCheckBox)currentList.getElementAt(i);
        }
        newList[newList.length - 1] = checkBox;
        setListData(newList);
    }

    public void appendCheckbox(JCheckBox checkBox) {
        ListModel currentList = this.getModel();
        int currentSize = currentList.getSize();
        JCheckBox[] newList = new JCheckBox[currentSize + 1];
        for (int i = 0; i < currentSize; i++) {
            newList[i] = (JCheckBox)currentList.getElementAt(i);
        }
        newList[newList.length - 1] = checkBox;
        setListData(newList);
    }


    public JCheckBox getCheckBox(int index) {
        ListModel currentList = this.getModel();
        JCheckBox checkBox = (JCheckBox)currentList.getElementAt(index);

        return checkBox;
    }

    public void setCheckBoxSelected(int index, Boolean selected) {
        ListModel currentList = this.getModel();
        JCheckBox checkBox = (JCheckBox)currentList.getElementAt(index);
        checkBox.setSelected(selected);
    }

    public void SetAllCheckBoxSelected(Boolean selected) {
        ListModel currentList = this.getModel();
        int size = currentList.getSize();
        for (int i = 0; i < size; i++) {
            setCheckBoxSelected(i, selected);
        }
    }

    public ArrayList<Integer> getSelectedCheckBox() {
        ArrayList<Integer> selectedIdx = new ArrayList<Integer>(10);
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


    protected class CellRenderer implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox)value;
            //checkbox.setBackground(isSelected ? getSelectionBackground() :
             //                      getBackground());
           // checkbox.setForeground(isSelected ? getSelectionForeground() :
           //                        getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusable(true);
            checkbox.setFocusPainted(true);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ?
                               UIManager.getBorder("List.focusCellHighlightBorder") :
                               noFocusBorder);

            repaint();

            return checkbox;
        }


    }
    
    class CheckBoxListModel<T> extends AbstractListModel {
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
    
    class ListItem<T> {
            protected T dataItem;
            protected boolean selected;

            public ListItem(T dataItem, boolean selected) {
                this.dataItem = dataItem;
                this.selected = selected;
            }
        }

}


