/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sides.GBScores;
import sides.RobotType;
import sides.Side;
import sound.SoundManager;
import support.FinePoint;
import support.GBColor;
import support.StringUtilities;
import ui.ObjectSelectionListener;
import ui.ObjectSelector;
import ui.SideSelectionListener;
import ui.TypeSelectionListener;
import exception.GBSimulationError;

public class GBGame implements ScoreKeeper, ObjectSelector {
	public List<Side> sides;
	GBWorld world;

	// Post-simulation actions
	List<Side> sidesToRemove;
	Side sideToReplace;
	Side replacementSide;
	int previousSidesAlive; // num of non-extinct sides last frame

	// stats
	int mannas, corpses;
	int mannaValue, corpseValue, robotValue;
	/**
	 * Sum of all side scores for this round
	 */
	GBScores roundScores;
	/**
	 * Sum of all side scores over all rounds
	 */
	GBScores tournamentScores;

	// Listeners
	List<ObjectSelectionListener> objectListeners;
	List<TypeSelectionListener> typeListeners;
	List<SideSelectionListener> sideListeners;

	// operation and tournament
	public boolean running;
	public int timeLimit;
	public boolean stopOnElimination;
	public boolean tournament;
	public int tournamentLength;

	// simulation parameters
	public int seedLimit;
	public boolean autoReseed;
	public long totalFrames;

	public static final int kDefaultTimeLimit = 18000;

	public GBGame() {
		sides = new ArrayList<Side>();
		sidesToRemove = new ArrayList<Side>();
		objectListeners = new ArrayList<ObjectSelectionListener>();
		typeListeners = new ArrayList<TypeSelectionListener>();
		sideListeners = new ArrayList<SideSelectionListener>();
		roundScores = new GBScores();
		tournamentScores = new GBScores();
		world = new GBWorld(this);
		stopOnElimination = true;
		timeLimit = kDefaultTimeLimit;
		tournamentLength = -1;
		seedLimit = 10;
	}

	public void startRound() {
		reset();
		roundScores.reset();
		pickSeededSides();
		world.addSeeds();
		world.currentFrame = 0;
		collectStatistics();
	}

	public void advanceFrame() {
		previousSidesAlive = world.getSidesAlive();
		world.simulateOneFrame();
		collectStatistics();
		if (previousSidesAlive > world.getSidesAlive()) {
			SoundManager.playSound(SoundManager.SoundType.stExtinction);
		}
		if (totalFrames < Long.MAX_VALUE)// yeah, right...
			totalFrames++;
		else
			totalFrames = 0;
		if (roundOver())
			EndRound();
		if (sidesToRemove.size() > 0) {
			for (Side side : sidesToRemove) {
				if (sides.contains(side))
					sides.remove(side);
				if (world.sides.contains(side))
					world.removeSide(side);
			}
			sidesToRemove.clear();
			checkSides();
		}
		if (sideToReplace != null && replacementSide != null) {
			handleSideReplacement(sideToReplace, replacementSide);
			sideToReplace = null;
			replacementSide = null;
		}
	}

	public void EndRound() {
		SoundManager.playSound(SoundManager.SoundType.stEndRound);
		// TODO extend biomassHistory to 18k when ending? (to avoid misleading
		// graph)
		reportRound();
		// Free references to the selected object so it can be GC'd
		for (ObjectSelectionListener l : objectListeners)
			l.setSelectedObject(null);
		// Don't set selected side or type to null since someone may want to add
		// a robot or something
		if (tournament) {
			if (tournamentLength > 0)
				--tournamentLength;
			if (tournamentLength != 0) {
				startRound();
			} else {
				tournament = false;
				running = false;
			}
		} else
			running = false;
	}

	void pickSeededSides() {
		// Randomly pick up to <seedLimit> sides from the sides list
		int seedsLeft = Math.min(seedLimit, sides.size());
		while (world.sides.size() < Math.min(seedLimit, sides.size())) {
			int sidesLeft = sides.size();
			for (int i = 0; i < sides.size() && seedsLeft > 0; ++i, --sidesLeft)
				if (seedsLeft >= sidesLeft
						|| world.random.bool((double) (seedsLeft) / sidesLeft)) {
					if (!world.sides.contains(sides.get(i))) {
						world.addSide(sides.get(i));
						seedsLeft--;
					}
				}
		}
	}

	public void reset() {
		world.reset();
		roundScores.reset();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).reset();
	}

	public void collectStatistics() {
		// reset
		mannas = 0;
		corpses = 0;
		mannaValue = 0;
		corpseValue = 0;
		robotValue = 0;
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).resetSampledStatistics();
		// collect
		try {
			for (int i = 0; i <= world.tilesX * world.tilesY; i++) {
				// robots and territory
				Side side = null;
				boolean exclusive = true;
				for (GBObject robot = world.objects.get(GBObjectClass.ocRobot)[i]; robot != null; robot = robot.next) {
					robot.collectStatistics(this);
					if (exclusive) {
						if (side == null)
							side = robot.getOwner();
						else if (side != robot.getOwner())
							exclusive = false;
					}
				}
				if (side != null && exclusive
						&& i != world.tilesX * world.tilesY)
					side.getScores().reportTerritory();
				// other classes
				for (int cur = GBObjectClass.ocFood.value; cur < GBObjectClass
						.values().length; cur++)
					for (GBObject ob = world.objects.get(GBObjectClass
							.byValue(cur))[i]; ob != null; ob = ob.next)
						ob.collectStatistics(this);
			}
		} catch (Exception err) {
			throw new GBSimulationError("Error collecting statistics: "
					+ err.getMessage());
		}
		// report
		roundScores.reset();
		for (int i = 0; i < sides.size(); ++i) {
			sides.get(i).getScores().reportFrame(world.currentFrame);
			roundScores.add(sides.get(i).getScores());
		}
		roundScores.oneRound();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).getScores().reportTotals(roundScores);
	}

	public void reseedDeadSides() {
		// since this uses side statistics, be sure statistics have been
		// gathered
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getScores().getSterileTime() != 0) {
				world.addSeed(sides.get(i), world.getRandomLocation(0));
			}
		collectStatistics();
	}

	public void addSeeds() {
		if (world.sides.size() == 0)
			pickSeededSides();
		world.addSeeds();
	}

	public int getSidesSeeded() {
		return world.sidesSeeded;
	}

	public void resize(FinePoint newsize) {
		if (newsize == world.size)
			return;
		world.resize(newsize);
		world.reset();
	}

	public int currentFrame() {
		return world.currentFrame;
	}

	public boolean roundOver() {
		return stopOnElimination && previousSidesAlive > getSidesAlive()
				&& getSidesAlive() <= 1
				|| (timeLimit > 0 && currentFrame() % timeLimit == 0);
	}

	public void addSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to add null side");
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getName().equals(side.getName()))
				side.setName(side.getName() + '\'');
		sides.add(side);
	}

	public void replaceSide(Side oldSide, Side newSide) {
		if (oldSide == null || newSide == null)
			throw new NullPointerException("replacing null side");
		if (!running) {
			handleSideReplacement(oldSide, newSide);
		} else {
			// Replacement will happen once the current frame is over.
			sideToReplace = oldSide;
			replacementSide = newSide;
		}

	}

	void handleSideReplacement(Side oldSide, Side newSide) {
		int pos = sides.indexOf(oldSide);
		sides.remove(oldSide);
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getName().equals(newSide.getName()))
				newSide.setName(newSide.getName() + '\'');
		sides.add(pos, newSide);
		if (world.sides.contains(oldSide))
			world.replaceSide(oldSide, newSide);
	}

	public void removeSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to remove null side");
		if (running) {
			sidesToRemove.add(side);
			return;
		}
		sides.remove(side);
		if (world.sides.contains(side))
			world.removeSide(side);
		checkSides();
	}

	void checkSides() {
		if (sides.size() == 0) {
			for (ObjectSelectionListener l : objectListeners)
				l.setSelectedObject(null);
			running = false;
			resetTournamentScores();
		}
	}

	public void removeAllSides() {
		if (running) {
			sidesToRemove.addAll(sides);
			return;
		}
		sides.clear();
		world.removeAllSides();
		checkSides();
	}

	public GBWorld getWorld() {
		return world;
	}

	public Side getSide(int index) {
		if (index <= 0 || index > sides.size())
			throw new IndexOutOfBoundsException("invalid side index: " + index);
		return sides.get(index - 1);
	}

	public int getSidesCount() {
		return sides.size();
	}

	public int getSidesAlive() {
		int sidesAlive = 0;
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getScores().getPopulation() > 0)
				sidesAlive++;
		return sidesAlive;
	}

	int getMannas() {
		return mannas;
	}

	int getCorpses() {
		return corpses;
	}

	public int getMannaValue() {
		return mannaValue;
	}

	public int getCorpseValue() {
		return corpseValue;
	}

	public int getRobotValue() {
		return robotValue;
	}

	public void reportManna(double amount) {
		mannas++;
		mannaValue += Math.round(amount);
	}

	public void reportCorpse(double amount) {
		corpses++;
		corpseValue += Math.round(amount);
	}

	public void reportRobot(double amount) {
		robotValue += Math.round(amount);
	}

	void reportRound() {
		roundScores.reset();
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getScores().getSeeded() != 0) {
				roundScores.add(sides.get(i).getScores()); // re-sum sides to get
														// elimination right
				sides.get(i).getTournamentScores().add(sides.get(i).getScores());
			}
		roundScores.oneRound();
		tournamentScores.add(roundScores);
	}

	public void resetTournamentScores() {
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).getTournamentScores().reset();
		tournamentScores.reset();
	}

	void putPercentCell(Writer f, boolean html, double percent, int digits,
			boolean enoughData, double low, double high, String lowclass,
			String highclass) throws IOException {
		String label = !enoughData ? "uncertain" : percent < low ? lowclass
				: percent > high ? highclass : "";
		if (html) {
			if (label != "")
				f.write("<td class=" + label + ">");
			else
				f.write("<td>");
		} else {
			f.write("||");
			if (label != "")
				f.write(" class='" + label + "'|");
		}
		f.write(StringUtilities.toPercentString(percent, digits));
	}

	public void dumpTournamentScores(boolean html) throws IOException {
		String date = new SimpleDateFormat("M-d-Y H:m:s").format(new Date());
		PrintWriter f = new PrintWriter("tournament-scores.html");
		if (html) {
			f.write(htmlTournamentResults(true));
			f.close();
			return;
		} else
			f.write("\n==="
					+ date
					+ "===\n\n"
					+ "{| class=\"wikitable sortable\"\n|-\n"
					+ "!Rank\n!Side\n!Author\n!Score\n!Nonsterile survival\n!Early death\n!Late death\n"
					+ "!Early score\n!Fraction\n!Kills\n");
		List<Side> sorted = sides;
		Collections.sort(sorted);
		double survival = tournamentScores.getSurvivalNotSterile();
		double earlyDeaths = tournamentScores.EarlyDeathRate();
		double lateDeaths = tournamentScores.getLateDeathRate();
		for (int i = 0; i < sorted.size(); ++i) {
			Side s = sorted.get(i);
			f.write("|-\n|" + (i + 1) + "||" + s.getName() + "||" + s.getAuthor());
			GBScores sc = s.getTournamentScores();
			int rounds = sc.rounds;
			int notearly = rounds - sc.earlyDeaths;
			putPercentCell(f, html, sc.getBiomassFraction(), 1, true, 0.0, 1.0,
					"", "");
			putPercentCell(f, html, sc.getSurvivalNotSterile(), 0,
					rounds >= kMinColorRounds, Math.min(survival, 0.2f),
					Math.max(survival, 0.4f), "bad", "good");
			putPercentCell(f, html, sc.EarlyDeathRate(), 0,
					rounds >= kMinColorRounds, Math.min(0.2f, earlyDeaths),
					Math.max(earlyDeaths, 0.4f), "good", "bad");
			putPercentCell(f, html, sc.getLateDeathRate(), 0,
					notearly >= kMinColorRounds, Math.min(0.4f, lateDeaths),
					Math.max(lateDeaths, 0.6f), "good", "bad");
			putPercentCell(f, html, sc.getEarlyBiomassFraction(), 1, rounds
					+ notearly >= kMinColorRounds * 2, 0.08f, 0.12f, "bad",
					"good");
			putPercentCell(f, html, sc.getSurvivalBiomassFraction(), 0,
					sc.survived >= kMinColorRounds, 0.2f, 0.4f, "low", "high");
			putPercentCell(f, html, sc.getKilledFraction(), 0,
					rounds >= kMinColorRounds, 0.05f, 0.15f, "low", "high");
			f.write("\n");
		}
		f.write("|-\n!colspan='4'|Overall ("
				+ tournamentScores.rounds
				+ " rounds):||"
				+ StringUtilities.toPercentString(
						tournamentScores.getSurvivalNotSterile(), 0)
				+ "||"
				+ StringUtilities.toPercentString(
						tournamentScores.EarlyDeathRate(), 0)
				+ "||"
				+ StringUtilities.toPercentString(
						tournamentScores.getLateDeathRate(), 0)
				+ "!!colspan='2'| ||"
				+ StringUtilities.toPercentString(tournamentScores.getKillRate(),
						0) + "\n|}\n");
		f.close();
	}

	public GBScores getRoundScores() {
		return roundScores;
	}

	public GBScores getTournamentScores() {
		return tournamentScores;
	}

	@Override
	public RobotType getSelectedType() {
		return null;
	}

	@Override
	public void addTypeSelectionListener(TypeSelectionListener listener) {
		if (listener != null)
			typeListeners.add(listener);
	}

	@Override
	public void removeTypeSelectionListener(TypeSelectionListener listener) {
		if (listener != null)
			typeListeners.remove(listener);
	}

	@Override
	public Side getSelectedSide() {
		return null;
	}

	@Override
	public void addSideSelectionListener(SideSelectionListener listener) {
		if (listener != null)
			sideListeners.add(listener);
	}

	@Override
	public void removeSideSelectionListener(SideSelectionListener listener) {
		if (listener != null)
			sideListeners.remove(listener);
	}

	@Override
	public GBObject getSelectedObject() {
		return null;
	}

	@Override
	public void addObjectSelectionListener(ObjectSelectionListener listener) {
		if (listener != null)
			objectListeners.add(listener);
	}

	@Override
	public void removeObjectSelectionListener(ObjectSelectionListener listener) {
		if (listener != null)
			objectListeners.remove(listener);
	}

	public void setReportErrors(boolean value) {
		world.reportErrors = value;
	}

	public void setReportPrints(boolean value) {
		world.reportPrints = value;
	}

	static final String headerText = "<thead class=header><tr>" + "<th></th>"
			+ "<th>Side</th>" + "<th>Author</th>" + "<th>Score</th>"
			+ "<th>Error</th>" + "<th>Nonsterile<br/>Survival</th>"
			+ "<th>Early<br/>Death</th>" + "<th>Late<br/>Death</th>"
			+ "<th>Early<br/>Score</th>" + "<th>Fraction</th>"
			+ "<th>Kills</th>" + "<th>Rounds</th>" + "</tr></thead>";

	static final String tableFormat = "%s<table><caption>%s</caption>%s<tbody>%s</tbody><tfoot>%s</tfoot></table>";

	static final String inlineStyle = "<style type=text/css>"
			+ ".header {border: 1px solid; padding: 3px; }"
			+ ".bodyrow {border: 1px solid; padding: 3px; }"
			+ ".footer {border: 1px solid; padding: 3px; font-weight: bold;}"
			+ "td {text-align: center; }"
			+ "th {text-align: center; padding: 5px;}"
			+ "caption {font-weight: bold; text-align: center; padding: 5px; font-size: large;}"
			+ "</style>";

	public String htmlTournamentResults(boolean asDump) {
		return String.format(
				tableFormat,
				inlineStyle,
				asDump ? "Tournament Results<br/>"
						+ new SimpleDateFormat("M-d-Y H:m:s")
								.format(new Date()) : "Standings", headerText,
				buildTableBody(asDump), buildFooter());
	}

	String buildTableBody(boolean asDump) {
		List<Side> list = new ArrayList<Side>();
		list.addAll(sides);
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			Side side = list.get(i);
			GBScores scores = side.getTournamentScores();
			int rounds = scores.rounds;
			int survived = scores.survived;
			int notearly = rounds - scores.earlyDeaths;
			sb.append("<tr class=bodyrow>");
			// ID and name
			sb.append(StringUtilities.makeTableCell("%d.",
					GBColor.ContrastingTextColor(side.getColor()), i + 1));
			if (asDump)
				sb.append(StringUtilities.makeTableCell(150,
						"<a href='sides/%s'>%s</a>", null, new File(
								side.filename).getName(), side.getName()));
			else
				sb.append(StringUtilities.makeTableCell(150, "%s", null,
						side.getName()));
			sb.append(StringUtilities.makeTableCell(75, "%s", null,
					side.getAuthor()));
			// Score error
			if (rounds + survived >= 10)
				sb.append(StringUtilities.makeTableCell("%.1f%%",
						survived > 10 ? null : Color.gray,
						scores.getBiomassFraction() * 100));
			else
				sb.append(StringUtilities.makeEmptyTableCell());
			// Other scores
			if (rounds > 0) {
				sb.append(StringUtilities.makeTableCell("%.1f%%",
						(rounds + survived < kMinColorRounds * 2 || scores
								.getBiomassFractionError() < scores
								.getBiomassFractionError() * 2) ? Color.gray
								: null, scores.getBiomassFractionError() * 100));
				sb.append(StringUtilities.makeTableCell(
						"%.0f%%",
						rangeColor(scores.getSurvivalNotSterile(), 0.2, 0.4,
								GBColor.darkRed, GBColor.darkGreen, rounds, 0),
						scores.getSurvivalNotSterile() * 100));
				sb.append(StringUtilities.makeTableCell(
						"%.0f%%",
						rangeColor(scores.EarlyDeathRate(), 0.2, 0.4,
								GBColor.darkGreen, GBColor.darkRed, rounds, 0),
						scores.EarlyDeathRate() * 100));
				if (notearly > 0) {
					sb.append(StringUtilities.makeTableCell(
							"%.0f%%",
							rangeColor(scores.getLateDeathRate(), 0.4, 0.6,
									GBColor.darkGreen, GBColor.darkRed,
									notearly, 0), scores.getLateDeathRate() * 100));
				} else {
					sb.append(StringUtilities.makeEmptyTableCell());
				}
				sb.append(StringUtilities.makeTableCell(
						"%.0f%%",
						rangeColor(scores.getEarlyBiomassFraction(), 0.08f, 0.12f,
								GBColor.darkRed, GBColor.darkGreen, rounds
										+ notearly, kMinColorRounds * 2),
						scores.getEarlyBiomassFraction() * 100));
				if (survived > 0) {
					sb.append(StringUtilities.makeTableCell(
							"%.0f%%",
							rangeColor(scores.getSurvivalBiomassFraction(), 0.2,
									0.4, Color.blue, GBColor.purple, survived,
									0), scores.getSurvivalBiomassFraction() * 100));
				} else {
					sb.append(StringUtilities.makeEmptyTableCell());
				}
				sb.append(StringUtilities.makeTableCell(
						"%.0f%%",
						rangeColor(scores.getKilledFraction(), 0.05, 0.15,
								Color.blue, GBColor.purple, survived, 0),
						scores.getKilledFraction() * 100));
			} else
				sb.append(StringUtilities.makeEmptyTableCells(7));
			sb.append(StringUtilities.makeTableCell("%d",
					rounds < kMinColorRounds ? GBColor.darkRed : null, rounds));
		}
		sb.append("</tr>");
		return sb.toString();
	}

	String buildFooter() {
		StringBuilder sb = new StringBuilder();
		GBScores scores = tournamentScores;
		int rounds = scores.rounds;
		int notearly = scores.survived;
		sb.append("<tr class=footer>");
		sb.append(StringUtilities.makeEmptyTableCell());
		sb.append(StringUtilities.makeTableCell(150, "%s", null, "Overall:"));
		sb.append(StringUtilities.makeEmptyTableCells(3));
		if (rounds > 0) {
			sb.append(StringUtilities.makeTableCell(
					"%.0f%%",
					rangeColor(scores.getSurvivalNotSterile(), 0.25, 0.5,
							GBColor.darkRed, GBColor.darkGreen, rounds, 0),
					scores.getSurvivalNotSterile() * 100));
			sb.append(StringUtilities.makeTableCell(
					"%.0f%%",
					rangeColor(scores.EarlyDeathRate(), 0.2, 0.4,
							GBColor.darkGreen, GBColor.darkRed, rounds, 0),
					scores.EarlyDeathRate() * 100));
			if (notearly > 0) {
				sb.append(StringUtilities.makeTableCell(
						"%.0f%%",
						rangeColor(scores.getLateDeathRate(), 0.45, 0.6,
								GBColor.darkGreen, GBColor.darkRed, rounds, 0),
						scores.getLateDeathRate() * 100));
			} else {
				sb.append(StringUtilities.makeEmptyTableCell());
			}
			sb.append(StringUtilities.makeEmptyTableCells(2));
			sb.append(StringUtilities.makeTableCell(
					"%.0f%%",
					rangeColor(scores.getKillRate(), 1.2, 1.8, Color.blue,
							GBColor.purple, rounds, 0), scores.getKillRate() * 100));
		} else
			sb.append(StringUtilities.makeEmptyTableCells(6));
		sb.append(StringUtilities.makeTableCell("%d",
				rounds < kMinColorRounds ? GBColor.darkRed : null, rounds));
		sb.append("</tr>");
		return sb.toString();
	}

	static final int kMinColorRounds = 10;

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
}
