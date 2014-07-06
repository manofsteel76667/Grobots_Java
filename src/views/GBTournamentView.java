/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import sides.GBScores;
import sides.Side;
import simulation.GBWorld;
import support.StringUtilities;
import ui.GBApplication;

public class GBTournamentView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253425791054675659L;

	Color darkRed = new Color(139, 0, 0);
	Color darkGreen = new Color(0, 100, 0);
	Color purple = new Color(1, 0, 1);
	GBApplication app;
	GBWorld world;
	int margin = 6;
	int padding = 2;
	int slotMargin = 3;
	int headerHeight = 24;
	int itemHeight = 15;
	int footerHeight = 15;
	int fontSize = 10;
	ArrayList<Side> sides;

	public GBTournamentView(GBApplication _app) {
		app = _app;
		world = _app.world;
		setBackground(Color.lightGray);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		sides = new ArrayList<Side>();
	}

	public static final int kNameLeft = 25;
	public static final int kPercentRight = 200;
	public static final int kErrorRight = kPercentRight + 40;
	public static final int kSurvivalRight = kErrorRight + 50;
	public static final int kEarlyDeathRight = kSurvivalRight + 35;
	public static final int kLateDeathRight = kEarlyDeathRight + 35;
	public static final int kEarlyScoreRight = kLateDeathRight + 40;
	public static final int kFractionRight = kEarlyScoreRight + 40;
	public static final int kKillsRight = kFractionRight + 50;
	public static final int kRoundsRight = kKillsRight + 40;
	public static final int kWidth = kRoundsRight + 10;
	public static final int kMinColorRounds = 10;

	private Color rangeColor(double value, double min, double max, Color low,
			Color high, int rounds, int minrounds) {
		if (rounds < minrounds)
			return Color.gray;
		if (value < min)
			return low;
		if (value > max)
			return high;
		return Color.black;
	}

	public final String Name() {
		return "Tournament";
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw((Graphics2D) g);
	}

	void draw(Graphics2D g) {
		sides.clear();
		sides.addAll(world.Sides());
		Collections.sort(sides);
		Font f = new Font("Serif", Font.PLAIN, 10);
		g.setFont(f);
		drawHeader(g);
		for (int i = 0; i < sides.size(); i++) {
			drawItem(g, i);
		}
		drawFooter(g);
	}

	void drawHeader(Graphics2D g) {
		headerHeight = g.getFontMetrics().getHeight() * 2 + padding * 2;
		Rectangle box = new Rectangle(margin, margin,
				getBounds().width - margin * 2, headerHeight);
		g.setPaint(Color.white);
		g.fill(box);
		g.setColor(Color.black);
		g.draw(box);
		box.grow(-padding * 2, -padding * 2);
		StringUtilities.drawStringLeft(g, "Side", box.x, box.y+box.height, 10, Color.black);
		// draw various column headers
		StringUtilities.drawStringRight(g, "Score", box.x + kPercentRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Error", box.x + kErrorRight, box.y
				+ box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Survival", box.x + kSurvivalRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Death rates:", box.x
				+ kLateDeathRight, box.y + g.getFontMetrics().getHeight(), 10, Color.black);
		StringUtilities.drawStringRight(g, "Early", box.x + kEarlyDeathRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Late", box.x + kLateDeathRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Early", box.x + kEarlyScoreRight,
				box.y + g.getFontMetrics().getHeight(), 10, Color.black);
		StringUtilities.drawStringRight(g, "Score", box.x + kEarlyScoreRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Fraction", box.x + kFractionRight,
				box.y + box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Kills", box.x + kKillsRight, box.y
				+ box.height, 10, Color.black);
		StringUtilities.drawStringRight(g, "Rounds", box.x + kRoundsRight,
				box.y + box.height, 10, Color.black);
	}

	void drawItem(Graphics2D g, int index) {
		itemHeight = g.getFontMetrics().getHeight() + padding * 2;
		Rectangle box = new Rectangle(margin, margin + headerHeight + slotMargin
				+ (itemHeight + slotMargin) * index, getBounds().width - margin
				* 2, itemHeight);
		g.setPaint(Color.white);
		g.fill(box);
		g.setColor(Color.black);
		g.draw(box);
		box.grow(-padding * 2, -padding * 2);
		// DrawBox(box);
		Side side = sides.get(index);
		if (side == null)
			return;
		GBScores scores = side.TournamentScores();
		// draw ID and name
		StringUtilities.drawStringRight(g, Integer.toString(index + 1) + '.', box.x
				+ kNameLeft - 5, box.y + box.height, 10, side.Color()
				.ContrastingTextColor());
		StringUtilities.drawStringLeft(g, side.Name(), box.x + kNameLeft, box.y
				+ box.height, 10, Color.black);
		// draw various numbers
		int rounds = scores.rounds;
		int survived = scores.survived;
		int notearly = rounds - scores.earlyDeaths;
		if (rounds + survived >= 10)
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(
							scores.BiomassFractionError(), 1), box.x
							+ kErrorRight, box.y + box.height, 10,
					survived > 10 ? Color.black : Color.gray);
		if (rounds > 0) {
			double score = scores.BiomassFraction();
			StringUtilities.drawStringRight(g, StringUtilities.toPercentString(
					score, 1), box.x + kPercentRight, box.y + box.height, 10,
					(rounds + survived < kMinColorRounds * 2 || score < scores
							.BiomassFractionError() * 2) ? Color.gray
							: Color.black);
			double survival = scores.SurvivalNotSterile();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(survival, 0),
					box.x + kSurvivalRight,
					box.y + box.height,
					10,
					rangeColor(survival, 0.2, 0.4, darkRed, darkGreen, rounds,
							0));
			double early = scores.EarlyDeathRate();
			StringUtilities.drawStringRight(g,
					StringUtilities.toPercentString(early, 0), box.x
							+ kEarlyDeathRight, box.y + box.height, 10,
					rangeColor(early, 0.2, 0.4, darkGreen, darkRed, rounds, 0));
		}
		if (notearly > 0) {
			double late = scores.LateDeathRate();
			StringUtilities
					.drawStringRight(
							g,
							StringUtilities.toPercentString(late, 0),
							box.x + kLateDeathRight,
							box.y + box.height,
							10,
							rangeColor(late, 0.4, 0.6, darkGreen, darkRed,
									notearly, 0));
		}
		if (rounds > 0) {
			double early = scores.EarlyBiomassFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(early, 0),
					box.x + kEarlyScoreRight,
					box.y + box.height,
					10,
					rangeColor(early, 0.08f, 0.12f, darkRed, darkGreen, rounds
							+ notearly, kMinColorRounds * 2));
		}
		if (survived > 0) {
			double fraction = scores.SurvivalBiomassFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(fraction, 0),
					box.x + kFractionRight,
					box.y + box.height,
					10,
					rangeColor(fraction, 0.2, 0.4, Color.blue, purple,
							survived, 0));
		}
		if (rounds > 0) {
			double kills = scores.KilledFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(kills, 0),
					box.x + kKillsRight,
					box.y + box.height,
					10,
					rangeColor(kills, 0.05, 0.15, Color.blue, purple, survived,
							0));
		}
		StringUtilities.drawStringRight(g, Integer.toString(rounds), box.x
				+ kRoundsRight, box.y + box.height, 10,
				rounds < kMinColorRounds ? darkRed : Color.black);
	}

	void drawFooter(Graphics2D g) {
		Rectangle box = new Rectangle(margin, margin + headerHeight + slotMargin
				+ (itemHeight + slotMargin) * sides.size() - 1,
				getBounds().width - margin * 2, g.getFontMetrics().getHeight() + padding * 2);
		g.setPaint(Color.white);
		g.fill(box);
		g.setColor(Color.black);
		g.draw(box);
		box.grow(-padding * 2, -padding * 2);
		StringUtilities.drawStringLeft(g, "Overall:", box.x + kNameLeft, box.y
				+ box.height, 10, Color.black);
		// draw various numbers
		int rounds = world.TournamentScores().rounds;
		int notearly = world.TournamentScores().survived;// .SurvivedEarly();
		if (rounds > 0) {
			double survival = world.TournamentScores().SurvivalNotSterile();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(survival, 0),
					box.x + kSurvivalRight,
					box.y + box.height,
					10,
					rangeColor(survival, 0.25, 0.5, darkRed, darkGreen, rounds,
							0));
			double early = world.TournamentScores().EarlyDeathRate();
			StringUtilities.drawStringRight(g,
					StringUtilities.toPercentString(early, 0), box.x
							+ kEarlyDeathRight, box.y + box.height, 10,
					rangeColor(early, 0.2, 0.4, darkGreen, darkRed, rounds, 0));
		}
		if (notearly > 0) {
			double late = world.TournamentScores().LateDeathRate();
			StringUtilities.drawStringRight(g,
					StringUtilities.toPercentString(late, 0), box.x
							+ kLateDeathRight, box.y + box.height, 10,
					rangeColor(late, 0.45, 0.6, darkGreen, darkRed, rounds, 0));
		}
		if (rounds > 0) {
			double kills = world.TournamentScores().KillRate();
			StringUtilities.drawStringRight(g,
					StringUtilities.toPercentString(kills, 0), box.x
							+ kKillsRight, box.y + box.height, 10,
					rangeColor(kills, 1.2, 1.8, Color.blue, purple, rounds, 0));
		}
		StringUtilities.drawStringRight(g, Integer.toString(rounds), box.x
				+ kRoundsRight, box.y + box.height, 10,
				rounds < kMinColorRounds ? darkRed : Color.blue);
	}

}
