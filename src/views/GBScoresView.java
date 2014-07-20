package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;

import sides.GBExpenditureStatistics;
import sides.GBIncomeStatistics;
import sides.GBScores;
import sides.Side;
import simulation.GBWorld;
import support.GBColor;
import support.GBObjectClass;
import support.StringUtilities;
import ui.GBApplication;

public class GBScoresView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4827315972785490558L;
	/*******************************************************************************
	 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy Copyright (c) 2014
	 * Devon and Warren Schudy, Mike Anderson
	 *******************************************************************************/

	public static final int kGraphHeight = 120;
	public static final int kInfoBoxHeight = 184;
	public static final int kTotalsHeight = 56;
	GBApplication app;
	GBWorld world;
	Side lastSideDrawn = null;
	boolean graphAllLastDrawn;
	boolean graphAllRounds;
	int kEdgeSpace;
	Color boxfillcolor = Color.white;
	Color boxbordercolor = Color.black;
	int colwidth=50;
	int graphwidth = 150;
	
	public GBScoresView(GBApplication _app) {
		app = _app;
		world = app.world;
		lastSideDrawn = null;
		graphAllLastDrawn = true;
		graphAllRounds = true;
		kEdgeSpace = 4;
	}

	public final String Name() {
		return "Statistics";
	}
	@Override
	public void paintComponent(Graphics g) {
		colwidth = (getWidth() - kEdgeSpace * 8) / 6;
		graphwidth = (getWidth() - kEdgeSpace * 3) / 2;
		super.paintComponent(g);
		Draw((Graphics2D) g);
	}
	@Override
	public Dimension getPreferredSize(){
		int height = kGraphHeight + kInfoBoxHeight + kTotalsHeight + kEdgeSpace * 4;
		int width = 400;
		return new Dimension(height, width);
	}
	
	void drawBox(Graphics2D g, Rectangle box){
		g.setPaint(boxfillcolor);
		g.fill(box);
		g.setColor(boxbordercolor);
		g.draw(box);
	}

	void DrawIncome(Graphics2D g, GBIncomeStatistics income, int left,
			int right, int top) {
		int total = income.Total();
		if (total == 0)
			return;
		StringUtilities.drawStringPair(g, "Solar:",
				StringUtilities.toPercentString(income.autotrophy/total, 1),
				left, right, top + 10, 9, GBColor.darkGreen, false);
		StringUtilities.drawStringPair(g, "Manna:",
				StringUtilities.toPercentString(income.theotrophy/total, 1),
				left, right, top + 20, 9, GBColor.darkGreen, false);
		StringUtilities.drawStringPair(g, "Enemies:",
				StringUtilities.toPercentString(income.heterotrophy/total, 1),
				left, right, top + 30, 9, GBColor.purple, false);
		StringUtilities.drawStringPair(g, "Stolen:",
				StringUtilities.toPercentString(income.kleptotrophy/total, 1),
				left, right, top + 40, 9, new GBColor(0.4f, 0.6f, 0), false);
		StringUtilities.drawStringPair(g, "Cannibal:",
				StringUtilities.toPercentString(income.cannibalism/total, 1),
				left, right, top + 50, 9, GBColor.darkRed, false);
	}

	void DrawExpenditures(Graphics2D g, GBExpenditureStatistics spent,
			int left, int right, int top) {
		int total = spent.Total();
		if (total == 0)
			return;
		StringUtilities.drawStringPair(g, "Growth:",
				StringUtilities.toPercentString(spent.construction/total,1),
				left, right, top + 10, 9, GBColor.darkGreen, false);
		StringUtilities.drawStringPair(g, "Engine:",
				StringUtilities.toPercentString(spent.engine/total,1), left,
				right, top + 20, 9, GBColor.black, false);
		StringUtilities.drawStringPair(g, "Sensors:",
				StringUtilities.toPercentString(spent.sensors/total,1), left,
				right, top + 30, 9, GBColor.blue, false);
		StringUtilities.drawStringPair(g, "Weapons:",
				StringUtilities.toPercentString(spent.weapons/total,1), left,
				right, top + 40, 9, GBColor.purple, false);
		StringUtilities.drawStringPair(g, "Force:",
				StringUtilities.toPercentString(spent.forceField/total,1), left,
				right, top + 50, 9, GBColor.blue, false);
		StringUtilities.drawStringPair(g, "Shield:",
				StringUtilities.toPercentString(spent.shield/total,1), left,
				right, top + 60, 9, GBColor.blue, false);
		StringUtilities.drawStringPair(g, "Repairs:",
				StringUtilities.toPercentString(spent.repairs/total,1), left,
				right, top + 70, 9, GBColor.black, false);
		StringUtilities.drawStringPair(g, "Brains:",
				StringUtilities.toPercentString(spent.brain/total,1), left,
				right, top + 80, 9, GBColor.black, false);
		StringUtilities.drawStringPair(g, "Stolen:",
				StringUtilities.toPercentString(spent.stolen/total,1), left,
				right, top + 90, 9, new GBColor(0.4f, 0.6f, 0), false);
		StringUtilities.drawStringPair(g, "Overflow:",
				StringUtilities.toPercentString(spent.wasted/total,1), left,
				right, top + 100, 9, GBColor.darkRed, false);
	}

	void DrawDeaths(Graphics2D g, GBScores scores, int left, int right, int top) {
		StringUtilities.drawStringPair(g, "Kills:",
				StringUtilities.toPercentString(scores.KilledFraction(), 0),
				left, right, top + 10, 9, GBColor.purple, false);
		if (scores.survived != 0)
			StringUtilities.drawStringPair(g, "Relative:",
					StringUtilities.toPercentString(scores.KillRate(), 0),
					left, right, top + 20, 9, GBColor.black, false);
		StringUtilities.drawStringPair(g, "Kills:",
				Double.toString(scores.killed), left, right, top + 30, 9,
				GBColor.purple, false);
		StringUtilities.drawStringPair(g, "Dead:",
				Double.toString(scores.dead), left, right, top + 40, 9,
				GBColor.black, false);
		StringUtilities.drawStringPair(g, "Suicide:",
				Double.toString(scores.suicide), left, right, top + 50, 9,
				GBColor.darkRed, false);
	}

	void DrawGraph(Graphics2D g, Rectangle box, int vscale, int hscale,
			List<Integer> hist, Color color) {
		g.setColor(color);
		// draw lines
		for (int i = 0; i < hist.size() - 1; ++i) 
			g.drawLine(box.x + box.width * i / hscale, box.y + box.height
					- hist.get(i) * box.height / vscale, box.x + box.width
					* (i + 1) / hscale, box.y + box.height - hist.get(i + 1)
					* box.height / vscale);
	}

	void DrawGraphs(Graphics2D g, Rectangle box) {
		if (world.Sides() == null)
			return;
		drawBox(g, box);
		Rectangle graph = box;
		graph.grow(-1, -1);
		Side side = app.getSelectedSide();
		int scale = 1;
		int hscale = 1;
		if (side != null) {
			List<Integer> hist = side.Scores().BiomassHistory();
			for (int i = 0; i < hist.size(); ++i)
				if (hist.get(i) > scale)
					scale = hist.get(i);
			hscale = hist.size();
			// average biomass, if available
			int rounds = side.TournamentScores().rounds;
			if (rounds != 0) {
				List<Integer> avg = side.TournamentScores().BiomassHistory();
				if (avg.size() > hscale)
					hscale = avg.size();
				for (int i = 0; i < avg.size(); ++i)
					if (avg.get(i) > scale)
						scale = avg.get(i);
				DrawGraph(g, graph, scale, hscale - 1, avg,
						rounds > 20 ? GBColor.black : GBColor.darkGray);
			}
			DrawGraph(g, graph, scale, hscale - 1, hist, side.Color()
					.ContrastingTextColor());
		} else { // all sides
			boolean allRounds = graphAllRounds
					&& world.TournamentScores().rounds != 0;
			for (Side s : world.Sides()) {
				if ((allRounds ? s.TournamentScores().rounds
						: s.Scores().rounds) == 0)
					continue;
				List<Integer> hist = (allRounds ? s.TournamentScores()
						.BiomassHistory() : s.Scores().BiomassHistory());
				for (int i = 0; i < hist.size(); ++i)
					if (hist.get(i) > scale)
						scale = hist.get(i);
				if (hist.size() > hscale)
					hscale = hist.size();
			}
			for (Side s : world.Sides()) {
				if ((allRounds ? s.TournamentScores().rounds
						: s.Scores().rounds) == 0)
					continue;
				DrawGraph(g, graph, scale, hscale - 1, (allRounds ? s
						.TournamentScores().BiomassHistory() : s.Scores()
						.BiomassHistory()), s.Color().ContrastingTextColor());
			}
		}
		StringUtilities.drawStringLeft(g, Integer.toString(scale), box.x + 4,
				box.y + 13, 10, GBColor.darkGray, false);
	}

	void DrawRoundScores(Graphics2D g, GBScores scores, Rectangle box) {
		int c1 = box.x + 3;
		int c2 = (box.x * 2 + box.width) / 2 + 2;
		// basics
		StringUtilities.drawStringPair(g, "Biomass:",
				Integer.toString(scores.Biomass()), c1, c2 - 4, box.y + 25, 9,
				GBColor.darkGreen, false);
		StringUtilities.drawStringPair(g, "Population:",
				Integer.toString(scores.Population()), c1, c2 - 4, box.y + 35,
				9, GBColor.blue, false);
		StringUtilities.drawStringPair(g, "Ever:",
				Integer.toString(scores.PopulationEver()), c1 + 10, c2 - 4,
				box.y + 45, 9, GBColor.blue, false);
		Side side = app.getSelectedSide();
		if (side != null) {
			if (side.Scores().sterile != 0
					&& side.Scores().SterileTime() != side.Scores()
							.ExtinctTime())
				StringUtilities.drawStringPair(g, "Sterile:",
						Integer.toString(side.Scores().SterileTime()), c1,
						c2 - 4, box.y + 60, 9, GBColor.purple, false);
			if (side.Scores().Population() == 0)
				StringUtilities.drawStringPair(g, "Extinct:",
						Integer.toString(side.Scores().ExtinctTime()), c1,
						c2 - 4, box.y + 70, 9, GBColor.red, false);
		}
		// income
		StringUtilities.drawStringPair(g, "Income:",
				Integer.toString(scores.income.Total()), c1, c2 - 4,
				box.y + 95, 9, GBColor.black, false);
		DrawIncome(g, scores.income, c1, c2 - 4, box.y + 95);
		StringUtilities.drawStringPair(g, "Seed:",
				Integer.toString(scores.Seeded()), c1, c2 - 4, box.y + 155, 9,
				GBColor.black, false);
		if (scores.Efficiency() > 0)
			StringUtilities.drawStringPair(g, "Efficiency:",
					StringUtilities.toPercentString(scores.Efficiency(), 0),
					c1, c2 - 4, box.y + 170, 9, GBColor.black, false);
		if (scores.Doubletime(world.CurrentFrame()) != 0)
			StringUtilities.drawStringPair(g, "Double:",
					Integer.toString(scores.Doubletime(world.CurrentFrame())),
					c1, c2 - 4, box.y + 180, 9, GBColor.black, false);
		// expenditures
		StringUtilities.drawStringPair(g, "Spent:",
				Integer.toString(scores.expenditure.Total()), c2, box.x
						+ box.width - 3, box.y + 25, 9, GBColor.black, false);
		DrawExpenditures(g, scores.expenditure, c2, box.x + box.width - 3,
				box.y + 25);
		// death
		DrawDeaths(g, scores, c2, box.x + box.width - 3, box.y + 130);
	}

	void DrawTournamentScores(Graphics2D g, GBScores tscores, Rectangle box) {
		int c3 = box.x + 3;
		int c4 = (box.x + box.x + box.width) / 2 + 2;
		StringUtilities.drawStringPair(g, "Rounds:",
				Integer.toString(tscores.rounds), c3, c4 - 4, box.y + 10, 9,
				GBColor.black, false);
		if (tscores.rounds != 0) {
			StringUtilities.drawStringPair(g, "Biomass:", StringUtilities
					.toPercentString(tscores.BiomassFraction(), 0), c3, c4 - 4,
					box.y + 25, 9, GBColor.darkGreen, false);
			StringUtilities.drawStringPair(
					g,
					"Early:",
					StringUtilities.toPercentString(
							tscores.EarlyBiomassFraction(), 0), c3 + 10,
					c4 - 4, box.y + 35, 9, GBColor.darkGreen, false);
			StringUtilities.drawStringPair(g, "Survival:",
					StringUtilities.toPercentString(tscores.Survival(), 0), c3,
					c4 - 4, box.y + 50, 9, GBColor.black, false);
			StringUtilities.drawStringPair(g, "Early death:", StringUtilities
					.toPercentString(tscores.EarlyDeathRate(), 0), c3, c4 - 4,
					box.y + 60, 9, GBColor.black, false);
			StringUtilities
					.drawStringPair(
							g,
							"Late death:",
							StringUtilities.toPercentString(
									tscores.LateDeathRate(), 0), c3, c4 - 4,
							box.y + 70, 9, GBColor.black, false);
			// income
			StringUtilities.drawStringPair(g, "Avg income:",
					Integer.toString(tscores.income.Total() / tscores.rounds),
					c3, c4 - 4, box.y + 95, 9, GBColor.darkGreen, false);
			DrawIncome(g, tscores.income, c3, c4 - 4, box.y + 95);
			StringUtilities.drawStringPair(g, "Avg seed:",
					Integer.toString(tscores.Seeded()), c3, c4 - 4,
					box.y + 155, 9, GBColor.black, false);
			if (tscores.Efficiency() > 0)
				StringUtilities
						.drawStringPair(
								g,
								"Efficiency:",
								StringUtilities.toPercentString(
										tscores.Efficiency(), 0), c3, c4 - 4,
								box.y + 170, 9, GBColor.black, false);
			// expenditures
			StringUtilities.drawStringPair(
					g,
					"Avg spent:",
					Integer.toString(tscores.expenditure.Total()
							/ tscores.rounds), c4, box.x + box.width - 4,
					box.y + 25, 9, GBColor.black, false);
			DrawExpenditures(g, tscores.expenditure, c4, box.x + box.width - 4,
					box.y + 25);
			// death
			DrawDeaths(g, tscores, c4, box.x + box.width - 4, box.y + 130);
		}
	}

	void Draw(Graphics2D g) {
		Side side = app.getSelectedSide();
		// DrawBackground();
		Rectangle graphbox = new Rectangle(kEdgeSpace, kEdgeSpace, getWidth()
				- kEdgeSpace * 2, kGraphHeight + kEdgeSpace * 2);
		// round statistics
		Rectangle box = new Rectangle(kEdgeSpace, graphbox.y + graphbox.height
				+ kEdgeSpace, (getWidth() - kEdgeSpace) / 2, graphbox.y
				+ graphbox.height + kEdgeSpace + kInfoBoxHeight);
		drawBox(g, box);
		StringUtilities.drawStringLeft(g, side != null ? side.Name()
				: "Overall statistics", box.x + 3, box.y + 13, 12,
				GBColor.black, false);
		// draw stats...
		if (side != null)
			DrawRoundScores(g, side.Scores(), box);
		else
			DrawRoundScores(g, world.RoundScores(), box);
		// tournament stats:
		box.x = box.x + box.width + kEdgeSpace;
		box.width = getWidth() - kEdgeSpace - box.x;
		drawBox(g, box);
		if (side != null)
			DrawTournamentScores(g, side.TournamentScores(), box);
		else
			DrawTournamentScores(g, world.TournamentScores(), box);
		// simulation total values
		box.y = box.y + box.height + kEdgeSpace;
		box.height = kTotalsHeight;
		drawBox(g, box);
		StringUtilities.drawStringLeft(g, "Total values:", box.x + 3,
				box.y + 13, 10, Color.black, false);
		StringUtilities.drawStringPair(g, "Manna:",
				Integer.toString(world.MannaValue()), box.x + 3, box.x
						+ box.width - 3, box.y + 23, 10, GBColor.darkGreen,
				false);
		StringUtilities.drawStringPair(g, "Corpses:",
				Integer.toString(world.CorpseValue()), box.x + 3, box.x
						+ box.width - 3, box.y + 33, 10, Color.red, false);
		StringUtilities.drawStringPair(g, "Robots:",
				Integer.toString(world.RobotValue()), box.x + 3, box.x
						+ box.width - 3, box.y + 43, 10, Color.blue, false);
		// object counts
		box.x = kEdgeSpace;
		box.width = (getWidth() - kEdgeSpace) / 2 - box.x;
		drawBox(g, box);
		StringUtilities.drawStringPair(g, "Robots:",
				Integer.toString(world.CountObjects(GBObjectClass.ocRobot)),
				box.x + 3, box.x + box.width - 3, box.y + 13, 10, Color.black,
				false);
		StringUtilities.drawStringPair(g, "Foods:",
				Integer.toString(world.CountObjects(GBObjectClass.ocFood)),
				box.x + 3, box.x + box.width - 3, box.y + 23, 10, Color.black,
				false);
		StringUtilities.drawStringPair(
				g,
				"Shots:",
				Integer.toString(world.CountObjects(GBObjectClass.ocShot)
						+ world.CountObjects(GBObjectClass.ocArea)), box.x + 3,
				box.x + box.width - 3, box.y + 33, 10, Color.black, false);
		StringUtilities.drawStringPair(g, "Sensors:", Integer.toString(world
				.CountObjects(GBObjectClass.ocSensorShot)), box.x + 3, box.x
				+ box.width - 3, box.y + 43, 10, Color.black, false);
		StringUtilities.drawStringPair(g, "Decorations:", Integer
				.toString(world.CountObjects(GBObjectClass.ocDecoration)),
				box.x + 3, box.x + box.width - 3, box.y + 53, 10, Color.black,
				false);
		// drawing graph last to reduce flicker
		DrawGraphs(g, graphbox);
		// record
		lastSideDrawn = app.getSelectedSide();
		graphAllLastDrawn = graphAllRounds;
	}
}
