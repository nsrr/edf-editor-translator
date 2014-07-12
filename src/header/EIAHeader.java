package header;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.HashMap;

import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import table.EIATable;
import table.EIATemplateTable;

public class EIAHeader extends EIA {

	// wei wang, 2014-6-18
	// Change HashMap to HashMap<String,String>
	
    private File hostEdfFile = null; // to register the host EDF file of current header
    private HashMap<String,Object> eiaHeader = new HashMap<String,Object>(); // store the eia header
    
    // what is this field used for? will conflict with EIA class.
    // conflict solved by change EIA field to aEIA, XML to aXML
    public static final int aEIA = 0;
    public static final int aXML = 1;
    
    /**
     * Default constructor
     */
    public EIAHeader() {
        eiaHeader = new HashMap<String,Object>(NUMBER_OF_ATTRIBUTES + 1); // plus file name
        eiaHeader.put(FILE_NAME, "");
        eiaHeader.put(VERSION, "");
        eiaHeader.put(LOCAL_PATIENT_ID, "");
        eiaHeader.put(LOCAL_RECORDING_ID, "");
        eiaHeader.put(START_DATE_RECORDING, "");
        eiaHeader.put(START_TIME_RECORDING, "");
        eiaHeader.put(NUMBER_OF_BYTES_IN_HEADER, "");
        eiaHeader.put(RESERVED, "");
        eiaHeader.put(NUMBER_OF_DATA_RECORDS, "");
        eiaHeader.put(DURATION_OF_DATA_RECORD, "");
        eiaHeader.put(NUMBER_OF_SIGNALS, "");
    }

    /**
     * Construct an EIA header from a file. With this constructor, the readFromDisk() is obsolete.
     * Algorithm:
     * 1. fill the value for "file name" key
     * 2. initialize byte pointer for read operation
     * 3. read attribute to pointer and put it to the header hash map structure;
     * 4. register host file of the header;
     * @param raf the file to be read
     * @param edfFile the host file
     * @throws IOException
     */
     public EIAHeader(RandomAccessFile raf, File edfFile) throws IOException {
         eiaHeader = new HashMap<String,Object>(); 
         
         String fullName = edfFile.getName();
         int extname_len = 4;
         eiaHeader.put(FILE_NAME, fullName.substring(0, fullName.length() - extname_len)); // end of 1.
 
         byte[] version = new byte[8];// start of 2.
         byte[] patientID = new byte[80];
         byte[] recordID = new byte[80];
         byte[] startDate = new byte[8];
         byte[] startTime = new byte[8];
         byte[] nbBytesHeader = new byte[8];
         // 44 bytes: reserved
         byte[] nbDataRecords = new byte[8];
         byte[] duration = new byte[8];
         byte[] nbSignals = new byte[4]; // end of 2.
         
         raf.seek(0); // start of 3.
         
         raf.readFully(version);
         eiaHeader.put(VERSION, new String(version).trim());

         raf.readFully(patientID);
         eiaHeader.put(LOCAL_PATIENT_ID, new String(patientID).trim());

         raf.readFully(recordID);
         eiaHeader.put(LOCAL_RECORDING_ID, new String(recordID).trim());

         raf.readFully(startDate);
         eiaHeader.put(START_DATE_RECORDING, new String(startDate).trim());

         raf.readFully(startTime);
         eiaHeader.put(START_TIME_RECORDING, new String(startTime).trim());

         raf.readFully(nbBytesHeader);
         eiaHeader.put(NUMBER_OF_BYTES_IN_HEADER,
                       new String(nbBytesHeader).trim());

         raf.skipBytes(44);
         eiaHeader.put(RESERVED, "");

         raf.readFully(nbDataRecords);
         eiaHeader.put(NUMBER_OF_DATA_RECORDS,
                       new String(nbDataRecords).trim());

         raf.readFully(duration);
         eiaHeader.put(DURATION_OF_DATA_RECORD, new String(duration).trim());

         raf.readFully(nbSignals);
         eiaHeader.put(NUMBER_OF_SIGNALS, new String(nbSignals).trim());// end of 3.
         
         setHostEdfFile(edfFile); // end of 4.
     }

    /**
     * Build EIA header from a row EIA table
     * Format is either XML or EIA
     * Algorithm: fill the member eiaHeader with the data in rowIndex row.
     * @param table EIATable used to extract attribute
     * @param rowIndex the row index to extract attribute
     */
    public EIAHeader(EIATable table, int rowIndex) {
        TableModel model = table.getModel(); // this is necessary for column hiding/showing 
        int nAttributes = model.getColumnCount();
        HashMap<String,Object> header = new HashMap<String,Object>();
        
        String attributeName;
        String cellValue;
        for (int i = 0; i < nAttributes; i++) {
        	// problem: EIA class conflict with EIAHeader.EIA attribute
        	// wei wang, 2014-6-18
//            attributeName = new EIA().getEIAAttributeAt(i);
        	attributeName = EIA.getEIAAttributeAt(i); // wei wang, 2014-6-19
            cellValue = (String) model.getValueAt(rowIndex, i);
            header.put(attributeName, cellValue);
        }
        this.setEiaHeader(header);
    }
    
    /**
     * Build EIA header from a EIATable
     * @param table the EIATable from which to build an EIA header
     */
    public EIAHeader(EIATable table) {
        TableModel model = table.getModel();
        int nAttributes = model.getColumnCount();
        HashMap<String,Object> header = new HashMap<String,Object>();
        
        String attributeName;
        String cellValue;
        for (int i = 0; i < nAttributes; i++) {
//            attributeName = new EIA().getEIAAttributeAt(i);
        	attributeName = EIA.getEIAAttributeAt(i);  // wei wang, 2014-6-19
            cellValue = (String) model.getValueAt(0, i);
            header.put(attributeName, cellValue);
        }
        this.setEiaHeader(header);
    }

    /**
     * Evaluate an ESA header directly from another header
     * @param header EIA header to be copied
     */
    public EIAHeader(HashMap<String,Object> header) {
        eiaHeader = header;
    }

//    /**
//     * @return the regularized header
//     * usage: use this method to regularize the eiaHeader, so that data can be 
//     * directly wrote back to disk.
//     */
//      public HashMap regularizeToBytes(HashMap header) {
//        HashMap regHeader = new HashMap(NUMBER_OF_ATTRIBUTES);
//        
//        String attributeValue;
//        for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {            
//            attributeValue = regularizeKey((String) header.get(keys[i]), byteLength[i]);
//            header.put(keys[i], attributeValue);
//        }
//        return regHeader;
//    }

    /**
     * regular an attribute value in the eia header to bytes 
     * Algorithm:
     * 1. fetch the attribute value from the header
     * 2. fetch the size of that attributes in the file header
     * 3. change the string value of the header to bytes
     * @param header the EIA header
     * @param index the index of the attribute
     * @return the bytes value of the attribute 
     */
    public byte[] regularizeToBytes(HashMap<String,Object> header, int index) { 
        String key = getEIAAttributeAt(index + 1); // note: the first attribute (FILE_NAME) is ignored
        String srcValue = (String) header.get(key); // end of 1.
        int byteSize = getByteLengthAt(index); // end of 2.
        byte[] rgdValue = regularizeKey(srcValue, byteSize).getBytes(); // end of 3.
        return rgdValue;
    }

    /**
     * Algorithm:
     * 1. regularize the string format of attribute values to bytes;
     * 2. write attribute byte[] value to the file one by one.
     * Note: this might be problematic
     * @param raf random file accessor
     * @throws IOException
     */
    public void writeEiaHeader(RandomAccessFile raf) throws IOException {
        HashMap<String,Object> header = this.getEIAHeader();
        raf.seek(0);
        for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
            raf.write(regularizeToBytes(header, i));
        }
    }
    
    /**
     * Save the current EIA header to file.
     * Algorithm:
     * 1. pull out the header data from EIA
     * 2. regularize the eiaHeader;
     * 3. write back to disk; 
     * 4. set the host file of EIA header;
     * Note: step 2 and 3 are implemented within the writeEiaHeader method
     * @param raf random file accessor to write data to file
     */
    public void saveToDisk(RandomAccessFile raf, File file) {
        try {
            writeEiaHeader(raf);  // 1, 2, 3.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        this.setHostEdfFile(file); // end of 4.
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    /////////////// START of setter and getter ///////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Set value per key
     * @param key the key of EIA header
     * @param value the value specified by the key
     */
    public void setValueAt(String key, Object value) {
        eiaHeader.put(key, (String)value); // wei wang, generic HashMap<String,String>
    }

    /**
     * Get current EIA header
     * @return current EIA header
     */
    public HashMap<String,Object> getEIAHeader() {
        return eiaHeader;
    }

    /**
     * Using external header to set this EIA header
     * @param eiaHeader current EIA header
     */
    public void setEiaHeader(HashMap<String,Object> eiaHeader) {
        this.eiaHeader = eiaHeader;
    }

    /**
     * Returns the value given the key
     * @param key key of the EIA header
     * @return string value of the attribute
     */
    public String getAttributeValueAt(String key) {
        return (String)eiaHeader.get(key);
    }

    /**
     * Set host EDF file
     * @param hostEdfFile host file of this EIA header
     */
    public void setHostEdfFile(File hostEdfFile) {
        this.hostEdfFile = hostEdfFile;
    }

    /**
     * Get host EDF file
     * @return File object of this host file
     */
    public File getHostEdfFile() {
        return hostEdfFile;
    }
    
    /**
     * Get EIA header from preview table 
     * @param pTable the preview table to extract EIA header information
     * @return the EIA header 
     */
    public static EIAHeader getEIAHeaderFromPreviewTable(EIATemplateTable pTable) {
        EIAHeader header = new EIAHeader();
        for (int i = EIATable.number_local_patientID; i <= EIATable.number_start_date_recording; i++) { 
            String value;
            if (pTable.getModel().getValueAt(0, i) == null)
                value = "";
            else
                value = (String) pTable.getModel().getValueAt(0, i);
            
            header.setValueAt(eiaAttributes[i], value.trim());           
        }
        return header;
    }

    public static final String root_label = "Header";
    public static final String eia_label = "EIA";
    public static final String pid_label = "PID";
    public static final String rid_label = "RID";
    public static final String date_label = "DATE";
    
    /**
     * Save file to XML format
     * @param filePath the output path
     * @return true if save operation is successful
     */
    public boolean saveToXml(String filePath) {
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(filePath));
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            
            String root = root_label;
            Element rootElement = document.createElement(root);
            rootElement.setAttribute("Type", "EIA Template");
            document.appendChild(rootElement);
            
            String subroot = eia_label;
            Element subrootElement = document.createElement(subroot);
            rootElement.appendChild(subrootElement);
            
            String element;
            String data;
            Element em;

            element = pid_label;
            data = getAttributeValueAt(LOCAL_PATIENT_ID);
            em = document.createElement(element);
            em.appendChild(document.createTextNode(data));
            subrootElement.appendChild(em);

            element = rid_label;
            data = getAttributeValueAt(LOCAL_RECORDING_ID);
            em = document.createElement(element);
            em.appendChild(document.createTextNode(data));
            subrootElement.appendChild(em);

            element = date_label;
            data = getAttributeValueAt(START_DATE_RECORDING);
            em = document.createElement(element);
            em.appendChild(document.createTextNode(data));
            subrootElement.appendChild(em);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Retrieve EIA header from XMl file
     * @param filePath path of the file to be parsed
     * @return an EIA header
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static EIAHeader retrieveEIAHeaderFromXml(String filePath) throws ParserConfigurationException,
                                                                             SAXException,
                                                                             IOException {        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        File file = new File(filePath);
        Document doc = builder.parse(file);
        Element root = doc.getDocumentElement();
        
        return yieldHeader(root);
    }

    /**
     * Yield EIA header from a given root of a document
     * @param root the root element of a document
     * @return an EIA header
     */
    private static EIAHeader yieldHeader(Element root) {
        EIAHeader header = new EIAHeader();
        
        NodeList list = root.getElementsByTagName(eia_label);
        Element el = null;
        if (list != null)
            el = (Element) list.item(0);
        
        String pidValue, ridValue, dateValue;
        pidValue = getTextValue(el, pid_label);
        ridValue = getTextValue(el, rid_label);
        dateValue = getTextValue(el, date_label);

        header.setValueAt(LOCAL_PATIENT_ID, pidValue);
        header.setValueAt(LOCAL_RECORDING_ID, ridValue);
        header.setValueAt(START_DATE_RECORDING, dateValue);
        
        return header;
    }
    
    /**
     * Get the text value of an element
     * @param ele the element used to extract text value
     * @param tagName the tag name of an element
     * @return the string value of an element specified by the tag name
     */
    private static String getTextValue(Element ele, String tagName) {
        String value = null;
        NodeList list = ele.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0){
            Element el = (Element) list.item(0);
            value = el.getFirstChild().getNodeValue();
        }        
        return value;
    }
}
