package translator.gui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import translator.utils.Keywords;

public class QuickViewerGUI extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	public String filename;
	private JTextArea textArea;
	
	JButton JButton_Refresh, JButton_Remove;
	
	public QuickViewerGUI(String filename, boolean canClose) {
		
		/* (1) Background */
		this.setLayout(new GridBagLayout());
		this.setBackground(Keywords.background);
		this.filename = filename;
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel("                                                                                                                                                                            "), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (2) "filename" Title */
		LayoutManager.addFirstField(new JLabel("Filename: "), this);
		JTextField JTextField_filename = new JTextField(filename);
		JTextField_filename.setEditable(false);
		LayoutManager.addMiddleField(JTextField_filename, this);
		
		/* (3) "Two-button Panel" */
		JPanel JPanel_TwoButtons = new JPanel();
		JPanel_TwoButtons.setLayout(new GridBagLayout());
		JPanel_TwoButtons.setBackground(Keywords.background);
		LayoutManager.addFirstField(JPanel_TwoButtons, this);
		
		/* (4) "Refresh" Button */
		JButton_Refresh = new JButton("Refresh");
		JButton_Refresh.addActionListener(this);
		LayoutManager.addFirstField(JButton_Refresh, JPanel_TwoButtons);
		
		/* (5) "Close" Button */
		JButton_Remove = new JButton("Close");
		JButton_Remove.addActionListener(this);
		JButton_Remove.setEnabled(canClose);
		JButton_Remove.setVisible(canClose);
		LayoutManager.addFirstField(JButton_Remove, JPanel_TwoButtons);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel("                                                                                                                                                          "), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (6) TextArea */
		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scroll = new JScrollPane (textArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		LayoutManager.addItemList(scroll, this);

		/* ==="Layout Separator"=== */
		LayoutManager.addFirstField(new JLabel(""), this);
		LayoutManager.addMiddleField(new JLabel(""), this);
		LayoutManager.addLastField(new JLabel(""), this);
		
		/* (7) Fill TextArea with a file */
		loadFilename(filename);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == JButton_Refresh) {
			loadFilename(filename);
		}
		else if (e.getSource() == JButton_Remove) {
			SubWindowGUI.removeViewerTab(this);
		}
	}
	
	private void loadFilename(String filename) {
		
		StringBuffer content = new StringBuffer();
		
		File f = new File(filename);
		if (f.exists()){
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
				String inline;
				while ((inline = in.readLine()) != null){
					content.append(inline);
					content.append("\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		textArea.setText(content.toString());
	}

}
