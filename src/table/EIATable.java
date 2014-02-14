package table;

import header.EDFFileHeader;
import header.EIA;
import header.EIAHeader;

import java.util.ArrayList;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public class EIATable extends EDFTable{    
    
    public static final int number_local_patientID = 2;
    public static final int number_local_recordingID = 3;
    public static final int number_start_date_recording = 4;
    private TableColumn[] allTableColumns;
    protected int[] immutableFieldIndices = {/*EIA.index_filename,*/ EIA.index_version, EIA.index_start_time, EIA.index_of_bytes, EIA.index_reserved, 
                           EIA.index_number_of_datarecord, EIA.index_duration, EIA.index_number_of_channels};
    private boolean forEiaTemplate;
                            
    public EIATable(Boolean forEIATemplate) {
        //super(forEIATemplate);
        super(new EIATableModel(1));        
        setForEiaTemplate(forEIATemplate);
        //setImmutableFieldIncides();
        customizeLook("eia");        
        //for template table, hide one more column of file name
        if (forEIATemplate){
            immutableFieldIndices = new int[]{EIA.index_filename, EIA.index_version, EIA.index_start_time, EIA.index_of_bytes, EIA.index_reserved, 
                            EIA.index_number_of_datarecord, EIA.index_duration, EIA.index_number_of_channels};
            hideImmutableFields();
        } 
    } 
    
    
    public EIATable(){ 
        super(new EIATableModel(1)); 
        customizeLook("eia");   
    }
    
    public EIATable(EDFFileHeader edfHeader){
        super(new EIATableModel(1));        
        
        customizeLook("eia");
        String key, value;
        EIAHeader eiaHeader;
        for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++){
            key = EIA.getEIAAttributeAt(ncolumn);
            eiaHeader = edfHeader.getEiaHeader();
            value = eiaHeader.getAttributeValueAt(key);
            this.getModel().setValueAt(value, 0, ncolumn);
        } 
        //cacheColumns();
        //setImmutableFieldIncides();
    }

    private void cacheColumns(){
        int ncol = getModel().getColumnCount();
         allTableColumns = new TableColumn[ncol];
         for (int i = 0; i < ncol; i++)
             allTableColumns[i] =  getColumnModel().getColumn(i);
     }
    
    //Fangping, 10/04/2010
    public void showImmutableFields(){
        for (int index: immutableFieldIndices){
            addColumn(allTableColumns[index]);
            getColumnModel().moveColumn(getColumnCount() - 1, index);
        }        
        validate();
    }
    
    public void hideImmutableFields(){
        cacheColumns();
        int k = 0;
        for (int i : immutableFieldIndices) {
            TableColumn column = getColumnModel().getColumn(i - k);
            getColumnModel().removeColumn(column);
            k++;
        }
        validate();
    }
    
    /**
     * @param edfHeaders
     * @param numberOfHeaders
     */
    public EIATable(ArrayList<EDFFileHeader> edfHeaders, int numberOfHeaders){
        super(new EIATableModel(numberOfHeaders));
        customizeLook("eia");
       
        for (int nrow = 0; nrow < numberOfHeaders; nrow++)   
            for (int ncolumn = 0; ncolumn < EIA.NUMBER_OF_ATTRIBUTES + 1; ncolumn++){
                String key = EIA.getEIAAttributeAt(ncolumn);
                EIAHeader eiaHeader = edfHeaders.get(nrow).getEiaHeader();
                String value = eiaHeader.getAttributeValueAt(key);
                this.getModel().setValueAt(value, nrow, ncolumn);
            }          
     }
    
    
    public void updateTableRow(EIAHeader header, int rowIndex){
        TableModel model = this.getModel();
        String value;
        for (int i = 0; i < EIA.NUMBER_OF_ATTRIBUTES; i++) {
            value = header.getAttributeValueAt(EIA.getEIAAttributeAt(i + 1));
            model.setValueAt(value, rowIndex, i + 1);
        }
        this.setUpdateSinceLastSave(true);
    }
    
    public void setForEiaTemplate(boolean forEiaTemplate) {
        this.forEiaTemplate = forEiaTemplate;
    }
        
    public boolean isForEiaTemplate() {
        return forEiaTemplate;
    }
}

