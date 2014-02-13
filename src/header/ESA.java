package header;
/*
 * ESA is to record attributes of a single channel contained in an EDF file.
 * ESAHeader, in comparison, records attributes of all channels, one by one, contained in an EDF file
 */

public class ESA {
    // a list of ESA attribute names
    public static final String LABEL = "label";
    public static final String TRANCEDUCER_TYPE = "transducer type";
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

    public static final int NUMBER_OF_ATTRIBUTES = 10;// the number of ESA attribtues
    
    public static final int EIA_OFFSET = 256; // the offset of EIA header
    
    // ESA attribute names in manner of array
    protected final static String[] esaAttributes =
    { "label", "transducer type", "physical dimension", "physical minimum",
      "physical maximum", "digital minimum", "digital maximum",
      "prefiltering", "number of samples", "reserved" };
    
    //ESA attribute names + corrected label field
    protected final static String[] esaTemplateAttributes =
    { "label", "corrected label", "transducer type", "physical dimension", "physical minimum",
        "physical maximum", "digital minimum", "digital maximum",
        "prefiltering", "number of samples", "reserved" };
    
    // array of space size to stored in file for ESA attributes
    protected static final int[] byteLength = { 16, 80, 8, 8, 8, 8, 8, 80, 8, 32 };
    
 // array of space size to stored in file for ESA Template attributes
    protected static final int[] tByteLength = { 16, 16, 80, 8, 8, 8, 8, 8, 80, 8, 32 };

    /**
     * @return the set of ESA attributes
     */
    public static String[] getESAAttributes() {
        return esaAttributes;
    }
    
    public static String[] getESATemplateAttributes() {
        return esaTemplateAttributes;
    }
    
    public static String getESAAttributeAt(int index){
        return esaAttributes[index];
    }
    
    public static String getESATemplateAttributeAt(int index){
        return esaTemplateAttributes[index];
    }
    
    public static int getByteLengthAt(int index){
        return byteLength[index];
    }
    
    public static int getTByteLengthAt(int index){
        return tByteLength[index];
    }

    /**
     * @param myString string to be regularied
     * @param length the expected length
     * @return the regularized string with length max(myString.length(), length)
     * Note: the returned value may have better length than expected when <BR>
     * myString.length() > length. This is definitely a shortback
     * Note: this method is the same as the one in the EIA class.
     */
    public String regularizeKey(String myString, int length) {
        String format = "%1$-" + length + "s";
        return String.format(format, myString);
    }

}
