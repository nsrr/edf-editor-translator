package header;

public class EIA {

    public static final String FILE_NAME = "file name";
    public static final String VERSION = "version";
    public static final String LOCAL_PATIENT_ID = "local patient ID";
    public static final String LOCAL_RECORDING_ID = "local recording ID";
    public static final String START_DATE_RECORDING =
        "start date of recording";
    public static final String START_TIME_RECORDING =
        "start time of recording";
    public static final String NUMBER_OF_BYTES_IN_HEADER =
        "number of bytes in header";
    public static final String RESERVED = "reserved";
    public static final String NUMBER_OF_DATA_RECORDS =
        "number of data records";
    public static final String DURATION_OF_DATA_RECORD =
        "duration of data record";
    public static final String NUMBER_OF_SIGNALS = "number of signals";
    
    public static final int index_filename = 0;
    public static final int index_version = 1;
    public static final int index_patient_id = 2;
    public static final int index_recording_id = 3;
    public static final int index_start_date = 4;
    public static final int index_start_time = 5;
    public static final int index_of_bytes = 6;
    public static final int index_reserved = 7;
    public static final int index_number_of_datarecord = 8;
    public static final int index_duration = 9;
    public static final int index_number_of_channels = 10;
    
    public static final int NUMBER_OF_ATTRIBUTES = 10;
    public static final int BYTES_OF_EIA = 256;

    protected final static String[] eiaAttributes =
    { "file name", "version", "local patient ID", "local recording ID",
      "start date of recording", "start time of recording",
      "number of bytes in header", "reserved", "number of data records",
      "duration of data record", "number of signals" };
    
    public static final String key_blank = "{blank}";
    public static final String key_rand = "{rand}";
    public static final String key_skip = "{skip}";
    public static final String key_filename = "{filename}";
    public static final String key_pid = "{pid}";
    public static final String key_rid = "{rid}";
    public static final String key_yy = "{yy}";
    public static final String key_mm = "{mm}";
    public static final String key_dd = "{dd}";
    
    public static final String[] Keys = {key_blank, key_rand, key_skip, key_filename, key_pid,
                                                  key_rid, key_yy, key_mm, key_dd};
    


    protected static int[] byteLength = { 8, 80, 80, 8, 8, 8, 44, 8, 8, 4 };


///////////////////////////////////////////////////////////////////////////////
////////////////////START of getter and setter zone ///////////////////////////
 //////////////////////////////////////////////////////////////////////////////

    /**
     * @return all attributes in the order compliant with the EDF file
     * Usage: used to construct the table header.
     */
    public static String[] getEIAAttributes() {
        return eiaAttributes;
    }

    /**
     * @param index
     * @return the index-th EIA attribute
     */
    public static String getEIAAttributeAt(int index) {
        return eiaAttributes[index];
    }

    public static int[] getByteLength() {
        return byteLength;
    }

    public static int getByteLengthAt(int index) {
        return byteLength[index];
    }


///////////////////////////////////////////////////////////////////////////////
//////////////////// END of getter and setter zone ///////////////////////////
//////////////////////////////////////////////////////////////////////////////

    public String regularizeKey(String myString, int length) {
        String format = "%1$-" + length + "s";
        return String.format(format, myString);
    }

    public static String[] getKeys() {
        return Keys;
    }
}
