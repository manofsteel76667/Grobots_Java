/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import sides.Side;
import simulation.GBGame;
import support.GBColor;
import support.StringUtilities;
import ui.SideSelectionListener;
import ui.SideSelector;

public class GBRosterView extends ListView implements SideSelectionListener,
		SideSelector {
	/**
	 * 
	 */
	static final int kFramecounterHeight = 15;
	static final int kSideBoxHeight = 17;
	static final int kPopulationWidth = 50;

	private static final long serialVersionUID = 6135247814368773456L;

	GBGame game;
	int margin = 6;
	int padding = 2;
	int slotMargin = 3;
	int fps;
	long lastFrame;
	long lastTime;
	Side selectedSide;

	List<SideSelectionListener> sideListeners;

	public GBRosterView(GBGame _game) {
		game = _game;
		sideListeners = new ArrayList<SideSelectionListener>();
		lastTime = System.currentTimeMillis();
		preferredWidth = 250;
		setVisible(true);
	}

	@Override
	protected void itemClicked(int index) {
		if (index == -1 || index > game.sides.size()) {
			selectedSide = null;
		} else
			selectedSide = game.sides.get(index);
		notifySideListeners();
	}

	@Override
	Rectangle drawHeader(Graphics2D g) {
		Rectangle hdr = getStartingHeaderRect(10, false);
		drawBox(g, hdr);
		Rectangle textRect = new Rectangle(hdr);
		textRect.grow(-padding * 2, -padding * 2);
		String text = String.format("Frame %d", game.CurrentFrame());
		StringUtilities.drawStringLeft(g, text, textRect, 10, Color.black);
		String status = game.tournament ? "tournament " : ""
				+ (game.running ? "running" : "paused");
		if (game.running && lastTime >= 0 && game.totalFrames > lastFrame) {
			long frames = game.totalFrames - lastFrame;
			int ms = (int) (System.currentTimeMillis() - lastTime);
			if (ms > 0)
				status += " at " + frames * 1000 / ms + " fps";
		}
		StringUtilities.drawStringRight(g, status, textRect, 10, Color.black);
		lastFrame = game.totalFrames;
		lastTime = System.currentTimeMillis();
		return hdr;
	}

	@Override
	Rectangle drawOneItem(Graphics2D g, int index) {
		Side side = game.sides.get(index);
		if (side == null)
			return null;
		Rectangle slot = getStartingItemRect(index, 12, false);
		drawBox(g, slot, side.equals(selectedSide));
		Rectangle textRect = new Rectangle(slot);
		textRect.grow(-padding * 2, -padding * 2);
		// Side ID
		StringUtilities.drawStringLeft(g, String.format("%d.", side.ID()),
				textRect, 12, GBColor.ContrastingTextColor(side.Color()));
		// Side Name
		StringUtilities.drawStringLeft(g, side.Name(), new Rectangle(
				textRect.x + 25, textRect.y, textRect.width - 25,
				textRect.height), 12, side.equals(selectedSide) ? Color.white
				: Color.black);
		String text = "";
		if (side.Scores().Seeded() != 0) {
			// Side status
			if (side.Scores().Population() != 0) {
				// Sterile?
				if (side.Scores().sterile != 0) {
					text = String.format("Sterile at %d", side.Scores()
							.SterileTime());
					StringUtilities.drawStringRight(g, text, textRect, 10,
							new Color(1, 0, 1));
				} else {
					// Doing fine
					// Bio percentage
					text = StringUtilities.toPercentString(side.Scores()
							.BiomassFraction(), 1);
					StringUtilities.drawStringLeft(g, text, new Rectangle(
							textRect.x + textRect.width - padding
									- kPopulationWidth, textRect.y,
							textRect.width, textRect.height), 12, side
							.equals(selectedSide) ? Color.white : Color.black);
					// Population
					text = Integer.toString(side.Scores().Population());
					StringUtilities.drawStringRight(g, text, textRect, 10,
							Color.blue);
				}
			} else {
				// Extinct
				text = String.format("Extinct at %d", side.Scores()
						.ExtinctTime());
				StringUtilities.drawStringRight(g, text, textRect, 10,
						Color.darkGray);
			}
		}
		return slot;
	}

	@Override
	Rectangle drawFooter(Graphics2D g) {
		if (game.sides.size() == 0) {
			Rectangle slot = getStartingFooterRect(20, false);
			drawBox(g, slot);
			slot.grow(-padding, -padding);
			StringUtilities.drawStringCenter(g, "No sides loaded.",
					new Rectangle(slot.x, slot.y, slot.width, slot.height / 2),
					10, Color.blue);
			StringUtilities.drawStringCenter(g,
					"Why not download some from the help menu?", slot, 10,
					Color.blue);
			return slot;
		} else
			return null;

	}

	@Override
	int setLength() {
		return game.sides.size();
	}

	@Override
	public void setSelectedSide(Side side) {
		selectedSide = side;
		repaint();
	}

	void notifySideListeners() {
		for (SideSelectionListener l : sideListeners)
			if (l != null)
				l.setSelectedSide(selectedSide);
	}

	@Override
	public Side getSelectedSide() {
		return selectedSide;
	}

	@Override
	public void addSideSelectionListener(SideSelectionListener listener) {
		if (listener != null)
			sideListeners.add(listener);

	}

	@Override
	public void removeSideSelectionListener(SideSelectionListener listener) {
		sideListeners.remove(listener);
	}
}
