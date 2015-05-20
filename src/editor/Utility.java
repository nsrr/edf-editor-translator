/**
 * this class serves as container for utilities used in this project.
 * All utility methods are public static method.
 * Fangping, 02/03/2010
 */

package editor;

import header.EDFFileHeader;
import header.EIA;
import header.EIAHeader;
import header.ESA;
import header.ESAChannel;
import header.ESAHeader;
import header.ESATemplateChannel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import table.TableRowHeader;

/**
 * Utility class used for general purpose
 * @author wei wang, 2014-6-19
 */
public class Utility {

	// wei wang, 2014-6-19
	// Changed HashMap to HashMap<String,Object>
	
	/**
	 * Copies File from <code>source</code> to <code>target</code>
	 * @param source the source file to be copied
	 * @param target the target to copy to
	 * @throws IOException IOException
	 */
    public static void copyEDFFile(File source, File target) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(4096);
        FileChannel in=null, out=null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(target).getChannel();
            while (in.read(buffer) > 0) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            } 
        } catch (IOException e) {
            e.printStackTrace(); // added by wei wang, 2014-6-19        
        } finally {
            try {
                out.close();
                in.close();   
            }   
            catch (Exception e) {
                e.printStackTrace(); // added by wei wang, 2014-6-19
            }
        }
    }

    /**
     * Builds file name from file directory name, file name and file type
     * @param directory path name of the file
     * @param file name of the file
     * @param fileType file type: eia, esa, edf, xml, csv, etc.
     * @return full path of the file
     */
    public static File buildFileFullName(File directory, File file, String fileType) {
        String dirName = directory.getName() + "/";
        String fileName = file.getName() + fileType;
        String fileFullName = dirName + "/" + fileName;
        File regFile = new File(fileFullName);

        return regFile;
    }

    /**
     * Renames a file
     * @param old the old file
     * @param newName the new name of the old file
     */
    public static void renameFile(File old, String newName) {
        if (!old.exists() || old.isDirectory())
            return;
        File newFile = new File(newName);
        old.renameTo(newFile);
    }


    /**
     * Maps the EIA attributes in the template header to EDF file header
     * serves for template applying
     * @param eiaHeader EIAHeader used 
     * @param eiaTemplateHeader EIATemplateHeader used
     */
    public static void mapEIAHeader(EIAHeader eiaHeader, EIAHeader eiaTemplateHeader) {
        HashMap<String,Object> eiaHeaderMap = eiaHeader.getEIAHeader();        
        HashMap<String,Object> templateHeaderMap = eiaTemplateHeader.getEIAHeader();        
        HashMap<String,Object> dupEIAHeaderMap = new HashMap<String,Object>(eiaHeader.getEIAHeader());     
        String datestr = (String) dupEIAHeaderMap.get(EIA.START_DATE_RECORDING);
        // Fangping, 10/12/10
        String[] dateStore = disassembleDateStr(datestr);

//      mapPID(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore);
//      mapRID(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore); 
//      mapStartDate(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore);
        //10/13/2010
        mapPRIDFields(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore, EIA.LOCAL_PATIENT_ID);
        mapPRIDFields(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore, EIA.LOCAL_RECORDING_ID);
        mapStartDateField(eiaHeaderMap, dupEIAHeaderMap, templateHeaderMap, dateStore);
//      mapStartTime(edfEIAHeaderMap, templateHeaderMap); 
        eiaHeader.setEiaHeader(eiaHeaderMap);
    }

    /**
     * Disassembles date string separated by '.'
     * @param datestr date string
     * @return an array with length 3, corresponding to the number of fields of date
     * @author Fangping, 10/12/10
     * @author wei wang, commented on 2014-6-19
     */
    public static String[] disassembleDateStr(String datestr){
        if (datestr == null || datestr.isEmpty())
            return new String[]{"", "", ""};
        
        final String symbol = "[.]";
        final int maxsegnum = 3; // maximum segment number, 3 for dd.mm.yy
        String[] temp = datestr.split(symbol);
        String[] result = new String[]{"", "", ""};
        
        // duplicate to result;
        for (int i = 0; i < Math.min(maxsegnum, temp.length); i++)
            result[i] = temp[i];
        
        return result;
    }


    /**
     * @deprecated obsolete // wei wang
     * @param EIAHeaderMap EIAHeaderMap
     * @param dupEDFEIAHeaderMap dupEDFEIAHeaderMap
     * @param templateHeaderMap templateHeaderMap
     * @param dateStore dateStore
     */
    public static void mapPID(HashMap<String,Object> EIAHeaderMap, HashMap<String,Object> dupEDFEIAHeaderMap,
                              HashMap<String,Object> templateHeaderMap, String[] dateStore) {
        String title = EIA.LOCAL_PATIENT_ID;
        String templateValue = "" + (String)templateHeaderMap.get(title);
        templateValue = templateValue.trim();
                 
        String value;

        //case 1: only one single key provided
        //1.1
        if (templateValue.equalsIgnoreCase(EIA.key_blank)){
            value = " ";
            EIAHeaderMap.put(title, value);
            return;
        }
        //1.2    
        if (templateValue.isEmpty() || templateValue.equalsIgnoreCase(EIA.key_skip)){
            String tmp = "" + (String) dupEDFEIAHeaderMap.get(title);
            value = (tmp == null)? " ": tmp;
            EIAHeaderMap.put(title, value);
            return; // do nothing
        }
        //1.3
        if (templateValue.equalsIgnoreCase(EIA.key_rand)) {
            value = generateRandomID();
            EIAHeaderMap.put(title, value);
            return;
        } 
        //1.4 
        if (templateValue.equalsIgnoreCase(EIA.key_rid)) {
            value = (String)dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID);
            EIAHeaderMap.put(title, value);
            return;
        } 
        //1.5
        if(templateValue.equalsIgnoreCase(EIA.key_pid)) {
            value = (String) dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID);
            EIAHeaderMap.put(title, value);
            return;
        }
        //1.6
        if(templateValue.equalsIgnoreCase(EIA.key_filename)) {
            value = (String)dupEDFEIAHeaderMap.get(EIA.FILE_NAME);
            EIAHeaderMap.put(title, value);
            return;
        } 
        //1.7
        if(templateValue.equalsIgnoreCase(EIA.key_dd)|| templateValue.equalsIgnoreCase(EIA.key_mm) ||
                templateValue.equalsIgnoreCase(EIA.key_yy)){
            value = (String)dupEDFEIAHeaderMap.get(EIA.START_DATE_RECORDING);
            EIAHeaderMap.put(title, value);
            return;
        }
        
        //case 2: more than 1 keys detected
        if (templateValue.indexOf("}") > 1) {
            int index1 = 0;
            final int tagsize = "}".length();
            int index2 = templateValue.indexOf("}");
            int sIndex = templateValue.indexOf("{", 2);
            int lastIndex = index2;
            String keep = "";
            String substrValue;
            while (index2 > -1) {
                substrValue = templateValue.substring(index1, index2 + tagsize);
                if (substrValue.equalsIgnoreCase(EIA.key_rid)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID);
                } else 
                if (substrValue.equalsIgnoreCase(EIA.key_pid)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID);
                } else 
                if (substrValue.equalsIgnoreCase(EIA.key_filename)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.FILE_NAME);
                } else 
                if (substrValue.equalsIgnoreCase(EIA.key_dd)){
                    keep += dateStore[0];
                } else 
                if (substrValue.equalsIgnoreCase(EIA.key_mm)) {
                    keep += dateStore[1];
                } else
                if (substrValue.equalsIgnoreCase(EIA.key_yy)) {
                    keep += dateStore[2];
                }
                
                if (sIndex > -1) {
                    keep += templateValue.substring(index2 + 2, sIndex);
                }

                index1 = sIndex;
                sIndex = templateValue.indexOf("<", sIndex + 1);
                lastIndex = index2;
                index2 = templateValue.indexOf("/>", index2 + tagsize);
            }
            
            keep += templateValue.substring(lastIndex + tagsize, templateValue.length());
            
            if (keep.length() > 80) {
                keep = keep.substring(0, 80);
            }
            
            EIAHeaderMap.put(title, keep);
            return;
        }
        //case 3: no template key found
        EIAHeaderMap.put(title, templateValue);
    }
    
    public static final String reg_key_blank = "[{]blank[}]";
    public static final String reg_key_rand = "[{]rand[}]";
    public static final String reg_key_skip = "[{]skip[}]";
    public static final String reg_key_filename = "[{]filename[}]";
    public static final String reg_key_pid = "[{]pid[}]";
    public static final String reg_key_rid = "[{]rid[}]";
    public static final String reg_key_yy = "[{]yy[}]";
    public static final String reg_key_mm = "[{]mm[}]";
    public static final String reg_key_dd = "[{]dd[}]";
    
    public static final String[] RegPatterns = {reg_key_rand, reg_key_filename, 
                                            reg_key_pid, reg_key_rid, reg_key_yy, reg_key_mm, reg_key_dd};
    
    /**
     * Maps specified field of attribute of a template to EIA header
     * @param EIAHeaderMap the EIA header to be mapped
     * @param dupEDFEIAHeaderMap duplicate of the current EIA header
     * @param templateHeaderMap the template header to map from
     * @param dateStore the date string array
     * @param title the attribute name
     */
    private static void mapPRIDFields(HashMap<String,Object> EIAHeaderMap, HashMap<String,Object> dupEDFEIAHeaderMap,
                                  HashMap<String,Object> templateHeaderMap, String[] dateStore, String title) {
            String templateValue;
            if (templateHeaderMap.get(title) == null)
                templateValue = "" ;
            else
                templateValue = (String)templateHeaderMap.get(title);
            
            templateValue = templateValue.trim();
            
            String value;
            //special treatment for "{blank}"
            if (templateValue.toLowerCase().contains(EIA.key_blank)){
                value = " ";
                EIAHeaderMap.put(title, value);
                return;
            }
            //special treatment for "{skip}" or  ""        
            if (templateValue.isEmpty() || templateValue.toLowerCase().contains(EIA.key_skip)){
                String tmp = (String) dupEDFEIAHeaderMap.get(title);
                tmp = (tmp == null)? " ": tmp;
                value = tmp.trim();
                EIAHeaderMap.put(title, value);
                return; 
            }
            
            int count = RegPatterns.length;
            String[] keywords = new String[count];
            keywords[0] = generateRandomID(); // random
            keywords[1] = (String) dupEDFEIAHeaderMap.get(EIA.FILE_NAME);// filename;
            keywords[2] = (String) dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID);// pid
            keywords[3] = (String) dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID);// rid
            keywords[4] = dateStore[2];
            keywords[5] = dateStore[1];
            keywords[6] = dateStore[0];
            
            for (int i = 0; i < count; i++)
                templateValue = templateValue.replaceAll(RegPatterns[i], keywords[i]);
            
            final int effective_len = 80;
            if (templateValue.length() > effective_len )
                templateValue = templateValue.substring(0, effective_len);
            
            EIAHeaderMap.put(title, templateValue);
        }

    /**
     * Generates random ID
     * @return random ID of string format
     */
    public static String generateRandomID() {
        Random randomHelper = new Random();
        String pid = Long.toString(Math.abs(randomHelper.nextLong()), 36);
        
        return pid;
    }

    /**
     * @deprecated  // wei wang, 2014-6-19
     * @param edfEIAHeaderMap edfEIAHeaderMap
     * @param dupEDFEIAHeaderMap dupEDFEIAHeaderMap
     * @param templateHeaderMap templateHeaderMap
     * @param dateStore dateStore
     * map RID
     */
    public static void mapRID(HashMap<String,Object> edfEIAHeaderMap, HashMap<String,Object> dupEDFEIAHeaderMap,
                              HashMap<String,Object> templateHeaderMap, String [] dateStore) {
        String title = EIA.LOCAL_RECORDING_ID;        
        String templateValue = "" + (String)templateHeaderMap.get(title);
        templateValue = templateValue.trim();
        
        String value;
        //case 1: find a single template key
        //1.1
        if (templateValue.equalsIgnoreCase(EIA.key_blank)){
            value = "";
            edfEIAHeaderMap.put(title, value);
            return;
        }
        //1.2
        if (templateValue.equalsIgnoreCase(EIA.key_skip) || templateValue.isEmpty()){
            return; // no change
        }
        //1.3        	
        if (templateValue.equalsIgnoreCase(EIA.key_rand)) {
            value = generateRandomID();
            edfEIAHeaderMap.put(title, value);
            return;
        } 
        //1.4
        if (templateValue.equalsIgnoreCase(EIA.key_rid)) {
            value = (String)dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID);
            edfEIAHeaderMap.put(title, value);
            return;
        } 
        //1.5
        if(templateValue.equalsIgnoreCase(EIA.key_pid)) {
            value = (String)dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID);
            edfEIAHeaderMap.put(title, value);
            return;
        }
        //1.6
        if(templateValue.equalsIgnoreCase(EIA.key_filename)) {
            value = (String) dupEDFEIAHeaderMap.get(EIA.FILE_NAME);
            edfEIAHeaderMap.put(title, value);
            return;
        }
        //case 2: more than one template keys found
        if (templateValue.indexOf("/>") > 1) {
            int index1 = 0;
            int index2 = templateValue.indexOf("/>");
            int sIndex = templateValue.indexOf("<", 2);
            int lastIndex = index2;
            String keep = "";
            String substrValue;
            while (index2 > -1) {
                substrValue = templateValue.substring(index1, index2 + 2);
                if (substrValue.equalsIgnoreCase(EIA.key_rid)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID);
                } else if (substrValue.equalsIgnoreCase(EIA.key_pid)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID);
                } else if (substrValue.equalsIgnoreCase(EIA.key_filename)) {
                    keep += dupEDFEIAHeaderMap.get(EIA.FILE_NAME);
                } else if (substrValue.equalsIgnoreCase(EIA.key_dd)) {
                    keep += dateStore[0];
                } else if (substrValue.equalsIgnoreCase(EIA.key_mm)) {
                    keep += dateStore[1];
                } else if (substrValue.equalsIgnoreCase(EIA.key_yy)) {
                    keep += dateStore[2];
                }

                if (sIndex > -1) {
                    keep += templateValue.substring(index2 + 2, sIndex);
                }

                index1 = sIndex;
                sIndex = templateValue.indexOf("<", sIndex + 1);
                lastIndex = index2;
                index2 = templateValue.indexOf("/>", index2 + 2);
            }

            keep += templateValue.substring(lastIndex + 2, templateValue.length());

            if (keep.length() > 80) {
                keep = keep.substring(0, 80);
            }

            edfEIAHeaderMap.put(title, keep);
            return;
        }
        
        //case 3: no template key found
        edfEIAHeaderMap.put(title, templateValue);
    }
    
    /**
     * Maps start date field from a template to the EIA headers 
     * @param edfEIAHeaderMap the EIA header to be mapped
     * @param dupEDFEIAHeaderMap duplicate of the EIA header 
     * @param templateHeaderMap the template to be mapped from
     * @param dateStore date string of the current EIA header
     */
    public static void mapStartDateField(HashMap<String,Object> edfEIAHeaderMap, HashMap<String,Object> dupEDFEIAHeaderMap,
                                      HashMap<String,Object> templateHeaderMap, String [] dateStore) {
          String title = EIA.START_DATE_RECORDING;
          String templateValue = "" + (String)templateHeaderMap.get(title);
          templateValue = templateValue.trim();
          final int ds_len = dateStore.length; // it must be 3
          
          final String marker = "00";
          String value = "";	
          // 1. only a single key assigned
          if (templateValue.equalsIgnoreCase(EIA.key_blank)){
              // 00.00.00
              value =  marker + "." + marker + "." + marker;
              edfEIAHeaderMap.put(title, value);
              return;
          }       
          
          if (templateValue.isEmpty() || templateValue.equalsIgnoreCase(EIA.key_skip) ){
              return; // no change
          }

          if (templateValue.equalsIgnoreCase(EIA.key_rand)) {
              value = generateRandomDate(3);
              edfEIAHeaderMap.put(title, value);
              return;
          } 
          
          if (templateValue.equalsIgnoreCase(EIA.key_dd)){
              value = dateStore[0] + "." + marker + "." + marker;
              edfEIAHeaderMap.put(title, value);
              return;
          }
          if (templateValue.equalsIgnoreCase(EIA.key_mm)){
              value = marker + "." + dateStore[1] + "." + marker;
              edfEIAHeaderMap.put(title, value);
              return;
          }
          
          if (templateValue.equalsIgnoreCase(EIA.key_yy)){
              value =  marker + "." + marker +  "." + dateStore[2];
              edfEIAHeaderMap.put(title, value);
              return;
          }

        // case 2: more than one key assigned

        String[] disassembledValues = disassembleDateStr(templateValue);
        String[] newValues = dateStore;

        for (int i = 0; i < ds_len; i++) {
            if (disassembledValues[i].equalsIgnoreCase(EIA.key_dd)) {
                newValues[i] = dateStore[0];
                continue;
            }

            if (disassembledValues[i].equalsIgnoreCase(EIA.key_mm)) {
                newValues[i] = dateStore[1];
                continue;
            }

            if (disassembledValues[i].equalsIgnoreCase(EIA.key_yy)) {
                newValues[i] = dateStore[2];
                continue;
            }

            if (disassembledValues[i].equalsIgnoreCase(EIA.key_rand)) {
                newValues[i] = generateRandomDate(i);
                continue;
            }

            if (disassembledValues[i].equalsIgnoreCase(EIA.key_blank)) {
                newValues[i] = marker;
                continue;
            }

            if (disassembledValues[i].isEmpty() ||
                disassembledValues[i].equalsIgnoreCase(EIA.key_skip)) {
                newValues[i] = dateStore[i];
                continue;
            }
            // no key matched
            boolean digital = true;
            try {
                Integer.parseInt(disassembledValues[i]);
            } catch (NumberFormatException e) {
                digital = false;
            }
            // keep the original value if not digital
            if (digital)
                newValues[i] = disassembledValues[i];
        }
        value = newValues[0] + "." + newValues[1] + "." + newValues[2];
        edfEIAHeaderMap.put(title, value);
        return;
    }
    
  /**
   * Maps start date from a template to the EIA headers  
   * @param edfEIAHeaderMap the EIA header to be mapped
   * @param dupEDFEIAHeaderMap duplicate of the EIA header 
   * @param templateHeaderMap the template to be mapped from
   * @param dateStore date string of the current EIA header
   */
  public static void mapStartDate(HashMap<String,Object> edfEIAHeaderMap, HashMap<String,Object> dupEDFEIAHeaderMap,
                                    HashMap<String,Object> templateHeaderMap, String [] dateStore) {
        String title = EIA.START_DATE_RECORDING;
        String templateValue = "" + (String)templateHeaderMap.get(title);
        templateValue = templateValue.trim();
        final int ds_len = dateStore.length; // it must be 3
        
        final String maskstr = "00";
        String value;
        //1. only a single key assigned
        if (templateValue.equalsIgnoreCase(EIA.key_blank)){
            //00.00.00
            value =  maskstr + "." + maskstr + "." + maskstr;
            edfEIAHeaderMap.put(title, value);
            return;
        }       
        
        if (templateValue.equalsIgnoreCase(EIA.key_skip) || templateValue.isEmpty()) {
            return; // no change
        }

        if (templateValue.equalsIgnoreCase(EIA.key_rand)) {
            value = generateRandomDate(3);
            edfEIAHeaderMap.put(title, value);
            return;
        } 
        
        if (templateValue.equalsIgnoreCase(EIA.key_dd)) {
            value = dateStore[0] + "." + maskstr + "." + maskstr;
            edfEIAHeaderMap.put(title, value);
            return;
        }
        if (templateValue.equalsIgnoreCase(EIA.key_mm)) {
            value = maskstr + "." + dateStore[1] + "." + maskstr;
            edfEIAHeaderMap.put(title, value);
            return;
        }
        
        if (templateValue.equalsIgnoreCase(EIA.key_yy)){
            value =  maskstr + "." + maskstr +  "." + dateStore[2];
            edfEIAHeaderMap.put(title, value);
            return;
        }

        //case 2: more than one key assgined
        if (templateValue.indexOf("/>") >= 1) {
            String[] disassembledValues = disassembleDateStr(templateValue);
            //by default value is 00
            String[] newValues = dateStore;

            for (int i = 0; i < ds_len; i++) {
                if (disassembledValues[i].equalsIgnoreCase(EIA.key_dd)) {
                    newValues[i] = dateStore[0];
                    continue;
                } 
                
                if (disassembledValues[i].equalsIgnoreCase(EIA.key_mm)) {
                    newValues[i] = dateStore[1];
                    continue;
                } 
                
                if (disassembledValues[i].equalsIgnoreCase(EIA.key_yy)) {
                    newValues[i] = dateStore[2];
                    continue;
                } 
                
                if (disassembledValues[i].equalsIgnoreCase(EIA.key_rand)) {
                    newValues[i] = generateRandomDate(i);
                    continue;
                } 
                //otherwise
                boolean digital = true;
                try{
                    Integer.parseInt(disassembledValues[i]);
                }
                catch(NumberFormatException e){
                    digital = false;
                }
                //keep the orginal value if not digital
                newValues[i] = digital?  disassembledValues[i]: newValues[i];                
            }

            edfEIAHeaderMap.put(title, newValues[0] + "." + newValues[1] + "." + newValues[2]);
            return;
        }
        //case 3: no template key assigned
        boolean digital = true;
        try {
            Integer.parseInt(templateValue);
        } catch(NumberFormatException e){
            digital = false;
        }
        //keep the orginal value if not digital        
        value = digital? templateValue: (String) dupEDFEIAHeaderMap.get(title);                
        edfEIAHeaderMap.put(title, value);
    }

  	/**
  	 * Generates random date string
  	 * @param choice specify which attribute to return. 0 for day, 1 for month, 2 for year, 3 for randomly generated date including day, month and year
  	 * @return string representation of the date specified by the choice option
  	 */
    public static String generateRandomDate(int choice) {
        Random helper = new Random();
        String day = "";
        String month = "";
        String year = "";
        switch (choice) {
        	case 0: {
                day = Integer.toString(helper.nextInt(30));
                if (day.length() < 2)
                    day = "0" + day;
                return day;
        	}
        	case 1: {
                month = Integer.toString(helper.nextInt(12) + 1);
                if (month.length() < 2)
                    month = "0" + month;
                return month;
        	}
        	case 2: {
                year = Integer.toString(helper.nextInt(100));
                if (year.length() < 2)
                    year = "0" + year;
                return year;
        	}
        	case 3: {
                day = Integer.toString(helper.nextInt(30));
                month = Integer.toString(helper.nextInt(12) + 1);
                year = Integer.toString(helper.nextInt(100));
                break;
        	}
        }
        if (day.length() < 2)
            day = "0" + day;
        if (month.length() < 2)
            month = "0" + month;
        if (year.length() < 2)
            year = "0" + year;

        return day + "." + month + "." + year;
    }
//
//    public static void mapStartTime(HashMap edfEIAHeaderMap, HashMap dupEDFEIAHeaderMap,
//                                    HashMap templateHeaderMap) {
//        String title = EIA.START_TIME_RECORDING;
//        String templateValue = (String)templateHeaderMap.get(title);
//        templateValue = templateValue.trim();
//        if (templateValue.equalsIgnoreCase(EIA.key_blank))
//            edfEIAHeaderMap.put(title, "");
//        else if (templateValue.equalsIgnoreCase(EIA.key_skip))
//            ; // do nothing
//        else if (templateValue.equalsIgnoreCase(EIA.key_rand)) {
//            String time = generateRandomTime();
//            edfEIAHeaderMap.put(title, time);
//        } else if (templateValue.equals(EIA.key_rand)) {
//        	edfEIAHeaderMap.put(title, dupEDFEIAHeaderMap.get(EIA.LOCAL_RECORDING_ID));
//        } else if(templateValue.equals(EIA.key_pid)) {
//        	edfEIAHeaderMap.put(title, dupEDFEIAHeaderMap.get(EIA.LOCAL_PATIENT_ID));
//        }else if(templateValue.equals(EIA.key_filename)) {
//        	edfEIAHeaderMap.put(title, dupEDFEIAHeaderMap.get(EIA.FILE_NAME));
//        } else
//            edfEIAHeaderMap.put(title, templateValue);
//    }
//
//    public static String generateRandomTime() {
//        Random helper = new Random();
//        String hour = Integer.toString(helper.nextInt(24));
//        if (hour.length() < 2)
//            hour = "0" + hour;
//        String minute = Integer.toString(helper.nextInt(60));
//        if (minute.length() < 2)
//            minute = "0" + minute;
//        String second = Integer.toString(helper.nextInt(60));
//        if (second.length() < 2)
//            second = "0" + second;
//
//        return hour + "." + minute + "." + second;
//    }

    /**
     * Maps the ESA attributes in the template header to the EDF file header.
     * serves for template applying.
     * @param edfFileHeader the ESA header to map to
     * @param esaTemplateHeader the template to be mapped from
     */
    public static void mapESAHeader(ESAHeader edfFileHeader,
                                    ESAHeader esaTemplateHeader) {
        ESAChannel edfFileChannels[] = edfFileHeader.getSignalHeader();
        ESATemplateChannel templateChannels[] =
            esaTemplateHeader.getSignalTemplateHeader();

        int numberOfEdfFileChannels = edfFileChannels.length;
        int numbOfTemplateChannels = templateChannels.length;
        
        String[] esaAttributes = ESA.getESAAttributes();
        
        HashMap<String,Object> currentChannel;
        String key;
        for (int i = 0; i < numberOfEdfFileChannels; i++) {
            
            currentChannel = edfFileChannels[i].getEsaChannel();

            key = (String)currentChannel.get(ESA.LABEL);
            key = key.trim();       
            
            HashMap<String,Object> templateChannel;
            HashMap<String,Object> clone;
            for (int j = 0; j < numbOfTemplateChannels; j++) {
                templateChannel = templateChannels[j].getEsaChannel();
                clone = new HashMap<String,Object>(templateChannel);
                for(int k = 0; k < ESA.NUMBER_OF_ATTRIBUTES; k++) {
                	if(((String)templateChannel.get(esaAttributes[k])).equalsIgnoreCase("")) {
                		clone.put(esaAttributes[k], currentChannel.get(esaAttributes[k]));
                	}
                }
                String temp = (String)templateChannel.get(ESA.LABEL);
                if (temp != null) {
                    temp = temp.trim();
                    if (temp.equals(key)) {
                        if (!((String)templateChannel.get(ESA.CORRECTED_LABEL)).trim().equals("")) {
                            clone.put(ESA.LABEL, templateChannel.get(ESA.CORRECTED_LABEL));
                        }
                        clone.put(ESA.NUMBER_OF_SAMPLES, (String)edfFileChannels[i].getSignalAttributeValueAt(ESA.NUMBER_OF_SAMPLES)); //keep the number of samples
                        clone.remove(ESA.CORRECTED_LABEL);
                        edfFileChannels[i].setEsaChannel(clone);
                    }
                }
            }
        }
    }
    
    /**
     * Gets current time as string 
     * @return the formatted time string
     */
    public static String currentTimeToString() {
        Date time = new Date();
        DateFormat df = new SimpleDateFormat("h:mm a, yyyy.MM.dd");
        return df.format(time);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Zendrix //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    // each attribute eia.VERSION is changed to EIA.VERSION format
    /**
     * Writes EIA header to XML specified by File object
     * @param file the File object used to save EIA header information
     * @param eia EIA header
     */
    public static void writeXMLEIA(File file, EIAHeader eia) {
        try {
            System.out.println("Start Wrtiting");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            output.write("<EIAHeader>\n");
            String writeStr = String.format("\t<Version>%s</Version>\n", eia.getAttributeValueAt(EIA.VERSION)); // wei wang modified on 2014-6-19
//                String.format("\t<Version>%s</Version>\n", eia.getAttributeValueAt(eia.VERSION));
            output.write(writeStr);
            writeStr =
                    String.format("\t<PatientID>%s</PatientID>\n", eia.getAttributeValueAt(EIA.LOCAL_PATIENT_ID));
            output.write(writeStr);
            writeStr =
                    String.format("\t<RecordInfo>%s</RecordInfo>\n", eia.getAttributeValueAt(EIA.LOCAL_RECORDING_ID));
            output.write(writeStr);
            writeStr =
                    String.format("\t<StartDate>%s</StartDate>\n", eia.getAttributeValueAt(EIA.START_DATE_RECORDING));
            output.write(writeStr);
            writeStr =
                    String.format("\t<StartTime>%s</StartTime>\n", eia.getAttributeValueAt(EIA.START_TIME_RECORDING));
            output.write(writeStr);
            writeStr =
                    String.format("\t<HeaderBytes>%s</HeaderBytes>\n", eia.getAttributeValueAt(EIA.NUMBER_OF_BYTES_IN_HEADER));
            output.write(writeStr);
            writeStr =
                    String.format("\t<Reserved>%s</Reserved>\n", eia.getAttributeValueAt(EIA.RESERVED));
            output.write(writeStr);
            writeStr =
                    String.format("\t<NumDataRecords>%s</NumDataRecords>\n", eia.getAttributeValueAt(EIA.NUMBER_OF_DATA_RECORDS));
            output.write(writeStr);
            writeStr =
                    String.format("\t<DurDataRecord>%s</DurDataRecord>\n", eia.getAttributeValueAt(EIA.DURATION_OF_DATA_RECORD));
            output.write(writeStr);
            writeStr =
                    String.format("\t<NumSignals>%s</NumSignals>\n", eia.getAttributeValueAt(EIA.NUMBER_OF_SIGNALS));
            output.write(writeStr);
            output.write("</EIAHeader>");
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes out EDF headers to CSV file
     * @param edfHeaders EDF headers to write out
     * @param fileNames list of files used for writing CSV file
     * @return the name of output CSV file
     */
    public static String writeCSVEIA(ArrayList<EDFFileHeader> edfHeaders, ArrayList<File> fileNames) {
        String fileName = "";
        try {
            File wkdir = MainWindow.workingDirectory;
            String wkdirPath = wkdir.getAbsolutePath();
            fileName = wkdirPath + "\\File_Attributes_" + getDateTime() + ".csv";
            File file = new File(fileName);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(
            	"FileName,Version,PatientID,RecordInfo,StartDate,StartTime,HeaderBytes,Reserved,NumDataRecords,DurDataRecord,NumSignals\n"
            );
            EIAHeader eia = null; 
            String fname = "";
            for (int i = 0; i < edfHeaders.size(); i++) {
                eia = edfHeaders.get(i).getEiaHeader();
                fname = fileNames.get(i).getAbsolutePath();
                output.write("\"" + fname + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.VERSION) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.LOCAL_PATIENT_ID) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.LOCAL_RECORDING_ID) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.START_DATE_RECORDING) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.START_TIME_RECORDING) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.NUMBER_OF_BYTES_IN_HEADER) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.RESERVED) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.NUMBER_OF_DATA_RECORDS) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.DURATION_OF_DATA_RECORD) + "\",");
                output.write("\"" + eia.getAttributeValueAt(EIA.NUMBER_OF_SIGNALS) + "\"");
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Writes XML file from existing ESA header
     * @param file output xml file path
     * @param esa ESA header used to generate xml file
     */
    public static void writeXMLESA(File file, ESAHeader esa) {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            output.write("<ESAHeader>\n");
            int channels = esa.getNumberOfChannels();
            for (int i = 0; i < channels; i++) {
                ESAChannel esaChannel = esa.getEsaChannelAt(i);
                output.write("\t<Channel>\n");
                String writeStr =
                    String.format("\t\t<Label>%s</Label>\n", esaChannel.getSignalAttributeValueAt(ESA.LABEL));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<Transducer>%s</Transducer>\n", esaChannel.getSignalAttributeValueAt(ESA.TRANSDUCER_TYPE));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<PhyDim>%s</PhyDim>\n", esaChannel.getSignalAttributeValueAt(ESA.PHYSICAL_DIMESNION));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<PhyMin>%s</PhyMin>\n", esaChannel.getSignalAttributeValueAt(ESA.PHYSICAL_MINIMUM));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<PhyMax>%s</PhyMax>\n", esaChannel.getSignalAttributeValueAt(ESA.PHYSICAL_MAXIMUM));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<DigMin>%s</DigMin>\n", esaChannel.getSignalAttributeValueAt(ESA.DIGITAL_MINIMUM));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<DigMax>%s</DigMax>\n", esaChannel.getSignalAttributeValueAt(ESA.DIGITAL_MAXIMUM));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<Prefiltering>%s</Prefiltering>\n",
                                      esaChannel.getSignalAttributeValueAt(ESA.PREFILTERING));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<NumSamples>%s</NumSamples>\n", esaChannel.getSignalAttributeValueAt(ESA.NUMBER_OF_SAMPLES));
                output.write(writeStr);
                writeStr =
                        String.format("\t\t<Reserved>%s</Reserved>\n", esaChannel.getSignalAttributeValueAt(ESA.RESERVED));
                output.write(writeStr);
                output.write("\t</Channel>\n");
            }
            output.write("</ESAHeader>");
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes out the ESA headers to csv file
     * @param edfHeaders the EDF file headers 
     * @param fileNames the corresponding EDF files
     * @return the csv file name to be written out
     */
    public static String writeCSVESA(ArrayList<EDFFileHeader> edfHeaders, ArrayList<File> fileNames) {
    	String fileName = "";
        try {
        	File wkdir = MainWindow.workingDirectory;
        	String wkdirStr = wkdir.getAbsolutePath();
        	fileName = wkdirStr + "\\Signal_Attributes_" + getDateTime() + ".csv";
        	File file = new File(fileName);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            
            ESAHeader esa; String fname;
            
            for(int i = 0; i < edfHeaders.size(); i++) {
            	esa = edfHeaders.get(i).getEsaHeader();
            	fname = fileNames.get(i).getAbsolutePath();	
            	output.write(fname + "\n");
            	output.write("Label,Transducer,PhyDim,PhyMin,PhyMax,DigMin,DigMax,Prefiltering,NumSamples,Reserved\n");
            	int channels = esa.getNumberOfChannels();
                for (int j = 0; j < channels; j++) {
                    ESAChannel ch = esa.getEsaChannelAt(j);
                    String str = ch.getSignalAttributeValueAt(ESA.LABEL) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.TRANSDUCER_TYPE) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.PHYSICAL_DIMESNION) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.PHYSICAL_MINIMUM) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.PHYSICAL_MAXIMUM) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.DIGITAL_MINIMUM) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.DIGITAL_MAXIMUM) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.PREFILTERING) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.NUMBER_OF_SAMPLES) + ",";
                    str += ch.getSignalAttributeValueAt(ESA.RESERVED) + "\n";
                    output.write(str);
                }
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Reads EIA header from XML file
     * @param file XML file to be read
     * @return EIA header corresponding to this XML file
     */
    public static EIAHeader readXMLEIA(File file) {
        EIAHeader eia = new EIAHeader();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String str = input.readLine();
            str = input.readLine();
            if (str.equals("<EIAHeader>")) {
                str = input.readLine().trim();
                eia.setValueAt(EIA.VERSION,
                               outputElement("<Version>", "</Version>", str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.LOCAL_PATIENT_ID,
                               outputElement("<PatientID>", "</PatientID>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.LOCAL_RECORDING_ID,
                               outputElement("<RecordInfo>", "</RecordInfo>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.START_DATE_RECORDING,
                               outputElement("<StartDate>", "</StartDate>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.START_TIME_RECORDING,
                               outputElement("<StartTime>", "</StartTime>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.NUMBER_OF_BYTES_IN_HEADER,
                               outputElement("<HeaderBytes>", "</HeaderBytes>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.RESERVED,
                               outputElement("<Reserved>", "</Reserved>",
                                             str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.NUMBER_OF_DATA_RECORDS,
                               outputElement("<NumDataRecords>",
                                             "</NumDataRecords>", str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.DURATION_OF_DATA_RECORD,
                               outputElement("<DurDataRecord>",
                                             "</DurDataRecord>", str));
                str = input.readLine().trim();
                eia.setValueAt(EIA.NUMBER_OF_SIGNALS,
                               outputElement("<NumSignals>", "</NumSignals>", str));
                /**
                 * wei wang, 2014-6-19
                 * Issue soved: input is never closed
                 */
                try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eia;
    }

    /**
     * Extracts string between start tag string and end tag string
     * @param startTag the start tag string
     * @param endTag the end tag string
     * @param str the target string to be extract from		
     * @return the extracted string 
     */
    public static String outputElement(String startTag, String endTag, String str) {
        String ret = "";
        int startIndex = str.indexOf(startTag);
        int endIndex = str.indexOf(endTag);
        ret = str.substring(startIndex + startTag.length(), endIndex);

        return ret;
    }

    /**
     * Parses CSV file into EIA header
     * @param file CSV file to be processed
     * @return EIA header extracted from this CSV file
     */
    public static EIAHeader readCSVEIA(File file) {
        EIAHeader eia = new EIAHeader();
        String[] elements;
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String str = input.readLine();
            str = input.readLine();
            str = str.substring(1, str.length()-1);
            elements = str.split("\",\"");
            eia.setValueAt(EIA.VERSION, elements[0]);
            eia.setValueAt(EIA.LOCAL_PATIENT_ID, elements[1]);
            eia.setValueAt(EIA.LOCAL_RECORDING_ID, elements[2]);
            eia.setValueAt(EIA.START_DATE_RECORDING, elements[3]);
            eia.setValueAt(EIA.START_TIME_RECORDING, elements[4]);
            eia.setValueAt(EIA.NUMBER_OF_BYTES_IN_HEADER, elements[5]);
            eia.setValueAt(EIA.RESERVED, elements[6]);
            eia.setValueAt(EIA.NUMBER_OF_DATA_RECORDS, elements[7]);
            eia.setValueAt(EIA.DURATION_OF_DATA_RECORD, elements[8]);
            eia.setValueAt(EIA.NUMBER_OF_SIGNALS, elements[9]);
            /**
             * wei wang, 2014-6-19
             * Issue solved: input is never closed
             */
            try {
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eia;
    }

    /**
     * Reads XML file and converted to ESA header
     * @param file the XML file to be converted from
     * @return the constructed ESA header
     */
    public static ESAHeader readXMLESA(File file) {
        ESAHeader esa = new ESAHeader();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String str = input.readLine();
            str = input.readLine();
            int counter = 0;
            if (str.equals("<ESAHeader>")) {
                str = input.readLine().trim();
                while (str.trim().equals("<Channel>")) {
                    ESAChannel ch = new ESAChannel();
//                    ArrayList<String> temp = new ArrayList<String>();  // temp not in use, deleted by wei wang on 2014-6-19
                    str = input.readLine().trim();
                    str = outputElement("<Label>", "</Label>", str);
                    ch.setAttributeValueAt(ESA.LABEL, str);
                    str = input.readLine().trim();
                    str = outputElement("<Transducer>", "</Transducer>", str);
                    ch.setAttributeValueAt(ESA.TRANSDUCER_TYPE, str);
                    str = input.readLine().trim();
                    str = outputElement("<PhyDim>", "</PhyDim>", str);
                    ch.setAttributeValueAt(ESA.PHYSICAL_DIMESNION, str);
                    str = input.readLine().trim();
                    str = outputElement("<PhyMin>", "</PhyMin>", str);
                    ch.setAttributeValueAt(ESA.PHYSICAL_MINIMUM, str);
                    str = input.readLine().trim();
                    str = outputElement("<PhyMax>", "</PhyMax>", str);
                    ch.setAttributeValueAt(ESA.PHYSICAL_MAXIMUM, str);
                    str = input.readLine().trim();
                    str = outputElement("<DigMin>", "</DigMin>", str);
                    ch.setAttributeValueAt(ESA.DIGITAL_MINIMUM, str);
                    str = input.readLine().trim();
                    str = outputElement("<DigMax>", "</DigMax>", str);
                    ch.setAttributeValueAt(ESA.DIGITAL_MAXIMUM, str);
                    str = input.readLine().trim();
                    str = outputElement("<Prefiltering>", "</Prefiltering>", str);
                    ch.setAttributeValueAt(ESA.PREFILTERING, str);
                    str = input.readLine().trim();
                    str = outputElement("<NumSamples>", "</NumSamples>", str);
                    ch.setAttributeValueAt(ESA.NUMBER_OF_SAMPLES, str);
                    str = input.readLine().trim();
                    str = outputElement("<Reserved>", "</Reserved>", str);
                    ch.setAttributeValueAt(ESA.RESERVED, str);
                    esa.setSignalChannel(counter, ch);
                    counter++;

                    str = input.readLine().trim();
                    str = input.readLine().trim();
                    
                    /**
                     * wei wang, 2014-6-19
                     * Issue solved: input is never closed
                     */
                    try {
        				input.close();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return esa;
    }

    /**
     * Reads CSV file and converted to ESA header
     * @param file the CSV file
     * @return the ESA header constructed from this CSV file
     */
    public static ESAHeader readCSVESA(File file) {
        ESAHeader esa = new ESAHeader();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String str = input.readLine();
            str = input.readLine();
            int counter = 0;
            while (str != null) {
                ESAChannel ch = new ESAChannel();
                String[] temp = str.split(",");
                ch.setAttributeValueAt(ESA.LABEL, temp[0]);
                ch.setAttributeValueAt(ESA.TRANSDUCER_TYPE, temp[1]);
                ch.setAttributeValueAt(ESA.PHYSICAL_DIMESNION, temp[2]);
                ch.setAttributeValueAt(ESA.PHYSICAL_MINIMUM, temp[3]);
                ch.setAttributeValueAt(ESA.PHYSICAL_MAXIMUM, temp[4]);
                ch.setAttributeValueAt(ESA.DIGITAL_MINIMUM, temp[5]);
                ch.setAttributeValueAt(ESA.DIGITAL_MAXIMUM, temp[6]);
                ch.setAttributeValueAt(ESA.PREFILTERING, temp[7]);
                ch.setAttributeValueAt(ESA.NUMBER_OF_SAMPLES, temp[8]);
                ch.setAttributeValueAt(ESA.RESERVED, temp[9]);
                esa.setSignalChannel(counter, ch);
                counter++;
                
                /**
                 * wei wang, 2014-6-19
                 * Issue solved: input is never closed
                 */
                try {
    				input.close();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return esa;
    }
    
    /**
     * Gets string representation of the current date
     * @return a string representation of the date and time
     */
    private static String getDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
        
    /**
     * Increments the system font size
     * @param scale the scale to increase the system font size
     * @author Fangping, 08/04/2010
     * @author wei wang, commented on 2014-6-19
     */
    public static void increaseSytemFont(int scale) {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value != null && value instanceof Font) {
                UIManager.put(key, null);
                Font font = UIManager.getFont(key);
                if (font != null) {
                    float size = font.getSize2D();
                    UIManager.put(key, new FontUIResource(font.deriveFont(size + scale)));
                }
            }
        }
    }
    
    /** 
     * Tests if the row header if visible in order to implement header for table
     * @param table the table to be tested against
     * @return true if the row header is visible
     * @author Fangping, 08/11/2010
     * @author wei wang, commented on 2014-6-19
     */
    public static boolean isRowHeaderVisible(JTable table) {
        Container p = table.getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                JViewport rowHeaderViewPort = scrollPane.getRowHeader();
                if (rowHeaderViewPort != null)
                    return rowHeaderViewPort.getView() != null;
            }
        }
        return false;
    }

    /**
     * Creates row header for table with row number (starting with 1) displayed
     * @param table the target table
     */
    public static void removeRowHeader(JTable table) {
        Container p = table.getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                scrollPane.setRowHeader(null);
            }
        }
    }

    /**
     * Creates row header for table with row number (starting with 1) displayed
     * @param table the table used to extract header
     */
    public static void setRowHeader(JTable table) {
        Container p = table.getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                scrollPane.setRowHeaderView(new TableRowHeader(table));
                JButton jbt = new JButton();
                jbt.setEnabled(false);
                scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, jbt);
            }
        }
    }
    
//    /*
//     * a group of name collision checking methods, including:
//     * (1) checkDirNameCollision: called when the Physiomimi Work directory
//     * (2) checkSingleFileNameCollision: called when rename/save as
//     * (3) checkFileGroupNameCollision: called when add files/new task
//     * Fangping, 08/17/2010
//     */

    /**
     * Name collision/auto new name for creating Physiomimi Work directory
     * this method should be merged with checkSingleFileNameCollision.
     * @author Fangping, 08/17/2010
     * @param sourceDir source file directory
     * @param subdirName the subdirectory to be tested
     * @return the subdirectory name 
     */
    public static File parseDirNameCollision(File sourceDir, String subdirName) {
        
        File newDir = (subdirName.equals(""))? sourceDir: new File(sourceDir.toString() + "/" + subdirName);

        //retrieve files in the current directory
        File files[] = sourceDir.listFiles();
//        /*
//         * check if file with the name has already existed
//         * cannot simply use new File().exists which does discriminate file from directory
//         * Fangping, 08/21/2010
//         */
        boolean collision = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() &&
                files[i].getName().equalsIgnoreCase(subdirName)) { 
                collision = true;
                break;
            }
        }
        // no collision with the initial name, then return the name
        if (collision == false)
            return newDir;

        // with name collision, go on to retrieve an available one.
        int suffix, k;

        label1:
        for (suffix = 1; suffix < Integer.MAX_VALUE; suffix++) {
            for (k = 0; k < files.length; k++)
                if (files[k].isDirectory() &&
                    files[k].getName().equalsIgnoreCase(subdirName + "(" +
                                                        suffix + ")"))
                    continue label1;

            break;
        }       
       newDir = new File(sourceDir.toString() + "/" + subdirName + "(" + suffix + ")");

        return newDir;
    }
    
    /**
     * Name collision check for a single file, called when renameing a single file
     * @author Fangping, 08/17/2010
     * @param afile the target file
     * @param filelist can be null, in which case name collision scope is restricted in the afile's directory
     * @return the file name without conflicting with the file list 
     */
    @SuppressWarnings("unused") // added for "filelist == null" if statement, by wei wang on 2014-6-19
	public static File parseSingleFileNameCollision(File afile, ArrayList<File> filelist) {
        // do nothing to a directory
        if (afile.isDirectory()) {
            System.out.println("invalid file. directory not allowed");
//            JOptionPane.showMessageDialog(null, "Directory not allowed to appear in destination folder", "Warning", JOptionPane.WARNING_MESSAGE);
//            MainWindow.middleStatusBar.setText("Directory not allowed to appear in destination folder!");
            return null;
        }
        
        File parentDir = afile.getParentFile();
        int sz = filelist.size();
        // take Array because listFiles return that type of data structure
        File sibFiles[] = new File[sz]; 
        
        if (filelist == null) {
            //if there is no file list given, then parse collision between afile and its siblings
            sibFiles = parentDir.listFiles();
        } else {
            for (int i = 0; i < sz; i++)
                sibFiles[i] = filelist.get(i);
        }
            //sibFiles = (File[])filelist.toArray(); 
        
        //get the index of file with the same name as the afile
        int index = indexOfCollision(afile, sibFiles);
        
        //no collision detected
        if (index == -1)
            return afile;    
        
        return getSuffixNamedFile(afile, sibFiles);        
    }
    
    /**
     * Checks if the {@code afile} has name collided with files in fileList except fileList.get(eindex)
     * eindex can be -1, means go through the whole fileList;
     * @author Fangping Huang, 08/26/2010
     * @param afile the file to be checked
     * @param fileList a list of files to be tested on
     * @param eindex index of the {@code afile} in working directory
     * @return true if the file has conflicted with the file list except for the file at index {@code eindex}
     */
    public static boolean isFileNameCollided(File afile, ArrayList<File> fileList, int eindex) {   
        if (afile == null || afile.isDirectory() || fileList == null || fileList.size() == 0)
            return false;
        
        int size = fileList.size();
        for(int i = 0; i < size; i++){
            if (i == eindex)
                continue;
            if (afile.getAbsolutePath().equalsIgnoreCase(fileList.get(i).getAbsolutePath()))
                return true;      
        }
        
        return false;
    }
    
    /**
     * Checks if the {@code afile} has name collided with files in the {@code fileList}
     * @param afile the file to be checked
     * @param fileList the file list to be tested on
     * @return true the file name is not collided with any one of the files in the {@code fileList}
     */
    public static boolean isFileNameCollided(File afile, ArrayList<File> fileList) {   
        if (afile == null || afile.isDirectory() || fileList == null || fileList.size() == 0)
            return false;
        
        int size = fileList.size();
        for(int i = 0; i < size; i++){
            if (afile.getAbsolutePath().equalsIgnoreCase(fileList.get(i).getAbsolutePath()))
                return true;      
        }
        
        return false;
    }

    /**
     * Renames a file name if it conflicted with other files in the {@code filelist}
     * @author Fangping, 08/21/2010
     * @param filelist the list of files to be parsed with name collision
     * @return the collision-free files after renamed
     */
    @SuppressWarnings("deprecation")
	public static ArrayList<File> parseFileGroupNameCollision(ArrayList<File> filelist) {
        int sz = filelist.size();
        boolean hasNullFile = false;
        
        // if there is no valid list of files
        if (sz == 0)
            return null;
        
        // file-wise analysis procedure
        File curfile;
        int num = 0; // test, 5/27/2014
        for (int k = 1; k < sz; k++) {
        	/**
        	 * increase progress percentage along with yieldNewEDFHeaders() method. 5/27/2014
        	 */
        	if((num + 1) % (NewTask_for_ValidityCommandLine.getScale() * 2) == 0) {                	
             	NewTask_for_ValidityCommandLine.getTask().increaseProgress();
             	System.out.println("Task progress increased to " + NewTask_for_ValidityCommandLine.getTask().progress);  // test         
             } else {}
            num++;
            curfile = filelist.get(k); 
            curfile = parseSingleFileNameCollision(curfile, sublist(filelist, 0, k-1)); 
            // curfile = parseSingleFileNameCollision(curfile, (ArrayList<File>)(filelist.subList(0, k-1)));
            if (curfile == null) {
              hasNullFile = true;
              continue;
            }
            filelist.set(k, curfile);
        }
        if (hasNullFile) {
          JOptionPane.showMessageDialog(null, "Output directory not allowed.\nPlease try again!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        return filelist;
    }
    
    /**
     * Creates a sublist from the given ArrayList
     * @param files the ArrayList of files  
     * @param start the start index from which to extract sublist, inclusive
     * @param end the end index inclusive
     * @return an ArrayList of newly constructed sublist
     */
    public static ArrayList<File> sublist(ArrayList<File> files, int start, int end) {
      if (files.isEmpty()) {
        return null;
      }
        ArrayList<File> output = new ArrayList<File>(end + 1 - start);
        for (int i = start; i <= end; i++)
            output.add(files.get(i));        
        return output;
    }
    
    /*
     * alert: works only if all working files have been saved in dir.
     */
//   public static File replicateSingleFileToDirectory(File file, File dir){
//        File[] dirfiles = dir.listFiles();
//        ArrayList<File> dirList = new ArrayList<File>();
//        for (int i = 0; i < dirfiles.length; i++)
//            dirList.add(dirfiles[i]);
//        
//        return parseSingleFileNameCollision(file, dirList);
//    }

    /**
     * Copies a list of files into the output directory
     * Used for new task mode of: File_Selections, NO_Override
     * @param srcfiles source file list
     * @param outputDir output directory
     * @return the list of added files 
     */
    public static ArrayList<File> copyFilestoDirectory(ArrayList<File> srcfiles, File outputDir) {
    	/***
    	 * This method need a progress bar to indicate progress, TBD
    	 * wei wang 05/22/2014
    	 */
    	JFrame frame = new JFrame("Copying files...");
    	JPanel pane = new JPanel();
    	
    	frame.add(pane);
        File[] olds = outputDir.listFiles();
        int nolds = olds.length;        
        
        ArrayList<File> tempFiles = new ArrayList<File>(srcfiles.size() + nolds);
        String dirname = outputDir.getAbsolutePath() + "/";
        //1. copy files already there to current list
        for (int i = 0; i < nolds; i++) {
            tempFiles.add(olds[i]);
        }
        //2. copy srcfiles into the list
        for (File file : srcfiles) {        	        	
            tempFiles.add(new File(dirname + file.getName()));
        }
        //3. produce new file names
        MainWindow.middleStatusBar.setText("Parse File Name Collision"); // test, wei wang, 5/27/2014
        ArrayList<File> output = parseFileGroupNameCollision(tempFiles);
        
        return sublist(output, nolds, tempFiles.size() - 1);
    }

    /**
     * Checks if there is name collision. If it is, return the index of the file
     * @author Fangping, 08/21/2010
     * @param target the file to be renamed
     * @param fileList the list of files to be referred to
     * @return the index having the same file name with target in the fileList
     */
    public static int indexOfCollision(File target, File fileList[]) {        
        for (int i = 0; i < fileList.length; i++) {
            if ((target.isDirectory() != fileList[i].isDirectory())) // dir does not collide with file with the same name
                continue;
            if (target.getAbsolutePath().equalsIgnoreCase(fileList[i].getAbsolutePath()))
                return i;
        }
        
        return -1;        
    }

    /**
     * Renames a target file in a file list to avoid naming conflict
     * @author Fangping, 08/21/2010
     * @param target the file to be renamed
     * @param filelist the list of files to be compared to target
     * @return the renamed file of the target
     */
    public static File getSuffixNamedFile(File target, File filelist[]) {
    	// verified

        String fullName = target.getName();
        int idx = fullName.lastIndexOf(".");
        String ordName = (idx == -1)? fullName: fullName.substring(0, idx);
        String dotExtName = (idx == -1)? "": fullName.substring(idx);

        boolean isDir = target.isDirectory();

        int suffix, k;
        out_loop:
        for (suffix = 1; suffix < Integer.MAX_VALUE; suffix++) {
            for (k = 0; k < filelist.length; k++) {
                if (filelist[k].getName().equalsIgnoreCase(ordName + "(" + suffix + ")" + dotExtName)
                    && (filelist[k].isDirectory() == isDir)) // have the same dir attribute as target
                    continue out_loop;
            }
            break;
        }

        String newName = ordName + "(" + suffix + ")" + dotExtName;
        return new File(target.getParent() + "/" + newName);
    }    

//   Helper to set the default focus on No button instead of Yes
//   copyied from http://forums.sun.com/thread.jspa?threadID=5395347
//   Fangping, 08/22/2010
    /**
     * Shows customized option dialog, default on "No" button
     * @param parent parent determines the Frame in which the dialog is displayed; if null, or if the parentComponent has no Frame, a default Frame is used
     * @param message the Object to display
     * @param title title the title string for the dialog
     * @param messageType messageType an integer designating the kind of message this is, primarily used to determine the icon from the pluggable Look and Feel
     * @return true if the user chose "Yes" option, false otherwise
     */
    public static boolean defaultNoOptionPane(Component parent, String message, String title, int messageType) {

        int reply = JOptionPane.showOptionDialog(parent, message, title,
            JOptionPane.YES_NO_OPTION, messageType, null,
            new String[] { "Yes", "No" }, "No");

        return (reply == 0? true: false);
    }
    
    /**
     * Show customized option dialog
     * @param parent determines the Frame in which the dialog is displayed; if null, or if the parentComponent has no Frame, a default Frame is used
     * @param message the Object to display
     * @param title the title string for the dialog
     * @param messageType an integer designating the kind of message this is, primarily used to determine the icon from the pluggable Look and Feel
     * @return true if the user chose "Yes" option, false otherwise
     */
    public static boolean NoThanksOptionPane(Component parent, String message, String title, int messageType) {

        int reply = JOptionPane.showOptionDialog(parent, message, title,
            JOptionPane.YES_NO_OPTION, messageType, null,
            new String[] { "Yes", "No, thanks" }, "Yes");

        return (reply == 0? true: false);
    }
    
    //Fangping, 08/23/2010
    //refer to: https://jdic.dev.java.net/
    //refer to: http://stackoverflow.com/questions/526037/java-how-to-open-user-system-preffered-editor-for-given-file
    /**
     * Opens default application according to the user operating system to process the file
     * @param file the file to be edited
     * @return true if this file can be edited
     */
    public static boolean editFile(final File file) {
      if (!Desktop.isDesktopSupported()) {
        return false;
      }

      Desktop desktop = Desktop.getDesktop();
      if (!desktop.isSupported(Desktop.Action.EDIT)) {
        return false;
      }

      try {
        desktop.edit(file);
        //desktop.browse(uri);
      } catch (IOException e) {
        // Log an error
        return false;
      }

      return true;
    }
    
    /**
     * Open default browser
     */
    private static final String errMsg = "Error attempting to launch web browser";

    /**
     * Open a url
     * @param url the url to be opened
     */
    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager"); // change Class to Class<?> by wei wang on 2014-6-19
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
                openURL.invoke(null, new Object[] { url });
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { // assume Unix or Linux
                String[] browsers = { 
                	"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" 
                };
                String browser = null;
                 for (int count = 0; count < browsers.length && browser == null; count++)
                    if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[] { browser, url });
            }
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }    
     
    /**
     * Scroll the table to specific location that can be viewed
     * @param table the table currently viewed
     * @param rr the row number
     * @param cc the column number
     */
    public static void scrollTableRowToVisible(JTable table, int rr, int cc) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)table.getParent();
        Rectangle rect = table.getCellRect(rr, cc, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }
    
    /**
     * Given a file, return the index of the tab it belongs
     * @param file the master file of the tab
     * @return the index of the tab of the file
     */
    public static int getTabIndexofMasterFile(File file) {
//        int index;  // deleted by wei wang on 2014-6-19
        EDFTabbedPane tabbedPane = MainWindow.tabPane;
        int tabCount = tabbedPane.getTabCount();
        BasicEDFPane pane;
        for (int i = 0; i < tabCount; i++){
            pane = (BasicEDFPane)tabbedPane.getComponentAt(i);
            if (file == pane.getMasterFile()){
//                index = i; // deleted by wei wang on 2014-6-19
                return i;
//                return index; // modified by wei wang on 2014-6-19
            }
        }      
        //not exist    
        return -1;        
    }
    
    /**
     * Recursively find files, which name ends with ".edf"
     * @param root root file to search.
     * @param tmp output ArrayList of found files.
     * @return a list of files found
     * wei wang, 5/23/2014
     */
    public static ArrayList<File> findFileAddRecursive(File root, ArrayList<File> tmp) {
        File[] list = root.listFiles();
        if(list == null) return tmp;
        for(int i = 0; i < list.length; i++) {
            File file = list[i];
            if(file.isDirectory()) {
                findFileAddRecursive(file, tmp);
                // TODO: process with this directory
            } else {
                // TODO: process file
                String str = file.getName().toLowerCase();
                if (str.toLowerCase().endsWith(".edf")) {
//                    listOfFiles.add(dirList[i]);
                    tmp.add(file);
                }
            }
        }
        return tmp;
    }
    
    /**
     * Start and end of busy waiting cursor.
     * @param component the component the cursor rested upon
     * wei wang, 5/27/2014 
     */
    public static void startWaitCurosr(JComponent component) {
    	int cursorType = Cursor.CROSSHAIR_CURSOR;
    	Component glassPane = ((RootPaneContainer)component.getTopLevelAncestor()).getGlassPane();
    	glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
		glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Changes cursor to default cursor
     * @param component where the cursor stays upon
     */
    public static void endWaitCursor(JComponent component) {
    	int cursorType = Cursor.DEFAULT_CURSOR;
    	Component glassPane = ((RootPaneContainer)component.getTopLevelAncestor()).getGlassPane();
    	glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
    }
    
    //Fangping, 10/12/2010
 /*    public static final String root_label = "Header";
    public static final String eia_label = "EIA";
    public static final String pid_label = "PID";
    public static final String rid_label = "RID";
    public static final String date_label = "DATE";
    
    public static boolean saveEIATemplateToXml(String filePath, EIAHeader header){
        try {
            Writer out =
                new OutputStreamWriter(new FileOutputStream(filePath));
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
            data = header.getAttributeValueAt(EIA.LOCAL_PATIENT_ID);
            em = document.createElement(element);
            em.appendChild(document.createTextNode(data));
            subrootElement.appendChild(em);

            element = rid_label;
            data = header.getAttributeValueAt(EIA.LOCAL_RECORDING_ID);
            em = document.createElement(element);
            em.appendChild(document.createTextNode(data));
            subrootElement.appendChild(em);

            element = date_label;
            data = header.getAttributeValueAt(EIA.START_DATE_RECORDING);
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
    
    private static EIAHeader yieldHeader(Element root){
        EIAHeader header = new EIAHeader();
        
        NodeList list = root.getElementsByTagName(eia_label);
        Element el = null;
        if (list != null)
            el = (Element) list.item(0);
        
        String pidValue, ridValue, dateValue;
        pidValue = getTextValue(el, pid_label);
        ridValue = getTextValue(el, rid_label);
        dateValue = getTextValue(el, date_label);
        
        header.setValueAt(pid_label, pidValue);
        header.setValueAt(rid_label, ridValue);
        header.setValueAt(date_label, dateValue);
        
        return header;
    }
    
    private static String getTextValue(Element ele, String tagName){
        String value = null;
        NodeList list = ele.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0){
            Element el = (Element) list.item(0);
            value = el.getFirstChild().getNodeValue();
        }
        
        return value;
    } */
    
}//end of Utility class