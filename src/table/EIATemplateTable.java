package table;

import header.EIA;
import header.EIAHeader;

import java.io.File;

import javax.swing.table.TableModel;

/**
 * The EIA template table
 * Used for EIA template editing
 */
@SuppressWarnings("serial")
public class EIATemplateTable extends EIATable {
	/**
	 * Construct EIA template table
	 */
    public EIATemplateTable() {
        super(true);
    }

    /**
     * Construct EIA template table using an EIA header
     * @param eiaHeader the EIA header
     */
    public EIATemplateTable(EIAHeader eiaHeader) {
        super();
        setEiaTemplate(true);
        immutableFieldIndices = new int[]{
        		EIA.index_filename, EIA.index_version, 
        		EIA.index_start_time, EIA.index_of_bytes, EIA.index_reserved, 
                EIA.index_number_of_datarecord, EIA.index_duration, EIA.index_number_of_channels
        };
        hideImmutableFields();
        TableModel model = this.getModel(); //new EIATableModel(1);
        String key, value;

        for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++) {
            key = EIA.getEIAAttributeAt(ncolumn);
            value = eiaHeader.getAttributeValueAt(key);
            model.setValueAt(value, 0, ncolumn);
        }

//      for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++) {
//            System.out.println("readout: " + getModel().getValueAt(0, ncolumn));
//      }
//      hideImmutableColumns();
    }
    
    /**
     * Return the master file of this EIA template table
     */
    public File getMasterFile() {
        return masterFile;
    }
}
