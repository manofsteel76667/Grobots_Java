package views;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import support.StringUtilities;

public class AboutBox extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4701878784219855320L;

	public AboutBox() {
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);
		setBackground(Color.black);
		StringUtilities.drawStringCentered(g2d, "Grobots", getWidth() / 2, 50,
				40, Color.green);
		StringUtilities.drawStringCentered(g2d, "by Devon and Warren Schudy",
				getWidth() / 2, 75, 12, Color.white);
		// TODO get actual build date from manifest?
		StringUtilities.drawStringCentered(g2d, "built July 22, 2014 for Java",
				getWidth() / 2, 95, 10, Color.magenta);
		StringUtilities.drawStringLeft(g2d, "Additional contributors:", 15,
				115, 10, Color.white);
		StringUtilities.drawStringLeft(g2d, "Tilendor", 35, 128, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d, "Daniel von Fange", 35, 138, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d, "Borg", 35, 148, 10, Color.white);
		StringUtilities.drawStringLeft(g2d, "Eugen Zagorodniy", 35, 158, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d, "Rick Manning", 35, 168, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d, "Mike Anderson", 35, 178, 10,
				Color.white);
		StringUtilities.drawStringCentered(g2d,
				"http://grobots.sourceforge.net/", getWidth() / 2, 195, 10,
				new Color(0, 0.7f, 1));
		StringUtilities.drawStringLeft(g2d,
				"Grobots comes with ABSOLUTELY NO WARRANTY.", 10, 208, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d,
				"This is free software, and you are welcome to", 10, 218, 10,
				Color.white);
		StringUtilities.drawStringLeft(g2d,
				"redistribute it under the GNU General Public License.", 10,
				228, 10, Color.white);
	}
}
