package simulation;
// GBWorld.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

/*#if MAC && ! HEADLESS
#define GBWORLD_PROFILING 1
#else
#define GBWORLD_PROFILING 0
#endif*/
import java.util.*;
import sides.*;
import support.*;

class GBWorld extends GBObjectWorld {
	public static final double kRandomMinWallDistance = 2;
	List<Side> sides;
	Side selectedSide;
	int currentFrame;
	int previousSidesAlive; //num of non-extinct sides last frame
	int sidesSeeded;
	GBRandomState random;
	GBObject followed;
	double mannaLeft;
// stats
	int mannas, corpses;
	int mannaValue, corpseValue, robotValue;
	GBScores roundScores;
	GBScores tournamentScores;
// timing
/*#if GBWORLD_PROFILING && MAC
	UInt64 simulationTime, moveTime, collideTime, thinkTime, actTime, resortTime, statisticsTime;
	UnsignedWide beginTime;
#endif*/
//public:
// operation and tournament
	boolean running; // here so PAUSE primitive can write it :(
	int timeLimit;
	boolean stopOnElimination;
	boolean tournament;
	int tournamentLength;
	boolean reportErrors, reportPrints;
// simulation parameters
	int seedLimit;
	boolean autoReseed;
	double mannaSize;
	double mannaDensity;
	double mannaRate;
	double seedValue;
	double seedTypePenalty;
//private:
	void ThinkAllObjects();
	void ActAllObjects();
	void AddManna();
	void AddInitialManna();
	void PickSeedPositions(FinePoint * positions, int numSeeds);
//public:
// constructors
	GBWorld();
	~GBWorld();
// operation
	void SimulateOneFrame();
	void AdvanceFrame();
	void EndRound();
	void CollectStatistics();
	void AddSeed(Side * side, FinePoint & where);
	void AddSeeds();
	void ReseedDeadSides();
// control
	void Reset();
	void Resize( FinePoint & newsize);
// accessors
	int CurrentFrame() ;
	boolean RoundOver() ;
// randoms
	GBRandomState & Randoms();
	FinePoint RandomLocation(double walldist = kRandomMinWallDistance);
// adding/removing sides
	void AddSide(Side * side);
	void ReplaceSide(Side * oldSide, Side * newSide);
	void RemoveSide(Side * side);
	void RemoveAllSides();
// sides
	public static final List<Side *> & Sides() ;
	Side * GetSide(int index) ;
// selected side
	Side * SelectedSide() ;
	void SelectSide(Side * which);
// stats
	int CountSides() ;
	int SidesAlive() ;
	int Mannas() ;
	int Corpses() ;
	int MannaValue() ;
	int CorpseValue() ;
	int RobotValue() ;
	void ReportManna(double amount);
	void ReportCorpse(double amount);
	void ReportRobot(double amount);
	void ReportRound();
	void ResetTournamentScores();
	void DumpTournamentScores(boolean html);
	public static final GBScores & RoundScores() ;
	public static final GBScores & TournamentScores() ;
// selected object
	void Follow(GBObject * ob);
	GBObject * Followed() ;
	void ReportDeletion( GBDeletionReporter * rep);
// timing
/*#if GBWORLD_PROFILING
	int TotalTime() ;
	int SimulationTime() ;
	double ThinkTime() ;
	double MoveTime() ;
	double ActTime() ;
	double CollideTime() ;
	double ResortTime() ;
	double StatisticsTime() ;
	void ResetTimes();
#endif*/

	public static final double kDefaultMannaDensity = 150; // energy / tile
public static final double kDefaultMannaRate = 0.25; // energy / tile / frame
public static final double kDefaultMannaSize = 400;

public static final short kDefaultTimeLimit = 18000;

public static final double kSeedRadius = 2;
public static final double kDefaultSeedValue = 5000;
public static final double kDefaultSeedTypePenalty = 100;


/*#if GBWORLD_PROFILING && MAC
	#define PROFILE_PHASE(var, code) \
		Microseconds(&phaseStart); \
		code \
		Microseconds(&end); \
		var = U64Add(U64Subtract(UnsignedWideToUInt64(end), UnsignedWideToUInt64(phaseStart)), var);
#else
	#define PROFILE_PHASE(var, code) code
#endif*/


void ThinkAllObjects() {
	//only bothers with robots
	try {
		for ( int i = 0; i <= tilesX * tilesY; i ++ )
			for ( GBObject * ob = objects[i][ocRobot]; ob != null; ob = ob.next )
				ob.Think(this);
	} catch ( GBError & err ) {
		NonfatalError("Error thinking object: ") + err.To));
	}
}

void ActAllObjects() {
	try {
		for ( int i = 0; i <= tilesX * tilesY; i ++ )
			for ( GBObjectClass cur = ocRobot; cur < kNumObjectClasses; cur ++ )
				for ( GBObject * ob = objects[i][cur]; ob != null; ob = ob.next )
					ob.Act(this);
	} catch ( GBError & err ) {
		NonfatalError("Error acting object: ") + err.To));
	}
}

void AddManna() {
	try {
		for ( mannaLeft += size.x * size.y * mannaRate / (kForegroundTileSize * kForegroundTileSize);
				mannaLeft > mannaSize; mannaLeft -= mannaSize )
			AddObjectDirectly(new GBManna(RandomLocation(), mannaSize));
	} catch ( GBError & err ) {
		NonfatalError("Error adding manna: ") + err.To));
	}
}

void PickSeedPositions(FinePoint * positions, int numSeeds) {
	if ( ! positions) throw new GBnullPointerError();
	if ( numSeeds < 1 ) return;
	try {
		double wallDist = kSeedRadius + Math.min(size.x, size.y) / 20;
		double separation = sqrt((size.x - wallDist * 2) * (size.y - wallDist * 2) / numSeeds);
		int iterations = 0;
		int iterLimit = 100 + 30 * numSeeds + numSeeds * numSeeds;
		boolean inRange;
	// pick positions
		for ( int i = 0; i < numSeeds; i++ ) {
			do {
				inRange = false;
				positions[i] = RandomLocation(wallDist);
				//TODO in small worlds, this leaves too much space in center
				if ( positions[i].InRange(Size() / 2, separation - separation * iterations * 2 / iterLimit) )
					inRange = true;
				else
					for ( int j = 0; j < i; j++ )
						if ( positions[i].InRange(positions[j], separation - separation * iterations / iterLimit) ) {
							inRange = true;
							break;
						}
				if ( ++ iterations > iterLimit ) throw new GBTooManyIterationsError();
			} while ( inRange ) ;
		}
		if ( reportErrors && iterations > iterLimit / 2 )
			NonfatalError("Warning: seed placement took " + Toiterations) + " iterations");
	// shuffle positions
		//the above algorithm is not uniform, in that the first element may have different typical location than the last
		//to fix this, permute randomly (Knuth Vol 2 page 125)
		for ( int j = numSeeds - 1; j > 0; j-- ) {  //iteration with j==0 is nop, so skip it
			int i = Randoms().intInRange(0, j);
			FinePoint temp = positions[i];
			positions[i] = positions[j];
			positions[j] = temp;
		}
	} catch ( GBTooManyIterationsError & ) {
		if ( reportErrors ) NonfatalError("Warning: PickSeedPositions failsafe used.");
		for ( int i = 0; i < numSeeds; ++ i )
			positions[i] = RandomLocation();
	}
}

void AddInitialManna() {
	double amount = size.x * size.y / (kForegroundTileSize * kForegroundTileSize) * mannaDensity;
	double placed;
	for ( ; amount > 0; amount -= placed ) {
		placed = amount > mannaSize ? Randoms().InRange(mannaSize / 10, mannaSize) : amount;
		AddObjectDirectly(new GBManna(RandomLocation(), placed));
	}
}

GBWorld()
	: GBObjectWorld(),
	sides(), selectedSide(null),
	roundScores(), tournamentScores(),
	currentFrame(0),
	followed(null),
	mannaLeft(0),
	random(),
	running(false),
	stopOnElimination(true), timeLimit(kDefaultTimeLimit),
	tournament(false), tournamentLength(-1),
	reportErrors(true), reportPrints(false),
	seedLimit(10), autoReseed(false),
	mannaSize(kDefaultMannaSize), mannaDensity(kDefaultMannaDensity), mannaRate(kDefaultMannaRate),
	seedValue(kDefaultSeedValue), seedTypePenalty(kDefaultSeedTypePenalty),
	previousSidesAlive(0),
	sidesSeeded(0),
	mannas(0), corpses(0),
	mannaValue(0), corpseValue(0), robotValue(0)
{
	AddInitialManna();
#if GBWORLD_PROFILING && MAC
	ResetTimes();
#endif
}

~GBWorld() {
	RemoveAllSides();
}

void SimulateOneFrame() {
#if GBWORLD_PROFILING && MAC
	UnsignedWide start, phaseStart, end;
	Microseconds(&start);
#endif
	previousSidesAlive = SidesAlive();
	if ( autoReseed )
		ReseedDeadSides();
	AddManna();
	PROFILE_PHASE(thinkTime, ThinkAllObjects();)
	PROFILE_PHASE(moveTime, MoveAllObjects();)
	PROFILE_PHASE(actTime, ActAllObjects();)
	PROFILE_PHASE(resortTime, ResortObjects();)
	PROFILE_PHASE(collideTime, CollideAllObjects();)
	currentFrame ++;
	PROFILE_PHASE(statisticsTime, CollectStatistics();)
	if ( previousSidesAlive > SidesAlive() )
		StartSound(siExtinction);
#if GBWORLD_PROFILING && MAC
	Microseconds(&end);
	simulationTime = U64Add(U64Subtract(UnsignedWideToUInt64(end), UnsignedWideToUInt64(start)), simulationTime);
#endif
	Changed();
}

void AdvanceFrame() {
	SimulateOneFrame();
	if (RoundOver())
		EndRound();
}

void EndRound() {
		StartSound(siEndRound);
		//TODO extend biomassHistory to 18k when ending? (to avoid misleading graph)
		ReportRound();
		if ( tournament ) {
			if ( tournamentLength > 0 ) -- tournamentLength;
			if ( tournamentLength != 0 ) {
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

void CollectStatistics() {
// reset
	mannas = 0; corpses = 0;
	mannaValue = 0; corpseValue = 0; robotValue = 0;
	for ( int i = 0; i < sides.size(); ++ i )
		sides[i].ResetSampledStatistics();
// collect
	try {
		for ( int i = 0; i <= tilesX * tilesY; i ++ ) {
		// robots and territory
			Side * side = null;
			boolean exclusive = true;
			for ( GBObject * robot = objects[i][ocRobot]; robot != null; robot = robot.next ) {
				robot.CollectStatistics(this);
				if ( exclusive ) {
					if (! side ) side = robot.Owner();
					else if ( side != robot.Owner() ) exclusive = false;
				}
			}
			if ( side && exclusive && i != tilesX * tilesY ) side.Scores().ReportTerritory();
		// other classes
			for ( GBObjectClass cur = ocFood; cur < kNumObjectClasses; cur ++ )
				for ( GBObject * ob = objects[i][cur]; ob != null; ob = ob.next )
					ob.CollectStatistics(this);
		}
	} catch ( GBError & err ) {
		NonfatalError("Error collecting statistics: ") + err.To));
	}
// report
	roundScores.Reset();
	for ( int i = 0; i < sides.size(); ++ i ) {
		sides[i].Scores().ReportFrame(currentFrame);
		roundScores += sides[i].Scores();
	}
	roundScores.OneRound();
	for ( int i = 0; i < sides.size(); ++ i )
		sides[i].Scores().ReportTotals(roundScores);
}

void AddSeed(Side side, FinePoint where) {
	try {
		double cost = seedValue - seedTypePenalty * side.CountTypes();
	//give side a number
		if ( ! side.ID() )
			side.SetID(++sidesSeeded);
	//add cells
		GBRobotType * type;
		GBRobot * bot = null;
		List<GBRobot *> placed;
		int lastPlaced = -1; //last value of i for last successful place
		for (int i = 0; ; i++) {
			type = side.GetSeedType(i);
			if ( ! type )
				throw new GBGenericError("must have at least one type to seed");
			if ( type.Cost() <= cost) {
				bot = new GBRobot(type, where + random.Vector(kSeedRadius));
				AddObjectDirectly(bot);
				side.Scores().ReportSeeded(type.Cost());
				cost -= type.Cost();
				lastPlaced = i;
				placed.push_back(bot);
			} else
				break; //if unseedable, stop - this one will be a fetus
			//Old version: keep trying until we've gone through list once
				//without placing anything
			//if (i - lastPlaced >= side.NumSeedTypes())
			//	break;
		}
	// give excess energy as construction
		int placedIndex;
		for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
			GBRobot * placee = placed[placedIndex];
			if (cost == 0) break;
			if ( placee.hardware.constructor.MaxRate() ) {
				double amt = Math.min(cost, side.GetSeedType(lastPlaced + 1).Cost());
				placee.hardware.constructor.Start(side.GetSeedType(lastPlaced + 1), amt);
				side.Scores().ReportSeeded(amt);
				cost -= amt;
				if ( cost > 0 )
					throw new GBGenericError("When seeding, energy left-over after bonus fetus");
			}
		}
	//energy still left (implies constructor-less side!); try giving as energy
		for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
			if (cost == 0) break;
			double amt = placed[placedIndex].hardware.GiveEnergy(cost);
			side.Scores().ReportSeeded(amt);
			cost -= amt;
		}
	//all else fails, make a manna.
		if ( cost > 0 )
			AddObjectDirectly(new GBManna(where, cost)); // no ReportSeeded because it's pretty worthless

	} catch ( GBError & err ) {
		NonfatalError("Error adding seed:") + err.To));
	}
}

void AddSeeds() {
	int numSides = CountSides();
	int numSeeds = seedLimit ? (seedLimit > numSides ? numSides : seedLimit) : numSides;
// pick positions
	FinePoint * positions = new FinePoint[numSeeds];
	if ( ! positions ) throw new GBOutOfMemoryError();
	PickSeedPositions(positions, numSeeds);
// seed sides
	int seedsLeft = numSeeds;
	int sidesLeft = numSides;
	for ( int i = 0; i < sides.size() && seedsLeft; ++ i, -- sidesLeft )
		if ( seedsLeft >= sidesLeft || random.booleanean(double(seedsLeft) / sidesLeft) ) {
			if ( ! seedsLeft ) throw new GBTooManyIterationsError();
			sides[i].center = positions[numSeeds - seedsLeft];
			AddSeed(sides[i], positions[numSeeds - seedsLeft]);
			-- seedsLeft;
		}
	delete[] positions;
	if ( seedsLeft ) throw new GBSimulationError();
	CollectStatistics();
	Changed();
}

void ReseedDeadSides() {
// since this uses side statistics, be sure statistics have been gathered
	for ( int i = 0; i < sides.size(); ++ i )
		if ( sides[i].Scores().Sterile() ) {
			//sides[i].Reset(); //why?
			AddSeed(sides[i], RandomLocation());
		}
	CollectStatistics();
}

void Reset() {
	currentFrame = 0;
	mannaLeft = 0;
	sidesSeeded = 0;
	ClearLists();
	for ( int i = 0; i < sides.size(); ++ i )
		sides[i].Reset();
	roundScores.Reset();
	AddInitialManna();
	Changed();
}

void Resize( FinePoint & newsize) {
	if ( newsize == size ) return;
	GBObjectWorld::Resize(newsize);
	Reset();
}

int CurrentFrame() {
	return currentFrame;
}

boolean RoundOver() {
	return stopOnElimination && previousSidesAlive > SidesAlive() && SidesAlive() <= 1
		|| timeLimit > 0 && CurrentFrame() % timeLimit == 0;
}

GBRandomState & Randoms() {
	return random;
}

FinePoint RandomLocation(double walldist) {
	return FinePoint(random.InRange(walldist, size.x - walldist),
						random.InRange(walldist, size.y - walldist));
}

void AddSide(Side * side) {
	if ( ! side )
		throw new GBnullPointerError();
	for ( int i = 0; i < sides.size(); ++ i )
		if ( sides[i].Name() == side.Name() )
			side.SetName(side.Name() + '\'');
	sides.push_back(side);
	Changed();
}

void ReplaceSide(Side * oldSide, Side * newSide) {
	if ( ! oldSide || ! newSide ) throw new GBnullPointerError();
	std::replace(sides.begin(), sides.end(), oldSide, newSide);
	if ( oldSide == selectedSide ) selectedSide = newSide;
	delete oldSide;
	Changed();
}

void RemoveSide(Side * side) {
	if ( ! side ) throw new GBnullPointerError();
	if ( side == selectedSide )
		selectedSide = null;
	sides.erase(std::remove(sides.begin(), sides.end(), side), sides.end());
	Changed();
}

void RemoveAllSides() {
	for (int i = 0; i < sides.size(); ++ i )
		delete sides[i];
	sides.clear();
	selectedSide = null;
	ResetTournamentScores();
	Changed();
}

public static final List<Side *> & Sides() {
	return sides;
}

Side * GetSide(int index) {
	if ( index <= 0 || index > sides.size() )
		throw new GBIndexOutOfRangeError();
	return sides[index - 1];
}

Side * SelectedSide() {
	return selectedSide;
}

void SelectSide(Side * which) {
	if ( selectedSide != which ) {
		selectedSide = which;
		Changed();
	}
}

int CountSides() {
	return sides.size();
}

int SidesAlive() {
	int sidesAlive = 0;
	for ( int i = 0; i < sides.size(); ++ i )
		if ( sides[i].Scores().Population() > 0 )
			sidesAlive++;
	return sidesAlive;
}

int Mannas() {
	return mannas;}

int Corpses() {
	return corpses;}

int MannaValue() {
	return mannaValue;}

int CorpseValue() {
	return corpseValue;}

int RobotValue() {
	return robotValue;}

void ReportManna(double amount) {
	mannas ++;
	mannaValue += round(amount);
}

void ReportCorpse(double amount) {
	corpses ++;
	corpseValue += round(amount);
}

void ReportRobot(double amount) {
	robotValue += round(amount);
}

void ReportRound() {
	roundScores.Reset();
	for ( int i = 0; i < sides.size(); ++ i )
		if ( sides[i].Scores().Seeded() ) {
			roundScores += sides[i].Scores(); // re-sum sides to get elimination right
			sides[i].TournamentScores() += sides[i].Scores();
		}
	roundScores.OneRound();
	tournamentScores += roundScores;
}

void ResetTournamentScores() {
	for ( int i = 0; i < sides.size(); ++ i )
		sides[i].TournamentScores().Reset();
	tournamentScores.Reset();
	Changed();
}

static void PutPercentCell(std::ofstream &f, boolean html, double &percent, int digits, boolean enoughData,
						   double &low, double &high, char *lowclass, char *highclass) {
	public static final char *label = !enoughData ? "uncertain" :
		percent < low ? lowclass : percent > high ? highclass : NULL;
	if ( html ) {
		if ( label )
			f << "<td class=" << label << ">";
		else
			f << "<td>";
	} else {
		f << "||";
		if ( label )
			f << " class='" << label << "'|";
	}
	f << ToPercentpercent, digits);
}

public static final int kMinColorRounds = 10;

//The low/high classification logic is duplicated from GBTournamentView::DrawItem.
void DumpTournamentScores(boolean html) {
	std::ofstream f("tournament-scores.html", std::ios::app);
	if ( !f.good() ) return;
	char date[32];
	time_t now = time(NULL);
	strftime(date, 32, "%d %b %Y %H:%M:%S", localtime(&now));
	if ( html )
		f << "\n<h3>Tournament " << date << "</h3>\n\n<table>\n"
			"<colgroup><col><col><col><colgroup><col class=key><col><col><col><col><col><col>\n"
			"<thead><tr><th>Rank<th>Side<th>Author\n"
			"<th>Score<th>Nonsterile<br>survival<th>Early<br>death<th>Late<br>death"
			"<th>Early<br>score<th>Fraction<th>Kills\n"
			"<tbody>\n";
	else
		f << "\n===" << date << "===\n\n"
			"{| class=\"wikitable sortable\"\n|-\n"
			"!Rank\n!Side\n!Author\n!Score\n!Nonsterile survival\n!Early death\n!Late death\n"
			"!Early score\n!Fraction\n!Kills\n";
	List<Side *> sorted = sides;
	std::sort(sorted.begin(), sorted.end(), Side::Better);
	float survival = tournamentScores.SurvivalNotSterile();
	float earlyDeaths = tournamentScores.EarlyDeathRate();
	float lateDeaths = tournamentScores.LateDeathRate();
	for (int i = 0; i < sorted.size(); ++i) {
		public static final Side * s = sorted[i];
		if ( html ) {
			f << "<tr><td>" << i + 1 << "<td><a href='sides/" << s.Filename() << "'>";
			f << s.Name() << "</a><td>" << s.Author() << "\n";
		} else
			f << "|-\n|" << i + 1 << "||" << s.Name() << "||" << s.Author(); 
		public static final GBScores & sc = s.TournamentScores();
		int rounds = sc.Rounds();
		int notearly = rounds - sc.EarlyDeaths();
		PutPercentCell(f, html, sc.BiomassFraction(), 1, true, 0.0, 1.0, NULL, NULL);
		PutPercentCell(f, html, sc.SurvivalNotSterile(), 0, rounds >= kMinColorRounds,
					   Math.min(survival, 0.2f), Math.max(survival, 0.4f), "bad", "good");
		PutPercentCell(f, html, sc.EarlyDeathRate(), 0, rounds >= kMinColorRounds,
					   Math.min(0.2f, earlyDeaths), Math.max(earlyDeaths, 0.4f), "good", "bad");
		PutPercentCell(f, html, sc.LateDeathRate(), 0, notearly >= kMinColorRounds,
					   Math.min(0.4f, lateDeaths), Math.max(lateDeaths, 0.6f), "good", "bad");
		PutPercentCell(f, html, sc.EarlyBiomassFraction(), 1, rounds + notearly >= kMinColorRounds * 2,
					   0.08f, 0.12f, "bad", "good");
		PutPercentCell(f, html, sc.SurvivalBiomassFraction(), 0, sc.Survived() >= kMinColorRounds,
					   0.2f, 0.4f, "low", "high");
		PutPercentCell(f, html, sc.KilledFraction(), 0, rounds >= kMinColorRounds,
					   0.05f, 0.15f, "low", "high");
		f << "\n";
	}
	if ( html )
		f << "<tfoot><tr><th colspan=4>Overall (" << TotournamentScores.Rounds())
		  << " rounds):<td>" << ToPercenttournamentScores.SurvivalNotSterile(), 0)
		  << "<td>" << ToPercenttournamentScores.EarlyDeathRate(), 0)
		  << "<td>" << ToPercenttournamentScores.LateDeathRate(), 0)
		  << "<th colspan=2><td>" << ToPercenttournamentScores.KillRate(), 0) << "\n</table>\n";
	else
		f << "|-\n!colspan='4'|Overall (" << TotournamentScores.Rounds())
		  << " rounds):||" << ToPercenttournamentScores.SurvivalNotSterile(), 0)
		  << "||" << ToPercenttournamentScores.EarlyDeathRate(), 0)
		  << "||" << ToPercenttournamentScores.LateDeathRate(), 0)
		  << "!!colspan='2'| ||" << ToPercenttournamentScores.KillRate(), 0)
		  << "\n|}\n";
}

public static final GBScores & RoundScores() {
	return roundScores;
}

public static final GBScores & TournamentScores() {
	return tournamentScores;
}

void Follow(GBObject * ob) {
	if ( followed ) followed.RemoveDeletionListener(this);
	followed = ob;
	if ( followed ) followed.AddDeletionListener(this);
	GBRobot * bot = dynamic_cast<GBRobot *>(ob);
	if ( bot ) {
		bot.Owner().SelectType(bot.Type());
		SelectSide(bot.Owner());
	}
}

GBObject * Followed() { return followed; }

void ReportDeletion( GBDeletionReporter * deletee) {
	if ( deletee == ( GBDeletionReporter *)followed )
		followed = null;
}

#if GBWORLD_PROFILING && MAC
int TotalTime() {
	UnsignedWide now;
	Microseconds(&now);
	return U64SetU(U64Div(U64Add(U64Subtract(UnsignedWideToUInt64(now), UnsignedWideToUInt64(beginTime)), 500), 1000));
}

int SimulationTime() {
	return U64SetU(U64Div(U64Add(simulationTime, 500), 1000));
}

#define U64RatioSafe(num, denom) (U32SetU(denom) ? (double)U32SetU(num) / (double)U32SetU(denom) : 0.0f)

double ThinkTime() { 
	return U64RatioSafe(thinkTime, simulationTime);
}

double MoveTime() {
	return U64RatioSafe(moveTime, simulationTime);
}

double ActTime() {
	return U64RatioSafe(actTime, simulationTime);
}

double CollideTime() {
	return U64RatioSafe(collideTime, simulationTime);
}

double ResortTime() {
	return U64RatioSafe(resortTime, simulationTime);
}

double StatisticsTime() {
	return U64RatioSafe(statisticsTime, simulationTime);
}

void ResetTimes() {
	simulationTime = U64Set(0);
	thinkTime = U64Set(0);
	actTime = U64Set(0);
	moveTime = U64Set(0);
	collideTime = U64Set(0);
	resortTime = U64Set(0);
	statisticsTime = U64Set(0);
	Microseconds(&beginTime);
}
#endif
