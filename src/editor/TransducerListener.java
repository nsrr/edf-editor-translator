package editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import table.EDFTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@SuppressWarnings("serial")
public class TransducerListener extends JDialog {
    protected static JComboBox procedureBox;
    protected static JComboBox deviceBox;
    protected static JComboBox locationBox;
    protected static JComboBox refLocBox;
    protected static JLabel procedureLabel;
    protected static JLabel deviceLabel;
    protected static JLabel locationLabel;
    protected static JLabel refLocLabel;
    
    //Fangping, 08/09/2010
    protected static JLabel summaryLabel;
    protected static JLabel finalValueLabel;
    
    //these three labels are obsolete and to be removed
    protected static JLabel finalString;
    protected static JLabel finalStringLabel;
    protected static JLabel finalStringCount;


    private JButton finishButton;
    private JButton cancelButton;

    private final int dialogWidth = 550;
    private final int dialogHeight = 400;

    protected String[] aFinalString = { "", "", "", "" };

    private HashMap<Integer, String> procedures;
    private HashMap<String, Integer> revProcedures;
    @SuppressWarnings("unused")
	private ArrayList<Integer> devicesRefLocReq;

    private EDFTable esa;
    private int row;
    private int col;
    
    /*
     * Fangping, 08/10/2010
     */
    private static final String COMMA = ":";
    private static final String AT = " at ";
    private static final String REFERENCED_TO = " referenced to ";


    /**
     * TODO
     * @param frame
     * @param x
     * @param y
     * @param esaTemp
     */
    public TransducerListener(JFrame frame, int x, int y, EDFTable esaTemp) {
        super(frame, true); // modal
        this.setLocationRelativeTo(frame);

        esa = esaTemp;
        row = x;
        col = y;
        devicesRefLocReq = new ArrayList<Integer>();
        procedures = new HashMap<Integer, String>();
        String curVal = (String)esaTemp.getValueAt(row, col);
       /*
        * Fanging, AT bug removed
        */
        if (curVal != null) {
            if (!curVal.equals("")) {
                int indC = curVal.indexOf(COMMA);
                //this is wrong for "Respiratory Effort:Impedance Belt at SUM of Chest and Abdomen";
                int indAt = curVal.indexOf(AT);
                int indRef = curVal.indexOf(REFERENCED_TO);
                if (indC != -1) {
                    aFinalString[0] = curVal.substring(0, indC).trim();
                    if (indAt != -1) {
                        aFinalString[1] = curVal.substring(indC + COMMA.length(), indAt).trim();
                        if (indRef != -1) {
                            aFinalString[2] = curVal.substring(indAt + AT.length(), indRef).trim();
                            aFinalString[3] =
                                    curVal.substring(indRef + REFERENCED_TO.length(), curVal.length()).trim();
                        } else {
                            aFinalString[2] =
                                    curVal.substring(indAt + AT.length(), curVal.length());
                        }
                    } else {
                        aFinalString[1] = curVal.substring(indC + 1, curVal.length()).trim();
                    }
                } else {
                    aFinalString[0] = curVal;
                }
            }
        }

        initUI();
        setDialogLayout();
        visualize();
    }

    /**
     * TODO
     */
    private void initUI() {
        this.setSize(new Dimension(dialogWidth, dialogHeight));

        procedureLabel = new JLabel("Procedure: ", JLabel.RIGHT);
        procedureLabel.setVisible(true);
        Font font = new Font(procedureLabel.getFont().getName(), procedureLabel.getFont().getStyle(), procedureLabel.getFont().getSize()+2);
        procedureLabel.setFont(font);

        deviceLabel = new JLabel("Device: ", JLabel.RIGHT);
        deviceLabel.setVisible(false);
        deviceLabel.setFont(font);

        locationLabel = new JLabel("Location: ", JLabel.RIGHT);
        locationLabel.setVisible(false);
        locationLabel.setFont(font);

        refLocLabel = new JLabel("Reference Location: ", JLabel.RIGHT);
        refLocLabel.setVisible(false);
        refLocLabel.setFont(font);

        Border border = LineBorder.createGrayLineBorder();
        
        //Fangping, 08/09/2010
        summaryLabel = new JLabel("Transducer Type:", JLabel.RIGHT);
        summaryLabel.setFont(font);

        finalStringLabel = new JLabel("Standard Signal Type:");
        finalStringLabel.setBorder(border);
        finalString = new JLabel();
        finalStringCount = new JLabel();
        finalStringCount.setBorder(border);

        procedures = readFile("/SignalTypes/procedures.txt");
        revProcedures = revHReadFile("/SignalTypes/procedures.txt");
        procedureBox = new JComboBox(procedures.values().toArray());

        deviceBox = new JComboBox();
        deviceBox.setVisible(false);

        locationBox = new JComboBox();
        locationBox.setVisible(false);

        refLocBox = new JComboBox();
        refLocBox.setVisible(false);

        if (!aFinalString[0].equals("")) {
            procedureBox.setSelectedItem(aFinalString[0]);
            updateLabels(0, procedureBox);
            setDeviceBox(revProcedures.get(aFinalString[0]));
            setLocationBox(revProcedures.get(aFinalString[0]));
            //why not setRefLocationBox()? Fangping, 08/10/2010
        }
        
        //Fangping, 08/09/2010
        finalValueLabel = new JLabel(" ", JLabel.CENTER);
        finalValueLabel.setOpaque(true);
        finalValueLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize()-2));
        finalValueLabel.setBorder(BorderFactory.createEmptyBorder());
        
        procedureBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    updateLabels(0, cb);
                    setDeviceBox(revProcedures.get(aFinalString[0]));
                    setLocationBox(revProcedures.get(aFinalString[0]));                        
                }
            });

        finishButton = new JButton("Finish");
        finishButton.addActionListener(new FinishButtonListener());

        cancelButton = new JButton("Cancel");
        InputMap im =
            cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = cancelButton.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
        am.put("Cancel", new CancelAction());

        cancelButton.addActionListener(new CancelButtonListener());
    }

    /**
     * TODO
     * @param procID
     */
    public void setDeviceBox(int procID) {
        deviceLabel.setVisible(true);
        deviceBox.setVisible(true);
        deviceBox.removeAllItems();
        deviceBox.addItem("");
        HashMap<Integer, String> devices = readFile("/SignalTypes/devices.txt");
        ArrayList<Integer> keep = new ArrayList<Integer>();
        try {
            InputStream is =
                Main.class.getResourceAsStream("/SignalTypes/proceduredevices.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = in.readLine();
            while (line != null) {
                String[] delimited = line.split(",");
                int pid = Integer.parseInt(delimited[1]);
                int did = Integer.parseInt(delimited[2]);
                if (procID == pid) {
                    keep.add(did);
                }
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!keep.isEmpty()) {
            for (int i = 0; i < keep.size(); i++) {
                String deviceName = devices.get(keep.get(i));
                deviceBox.addItem(deviceName);
            }
        } else {
            deviceBox.setVisible(false);
            deviceLabel.setVisible(false);
        }
        if (!aFinalString[1].equals("")) {
            deviceBox.setSelectedItem(aFinalString[1]);
            updateLabels(1, deviceBox);
        }
        
        deviceBox.addActionListener(new TransducerBoxesListener(1, deviceBox));
/*         deviceBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    updateLabels(1, cb);
                }
            }); */
    }

    /**
     * TODO
     * @param devSel
     */
    private void setLocationBox(int devSel) {
        HashMap<Integer, String> locations =
            readFile("/SignalTypes/locations.txt");
        ArrayList<Integer> locPriStore = new ArrayList<Integer>();
        ArrayList<Integer> locRefStore = new ArrayList<Integer>();
        locationBox.removeAllItems();
        refLocBox.removeAllItems();
        try {
            InputStream is =
                Main.class.getResourceAsStream("/SignalTypes/procedurelocations.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = in.readLine();
            while (line != null) {
                String[] delimited = line.split(",");
                if (Integer.parseInt(delimited[1]) == devSel) {
                    if (Integer.parseInt(delimited[3]) == 1) {
                        locPriStore.add(Integer.parseInt(delimited[2]));
                    }
                    if (Integer.parseInt(delimited[4]) == 1) {
                        locRefStore.add(Integer.parseInt(delimited[2]));
                    }
                }
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!locPriStore.isEmpty()) {
            locationLabel.setVisible(true);
            locationBox.setVisible(true);
            locationBox.addItem("");
            for (int i = 0; i < locPriStore.size(); i++) {
                locationBox.addItem(locations.get(locPriStore.get(i)));
            }
        } else {
            locationLabel.setVisible(false);
            locationBox.setVisible(false);
        }
        if (!locRefStore.isEmpty()) {
            refLocLabel.setVisible(true);
            refLocBox.setVisible(true);
            refLocBox.addItem("");
            for (int i = 0; i < locRefStore.size(); i++) {
                refLocBox.addItem(locations.get(locRefStore.get(i)));
            }
        } else {
            refLocLabel.setVisible(false);
            refLocBox.setVisible(false);
        }
        //System.out.println("aFinalString[2] = " + aFinalString[2]);
        if (!aFinalString[2].equals("")) {
            locationBox.setSelectedItem(aFinalString[2]);
            updateLabels(2, locationBox);
        }
        
        locationBox.addActionListener(new TransducerBoxesListener(2, locationBox));
        /* locationBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    updateLabels(2, cb);
                }
            }); */
        
        if (!aFinalString[3].equals("")) {
            refLocBox.setSelectedItem(aFinalString[3]);
            updateLabels(3, refLocBox);
        }
        
        refLocBox.addActionListener(new TransducerBoxesListener(3, refLocBox));
/*         refLocBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    System.out.println(refLocBox ==  cb);
                    updateLabels(3, cb);
                }
            }); */
    }
    
    /**
     * TODO
     * Fangping, 08/10/2010
     */
    private class TransducerBoxesListener implements ActionListener {
        private int option;
        private JComboBox box;
        TransducerBoxesListener(int opt, JComboBox bx) {
            option = opt; box = bx;
        }
        public void actionPerformed(ActionEvent e) {
            updateLabels(option, box);
        }
    }

    /**
     * TODO
     */
    private void visualize() {
        this.setTitle("Customize Transducer Type");
        setLogo();
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
    }

    /**
     * TODO
     * @param loc
     * @return
     */
    private HashMap<Integer, String> readFile(String loc) {
        HashMap<Integer, String> ret = new HashMap<Integer, String>();
        ret.put(0, "");
        try {
            InputStream is = this.getClass().getResourceAsStream(loc);
            BufferedReader in = new BufferedReader(new InputStreamReader(is)); 

            String line = in.readLine();
            while (line != null) {
                String[] delimited = line.split(",");
                ret.put(Integer.parseInt(delimited[0]),
                        delimited[1].substring(1, delimited[1].length() - 1));
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ret;
    }
    
    /**
     * TODO
     * @param loc
     * @return
     */
    private HashMap<String, Integer> revHReadFile(String loc) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        ret.put("", 0);
        try {
            InputStream is = this.getClass().getResourceAsStream(loc);
            BufferedReader in = new BufferedReader(new InputStreamReader(is)); 

            String line = in.readLine();
            while (line != null) {
                String[] delimited = line.split(",");
                ret.put(delimited[1].substring(1, delimited[1].length() - 1), Integer.parseInt(delimited[0]));
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ret;
    }
    
    /**
     * create the main panel
     * Fangping, 08/09/2010
     * TODO
     * @return
     */
    private JPanel createMainPanel() {
        FormLayout layout = new FormLayout(
                                        "2dlu, pref, 4dlu, 56dlu:grow, 8dlu", //columns
                                        "20dlu, 4dlu, 20dlu, 4dlu,20dlu, 4dlu, 20dlu, 4dlu, 4dlu, 4dlu, 20dlu, 4dlu"); //rows
        //layout.setColumnGroups(new int[][]{{2, 4}});
        layout.setRowGroups(new int[][]{{2, 4, 6, 8, 10}});        
        JPanel mainPanel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        mainPanel.add(procedureLabel, cc.xy(2, 1));
        mainPanel.add(deviceLabel, cc.xy(2, 3));
        mainPanel.add(locationLabel, cc.xy(2,5));
        mainPanel.add(refLocLabel, cc.xy(2, 7));
        mainPanel.add(new JSeparator(JSeparator.HORIZONTAL), cc.xyw(1, 9, 5));
        //mainPanel.add(summaryLabel, cc.xy(2, 11));
          
        mainPanel.add(procedureBox, cc.xy(4, 1));
        mainPanel.add(deviceBox, cc.xy(4, 3));
        mainPanel.add(locationBox, cc.xy(4, 5));
        mainPanel.add(refLocBox, cc.xy(4, 7));
        mainPanel.add(finalValueLabel, cc.xywh(2, 11, 4, 1)); 
        
        String title = " ";
        mainPanel.setBorder(BorderFactory.createTitledBorder(title));       
             
        return mainPanel;      
    }

    /**
     * TODO
     */
    private void setDialogLayout() {

/*         JPanel specPanel = new JPanel(new GridLayout(5, 1));

        JPanel procedurePanel = createPanel(procedureLabel, procedureBox);
        JPanel devicePanel = createPanel(deviceLabel, deviceBox);
        JPanel locationPanel = createPanel(locationLabel, locationBox);
        JPanel refLocPanel = createPanel(refLocLabel, refLocBox);
        JPanel finalPanel1 = new JPanel();
        JPanel finalPanel2 = new JPanel();
        finalPanel2.add(finalString);
        finalPanel2.add(finalStringCount);
        finalPanel1.add(finalStringLabel);
        finalPanel1.add(finalPanel2); 

        specPanel.add(procedurePanel);
        specPanel.add(devicePanel);
        specPanel.add(locationPanel);
        specPanel.add(refLocPanel);
        specPanel.add(finalPanel1); */
        
        //Fangping, 08/09/2010
        JPanel specPanel = createMainPanel();

        //specPanel.setPreferredSize(new Dimension(dialogWidth, dialogHeight));
        //specPanel.setMaximumSize(new Dimension(dialogWidth, dialogHeight));

        JPanel controlPanel = createControlPanel();

        this.getContentPane().add(createTipPanel(), BorderLayout.NORTH);
        this.getContentPane().add(specPanel, BorderLayout.CENTER);
        this.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * TODO
     * @param label
     * @param cBox
     * @return
     */
    public JPanel createPanel(JLabel label, JComboBox cBox) {

        JPanel selectionPanel = new JPanel();
        selectionPanel.add(label);
        selectionPanel.add(cBox);

        return selectionPanel;
    }

    /**
     * TODO
     * @return
     */
    public JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        controlPanel.setPreferredSize(new Dimension(dialogWidth, 40));

        controlPanel.add(finishButton);
        controlPanel.add(cancelButton);

        return controlPanel;
    }

    /**
     * TODO
     * @return
     */
    public JPanel createTipPanel() {
        JPanel tipPanel = new JPanel();
        tipPanel.setMinimumSize(new Dimension(dialogWidth, 40));
        tipPanel.setPreferredSize(new Dimension(dialogWidth, 40));
        
        String title = "Customize transducer signal type. Start with selecting procedure.";
        JLabel tipLabel =
            new JLabel(title, JLabel.LEFT);
        tipLabel.setHorizontalAlignment(JLabel.LEADING);
        tipPanel.add(tipLabel);
        tipPanel.setBackground(new Color(255, 240, 188));
        tipPanel.setBorder(BorderFactory.createEtchedBorder());

        return tipPanel;
    }

    /**
     * TODO
     * @param part
     * @param cb
     */
    private void updateLabels(int part, JComboBox cb) {        
        
        aFinalString[part] = (String)cb.getSelectedItem(); 
        /*
         * aFinalSting[part] might be null from the prior assignment, so
         * we have to explicitly force its non-nullness.
         * Fangping, 08/10/2010
        */
        if (aFinalString[part] == null)
            aFinalString[part] = "";

        String finalLabel = "";
        for (int i = 0; i < aFinalString.length; i++) {
            if (aFinalString[i] != null) {
                if (!aFinalString[i].equals("")) {
                    switch (i) {
                    case 0:
                        {
                            finalLabel = aFinalString[i];
                            break;
                        }
                    case 1:
                        {
                            finalLabel += COMMA + aFinalString[i];
                            break;
                        }
                    case 2:
                        {
                            finalLabel += AT + aFinalString[i];
                            break;
                        }
                    case 3:
                        {
                            finalLabel += REFERENCED_TO + aFinalString[i];
                            break;
                        }
                    }
                    ;
                }
            }
        }
 
        finalValueLabel.setText(finalLabel);
        //finalString.setText(finalLabel);
        //finalStringCount.setText(Integer.toString(finalLabel.length()));
    }

    /**
     * TODO
     */
    class FinishButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            String value = finalValueLabel.getText();
            if (value.length() > 80){
                JOptionPane.showMessageDialog(null,
                             "Transducer label is longer than 80 characters. Label will be truncated.",
                             "Transducer Label Too Long",
                            JOptionPane.WARNING_MESSAGE);
                value = value.substring(0, 80);                
            }
            esa.setValueAt(value, row, col);
            dispose();
        }


    } //end of FinishButtonListener class

    /**
     * TODO
     */
    class CancelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    /**
     * TODO	
     */
    class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            dispose();
        }
    }

    /**
     * TODO
     */
    private void setLogo() {
        BufferedImage image = null;
        try {
            image =
                    ImageIO.read(this.getClass().getResource("/icon/mimilogo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setIconImage(image);
    }
}
