package editor;

import header.ESAHeader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import table.EDFTable;
import table.EIATable;
import table.ESATable;

public class DiscardChangesListener implements ActionListener{
	
	private int selectedEDF;
	private ArrayList<File> sourceFiles;
	private int tabLocation;
	
	/**
     * build the eia Table
     */
    private void yieldEiaTable(){
    	EIATable eiaTable = null;
        int numberOfOpenedFiles = sourceFiles.size(); 
        //MainWindow.iniEiaTable = MainWindow.dupEiaTable;
        eiaTable = new EIATable(MainWindow.dupEdfFileHeaders, numberOfOpenedFiles);
        eiaTable.setUpdateSinceLastSave(true); //the initial update status should be true
        eiaTable.setSavedOnce(false);

        eiaTable.setMasterHeaderCategory(EDFTable.MasterHeaderCategory.EIA_WORKSET); //obsolete line
        
        MainWindow.setIniEiaTable(eiaTable);
    }
    
    /**
     * construct esa Tables
     * one esa header corresponds to one esa table
     * algorithm is:
     * 1. acquire the eiaHeader of the current file;
     * 2. construct the ESA table one channel after another;
     * 3. update the status.
     */
    private void yieldEsaTable(){
        ESATable esaTable = null;
       
       //need check, Fangping, 08/20/2010
        ESAHeader esaHeader = MainWindow.dupEdfFileHeaders.get(selectedEDF).getEsaHeader();//1.
        esaTable= new ESATable(esaHeader, true);//2.
        // configure the status 
        Boolean savedOnce = false; // start of 3.
        Boolean updateSinceLastSave = true;
        File workingFile = MainWindow.getWkEdfFiles().get(selectedEDF);
        int category = EDFTable.MasterHeaderCategory.ESA_WORKSET;                
        esaTable.setStatesAllInOne(savedOnce, updateSinceLastSave, workingFile, category, selectedEDF);//end of 4.
        esaTable.setSourceMasterFile(sourceFiles.get(selectedEDF));// set source file  
        
        MainWindow.setIniEsaTable(esaTable, selectedEDF);
    }

	private void updatePrimaryESATab(){
        if ( MainWindow.tabPane.isPrimaryTabsOpened()){
            MainWindow.tabPane.removeTabAt(1);
        }
        
        String ft = "Signal Attributes - " + MainWindow.getWkEdfFiles().get(selectedEDF).getName();
       /*  String ft = "<html><body leftmargin=10 topmargin=10 marginwidth=10" +
            "marginheight=5>Normalize</body></html>"; */
                   
        ESATable esaTable = MainWindow.getIniEsaTables().get(selectedEDF);
        WorkingTablePane pane = new WorkingTablePane(esaTable);
        MainWindow.tabPane.insertTab(ft, null, pane, null, 1);
        MainWindow.tabPane.setToolTipTextAt(1, "Signal Attributes of EDF Files");
        
        //since primary tabs have been dispalyed, set primaryTabsOpened true.
        MainWindow.tabPane.setPrimaryTabsOpened(true);   
    }
    
    private void updatePrimaryEIATab(){
        if ( MainWindow.tabPane.isPrimaryTabsOpened()){
            MainWindow.tabPane.removeTabAt(0);               
        }
        
        String ft = "Identity attributes";
        
/*             String ft = "<html><body leftmargin=10 topmargin=10 marginwidth=10" +
            "marginheight=10>De-identify</body></html>"; */
                   
        EIATable eiaTable = MainWindow.getIniEiaTable();
        WorkingTablePane pane = new WorkingTablePane(eiaTable);
        MainWindow.tabPane.insertTab(ft, null, pane, null, 0);
        MainWindow.tabPane.setToolTipTextAt(0, "Deidentify Attributes of EDF Files");
        
        //MainWindow.tabPane.setPrimaryTabsOpened(true);    
    }

    public void actionPerformed(ActionEvent e) {
        performActions();
    }
    
    private void performActions(){
        // do nothing is there is not tab pane at all
        if (MainWindow.tabPane == null || MainWindow.tabPane.getTabCount() == 0)
            return;
        
        int n = JOptionPane.showConfirmDialog(null, "Are you sure to discard changes?",
                                "Discard changes?", JOptionPane.YES_NO_OPTION);
        sourceFiles = MainWindow.getSrcEdfFiles();
        if (sourceFiles != null && n == JOptionPane.YES_OPTION) {
            tabLocation = MainWindow.getSelectedTabIndex();
            selectedEDF = MainWindow.getSelectedEDFIndex() - 2;
            if (tabLocation == 0) {
                yieldEiaTable();
                updatePrimaryEIATab();
                MainWindow.tabPane.setSelectedIndex(0);
            }
            if (tabLocation == 1) {
                yieldEsaTable();
                updatePrimaryESATab();
                MainWindow.tabPane.setSelectedIndex(1);
            }
        }
    }
}
