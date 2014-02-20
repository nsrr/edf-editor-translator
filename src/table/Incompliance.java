package table;

import javax.swing.JTable;

public class Incompliance{
    //protected static int errorNumber;
    protected String fileName;
    protected String incomplianceType;
    protected String description;
    protected int rowIndex;
    protected int columnIndex;
    protected int srcTypeIndex;
    
    //the two types of incompliance: warning and critical error
    protected static final int errorLevel_warning = 0;
    protected static final int errorLevel_critical = 1; 
    
    //the description of warnings
    protected static final String warning_eia_day = "value for day segment should lie in [1, 31]";
    protected static final String warning_eia_month = "value for month segment should lie in [1, 12]";
    protected static final String warning_eia_year = "value for year segment should lie in [0, 99]";
    
    //the description about critical errors
    protected static final String error_eia_format = "Start date should be of format dd.mm.yy";
    protected static final String error_eia_dmyint = "each segment of the starttime field should be number";
/*     protected static final String error_eia_day = "value for day segment should be numbers in [1, 31]";
    protected static final String error_eia_month = "value for month segment should be numbers in [1, 12]";
    protected static final String error_eia_year = "value for year segment should be numbers in [0, 99]"; */
    
    protected static final String error_esa_ascii       = "Signal Header: Non Ascii character in signal";
    protected static final String error_esa_empty       = "Signal Header: Cannot be empty field";
    protected static final String error_esa_label       = "Signal Header: Duplicate labels at row ";
    protected static final String error_esa_phymin      = "Signal Header: Physical minimum is a floating point number";
    protected static final String error_esa_phymax      = "Signal Header: Physical maximum is a floating point number";
    protected static final String error_esa_phy_equal   = "Signal Header: Physical maximum cannot equal to physical minimum";
    protected static final String error_esa_digmin      = "Signal Header: Digital minimum is an integer";
    protected static final String error_esa_digmax      = "Signal Header: Digital maximum is an integer";
    protected static final String error_esa_dig_equal   = "Signal Header: Digital maximum cannot equal to digital minimum";
    protected static final String error_esa_digrange    = "Signal Header: Digital integer is out of range [-32768, 327670]";
    protected static final String error_esa_digmaxmin   = "Signal Header: Digital maximum must be larger than digital minimum";
    protected static final String error_esa_nrSig       = "Signal Header: Number of signals is an integer";
    protected static final String error_esa_nrSig_range = "Signal Header: Number of signals is greater than 0";
    
    public static final String Title_ErroIndex = "Error #"; //this might be redundant, Fangping, 09/29/2010
    public static final String Title_Description = "Description";
    public static final String Title_File = "File";
    public static final String Title_Row = "Row";
    public static final String Title_Column = "Column";
    public static final String Title_Type = "Type";
    
    //text and index the source of incompliances, 
    //corresponding to the four types of incompliance container defined in MainWindow class
    public static final String typeOfErrorHeader[] = {"ESA", "ESA template", "EIA",  "EIA template"};  
    public static final int index_incomp_src_esa = 0;
    public static final int index_incomp_src_esatemplate = 1;
    public static final int index_incomp_src_eia = 2;
    public static final int index_incomp_src_eiatemplate = 3;
    
    
    public Incompliance(String incomplianceType, String description, String fileName,  int rowIndex, int columnIndex, int srcTypeIndex) {
        this.incomplianceType = incomplianceType; 
        this.description = description;
        this.fileName = fileName;

        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.srcTypeIndex = srcTypeIndex;
    }
    
    public Incompliance(){
        ;
    }

/*     public static void setErrorNumber(int errorNumber) {
        Incompliance.errorNumber = errorNumber;
    }

    public static int getErrorNumber() {
        return errorNumber;
    } */

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setIncomplianceType(String incomplianceType) {
        this.incomplianceType = incomplianceType;
    }

    public String getIncomplianceType() {
        return incomplianceType;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setErrorSrcTypeIndex(int typeIndex) {
        this.srcTypeIndex = typeIndex;
    }

    public int getSrcTypeIndex() {
        return srcTypeIndex;
    }
}
