/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import sides.GBScores;
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.StringUtilities;
import ui.GBApplication;
import ui.ObjectSelectionListener;
import ui.ObjectSelector;
import ui.SideSelectionListener;
import ui.TypeSelectionListener;
import exception.GBSimulationError;

public class GBGame implements ScoreKeeper, ObjectSelector {
	public List<Side> sides;
	GBApplication app;
	int previousSidesAlive; // num of non-extinct sides last frame
	GBWorld world;
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
	
	// things to do after a round
	public boolean slowDrawRequested;
	public boolean fastDrawRequested;

	// simulation parameters
	public int seedLimit;
	public boolean autoReseed;
	public long totalFrames;

	public static final int kDefaultTimeLimit = 18000;

	public GBGame(GBApplication _app) {
		sides = new ArrayList<Side>();
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
		app = _app;
	}

	public void StartRound() {
		Reset();
		roundScores.Reset();
		pickSeededSides();
		world.AddSeeds();
		world.currentFrame = 0;
		CollectStatistics();
	}

	public void advanceFrame() {
		previousSidesAlive = world.SidesAlive();
		world.SimulateOneFrame();
		CollectStatistics();
		if (previousSidesAlive > world.SidesAlive()) {
			// TODO: play extinction sound
		}
		if (totalFrames < Long.MAX_VALUE)// yeah, right...
			totalFrames++;
		else
			totalFrames = 0;
		if (RoundOver())
			EndRound();
		if (fastDrawRequested || world.currentFrame == 0){
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					app.drawFastPanels();
				}
			});
			fastDrawRequested = false;
		}
		if (slowDrawRequested || world.currentFrame == 0){
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					app.drawSlowPanels();
				}
			});
			slowDrawRequested = false;
		}
	}

	public void EndRound() {
		// TODO: Replace when sound is implemented
		// StartSound(siEndRound);
		// TODO extend biomassHistory to 18k when ending? (to avoid misleading
		// graph)
		ReportRound();
		// Free references to the selected object so it can be GC'd
		for (ObjectSelectionListener l : objectListeners)
			l.setSelectedObject(null);
		// Don't set selected side or type to null since someone may want to add
		// a robot or something
		if (tournament) {
			if (tournamentLength > 0)
				--tournamentLength;
			if (tournamentLength != 0) {
				StartRound();
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
						world.AddSide(sides.get(i));
						seedsLeft--;
					}
				}
		}
	}

	public void Reset() {
		world.Reset();
		roundScores.Reset();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).Reset();
	}

	public void CollectStatistics() {
		// reset
		mannas = 0;
		corpses = 0;
		mannaValue = 0;
		corpseValue = 0;
		robotValue = 0;
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).ResetSampledStatistics();
		// collect
		try {
			for (int i = 0; i <= world.tilesX * world.tilesY; i++) {
				// robots and territory
				Side side = null;
				boolean exclusive = true;
				for (GBObject robot = world.objects.get(GBObjectClass.ocRobot)[i]; robot != null; robot = robot.next) {
					robot.CollectStatistics(this);
					if (exclusive) {
						if (side == null)
							side = robot.Owner();
						else if (side != robot.Owner())
							exclusive = false;
					}
				}
				if (side != null && exclusive
						&& i != world.tilesX * world.tilesY)
					side.Scores().ReportTerritory();
				// other classes
				for (int cur = GBObjectClass.ocFood.value; cur < GBObjectClass
						.values().length; cur++)
					for (GBObject ob = world.objects.get(GBObjectClass
							.byValue(cur))[i]; ob != null; ob = ob.next)
						ob.CollectStatistics(this);
			}
		} catch (Exception err) {
			throw new GBSimulationError("Error collecting statistics: "
					+ err.getMessage());
		}
		// report
		roundScores.Reset();
		for (int i = 0; i < sides.size(); ++i) {
			sides.get(i).Scores().ReportFrame(world.currentFrame);
			roundScores.add(sides.get(i).Scores());
		}
		roundScores.OneRound();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).Scores().ReportTotals(roundScores);
	}

	public void ReseedDeadSides() {
		// since this uses side statistics, be sure statistics have been
		// gathered
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Scores().SterileTime() != 0) {
				world.AddSeed(sides.get(i), world.RandomLocation(0));
			}
		CollectStatistics();
	}

	public void addSeeds() {
		if (world.sides.size() == 0)
			pickSeededSides();
		world.AddSeeds();
	}

	public int getSidesSeeded() {
		return world.sidesSeeded;
	}

	public void Resize(FinePoint newsize) {
		if (newsize == world.size)
			return;
		world.Resize(newsize);
		world.Reset();
	}

	public int CurrentFrame() {
		return world.currentFrame;
	}

	public boolean RoundOver() {
		return stopOnElimination && previousSidesAlive > SidesAlive()
				&& SidesAlive() <= 1
				|| (timeLimit > 0 && CurrentFrame() % timeLimit == 0);
	}

	public void AddSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to add null side");
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Name().equals(side.Name()))
				side.SetName(side.Name() + '\'');
		sides.add(side);
	}

	public void ReplaceSide(Side oldSide, Side newSide) {
		if (oldSide == null || newSide == null)
			throw new NullPointerException("replacing null side");
		int pos = sides.indexOf(oldSide);
		sides.remove(oldSide);
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Name().equals(newSide.Name()))
				newSide.SetName(newSide.Name() + '\'');
		sides.add(pos, newSide);
		if (world.sides.contains(oldSide))
			world.ReplaceSide(oldSide, newSide);
	}

	public void RemoveSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to remove null side");
		sides.remove(side);
		if (world.sides.contains(side))
			world.RemoveSide(side);
	}

	public void RemoveAllSides() {
		sides.clear();
		ResetTournamentScores();
		world.RemoveAllSides();
	}

	public GBWorld getWorld() {
		return world;
	}

	public Side GetSide(int index) {
		if (index <= 0 || index > sides.size())
			throw new IndexOutOfBoundsException("invalid side index: " + index);
		return sides.get(index - 1);
	}

	public int CountSides() {
		return sides.size();
	}

	public int SidesAlive() {
		int sidesAlive = 0;
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Scores().Population() > 0)
				sidesAlive++;
		return sidesAlive;
	}

	int Mannas() {
		return mannas;
	}

	int Corpses() {
		return corpses;
	}

	public int MannaValue() {
		return mannaValue;
	}

	public int CorpseValue() {
		return corpseValue;
	}

	public int RobotValue() {
		return robotValue;
	}

	public void ReportManna(double amount) {
		mannas++;
		mannaValue += Math.round(amount);
	}

	public void ReportCorpse(double amount) {
		corpses++;
		corpseValue += Math.round(amount);
	}

	public void ReportRobot(double amount) {
		robotValue += Math.round(amount);
	}

	void ReportRound() {
		roundScores.Reset();
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Scores().Seeded() != 0) {
				roundScores.add(sides.get(i).Scores()); // re-sum sides to get
														// elimination right
				sides.get(i).TournamentScores().add(sides.get(i).Scores());
			}
		roundScores.OneRound();
		tournamentScores.add(roundScores);
	}

	public void ResetTournamentScores() {
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).TournamentScores().Reset();
		tournamentScores.Reset();
	}

	void PutPercentCell(Writer f, boolean html, double percent, int digits,
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

	public static final int kMinColorRounds = 10;

	// The low/high classification logic is duplicated from
	// GBTournamentView::DrawItem.
	public void DumpTournamentScores(boolean html) throws IOException {
		PrintWriter f = new PrintWriter("tournament-scores.html");
		// FileOutputStream f = new FileOutputStream("tournament-scores.html");
		// std::ofstream f("tournament-scores.html", std::ios::app);
		// if ( !f.good() ) return;
		// Date now = new Date();
		String date = new SimpleDateFormat("M d Y H:m:s").format(new Date());
		// strftime(date, 32, "%d %b %Y %H:%M:%S", localtime(&now));
		if (html)
			f.write("\n<h3>Tournament "
					+ date
					+ "</h3>\n\n<table>\n"
					+ "<colgroup><col><col><col><colgroup><col class=key><col><col><col><col><col><col>\n"
					+ "<thead><tr><th>Rank<th>Side<th>Author\n"
					+ "<th>Score<th>Nonsterile<br>survival<th>Early<br>death<th>Late<br>death"
					+ "<th>Early<br>score<th>Fraction<th>Kills\n" + "<tbody>\n");
		else
			f.write("\n==="
					+ date
					+ "===\n\n"
					+ "{| class=\"wikitable sortable\"\n|-\n"
					+ "!Rank\n!Side\n!Author\n!Score\n!Nonsterile survival\n!Early death\n!Late death\n"
					+ "!Early score\n!Fraction\n!Kills\n");
		List<Side> sorted = sides;
		Collections.sort(sorted);
		// std::sort(sorted.begin(), sorted.end(), Side::Better);
		double survival = tournamentScores.SurvivalNotSterile();
		double earlyDeaths = tournamentScores.EarlyDeathRate();
		double lateDeaths = tournamentScores.LateDeathRate();
		for (int i = 0; i < sorted.size(); ++i) {
			Side s = sorted.get(i);
			if (html) {
				f.write("<tr><td>" + (i + 1) + "<td><a href='sides/"
						+ s.filename + "'>");
				f.write(s.Name() + "</a><td>" + s.Author() + "\n");
			} else
				f.write("|-\n|" + (i + 1) + "||" + s.Name() + "||" + s.Author());
			GBScores sc = s.TournamentScores();
			int rounds = sc.rounds;
			int notearly = rounds - sc.earlyDeaths;
			PutPercentCell(f, html, sc.BiomassFraction(), 1, true, 0.0, 1.0,
					"", "");
			PutPercentCell(f, html, sc.SurvivalNotSterile(), 0,
					rounds >= kMinColorRounds, Math.min(survival, 0.2f),
					Math.max(survival, 0.4f), "bad", "good");
			PutPercentCell(f, html, sc.EarlyDeathRate(), 0,
					rounds >= kMinColorRounds, Math.min(0.2f, earlyDeaths),
					Math.max(earlyDeaths, 0.4f), "good", "bad");
			PutPercentCell(f, html, sc.LateDeathRate(), 0,
					notearly >= kMinColorRounds, Math.min(0.4f, lateDeaths),
					Math.max(lateDeaths, 0.6f), "good", "bad");
			PutPercentCell(f, html, sc.EarlyBiomassFraction(), 1, rounds
					+ notearly >= kMinColorRounds * 2, 0.08f, 0.12f, "bad",
					"good");
			PutPercentCell(f, html, sc.SurvivalBiomassFraction(), 0,
					sc.survived >= kMinColorRounds, 0.2f, 0.4f, "low", "high");
			PutPercentCell(f, html, sc.KilledFraction(), 0,
					rounds >= kMinColorRounds, 0.05f, 0.15f, "low", "high");
			f.write("\n");
		}
		if (html)
			f.write("<tfoot><tr><th colspan=4>Overall ("
					+ tournamentScores.rounds
					+ " rounds):<td>"
					+ StringUtilities.toPercentString(
							tournamentScores.SurvivalNotSterile(), 0)
					+ "<td>"
					+ StringUtilities.toPercentString(
							tournamentScores.EarlyDeathRate(), 0)
					+ "<td>"
					+ StringUtilities.toPercentString(
							tournamentScores.LateDeathRate(), 0)
					+ "<th colspan=2><td>"
					+ StringUtilities.toPercentString(
							tournamentScores.KillRate(), 0) + "\n</table>\n");
		else
			f.write("|-\n!colspan='4'|Overall ("
					+ tournamentScores.rounds
					+ " rounds):||"
					+ StringUtilities.toPercentString(
							tournamentScores.SurvivalNotSterile(), 0)
					+ "||"
					+ StringUtilities.toPercentString(
							tournamentScores.EarlyDeathRate(), 0)
					+ "||"
					+ StringUtilities.toPercentString(
							tournamentScores.LateDeathRate(), 0)
					+ "!!colspan='2'| ||"
					+ StringUtilities.toPercentString(
							tournamentScores.KillRate(), 0) + "\n|}\n");
	}

	public GBScores RoundScores() {
		return roundScores;
	}

	public GBScores TournamentScores() {
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
	
	public void setReportErrors(boolean value){
		world.reportErrors = value;
	}
	
	public void setReportPrints(boolean value){
		world.reportPrints = value;
	}
}
