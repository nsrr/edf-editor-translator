package header;

/**
 * ESA is to record attributes of a single channel contained in an EDF file.
 * ESAHeader, in comparison, records attributes of all channels, one by one, contained in an EDF file
 */
public class ESA {
    // a list of ESA attribute names
    public static final String LABEL = "label";
    public static final String TRANSDUCER_TYPE = "transducer type";
    public static final String PHYSICAL_DIMESNION = "physical dimension";
    public static final String PHYSICAL_MINIMUM = "physical minimum";
    public static final String PHYSICAL_MAXIMUM = "physical maximum";
    public static final String DIGITAL_MINIMUM = "digital minimum";
    public static final String DIGITAL_MAXIMUM = "digital maximum";
    public static final String PREFILTERING = "prefiltering";
    public static final String NUMBER_OF_SAMPLES = "number of samples";
    public static final String RESERVED = "reserved";
    
    //For template table
    public static final String CORRECTED_LABEL = "corrected label";
    public static final int NUMBER_OF_ATTRIBUTES = 10; // the number of ESA attributes
    public static final int EIA_OFFSET = 256; // the offset of EIA header
    
    // ESA attribute names in manner of array
    protected final static String[] esaAttributes = {
    	"label", "transducer type", "physical dimension", "physical minimum",
    	"physical maximum", "digital minimum", "digital maximum",
    	"prefiltering", "number of samples", "reserved" 
    };
    
    //ESA attribute names + corrected label field
    protected final static String[] esaTemplateAttributes = {
    	"label", "corrected label", "transducer type", "physical dimension", "physical minimum",
        "physical maximum", "digital minimum", "digital maximum",
        "prefiltering", "number of samples", "reserved" 
    };
    
    // array of space sizes to be stored in file for ESA attributes
    protected static final int[] byteLength = { 16, 80, 8, 8, 8, 8, 8, 80, 8, 32 };
    
    // array of space sizes to be stored in file for ESA Template attributes
    protected static final int[] tByteLength = { 16, 16, 80, 8, 8, 8, 8, 8, 80, 8, 32 };

    /**
     * Get the ESA attributes
     * @return the set of ESA attributes
     */
    public static String[] getESAAttributes() {
        return esaAttributes;
    }
    
    /**
     * Get the ESA template attributes
     * @return the set of ESA template attributes
     */
    public static String[] getESATemplateAttributes() {
        return esaTemplateAttributes;
    }
    
    /**
     * Get an ESA attribute at the specified index
     * @param index the position to return an ESA attribute
     * @return the value of an ESA attribute
     */
    public static String getESAAttributeAt(int index) {
        return esaAttributes[index];
    }
    
    /**
     * Get an ESA template attribute at the specified index
     * @param index the position to return an ESA template attribute
     * @return the value of an ESA template attribute
     */
    public static String getESATemplateAttributeAt(int index) {
        return esaTemplateAttributes[index];
    }
    
    /**
     * Get the byte length of an ESA attribute
     * @param index the position to return a byte length of an ESA attribute
     * @return the number of bytes an ESA attribute occupies
     */
    public static int getByteLengthAt(int index) {
        return byteLength[index];
    }
    
    /**
     * Get the byte length of an ESA template attribute
     * @param index the position to return a byte length of an ESA template attribute
     * @return the number of bytes an ESA template attribute occupies
     */
    public static int getTByteLengthAt(int index) {
        return tByteLength[index];
    }

    /**
     * Note: the returned value may have better length than expected when <BR>
     * myString.length() > length. This is definitely a shortback
     * Note: this method is the same as the one in the EIA class.
     * @param myString string to be regularied
     * @param length the expected length
     * @return the regularized string with length max(myString.length(), length)
     */
    public String regularizeKey(String myString, int length) {
        String format = "%1$-" + length + "s";
        return String.format(format, myString);
    }
}
