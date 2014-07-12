package header;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EDFFileHeader {

    private EIAHeader eiaHeader;
    private ESAHeader esaHeader;

    /**
     * TODO
     */
    public EDFFileHeader() {
        super();
    }

    /**
     * header saving options.
     */
    protected static final int SAVE_EIA_ONLY = 0;
    protected static final int SAVE_ESA_ONLY = 1;
    protected static final int SAVE_EIA_ESA_BOTH = 2;

    /**
     * TODO
     * @param raf
     * @param edfFile
     * @param istemplate
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } //1.
    }


    /**
     * TODO
     * @param raf the Random AccessFile for saving data in tables
     * Usage: used to save the EDFHeader, not including the signal  data body.
     * Algorithm:
     * 1. save EIA Header to disk
     * 2. save ESA Headers to disk
     * 3. close the file accessor
     */
    public void saveToDisk(RandomAccessFile raf, File file, int saveOption) throws IOException {
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
     * TODO
     * @param eiaHeader
     */
    public void setEiaHeader(EIAHeader eiaHeader) {
        this.eiaHeader = eiaHeader;
    }

    /**
     * TODO
     * @return
     */
    public EIAHeader getEiaHeader() {
        return eiaHeader;
    }

    /**
     * TODO
     * @param esaHeader
     */
    public void setEsaHeader(ESAHeader esaHeader) {
        this.esaHeader = esaHeader;
    }


    /**
     * TODO
     * @return
     */
    public ESAHeader getEsaHeader() {
        return esaHeader;
    }

    ///////////////////////////////////////////////////////////////////////////////
    /////////////////////// END of getters and setters zone ///////////////////////
    ///////////////////////////////////////////////////////////////////////////////
}
