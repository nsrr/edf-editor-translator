package validator.fix;

import java.util.ArrayList;

import javax.swing.table.TableModel;

import table.ESATable;
import table.Incompliance;
import translator.utils.MyDate;
import editor.MainWindow;
import editor.NewTask_for_ValidityCommandLine;

/**
 * A Fix-error routine found in the headers
 */
public class ErrorFix {
	
	/**
	 * This procedure is used for fixing errors in the selected EDF files
	 * @param selected_edf_files the selected EDF files
	 * @param errorTypeAL a list of error types
	 */
	public static void fixErrors(ArrayList<String> selected_edf_files, ArrayList<ErrorTypes> errorTypeAL) {
		
		NewTask_for_ValidityCommandLine.addElementIntoLog("===============================================================", true, MainWindow.log);
		NewTask_for_ValidityCommandLine.addElementIntoLog("  => User start an error-fix task at " + MyDate.currentDateTime(), true, MainWindow.log);
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:1/3) Errors before applying bug-fixes:", true, MainWindow.log);
				
		//(1)Validate tables and have a list of errors
		(new MainWindow.VerifyHeaderListener()).verifyHeaders();
		ArrayList<Incompliance> incompliances1 = MainWindow.aggregateIncompliances();
		NewTask_for_ValidityCommandLine.addElementIntoLog("    Total errors are found: " + incompliances1.size(), true, MainWindow.log);
		ArrayList<ESATable> esaTables = MainWindow.getIniEsaTables();
		
		//(2)Apply error-fixes on selected edf files
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:2/3) Applying bug-fixes:", true, MainWindow.log);
		for (String filename : selected_edf_files) {
			
			String message = "";
			message += "   ------------------" + "\r\n";
			message += "   Fixed EDF file: " + filename;
			NewTask_for_ValidityCommandLine.addElementIntoLog(message, true, MainWindow.log);
			
			//(2.1)Find out incompliancess associated with the EDF file. 
			ArrayList<Incompliance> incompliances2 = new ArrayList<Incompliance>();
			for (Incompliance incompliance : incompliances1) {
				if (incompliance.getFileName().equals(filename)) {
					incompliances2.add(incompliance);
				}
			}
			//(2.2)Find out ESA table associated with the EDF file.
			ESATable esaTable2 = null;
			for (ESATable esaTable1 : esaTables) {
				String src = esaTable1.getSourceMasterFile().getAbsoluteFile().getAbsolutePath();
				if (src.equals(filename)) {
					esaTable2 = esaTable1;
					break;
				}
			}
			//(2.3)Fix the errors associated with the chosen EDF_files and with the chosen Error_types.
			for (ErrorTypes errorType : errorTypeAL) {
				switch(errorType) {
					case phyMaxMin:
						fix01_SwapPhyMaxMin(incompliances2, esaTable2);
						break;
					case emptyVersion:
						break;
					case InvalidDateTimeSeparator:
						break;
				}
			}
		}
		
		NewTask_for_ValidityCommandLine.addElementIntoLog("(Step:3/3) Errors after applying bug-fixes:", true, MainWindow.log);
		(new MainWindow.VerifyHeaderListener()).verifyHeaders();
		ArrayList<Incompliance> incompliances3 = MainWindow.aggregateIncompliances();
		NewTask_for_ValidityCommandLine.addElementIntoLog("    Total errors are found: " + incompliances3.size(), true, MainWindow.log);
		
	}
	
	/**
	 * Fixes error by swapping physical max and min value
	 * @param incompliances the incompliances 
	 * @param esaTable the ESA table that the Incompliances coming from
	 * @return a list of Incompliances solved
	 */
	public static ArrayList<Incompliance> fix01_SwapPhyMaxMin(ArrayList<Incompliance> incompliances, 
			ESATable esaTable) {
		
		ArrayList<Incompliance> incompliances_solved = new ArrayList<Incompliance>();
		
		//ESA table
		final int COL_INDEX_PHYSICAL_MINIMUM = 3;
		final int COL_INDEX_PHYSICAL_MAXIMUM = 4;
		
		TableModel tableModel = esaTable.getModel();
		int row, col;
		for (Incompliance incompliance : incompliances) {
			String des = incompliance.getDescription();
			if (incompliance.getDescription().equals(Incompliance.error_esa_phymaxmin)) {
				
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
