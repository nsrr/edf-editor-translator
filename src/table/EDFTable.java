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
    
    static final int START_DATE_INDEX = 4;
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
    
    /*
     * used for both ESA and ESA template tables
     * Fangping, 09/29/2010
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
		
		if (this instanceof ESATable) {
			errorSrcTypeIndex = Incompliance.index_incomp_src_esa;
			incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex]; // "ESA"
			
			//Check "ESA Table" fields
			final int COL_INDEX_LABEL = 0;
			final int COL_INDEX_TRANSDUCER_TYPE = 1;
			final int COL_INDEX_PHYSICAL_DIMENSION = 2;
			final int COL_INDEX_PHYSICAL_MINIMUM = 3;
			final int COL_INDEX_PHYSICAL_MAXIMUM = 4;
			final int COL_INDEX_DIGITAL_MINIMUM = 5;
			final int COL_INDEX_DIGITAL_MAXIMUM = 6;
			final int COL_INDEX_PREFILTERING = 7;
			final int COL_INDEX_NR_OF_SAMPLES = 8;
			final int COL_INDEX_RESERVED = 9;
			
			for (int i = 0; i < nrow; i++) {
				
				/************************************************************
				 * ns * 16 ascii : ns * label (e.g. EEG Fpz-Cz or Body temp)
				 ************************************************************/
				String alabel = (String) getModel().getValueAt(i, COL_INDEX_LABEL);
				if (alabel==null || alabel.equals("")){
					//[Label](K.3) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_LABEL, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Label](K.1) check for ascii 
					bASCII = checkAsciiF(alabel);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_LABEL, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
					
					//[Label](K.2) no duplicate signal labels
					boolean repeated = false;
					description = Incompliance.error_esa_label + (i + 1);
					for (int j = i + 1; j < nrow; j++) {
						String blabel = (String) getModel().getValueAt(j, COL_INDEX_LABEL);
						if (alabel.equalsIgnoreCase(blabel)) {
							repeated = true;
							description = description + ", " + (j + 1);
						}
					}
					if (repeated){
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_LABEL, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}

				/************************************************************
				 * ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode) 
				 ************************************************************/
				String transducer_type = (String) getModel().getValueAt(i, COL_INDEX_TRANSDUCER_TYPE);
				if (transducer_type==null || transducer_type.equals("")){
					//[Transducer_Type](L.2) can be empty field
				}
				else{
					//[Transducer_Type](L.1) check for ascii
					bASCII = checkAsciiF(transducer_type);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_TRANSDUCER_TYPE, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC) 
				 ************************************************************/
				String physical_dimension = (String) getModel().getValueAt(i, COL_INDEX_PHYSICAL_DIMENSION);
				if (physical_dimension==null || physical_dimension.equals("")){
					//[Physical_Dimension](M.2) can be empty field
				}
				else{
					//[Physical_Dimension](M.1) check for ascii
					bASCII = checkAsciiF(physical_dimension);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_PHYSICAL_DIMENSION, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * physical minimum (e.g. -500 or 34) 
				 ************************************************************/
				String physical_minimum = (String) getModel().getValueAt(i, COL_INDEX_PHYSICAL_MINIMUM);
				boolean bGood_physical_minimum = false;
				if (physical_minimum==null || physical_minimum.equals("")){
					//[Physical_Minimum](N.5) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Minimum](N.1) check for ascii
					bASCII = checkAsciiF(physical_minimum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
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
									fileName, i, COL_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}
				
				/************************************************************
				 * ns * 8 ascii : ns * physical maximum (e.g. 500 or 40) 
				 ************************************************************/
				String physical_maximum = (String) getModel().getValueAt(i, COL_INDEX_PHYSICAL_MAXIMUM);
				boolean bGood_physical_maximum = false;
				if (physical_maximum==null || physical_maximum.equals("")){
					//[Physical_Maximum](O.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Maximum](O.1) check for ascii
					bASCII = checkAsciiF(physical_maximum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
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
									fileName, i, COL_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}
				
				//[Physical_Maximum](O.3) physical maximum NOT = physical minimum
				if (bGood_physical_minimum && bGood_physical_maximum){
					if (Float.parseFloat(physical_minimum) == Float.parseFloat(physical_maximum)){
						description = Incompliance.error_esa_phy_equal;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * digital minimum (e.g. -2048)
				 ************************************************************/
				String digital_minimum = (String) getModel().getValueAt(i, COL_INDEX_DIGITAL_MINIMUM);
				boolean bGood_digital_minimum = false;
				if (digital_minimum==null || digital_minimum.equals("")){
					//[Digital_Minimum](P.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Minimum](P.1) check for ascii
					bASCII = checkAsciiF(digital_minimum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
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
										fileName, i, COL_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
								esaIncompliances.add(incomp);
							}
						}catch (NumberFormatException e) {
							description = Incompliance.error_esa_digmin;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, COL_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * digital maximum (e.g. 2047) 
				 ************************************************************/
				String digital_maximum = (String) getModel().getValueAt(i, COL_INDEX_DIGITAL_MAXIMUM);
				boolean bGood_digital_maximum = false;
				if (digital_maximum==null || digital_maximum.equals("")){
					//[Digital_Maximum](Q.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Maximum](Q.1) check for ascii
					bASCII = checkAsciiF(digital_maximum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
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
										fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
								esaIncompliances.add(incomp);
							}
						}catch (NumberFormatException e) {
							description = Incompliance.error_esa_digmax;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
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
								fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
					//[Digital_Minimum](Q.5) digital minimum < digital maximum
					else if (dig_minimum > dig_maximum){
						description = Incompliance.error_esa_digmaxmin;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
						
					}
				}

				/************************************************************
				 * ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz) 
				 ************************************************************/
				String prefiltering = (String) getModel().getValueAt(i, COL_INDEX_PREFILTERING);
				if (prefiltering==null || prefiltering.equals("")){
					//[Prefiltering](R.2) can be empty field
				}
				else{
					//[Prefiltering](R.1) check for ascii
					bASCII = checkAsciiF(prefiltering);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_PREFILTERING, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
				
				/************************************************************
				 * ns * 8 ascii : ns * nr of samples in each data record 
				 ************************************************************/
				String num_signals = (String) getModel().getValueAt(i, COL_INDEX_NR_OF_SAMPLES);
				if (num_signals==null || num_signals.equals("")){
					//[Num_signals](S.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, COL_INDEX_NR_OF_SAMPLES, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Num_signals](S.1) check for ascii
					bASCII = checkAsciiF(num_signals);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_NR_OF_SAMPLES, errorSrcTypeIndex);
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
										fileName, i, COL_INDEX_NR_OF_SAMPLES, errorSrcTypeIndex);
								esaIncompliances.add(incomp);
							}
						}catch (NumberFormatException e) {
							description = Incompliance.error_esa_nrSig;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, COL_INDEX_NR_OF_SAMPLES, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}
				
				/************************************************************
				 * ns * 32 ascii : ns * reserved
				 ************************************************************/
				String reserved = (String) getModel().getValueAt(i, COL_INDEX_RESERVED);
				if (reserved==null || reserved.equals("")){
					//[Reserved](T.2) can be empty field
				}
				else{
					//[Reserved](T.1) check for ascii
					bASCII = checkAsciiF(reserved);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, COL_INDEX_RESERVED, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}
				
			}//for loop ends
			
		}
		else if (this instanceof ESATemplateTable) {
			errorSrcTypeIndex = Incompliance.index_incomp_src_esatemplate;
			incomplianceType = Incompliance.typeOfErrorHeader[errorSrcTypeIndex];
			
			//Check "ESA Template" fields
		    final int TEMPLATE_INDEX_PHYSICAL_MINIMUM = 4;
		    final int TEMPLATE_INDEX_PHYSICAL_MAXIMUM = 5;
		    final int TEMPLATE_INDEX_DIGITAL_MINIMUM = 6;
		    final int TEMPLATE_INDEX_DIGITAL_MAXIMUM = 7;
			
			for (int i = 0; i < nrow; i++) {
				
				/************************************************************
				 * ns * 8 ascii : ns * physical minimum (e.g. -500 or 34) 
				 ************************************************************/
				String physical_minimum = (String) getModel().getValueAt(i, TEMPLATE_INDEX_PHYSICAL_MINIMUM);
				boolean bGood_physical_minimum = false;
				if (physical_minimum==null || physical_minimum.equals("")){
					//[Physical_Minimum](N.5) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, TEMPLATE_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Minimum](N.1) check for ascii
					bASCII = checkAsciiF(physical_minimum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
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
									fileName, i, TEMPLATE_INDEX_PHYSICAL_MINIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}
				
				/************************************************************
				 * ns * 8 ascii : ns * physical maximum (e.g. 500 or 40) 
				 ************************************************************/
				String physical_maximum = (String) getModel().getValueAt(i, TEMPLATE_INDEX_PHYSICAL_MAXIMUM);
				boolean bGood_physical_maximum = false;
				if (physical_maximum==null || physical_maximum.equals("")){
					//[Physical_Maximum](O.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, TEMPLATE_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Physical_Maximum](O.1) check for ascii
					bASCII = checkAsciiF(physical_maximum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
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
									fileName, i, TEMPLATE_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}
				
				//[Physical_Maximum](O.3) physical maximum NOT = physical minimum
				if (bGood_physical_minimum && bGood_physical_maximum){
					if (Float.parseFloat(physical_minimum) == Float.parseFloat(physical_maximum)){
						description = Incompliance.error_esa_phy_equal;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_PHYSICAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * digital minimum (e.g. -2048)
				 ************************************************************/
				String digital_minimum = (String) getModel().getValueAt(i, TEMPLATE_INDEX_DIGITAL_MINIMUM);
				boolean bGood_digital_minimum = false;
				if (digital_minimum==null || digital_minimum.equals("")){
					//[Digital_Minimum](P.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, TEMPLATE_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Minimum](P.1) check for ascii
					bASCII = checkAsciiF(digital_minimum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
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
										fileName, i, TEMPLATE_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
								esaIncompliances.add(incomp);
							}
						}catch (NumberFormatException e) {
							description = Incompliance.error_esa_digmin;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, TEMPLATE_INDEX_DIGITAL_MINIMUM, errorSrcTypeIndex);
							esaIncompliances.add(incomp);
						}
					}
				}

				/************************************************************
				 * ns * 8 ascii : ns * digital maximum (e.g. 2047) 
				 ************************************************************/
				String digital_maximum = (String) getModel().getValueAt(i, TEMPLATE_INDEX_DIGITAL_MAXIMUM);
				boolean bGood_digital_maximum = false;
				if (digital_maximum==null || digital_maximum.equals("")){
					//[Digital_Maximum](Q.4) cannot be empty field
					description = Incompliance.error_esa_empty;
					incomp = new Incompliance(incomplianceType, description,
							fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
					esaIncompliances.add(incomp);
				}
				else{
					//[Digital_Maximum](Q.1) check for ascii
					bASCII = checkAsciiF(digital_maximum);
					if (!bASCII) {
						description = Incompliance.error_esa_ascii;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
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
										fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
								esaIncompliances.add(incomp);
							}
						}catch (NumberFormatException e) {
							description = Incompliance.error_esa_digmax;
							incomp = new Incompliance(incomplianceType, description,
									fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
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
								fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
					}
					//[Digital_Minimum](Q.5) digital minimum < digital maximum
					else if (dig_minimum > dig_maximum){
						description = Incompliance.error_esa_digmaxmin;
						incomp = new Incompliance(incomplianceType, description,
								fileName, i, TEMPLATE_INDEX_DIGITAL_MAXIMUM, errorSrcTypeIndex);
						esaIncompliances.add(incomp);
						
					}
				}

			}//for loop ends
			
		}
		else{
			return esaIncompliances; // at this time, esaIncompliances.size() = 0;
		}
		
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
        //index always be Incompliance.index_incomp_src_eia        
        final int srcTypeIndex = Incompliance.index_incomp_src_eia;
        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
        
        int nrow = this.getRowCount();
            
        Incompliance incomp;
        String description;
        String fileName;
        int rowIndex, columnIndex;
        
        int segnumber = 3;
        int seg[] = new int[segnumber];
        String[] descriptions = {Incompliance.warning_eia_day, 
                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
        //int[] lowerbounds = {0, 0, 0};
        //int[] upperbounds = {31, 12, 99}; // 31 day, 12 month, 2099 years
        
       // boolean cellHiding = (getModel().getColumnCount() != getColumnModel().getColumnCount());
        boolean cellHiding = false;
        
        String sd;
        for (int i = 0; i < nrow; i++) {
            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
            fileName = MainWindow.getWkEdfFiles().get(i).getAbsolutePath();
            rowIndex = i;
            columnIndex = cellHiding? START_DATE_INDEX - 1: START_DATE_INDEX;
            String[] psd = sd.split("\\.");
            
            //1. check format of dd.mm.yy
            int psdlen = psd.length;
            if (psdlen != segnumber){
                setEdfValid(false);
                description = Incompliance.error_eia_format;
                incomp = new Incompliance( incomplianceType, description, 
                                           fileName, rowIndex, columnIndex, srcTypeIndex);
                //MainWindow.getAggregateIncompliances().add(incomp);
                eiaIncompliances.add(incomp);
                continue;
            } 
            
            //2. check each segment of startdate is number or not
            try{
                for (int j = 0; j < segnumber; j++)
                    seg[j] = Integer.parseInt(psd[j]);
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_eia_dmyint;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, srcTypeIndex);
                //MainWindow.getAggregateIncompliances().add(incomp);
                eiaIncompliances.add(incomp);
                continue;
            }
            
            //3. check segment valid
            for (int j = 0; j < psdlen; j++) {   
                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
                    setEdfValid(false);
                    description = descriptions[j];
                    incomp = new Incompliance(incomplianceType, description, 
                                              fileName, rowIndex, columnIndex, srcTypeIndex);
                    //MainWindow.getAggregateIncompliances().add(incomp);
                    eiaIncompliances.add(incomp);
                }
            }
        } 
        return eiaIncompliances;
    }
    
    public ArrayList<Incompliance> parseEIATemplateTable(){
        ArrayList<Incompliance> eiaTemplateIncomps = new ArrayList<Incompliance>();
        final int srcTypeIndex = Incompliance.index_incomp_src_eiatemplate;        
        final String incomplianceType = Incompliance.typeOfErrorHeader[srcTypeIndex];     
        Incompliance incomp;
        String description;
        String fileName = this.getMasterFile().getPath();
        int nrow = 1;
        
        int rowIndex, columnIndex = 2;
        int segnumber = 3;
        int seg[] = new int[segnumber];
        String[] descriptions = {Incompliance.warning_eia_day, 
                                 Incompliance.warning_eia_month, Incompliance.warning_eia_year};
        
       // boolean cellHiding = true;
        
        String sd;
        for (int i = 0; i < nrow; i++) {
            sd = ((String)this.getModel().getValueAt(i, START_DATE_INDEX)).trim();
            fileName = this.getMasterFile().getAbsolutePath();
            rowIndex = i;
            String[] psd = sd.split("\\.");
            
            //1. check format of dd.mm.yy
            int psdlen = psd.length;
            if (psdlen != segnumber){
                setEdfValid(false);
                description = Incompliance.error_eia_format;
                incomp = new Incompliance( incomplianceType, description, 
                                           fileName, rowIndex, columnIndex, srcTypeIndex);
                eiaTemplateIncomps.add(incomp);
                continue;
            } 
            
            //2. check each segment of startdate is number or not
            try{
                for (int j = 0; j < segnumber; j++)
                    seg[j] = Integer.parseInt(psd[j]);
            }
            catch(NumberFormatException e){
                setEdfValid(false);
                description = Incompliance.error_eia_dmyint;
                incomp = new Incompliance(incomplianceType, description, 
                                          fileName, rowIndex, columnIndex, srcTypeIndex);
                  eiaTemplateIncomps.add(incomp);
                continue;
            }
            
            //3. check segment valid
            for (int j = 0; j < psdlen; j++) {   
                if (seg[j] < lowerbounds[j] || seg[j] > upperbounds[j]) {
                    setEdfValid(false);
                    description = descriptions[j];
                    incomp = new Incompliance(incomplianceType, description, 
                                              fileName, rowIndex, columnIndex, srcTypeIndex);
                     eiaTemplateIncomps.add(incomp);
                }
            }
        }        
        
        return eiaTemplateIncomps;
    }
    
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
