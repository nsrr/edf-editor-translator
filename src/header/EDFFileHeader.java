package header;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * EDF file header, consists of EIAHeader and ESA header
 */
public class EDFFileHeader {

    private EIAHeader eiaHeader;
    private ESAHeader esaHeader;

    /**
     * Default constructor
     */
    public EDFFileHeader() {
        super();
    }

    /**
     * Header saving options.
     */
    // Consider using enum, wei wang, 2014-7-16
    protected static final int SAVE_EIA_ONLY = 0;
    protected static final int SAVE_ESA_ONLY = 1;
    protected static final int SAVE_EIA_ESA_BOTH = 2;

    /**
     * Constructs EDFFileHeader using RandomAccessFile, EDF file name and whether or not it is a template	 
     * @param raf the file to be read
     * @param edfFile the host EDF file
     * @param istemplate true if this is a template
     */
    public EDFFileHeader(RandomAccessFile raf, File edfFile, boolean istemplate) {
        try {
            eiaHeader = new EIAHeader(raf, edfFile);
            
            if (edfFile.getAbsolutePath().indexOf(".eia") == -1) {
                int numberOfChannels = 
                		Integer.parseInt(eiaHeader.getAttributeValueAt(EIA.NUMBER_OF_SIGNALS)); //2.
                esaHeader = new ESAHeader(raf, edfFile, numberOfChannels, istemplate); //3.
            }
            raf.close(); //4.
        } catch (IOException e) {
            e.printStackTrace();
        } //1.
    }

    /**
     * Saves both EIA and ESA header to disk. Assuming EIA, ESA header are not 
     * template and EIA header not existed before 
     * Used to save the EDFHeader, not including the signal data body.
     * @param raf the Random AccessFile for saving data in tables 
     * @param file the file to store the header. the file must conform to raf 
     * @param saveOption save according to save_options provided
     * @throws IOException IOException
     */
    public void saveToDisk(RandomAccessFile raf, File file, int saveOption) throws IOException {
//      Algorithm:
//      1. save EIA Header to disk
//      2. save ESA Headers to disk
//      3. close the file accessor
        switch (saveOption) {
        case SAVE_EIA_ONLY:
            this.eiaHeader.saveToDisk(raf, file); //1.
            break;
        case SAVE_ESA_ONLY: //
            this.esaHeader.saveToDisk(raf, file, false,
                                      false); //2. TODO: need attention
            break;
        case SAVE_EIA_ESA_BOTH:
            this.eiaHeader.saveToDisk(raf, file); // 1.
            this.esaHeader.saveToDisk(raf, file, true,
                                      false); // 2. TODO: need attention
            break;
        default:
            System.out.println("wrong specification of saving option");
            break;
        }

        raf.close(); // 3.
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////////////// START of getters and setters ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    
    /**
     * Sets EIAHeader using an EIHeader
     * @param eiaHeader the EIAHeader to set
     */
    public void setEiaHeader(EIAHeader eiaHeader) {
        this.eiaHeader = eiaHeader;
    }

    /**
     * Returns the EIAHeader
     * @return the EIAHeader of this EDFFileHeader
     */
    public EIAHeader getEiaHeader() {
        return eiaHeader;
    }

    /**
     * Sets ESAHeader using an ESAHeader
     * @param esaHeader the ESAHeader to set
     */
    public void setEsaHeader(ESAHeader esaHeader) {
        this.esaHeader = esaHeader;
    }


    /**
     * Returns the ESAHeader
     * @return the ESAHeader of this EDFFileHeader
     */
    public ESAHeader getEsaHeader() {
        return esaHeader;
    }

    ///////////////////////////////////////////////////////////////////////////////
    /////////////////////// END of getters and setters zone ///////////////////////
    ///////////////////////////////////////////////////////////////////////////////
}
