package support;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class StringUtilities {

	public static Integer parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public static Double parseDouble(String str) {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public static String toPercentString(double f, int digitsAfterDP) {
		String formatstring = "%." + digitsAfterDP + "f";
		return String.format(formatstring, f * 100) + "%";
	}
	
	public static void drawStringRight(Graphics2D g, String text, Rectangle rect,
			int fontHeight, Color c){
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, rect.x + rect.width - fm.stringWidth(text), rect.y + rect.height);
	}
	public static void drawStringLeft(Graphics2D g, String text, Rectangle rect,
			int fontHeight, Color c){
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		g.setColor(c);
		g.drawString(text, rect.x, rect.y + rect.height);
	}
	public static void drawStringCenter(Graphics2D g, String text, Rectangle rect,
			int fontHeight, Color c){
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, (int)rect.getCenterX() - fm.stringWidth(text)/2, rect.y + rect.height);
	}
}
