package viewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;

public class SetViewerLocation{

	private static SetViewerLocation setting = null;
	private static JFrame mainFrame;
	private static JLabel statusLabel;
	private static JList jList;
	
	public static void main() {
		
		if (setting==null){
			
			setting = new SetViewerLocation();
			
			mainFrame = new JFrame("Tell us where are the compiled Matlab EDF-Viewers");
			mainFrame.setSize(600, 300);
			mainFrame.setLayout(new GridLayout(3, 1));
			Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			int x = dimension.width / 2 - mainFrame.getWidth() / 2;
		    int y = dimension.height / 2 - mainFrame.getHeight() / 2;
		    mainFrame.setLocation(x, y);
			
		    
			JPanel controlPanel = new JPanel();
			controlPanel.setLayout(new FlowLayout());
			controlPanel.setSize(550, 100);
			mainFrame.add(controlPanel);
			JButton button1 = new JButton("Locate Viewer Directory");
			controlPanel.add(button1);
			
			final JFileChooser chooser = new JFileChooser();
			if (ViewerLocations.viewer_dir != null)
				chooser.setCurrentDirectory(new File(ViewerLocations.viewer_dir));
			else
				chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				
			chooser.setDialogTitle("Select Directory of Compiled Matlab-Viewers");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String dir = ViewerLocations.viewer_dir == null ? null : ViewerLocations.viewer_dir;
					if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
						File folder = chooser.getSelectedFile();
						dir = folder.getAbsolutePath() + File.separator;	
					}
					if (dir!=null){
						statusLabel.setText("Directory Selected :" + dir);
						ViewerLocations.viewer_dir = dir;
						
						@SuppressWarnings("unchecked")
						Collection<File> fileCollection = FileUtils.listFiles(
								new File(dir), new String[]{"exe", "EXE"}, true);
						if (fileCollection != null){
							String[] files = new String[fileCollection.size()];
							int i = 0;
							for (File file : fileCollection)
								files[i++] = file.getAbsolutePath();
							jList.setListData(files);
						}
					}
					
				}
			});
			
			statusLabel = new JLabel("", JLabel.CENTER);
			statusLabel.setSize(550, 100);
			mainFrame.add(statusLabel);
			
			jList = new JList();
			JScrollPane jSroll = new JScrollPane(jList);
			jSroll.setPreferredSize(new Dimension(550,300));
			mainFrame.add(jSroll);
		}
		
		mainFrame.setVisible(true);
	}
}
