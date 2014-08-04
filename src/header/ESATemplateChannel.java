package header;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * ESATemplateChannel is responsible of reading and writing signal template channel information from and to a file
 */
public class ESATemplateChannel extends ESA {
	
	 private HashMap<String,Object> esaChannel = null;
	 ///////////////////////////////////////////////////////////////////////////////
	 ///////////////// START of constructor zone ///////////////////////////////////
	 ///////////////////////////////////////////////////////////////////////////////
	 /**
	  * Construct the ESA template channel
	  */
	 public ESATemplateChannel() {
		 super();
	 }
	
	 /**
	  * Construct the ESA template channel using another channel
	  * @param channel the channel to use
	  */
	 public ESATemplateChannel(HashMap<String,Object> channel) {
		 esaChannel = channel;
	 }

	 /**
	  * Construct the ESA template channel from a file (represented by a RandomAccessFile)
	  * @param raf random file accessor
	  * @param channelNumber the serial number of current channel
	  * @param numberOfChannels the total number of channels in current EDF file
	  */
	 public ESATemplateChannel(RandomAccessFile raf, int channelNumber, int numberOfChannels) {
		 byte[] label = new byte[16];
		 byte[] correctedLabel = new byte[16];  // this field is different from ESAChannel
		 byte[] transducerType = new byte[80];
		 byte[] physDim = new byte[8];
		 byte[] physMin = new byte[8];
		 byte[] physMax = new byte[8];
		 byte[] digMin = new byte[8];
		 byte[] digMax = new byte[8];
		 byte[] prefiltering = new byte[80];
		 byte[] nbSamples = new byte[8];
		 byte[] reserved = new byte[32];
		
		 int offset = EIA_OFFSET; //skip the EIA part of 256 bytes large
		
		 esaChannel = new HashMap<String,Object>(NUMBER_OF_ATTRIBUTES+1);
		
		 /*
		  * the algorithm is:
		  * (1) first search for our signal position
		  * (2) then read it and transform it to String type
		  * (3) last, jump to the location of next attribute
		  */
		 try { // 1. label
			 raf.seek(offset + channelNumber * 16);
			 raf.readFully(label);
			 setAttributeValueAt(ESA.LABEL, new String(label).trim());
			 // System.out.println(esaChannel.get(ESA.LABEL)); //for test
			 offset += numberOfChannels * 16;
		
			 // 2. corrected label
			 raf.seek(offset + channelNumber * 16);
			 raf.readFully(correctedLabel);
			 setAttributeValueAt(ESA.CORRECTED_LABEL, new String(correctedLabel).trim());
			 // System.out.println(esaChannel.get(ESA.LABEL)); //for test
			 offset += numberOfChannels * 16;
		
			 // 3. transducer
			 raf.seek(offset + channelNumber * 80);
			 raf.readFully(transducerType);
			 setAttributeValueAt(ESA.TRANSDUCER_TYPE,
					 new String(transducerType).trim());
			 // System.out.println(esaChannel.get(ESA.TRANCEDUCER_TYPE));
			 offset += numberOfChannels * 80;
		
			 // 4. physical dimension
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(physDim);
			 setAttributeValueAt(ESA.PHYSICAL_DIMESNION,
					 new String(physDim).trim());
			 // System.out.println(esaChannel.get(ESA.PHYSICAL_DIMESNION));
			 offset += numberOfChannels * 8;
		
			 // physical minimum
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(physMin);
			 // System.out.println(new String(physMin).trim());
			 if (new String(physMin).trim() == null)
				 setAttributeValueAt(ESA.PHYSICAL_MINIMUM, "");
			 else
				 setAttributeValueAt(ESA.PHYSICAL_MINIMUM,
						 new String(physMin).trim());
			 offset += numberOfChannels * 8;
		
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(physMax);
			 if (new String(physMax).trim() == null)
				 setAttributeValueAt(ESA.PHYSICAL_MAXIMUM, "");
			 else
				 setAttributeValueAt(ESA.PHYSICAL_MAXIMUM,
						 new String(physMax).trim());
			 // System.out.println(esaChannel.get(ESA.PHYSICAL_MAXIMUM));
			 offset += numberOfChannels * 8;
		
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(digMin);
			 if (new String(digMin).trim() == null)
				 setAttributeValueAt(ESA.DIGITAL_MINIMUM, "");
			 else
				 setAttributeValueAt(ESA.DIGITAL_MINIMUM,
						 new String(digMin).trim());
			 // System.out.println(esaChannel.get(ESA.DIGITAL_MINIMUM));
			 offset += numberOfChannels * 8;
		
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(digMax);
			 if (new String(digMax).trim() == null)
				 setAttributeValueAt(ESA.DIGITAL_MAXIMUM, "");
			 else
				 setAttributeValueAt(ESA.DIGITAL_MAXIMUM,
						 new String(digMax).trim());
			 // System.out.println(esaChannel.get(ESA.DIGITAL_MAXIMUM));
			 offset += numberOfChannels * 8;
		
			 raf.seek(offset + channelNumber * 80);
			 raf.readFully(prefiltering);
			 setAttributeValueAt(ESA.PREFILTERING,
					 new String(prefiltering).trim());
			 // System.out.println(esaChannel.get(ESA.PREFILTERING));
			 offset += numberOfChannels * 80;
		
			 raf.seek(offset + channelNumber * 8);
			 raf.readFully(nbSamples);
			 if (new String(nbSamples).trim() == null)
				 setAttributeValueAt(ESA.NUMBER_OF_SAMPLES, "");
			 else
				 setAttributeValueAt(ESA.NUMBER_OF_SAMPLES,
						 new String(nbSamples).trim());
			 // System.out.println(new String(nbSamples).trim());
			 offset += numberOfChannels * 8;
		
			 raf.seek(offset + channelNumber * 32);
			 raf.readFully(reserved);
			 setAttributeValueAt(ESA.RESERVED, new String(reserved).trim());
			 // System.out.println(esaChannel.get(ESA.RESERVED) + " reserved!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	//////////////////////////////////////////////////////////////////////////////
	//////////////////////END of constructor zone ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////


//  public byte[] regularizeToBytes(HashMap currentChannel, int index) {
// 		String key = getESAattributes()[index];
//		String srcValue = (String) header.get(key); // 1.
//		int byteSize = byteLength[index]; // 2.
//		byte[] rgdValue = regularizeKey(srcValue, byteSize).getBytes(); // 3.
//		return rgdValue;
//	}


	/**
	 * Regularizes the attribute value to byte array
	 * @param channelHeader the attribute values of current channel
	 * @param index the index number of the selected attribute
	 * @return byte value of the selected attribute
	 */
	 public byte[] regularizeToBytes(HashMap<String,Object> channelHeader, int index) {
//		 Algorithm:
//		 1. single out the attribute value from the header;
//		 2. get the size of the attribute specified by EDF Standard;
//		 3. regularize the attribute value to bytes.
		 String key = getESATemplateAttributeAt(index);
		 String srcValue = (String) channelHeader.get(key); // 1.
		 int byteSize = getTByteLengthAt(index); // 2.
		 byte[] rgdValue = regularizeKey(srcValue, byteSize).getBytes(); // 3.
		
		 return rgdValue;
	 }
	
	 /**
	  * Writes current ESA channel to file in manner of random file accessor
	  * @param raf random file accessor
	  * @param indexOfChannel index of current channel
	  * @param numberOfChannels the number of channels
	  * @throws IOException IOException
	  */
	 public void writeESATemplateChannelToDisk(RandomAccessFile raf, int indexOfChannel, int numberOfChannels) throws IOException {
	
//	  	 Algorithm:
//	  	 1. regularize each attribute value of current channel to byte form;
//	  	 2. write each attribute in bytes to file
//	  	 Note: since ESA attribute values in file is non-linear, so step 1 and 2 are manually pieced together
		 HashMap<String,Object> header = getEsaChannel();
		
//		byte[][] byteValue = new byte[NUMBER_OF_ATTRIBUTES][90]; //this might be problematic
//		for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
//			byteValue[i] = regularizeToBytes(header, i); //1.
//		}
//		/* For each attribute, operations follow: 
//		 * 1. move the file pointer;
//		 * 2. write;
//		 * 3. update the offset.
//		 */
		
		 int offset = EIA_OFFSET;
		 raf.seek(offset + indexOfChannel * 16);
		 raf.write(regularizeToBytes(header, 0));
		 offset += numberOfChannels * 16;
		
		 raf.seek(offset + indexOfChannel * 16);
		 raf.write(regularizeToBytes(header, 1));
		 offset += numberOfChannels * 16;
		
		 raf.seek(offset + indexOfChannel * 80);
		 raf.write(regularizeToBytes(header, 2));
		 offset += numberOfChannels * 80;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 3));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 4));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 5));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 6));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 7));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 80);
		 raf.write(regularizeToBytes(header, 8));
		 offset += numberOfChannels * 80;
		
		 raf.seek(offset + indexOfChannel * 8);
		 raf.write(regularizeToBytes(header, 9));
		 offset += numberOfChannels * 8;
		
		 raf.seek(offset + indexOfChannel * 32);
		 raf.write(regularizeToBytes(header, 10));      
	 }
	
	///////////////////////////////////////////////////////////////////////////////
	///////////////////// START of setter and getter zone /////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	 /**
	  * Get the channel of this template
	  * @return the complete channel in form of HashMap
	  */
	 public HashMap<String,Object> getThisChannel() {
		 return esaChannel;
	 }
	
	 /**
	  * Get signal attribute value given the key
	  * @param key the attribute key for the ESA channel
	  * @return  the attribute value
	  */
	 public Object getSignalAttributeValueAt(String key) {
		 return esaChannel.get(key);
	 }
	
	 /**
	  * Get signal attribute value at the specified index
	  * @param index the index of the value in this channel
	  * @return the attribute value
	  */
	 public Object getSignalAttributeValueAt(int index) {
		 String key = ESA.getESAAttributeAt(index);
		 return esaChannel.get(key);
	 }
	
	 /**
	  * Set the attribute at the specified key
	  * @param key the attribute key for the ESA channel
	  * @param value the value for the attribute
	  */
	 public void setAttributeValueAt(String key, Object value) {
		 esaChannel.put(key, value);
	 }
	
	 /**
	  * Set ESA channel using parameter
	  * @param esaChannel the ESA channel represented as a hash map
	  */
	 public void setEsaChannel(HashMap<String,Object> esaChannel) {
		 this.esaChannel = esaChannel;
	 }
	
	 /**
	  * Return the current ESA channel
	  * @return the hash map representation of the current channel
	  */
	 public HashMap<String,Object> getEsaChannel() {
		 return esaChannel;
	 }
	
	 /**
	  * Create a new ESA channel
	  * @return a hash map representation of newly created channel
	  */
	 public HashMap<String,Object> initializeEsaChannel() {
		 return new HashMap<String,Object>();
	 }

	///////////////////////////////////////////////////////////////////////////////
	///////////////////// END of setter and getter zone ///////////////////////////
	///////////////////////////////////////////////////////////////////////////////
}
