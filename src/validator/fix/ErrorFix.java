package validator.fix;

import header.EDFFileHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.TableModel;

import editor.MainWindow;
import editor.NewTask_for_ValidityCommandLine;
import table.*;
import translator.utils.MyDate;

public class ErrorFix {
	
	public static void fixErrors(ArrayList<String> selected_edf_files, ArrayList<ErrorTypes> errorTypeAL){
		
		NewTask_for_ValidityCommandLine.addElementIntoLog("===============================================================", true, MainWindow.log);
		NewTask_for_ValidityCommandLine.addElementIntoLog("  => User start an error-fix task at " + MyDate.currentDateTime(), true, MainWindow.log);
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:1/3) Errors before applying bug-fixes:", true, MainWindow.log);
				
		//(0)Prepare esa/eia tables for selected edf files
		HashMap<String, String> map_src_with_wrk = new HashMap<String, String>();
		for (int i = 0; i < MainWindow.getWkEdfFiles().size(); i++){
			String src = MainWindow.getSrcEdfFiles().get(i).getAbsolutePath();
			String wrk = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
			map_src_with_wrk.put(src, wrk);
		}
		
		//(1)Validate tables and have a list of errors
		(new MainWindow.VerifyHeaderListener()).verifyHeaders();
		ArrayList<Incompliance> incompliances1 = MainWindow.aggregateIncompliances();
		NewTask_for_ValidityCommandLine.addElementIntoLog("    Total errors are found: " + incompliances1.size(), true, MainWindow.log);
		ArrayList<ESATable> esaTables = MainWindow.getIniEsaTables();
		
		//(2)Apply error-fixes on selected edf files
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:2/3) Applying bug-fixes:", true, MainWindow.log);
		for (String filename : selected_edf_files){
			
			String message = "";
			message += "   ------------------" + "\r\n";
			message += "   Fixed EDF file: " + filename;
			NewTask_for_ValidityCommandLine.addElementIntoLog(message, true, MainWindow.log);
			
			//(2.1)Find out incompliancess associated with the EDF file. 
			ArrayList<Incompliance> incompliances2 = new ArrayList<Incompliance>();
			for (Incompliance incompliance : incompliances1){
				if (incompliance.getFileName().equals(filename)){
					incompliances2.add(incompliance);
				}
			}
			//(2.2)Find out ESA table associated with the EDF file.
			ESATable esaTable2 = null;
			for (ESATable esaTable1 : esaTables){
				String src = esaTable1.getSourceMasterFile().getAbsoluteFile().getAbsolutePath();
				String wrk = map_src_with_wrk.get(src);
				if (wrk.equals(filename)){
					esaTable2 = esaTable1;
					break;
				}
			}
			//(2.3)Fix the errors associated with the chosen EDF_files and with the chosen Error_types.
			for (ErrorTypes errorType : errorTypeAL){
				switch(errorType){
					case phyMaxMin:
						fix01_SwapPhyMaxMin(incompliances2, esaTable2);
						break;
					case emptyVersion:
						break;
					case InvalidDateTimeSeparator:
						break;
				}
			}
			//
		}
		
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:3/3) Errors after applying bug-fixes:", true, MainWindow.log);
		(new MainWindow.VerifyHeaderListener()).verifyHeaders();
		ArrayList<Incompliance> incompliances3 = MainWindow.aggregateIncompliances();
		NewTask_for_ValidityCommandLine.addElementIntoLog("    Total errors are found: " + incompliances3.size(), true, MainWindow.log);
		
	}
	
//	public static void fixErrors2(ArrayList<String> selected_edf_files, ArrayList<ErrorTypes> errorTypeAL){
//		
//		String[] fileList = selected_edf_files.toArray(new String[selected_edf_files.size()]);
//		for (String filename : selected_edf_files){
//			
//			System.out.println(filename);
//			
//			ArrayList<Incompliance> incompliances2 = new ArrayList<Incompliance>();
//			try {
//				//(1) Validate EDF file, and generate a summary for all kinds of EDF errors
//				RandomAccessFile raf = new RandomAccessFile(filename, "rw");
//				File edfFile = new File(filename);
//				
//				EDFFileHeader srcFile = new EDFFileHeader(raf, edfFile, false);
//				EIATable eiaTable = new EIATable(srcFile);
//				ESATable esaTable = new ESATable(srcFile, true);
//				
//				ArrayList<Incompliance> eiaIncompliances = ValidateEDF.parseEIATable(eiaTable, fileList);
//				ArrayList<Incompliance> esaIncompliances = ValidateEDF.parseESATable(esaTable, filename);
//				
//				incompliances2.addAll(eiaIncompliances);
//				incompliances2.addAll(esaIncompliances);
//				
//
//				//(2)Fix certain errors
//				for (ErrorTypes errorType : errorTypeAL){
//					System.out.println(errorType.toString());
//					switch(errorType){
//						case phyMaxMin:
//							fix01_SwapPhyMaxMin(incompliances2, esaTable);
//							break;
//						case emptyVersion:
//							break;
//						case InvalidDateTimeSeparator:
//							break;
//					}
//				}
//				
//				
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//		}
//	}
	
	public static ArrayList<Incompliance> fix01_SwapPhyMaxMin(ArrayList<Incompliance> incompliances, ESATable esaTable){
		
		ArrayList<Incompliance> incompliances_solved = new ArrayList<Incompliance>();
		
		//ESA table
		final int COL_INDEX_PHYSICAL_MINIMUM = 3;
		final int COL_INDEX_PHYSICAL_MAXIMUM = 4;
		
		TableModel tableModel = esaTable.getModel();
		int row, col;
		for (Incompliance incompliance : incompliances){
			String des = incompliance.getDescription();
			if (incompliance.getDescription().equals(Incompliance.error_esa_phymaxmin)){
				
				incompliances_solved.add(incompliance);
				
				row = incompliance.getRowIndex();
				col = incompliance.getColumnIndex();
				
				String phy_Min = (String)tableModel.getValueAt(row, COL_INDEX_PHYSICAL_MINIMUM);
				String phy_Max = (String)tableModel.getValueAt(row, COL_INDEX_PHYSICAL_MAXIMUM);
				
				//Swap the phy_min and phy_max
				tableModel.setValueAt(phy_Max, row, COL_INDEX_PHYSICAL_MINIMUM);
				tableModel.setValueAt(phy_Min, row, COL_INDEX_PHYSICAL_MAXIMUM);
				
				String message = "";
				message += "   + Fixed error at [Row: " + row + ", Col: " + col + "]    " + des + "\r";
				message += "     Wrong Physical Min/Max:" + phy_Min + "/" + phy_Max + ";";
				message += "     Fixed Physical Min/Max:" + phy_Max + "/" + phy_Min + "";
				NewTask_for_ValidityCommandLine.addElementIntoLog(message, true, MainWindow.log);
			}
		}
		
		esaTable.setModel(tableModel);
		esaTable.repaint();
		
		return incompliances_solved;
	}
	
}


	
	
	