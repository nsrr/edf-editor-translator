package translator.logic.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

public class TestReadEDF {
  
  private static String edf1 = "/Users/wxwcase/git/HeartBEAT/30516_01102011s2.EDF";
  private static String edf = "/Users/wxwcase/git/SHHS/ClevelandFamily/1.EDF";
  
  public static void main(String[] args) {
    try {
      File edfFile = new File(edf);
      if (edfFile.exists() && edfFile.isFile()) {
        System.out.println("EDF file exists.");
        RandomAccessFile raf = new RandomAccessFile(edfFile, "r");
        try {
          System.out.println("Signal Numbers: " + getSignalNumber(raf));
          System.out.println("Patient Info: " + getStartDate(raf));
          System.out.println("-----------------------------");
          for (String label : getSignalLabels(raf, Integer.valueOf(getSignalNumber(raf)))) {
            System.out.println("<" + label + ">");
          }
        } finally {
          raf.close();
        }
      } else {
        System.out.println("EDF file does not exists.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static String getSignalNumber(RandomAccessFile raf) throws IOException {
    byte[] signalNumber = new byte[4];
    raf.seek(252);
    raf.readFully(signalNumber);
    return new String(signalNumber).trim();
  }
  
  public static String getStartDate(RandomAccessFile raf) throws IOException {
    byte[] startdate = new byte[8];
    raf.seek(168);
    raf.readFully(startdate);
    return new String(startdate).trim();
  }
  
  public static String[] getSignalLabels(RandomAccessFile raf, int numOfSignals) throws IOException {
    String[] signalLabels = new String[numOfSignals];
    for (int i = 0; i < numOfSignals; i++) {
      signalLabels[i] = getSignalLabel(raf, i);
    }
    return signalLabels;
  }

  private static String getSignalLabel(RandomAccessFile raf, int signalNumber) {
    String labelString;
    byte[] label = new byte[16];
    try {
      raf.seek(256 + signalNumber * 16);
      raf.readFully(label);
      labelString = new String(label).trim();   
    } catch(IOException e) {
      labelString = "";
      e.printStackTrace();
    }
    return labelString;
  }
}
