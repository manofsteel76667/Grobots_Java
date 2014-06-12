package support;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GBGraphics {
	public static void drawOval(Graphics g, Rectangle where, Color c){
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(c);
		g2.drawOval((int)where.getCenterX(), (int)where.getCenterY(), 
				(int)where.getWidth(), (int)where.getHeight());
	}
	public static void drawRect(Graphics g, Rectangle where, Color c){
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(c);
		g2.drawRect((int)where.getX(), (int)where.getY(), (int)where.getWidth(), (int)where.getHeight());
	}
	public static void fillOval(Graphics g, Rectangle where, Color c){
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(c);
		g2.fillOval((int)where.getCenterX(), (int)where.getCenterY(), 
				(int)where.getWidth(), (int)where.getHeight());
	}
	public static void fillRect(Graphics g, Rectangle where, Color c){
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(c);
		g2.fillRect((int)where.getX(), (int)where.getY(), (int)where.getWidth(), (int)where.getHeight());
	}
	public static void drawLine(Graphics g, int x1, int y1, int x2, int y2, Color c, int weight){
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(c);
		g2.setStroke(new BasicStroke(weight));
		g2.drawLine(x1, y1, x2, y2);
	}
}
