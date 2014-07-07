package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;

public abstract class ListView extends JPanel {
	/**
	 * Ancestor class for various views
	 */
	private static final long serialVersionUID = -3346698982530634063L;
	public Rectangle header;
	public Rectangle items;
	public Rectangle footer;
	public ArrayList<Rectangle> itemlist;
	
	public int preferredWidth;
	public int padding;
	public int margin;
	public int itemmargin;
	
	public Color boxFillColor = Color.white;
	public Color boxBorderColor = Color.black;
	public Color boxSelectedColor = Color.black;
	
	public ListView(){
		itemlist = new ArrayList<Rectangle>();
		padding = 2;
		margin = 6;
		itemmargin = 4;
		preferredWidth = 0;
		header = new Rectangle(0,0,0,0);
		items = new Rectangle(0,0,0,0);
		footer = new Rectangle(0,0,0,0);
	}
	
	protected void addItem(Rectangle item){
		if (item == null)
			return;
		itemlist.add(item);
		items.setSize(items.width, items.height + item.height + itemmargin);
	}
	
	protected Rectangle getItemRect(int index, int fontHeight, boolean bold) {
		int itemHeight = getFontMetrics(new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontHeight)).getHeight() + padding * 2;
		int y = margin + header.height + itemmargin;
		for(int i = 0;i< index && i < itemlist.size();i++)
			y += itemlist.get(i).height + itemmargin;
		return new Rectangle(header.x, y, header.width, itemHeight);
	}
	
	Rectangle getStartingHeaderRect(int fontSize, boolean bold){
		return new Rectangle(margin, margin, getWidth() - margin * 2, 
				getFontMetrics(new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontSize)).getHeight() + padding * 2);
	}
	
	Rectangle getStartingItemsRect(int fontSize, boolean bold){
		return new Rectangle(header.x, margin + header.height + itemmargin, header.width,
				getFontMetrics(new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontSize)).getHeight() + padding * 2);
	}	
	
	Rectangle getStartingFooterRect(int fontSize, boolean bold){
		return new Rectangle(header.x, margin + header.height + items.height + itemmargin * 2, 
				header.width,
				getFontMetrics(new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontSize)).getHeight() + padding * 2);
	}
	
	public int getPreferredHeight(){
		return margin * 2 + itemmargin * 2 + header.height + items.height + footer.height;
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(preferredWidth, getPreferredHeight());
	}
	
	protected void draw(Graphics2D g){
		Graphics2D g2d = (Graphics2D)g;
		header = drawHeader(g2d);
		items = drawItems(g2d);
		footer = drawFooter(g2d);
	}
	
	protected void drawBox(Graphics2D g, Rectangle box, boolean selected){
		g.setPaint(selected ? boxSelectedColor : boxFillColor);
		g.fill(box);
		g.setColor(boxBorderColor);
		g.draw(box);
	}
	
	protected void drawBox(Graphics2D g, Rectangle box){
		drawBox(g, box, false);
	}
	/**
	 * Draws the list header and returns a rectangle containing it
	 * @param g
	 * @return
	 */
	abstract Rectangle drawHeader(Graphics2D g);
	/**
	 * Draws all members of the list and returns a rectangle containing them
	 * @param g
	 * @return
	 */
	abstract Rectangle drawItems(Graphics2D g);
	/**
	 * Draws one member of the list and returns a rectangle containing it
	 * @param g
	 * @return
	 */
	abstract Rectangle drawOneItem(Graphics2D g, int index);
	/**
	 * Draws the list footer and returns a rectangle containing it
	 * @param g
	 * @return
	 */
	abstract Rectangle drawFooter(Graphics2D g);
}
