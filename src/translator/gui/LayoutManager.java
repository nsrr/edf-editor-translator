package translator.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LayoutManager {
	
	private static GridBagConstraints first;
	private static GridBagConstraints last;
	private static GridBagConstraints middle;
	
	static {
		// Set up grid bag constraint for last components
		last = new GridBagConstraints();
		last.weightx = 0.0;
		last.fill = GridBagConstraints.HORIZONTAL;
		last.anchor = GridBagConstraints.NORTHWEST;
		last.gridwidth = GridBagConstraints.REMAINDER;
		last.insets = new Insets(1, 1, 1, 1);
		
		// Set up grid bag constraint for middle components
		middle = (GridBagConstraints) last.clone();
		middle.anchor = GridBagConstraints.CENTER;
		middle.gridwidth = GridBagConstraints.RELATIVE;
		
		// Set up grid bag constraint for first components
		first = (GridBagConstraints) last.clone();
		first.weightx = 0.0;
		first.gridwidth = 1;
	}
	
	public static void addFirstField(Component c, Container parent){
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, first);
        parent.add(c);
	}
	
	public static void addMiddleField(Component c, Container parent){
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, middle);
        parent.add(c);
	}
	
	public static void addLastField(Component c, Container parent){
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, last);
        parent.add(c);
	}
	
	public static void addItemList(Component c, Container parent){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(1, 1, 1, 1);
		parent.add(c, gbc);
	}
}
