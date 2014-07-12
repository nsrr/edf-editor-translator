package listener;

import header.EDFFileHeader;
import header.ESAHeader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.ArrayList;

import javax.swing.JLabel;

import table.EIATable;
import table.ESATable;


/**
 * this class is to acts as the listener for the menu item "File -> Select Files"
 * usage: fileSelectFilesItem.addActionListener(frame, eiaTable, esaTable)
 */
public class SelectFilesListener implements ActionListener {
    private JLabel statusLabel;
    private EIATable eiaTable;
    private ArrayList<ESATable> esaTables;
    private ArrayList<EDFFileHeader> edfFileHeaders;
    private ArrayList<File> edfFiles;
    private int numberOfOpenedFiles = 0;
    
    public SelectFilesListener() {
    	// 
    }

    /**
     * TODO
     * @param wkLabel status label in the main frame
     * @param iniEiaTable the initial EIA table built from source files
     * @param iniEsaTables the initial group of ESA tables built from source files
     * @param iniEdfFileHeaders the initial EDF Headers acquired from source files
     * @param srcEdfFiles the path names of source files
     */
    public SelectFilesListener(JLabel wkLabel, EIATable iniEiaTable, ArrayList<ESATable> iniEsaTables, 
                               ArrayList<EDFFileHeader> iniEdfFileHeaders, ArrayList<File> srcEdfFiles) {
        statusLabel = wkLabel;
        eiaTable = iniEiaTable;
        esaTables = iniEsaTables;
        edfFileHeaders = iniEdfFileHeaders;
        edfFiles = srcEdfFiles;
    }


    /**
     * TODO
     * @param event the actionEvent
     * algorithm:
     * (1) first, select files through JFileChooser
     * (2) build an array of EIA headers and ESA headers;
     * (3) build a EIATable and multiple ESATables (each one of which corresponds
     * to a file's signal attributes.
     * (4) render all tables
     */
    public void actionPerformed(ActionEvent event) {
//        File[] edfFiles;
//        create a file filter for opening files
//        String extName = "edf";
//        String description = "EDF Files (*.edf)";
//        EDFFileFilter filter =
//            new EDFFileFilter(new String[] { extName }, description);
//
//        customize the file chooser
//        JFileChooser chooser = new JFileChooser();
//        chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
//        chooser.setMultiSelectionEnabled(true);
//        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //this selection mode guarantees the unselection of directories.
//        chooser.setDialogTitle("Open EDF Files");
//
//        get the user option
//        int option = chooser.showOpenDialog(null);
//        File sourceDirectory = null;
//        if (option != JFileChooser.APPROVE_OPTION) 
//            return;          
//        edfFiles = chooser.getSelectedFiles();
//        sourceDirectory = edfFiles[0].getParentFile(); // evaluate source directory;
        /**
         * deal with two things
         * (1) create the EIA and ESA headers;
         * (2) render the EIA and ESA tables;
         */
        numberOfOpenedFiles = edfFiles.size(); //evaluate the number of opened files

        edfFileHeaders = new ArrayList<EDFFileHeader>(numberOfOpenedFiles);

        // read each file to construct headers
        File currentFile;
        for (int i = 0; i < edfFiles.size(); i++) {
            currentFile = edfFiles.get(i);
            try {
                RandomAccessFile raf = new RandomAccessFile(currentFile, "r");
                edfFileHeaders.set(i, new EDFFileHeader(raf, currentFile, false));  // not a template
            } catch (IOException e) {
                e.printStackTrace(); // TODO: require more thoughtful handler
            }
        }
        
        /**
         * construct EIA Table
         * Note: an EIA table consists multiple EIA headers
         */
        eiaTable = new EIATable(edfFileHeaders, numberOfOpenedFiles);
                
        /** 
         * construct ESA Tables
         * one ESA header corresponds to one ESA table
         * algorithm is: 
         * 1. acquire the eiaHeader of the current file
         * 1. construct the ESA table one channel after another.
         */
        esaTables = new ArrayList<ESATable>(numberOfOpenedFiles);
        ESAHeader esaHeader;
        for (int i = 0; i < numberOfOpenedFiles; i++) {
            esaHeader =  edfFileHeaders.get(i).getEsaHeader();
            esaTables.set(i, new ESATable(esaHeader, true));
        }          
    }
}

///////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////// junk, yet not removed please ////////////////////////////////
//    /**
//     * listeners for the select files menu item
//     */
//    private class SelectFilesListener implements ActionListener {
//
//        private int numberOfOpenedFiles = 0;
//        public SelectFilesListener() {}
//        /**
//         * @param e the actionEvent
//         * algorithm:
//         * (1) first, select files through JFileChooser
//         * (2) build an array of eia headers and esa headers;
//         * (3) build a EIATable and multiple ESATables (each one of which corresponds <br>
//         * to a file's signal attributes.
//         * (4) render all tables
//         
//        public void actionPerformed(ActionEvent e) {
//            NewTaskDialog newTask = new NewTaskDialog();
//            File sourceDirectory = null;
//            if (srcEdfFiles != null)
//                sourceDirectory =
//                        srcEdfFiles[0].getParentFile(); // source directory;
//            System.out.println("passed here");
//            srcEdfFiles = NewTaskDialog.sourceFiles;
//        	  //File[] edfFiles;
//
//            //create a file filter for opening files
//            String extName = "edf";
//            String description = "EDF Files (*.edf)";
//            EDFFileFilter filter =
//                new EDFFileFilter(new String[] { extName }, description);
//
//            //customize the file chooser
//            JFileChooser chooser = new JFileChooser();
//            chooser.setFileFilter(filter); // to use multiple filter, use chooser.addChoosableFileFilter(anotherFilter);
//            chooser.setMultiSelectionEnabled(true);
//            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //this selection mode guarrentees the unselection of directories.
//            chooser.setDialogTitle("Open EDF Files");
//
//            // get the user option
//            int option = chooser.showOpenDialog(null);
//            File sourceDirectory = null;
//            if (option != JFileChooser.APPROVE_OPTION)
//                return;
//            srcEdfFiles = chooser.getSelectedFiles();
//
//            //File sourceDirectory = null;
//            if (srcEdfFiles != null)
//                sourceDirectory = srcEdfFiles[0].getParentFile(); // source directory; 
//            /**
//             * deal with two things
//             * (1) create the EIA and ESA headers;
//             * (2) render the EIA and ESA tables;
//             
//          numberOfOpenedFiles =
//                    srcEdfFiles.length; //evaluate the number of opened files
//
//            srcEdfFileHeaders = new EDFFileHeader[numberOfOpenedFiles];
//
//            //read each file to construct headers
//            for (int i = 0; i < srcEdfFiles.length; i++) {
//                File currentFile = srcEdfFiles[i];
//                try {
//                    RandomAccessFile raf =
//                        new RandomAccessFile(currentFile, "r");
//                    srcEdfFileHeaders[i] = new EDFFileHeader(raf, currentFile);
//                } catch (IOException f) {
//                    f.printStackTrace(); // TO-DO: require more thoughtful handler
//                }
//            }
//
//            /**
//             * construct eia Table
//             * Note: an eia table consistes mulitple eia headers
//          
//          iniEiaTable = new EIATable(srcEdfFileHeaders, numberOfOpenedFiles);
//
//            /**
//             * construct esa Tables
//             * one esa header corresponds to one esa table
//             * algorithm is:
//             * (1) first, acquire the eiaHeader of the current file
//             * (2) secondly, construct the ESA table one channel after another.
//             
//            iniEsaTables = new ESATable[numberOfOpenedFiles];
//            for (int i = 0; i < numberOfOpenedFiles; i++) {
//                ESAHeader esaHeader = srcEdfFileHeaders[i].getEsaHeader();
//                iniEsaTables[i] = new ESATable(esaHeader);
//            }
//
//            leftStatusBar.setText("Source Directory:" +
//                                  sourceDirectory.toString());
//
//            tabPane.addTab("<html><body leftmargin=15 topmargin=8 marginwidth=15 marginheight=5>Normalize</body></html>",
//                           new JScrollPane(iniEsaTables[0]));
//            tabPane.addTab("<html><body leftmargin=15 topmargin=8 marginwidth=15 marginheight=5>De-identify</body></html>",
//                           new JScrollPane(iniEiaTable));
//            tabPane.setSelectedIndex(0);
//
//            taskTree.addFileNodesAt(workingDir, srcEdfFiles);
//
//        } //end of ActionPerformed()
//
//    } //end of the SelectFilesListener */
