package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import sides.Side;
import simulation.GBWorld;
import ui.GBApplication;
import support.StringUtilities;

public class GBRosterView extends JPanel {
	/**
	 * 
	 */
	static final int kFramecounterHeight = 15;
	static final int kSideBoxHeight = 17;
	static final int kPopulationWidth = 50;

	private static final long serialVersionUID = 6135247814368773456L;
	GBWorld world;
	GBApplication app;
	int margin = 6;
	int padding = 2;
	int slotMargin = 3;
	int fps;
	int lastFrame;
	long lastTime;
	List<Rectangle> slots;

	public GBRosterView(GBApplication _app) {
		app = _app;
		world = app.world;
		slots = new ArrayList<Rectangle>();
		setLayout(new BorderLayout());
		setBackground(Color.lightGray);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// Select side on click
				app.selectedSide = null;
				for (int i = 0; i < slots.size(); i++)
					if (slots.get(i).contains(arg0.getPoint())) {
						app.selectedSide = world.Sides().get(i);
					}
				repaint();
			}
		};
		addMouseListener(ma);
		lastTime = System.currentTimeMillis();
		setVisible(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	void draw(Graphics g) {
		slots.clear();
		Graphics2D g2d = (Graphics2D) g;
		Font f = new Font("Serif", Font.PLAIN, 10);
		g2d.setFont(f);
		FontMetrics fm = g.getFontMetrics();

		// Header
		Rectangle hdr = new Rectangle(margin, margin, getBounds().width
				- margin * 2, fm.getHeight() + padding * 2);
		g2d.setPaint(Color.white);
		g2d.fill(hdr);
		g2d.setColor(Color.black);
		g2d.draw(hdr);
		Rectangle textRect = new Rectangle(hdr);
		textRect.grow(-padding, -padding);
		String text = String.format("Frame %d", world.CurrentFrame());
		StringUtilities.drawStringLeft(g2d, text, textRect, 10, Color.black);
		String status = world.tournament ? "tournament " : ""
				+ (world.running ? "running" : "paused");
		if (world.running && lastTime >= 0 && world.CurrentFrame() > lastFrame) {
			int frames = world.CurrentFrame() - lastFrame;
			int ms = (int) (System.currentTimeMillis() - lastTime);
			if (ms > 0)
				status += " at " + frames * 1000 / ms + " fps";
		}
		StringUtilities.drawStringRight(g2d, status, textRect, 10, Color.black);

		// Sides
		for (int i = 0; i < world.Sides().size(); i++) {
			f = new Font("Serif", Font.PLAIN, 12);
			g.setFont(f);
			Rectangle slot = new Rectangle(margin, 0, getBounds().width
					- margin * 2, fm.getHeight() + padding * 2);
			Side side = world.Sides().get(i);
			if (side == null)
				continue;
			int y = hdr.height + margin + slotMargin
					+ (slot.height + slotMargin) * i;
			slot.setLocation(margin, y);
			slots.add(slot);
			g2d.setPaint(side.equals(app.selectedSide) ? Color.black
					: Color.white);
			g2d.fill(slot);
			g2d.setColor(side.equals(app.selectedSide) ? Color.white
					: Color.black);
			g2d.draw(slot);
			textRect = new Rectangle(slot);
			textRect.grow(-padding, -padding);
			// Side ID
			StringUtilities.drawStringLeft(g2d, String.format("%d.", side.ID()), 
					textRect, 12, side.Color().ContrastingTextColor());
			// Side Name
			StringUtilities.drawStringLeft(g2d, side.Name(), 
					new Rectangle(textRect.x + 25, textRect.y, textRect.width-25, textRect.height), 
					12, side.equals(app.selectedSide) ? Color.white
							: Color.black);
			if (side.Scores().Seeded() != 0) {
				// Side status
				if (side.Scores().Population() != 0) {
					// Sterile?
					if (side.Scores().sterile != 0) {
						text = String.format("Sterile at %d", side.Scores()
								.SterileTime());
						StringUtilities.drawStringRight(g2d, text, textRect, 10, new Color(1,0,1));
					} else {
						// Doing fine
						// Bio percentage
						text = StringUtilities.toPercentString(side.Scores()
								.BiomassFraction(), 1);
						StringUtilities.drawStringLeft(g2d, text, 
								new Rectangle(textRect.x + textRect.width - padding - kPopulationWidth,
										textRect.y, textRect.width, textRect.height), 
								12, Color.black);
						// Population
						text = Integer.toString(side.Scores().Population());
						StringUtilities.drawStringRight(g2d, text, textRect, 10, Color.blue);
					}
				} else {
					// Extinct
					text = String.format("Extinct at %d", side.Scores()
							.ExtinctTime());
					StringUtilities.drawStringRight(g2d, text, textRect, 10, Color.darkGray);
				}
			}
		}
		lastFrame = world.CurrentFrame();
		lastTime = System.currentTimeMillis();
	}
}
