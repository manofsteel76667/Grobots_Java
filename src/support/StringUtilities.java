/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
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

	public static void drawStringRight(Graphics2D g, String text,
			Rectangle rect, int fontHeight, Color c) {
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, rect.x + rect.width - fm.stringWidth(text), rect.y
				+ rect.height);
	}

	public static void drawStringLeft(Graphics2D g, String text,
			Rectangle rect, int fontHeight, Color c) {
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		g.setColor(c);
		g.drawString(text, rect.x, rect.y + rect.height);
	}

	public static void drawStringCenter(Graphics2D g, String text,
			Rectangle rect, int fontHeight, Color c) {
		Font f = new Font("Serif", Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, (int) rect.getCenterX() - fm.stringWidth(text) / 2,
				rect.y + rect.height);
	}

	public static void drawStringRight(Graphics2D g, String text, int x, int y,
			int fontHeight, Color c) {
		drawStringRight(g, text, x, y, fontHeight, c, false);
	}

	public static void drawStringRight(Graphics2D g, String text, int x, int y,
			int fontHeight, Color c, boolean bold) {
		Font f = new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, x - fm.stringWidth(text), y);
	}

	public static void drawStringLeft(Graphics2D g, String text, int x, int y,
			int fontHeight, Color c) {
		drawStringLeft(g, text, x, y, fontHeight, c, false);
	}

	public static void drawStringLeft(Graphics2D g, String text, int x, int y,
			int fontHeight, Color c, boolean bold) {
		Font f = new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontHeight);
		g.setFont(f);
		g.setColor(c);
		g.drawString(text, x, y);
	}

	public static void drawStringCentered(Graphics2D g, String text, int x,
			int y, int fontHeight, Color c) {
		drawStringCentered(g, text, x, y, fontHeight, c, false);
	}

	public static void drawStringCentered(Graphics2D g, String text, int x,
			int y, int fontHeight, Color c, boolean bold) {
		Font f = new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontHeight);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		g.drawString(text, x - fm.stringWidth(text) / 2, y);
	}

	public static void drawStringPair(Graphics2D g, String str1, String str2,
			int left, int right, int y, int size, Color color, boolean bold) {
		drawStringLeft(g, str1, left, y, size, color, bold);
		drawStringRight(g, str2, right, y, size, color, bold);
	}

	public static String makeTableCell(int cellWidth, String format,
			Color color, Object... cellValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("<td width=");
		sb.append(cellWidth);
		if (color != null) {
			sb.append("><font color=");
			sb.append(GBColor.toHex(color));
		}
		sb.append(">");
		sb.append(String.format(format, cellValue));
		sb.append("</td>");
		return sb.toString();
	}

	static final int numberWidth = 20;

	public static String makeTableCell(String format, Color color,
			Object... cellValue) {
		return makeTableCell(numberWidth, format, color, cellValue);
	}

	public static String makeEmptyTableCell() {
		return "<td></td>";
	}

	public static String makeEmptyTableCells(int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++)
			sb.append(makeEmptyTableCell());
		return sb.toString();
	}
}
