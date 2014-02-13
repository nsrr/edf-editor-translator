package editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

class CloseIcon implements Icon {
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(Color.RED);
    g.drawLine(6, 6, getIconWidth() - 7, getIconHeight() - 7);
    g.drawLine(getIconWidth() - 7, 6, 6, getIconHeight() - 7);
  }
  public int getIconWidth() {
    return 20;
  }
  public int getIconHeight() {
    return 20;
  }
}

