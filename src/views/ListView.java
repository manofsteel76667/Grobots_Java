package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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

	public int size;

	public Color boxFillColor = Color.white;
	public Color boxBorderColor = Color.black;
	public Color boxSelectedColor = Color.black;

	public ListView() {
		itemlist = new ArrayList<Rectangle>();
		padding = 2;
		margin = 6;
		itemmargin = 4;
		preferredWidth = 0;
		header = new Rectangle(0, 0, 0, 0);
		items = new Rectangle(0, 0, 0, 0);
		footer = new Rectangle(0, 0, 0, 0);
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				int ret = -1;
				for (int i = 0; i < itemlist.size(); i++)
					if (itemlist.get(i).contains(arg0.getPoint())) {
						ret = i;
						break;
					}
				itemClicked(ret);
				repaint();
			}
		};
		addMouseListener(ma);
	}

	/*
	 * Bit of a hack, but allows drawing of the panel when it's not visible so
	 * getPreferredSize() works on dialogs
	 */
	public BufferedImage drawInBackground() {
		BufferedImage image = new BufferedImage(1280, 1024,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		draw(g);
		Dimension d = getPreferredSize();
		return image.getSubimage(0, 0, d.width, d.height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw((Graphics2D) g);
	}

	protected void itemClicked(int index) {
		// Override if selectable items are desired
	}

	protected void addItem(Rectangle item) {
		if (item == null)
			return;
		itemlist.add(item);
		items.setSize(items.width, items.height + item.height + itemmargin);
	}

	Rectangle getStartingHeaderRect(int fontSize, boolean bold) {
		return new Rectangle(margin, margin, getWidth() - margin * 2,
				getFontMetrics(
						new Font("Serif", bold ? Font.BOLD : Font.PLAIN,
								fontSize)).getHeight()
						+ padding * 2);
	}

	Rectangle getStartingItemsRect() {
		return new Rectangle(header.x, header.y + header.height + itemmargin,
				header.width, 0);
	}

	protected Rectangle getStartingItemRect(int index, int fontHeight,
			boolean bold) {
		int itemHeight = getFontMetrics(
				new Font("Serif", bold ? Font.BOLD : Font.PLAIN, fontHeight))
				.getHeight() + padding * 2;
		int y = header.y + header.height + itemmargin;
		for (int i = 0; i < index && i < itemlist.size(); i++)
			y += itemlist.get(i).height + itemmargin;
		return new Rectangle(header.x, y, header.width, itemHeight);
	}

	Rectangle getStartingFooterRect(int fontSize, boolean bold) {
		return new Rectangle(items.x, items.y + items.height + itemmargin,
				items.width, getFontMetrics(
						new Font("Serif", bold ? Font.BOLD : Font.PLAIN,
								fontSize)).getHeight()
						+ padding * 2);
	}

	public int getPreferredHeight() {
		int h = margin;
		h += header == null ? 0 : header.height;
		h += items == null ? 0 : items.height + itemmargin;
		h += footer == null ? 0 : footer.height + itemmargin;
		h += margin;
		return h;
	}

	@Override
	public Dimension getPreferredSize() {
		// Only works after it's been drawn once. Ugh.
		return new Dimension(preferredWidth, getPreferredHeight());
	}

	protected void draw(Graphics2D g) {
		header = new Rectangle(0, 0, 0, 0);
		items = new Rectangle(0, 0, 0, 0);
		footer = new Rectangle(0, 0, 0, 0);
		itemlist.clear();
		size = setLength();
		header = drawHeader(g);
		if (header == null)
			header = new Rectangle(0, 0, 0, 0);
		items = getStartingItemsRect();
		for (int i = 0; i < size; i++) {
			Rectangle r = drawOneItem(g, i);
			if (r != null)
				addItem(r);
		}
		footer = drawFooter(g);
	}

	protected void drawBox(Graphics2D g, Rectangle box, boolean selected) {
		g.setPaint(selected ? boxSelectedColor : boxFillColor);
		// g.fill(box);
		g.fillRoundRect(box.x, box.y, box.width, box.height, 10, 10);
		g.setColor(boxBorderColor);
		// g.draw(box);
		g.drawRoundRect(box.x, box.y, box.width, box.height, 10, 10);
	}

	protected void drawBox(Graphics2D g, Rectangle box) {
		drawBox(g, box, false);
	}

	/**
	 * Draws the list header and returns a rectangle containing it
	 * 
	 * @param g
	 * @return
	 */
	abstract Rectangle drawHeader(Graphics2D g);

	/**
	 * Draws one member of the list and returns a rectangle containing it
	 * 
	 * @param g
	 * @return
	 */
	abstract Rectangle drawOneItem(Graphics2D g, int index);

	/**
	 * Draws the list footer and returns a rectangle containing it
	 * 
	 * @param g
	 * @return
	 */
	abstract Rectangle drawFooter(Graphics2D g);

	/**
	 * Set list length
	 */
	abstract int setLength();
}
