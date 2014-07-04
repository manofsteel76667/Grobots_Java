package simulation;

// GBWorld.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

/*/*#if MAC && ! HEADLESS
 #define GBWORLD_PROFILING 1
 #else
 #define GBWORLD_PROFILING 0
 #endif*/
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
import support.FinePoint;
import support.GBObjectClass;
import support.GBRandomState;
import support.StringUtilities;
import exception.GBError;
import exception.GBSimulationError;
import exception.GBTooManyIterationsError;

public class GBWorld extends GBObjectWorld {
	public static final double kRandomMinWallDistance = 2;
	List<Side> sides;
	Side _selectedSide;
	int currentFrame;
	int previousSidesAlive; // num of non-extinct sides last frame
	int sidesSeeded;
	GBRandomState random;
	double mannaLeft;
	// stats
	int mannas, corpses;
	int mannaValue, corpseValue, robotValue;
	GBScores roundScores;
	GBScores tournamentScores;
	// timing
	/*
	 * /*#if GBWORLD_PROFILING && MAC UInt64 simulationTime, moveTime,
	 * collideTime, thinkTime, actTime, resortTime, statisticsTime; UnsignedWide
	 * beginTime; #endif
	 */
	// //public:
	// operation and tournament
	public boolean running; // here so PAUSE primitive can write it :(
	public int timeLimit;
	public boolean stopOnElimination;
	public boolean tournament;
	public int tournamentLength;
	public boolean reportErrors, reportPrints;
	// simulation parameters
	public int seedLimit;
	public boolean autoReseed;
	public double mannaSize;
	public double mannaDensity;
	public double mannaRate;
	public double seedValue;
	public double seedTypePenalty;

	// //public:

	// timing
	/*
	 * /*#if GBWORLD_PROFILING int TotalTime() ; int SimulationTime() ; double
	 * ThinkTime() ; double MoveTime() ; double ActTime() ; double CollideTime()
	 * ; double ResortTime() ; double StatisticsTime() ; void ResetTimes();
	 * #endif
	 */

	public static final double kDefaultMannaDensity = 150; // energy / tile
	public static final double kDefaultMannaRate = 0.25; // energy / tile /
															// frame
	public static final double kDefaultMannaSize = 400;

	public static final int kDefaultTimeLimit = 18000;

	public static final double kSeedRadius = 2;
	public static final double kDefaultSeedValue = 5000;
	public static final double kDefaultSeedTypePenalty = 100;

	/*
	 * /*#if GBWORLD_PROFILING && MAC #define PROFILE_PHASE(var, code) \
	 * Microseconds(&phaseStart); \ code \ Microseconds(&end); \ var =
	 * U64Add(U64Subtract(UnsignedWideToUInt64(end),
	 * UnsignedWideToUInt64(phaseStart)), var); #else #define PROFILE_PHASE(var,
	 * code) code #endif
	 */

	void ThinkAllObjects() {
		// only bothers with robots
		// try {
		for (int i = 0; i <= tilesX * tilesY; i++)
			for (GBObject ob = objects.get(GBObjectClass.ocRobot)[i]; ob != null; ob = ob.next)
				ob.Think(this);
		// } catch ( Exception err ) {
		// GBError.NonfatalError("Error thinking object: " + err.toString());
		// }
	}

	void ActAllObjects() {
		try {
			for (int i = 0; i <= tilesX * tilesY; i++)
				for (GBObjectClass cur : GBObjectClass.values())
					for (GBObject ob = objects.get(cur)[i]; ob != null; ob = ob.next)
						ob.Act(this);
		} catch (Exception e) {
			GBError.NonfatalError("Error acting object: " + e.toString());
		}
	}

	void AddManna() {
		for (mannaLeft += size.x * size.y * mannaRate
				/ (kForegroundTileSize * kForegroundTileSize); mannaLeft > mannaSize; mannaLeft -= mannaSize)
			AddObjectNew(new GBManna(RandomLocation(0), mannaSize));
	}

	void PickSeedPositions(FinePoint[] positions, int numSeeds) {
		if (numSeeds < 1)
			return;
		try {
			double wallDist = kSeedRadius + Math.min(size.x, size.y) / 20;
			double separation = Math.sqrt((size.x - wallDist * 2)
					* (size.y - wallDist * 2) / numSeeds);
			int iterations = 0;
			int iterLimit = 100 + 30 * numSeeds + numSeeds * numSeeds;
			boolean inRange;
			// pick positions
			for (int i = 0; i < numSeeds; i++) {
				do {
					inRange = false;
					positions[i] = RandomLocation(wallDist);
					// TODO in small worlds, this leaves too much space in
					// center
					if (positions[i].inRange(Size().divide(2), separation
							- separation * iterations * 2 / iterLimit))
						inRange = true;
					else
						for (int j = 0; j < i; j++)
							if (positions[i].inRange(positions[j], separation
									- separation * iterations / iterLimit)) {
								inRange = true;
								break;
							}
					if (++iterations > iterLimit)
						throw new GBTooManyIterationsError();
				} while (inRange);
			}
			if (reportErrors && iterations > iterLimit / 2)
				GBError.NonfatalError("Warning: seed placement took "
						+ iterations + " iterations");
			// shuffle positions
			// the above algorithm is not uniform, in that the first element may
			// have different typical location than the last
			// to fix this, permute randomly (Knuth Vol 2 page 125)
			for (int j = numSeeds - 1; j > 0; j--) { // iteration with j==0 is
														// nop, so skip it
				int i = Randoms().intInRange(0, j);
				FinePoint temp = positions[i];
				positions[i] = positions[j];
				positions[j] = temp;
			}
		} catch (GBTooManyIterationsError err) {
			if (reportErrors)
				GBError.NonfatalError("Warning: PickSeedPositions failsafe used.");
			for (int i = 0; i < numSeeds; ++i)
				positions[i] = RandomLocation(0);
		}
	}

	void AddInitialManna() {
		double amount = size.x * size.y
				/ (kForegroundTileSize * kForegroundTileSize) * mannaDensity;
		double placed;
		for (; amount > 0; amount -= placed) {
			placed = amount > mannaSize ? Randoms().InRange(mannaSize / 10,
					mannaSize) : amount;
			AddObjectNew(new GBManna(RandomLocation(0), placed));
			// AddObjectDirectly(new GBManna(RandomLocation(0), placed));
		}
		addNewObjects();
	}

	public GBWorld() {
		sides = new ArrayList<Side>();
		roundScores = new GBScores();
		tournamentScores = new GBScores();
		random = new GBRandomState();
		stopOnElimination = true;
		timeLimit = kDefaultTimeLimit;
		tournamentLength = -1;
		reportErrors = true;
		seedLimit = 10;
		mannaSize = kDefaultMannaSize;
		mannaDensity = kDefaultMannaDensity;
		mannaRate = kDefaultMannaRate;
		seedValue = kDefaultSeedValue;
		seedTypePenalty = kDefaultSeedTypePenalty;
		AddInitialManna();
		/*
		 * #if GBWORLD_PROFILING && MAC ResetTimes(); #endif
		 */
	}

	/*
	 * ~GBWorld() { RemoveAllSides(); }
	 */

	public void SimulateOneFrame() {
		/*
		 * #if GBWORLD_PROFILING && MAC UnsignedWide start, phaseStart, end;
		 * Microseconds(&start); #endif
		 */
		previousSidesAlive = SidesAlive();
		if (autoReseed)
			ReseedDeadSides();
		AddManna();
		/* PROFILE_PHASE(thinkTime, */ThinkAllObjects();// )
		/* PROFILE_PHASE(moveTime, */MoveAllObjects();// )
		/* PROFILE_PHASE(actTime, */ActAllObjects();// )
		/* PROFILE_PHASE(resortTime, */ResortObjects();// )
		/* PROFILE_PHASE(collideTime, */CollideAllObjects();// )
		currentFrame++;
		/* PROFILE_PHASE(statisticsTime, */CollectStatistics();// )
		// TODO: Replace when sound is implemented
		// if ( previousSidesAlive > SidesAlive() )
		// StartSound(siExtinction);
		/*
		 * #if GBWORLD_PROFILING && MAC Microseconds(&end); simulationTime =
		 * U64Add(U64Subtract(UnsignedWideToUInt64(end),
		 * UnsignedWideToUInt64(start)), simulationTime); #endif
		 */
		Changed();
	}

	public synchronized void AdvanceFrame() throws
						       GBTooManyIterationsError,
						       GBSimulationError {
		SimulateOneFrame();
		if (RoundOver())
			EndRound();
	}

	public void EndRound() throws GBTooManyIterationsError, GBSimulationError {
		// TODO: Replace when sound is implemented
		// StartSound(siEndRound);
		// TODO extend biomassHistory to 18k when ending? (to avoid misleading
		// graph)
		ReportRound();
		if (tournament) {
			if (tournamentLength > 0)
				--tournamentLength;
			if (tournamentLength != 0) {
				Reset();
				AddSeeds();
				CollectStatistics();
			} else {
				tournament = false;
				running = false;
			}
		} else
			running = false;
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
			for (int i = 0; i <= tilesX * tilesY; i++) {
				// robots and territory
				Side side = null;
				boolean exclusive = true;
				for (GBObject robot = objects.get(GBObjectClass.ocRobot)[i]; robot != null; robot = robot.next) {
					robot.CollectStatistics(this);
					if (exclusive) {
						if (side == null)
							side = robot.Owner();
						else if (side != robot.Owner())
							exclusive = false;
					}
				}
				if (side != null && exclusive && i != tilesX * tilesY)
					side.Scores().ReportTerritory();
				// other classes
				for (int cur = GBObjectClass.ocFood.value; cur < GBObjectClass
						.values().length; cur++)
					for (GBObject ob = objects.get(GBObjectClass.byValue(cur))[i]; ob != null; ob = ob.next)
						ob.CollectStatistics(this);
			}
		} catch (Exception err) {
			GBError.NonfatalError("Error collecting statistics: "
					+ err.toString());
		}
		// report
		roundScores.Reset();
		for (int i = 0; i < sides.size(); ++i) {
			sides.get(i).Scores().ReportFrame(currentFrame);
			roundScores.add(sides.get(i).Scores());
		}
		roundScores.OneRound();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).Scores().ReportTotals(roundScores);
	}

	public void AddSeed(Side side, FinePoint where) {
		try {
			double cost = seedValue - seedTypePenalty * side.CountTypes();
			// give side a number
			if (side.ID() == 0)
				side.SetID(++sidesSeeded);
			// add cells
			RobotType type;
			GBRobot bot = null;
			List<GBRobot> placed = new ArrayList<GBRobot>();
			int lastPlaced = -1; // last value of i for last successful place
			for (int i = 0;; i++) {
				type = side.GetSeedType(i);
				if (type == null)
					throw new RuntimeException(
							"must have at least one type to seed");
				if (type.Cost() <= cost) {
					bot = new GBRobot(type, where.add(random
							.Vector(kSeedRadius)));
					AddObjectNew(bot);
					// AddObjectDirectly(bot);
					side.Scores().ReportSeeded(type.Cost());
					cost -= type.Cost();
					lastPlaced = i;
					placed.add(bot);
				} else
					break; // if unseedable, stop - this one will be a fetus
				// Old version: keep trying until we've gone through list once
				// without placing anything
				// if (i - lastPlaced >= side.NumSeedTypes())
				// break;
			}
			// give excess energy as construction
			int placedIndex;
			for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
				GBRobot placee = placed.get(placedIndex);
				if (cost == 0)
					break;
				if (placee.hardware.constructor.MaxRate() != 0) {
					double amt = Math.min(cost, side
							.GetSeedType(lastPlaced + 1).Cost());
					placee.hardware.constructor.Start(
							side.GetSeedType(lastPlaced + 1), amt);
					side.Scores().ReportSeeded(amt);
					cost -= amt;
					if (cost > 0)
						throw new RuntimeException(
								"When seeding, energy left-over after bonus fetus");
				}
			}
			// energy still left (implies constructor-less side!); try giving as
			// energy
			for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
				if (cost == 0)
					break;
				double amt = placed.get(placedIndex).hardware.GiveEnergy(cost);
				side.Scores().ReportSeeded(amt);
				cost -= amt;
			}
			// all else fails, make a manna.
			if (cost > 0)
				AddObjectNew(new GBManna(where, cost));
			// AddObjectDirectly(new GBManna(where, cost)); // no ReportSeeded
			// because it's
			// pretty
			// worthless
			addNewObjects();
		} catch (Exception e) {
			GBError.NonfatalError("Error adding seed:" + e.toString());
		}
	}

	public void AddSeeds() throws
			GBTooManyIterationsError, GBSimulationError {
		int numSides = CountSides();
		int numSeeds = seedLimit != 0 ? (seedLimit > numSides ? numSides
				: seedLimit) : numSides;
		// pick positions
		FinePoint[] positions = new FinePoint[numSeeds];
		PickSeedPositions(positions, numSeeds);
		// seed sides
		int seedsLeft = numSeeds;
		int sidesLeft = numSides;
		for (int i = 0; i < sides.size() && seedsLeft != 0; ++i, --sidesLeft)
			if (seedsLeft >= sidesLeft
					|| random.bool((double) (seedsLeft) / sidesLeft)) {
				if (seedsLeft == 0)
					throw new GBTooManyIterationsError();
				sides.get(i).center = positions[numSeeds - seedsLeft];
				AddSeed(sides.get(i), positions[numSeeds - seedsLeft]);
				--seedsLeft;
			}
		// delete[] positions;
		if (seedsLeft != 0)
			throw new GBSimulationError();
		CollectStatistics();
		Changed();
	}

	public void ReseedDeadSides() {
		// since this uses side statistics, be sure statistics have been
		// gathered
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Scores().SterileTime() != 0) {
				// sides.get(i).Reset(); //why?
				AddSeed(sides.get(i), RandomLocation(0));
			}
		CollectStatistics();
	}

	public void Reset() {
		currentFrame = 0;
		mannaLeft = 0;
		sidesSeeded = 0;
		ClearLists();
		for (int i = 0; i < sides.size(); ++i)
			sides.get(i).Reset();
		roundScores.Reset();
		AddInitialManna();
		Changed();
	}

	@Override
	public void Resize(FinePoint newsize) {
		if (newsize == size)
			return;
		super.Resize(newsize);
		Reset();
	}

	public int CurrentFrame() {
		return currentFrame;
	}

	public boolean RoundOver() {
		return stopOnElimination && previousSidesAlive > SidesAlive()
				&& SidesAlive() <= 1 || timeLimit > 0
				&& CurrentFrame() % timeLimit == 0;
	}

	public GBRandomState Randoms() {
		return random;
	}

	FinePoint RandomLocation(double walldist) {
		return new FinePoint(random.InRange(walldist, size.x - walldist),
				random.InRange(walldist, size.y - walldist));
	}

	public void AddSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to add null side");
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).Name().equals(side.Name()))
				side.SetName(side.Name() + '\'');
		sides.add(side);
		Changed();
	}

	public void ReplaceSide(Side oldSide, Side newSide) {
		if (oldSide == null || newSide == null)
			throw new NullPointerException("replacing null side");
		int pos = sides.indexOf(oldSide);
		sides.remove(oldSide);
		sides.add(pos, newSide);
		Changed();
	}

	public void RemoveSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to remove null side");
		// if (side == selectedSide)
		// selectedSide = null;
		sides.remove(side);
		Changed();
	}

	public void RemoveAllSides() {
		// for (int i = 0; i < sides.size(); ++ i )
		// delete sides.get(i);
		sides.clear();
		// selectedSide = null;
		ResetTournamentScores();
		Changed();
	}

	public List<Side> Sides() {
		return sides;
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

	int MannaValue() {
		return mannaValue;
	}

	int CorpseValue() {
		return corpseValue;
	}

	public int RobotValue() {
		return robotValue;
	}

	void ReportManna(double amount) {
		mannas++;
		mannaValue += Math.round(amount);
	}

	void ReportCorpse(double amount) {
		corpses++;
		corpseValue += Math.round(amount);
	}

	void ReportRobot(double amount) {
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
		Changed();
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

	/*
	 * #if GBWORLD_PROFILING && MAC int TotalTime() { UnsignedWide now;
	 * Microseconds(&now); return
	 * U64SetU(U64Div(U64Add(U64Subtract(UnsignedWideToUInt64(now),
	 * UnsignedWideToUInt64(beginTime)), 500), 1000)); }
	 * 
	 * int SimulationTime() { return U64SetU(U64Div(U64Add(simulationTime, 500),
	 * 1000)); }
	 * 
	 * #define U64RatioSafe(num, denom) (U32SetU(denom) ? (double)U32SetU(num) /
	 * (double)U32SetU(denom) : 0.0f)
	 * 
	 * double ThinkTime() { return U64RatioSafe(thinkTime, simulationTime); }
	 * 
	 * double MoveTime() { return U64RatioSafe(moveTime, simulationTime); }
	 * 
	 * double ActTime() { return U64RatioSafe(actTime, simulationTime); }
	 * 
	 * double CollideTime() { return U64RatioSafe(collideTime, simulationTime);
	 * }
	 * 
	 * double ResortTime() { return U64RatioSafe(resortTime, simulationTime); }
	 * 
	 * double StatisticsTime() { return U64RatioSafe(statisticsTime,
	 * simulationTime); }
	 * 
	 * void ResetTimes() { simulationTime = U64Set(0); thinkTime = U64Set(0);
	 * actTime = U64Set(0); moveTime = U64Set(0); collideTime = U64Set(0);
	 * resortTime = U64Set(0); statisticsTime = U64Set(0);
	 * Microseconds(&beginTime); } #endif
	 */
}
