/**
 * signal header consists of multiple signal channels.
*/

package header;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.HashMap;

import javax.swing.JTable;

public class ESAHeader extends ESAChannel {

    private ESAChannel[] signalHeader = null; // all attribute values of the channel
    private ESATemplateChannel[] signalTemplateHeader = null;
    private int numberOfChannels = 0; // the number of channels
    private File hostEdfFile = null; // the host file of the header

    //////////////////////////////////////////////////////////////////////////////
    ////////////// START of constructor zone /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////    
    
    /**
     * to be implemented
     */
    public ESAHeader() {
        //TODO: may not code for this. go and see;
    }
    
    /**
     * Construct an ESA header from a file (in form of RandomAccessFile).
     * Algorithm:
     * 1. set the number of channels;
     * 2. set the host file of the header
     * 3. attach channels to the header
     * @param raf Random file accessor  
     * @param edfFile host EDF file of the header
     * @param nChannels the number of Channels
     */
    public ESAHeader(RandomAccessFile raf, File edfFile, int nChannels, boolean istemplate) {   
    	if(!istemplate) {
    		setNumberOfChannels(nChannels); // end of 1.
    	    setHostEdfFile(edfFile); // end of 2.
    	    if (nChannels == 0) {
    	        signalHeader = null;
    	        return;
    	    }
    	    signalHeader = new ESAChannel[nChannels]; // start of 3.
    	    for (int i = 0; i < nChannels; i++) {                    
    	        signalHeader[i] = new ESAChannel(raf, i, nChannels);          
    	    } // end of 3.
    	} else {
    		setNumberOfChannels(nChannels); // end of 1.
    	    setHostEdfFile(edfFile); // end of 2.
    	    if (nChannels == 0) {
    	        signalTemplateHeader = null;
    	        return;
    	    }
    	    signalTemplateHeader = new ESATemplateChannel[nChannels]; // start of 3.
    	    for (int i = 0; i < nChannels; i++) {                    
    	        signalTemplateHeader[i] = new ESATemplateChannel(raf, i, nChannels);          
    	    } // end of 3.
    	}                    
    }

    /**
     * Construct an ESA header from an ESA table
     * Algorithm:
     * 1. calculate the number of rows and assign it to the numberOfChannels
     * 2. set host file (!! this depends on the table has been saved or not)
     * 3. turn each row of the table into a channel which is attached the header
     * @param table  ESA table
     */
    public ESAHeader(JTable table, boolean template) { 
    	if(!template) {
            int nRows = table.getRowCount();
            int nColumns = table.getColumnCount();
            int nChannels = nRows; // number of channels, end of 1.
            
            signalHeader = new ESAChannel[nChannels];
            for (int i = 0; i < nChannels; i++) {
                signalHeader[i] = new ESAChannel();
            }
                 
            String cellValue;
            for (int row = 0; row < nChannels; row++) {
                HashMap<String,Object> tempHeader = new HashMap<String,Object>(ESA.NUMBER_OF_ATTRIBUTES);
                for (int col = 0; col < nColumns; col++) {
                    String attributeName = ESA.getESAAttributeAt(col);
                    cellValue = (String) table.getModel().getValueAt(row, col); 
                    if (cellValue == null)
                        cellValue = "";
                    else
                        cellValue = cellValue.trim();
                    tempHeader.put(attributeName, cellValue);
                }
                signalHeader[row] = new ESAChannel(tempHeader);
                //signalHeader[row].setEsaChannel(tempHeader);
            }            
            this.setNumberOfChannels(nChannels);
    	} else {
            int nRows = table.getRowCount();
            int nColumns = table.getColumnCount();
            int nChannels = nRows; // number of channels
            
            signalTemplateHeader = new ESATemplateChannel[nChannels];
            for (int i = 0; i < nChannels; i++) {
                signalTemplateHeader[i] = new ESATemplateChannel();
            }
                 
            String cellValue;
            for (int row = 0; row < nChannels; row++) {
                HashMap<String,Object> tempHeader = new HashMap<String,Object>(ESA.NUMBER_OF_ATTRIBUTES);
                for (int col = 0; col < nColumns; col++) {
                    String attributeName = ESA.getESATemplateAttributeAt(col);
                    cellValue = (String) table.getModel().getValueAt(row, col); 
                    if (cellValue == null)
                        cellValue = "";
                    else
                        cellValue = cellValue.trim();
                    tempHeader.put(attributeName, cellValue);
                }
                signalTemplateHeader[row] = new ESATemplateChannel(tempHeader);
                //signalHeader[row].setEsaChannel(tempHeader);
            }            
            this.setNumberOfChannels(nChannels);
    	}   
    }   
    
    /**
     * Construct an empty table for, for example, ESA template creating
     * Note: seems to be obsolete. Not removed before finalized
     * @param nChannels the number of channels of an EDF file
     */
    public ESAHeader(int nChannels) {
        signalHeader = new ESAChannel[nChannels];
        signalTemplateHeader = new ESATemplateChannel[nChannels];
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////// END of constructor zone ///////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Save ESA header part to disk
     * Algorithm: 
     * 1. get the number of channels;
     * 2. create an EIA Header only nChannels valid. only for ESA template header
     * 3. designate the saving tasks to every channel.
     * Note: index is required for saving designation.
     * @param raf random file accessor
     * @param file the file to store the header. the file must conform to raf
     * @param alreadyHasEIAHeader true for EDF file, false for ESA template header
     * @throws IOException
     */
    public void saveToDisk(
    		RandomAccessFile raf, File file, boolean alreadyHasEIAHeader, boolean template) throws IOException {
        if(!template) {
        	int nChannels = this.getNumberOfChannels(); // end of 1.
            
            if (!alreadyHasEIAHeader)  // end of 2.
                writeNumberOfChannelsToHeader(raf, file, nChannels);
            for (int index = 0; index < nChannels; index++) { // start of 3.
                ESAChannel currentChannel = this.getSignalChannelAt(index);
                currentChannel.writeESAChannelToDisk(raf, index, nChannels);
            }//end of 3.
        } else {
            int nChannels = this.getNumberOfChannels(); // 1.
            if (!alreadyHasEIAHeader)  // 2.
                writeNumberOfChannelsToHeader(raf, file, nChannels);
            
            for (int index = 0; index < nChannels; index++) { // start of 3.
                ESATemplateChannel currentChannel = this.getSignalTemplateChannelAt(index);
                currentChannel.writeESATemplateChannelToDisk(raf, index, nChannels);
            }//end of 3.
        }
    	    
        raf.close();
        this.setHostEdfFile(file);
    }

    /**
     * Write the number of channels to EIA header
     * @param raf file to save the EIA information to
     * @param file host file of the header
     * @param nChannel the number of channels of this file
     */
    private void writeNumberOfChannelsToHeader(RandomAccessFile raf, File file, int nChannel) {
        EIAHeader eiaHeader = new EIAHeader();
        
        eiaHeader.getEIAHeader().put(EIA.NUMBER_OF_SIGNALS, "" + nChannel);
        eiaHeader.saveToDisk(raf, file);
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////// START of getter and setter zone //////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Set the channel number
     * @param nChannels number of Channels
     * @literal numberOfChannels setter
     */
    public void setNumberOfChannels(int nChannels) {
        this.numberOfChannels = nChannels;
    }

    /**
     * Get the channel number
     * @return number of Channels
     * @literal numberOfChannels getter
     */
    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    /**
     * Get the ESA channel at specified position
     * @param index the index of channel to be acquired
     * @return the ESA channel with indexed channel
     */
    public ESAChannel getEsaChannelAt(int index) {
        return signalHeader[index];
    }
    
    /**
     * Get ESA template channel at specified position
     * @param index the specified position
     * @return ESA template channel  
     */
    public ESATemplateChannel getEsaTemplateChannelAt(int index) {
        return signalTemplateHeader[index];
    }

    /**
     * Set the host file
     * @param hostEdfFile the host file of this header
     */
    public void setHostEdfFile(File hostEdfFile) {
        this.hostEdfFile = hostEdfFile;
    }

    /**
     * Get the host file of this header
     * @return a File represent the host EDF file
     */
    public File getHostEdfFile() {
        return hostEdfFile;
    }
    
    /**
     * Set the signal header from an array of ESA channel
     * @param signalHeader an array of ESA channel
     */
    public void setSignalHeader(ESAChannel[] signalHeader) {
        this.signalHeader = signalHeader;
    }
    
    /**
     * Set the signal channel at specified position
     * @param index the position to set 
     * @param channel the channel to set at the specified position
     */
    public void setSignalChannel(int index, ESAChannel channel) {
        this.signalHeader[index] = channel;
    }

    /**
     * Get Signal header
     * @return signal header
     */
    public ESAChannel[] getSignalHeader() {
        return signalHeader;
    }
    
    /**
     * Get ESA template channel
     * @return ESA template channel
     */
    public ESATemplateChannel[] getSignalTemplateHeader() {
        return signalTemplateHeader;
    }
    
    /**
     * Get ESA channel at specified position
     * @param index the specified position
     * @return the ESA channel at index 
     */
    public ESAChannel getSignalChannelAt(int index) {
        return signalHeader[index];
    }
    
    /**
     * Get the ESA template channel at the specified position
     * @param index the specified position
     * @return the ESA template channel 
     */
    public ESATemplateChannel getSignalTemplateChannelAt(int index) {
        return signalTemplateHeader[index];
    }
    
    /**
     * Get the attribute of a specified channel
     * @param indexOfChannel the channel position
     * @param indexOfAttribute the attribute position of a channel
     * @return the String value of an attribute
     */
    public String getValueAt(int indexOfChannel, int indexOfAttribute) {
        ESAChannel channel = this.getEsaChannelAt(indexOfChannel);
        String value = (String) channel.getSignalAttributeValueAt(indexOfAttribute);
        return value;
    }

    
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////// END of getter and setter zone ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Print the ESA header information to screen
     */
    public void printEsaHeaderToScreen() {
        int nChannels = this.getNumberOfChannels();
        for (int i = 0; i < nChannels; i++) {
            ESAChannel channel = this.getSignalChannelAt(i);
            for (int j = 0; j < ESA.NUMBER_OF_ATTRIBUTES; j++) {
                String value = (String) channel.getSignalAttributeValueAt(ESA.getESAAttributeAt(j));
                System.out.print(value + "\t");                
            }
            System.out.print("\n");
        }
    }
}
