package editor;


import header.EDFFileHeader;
import header.EIAHeader;
import header.ESAHeader;

import java.io.File;
import java.io.RandomAccessFile;

import javax.swing.UIManager;

public class Main {   
    //identify the system to be mac os or not
    public static final boolean mac_os;
    static{
        if (System.getProperty("os.name").contains("Mac OS")){
            mac_os = true;
            System.setProperty("apple.laf.useScreenMenuBar", "true");   
        }
        else
            mac_os = false;
    }    
  
    public static void main(String[] args) throws Exception{
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	if(args.length == 0)
    	{
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");               
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	                public void run() { 
	                    Utility.increaseSytemFont(1);
	                    MainWindow mainWindow = new MainWindow();
	                    
	                    mainWindow.setVisible(true);
	                }
	            }); 
    	}
    	else
    	{
    		/*
    		 * args[0] - eia or esa for identity or signal attribute template application
    		 * args[1] - source file
    		 * args[2] - template file
    		 * args[3] - output directory
    		 */
    		if(args[0].equalsIgnoreCase("eia"))
    		{
    			//Read the source and template files
    			RandomAccessFile raf = new RandomAccessFile(args[1], "rw");
    			File edfFile = new File(args[1]);
    			EIAHeader srceia = new EIAHeader(raf, edfFile);
    			EIAHeader tempeia = EIAHeader.retrieveEIAHeaderFromXml(args[2]);
    			//Map the template file to the source file
    			Utility.mapEIAHeader(srceia, tempeia);
    			//Save the file
    			String dir = "";
    			if(args.length <= 3)
    			{
    				dir = edfFile.getParent();
        			dir = dir + File.separator + "Physiomimi Work";
        			System.out.println(dir);
        			File newDir = new File(dir);
        			newDir.mkdir();	
    			}
    			else
    			{
    				dir = args[3];
    				File newDir = new File(dir);
        			newDir.mkdir();
    			}
    			String newFile = dir + File.separator + edfFile.getName();
    			File nFile = new File(newFile);
    			Utility.copyEDFFile(edfFile, nFile);
    			raf = new RandomAccessFile(nFile, "rw");
    			srceia.saveToDisk(raf, nFile);
    		}
    		if(args[0].equalsIgnoreCase("esa"))
    		{
    			//Read the source and template files
    			RandomAccessFile raf = new RandomAccessFile(args[1], "rw");
    			File edfFile = new File(args[1]);
    			EDFFileHeader srcFile = new EDFFileHeader(raf, edfFile, false);
    			ESAHeader esa = srcFile.getEsaHeader();
    			raf = new RandomAccessFile(args[2], "rw");
    			File templateFile = new File(args[2]);
    			EDFFileHeader tempFile = new EDFFileHeader(raf, templateFile, true);
    			ESAHeader tempEsa = tempFile.getEsaHeader();
    			//Map the template file to the source file
    			Utility.mapESAHeader(esa, tempEsa);
    			//Save the file
    			String dir = "";
    			if(args.length <= 3)
    			{
    				dir = edfFile.getParent();
        			dir = dir + File.separator + "Physiomimi Work";
        			System.out.println(dir);
        			File newDir = new File(dir);
        			newDir.mkdir();	
    			}
    			else
    			{
    				System.out.println("here");
    				dir = args[3];
    				File newDir = new File(dir);
        			newDir.mkdir();
    			}
    			String newFile = dir + File.separator + edfFile.getName();
    			File nFile = new File(newFile);
    			Utility.copyEDFFile(edfFile, nFile);
    			raf = new RandomAccessFile(nFile, "rw");
    			esa.saveToDisk(raf, nFile, true, false);
    		}
    		/************************************************************** 
    		 * The below feature improvement was made by Gang Shu on February 6, 2014
    		 **************************************************************/
    		if (args[0].equalsIgnoreCase("-translator")){
    			translator.logic.CommandLineController.Start(args);
    			System.exit(0);
    		}
    		if (args[0].equalsIgnoreCase("-validator")){
    			validator.logic.CommandLineController.Start(args);
    			System.exit(0);
    		}
    		/************************************************************** 
    		 * The above feature improvement was made by Gang Shu on February 6, 2014
    		 **************************************************************/ 
    	}
    	
    	
    }
}

