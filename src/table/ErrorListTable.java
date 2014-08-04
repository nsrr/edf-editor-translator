package table;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import editor.EDFTreeNode;
import editor.EIATemplatePane;
import editor.ESATemplatePane;
import editor.MainWindow;
import editor.Utility;
import editor.WorkingTablePane;


/**
 * A JTable that displays error messages and can be clicked to select corresponding cell
 */
@SuppressWarnings("serial")
public class ErrorListTable extends JTable {

//	  moved to the Incompliance class
//    public static final String Title_ErroIndex = "Error #";
//    public static final String Title_Description = "Description";
//    public static final String Title_File = "File";
//    public static final String Title_Row = "Row";
//    public static final String Title_Column = "Column";
//    public static final String Title_Type = "Type"; */
//    public static final String tableTypes[] = {"EIA", "ESA", "EIA template", "ESA template"};
    protected static final String columnNames[] = {
    	Incompliance.Title_ErroIndex, Incompliance.Title_Type, Incompliance.Title_Description, 
        Incompliance.Title_File, Incompliance.Title_Row, Incompliance.Title_Column
    };
    
//    public static final int error_number = 0;
    public static final int index_number = 0;
    public static final int type_number = 1;
    public static final int description_number = 2;
    public static final int file_number = 3;
    public static final int row_number = 4;
    public static final int column_number = 5;    
    
    public static final int maxViewableRows = 10;  
    
    /**
     * Default constructor for this ErrorListTable
     */
    public ErrorListTable() {
        super();
        this.setModel(new ErrorListModel());
        customizeLook();
        addListeners();
    }
    
    /**
     * Constructs the ErrorListTable using provided incompliances
     * @param incompliances Incompliances used to get the error list
     */
    public ErrorListTable(ArrayList<Incompliance> incompliances) {
        super();
        this.setModel(new ErrorListModel(0));
        customizeLook();
        
        yieldTableFrom(incompliances);
        addListeners();
    }
        
    /**
     * Customizes the appearance of this table
     */
    public void customizeLook() {
        this.getTableHeader().setFont(new Font("Dialog", Font.PLAIN, 12));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        this.setRowHeight(18);
        
        TableColumn verCol;
                  
        for (int i = 0; i <= 5; i++) {
            verCol = this.getColumnModel().getColumn(i);
            
            if (description_number == i || file_number == i)
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() * 2.0));
            else if (index_number == i) {
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() + 20));
                verCol.setMaxWidth((int)(verCol.getPreferredWidth() + 20));
            } else {
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() * 1.0));
                verCol.setMaxWidth((int)(verCol.getPreferredWidth() * 1.0));
            }         
        }
    }

    /**
     * Sets the icon of this error list table
     * @param table the table to set the icon for
     * @param errorCount the number of errors
     */
    public static void setIcon(JTable table, int errorCount) {
        ImageIcon icon = MainWindow.errorIcon;
        int colIndex = index_number;
        String name = (errorCount > 1)? " Errors": " Error";
        name = " " + errorCount + name;
        table.getTableHeader().getColumnModel().getColumn(colIndex).setHeaderRenderer(new HeaderIconRenderer());
        table.getColumnModel().getColumn(colIndex).setHeaderValue(new TextIcon(name, icon));        
        table.validate();        
    }
    
    /**
     * Clears and reset the number of rows to the maxViewableRows in the model
     */
    public void blankOut() {
        ErrorListModel model = (ErrorListModel)getModel();
        model.setRowCount(0);// clean all rows
        model.setRowCount(maxViewableRows);
    }
    
    /**
     * Inserts incompliances using the incompliances provided as an ArrayList
     * @param incompliances the incompliances used to generate the ErrorListTable
     */
    public void yieldTableFrom(ArrayList<Incompliance> incompliances) {        
        blankOut();      
        
        int nrow = incompliances.size();        
        Incompliance incomp;
        for (int i = 0; i < nrow; i++){
            incomp = incompliances.get(i);
            insertRowAt(incomp, i);
        }
    }
    
    /**
     * Inserts an Incompliance at specific row, expand the table accordingly
     * @param incomp the Incompliance to be inserted
     * @param rr the row number
     */
    public void insertRowAt(Incompliance incomp, int rr) {
        String type;
        String description;
        String fileName;
        int rowIndex;
        int columnIndex;
                
        type = incomp.getIncomplianceType();
        description = incomp.getDescription();
        fileName = incomp.getFileName();
        rowIndex = incomp.getRowIndex();
        columnIndex = incomp.getColumnIndex();
        ErrorListModel model = (ErrorListModel)getModel();
        if (rr < maxViewableRows) {
            model.setValueAt(rr + 1, rr, index_number);
            model.setValueAt(type, rr, type_number);
            model.setValueAt(description, rr, description_number);
            model.setValueAt(fileName, rr, file_number);
            model.setValueAt(rowIndex + 1, /*starting from 1*/rr, row_number);
            model.setValueAt(columnIndex + 1, /*starting from 1*/rr, column_number);
        } else { 
        	// when row count exceeds maxViewableRows
            model.addRow(new Object[]{rr + 1, type, description, fileName, rowIndex + 1, columnIndex + 1});
        }    
    }
    
    /**
     * Adds a mouse listener
     */
    public void addListeners() {
        this.addMouseListener(new CellMouseClickedListener());
        //this.addKeyListener(null);
    }
        
    private class CellMouseClickedListener extends MouseAdapter {
        JTable table;
        String fileName, type;
        int rowNumber, columnNumber;         
        
        public void mouseClicked(MouseEvent e) {
            int count = e.getClickCount();
            if (count < 1)
                return;
            
            table = (JTable) e.getSource(); 
            int selRow = table.getSelectedRow(); 
            try {
                if(retrieveLocation(selRow) == false)
                    return;
            } catch(NumberFormatException exception) {
                return;
            }
            
            int typeCode = retrieveHeaderTableTypeCode();
            
            if (typeCode == -1) {
                System.out.println("wrong type code");
                return;
            }
            
            /* 
             * The below code is used to highlight working-file node on the task tree
             * This feature is made by March 6, 2014
             */
            String srcFileName = fileName;
            String wrkFileName = null;
    		for (int i = 0; i < MainWindow.getSrcEdfFiles().size(); i++) {
    			File f = MainWindow.getSrcEdfFiles().get(i);
    			if (f.getAbsolutePath().equals(srcFileName)){
    				wrkFileName = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
    				break;
    			}
    		}
            
    		if (wrkFileName != null) {
    			for (int i = 0; i < MainWindow.taskTree.getEdfRootNode().getChildCount(); i++) {
            		EDFTreeNode child = (EDFTreeNode)MainWindow.taskTree.getEdfRootNode().getChildAt(i);
            		if (wrkFileName.equals(child.getHostFile().getAbsolutePath())){
            			MainWindow.taskTree.setSelectionRow(i + 2);
            			break;
            		}
            	}
    		}
        	/* The above code is used to highlight working-file node on the task tree*/
            
            if (typeCode == Incompliance.index_incomp_src_eia) {/* EIA header table*/
                redirectToEIATable(rowNumber, columnNumber);
                return;
            }
            
            if (typeCode == Incompliance.index_incomp_src_esa) {/* ESA header table*/
            	redirectToESATable(fileName, rowNumber, columnNumber);
                return;
            }
            
            if (typeCode == Incompliance.index_incomp_src_eiatemplate) {/* EIA template header table*/
            	redirectToEIATemplateTable(fileName, rowNumber, columnNumber);
                return;
            }
            
            if (typeCode == Incompliance.index_incomp_src_esatemplate) {/* ESA template header table*/
                redirectToESATemplateTable(fileName, rowNumber, columnNumber);
                return;
            }         
        }
        
        /**
         * Redirects the view to EIA table
         * @param rr the row index
         * @param cc the column index
         */
        public void redirectToEIATable(int rr, int cc) {
            MainWindow.getTabPane().setSelectedIndex(0);
            EIATable eiaTable = MainWindow.getIniEiaTable();
            if (eiaTable.isTableColumnHidden())
                cc = cc - 1; //left shift for file name field
            highlightCell(eiaTable, rr, cc);
        }
        
        /**
         * Redirects the view to ESA table
         * @param fileName the file containing the ESA table
         * @param rr row index
         * @param cc column index
         */
        public void redirectToESATable(String fileName, int rr, int cc) {        	
//        	System.out.println("[" + rr + "," + cc + "]" + fileName);
        	String srcFileName = fileName;
    		String wrkFileName = "";
    		for (int i = 0; i < MainWindow.getSrcEdfFiles().size(); i++) {
    			File f = MainWindow.getSrcEdfFiles().get(i);
    			if (f.getAbsolutePath().equals(srcFileName)){
    				wrkFileName = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
    			}
    		}    		
    		
            ArrayList<ESATable> tables = MainWindow.getIniEsaTables();
            int index = 0;
            for (; index < tables.size(); index++)
                if (tables.get(index).getMasterFile().getAbsolutePath().equalsIgnoreCase(wrkFileName))
                    break;
            ESATable esaTable = tables.get(index);
            WorkingTablePane pane = new WorkingTablePane(esaTable);
            MainWindow.getTabPane().remove(1);
            MainWindow.getTabPane().insertTab("Signal Header", null, pane, null, 1);
            MainWindow.getTabPane().setSelectedIndex(1);
            highlightCell(esaTable, rr, cc);
        }
        
        /**
         * Redirects the view to EIA template table
         * @param fileName the file containing the EIA template table
         * @param rr row index
         * @param cc column index
         */
        public void redirectToEIATemplateTable(String fileName, int rr, int cc) {
            int tabs = MainWindow.getTabPane().getTabCount();
            EIATemplatePane etPane;
            Object object;
            JTable table;
            for (int i = 0; i < tabs; i++) {
                object = MainWindow.getTabPane().getComponentAt(i);
                if (object instanceof EIATemplatePane) {
                    etPane =  (EIATemplatePane) object;
                    if (etPane.getMasterFile().getAbsolutePath().equalsIgnoreCase(fileName)) {
                        MainWindow.getTabPane().setSelectedIndex(i);
                        table = etPane.getPreviewTable();
                        highlightCell(table, rr, cc);
                        break;
                    }
                }                
            }     
        }
        
        /**
         * Redirects the view to ESA template table
         * @param fileName the file containing the ESA template table
         * @param rr row index
         * @param cc column index
         */
        public void redirectToESATemplateTable(String fileName, int rr, int cc) {
            int tabs = MainWindow.getTabPane().getTabCount();
            ESATemplatePane etPane;
            Object object;
            EDFTable table;
            for (int i = 0; i < tabs; i++) {
                object = MainWindow.getTabPane().getComponentAt(i);
                if (object instanceof ESATemplatePane){
                    etPane =  (ESATemplatePane) object;
                    if (etPane.getMasterFile().getAbsolutePath().equalsIgnoreCase(fileName)) {
                        table = etPane.getEsaTemplateTable();
                        MainWindow.getTabPane().setSelectedIndex(i);
                        highlightCell(table, rr, cc);
                        break;
                    }
                }                
            }            
        }
        
        /**
         * Makes the specific cell of the table to be viewable
         * @param jtable the table used
         * @param rr the row index
         * @param cc the column index
         */
        void highlightCell(JTable jtable, int rr, int cc) {
        	// row selection
            jtable.getSelectionModel().setSelectionInterval(rr, rr);
            // column selection
            jtable.getColumnModel().getSelectionModel().setSelectionInterval(cc, cc);
            // scroll to viewable
            Utility.scrollTableRowToVisible(jtable, rr, cc);           
        }
                
        /**
         * Given a selected row, retrieve the error information if possible
         * @param selRow the selected row number
         * @return true if the error information is retrieved, false otherwise
         */
        boolean retrieveLocation(int selRow) {
            fileName = (String)table.getModel().getValueAt(selRow, file_number);
            if (fileName == null)
                return false;
            
            fileName = fileName.trim();
            type = (String)table.getModel().getValueAt(selRow, type_number);
            type = type.trim();
            
            Object rn = table.getModel().getValueAt(selRow, row_number);
            rowNumber = Integer.parseInt(rn.toString().trim()) - 1; 
            //rowNumber = Integer.parseInt((String)table.getModel().getValueAt(selRow, row_number));
            Object cn = table.getModel().getValueAt(selRow, column_number);
            columnNumber = Integer.parseInt(cn.toString().trim()) - 1;
            
            return true;
        }

        /**
         * Retrieves the index of the error type, -1 if not found
         * @return the index of the error type according to {@link Incompliance#typeOfErrorHeader}
         */
        private int retrieveHeaderTableTypeCode() {
        	int count = Incompliance.typeOfErrorHeader.length;
        	for (int i = 0; i < count; i++)
        		if (type.equalsIgnoreCase(Incompliance.typeOfErrorHeader[i]))
        			return i;
            return -1;
        }
    } //end of class CellMouseClickedListener
    
    /**
     * A text icon
     */
    public static class TextIcon {
    	private String text;
        private Icon icon;
        TextIcon(String text, Icon icon) {
            this.text = text; 
            this.icon = icon;
        }
    }
    
    /**
     * A DefaultTableCellRenderer that sets the icon cell
     */
    public static class HeaderIconRenderer extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = 1L;

	    /**
	     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(
	     * javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	     */
	    public Component getTableCellRendererComponent (
	    		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Inherit the colors and font from the header component
//               if (table != null) {
//                    JTableHeader header = table.getTableHeader();
//                    if (header != null) {
//                        setForeground(header.getForeground());
//                        setBackground(header.getBackground());
//                        setFont(header.getFont());
//                    }
//                }

        	if (value instanceof TextIcon) {
                TextIcon txtIcon = (TextIcon)value;
                setIcon(txtIcon.icon);
                setText(txtIcon.text);
            } else {
                setText((value == null) ? "" : value.toString());
                setIcon(null);
            }
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }
}
