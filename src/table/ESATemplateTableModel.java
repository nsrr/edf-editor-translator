package table;

import header.ESA;

@SuppressWarnings("serial")
public class ESATemplateTableModel extends ESATableModel {
	
    public ESATemplateTableModel(int rows) {
        super(ESA.getESATemplateAttributes(), rows);
    }
    
    /**
     * make the number of signal column uneditable
     * Fangping, 08/05/2010
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
