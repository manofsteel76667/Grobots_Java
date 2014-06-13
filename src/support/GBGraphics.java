package support;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

public class GBGraphics {
	public static void drawOval(Graphics g, Rectangle where, Color c) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.drawOval((int) where.getCenterX(), (int) where.getCenterY(),
				(int) where.getWidth(), (int) where.getHeight());
	}

	public static void drawRect(Graphics g, Rectangle where, Color c) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.drawRect((int) where.getX(), (int) where.getY(),
				(int) where.getWidth(), (int) where.getHeight());
	}

	public static void fillOval(Graphics g, Rectangle where, Color c) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.fillOval((int) where.getCenterX(), (int) where.getCenterY(),
				(int) where.getWidth(), (int) where.getHeight());
	}

	public static void fillRect(Graphics g, Rectangle where, Color c) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.fillRect((int) where.getX(), (int) where.getY(),
				(int) where.getWidth(), (int) where.getHeight());
	}

	public static void drawLine(Graphics g, int x1, int y1, int x2, int y2,
			Color c) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.drawLine(x1, y1, x2, y2);
	}

	public static void drawLine(Graphics g, double x1, double y1, double x2,
			double y2, Color c) {
		drawLine(g, (int) x1, (int) y1, (int) x2, (int) y2, c);
	}

	public static Stroke dashedStroke(float dash) {
		float dash1[] = { 10.0f };
		BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, dash, dash1, 0.0f);
		return dashed;
	}

	public static void drawArc(Graphics g, Rectangle where, int startAngle,
			int length, Color c, int width) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(c);
		g2.setStroke(new BasicStroke(width));
		g2.drawArc((int) where.getX(), (int) where.getY(),
				(int) where.getWidth(), (int) where.getHeight(), startAngle,
				length);
	}
}
