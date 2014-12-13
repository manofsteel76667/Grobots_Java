package views;

/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy Copyright (c) 2014
 * Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import sides.GBExpenditureStatistics;
import sides.GBIncomeStatistics;
import sides.GBScores;
import sides.Side;
import simulation.GBGame;
import support.GBColor;
import support.StringUtilities;
import ui.SideSelectionListener;

class StatisticsLine implements Comparable<StatisticsLine> {
	public String name;
	public String value;
	public Color color;
	public int order;
	public int group;
	public int fontHeight = 10;
	public boolean bold = false;

	public StatisticsLine(String statName, String statValue, Color statColor,
			int _group, int _order) {
		name = statName;
		value = statValue;
		color = statColor;
		group = _group;
		order = _order;
	}

	public StatisticsLine(String statName, String statValue, Color statColor,
			int _group, int _order, int _fontHeight, boolean _bold) {
		name = statName;
		value = statValue;
		color = statColor;
		group = _group;
		order = _order;
		fontHeight = _fontHeight;
		bold = _bold;
	}

	@Override
	public int compareTo(StatisticsLine arg0) {
		if (this.group != arg0.group)
			return this.order - arg0.order;
		else
			return this.group - arg0.group;
	}
}

public class GBScoresView extends JPanel implements SideSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4827315972785490558L;
	static final int kGraphTop = 15;
	int kGraphWidth = 1;
	static final int kGraphHeight = 120;
	static final int kInfoBoxHeight = 85;
	static final int kTotalsHeight = 66;
	static final int kTableHeight = 113;
	// GBApplication app;
	GBGame game;
	Side selectedSide;
	Side lastSideDrawn = null;
	int kEdgeSpace;
	Color boxfillcolor = Color.white;
	Color boxbordercolor = Color.black;
	int colwidth = 50;
	int graphwidth = 150;
	int padding = 2;
	List<StatisticsLine> roundStatsList;
	List<StatisticsLine> tournStatsList;

	public GBScoresView(GBGame _game) {
		game = _game;
		lastSideDrawn = null;
		kEdgeSpace = 4;
	}

	public final String Name() {
		return "Statistics";
	}

	@Override
	public void paintComponent(Graphics g) {
		colwidth = (getWidth() - kEdgeSpace * 5) / 6;
		graphwidth = (getWidth() - kEdgeSpace) / 2;
		kGraphWidth = colwidth * 3 + kEdgeSpace * 2;
		;
		super.paintComponent(g);
		Draw((Graphics2D) g);
	}

	@Override
	public Dimension getPreferredSize() {
		int height = kGraphTop + kGraphHeight + kInfoBoxHeight + kTotalsHeight
				+ kEdgeSpace * 3;
		return new Dimension(200, height);
	}

	void drawBox(Graphics2D g, Rectangle box) {
		g.setPaint(boxfillcolor);
		g.fill(box);
		g.setColor(boxbordercolor);
		g.draw(box);
	}

	ArrayList<StatisticsLine> buildStatsList(GBScores in, boolean tournament) {
		ArrayList<StatisticsLine> ret = new ArrayList<StatisticsLine>();
		// Income
		GBIncomeStatistics income = in.income;
		int total = income.Total();
		ret.add(new StatisticsLine("Income", Integer.toString(total),
				Color.black, 0, 0, 10, true));
		if (total != 0) {
			ret.add(new StatisticsLine("Solar", StringUtilities
					.toPercentString(income.autotrophy / total, 1),
					GBColor.darkGreen, 0, 1));
			ret.add(new StatisticsLine("Manna", StringUtilities
					.toPercentString(income.theotrophy / total, 1),
					GBColor.darkGreen, 0, 2));
			ret.add(new StatisticsLine("Enemies", StringUtilities
					.toPercentString(income.heterotrophy / total, 1),
					GBColor.purple, 0, 3));
			ret.add(new StatisticsLine("Stolen", StringUtilities
					.toPercentString(income.kleptotrophy / total, 1),
					GBColor.gold, 0, 4));
			ret.add(new StatisticsLine("Cannibal", StringUtilities
					.toPercentString(income.cannibalism / total, 1),
					GBColor.darkRed, 0, 5));
		}
		// Deaths
		ret.add(new StatisticsLine("Kill Rate", StringUtilities
				.toPercentString(in.KilledFraction(), 0), Color.black, 1, 0));
		if (in.survived != 0)
			ret.add(new StatisticsLine("Relative:", StringUtilities
					.toPercentString(in.KillRate(), 0), Color.black, 1, 1));
		ret.add(new StatisticsLine("Kills", Integer.toString((int) in.killed),
				GBColor.purple, 1, 2));
		ret.add(new StatisticsLine("Losses",
				Integer.toString((int) in.dead, 0), Color.black, 1, 3));
		ret.add(new StatisticsLine("Suicides", Integer.toString(
				(int) in.suicide, 0), GBColor.darkRed, 1, 4));
		// Expenditures
		GBExpenditureStatistics spent = in.expenditure;
		total = spent.Total();
		ret.add(new StatisticsLine("Spent", Integer.toString(total),
				Color.black, 2, 0, 10, true));
		if (total != 0) {
			ret.add(new StatisticsLine("Growth", StringUtilities
					.toPercentString(spent.construction / total, 1),
					GBColor.darkGreen, 2, 1));
			ret.add(new StatisticsLine("Engine", StringUtilities
					.toPercentString(spent.engine / total, 1), Color.black, 2,
					2));
			ret.add(new StatisticsLine("Sensors", StringUtilities
					.toPercentString(spent.sensors / total, 1), Color.blue, 2,
					3));
			ret.add(new StatisticsLine("Weapons", StringUtilities
					.toPercentString(spent.weapons / total, 1), GBColor.purple,
					2, 4));
			ret.add(new StatisticsLine("Force", StringUtilities
					.toPercentString(spent.forceField / total, 1), Color.blue,
					2, 5));
			ret.add(new StatisticsLine("Shield", StringUtilities
					.toPercentString(spent.shield / total, 1), Color.black, 2,
					6));
			ret.add(new StatisticsLine("Repairs", StringUtilities
					.toPercentString(spent.repairs / total, 1), Color.black, 2,
					7));
			ret.add(new StatisticsLine("Brains", StringUtilities
					.toPercentString(spent.brain / total, 1), Color.black, 2, 8));
			ret.add(new StatisticsLine("Stolen", StringUtilities
					.toPercentString(spent.stolen / total, 1), GBColor.gold, 2,
					9));
			ret.add(new StatisticsLine("Overflow", StringUtilities
					.toPercentString(spent.wasted / total, 1), GBColor.darkRed,
					2, 10));
		}
		// Biomass, population, misc
		if (tournament) {
			// Show tournament stats
			ret.add(new StatisticsLine("Biomass",
					Integer.toString(in.Biomass()), Color.black, 3, 0, 10, true));
			ret.add(new StatisticsLine("Early", Integer
					.toString((int) in.earlyBiomass), Color.black, 3, 1));
			ret.add(new StatisticsLine("Survival", StringUtilities
					.toPercentString(in.Survival(), 1), Color.black, 3, 2));
			ret.add(new StatisticsLine("Early Death", StringUtilities
					.toPercentString(in.EarlyDeathRate(), 1), Color.black, 3, 3));
			ret.add(new StatisticsLine("Late Death", StringUtilities
					.toPercentString(in.LateDeathRate(), 1), Color.black, 3, 4));
			ret.add(new StatisticsLine("Seeded", Integer.toString(in.Seeded()),
					Color.black, 3, 5));
			ret.add(new StatisticsLine("Efficiency", StringUtilities
					.toPercentString(in.Efficiency(), 1), Color.black, 3, 6));
		} else {
			// Show this round's stats
			ret.add(new StatisticsLine("Biomass", StringUtilities
					.toPercentString(in.BiomassFraction(), 0), Color.black, 3,
					0));
			ret.add(new StatisticsLine("Population", Integer
					.toString(in.population), Color.blue, 3, 1));
			ret.add(new StatisticsLine("Ever", Integer.toString(in
					.PopulationEver()), Color.blue, 3, 2));
			ret.add(new StatisticsLine("Manna", Integer.toString(game
					.MannaValue()), GBColor.darkGreen, 3, 3));
			ret.add(new StatisticsLine("Corpses", Integer.toString(game
					.CorpseValue()), Color.red, 3, 4));
			ret.add(new StatisticsLine("Seeded", Integer
					.toString((int) in.seeded), Color.black, 3, 5));
			ret.add(new StatisticsLine("Efficiency", StringUtilities
					.toPercentString(in.Efficiency(), 0), Color.black, 3, 6));
		}
		int doubletime = in.Doubletime(game.CurrentFrame());
		if (tournament && doubletime != 0 && doubletime < 100000)
			ret.add(new StatisticsLine("DoubleTime", Integer
					.toString(doubletime), Color.black, 3, 7));// Blank line
		if (in.population != 0) {
			ret.add(new StatisticsLine("Economy", StringUtilities
					.toPercentString(in.EconFraction(), 1), Color.black, 3, 8));
			ret.add(new StatisticsLine("Combat", StringUtilities
					.toPercentString(in.CombatFraction(), 1), Color.black, 3, 9));
		}
		ret.add(new StatisticsLine("Territory", Integer.toString(in.territory),
				Color.black, 3, 10));
		return ret;
	}

	void Draw(Graphics2D g) {
		roundStatsList = buildStatsList(
				selectedSide == null ? game.RoundScores()
						: selectedSide.Scores(), false);
		tournStatsList = buildStatsList(
				selectedSide == null ? game.TournamentScores()
						: selectedSide.TournamentScores(), true);
		List<Rectangle> boxes = new ArrayList<Rectangle>();
		boxes.add(new Rectangle(0, kGraphTop + kGraphHeight + kEdgeSpace,
				colwidth, kInfoBoxHeight)); // Income
		boxes.add(new Rectangle(0, kGraphTop + kGraphHeight + kEdgeSpace * 2
				+ kInfoBoxHeight, colwidth, kTotalsHeight)); // Deaths
		boxes.add(new Rectangle(kEdgeSpace + colwidth, kEdgeSpace
				+ kGraphHeight + kGraphTop, colwidth, kInfoBoxHeight
				+ kTotalsHeight + kEdgeSpace)); // Expenditures
		boxes.add(new Rectangle(kEdgeSpace * 2 + colwidth * 2, kEdgeSpace
				+ kGraphHeight + kGraphTop, colwidth - 1, kInfoBoxHeight
				+ kTotalsHeight + kEdgeSpace)); // Biomass
		drawGroups(g, boxes, roundStatsList);
		// Offset the group statistics boxes to the right to be used again for
		// tournament stats
		for (Rectangle box : boxes)
			box.x += kEdgeSpace * 3 + colwidth * 3;
		drawGroups(g, boxes, tournStatsList);
		// drawing graphs last to reduce flicker
		Rectangle graphbox = new Rectangle(0, kGraphTop, kGraphWidth - 1,
				kGraphHeight);
		DrawGraph(g, graphbox, false);
		graphbox.x += graphbox.width + kEdgeSpace + 1;
		if (game.TournamentScores().rounds != 0)
			DrawGraph(g, graphbox, true);
		// record
		lastSideDrawn = selectedSide;
	}

	void drawGroups(Graphics2D g, List<Rectangle> boxes,
			List<StatisticsLine> stats) {
		for (int i = 0; i < boxes.size(); i++) {
			Rectangle box = boxes.get(i);
			drawBox(g, box);
			for (StatisticsLine l : stats)
				if (l.group == i)
					StringUtilities.drawStringPair(g, l.name, l.value, box.x
							+ padding, box.x + box.width - padding, box.y
							+ padding + (l.order + 1)
							* (l.fontHeight + padding), l.fontHeight, l.color,
							l.bold);
		}
	}

	void DrawGraph(Graphics2D g, Rectangle box, int vscale, int hscale,
			List<Integer> hist, Color color, int weight) {
		int n = hist.size() - 1;
		// draw lines
		for (int i = 0; i < n; ++i) {
			g.setColor(color);
			g.setStroke(new BasicStroke(weight));
			g.drawLine(box.x + box.width * i / hscale, box.y + box.height
					- hist.get(i) * box.height / vscale, box.x + box.width
					* (i + 1) / hscale, box.y + box.height - hist.get(i + 1)
					* box.height / vscale);
		}
	}

	void DrawGraph(Graphics2D g, Rectangle box, boolean allRounds) {
		if (game.CountSides() == 0)
			return;
		drawBox(g, box);
		Rectangle graph = new Rectangle(box.x + 1, box.y + 1, box.width - 2,
				box.height - 2);
		Side side = selectedSide;
		// find scale
		int scale = 1;
		int hscale = 1;
		List<Side> sides = game.sides;
		for (int i = 0; i < sides.size(); ++i) {
			Side s = sides.get(i);
			if ((allRounds ? s.TournamentScores().rounds : s.Scores().rounds) == 0)
				continue;
			List<Integer> hist = allRounds ? s.TournamentScores()
					.BiomassHistory() : s.Scores().BiomassHistory();
			for (Integer value : hist)
				scale = (int) Math.max(value, scale);
			hscale = Math.max((int) hist.size() - 1, hscale);
		}
		if (hscale < 1)
			return;
		// draw gridlines
		for (int t = 45; t < hscale; t += 45) {
			int x = graph.x + t * graph.width / hscale;
			g.setColor(Color.lightGray);
			g.drawLine(x, graph.y + graph.height, x, graph.y);
		}
		for (int quantum = 1000; quantum < scale; quantum *= 10) {
			if (quantum < scale / 40)
				continue;
			for (int en = quantum; en < scale; en += quantum) {
				int y = graph.y + graph.height - en * graph.height / scale;
				g.setColor(GBColor.getGreyColor(0.98f - 0.4f * quantum / scale));
				g.drawLine(graph.x, y, graph.x + graph.width, y);
			}
		}
		// draw curves
		for (int i = 0; i < sides.size(); ++i) {
			Side s = sides.get(i);
			if ((allRounds ? s.TournamentScores().rounds : s.Scores().rounds) == 0)
				continue;
			List<Integer> hist = (allRounds ? s.TournamentScores()
					.BiomassHistory() : s.Scores().BiomassHistory());
			DrawGraph(g, graph, scale, hscale, hist,
					GBColor.ContrastingTextColor(s.Color()), s == side ? 2 : 1);
		}
		StringUtilities.drawStringLeft(g, Integer.toString(scale), box.x + 3,
				box.y + 10, 9, Color.gray);
		StringUtilities.drawStringRight(g, Integer.toString(hscale * 100),
				box.x + box.width - 3, box.y + box.height - 3, 9, Color.gray);
		g.setColor(Color.black);
		GBScores scores = game.TournamentScores();
		String title = allRounds ? "Average over "
				+ Integer.toString(scores.rounds)
				+ (scores.rounds == 1 ? " round" : " rounds")
				: side != null ? side.Name() + " this round" : "This round";
		StringUtilities.drawStringLeft(g, title + ":", box.x + 3,
				kGraphTop - 4, 10, Color.black);
		g.draw(box); // clean up spills
	}

	@Override
	public void setSelectedSide(Side side) {
		selectedSide = side;
		repaint();
	}
}
