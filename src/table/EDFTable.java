package table;

import editor.Main;
import editor.MainWindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public class EDFTable extends JTable {

    private static final long serialVersionUID = 1L;
	
	protected Boolean savedOnce = false; // usage: if the body should be copied in current save
    protected Boolean updateSinceLastSave = false; //false when ESA/EIA table is created; true when template file is created
    protected File masterFile = null; //usage: keep the master file of the table
    protected int masterFileIndex; // index of the master file in the master file array
    
    //represent the category of the table's master header
    //obsolete
    protected int masterHeaderCategory;
    JTextField jtf = new JTextField();
    private boolean edfValid = true; // if all fields conform to EDF specification
    
    public static final Color tableHeaderForegroundClr = new Color(212, 220, 225);
    public static final Color tableHeaderBackgroundClr = new Color(118, 146, 60);
    public static final Color tableOddRowClr = new Color(205, 221, 172);
    public static final Color tableEvenRowClr = new Color(234, 241, 221);
    public static final Color stripBackgroundClr = new Color(102, 78, 130);
    public static final Color stripForegroundClr = new Color(255, 255, 219);    
    public static final Color selectedCellColor = new Color(57, 105, 138);
    
    static final String physical_maximum_nonnumber_error = "Physical Maximum field should contain scalar value. ";
    static final String physical_minimum_nonnumber_error = "Physical Minimum field should contain scalar value. ";
    static final String digital_order_error = "Digtial Maximum should be larger than Digital Minimum.";
    static final String digital_field_blank_error = "Digital field should contain integer value.";
    
    static final String start_date_error = "Start date needs to be in the form of xx.xx.xx where x are integers";
    static final String altName = (Main.mac_os)? "Command": "Alt";
    
    //to store the indices of immuatable cells
    //protected int immutableFieldIndices[];

    public void setMasterFileIndex(int masterFileIndex) {
        this.masterFileIndex = masterFileIndex;
    }


    public int getMasterFileIndex() {
        return masterFileIndex;
    }

    public void setEdfValid(boolean valid) {
        this.edfValid = valid;
    }

    public boolean isEdfValid() {
        return edfValid;
    }
    
    
    public static class MasterHeaderCategory {
        public static final int EIA_WORKSET = 0;
        public static final int ESA_WORKSET = 1;
        public static final int EIA_TEMPLATE = 2;
        public static final int ESA_TEMPLATE = 3;
    }

    public EDFTable() {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        renderHeader();
    }

    public EDFTable(TableModel tableModel) {
        super(tableModel);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        this.getTableHeader().setReorderingAllowed(false);
    }

    public EDFTable(Boolean forEIATemplate) {
        this.getTableHeader().setReorderingAllowed(false);
        this.stripTable(tableOddRowClr, null, tableEvenRowClr, null); 
        if (forEIATemplate == true)
            new JTable(new EIATableModel(1));
        else
            new JTable(new ESATemplateTableModel(1));
    }

    public void customizeLook(String dataType) {
        this.getTableHeader().setFont(new Font("Dialog", Font.PLAIN, 16));
        this.setRowHeight((int)(this.getRowHeight() * 2.0));
        this.setCellSelectionEnabled(true);
        this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        if (dataType.equals("eia")) {
            for (int i = 0; i < 2; i++) {
                this.stripTableInColumn(i, stripBackgroundClr,
                                        stripForegroundClr, stripBackgroundClr,
                                        stripForegroundClr);
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               0.7));
            }
            
            for (int i = 2; i < 5; i++) {
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               1.5));
            }

            for (int i = 5; i < 11; i++) {
                this.stripTableInColumn(i, stripBackgroundClr,
                                        stripForegroundClr, stripBackgroundClr,
                                        stripForegroundClr);
                TableColumn verCol = this.getColumnModel().getColumn(i);
                verCol.setPreferredWidth((int)(verCol.getPreferredWidth() *
                                               0.7));
            }
        } else if (dataType.equals("esa"))
            this.stripTableInColumn(8, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
        else if (dataType.equals("esa_template"))
            this.stripTableInColumn(9, stripBackgroundClr, stripForegroundClr,
                                    stripBackgroundClr, stripForegroundClr);
    }

    private void renderHeader() {
        int ncol = this.getColumnCount();
        for (int i = 0; i < ncol; i++) {
            TableColumn tc = this.getColumnModel().getColumn(i);
            tc.setHeaderRenderer(new EDFTableHeaderRenderer());
        }
    }

/*
 * this function has been reformed in Utility. They have the same functionality
 * Fangping, 09/28/2010
 */
    public void scrollToVisible(int rr, int cc) {
        if (!(getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)getParent();
        Rectangle rect = getCellRect(rr, cc, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }

    public void setSavedOnce(Boolean savedEver) {
        this.savedOnce = savedEver;
    }
    
    public boolean isTableColumnHidden(){
        return (getModel().getColumnCount() != getColumnModel().getColumnCount());
    }

    public Boolean getSavedOnce() {
        return savedOnce;
    }

    public void setUpdateSinceLastSave(Boolean updateSinceLastSave) {
        this.updateSinceLastSave = updateSinceLastSave;
    }

    public Boolean getUpdateSinceLastSave() {
        return updateSinceLastSave;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public File getMasterFile() {
/*         if (this.masterHeaderCategory == MasterHeaderCategory.EIA_WORKSET)
            return null; */
        return masterFile;
    }

    /**
     * obsolete method
     * @param masterHeaderCategory
     */
    public void setMasterHeaderCategory(int masterHeaderCategory) {
        this.masterHeaderCategory = masterHeaderCategory;
    }

    /**
     * obsolete method
     * @return
     */
    public int getMasterHeaderCategory() {
        return masterHeaderCategory;
    }

    /**
     * update all the four attributes.
     * @param saved saved once or not
     * @param updated updated or not since last save
     * @param masterFile master file of the table
     * @param categoryValue categroy of the table
     */
    public void setStatesAllInOne(Boolean saved, Boolean updated,
                                  File masterFile, int categoryValue,
                                  int indexofmasterFile) {
        this.setSavedOnce(saved);
        this.setUpdateSinceLastSave(updated);
        this.setMasterFile(masterFile);
        this.setMasterHeaderCategory(categoryValue);
        this.setMasterFileIndex(indexofmasterFile);
    }
    
    /**
     * used for both ESA and ESA template tables
     * Fangping, 09/29/2010
     * 
     * Validation improvement for ESA tables
     * Gang Shu, 02/20/2014
     */
	public ArrayList<Incompliance> parseESATable() {
		
		ArrayList<Incompliance> esaIncompliances = new ArrayList<Incompliance>();
		String incomplianceType;
		int errorSrcTypeIndex = -1;
		int nrow = this.getRowCount();
		Incompliance incomp;
		String description;
		String fileName = this.getMasterFile().getAbsolutePath();
		boolean bASCII;
		int col;
		
		/////
		int COL_INDEX_LABEL;
		int COL_INDEX_CORRECTED_LABEL = -1;
		int COL_INDEX_TRANSDUCER_TYPE;
		int COL_INDEX_PHYSICAL_DIMENSION;
		int COL_INDEX_PHYSICAL_MINIMUM;
		int COL_INDEX_PHYSICAL_MAXIMUM;
		int COL_INDEX_DIGITAL_MINIMUM;
		int COL_INDEX_DIGITAL_MAXIMUM;
		int COL_INDEX_PREFILTERING;
		int COL_INDEX_NR_OF_SAMPLES;
		int COL_INDEX_RESERVED;
		/////
		
		if (this instanceof ESATable) {
			errorSrcTypeIndex = Incompliance.index_incomp_src_esa;
			incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex];
			
			//Check "ESA Table" fields
			COL_INDEX_LABEL = 0;
			COL_INDEX_TRANSDUCER_TYPE = 1;
			COL_INDEX_PHYSICAL_DIMENSION = 2;
			COL_INDEX_PHYSICAL_MINIMUM = 3;
			COL_INDEX_PHYSICAL_MAXIMUM = 4;
			COL_INDEX_DIGITAL_MINIMUM = 5;
			COL_INDEX_DIGITAL_MAXIMUM = 6;
			COL_INDEX_PREFILTERING = 7;
			COL_INDEX_NR_OF_SAMPLES = 8;
			COL_INDEX_RESERVED = 9;
		}
		else if (this instanceof ESATemplateTable) {
			errorSrcTypeIndex = Incompliance.index_incomp_src_esatemplate;
			incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex];
			
			//Check "ESA Template" fields
			COL_INDEX_LABEL = 0;
			COL_INDEX_CORRECTED_LABEL = 1;
			COL_INDEX_TRANSDUCER_TYPE = 2;
			COL_INDEX_PHYSICAL_DIMENSION = 3;
			COL_INDEX_PHYSICAL_MINIMUM = 4;
			COL_INDEX_PHYSICAL_MAXIMUM = 5;
			COL_INDEX_DIGITAL_MINIMUM = 6;
			COL_INDEX_DIGITAL_MAXIMUM = 7;
			COL_INDEX_PREFILTERING = 8;
			COL_INDEX_NR_OF_SAMPLES = 9;
			COL_INDEX_RESERVED = 10;
		}
		else{
			return esaIncompliances; // at this time, esaIncompliances.size() = 0;
		}
		
		for (int i = 0; i < nrow; i++) {
			
			/************************************************************
			 * ns * 16 ascii : ns * label (e.g. EEG Fpz-Cz or Body temp)
			 ************************************************************/
			col = COL_INDEX_LABEL;
			String alabel = (String) getModel().getValueAt(i, col);
			if (alabel==null || alabel.equals("")){
				//[Label](K.3) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Label](K.1) check for ascii 
				bASCII = checkAsciiF(alabel);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				
				//[Label](K.2) no duplicate signal labels
				boolean repeated = false;
				description = Incompliance.error_esa_label + (i + 1);
				for (int j = i + 1; j < nrow; j++) {
					String blabel = (String) getModel().getValueAt(j, col);
					if (alabel.equalsIgnoreCase(blabel)) {
						repeated = true;
						description = description + ", " + (j + 1);
					}
				}
				if (repeated){
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}
			
			if (this instanceof ESATemplateTable && COL_INDEX_CORRECTED_LABEL != -1) {
				col = COL_INDEX_CORRECTED_LABEL;
				String alabel2 = (String) getModel().getValueAt(i, col);
				if (alabel2==null || alabel2.equals("")){
					//[Label](K.3) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Label](K.1) check for ascii 
					bASCII = checkAsciiF(alabel2);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
					
					//[Label](K.2) no duplicate signal labels
					boolean repeated = false;
					description = Incompliance.error_esa_label + (i + 1);
					for (int j = i + 1; j < nrow; j++) {
						String blabel = (String) getModel().getValueAt(j, col);
						if (alabel2.equalsIgnoreCase(blabel)) {
							repeated = true;
							description = description + ", " + (j + 1);
						}
					}
					if (repeated){
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}
			
			
			/************************************************************
			 * ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode) 
			 ************************************************************/
			col = COL_INDEX_TRANSDUCER_TYPE;
			String transducer_type = (String) getModel().getValueAt(i, col);
			if (transducer_type==null || transducer_type.equals("")){
				//[Transducer_Type](L.2) can be empty field
			}
			else{
				//[Transducer_Type](L.1) check for ascii
				bASCII = checkAsciiF(transducer_type);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}

			/************************************************************
			 * ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC) 
			 ************************************************************/
			col = COL_INDEX_PHYSICAL_DIMENSION;
			String physical_dimension = (String) getModel().getValueAt(i, col);
			if (physical_dimension==null || physical_dimension.equals("")){
				//[Physical_Dimension](M.2) can be empty field
			}
			else{
				//[Physical_Dimension](M.1) check for ascii
				bASCII = checkAsciiF(physical_dimension);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}

			/************************************************************
			 * ns * 8 ascii : ns * physical minimum (e.g. -500 or 34) 
			 ************************************************************/
			col = COL_INDEX_PHYSICAL_MINIMUM;
			String physical_minimum = (String) getModel().getValueAt(i, col);
			boolean bGood_physical_minimum = false;
			if (physical_minimum==null || physical_minimum.equals("")){
				//[Physical_Minimum](N.5) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Physical_Minimum](N.1) check for ascii
				bASCII = checkAsciiF(physical_minimum);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Minimum](N.2) is a floating point number
					try{
						Float.parseFloat(physical_minimum);
						bGood_physical_minimum = true;
					} catch (NumberFormatException e) {
						description = Incompliance.error_esa_phymin;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * ns * 8 ascii : ns * physical maximum (e.g. 500 or 40) 
			 ************************************************************/
			col = COL_INDEX_PHYSICAL_MAXIMUM;
			String physical_maximum = (String) getModel().getValueAt(i, col);
			boolean bGood_physical_maximum = false;
			if (physical_maximum==null || physical_maximum.equals("")){
				//[Physical_Maximum](O.4) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Physical_Maximum](O.1) check for ascii
				bASCII = checkAsciiF(physical_maximum);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Maximum](O.2) is a floating point number
					try{
						Float.parseFloat(physical_maximum);
						bGood_physical_maximum = true;
					} catch (NumberFormatException e) {
						description = Incompliance.error_esa_phymax;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}
			
			//[Physical_Maximum](O.3) physical maximum NOT = physical minimum
			if (bGood_physical_minimum && bGood_physical_maximum){
				if (Float.parseFloat(physical_minimum) == Float.parseFloat(physical_maximum)){
					description = Incompliance.error_esa_phy_equal;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}

			/************************************************************
			 * ns * 8 ascii : ns * digital minimum (e.g. -2048)
			 ************************************************************/
			col = COL_INDEX_DIGITAL_MINIMUM;
			String digital_minimum = (String) getModel().getValueAt(i, col);
			boolean bGood_digital_minimum = false;
			if (digital_minimum==null || digital_minimum.equals("")){
				//[Digital_Minimum](P.4) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Digital_Minimum](P.1) check for ascii
				bASCII = checkAsciiF(digital_minimum);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Minimum](P.2) is an integer
					try{
						int dig_minimum = Integer.parseInt(digital_minimum);
						bGood_digital_minimum = true;
						
						//[Digital_Minimum](P.3) since each date sample is a 2-byte integer, range check [-32768,32767]
						boolean bRange = check_digital_range(dig_minimum);
						if (!bRange){
							description = Incompliance.error_esa_digrange;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}catch (NumberFormatException e) {
						description = Incompliance.error_esa_digmin;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}

			/************************************************************
			 * ns * 8 ascii : ns * digital maximum (e.g. 2047) 
			 ************************************************************/
			col = COL_INDEX_DIGITAL_MAXIMUM;
			String digital_maximum = (String) getModel().getValueAt(i, col);
			boolean bGood_digital_maximum = false;
			if (digital_maximum==null || digital_maximum.equals("")){
				//[Digital_Maximum](Q.4) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Digital_Maximum](Q.1) check for ascii
				bASCII = checkAsciiF(digital_maximum);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Maximum](Q.2) is an integer
					try{
						int dig_maximum = Integer.parseInt(digital_maximum);
						bGood_digital_maximum = true;
						
						//[Digital_Maximum](Q.3) since each date sample is a 2-byte integer, range check [-32768,32767]
						boolean bRange = check_digital_range(dig_maximum);
						if (!bRange){
							description = Incompliance.error_esa_digrange;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}catch (NumberFormatException e) {
						description = Incompliance.error_esa_digmax;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}

			if (bGood_digital_minimum && bGood_digital_maximum){
				int dig_minimum = Integer.parseInt(digital_minimum);
				int dig_maximum = Integer.parseInt(digital_maximum);
				
				//[Digital_Minimum](Q.6) digital minimum NOT = digital maximum (division-by-0 condition)
				if (dig_minimum == dig_maximum){
					description = Incompliance.error_esa_dig_equal;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				//[Digital_Minimum](Q.5) digital minimum < digital maximum
				else if (dig_minimum > dig_maximum){
					description = Incompliance.error_esa_digmaxmin;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
					
				}
			}

			/************************************************************
			 * ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz) 
			 ************************************************************/
			col = COL_INDEX_PREFILTERING;
			String prefiltering = (String) getModel().getValueAt(i, col);
			if (prefiltering==null || prefiltering.equals("")){
				//[Prefiltering](R.2) can be empty field
			}
			else{
				//[Prefiltering](R.1) check for ascii
				bASCII = checkAsciiF(prefiltering);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}
			
			/************************************************************
			 * ns * 8 ascii : ns * nr of samples in each data record 
			 ************************************************************/
			col = COL_INDEX_NR_OF_SAMPLES;
			String num_signals = (String) getModel().getValueAt(i, col);
			if (num_signals==null || num_signals.equals("")){
				//[Num_signals](S.4) cannot be empty field
				description = Incompliance.error_esa_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				esaIncompliances.add(incomp);
			}
			else{
				//[Num_signals](S.1) check for ascii
				bASCII = checkAsciiF(num_signals);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Num_signals](S.2) is an integer
					try{
						int n_signals = Integer.parseInt(num_signals);
						//[Num_signals](S.3) value is greater than 0
						if (n_signals <= 0){
							description = Incompliance.error_esa_nrSig_range;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}catch (NumberFormatException e) {
						description = Incompliance.error_esa_nrSig;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * ns * 32 ascii : ns * reserved
			 ************************************************************/
			col = COL_INDEX_RESERVED;
			String reserved = (String) getModel().getValueAt(i, col);
			if (reserved==null || reserved.equals("")){
				//[Reserved](T.2) can be empty field
			}
			else{
				//[Reserved](T.1) check for ascii
				bASCII = checkAsciiF(reserved);
				if (!bASCII) {
					description = Incompliance.error_esa_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
			}
			
		}//for loop ends
		
		
		if (esaIncompliances != null && esaIncompliances.size() > 0){
			setEdfValid(false);
		}
		
		return esaIncompliances;
	}
    
	/************************************************************** 
	 * The below code was made by Gang Shu on February 18, 2014
	 **************************************************************/ 
    private boolean checkAsciiF(String text){
    	if (text==null){
    		return false;
    	}
    	else{
    		return text.matches("\\A\\p{ASCII}*\\z");
    	}
    }
    
    private boolean check_digital_range(int x){
    	return x >= -32768 && x <= 32767;
    }
	/************************************************************** 
	 * The above code was made by Gang Shu on February 18, 2014
	 **************************************************************/ 
      
    protected final int[] lowerbounds = {0, 0, 0};
    protected final int[] upperbounds = {31, 12, 99}; // 31 day, 12 month, 2099 years
     
    /**
     * TODO: [Validation] parse EIA table.
     * this one can be used on both EIATable
     */
    public ArrayList<Incompliance> parseEIATable( ) {
    	
    	ArrayList<Incompliance> eiaIncompliances = new ArrayList<Incompliance>();

    	final int errorSrcTypeIndex = Incompliance.index_incomp_src_eia;
    	final String incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex];
    	Incompliance incomp;
    	String description;
    	boolean bASCII;
    	
        final int COL_INDEX_VERSION = 0;
        final int COL_INDEX_LOCAL_PATIENT_ID = 1;
        final int COL_INDEX_LOCAL_RECORDING_ID  = 2;
        final int COL_INDEX_START_DATE = 3;
        final int COL_INDEX_START_TIME = 4;
        final int COL_INDEX_NUMBER_OF_BYTES_IN_HEADER_RECORD = 5;
        final int COL_INDEX_RESERVED = 6;
        final int COL_INDEX_NUMBER_OF_DATA_RECORDS = 7;
        final int COL_INDEX_DURATION_OF_A_DATA_RECORD = 8;
        final int COL_INDEX_NUMBER_OF_SIGNALS_IN_DATA_RECORD = 9;
        
        int nrow = this.getRowCount();
        int col;
        for (int i = 0; i < nrow; i++) {

        	String fileName = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
        			
			/************************************************************
			 * 8 ascii : version of this data format (0) 
			 ************************************************************/
        	col = COL_INDEX_VERSION + 1;
			String version = (String) getModel().getValueAt(i, col);
			if (version==null || version.equals("")){
				//[Version](A.3) cannot be empty field
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Version](A.1) check for ascii
				bASCII = checkAsciiF(version);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Version](A.2) is equal to 0
					try{
						int ver = Integer.parseInt(version);
						if (ver != 0){
							description = Incompliance.error_eia_version;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}catch (NumberFormatException e) {
						description = Incompliance.error_eia_version;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * 80 ascii : local patient identification
			 ************************************************************/
			col = COL_INDEX_LOCAL_PATIENT_ID + 1;
			String partient_id = (String) getModel().getValueAt(i, col);
			if (partient_id==null || partient_id.equals("")){
				//[Partient_id](B.2) can be empty field
			}
			else{
				//[Partient_id](B.1) check for ascii
				bASCII = checkAsciiF(partient_id);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
			}

			/************************************************************
			 * 80 ascii : local recording identification
			 ************************************************************/
			col = COL_INDEX_LOCAL_RECORDING_ID + 1;
			String recording_id = (String) getModel().getValueAt(i, col);
			if (recording_id==null || recording_id.equals("")){
				//[Recording_id](C.2) can be empty field
			}
			else{
				//[Recording_id](C.1) check for ascii
				bASCII = checkAsciiF(recording_id);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
			}
			
			/************************************************************
			 * 8 ascii : startdate of recording (dd.mm.yy) 
			 ************************************************************/
			col = COL_INDEX_START_DATE + 1;
			String startdate = (String) getModel().getValueAt(i, col);
			if (startdate==null || startdate.equals("")){
				//[Startdate](D.2) cannot be empty field
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Startdate](D.1) check for ascii
				bASCII = checkAsciiF(startdate);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Startdate](D.4) separator between digits should be only ‘period’
					String[] items = startdate.split("\\.");
					if (items.length != 3){
						 description = Incompliance.error_eia_dateformat;
						 incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
						 eiaIncompliances.add(incomp);
					}
					else{
						//[Startdate](D.3) dd:00-31, mm:00-12, yy:00-99
						try{
							int dd = Integer.parseInt(items[0]);
							int mm = Integer.parseInt(items[1]);
							int yy = Integer.parseInt(items[2]);
							if (dd >=0 && dd <=31 && mm >= 0 && mm <= 12 && yy >= 00 && yy <= 99){
								//valid date format
							}
							else{
								description = Incompliance.error_eia_daterange;
								incomp = new Incompliance(incomplianceType, description,
										fileName, i, col, errorSrcTypeIndex);
								eiaIncompliances.add(incomp);
							}
						}
						catch (NumberFormatException e) {
							description = Incompliance.error_eia_daterange;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
				}
			}
			
			/************************************************************
			 * 8 ascii : starttime of recording (hh.mm.ss) 
			 ************************************************************/
			col = COL_INDEX_START_TIME + 1;
			String starttime = (String) getModel().getValueAt(i, col);
			if (starttime==null || starttime.equals("")){
				//[Start-time](E.2) cannot be empty field
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Start-time](E.1) check for ascii
				bASCII = checkAsciiF(starttime);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Start-time](E.4) separator between digits should be only ‘period’
					String[] items = starttime.split("\\.");
					if (items.length != 3){
						 description = Incompliance.error_eia_timeformat;
						 incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
						 eiaIncompliances.add(incomp);
					}
					else{
						//[Start-time](E.3) hh:00-23, mm:00-59, ss:00-59
						try{
							int hh = Integer.parseInt(items[0]);
							int mm = Integer.parseInt(items[1]);
							int ss = Integer.parseInt(items[2]);
							if (hh >=0 && hh <=23 && mm >= 0 && mm <= 59 && ss >= 00 && ss <= 59){
								//valid time format
							}
							else{
								description = Incompliance.error_eia_timerange;
								incomp = new Incompliance(incomplianceType, description,
										fileName, i, col, errorSrcTypeIndex);
								eiaIncompliances.add(incomp);
							}
						}
						catch (NumberFormatException e) {
							description = Incompliance.error_eia_timerange;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
				}
			}
			
			/************************************************************
			 * 8 ascii : number of bytes in header record 
			 ************************************************************/
			col = COL_INDEX_NUMBER_OF_BYTES_IN_HEADER_RECORD + 1;
			String nBytes = (String) getModel().getValueAt(i, col);
			if (nBytes==null || nBytes.equals("")){
				//[Number_of_bytes](F.2) should not be empty
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Number_of_bytes](F.1) check for ascii
				bASCII = checkAsciiF(nBytes);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Number_of_bytes](F.3) is an integer
					try{
						int nbytes = Integer.parseInt(nBytes);
						if (nbytes <= 0){
							description = Incompliance.error_eia_num_bytes;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
					catch (NumberFormatException e) {
						description = Incompliance.error_eia_num_bytes;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * 44 ascii : reserved 
			 ************************************************************/
			col = COL_INDEX_RESERVED + 1;
			String reserved = (String) getModel().getValueAt(i, col);
			if (reserved==null || reserved.equals("")){
				//[Reserved](G.2) can be empty field
			}
			else{
				//[Reserved](G.1) check for ascii
				bASCII = checkAsciiF(reserved);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
			}
			
			/************************************************************
			 * 8 ascii : number of data records (-1 if unknown)
			 ************************************************************/
			col = COL_INDEX_NUMBER_OF_DATA_RECORDS + 1;
			String nDataRecords = (String) getModel().getValueAt(i, col);
			if (nDataRecords==null || nDataRecords.equals("")){
				//[Num_of_DataRecords](H.2) should not be empty
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Num_of_DataRecords](H.1) check for ascii
				bASCII = checkAsciiF(nDataRecords);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Num_of_DataRecords](H.3) is a positive integer
					try{
						int ndatarecords = Integer.parseInt(nDataRecords);
						if (ndatarecords > 0 || ndatarecords == -1){
							//valid values
						}
						else{
							description = Incompliance.error_eia_num_records;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
					catch (NumberFormatException e) {
						description = Incompliance.error_eia_num_records;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * 8 ascii : duration of a data record, in seconds 
			 ************************************************************/
			col = COL_INDEX_DURATION_OF_A_DATA_RECORD + 1;
			String duration = (String) getModel().getValueAt(i, col);
			if (duration==null || duration.equals("")){
				//[Duration_of_a_data_record](I.2) should not be empty field
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Duration_of_a_data_record](I.1) check for ascii
				bASCII = checkAsciiF(duration);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Duration_of_a_data_record](I.3) is a positive floating point number (eg. 1, 0.2, 0.001)
					try{
						float dur = Float.parseFloat(duration);
						if (dur < 0){
							description = Incompliance.error_eia_duration;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
					catch (NumberFormatException e) {
						description = Incompliance.error_eia_duration;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
			
			/************************************************************
			 * 4 ascii : number of signals (ns) in data record
			 ************************************************************/
			col = COL_INDEX_NUMBER_OF_SIGNALS_IN_DATA_RECORD + 1;
			String nSignals = (String) getModel().getValueAt(i, col);
			if (nSignals==null || nSignals.equals("")){
				//[Number_of_signals](J.2) cannot be empty
				description = Incompliance.error_eia_empty;
				incomp = new Incompliance(incomplianceType, description,
						fileName, i, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Number_of_signals](J.1) check for ascii
				bASCII = checkAsciiF(nSignals);
				if (!bASCII) {
					description = Incompliance.error_eia_ascii;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, col, errorSrcTypeIndex);
					eiaIncompliances.add(incomp);
				}
				else{
					//[Number_of_signals](J.3) is a positive integer
					try{
						int nsignals = Integer.parseInt(nSignals);
						if (nsignals <= 0){
							description = Incompliance.error_eia_nsignals;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
					catch (NumberFormatException e) {
						description = Incompliance.error_eia_nsignals;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
			
        }//for loop ends
        
        if (eiaIncompliances != null && eiaIncompliances.size() > 0){
        	setEdfValid(false);        	
        }
        
        return eiaIncompliances;    
    }
    
    public ArrayList<Incompliance> parseEIATemplateTable(){
    	
    	ArrayList<Incompliance> eiaIncompliances = new ArrayList<Incompliance>();

    	final int errorSrcTypeIndex = Incompliance.index_incomp_src_eiatemplate;
    	final String incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex];
    	Incompliance incomp;
    	String description;
    	boolean bASCII;
        String fileName = this.getMasterFile().getPath();
        int row = 0, col;
    	
        final int COL_INDEX_LOCAL_PATIENT_ID = 2;
        final int COL_INDEX_LOCAL_RECORDING_ID  = 3;
        final int COL_INDEX_START_DATE = 4;
    	
        /************************************************************
		 * 80 ascii : local patient identification
		 ************************************************************/
        col = 0;
		String partient_id = ((String)this.getModel().getValueAt(row, COL_INDEX_LOCAL_PATIENT_ID));
		if (partient_id==null || partient_id.equals("")){
			//[Partient_id](B.2) can be empty field
		}
		else{
			//[Partient_id](B.1) check for ascii
			bASCII = checkAsciiF(partient_id);
			if (!bASCII) {
				description = Incompliance.error_eia_ascii;
				incomp = new Incompliance(incomplianceType, description,
						fileName, row, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
		}

		/************************************************************
		 * 80 ascii : local recording identification
		 ************************************************************/
		col = 1;
		String recording_id = ((String)this.getModel().getValueAt(row, COL_INDEX_LOCAL_RECORDING_ID));
		if (recording_id==null || recording_id.equals("")){
			//[Recording_id](C.2) can be empty field
		}
		else{
			//[Recording_id](C.1) check for ascii
			bASCII = checkAsciiF(recording_id);
			if (!bASCII) {
				description = Incompliance.error_eia_ascii;
				incomp = new Incompliance(incomplianceType, description,
						fileName, row, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
		}
		
		/************************************************************
		 * 8 ascii : startdate of recording (dd.mm.yy) 
		 ************************************************************/
		col = 2;
		String startdate = ((String)this.getModel().getValueAt(row, COL_INDEX_START_DATE)).trim();
		if (startdate==null || startdate.equals("")){
			//[Startdate](D.2) cannot be empty field
			description = Incompliance.error_eia_empty;
			incomp = new Incompliance(incomplianceType, description,
					fileName, row, col, errorSrcTypeIndex);
			eiaIncompliances.add(incomp);
		}
		else{
			//[Startdate](D.1) check for ascii
			bASCII = checkAsciiF(startdate);
			if (!bASCII) {
				description = Incompliance.error_eia_ascii;
				incomp = new Incompliance(incomplianceType, description,
						fileName, row, col, errorSrcTypeIndex);
				eiaIncompliances.add(incomp);
			}
			else{
				//[Startdate](D.4) separator between digits should be only ‘period’
				String[] items = startdate.split("\\.");
				if (items.length != 3){
					 description = Incompliance.error_eia_dateformat;
					 incomp = new Incompliance(incomplianceType, description,
								fileName, row, col, errorSrcTypeIndex);
					 eiaIncompliances.add(incomp);
				}
				else{
					//[Startdate](D.3) dd:00-31, mm:00-12, yy:00-99
					try{
						int dd = Integer.parseInt(items[0]);
						int mm = Integer.parseInt(items[1]);
						int yy = Integer.parseInt(items[2]);
						if (dd >=0 && dd <=31 && mm >= 0 && mm <= 12 && yy >= 00 && yy <= 99){
							//valid date format
						}
						else{
							description = Incompliance.error_eia_daterange;
							incomp = new Incompliance(incomplianceType, description,
									fileName, row, col, errorSrcTypeIndex);
							eiaIncompliances.add(incomp);
						}
					}
					catch (NumberFormatException e) {
						description = Incompliance.error_eia_daterange;
						incomp = new Incompliance(incomplianceType, description,
								fileName, row, col, errorSrcTypeIndex);
						eiaIncompliances.add(incomp);
					}
				}
			}
		}
        
    	
        if (eiaIncompliances != null && eiaIncompliances.size() > 0){
        	setEdfValid(false);        	
        }
        
        return eiaIncompliances;  
    }
    
//    public ArrayList<Incompliance> parseEIATemplateTable(){
//        ArrayList<Incompliance> eiaTemplateIncomps = new ArrayList<Incompliance>();
//        final int srcTypeIndex = Incompliance.index_incomp_src_eiatemplate;        
//        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
//        Incompliance incomp;
//        String description;
//        String fileName = this.getMasterFile().getPath();
//        int nrow = 1;
//        
//        int rowIndex, columnIndex = 2;
//        int segnumber = 3;
//        int seg[] = new int[segnumber];
//        String[] descriptions = {Incompliance.warning_eia_day, 
//                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
//        
//       // boolean cellHiding = true;
//        
//        String sd;
//        for (int i = 0; i < nrow; i++) {
//            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
//            fileName = this.getMasterFile().getAbsolutePath();
//            rowIndex = i;
//            String[] psd = sd.split("\\.");
//            
//            //1. check format of dd.mm.yy
//            int psdlen = psd.length;
//            if (psdlen != segnumber){
//                setEdfValid(false);
//                description = Incompliance.error_eia_dateformat;
//                incomp = new Incompliance( incomplianceType, description, 
//                                           fileName, rowIndex, columnIndex, srcTypeIndex);
//                eiaTemplateIncomps.add(incomp);
//                continue;
//            } 
//            
//            //2. check each segment of startdate is number or not
//            try{
//                for (int j = 0; j < segnumber; j++)
//                    seg[j] = Integer.parseInt(psd[j]);
//            }
//            catch(NumberFormatException e){
//                setEdfValid(false);
//                description = Incompliance.error_eia_dmyint;
//                incomp = new Incompliance(incomplianceType, description, 
//                                          fileName, rowIndex, columnIndex, srcTypeIndex);
//                  eiaTemplateIncomps.add(incomp);
//                continue;
//            }
//            
//            //3. check segment valid
//            for (int j = 0; j < psdlen; j++) {   
//                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
//                    setEdfValid(false);
//                    description = descriptions[j];
//                    incomp = new Incompliance(incomplianceType, description, 
//                                              fileName, rowIndex, columnIndex, srcTypeIndex);
//                     eiaTemplateIncomps.add(incomp);
//                }
//            }
//        }        
//        
//        return eiaTemplateIncomps;
//    }
    
    //stub method
    //further implementation in EIATable and ESATable
    public void showImmutableFields(){
       ;
    }
    
    //stub method
    //further implementation in EIATable and ESATable
    public void hideImmutableFields(){
       ;
    }
    
    //Implement table cell tool tips.

    public String getToolTipText(MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        
        if (realColumnIndex == 1)
            tip = altName + "key + Mouse right-click to customize transducer type";
        else
            tip = super.getToolTipText(e);
        
        return tip;
    }
   
    class StripedTableCellRenderer implements TableCellRenderer {
        
        protected TableCellRenderer targetRenderer;
        protected Color evenBack;
        protected Color evenFore;
        protected Color oddBack;
        protected Color oddFore;
        
      public StripedTableCellRenderer(TableCellRenderer targetRenderer,
          Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
        this.targetRenderer = targetRenderer;
        this.evenBack = evenBack;
        this.evenFore = evenFore;
        this.oddBack = oddBack;
        this.oddFore = oddFore;
      }

      // Implementation of TableCellRenderer interface
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer renderer = targetRenderer;
        if (renderer == null) {
          renderer = table.getDefaultRenderer(table.getColumnClass(column));
        }

        Component comp = renderer.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);

        if (isSelected == false && hasFocus == false) {
          if ((row & 1) == 0) {
            comp.setBackground(evenBack != null ? evenBack : table
                .getBackground());
            comp.setForeground(evenFore != null ? evenFore : table
                .getForeground());
          } else {
            comp.setBackground(oddBack != null ? oddBack : table
                .getBackground());
            comp.setForeground(oddFore != null ? oddFore : table
                .getForeground());
          }
        }
        
        return comp;
      }
    }
    
    // Convenience method to apply this renderer to single column
    public void stripTableInColumn(int columnIndex, Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
      TableColumn tc = this.getColumnModel().getColumn(columnIndex);
      TableCellRenderer targetRenderer = tc.getCellRenderer();
      
      tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
          evenBack, evenFore, oddBack, oddFore));   
    }
    
    public void stripTable(Color evenBack, Color evenFore, Color oddBack, Color oddFore) {
      StripedTableCellRenderer sharedInstance = null;
      int columns = this.getColumnCount();
      for (int i = 0; i < columns; i++) {
        TableColumn tc = this.getColumnModel().getColumn(i);
        TableCellRenderer targetRenderer = tc.getCellRenderer();
        if (targetRenderer != null) {
          tc.setCellRenderer(new StripedTableCellRenderer(targetRenderer,
              evenBack, evenFore, oddBack, oddFore));
        } else {
          if (sharedInstance == null) {
            sharedInstance = new StripedTableCellRenderer(null,
                evenBack, evenFore, oddBack, oddFore);
          }
          tc.setCellRenderer(sharedInstance);
        }
      }
    }
    
    class EDFUndoableEditListener implements UndoableEditListener{
        public EDFUndoableEditListener(){
            super();
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            MainWindow.getUndoManager().addEdit(e.getEdit());
           if (MainWindow.getUndoManager().canUndo()) {
                MainWindow.getEditUndoItem().setEnabled(true);
                MainWindow.getUndoButton().setEnabled(true);
            } else {
                MainWindow.getEditUndoItem().setEnabled(false);
                MainWindow.getUndoButton().setEnabled(false);
            }

            if (MainWindow.getUndoManager().canRedo()) {
                MainWindow.getEditRedoItem().setEnabled(true);
                MainWindow.getRedoButton().setEnabled(true);
            } else {
                MainWindow.getEditRedoItem().setEnabled(false);
                MainWindow.getRedoButton().setEnabled(false);
            }
        }
    }
    

    private class EDFTableHeaderRenderer extends JLabel implements TableCellRenderer{

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, // column header value 
                                                       boolean isSelected, // always set to false
                                                       boolean hasFocus, // always set to false
                                                       int row /* row is always -1 */, int column) {
            
            setText(value.toString());
            this.setOpaque(false);
            this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            this.setForeground(tableHeaderForegroundClr);
            this.setBackground(tableHeaderBackgroundClr);
            
            return this;
        }
        
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String ptyName, Object oldValue, Object newValue){}
        public void firePropertyChange(String ptyName, boolean oldValue, boolean newValue){}
    }
}
