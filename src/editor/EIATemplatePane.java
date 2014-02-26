package editor;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import header.EIA;
import header.EIAHeader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;

import table.EIATemplateTable;


public class EIATemplatePane extends BasicEDFPane {

    protected static final String prtags[] = EIA.getKeys();
    //protected static final String 
    protected static final String datetags[] = {EIA.key_blank, EIA.key_rand, EIA.key_skip, EIA.key_yy, EIA.key_mm, EIA.key_dd};
    
    protected static final String mutableKeys[] ={"Local Patient ID", "Local Recording ID", "Start Date of Recording",
      "Start Time of Recording"};

    private JLabel pidLabel = new JLabel(mutableKeys[0]);
    private JRadioButton pidRadio = new JRadioButton();
    private JFormattedTextField pidField = new JFormattedTextField();
    private JLabel pidTipLabel = new JLabel("(No more than 80 characters)");
    private JRadioButton pidTagRadio = new JRadioButton();
    private JComboBox pidTagBox = this.createPRBox();

    private JLabel ridLabel = new JLabel(mutableKeys[1]);
    private JRadioButton ridRadio = new JRadioButton();
    private JFormattedTextField ridField = new JFormattedTextField();
    private JLabel ridTipLabel = new JLabel("(No more than 80 characters)");
    private JRadioButton ridTagRadio = new JRadioButton();
    private JComboBox ridTagBox = this.createPRBox();

    private JLabel dateLabel = new JLabel(mutableKeys[2]);
    private JRadioButton dateRadio = new JRadioButton();
    private JFormattedTextField dateField = new JFormattedTextField();
    private JLabel dateTipLabel = new JLabel("(Format: dd.mm.yy)");
    private JRadioButton dateTagRadio = new JRadioButton();
    private JComboBox dateTagBox = createDateBox();
   
    private JLabel previewLabel;

    //two main components in the panel
    private JPanel formPane;
    private EIATemplateTable previewTable;
    
    private static long uid; // universal id
    private long pid; // personal id
    
    private final static Dimension fieldDim = new Dimension(250, 30);
    private final static Font font = new Font(MainWindow.tabPane.getFont().getName(),
                MainWindow.tabPane.getFont().getStyle(),
                 MainWindow.tabPane.getFont().getSize() + 2);
    //private final static Font previewLable_font = new Font(font.getName(), font.getStyle(), font.getSize() + 4);
    

    /**
     * create pane for opened EIA template
     * @param eiaHeader
     */
    public EIATemplatePane(EIAHeader eiaHeader, File msFile) {
        super();
        
        incrementUid();
        pid = uid;

        this.setIsPrimaryTab(false);
        
        if (eiaHeader == null)
            previewTable = new EIATemplateTable(); 
        else
            previewTable = new EIATemplateTable(eiaHeader);//createPreviewTable(eiaHeader);
        
        previewTable.setEnabled(false);
        
        //added by Fangping, 10/01/2010        
        previewTable.setMasterFile(msFile);
        this.setMasterFile(msFile);
                
        formPane = createFormPane();

        setupLayout();
    }

    private void adjustBoxSize(JComboBox box, double widthRatio, double heightRatio) {
        Dimension boxSize = box.getPreferredSize();
        int width = (int)(boxSize.getWidth() * widthRatio);
        int height = (int)(boxSize.getHeight() * heightRatio);
        box.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        box.setPreferredSize(new Dimension(width, height));
    }


    private JComboBox createPRBox() {
        JComboBox box = new JComboBox();
        adjustBoxSize(box, 1.0, 1.3);

        for (int i = 0; i < prtags.length; i++) {
            box.addItem(prtags[i]);
        }

        box.setEditable(false);

        return box;
    } 
    
    private JComboBox createDateBox() {
        JComboBox box = new JComboBox();
        adjustBoxSize(box, 1.0, 1.3);

        for (int i = 0; i < datetags.length; i++) {
            box.addItem(datetags[i]);
        }

        box.setEditable(false);

        return box;
    }
    

   /*  private void writeInstructionToLogContentPane() {
        this.appendToLog("Each attribute field can specify with a specific value, or one of the eight attribute tags: " +
                         "<BLANK/>, <RAND/>, <SKIP/>, <Local Patient ID/>, <Local Recording ID/>, <YY/>, <MM/>, <DD/>.",
                         "instruction");
        this.appendToLog("<BLANK/> : Blank out target attribute value when applied. ",
                         "instruction");
        this.appendToLog("<RAND/>  : Yield a random value and substitute target attribute value when applied.",
                         "instruction");
        this.appendToLog("<SKIP/>  : Untouch target attribute value when applied.",
                         "instruction");
        this.appendToLog("<FileName/> : Uses the file name", "instruction");
        this.appendToLog("<PID/> : Uses the value in the Local Patient ID field",
                         "instruction");
        this.appendToLog("<RID/>: Uses the value in the Local Recording ID field",
                         "instruction");
        this.appendToLog("<YY/> : Uses the Year value from the Start Date of Recording field",
                         "instruction");
        this.appendToLog("<MM/> : Uses the Month value from the Start Date of Recording field",
                         "instruction");
        this.appendToLog("<DD/> : Uses the Day value from the Start Date of Recording field",
                         "instruction");
        this.appendToLog("========================================================================================\n",
                         "instruction");
        this.appendToLog("Caution: Any fields that exceed 80 characters in length will automatically be truncated.",
                         "instruction");
    } */


    public EIATemplateTable createPreviewTable(EIAHeader eiaHeader) {
        String pid, rid, startDate, startTime;
        if (eiaHeader == null) {
            pid = " ";
            rid = " ";
            startDate = " ";
            startTime = " ";
        } else {
            HashMap header = eiaHeader.getEIAHeader();
            pid = (String)header.get(EIA.LOCAL_PATIENT_ID);
            rid = (String)header.get(EIA.LOCAL_RECORDING_ID);
            startDate = (String)header.get(EIA.START_DATE_RECORDING);
            startTime = (String)header.get(EIA.START_TIME_RECORDING);
        }

        String[][] firstRow = { { pid, rid, startDate } }; //, startTime}};

        EIATemplateTable pTable = new EIATemplateTable(); //(firstRow, columnNames);
        
        previewTable.getTableHeader().setFont(new Font("Dialog", Font.PLAIN,
                                                       14));
        previewTable.getTableHeader().setForeground(Color.black);
        previewTable.setRowHeight((int)(previewTable.getRowHeight() * 1.5));
        previewTable.setCellSelectionEnabled(true);
        previewTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        previewTable.getTableHeader().setReorderingAllowed(false);
        previewTable.setEnabled(false);

        previewTable.addMouseMotionListener(new CellMouseListener());
        
        return previewTable;
    }
    
    private JLabel createPreviewLabel(){
        JLabel previewLabel = new JLabel("Attributes Preview");
        previewLabel.setFont(font);
        
        return previewLabel;
    }

    private void makePidRowLook(String value) {
        if (value == null)
            value = " ";
        ButtonGroup group = new ButtonGroup();
        group.add(pidRadio);
        group.add(pidTagRadio);
        pidField.setText(value);
        pidField.setPreferredSize(fieldDim);
        pidTipLabel.setFont(font);
        pidTipLabel.setForeground(Color.red);

        pidTagBox.setEnabled(false);
        pidTagBox.addActionListener(new BoxListener());
        pidField.getDocument().addDocumentListener(new TextFieldListener(EIA.index_patient_id)); // 0
        pidField.setInputVerifier(new FieldInputVerifier());
        pidRadio.addActionListener(new RadioListener());
        pidTagRadio.addActionListener(new RadioListener());

        if (isAttributeTag(value)) {
            pidRadio.setSelected(false);
            pidTagRadio.setSelected(true);
            pidField.setEditable(false);
            pidTagBox.setEnabled(true);
            pidTagBox.setSelectedItem(value);
        } else {
            pidRadio.setSelected(true);
            pidField.setEditable(true);
            //pidField.setText("yes "); // value replaced

            pidTagRadio.setSelected(false);
            pidTagBox.setEnabled(false);
        }
    }

    private void makeRidRowLook(String value) {
        if (value == null)
            value = " ";

        ButtonGroup group = new ButtonGroup();
        group.add(ridRadio);
        group.add(ridTagRadio);
        ridField.setText(value);
        ridField.setPreferredSize(fieldDim);
        ridTipLabel.setFont(font);
        ridTipLabel.setForeground(Color.red);

        ridTagBox.setEnabled(false);
        ridTagBox.addActionListener(new BoxListener());
        ridField.getDocument().addDocumentListener(new TextFieldListener(EIA.index_recording_id)); //1
        ridField.setInputVerifier(new FieldInputVerifier());

        ridRadio.addActionListener(new RadioListener());
        ridTagRadio.addActionListener(new RadioListener());

        if (isAttributeTag(value)) {
            ridRadio.setSelected(false);
            ridField.setEditable(false);

            ridTagRadio.setSelected(true);
            ridTagBox.setEnabled(true);
            ridTagBox.setSelectedItem(value);
        } else {
            ridRadio.setSelected(true);
            ridField.setEditable(true);
            ridField.setText(value);

            ridTagRadio.setSelected(false);
            ridTagBox.setEnabled(false);
        }
    }

    private void makeDateRowLook(String value) {
        if (value == null)
            value = " ";

        ButtonGroup group = new ButtonGroup();
        group.add(dateRadio);
        group.add(dateTagRadio);
        dateField.setText(value);
        dateField.setPreferredSize(fieldDim);
        dateTipLabel.setFont(font);
        dateTipLabel.setForeground(Color.red);

        dateTagBox.setEnabled(false);
        dateTagBox.addActionListener(new BoxListener());
        dateField.getDocument().addDocumentListener(new TextFieldListener(EIA.index_start_date)); //2
        dateField.setInputVerifier(new FieldInputVerifier());

        dateRadio.addActionListener(new RadioListener());
        dateTagRadio.addActionListener(new RadioListener());

        if (isAttributeTag(value)) {
            dateRadio.setSelected(false);
            dateField.setEditable(false);

            dateTagRadio.setSelected(true);
            dateTagBox.setEnabled(true);
            dateTagBox.setSelectedItem(value);
        } else {
            dateRadio.setSelected(true);
            dateField.setEditable(true);
            dateField.setText(value);

            dateTagRadio.setSelected(false);
            dateTagBox.setEnabled(false);
        }
    }

       
   private JPanel createFormPane() {
        String headerValues[] = new String[4];
        for (int i = EIA.index_patient_id; i <=EIA.index_start_date; i++)
            headerValues[i - EIA.index_patient_id] =(String)previewTable.getModel().getValueAt(0, i);
        
        previewLabel = createPreviewLabel();
        String colSpec = "r:80dlu:n, 4dlu:n, 12dlu:n, 2dlu:n, f:150dlu:n, 4dlu:n, r:120dlu:n, f:p:g";
        String rowSpec = "4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:p:n, 4dlu:n, c:p:n, 2dlu:n, c:35dlu:n, 4dlu:n";
        FormLayout layout = new FormLayout(colSpec, rowSpec);
        JPanel form = new JPanel(layout);
        form.setBorder(BorderFactory.createTitledBorder("Configure Mutable Identity Attributes"));
        CellConstraints cc = new CellConstraints();

        //pid row 1:
        makePidRowLook(headerValues[0]);
        form.add(pidLabel, cc.xy(1, 2));
        form.add(pidRadio, cc.xy(3, 2));
        form.add(pidField, cc.xy(5, 2));
        form.add(pidTipLabel, cc.xy(7, 2));
        //pid row 2:
        form.add(pidTagRadio, cc.xy(3, 4));
        form.add(pidTagBox, cc.xywh(5, 4, 3, 1));

        //rid row 1:
        makeRidRowLook(headerValues[1]);
        form.add(ridLabel, cc.xy(1, 6));
        form.add(ridRadio, cc.xy(3, 6));
        form.add(ridField, cc.xy(5, 6));
        form.add(ridTipLabel, cc.xy(7, 6));
        //rid row 2:
        form.add(ridTagRadio, cc.xy(3, 8));
        form.add(ridTagBox, cc.xywh(5, 8, 3, 1));

        //start date row 1:
        makeDateRowLook(headerValues[2]);
        form.add(dateLabel, cc.xy(1, 10));
        form.add(dateRadio, cc.xy(3, 10));
        form.add(dateField, cc.xy(5, 10));
        form.add(dateTipLabel, cc.xy(7, 10));
        //start date row 2:
        form.add(dateTagRadio, cc.xy(3, 12));
        form.add(dateTagBox, cc.xywh(5, 12, 3, 1));
        
        form.add(previewLabel, cc.xyw(1, 14, 2));
        form.add(new JScrollPane(previewTable), cc.xyw(1, 16, 7));

        return form;
    } 
    
    

    private String[] getMutableAttributeValues(EIAHeader eiaHeader) {
        String values[] = new String[4];
        HashMap header = eiaHeader.getEIAHeader();

        for (int i = 0; i < 4; i++) {
            values[i] = (String)header.get(mutableKeys[i]);
        }

        return values;
    }

    private boolean isAttributeTag(String value) {
        for (int i = 0; i < prtags.length; i++) {
            if (value.equalsIgnoreCase(prtags[i]))
                return true;
        }

        return false;
    }


    public void setupLayout() {
        FormLayout layout =
            new FormLayout("6dlu, f:max(400dlu;p):n, f:p:g, 10dlu:n", "c:p:n, 6dlu:n, c:min(35dlu;p):n, 6dlu:n, c:min(35dlu;p):n");
        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();

        //JScrollPane tableScroller = new JScrollPane(previewTable);

        //this.add(topPane, cc.xywh(2, 1, 3, 1));
        this.add(formPane, cc.xywh(2, 1, 2, 1));
        //this.add(tableScroller, cc.xy(2, 3));

        this.setBorder(BorderFactory.createEtchedBorder());
        

    }

    public void setPreviewTable(EIATemplateTable previewTable) {
        this.previewTable = previewTable;
    }

    public EIATemplateTable getPreviewTable() {
        return previewTable;
    }

    public String currentTimeToString() {
        Date time = new Date();
        DateFormat df = new SimpleDateFormat("h:mm a");
        return df.format(time);
    }

    public void setUid(long myPid) {
        pid = myPid;
    }

    public long getPid() {
        return pid;
    }

    public static void incrementUid() {
        uid++;
    }

    /**
     * this can be used for both template applying and saving
     * @return the header in current table
     */
    public EIAHeader eiaHeaderFromPreviewTable() {
        return new EIAHeader(previewTable);
/*         EIAHeader header = new EIAHeader();

        TableModel model = previewTable.getModel();

        String localPid = (String)model.getValueAt(0, EIA.index_patient_id);
        header.setValueAt(EIA.LOCAL_PATIENT_ID, localPid);

        String localRid = (String)model.getValueAt(0, EIA.index_recording_id);
        header.setValueAt(EIA.LOCAL_RECORDING_ID, localRid);

        String startDate = (String)model.getValueAt(0, EIA.index_start_date);
        header.setValueAt(EIA.START_DATE_RECORDING, startDate);

        //String startTime = (String) model.getValueAt(0, EIA.index_start_time);
        //header.setValueAt(EIA.START_TIME_RECORDING, startTime);

        return header; */
    }

    private class RadioListener implements ActionListener {

        private void switchPidRows(JRadioButton radio) {
            if (radio == pidRadio) {
                pidField.setEnabled(true);
                pidField.setEditable(true);
                pidTagBox.setEnabled(false);
                previewTable.getModel().setValueAt(pidField.getText(), 0, EIA.index_patient_id);
            }
            if (radio == pidTagRadio) {
                pidTagBox.setEnabled(true);
                pidField.setEditable(false);
                pidField.setEnabled(false);
                previewTable.getModel().setValueAt(pidTagBox.getSelectedItem(), 0, EIA.index_patient_id);
            }

        }

        private void switchRidRows(JRadioButton radio) {
            if (radio == ridRadio) {
                ridField.setEnabled(true);
                ridField.setEditable(true);
                ridTagBox.setEnabled(false);
                previewTable.getModel().setValueAt(ridField.getText(), 0, EIA.index_recording_id);
            } else {
                ridTagBox.setEnabled(true);
                ridField.setEnabled(false);
                ridField.setEditable(false);
                previewTable.getModel().setValueAt(ridTagBox.getSelectedItem(), 0, EIA.index_recording_id);
            }

        }

        private void switchStartDateRows(JRadioButton radio) {
            if (radio == dateRadio) {
                dateField.setEnabled(true);
                dateField.setEditable(true);
                dateTagBox.setEnabled(false);
                previewTable.getModel().setValueAt(dateField.getText(), 0, EIA.index_start_date);
            } else {
                dateTagBox.setEnabled(true);
                dateField.setEnabled(false);
                dateField.setEditable(false);
                previewTable.getModel().setValueAt(dateTagBox.getSelectedItem(), 0, EIA.index_start_date);
            }
        }

        public void actionPerformed(ActionEvent e) {

            JRadioButton srcRadio = (JRadioButton)e.getSource();

            if (srcRadio == pidRadio || srcRadio == pidTagRadio) {
                switchPidRows(srcRadio);
                return;
            }

            if (srcRadio == ridRadio || srcRadio == ridTagRadio) {
                switchRidRows(srcRadio);
                return;
            }

            if (srcRadio == dateRadio || srcRadio == dateTagRadio) {
                switchStartDateRows(srcRadio);
                return;
            }
        }
    }

    private class BoxListener implements ActionListener {

        private void updatePreviewTableValueAt(int colIndex, String value) {
            previewTable.getModel().setValueAt(value, 0, colIndex);
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox box = (JComboBox)e.getSource();

            int colIndex = -1;
            String value = (String)box.getSelectedItem();

            if (box == pidTagBox)
                colIndex = EIA.index_patient_id;
            else if (box == ridTagBox)
                colIndex = EIA.index_recording_id;
            else if (box == dateTagBox)
                colIndex = EIA.index_start_date;
            else
                colIndex = EIA.index_start_time;

            updatePreviewTableValueAt(colIndex, value);
        }
    }

    private class TextFieldListener implements DocumentListener {
        private int colIndex;
        private String value;

        private void updatePreviewTableValueAt(int colIndex, String value) {
            previewTable.getModel().setValueAt(value, 0, colIndex);
        }

        public TextFieldListener(int colIndex) {
            this.colIndex = colIndex;
        }

        public void insertUpdate(DocumentEvent e) {
            int nlen = e.getDocument().getLength();
            try {
                value = e.getDocument().getText(0, nlen);
            } catch (BadLocationException f) {
                f.printStackTrace();
            }
            updatePreviewTableValueAt(colIndex, value);
        }

        public void removeUpdate(DocumentEvent e) {
            int nlen = e.getDocument().getLength();
            try {
                value = e.getDocument().getText(0, nlen);
            } catch (BadLocationException f) {
                f.printStackTrace();
            }
            updatePreviewTableValueAt(colIndex, value);
        }

        public void changedUpdate(DocumentEvent e) {
            updatePreviewTableValueAt(colIndex, value);
        }
    }

    private class FieldInputVerifier extends InputVerifier {
        private String errMsg[] = new String[10];
        private int errno;

        public FieldInputVerifier() {
            errMsg[0] = "input more than 80 characters";
            errMsg[1] = "input an attribute tag";
            errMsg[2] = "input 8 characters in dd.mm.yy format";
            errMsg[3] = "value in dd field is not a number in [0, 31]";
            errMsg[4] = "value in mm field is not a number in [1, 12]";
            errMsg[5] = "value in yy field is not a number";
            errMsg[6] = "input 8 characters in dd.mm.yy format";
            errMsg[7] = "value in hh field is not a number in [0, 59]";
            errMsg[8] = "value in mm field is not a number in [0, 59]";
            errMsg[9] = "value in ss field is not a number in [0, 59]";
        }

        public String[] parseDateTime(String value) {
            String hd[] = new String[3];
            for (int i = 0; i < 3; i++) {
                hd[i] = value.substring(3 * i, 3 * i + 2);
            }

            return hd;
        }


        public boolean verify(JComponent input) {
            JFormattedTextField ft = (JFormattedTextField)input;
            String value = ft.getText().trim();
            int valueLen = value.length();

            if (valueLen == 0)
                return true;

            if (ft == pidField || ft == ridField) {
                if (valueLen > 80) {
                    errno = 0;
                    return false;
                }

                for (int i = 0; i < 3; i++) {
                    if (value.equalsIgnoreCase(prtags[i])) {
                        errno = 1;
                        return false;
                    }
                }
                return true;
            }

            //            if (ft == dateField){ // for date
            //                if (valueLen != 8){
            //                    errno = 2;
            //                    return false;
            //                }
            //
            //                char dot2 = value.charAt(2);
            //                char dot5 = value.charAt(5);
            //                if (dot2 != '.' || dot5 != '.'){
            //                    errno = 2;
            //                    return false;
            //                }
            //
            //                String dmy[] = parseDateTime(value); // hms.lengh = 3;
            //                System.out.println("dmy[2] of the field is: " + dmy[2]);
            //                int i = 0;
            //                try {
            //                    for (i = 0; i < 3; i++) {
            //                        Integer.parseInt(dmy[i]);
            //                    }
            //                } catch (NumberFormatException e) {
            //                     errno = i + 3;
            //                     return false;
            //                }
            //                return true;
            //            }

/*             if (ft == timeField) { // for time
                char dot2 = value.charAt(2);
                char dot5 = value.charAt(5);
                if (valueLen != 8 || dot2 != '.' || dot5 != '.') {
                    errno = 6;
                    return false;
                }

                String dmy[] = parseDateTime(value); // hms.lengh = 3;
                int i = 0;
                try {
                    for (i = 0; i < 3; i++) {
                        Integer.parseInt(dmy[i]);
                    }
                } catch (NumberFormatException e) {
                    errno = i + 7;
                    return false;
                }
                return true;
            } */

            return true;
        }

        public boolean shouldYieldFocus(JComponent input) {
            boolean valid = super.shouldYieldFocus(input);
            if (!valid) {
                String message = errMsg[errno];
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, message, "invalid input",
                                              JOptionPane.ERROR_MESSAGE);

            }
            return valid;
        }
    }

    /**
     * update the status bar with current mouse-pointed cell
     */
    private class CellMouseListener implements MouseMotionListener {

        public void mouseDragged(MouseEvent e) {
            JTable table = (JTable)e.getSource();
            int nrows = table.getSelectedRowCount();
            int ncols = table.getSelectedColumnCount();

            String tr = (nrows > 1) ? " rows" : " row";
            String tc = (ncols > 1) ? " columns" : " column";
            String vb = (ncols == 1 && nrows == 1) ? " is" : " are";

            String text =
                nrows + " " + tr + " and " + ncols + tc + vb + " Selected.";
            MainWindow.setCellContent(text);
        }

        public void mouseMoved(MouseEvent e) {
            JTable table = (JTable)e.getSource();
            int rr = table.rowAtPoint(e.getPoint());
            int cc = table.columnAtPoint(e.getPoint());
            String value = (String)table.getValueAt(rr, cc);
            if (value == null)
                value = "";
            String text = "Selected Value:  " + value;
            MainWindow.setCellContent(text);
        }
    }


}
