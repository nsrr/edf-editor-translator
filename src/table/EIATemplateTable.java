package table;

import header.EIA;
import header.EIAHeader;

import java.io.File;

import javax.swing.table.TableModel;

public class EIATemplateTable extends EIATable {
    public EIATemplateTable() {
        super(true);
    }
    
    public EIATemplateTable(EIAHeader eiaHeader){
        super();
        setForEiaTemplate(true);
        immutableFieldIndices = new int[]{EIA.index_filename, EIA.index_version, EIA.index_start_time, EIA.index_of_bytes, EIA.index_reserved, 
                        EIA.index_number_of_datarecord, EIA.index_duration, EIA.index_number_of_channels};
        hideImmutableFields();
        TableModel model = this.getModel(); //new EIATableModel(1);     
        String key, value;
        
        for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++){
            key = EIA.getEIAAttributeAt(ncolumn);
            value = eiaHeader.getAttributeValueAt(key);
            model.setValueAt(value, 0, ncolumn);
        } 
          
/*         for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++){
            System.out.println("readout: " + getModel().getValueAt(0, ncolumn));
        }  */

        //hideImmutableColumns();   

    }
    
    public File getMasterFile(){
        return masterFile;
    }
}
