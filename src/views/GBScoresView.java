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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import sides.GBScores;
import sides.Side;
import simulation.GBGame;
import support.GBColor;
import support.StringUtilities;
import ui.SideSelectionListener;

public class GBScoresView extends JPanel implements SideSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4827315972785490558L;

	GBGame game;
	Side selectedSide;

	JLabel roundLabel, tournLabel;
	JPanel roundGraph, tournGraph;
	JTextPane roundIncome, roundKills, roundExpenditure, roundCounts;
	JTextPane tournIncome, tournKills, tournExpenditure, tournCounts;

	Style basicStyle;
	SimpleAttributeSet basicAttr;
	SimpleAttributeSet bold;
	SimpleAttributeSet blue;
	SimpleAttributeSet green;
	SimpleAttributeSet red;
	SimpleAttributeSet gray;
	SimpleAttributeSet brown;

	public GBScoresView(GBGame _game) {
		game = _game;
		Border border = BorderFactory.createLineBorder(getForeground(), 1);
		roundLabel = new JLabel();
		tournLabel = new JLabel();
		roundGraph = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3268061604878534231L;
			Point max = new Point(0, 0);
			int rounds = 0;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (game.getTournamentScores().rounds > rounds)
					max = new Point(0, 0);
				drawGraph((Graphics2D) g, getVisibleRect(), false, max);
			};
		};
		roundGraph.setBorder(border);
		roundGraph.setPreferredSize(new Dimension(200, 150));
		roundGraph.setBackground(Color.white);
		roundGraph.setIgnoreRepaint(true);
		tournGraph = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3274144226270291895L;
			Point max = new Point(0, 0);

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawGraph((Graphics2D) g, getVisibleRect(), true, max);
			};
		};
		tournGraph.setBorder(border);
		tournGraph.setPreferredSize(new Dimension(200, 150));
		tournGraph.setBackground(Color.white);
		tournGraph.setIgnoreRepaint(true);
		roundIncome = new JTextPane();
		roundIncome.setBorder(border);
		roundKills = new JTextPane();
		roundKills.setBorder(border);
		roundExpenditure = new JTextPane();
		roundExpenditure.setBorder(border);
		roundCounts = new JTextPane();
		roundCounts.setBorder(border);
		tournIncome = new JTextPane();
		tournIncome.setBorder(border);
		tournKills = new JTextPane();
		tournKills.setBorder(border);
		tournExpenditure = new JTextPane();
		tournExpenditure.setBorder(border);
		tournCounts = new JTextPane();
		tournCounts.setBorder(border);
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
								.addComponent(roundLabel)
								.addComponent(roundGraph)
								.addGroup(
										layout.createSequentialGroup()
												.addGroup(
														layout.createParallelGroup()
																.addComponent(
																		roundIncome)
																.addComponent(
																		roundKills))
												.addComponent(roundExpenditure)
												.addComponent(roundCounts)))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(tournLabel)
								.addComponent(tournGraph)
								.addGroup(
										layout.createSequentialGroup()
												.addGroup(
														layout.createParallelGroup()
																.addComponent(
																		tournIncome)
																.addComponent(
																		tournKills))
												.addComponent(tournExpenditure)
												.addComponent(tournCounts))));
		layout.setVerticalGroup(layout
				.createParallelGroup()
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(roundLabel)
								.addComponent(roundGraph)
								.addGroup(
										layout.createParallelGroup()
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		roundIncome)
																.addComponent(
																		roundKills))
												.addComponent(roundExpenditure)
												.addComponent(roundCounts)))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(tournLabel)
								.addComponent(tournGraph)
								.addGroup(
										layout.createParallelGroup()
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		tournIncome)
																.addComponent(
																		tournKills))
												.addComponent(tournExpenditure)
												.addComponent(tournCounts))));
		setAttributes();
		setIgnoreRepaint(true);
	}

	void setAttributes() {
		basicAttr = new SimpleAttributeSet();
		StyleConstants.setFontSize(basicAttr, 9);
		bold = new SimpleAttributeSet(basicAttr);
		StyleConstants.setBold(bold, true);
		red = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(red, Color.red);
		green = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(green, GBColor.darkGreen);
		blue = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(blue, Color.blue);
		red = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(red, Color.red);
		gray = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(gray, Color.gray);
		brown = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(brown, new Color(150, 113, 23));
	}

	public void updateScores() {
		roundLabel.setText(String.format("%s This Round:",
				selectedSide == null ? "" : selectedSide.getName()));
		tournLabel.setText(String.format("%s Average over %d rounds:",
				selectedSide == null ? "" : selectedSide.getName(),
				game.getTournamentScores().rounds));
		GBScores scores = selectedSide == null ? game.getRoundScores()
				: selectedSide.getScores();
		drawIncome(roundIncome, scores);
		drawKills(roundKills, scores);
		drawExpenditure(roundExpenditure, scores);
		drawCounts(roundCounts, scores);
		scores = selectedSide == null ? game.getTournamentScores() : selectedSide
				.getTournamentScores();
		drawIncome(tournIncome, scores);
		drawKills(tournKills, scores);
		drawExpenditure(tournExpenditure, scores);
		drawCounts(tournCounts, scores);
	}

	void drawIncome(JTextPane pane, GBScores score) {
		pane.setText("");
		Document doc = pane.getDocument();
		try {
			double total = score.income.total() != 0 ? score.income.total() / 100.0
					: 1;
			doc.insertString(doc.getLength(), "Income\t", bold);
			doc.insertString(doc.getLength(),
					String.format("%d\n", score.income.total()), bold);
			doc.insertString(doc.getLength(), "Solar\t", green);
			doc.insertString(doc.getLength(),
					String.format("%.1f%%\n", score.income.autotrophy / total),
					green);
			doc.insertString(doc.getLength(), "Manna\t", green);
			doc.insertString(doc.getLength(),
					String.format("%.1f%%\n", score.income.theotrophy / total),
					green);
			doc.insertString(doc.getLength(), "Enemies\t", basicAttr);
			doc.insertString(doc.getLength(), String.format("%.1f%%\n",
					score.income.heterotrophy / total), basicAttr);
			doc.insertString(doc.getLength(), "Stolen\t", green);
			doc.insertString(doc.getLength(), String.format("%.1f%%\n",
					score.income.kleptotrophy / total), green);
			doc.insertString(doc.getLength(), "Cannibal\t", red);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.income.cannibalism / total),
					red);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void drawKills(JTextPane pane, GBScores score) {
		pane.setText("");
		Document doc = pane.getDocument();
		try {
			doc.insertString(doc.getLength(), "Kill Rate\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.0f%%\n", score.getKilledFraction()),
					basicAttr);
			if (score.survived != 0) {
				doc.insertString(doc.getLength(), "Relative\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.0f%%\n", score.getKillRate()), basicAttr);
			} else
				doc.insertString(doc.getLength(), "\n", basicAttr);
			doc.insertString(doc.getLength(), "Kills\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.0f\n", score.killed), basicAttr);
			doc.insertString(doc.getLength(), "Losses\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.0f\n", score.dead), basicAttr);
			doc.insertString(doc.getLength(), "Suicides\t", red);
			doc.insertString(doc.getLength(),
					String.format("%.0f\n", score.suicide), red);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void drawExpenditure(JTextPane pane, GBScores score) {
		pane.setText("");
		Document doc = pane.getDocument();
		try {
			double total = score.expenditure.total() != 0 ? score.income
					.total() / 100.0 : 1;
			doc.insertString(doc.getLength(), "Spent\t", bold);
			doc.insertString(doc.getLength(),
					String.format("%d\n", score.expenditure.total()), bold);
			doc.insertString(doc.getLength(), "Growth\t", green);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.construction
							/ total), green);
			doc.insertString(doc.getLength(), "Engine\t", basicAttr);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.engine / total),
					basicAttr);
			doc.insertString(doc.getLength(), "Sensors\t", blue);
			doc.insertString(doc.getLength(), String.format("%.1f%%\n",
					score.expenditure.sensors / total), blue);
			doc.insertString(doc.getLength(), "Weapons\t", basicAttr);
			doc.insertString(doc.getLength(), String.format("%.1f%%\n",
					score.expenditure.weapons / total), basicAttr);
			doc.insertString(doc.getLength(), "Force\t", blue);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.forceField
							/ total), blue);
			doc.insertString(doc.getLength(), "Shield\t", basicAttr);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.shield / total),
					basicAttr);
			doc.insertString(doc.getLength(), "Repairs\t", basicAttr);
			doc.insertString(doc.getLength(), String.format("%.1f%%\n",
					score.expenditure.repairs / total), basicAttr);
			doc.insertString(doc.getLength(), "Brains\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.brain / total),
					basicAttr);
			doc.insertString(doc.getLength(), "Stolen\t", brown);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.stolen / total),
					brown);
			doc.insertString(doc.getLength(), "Overflow\t", red);
			doc.insertString(
					doc.getLength(),
					String.format("%.1f%%\n", score.expenditure.wasted / total),
					red);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void drawCounts(JTextPane pane, GBScores score) {
		pane.setText("");
		Document doc = pane.getDocument();
		try {
			if (selectedSide == null) {
				doc.insertString(doc.getLength(), "Biomass\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.0f\n", score.biomass), basicAttr);
				doc.insertString(doc.getLength(), "Population\t", blue);
				doc.insertString(doc.getLength(),
						String.format("%d\n", score.population), blue);
				doc.insertString(doc.getLength(), "Ever\t", blue);
				doc.insertString(doc.getLength(),
						String.format("%d\n", score.getPopulationEver()), blue);
				doc.insertString(doc.getLength(), "Manna\t", green);
				doc.insertString(doc.getLength(),
						String.format("%d\n", game.getMannaValue()), green);
				doc.insertString(doc.getLength(), "Corpses\t", red);
				doc.insertString(doc.getLength(),
						String.format("%d\n", game.getCorpseValue()), red);
			} else {
				doc.insertString(doc.getLength(), "Biomass\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.1f%%\n", score.biomassFraction * 100),
						basicAttr);
				doc.insertString(doc.getLength(), "Early\t", blue);
				doc.insertString(doc.getLength(),
						String.format("%.0f\n", score.earlyBiomass), blue);
				doc.insertString(doc.getLength(), "Survival\t", blue);
				doc.insertString(doc.getLength(),
						String.format("%.1f%%\n", score.getSurvival() * 100), blue);
				doc.insertString(doc.getLength(), "Early Death\t", green);
				doc.insertString(
						doc.getLength(),
						String.format("%.1f%%\n", score.EarlyDeathRate() * 100),
						green);
				doc.insertString(doc.getLength(), "Late Death\t", red);
				doc.insertString(doc.getLength(),
						String.format("%.1f%%\n", score.getLateDeathRate() * 100),
						red);
			}
			doc.insertString(doc.getLength(), "Seeded\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.0f\n", score.seeded), basicAttr);
			doc.insertString(doc.getLength(), "Efficiency\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%.1f%%\n", score.getEfficiency() * 100),
					basicAttr);
			int doubletime = score.getDoubletime(game.currentFrame());
			if (selectedSide != null && doubletime != 0 && doubletime < 100000) {
				doc.insertString(doc.getLength(), "DoubleTime\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.d\n", doubletime), basicAttr);
			} else
				doc.insertString(doc.getLength(), "\n", basicAttr);
			if (score.population != 0) {
				doc.insertString(doc.getLength(), "Economy\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.1f%%\n", score.getEconFraction()),
						basicAttr);
				doc.insertString(doc.getLength(), "Combat\t", basicAttr);
				doc.insertString(doc.getLength(),
						String.format("%.1f%%\n", score.getCombatFraction()),
						basicAttr);
			} else
				doc.insertString(doc.getLength(), "\n\n", basicAttr);
			doc.insertString(doc.getLength(), "Territory\t", basicAttr);
			doc.insertString(doc.getLength(),
					String.format("%d\n", score.territory), basicAttr);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		roundGraph.repaint();
		tournGraph.repaint();
	}

	void drawGraph(Graphics2D g, Rectangle rect, boolean allRounds, Point max) {
		if (game.getSidesCount() == 0)
			return;
		rect.grow(-10, -5);
		Side side = selectedSide;
		// Check for changes to the max sizes
		if (allRounds)
			max.y = 0;
		for (Side s : game.sides) {
			if ((allRounds ? s.getTournamentScores().rounds : s.getScores().rounds) == 0)
				continue;
			List<Integer> hist = allRounds ? s.getTournamentScores()
					.getBiomassHistory() : s.getScores().getBiomassHistory();
			if (hist.size() == 0)
				continue;
			if (hist.size() - 1 > max.x) {
				max.x = hist.size() - 1;
			}
			if (!allRounds) {
				max.y = Math.max(hist.get(hist.size() - 1), max.y);
			} else {
				for (Integer i : hist)
					max.y = Math.max(max.y, i);
			}
		}
		if (max.x <= 0) {
			// max.x = 0;
			max.y = 0;
			return;
		}
		// draw gridlines
		// Vertical
		for (int t = 50; t < max.x; t += 50) {
			int x = (int) (rect.width * (double) t / max.x);
			g.setColor(Color.lightGray);
			g.drawLine(x, rect.y, x, rect.height);
		}
		// Horizontal
		for (int quantum = 1000; quantum < max.y; quantum *= 10) {
			if (quantum < max.y / 40)
				continue;
			for (int en = quantum; en < max.y; en += quantum) {
				int y = rect.height - (int) (rect.height * (double) en / max.y);
				g.setColor(Color.lightGray);
				g.drawLine(rect.x, y, rect.x + rect.width, y);
			}
		}
		// draw curves
		for (Side s : game.sides) {
			if ((allRounds ? s.getTournamentScores().rounds : s.getScores().rounds) == 0)
				continue;
			List<Integer> hist = (allRounds ? s.getTournamentScores()
					.getBiomassHistory() : s.getScores().getBiomassHistory());
			// draw lines

			for (int j = 0; j < hist.size() - 1; ++j) {
				g.setColor(GBColor.ContrastingTextColor(s.getColor()));
				g.setStroke(new BasicStroke(s == side ? 2 : 1));
				g.drawLine(
						(int) (rect.width * (double) j / max.x),
						rect.height
								- (int) ((double) rect.height
										* (double) hist.get(j) / max.y),
						(int) (rect.width * (double) (j + 1) / max.x),
						rect.height
								- (int) ((double) rect.height
										* (double) hist.get(j + 1) / max.y));
			}
		}
		StringUtilities.drawStringLeft(g, Integer.toString(max.y), 3, 10, 9,
				Color.gray);
		StringUtilities.drawStringRight(g, Integer.toString(max.x * 100),
				(int) rect.getWidth() - 3, (int) rect.getHeight() - 3, 9,
				Color.gray);
	}

	@Override
	public void setSelectedSide(Side side) {
		selectedSide = side;
		updateScores();
		if (isVisible())
			repaint();
	}
}
