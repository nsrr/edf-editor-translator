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
     * @param raf Random file accessor  
     * @param edfFile host EDF file of the header
     * @param nChannels the number of Channels
     * construct an ESA header from a file (in form of RandomAccessFile).
     * Algorithm:
     * 1. set the number of channels;
     * 2. set the host file of the header
     * 3. attach channels to the header
     */
    public ESAHeader(RandomAccessFile raf, File edfFile, int nChannels, boolean istemplate) {   
        
    	if(!istemplate) {
    		setNumberOfChannels(nChannels); // 1.
            
    	        setHostEdfFile(edfFile); //2.
    	        
    	        if (nChannels == 0){
    	            signalHeader = null;
    	            return;
    	        }
    	                
    	        signalHeader = new ESAChannel[nChannels]; // start of 3.
    	        for (int i = 0; i < nChannels; i++){                    
    	            signalHeader[i] = new ESAChannel(raf, i, nChannels);          
    	        } // end of 3.
    	} else {
    		setNumberOfChannels(nChannels); // 1.
            
    	    setHostEdfFile(edfFile); //2.
    	        
    	    if (nChannels == 0){
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
     * @param table  ESA table
     * construct an ESA header from an ESA table
     * Algorithm:
     * 1. calculate the number of rows and assign it to the numberOfChannels
     * 2. set host file (!! this depends on the table has been saved or not)
     * 3. turn each row of the table into a channel whcih is attached the header
     */
    public ESAHeader(JTable table, boolean template) { 
    	if(!template) {
            int nRows = table.getRowCount();
            int nColumns = table.getColumnCount();
            int nChannels = nRows; // number of channels
            
            signalHeader = new ESAChannel[nChannels];
            for (int i = 0; i < nChannels; i++) {
                signalHeader[i] = new ESAChannel();
            }
                 
            String cellValue;
            for (int row = 0; row < nChannels; row++) {
                HashMap tempHeader = new HashMap(ESA.NUMBER_OF_ATTRIBUTES);
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
                HashMap tempHeader = new HashMap(ESA.NUMBER_OF_ATTRIBUTES);
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
     * @param nChannels the number of channels of an EDF file
     * Construct an empty table for, for example, ESA template creating
     * Note: seems to be obsolete. Not removed before finalized
     */
    public ESAHeader(int nChannels) {
        signalHeader = new ESAChannel[nChannels];
        signalTemplateHeader = new ESATemplateChannel[nChannels];
    }

//////////////////////////////////////////////////////////////////////////////
////////////// END of constructor zone ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////+


    /**
     * save ESA header part to disk
     * @param raf random file accessor
     * @param file the file to store the header. the file must conform to raf
     * @param alreadyHasEIAHeader true for EDF file, false for ESA template header
     * Algorithm: 
     * 1. get the number of channels;
     * 2. create an EIA Header only nChannels valid. only for ESA template header
     * 3. designate the saving tasks to every channel.
     * Note: index is required for saving designation.
     */
    public void saveToDisk(
    		RandomAccessFile raf, File file, boolean alreadyHasEIAHeader, boolean template) throws IOException {
        if(!template) {
        	int nChannels = this.getNumberOfChannels(); // 1.
            
            if (!alreadyHasEIAHeader)  // 2.
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
    
    private void writeNumberOfChannelsToHeader(RandomAccessFile raf, File file, int nChannel) {
        EIAHeader eiaHeader = new EIAHeader();
        
        eiaHeader.getEIAHeader().put(EIA.NUMBER_OF_SIGNALS, "" + nChannel);
        eiaHeader.saveToDisk(raf, file);
    }
    
////////////////////////////////////////////////////////////////////////////////
///////////////////// START of getter and setter zone //////////////////////////
///////////////////////////////////////////////////////////////////////////////

    /**
     * @param nChannels number of Channels
     * @literal numberOfChannels setter
     */
    public void setNumberOfChannels(int nChannels) {
        this.numberOfChannels = nChannels;
    }

    /**
     * @return numbe of Channels
     * @literal numberOfChannels getter
     */
    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    /**
     * @param index the index of channle to be acquired
     * @return the ESA channel with indexed channel
     */
    public ESAChannel getEsaChannelAt(int index) {
        return signalHeader[index];
    }
    
    public ESATemplateChannel getEsaTemplateChannelAt(int index) {
        return signalTemplateHeader[index];
    }

    public void setHostEdfFile(File hostEdfFile) {
        this.hostEdfFile = hostEdfFile;
    }

    public File getHostEdfFile() {
        return hostEdfFile;
    }
    
    public void setSignalHeader(ESAChannel[] signalHeader) {
        this.signalHeader = signalHeader;
    }
    
    public void setSignalChannel(int index, ESAChannel channel) {
        this.signalHeader[index] = channel;
    }

    public ESAChannel[] getSignalHeader() {
        return signalHeader;
    }
    
    public ESATemplateChannel[] getSignalTemplateHeader() {
        return signalTemplateHeader;
    }
    
    public ESAChannel getSignalChannelAt(int index) {
        return signalHeader[index];
    }
    
    public ESATemplateChannel getSignalTemplateChannelAt(int index) {
        return signalTemplateHeader[index];
    }
    
    public String getValueAt(int indexOfChannel, int indexOfAttribute) {
        ESAChannel channel = this.getEsaChannelAt(indexOfChannel);
        String value = (String) channel.getSignalAttributeValueAt(indexOfAttribute);
        return value;
    }

    
////////////////////////////////////////////////////////////////////////////////
////////////////////// END of getter and setter zone //////////////////////////
///////////////////////////////////////////////////////////////////////////////
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
