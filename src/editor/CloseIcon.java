package editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * TODO
 */
class CloseIcon implements Icon {

	/**
	 * TODO
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.RED);
		g.drawLine(6, 6, getIconWidth() - 7, getIconHeight() - 7);
		g.drawLine(getIconWidth() - 7, 6, 6, getIconHeight() - 7);
	}
	
	/**
	 * Returns the width of this icon 
	 */
	public int getIconWidth() {
		return 20;
	}
	
	/**
	 * Returns the height of this icon
	 */
	public int getIconHeight() {
		return 20;
	}
}

