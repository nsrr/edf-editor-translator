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
    
    protected static final String error_eia_dmyint = "each segment of the starttime field should be number";
/*     protected static final String error_eia_day = "value for day segment should be numbers in [1, 31]";
    protected static final String error_eia_month = "value for month segment should be numbers in [1, 12]";
    protected static final String error_eia_year = "value for year segment should be numbers in [0, 99]"; */
    
    public static final String error_eia_ascii       = "EDF Header: Non-Ascii character in header";
    public static final String error_eia_empty       = "EDF Header: Cannot be empty field";
    public static final String error_eia_version     = "EDF Header: Version should be 0";
    public static final String error_eia_dateformat  = "EDF Header: Date should follow the format of dd.mm.yy";
    public static final String error_eia_daterange   = "EDF Header: Date is valid under the values:  dd:00-31, mm:00-12, yy:00-99";
    public static final String error_eia_timeformat  = "EDF Header: Time should follow the format of hh.mm.ss";
    public static final String error_eia_timerange   = "EDF Header: Time is valid under the values:  hh:00-23, mm:00-59, ss:00-59";
    public static final String error_eia_num_bytes   = "EDF Header: \"Number of bytes in header record\" should be a non-negative integer (>=0)";
    public static final String error_eia_num_records = "EDF Header: \"Number of data records\" should be a positive integer (>0, or -1 if unknown)";
    public static final String error_eia_duration    = "EDF Header: \"Duration of a data record\" is a positive floating point number (eg. 1, 0.2, 0.001)";
    public static final String error_eia_nsignals    = "EDF Header: \"Number of signals in data record\" is a positive integer (>0)";
    
    public static final String error_esa_ascii       = "Signal Header: Non-Ascii character in signal";
    public static final String error_esa_empty       = "Signal Header: Cannot be empty field";
    public static final String error_esa_label       = "Signal Header: Duplicated labels at row ";
    public static final String error_esa_phymin      = "Signal Header: Physical minimum should be a floating point number";
    public static final String error_esa_phymax      = "Signal Header: Physical maximum should be a floating point number";
    public static final String error_esa_phy_equal   = "Signal Header: Physical maximum cannot equal to physical minimum";
    public static final String error_esa_phymaxmin   = "Signal Header: Physical maximum must be larger than physical minimum";
    public static final String error_esa_digmin      = "Signal Header: Digital minimum should be an integer";
    public static final String error_esa_digmax      = "Signal Header: Digital maximum should be an integer";
    public static final String error_esa_dig_equal   = "Signal Header: Digital maximum cannot equal to digital minimum";
    public static final String error_esa_digrange    = "Signal Header: Digital integer should be in the range of [-32768, 32767]";
    public static final String error_esa_digmaxmin   = "Signal Header: Digital maximum must be larger than digital minimum";
    public static final String error_esa_nrSig       = "Signal Header: Number of signals should be an integer";
    public static final String error_esa_nrSig_range = "Signal Header: Number of signals should be greater than 0";
    
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
