package table;

import header.ESA;

/**
 * An ESA template model
 */
@SuppressWarnings("serial")
public class ESATemplateTableModel extends ESATableModel {
	
    /**
     * Constructs ESATemplateTableModel using ESA template attributes and row number
     * @param rows number of rows of this model
     */
    public ESATemplateTableModel(int rows) {
        super(ESA.getESATemplateAttributes(), rows);
    }
    
    /**
     * Makes the number of signal column uneditable
     * Fangping, 08/05/2010
     * @see table.ESATableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex){
        case 9: 
            return false;
        default:            
            return true;
        }
    }
}
