package translator.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import translator.utils.Keywords;

/**
 * This interface is used by each of the vendors who will be implementing the
 * translation process
 * 
 * @author wei, 2014-8-27
 */
public abstract class AbstractTranslatorFactory {

  protected String softwareVersion;
  protected String xmlAnnotation; // annotation file path
  protected String edfFile; // EDF file path
  protected String output; // output file path: initialized in write() method
  protected Document document; // document is the result of resolving BOM
  protected Set<String> events;
  protected HashMap<String, Object>[] map; // configuration structure from
                                           // mapping file

  // Local variables, initialized in method initLocalVariables();
  protected String[] timeStart; // (00.00.00 00.00.00) (date time)
  protected DateTime edfDateTime;
  protected DateTime eventDateTime;
  protected int numberOfSignals;
  protected String[] signalLabels;
  private static HashSet<String> errorSet = new HashSet<String>();

  /**
   * Reads the path of the EDF file, the annotation file and the mapping file
   * 
   * @param edfFile
   *          the path of the EDF file
   * @param annotationFile
   *          the path of the annotation file
   * @param mappingFile
   *          the path of the mapping file
   * @return true if this process is successful
   */
  public abstract boolean read(String edfFile, String annotationFile,
      String mappingFile);

  /**
   * Does the translation process
   * 
   * @return true if successful
   */
  public abstract boolean translate();

  /**
   * Writes the result to the path indicated by the argument
   * 
   * @param outputFile
   *          the output path
   * @return true if this process is done successful
   */
  public abstract boolean write2xml(String outputFile);

  /**
   * Writes the result to JSON file
   * 
   * @param outputFile
   *          the output file path
   * @return true if this process is done successful
   */
  public abstract boolean write2JSON(String outputFile);

  /**
   * Reads mapping file and saves as an array of HashMap Can be put in a higher
   * hierarchy
   * 
   * @param mapFile
   *          the mapping file name
   * @return the mapping in form of HashMap
   */
  public abstract HashMap<String, Object>[] readMapFile(String mapFile);

  /**
   * Logs messages.
   * 
   * @param message
   *          the message to be logged
   */
  public static void log(String message) {
    AnnotationTranslatorClient.translationErrors += message;
  }

  /**
   * Get signal location from scored event Assume that readMapFile(mapFile)
   * method is called Algorithm: 1. check whether annLocation is one of the EDF
   * file's signal labels 1.1 if true, then return the annLocation 1.2 if false,
   * then check if the signal name list from the mapping file match the EDF
   * signal names 1.3 if true, return the matched one 1.4 if all of above is
   * false, choose the first EDF signal name as default channel
   * 
   * @param scoredEvent
   *          event element
   * @param annLocation
   *          the signal location recored in the annotation file
   * @return signal location
   */
  public abstract String getSignalLocationFromEvent(Element scoredEvent,
      String annLocation);

  /**
   * Save edf date time information and channel info into local storage
   * 
   * @param edfFile
   *          EDF file path
   * @return Signal channel names
   */
  public void initLocalVariables(String edfFile) {
    try {
      File edf = new File(edfFile);
      if (edf.exists() && edf.isFile()) {
        RandomAccessFile raf = new RandomAccessFile(edfFile, "r");
        try {
          getStartDateTime(raf);
          getSignalNumber(raf); // record number of signals
          getSignalLabels(raf, numberOfSignals);
        } finally {
          raf.close();
        }
      } else {
        System.out.println("EDF file does not exists.");
        return;
      }
    } catch (IOException e) {
      e.printStackTrace();
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      log(errors.toString());
    }
  }

  private void getSignalNumber(RandomAccessFile raf) throws IOException {
    byte[] signalNumber = new byte[4];
    raf.seek(252);
    raf.readFully(signalNumber);
    numberOfSignals = Integer.valueOf(new String(signalNumber).trim());
  }

  private void getSignalLabels(RandomAccessFile raf, int signalCount)
      throws IOException {
    signalLabels = new String[signalCount];
    for (int i = 0; i < signalCount; i++) {
      signalLabels[i] = getSignalLabel(raf, i);
    }
    return;
  }

  /**
   * Get specified signal label
   * 
   * @param raf
   *          the RandomAccessFile of corresponding EDF file
   * @param signalNumber
   *          the 'signalNumber'th signal
   * @return the label name
   */
  private String getSignalLabel(RandomAccessFile raf, int signalNumber) {
    String labelString;
    byte[] label = new byte[16];
    try {
      raf.seek(256 + signalNumber * 16);
      raf.readFully(label);
      labelString = new String(label).trim();
    } catch (IOException e) {
      labelString = "";
      e.printStackTrace();
    }
    return labelString;
  }

  /**
   * Records the start date from the EDF file Can be put in a higher hierarchy
   * 
   * @param edfFile
   *          the EDF file name
   * @return the start date and duration, first string is start date and the
   *         second is duration
   */
  public void getStartDateTime(RandomAccessFile raf) throws IOException {
    timeStart = new String[2];
    raf.seek(168);
    char[] date = new char[8];
    for (int i = 0; i < 8; i++) {
      date[i] = (char) raf.readByte();
    }

    // edf.read(date);
    char[] time = new char[8];
    for (int i = 0; i < 8; i++) {
      time[i] = (char) raf.readByte();
    }
    raf.seek(236);

    char[] numRec = new char[8];
    for (int i = 0; i < 8; i++) {
      numRec[i] = (char) raf.readByte();
      // System.out.println(dur[i]);
    }
    char[] durRec = new char[8];
    for (int i = 0; i < 8; i++) {
      durRec[i] = (char) raf.readByte();
      // System.out.println(dur[i]);
    }

    double duration = Double.parseDouble(String.valueOf(durRec).trim()) * // TODO
        Double.parseDouble(String.valueOf(numRec).trim());
    timeStart[0] = String.valueOf(date) + " " + String.valueOf(time);
    timeStart[1] = String.valueOf(duration);

    String dateStr = new String(date);
    DateTimeFormatter fmt;
    int month = Integer.valueOf(dateStr.substring(3, 5));
    int day = Integer.valueOf(dateStr.substring(0, 2));
    int year = Integer.valueOf(dateStr.substring(6, 8));
    if (month >= 1 && month <= 12 && day >= 1 && day <= 31 && year > 0) {
      fmt = DateTimeFormat.forPattern("dd.MM.yy HH.mm.ss");
      edfDateTime = fmt
          .parseDateTime(new String(date) + " " + new String(time));
    } else {
      fmt = DateTimeFormat.forPattern("HH.mm.ss"); // default year 1970
      edfDateTime = fmt.parseDateTime(new String(time));
    }
  }

  /**
   * Resolves BOM and stores the result document
   * 
   * @param xmlAnnotationFile
   *          the xml annotation file
   * @return true if this operation successful
   */
  public Document resolveBOM(String xmlAnnotationFile) {
    Document doc = null;
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File(xmlAnnotationFile));
      @SuppressWarnings("resource")
      BOMInputStream bomInputStream = new BOMInputStream(inputStream);
      ByteOrderMark bom = bomInputStream.getBOM();
      String charsetName = bom == null ? "UTF-8" : bom.getCharsetName();
      inputStream.close();

      inputStream = new FileInputStream(new File(xmlAnnotationFile));
      Reader reader = new InputStreamReader(inputStream, charsetName);
      InputSource is = new InputSource(reader);
      is.setEncoding(charsetName);

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      doc = docBuilder.parse(is);
      doc.getDocumentElement().normalize();
    } catch (Exception e) {
      e.printStackTrace();
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      log(errors.toString());
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return doc;
  }

  /**
   * Gets the text content of the <code>childName</code> node from a parent
   * element
   * 
   * @param parent
   *          an scored event element
   * @param childName
   *          the child name
   * @return the text content in the child node
   */
  public String getElementByChildTag(Element parent, String childName) {
    NodeList list = parent.getElementsByTagName(childName);
    if (list.getLength() > 1) {
      throw new IllegalStateException("Multiple child elements with name "
          + childName);
    } else if (list.getLength() == 0) {
      return null;
    }
    Element child = (Element) list.item(0);
    return getText(child);
  }

  /**
   * Gets the text content of an element
   * 
   * @param element
   *          the element to extract from
   * @return the text content of this element
   */
  public static String getText(Element element) {
    StringBuffer buf = new StringBuffer();
    NodeList list = element.getChildNodes();
    boolean found = false;
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      if (node.getNodeType() == Node.TEXT_NODE) {
        buf.append(node.getNodeValue());
        found = true;
      }
    }
    return found ? buf.toString() : null;
  }
  
  /**
   * Adds message into log file
   * 
   * @param message
   *          the message to be logged
   * @param showOnScreen
   *          if true, also show message on the console
   */
  public static void addElementIntoLog(String message, boolean showOnScreen) {
    if (errorSet.contains(message)) {
      return;
    }
    errorSet.add(message);
    if (showOnScreen) {
      System.out.println(message);
    }
    BufferedWriter out = null;
    try {
      String filename = Keywords.translator_log;
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          filename, (new File(filename)).exists())));
      out.write(message + "\r\n");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (out != null)
          out.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
