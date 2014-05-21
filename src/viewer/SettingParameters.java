package viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import configure.ConfigureManager;

public class SettingParameters extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private static SettingParameters sp = null;
	protected JLabel chosenDirectory = null;
	
	public SettingParameters(final String dialogType){
		
		super();
		
		if (dialogType.equals("MCR_Dir")){
			this.setTitle("Setting Directory of MATLAB Compiler Runtime (MCR)");
		}
		else if (dialogType.equals("Viewer_Dir")){
			this.setTitle("Setting Directory of EDF Viewer");
		}
		else{
			this.setTitle("");
		}
		this.setSize(600, 300);
		this.setLayout(new GridLayout(3, 1));
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = dimension.width / 2 - this.getWidth() / 2;
	    int y = dimension.height / 2 - this.getHeight() / 2;
	    this.setLocation(x, y);
	    
	    //controlPanel1
		JPanel controlPanel1 = new JPanel();
		controlPanel1.setLayout(new FlowLayout());
		this.add(controlPanel1);
		JButton button = new JButton("Select Directory");
		controlPanel1.add(button);
		
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				String path = chosenDirectory.getText();
				path = path.equals("") ? System.getProperty("user.dir") : path;
				chooser.setCurrentDirectory(new File(path));
				if (chooser.showOpenDialog(sp) == JFileChooser.APPROVE_OPTION) {
					File folder = chooser.getSelectedFile();
					chosenDirectory.setText(folder.getAbsolutePath());
					chosenDirectory.setForeground(Color.BLUE);
					chosenDirectory.setFont(new Font("Serif", Font.BOLD, 14));
					ConfigureManager.addOrUpdateConfiguration(dialogType, folder.getAbsolutePath());
				}
			}
		});
		
		//controlPanel2
		JPanel controlPanel2 = new JPanel();
		controlPanel2.setLayout(new GridLayout(2, 1));
		this.add(controlPanel2);
		JLabel label = new JLabel(dialogType, JLabel.CENTER);
		controlPanel2.add(label);
		chosenDirectory = new JLabel("", JLabel.CENTER);
		controlPanel2.add(chosenDirectory);
	}

}