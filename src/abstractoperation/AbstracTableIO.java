/**
 * So far, this interface is not implemented by any class!! (02-02-2010, Fangping)
 */

package abstractoperation;

import header.EIAHeader;
import header.ESAHeader;

import java.io.File;


/*
 * this interface is to be implemented in the MainWindow class.
 * Write methods are for save functionalities.
 * read methods are for select files and import functionalities.
 * only support io on EDF/EIA/ESA, not on xml and so on.
 */
public interface AbstracTableIO {
    /**
     * @param eiaHeader EIA header retrieved from EIA table model
     * @param esaHeader ESA header retrieved from ESA table model
     * @param file the target to be written.
     * used to save both EIA and ESA headers back to file after teh file body has been saved.
     */
    void write(EIAHeader eiaHeader, ESAHeader esaHeader, File file);


    /**
     * @param eiaHeader EIA header retrived from EIA table model
     * @param file the target file to be written. 
     * used to save the EIA header after the file body has been saved
     */
    void write(EIAHeader eiaHeader, File file);


    /**
     * @param esaHeader ESA header retrieved from ESA table model
     * @param file the target file to be written. 
     * used to save the ESA header after the file body has been saved
     */
    void write(ESAHeader esaHeader, File file);

    /**
     * @param eiaHeader eiaHeader EIA header retrieved from the file
     * @param esaHeader ESA header retrieved from EDF file
     * @param file the source file to be read
     */
    void read(EIAHeader eiaHeader, ESAHeader esaHeader, File file);


    /**
     * @param eiaHeader EIA header retrieved from the file
     * @param file the source file to be read
     */
    void read(EIAHeader eiaHeader, File file);

    /**
     * @param esaHeader
     * @param file the source file to be read
     */
    void read(ESAHeader esaHeader, File file);    
}
