// GBWorld.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBWorld.h"
#include "GBFood.h"
#include "GBShot.h"
#include "GBRobot.h"
#include "GBErrors.h"
#include "GBSide.h"
#include "GBRobotType.h"
#include "GBSound.h"
#include "GBStringUtilities.h"
#include <fstream>
#include <vector>
#include <algorithm>

#if MAC && ! MAC_OS_X
#include <Timer.h>
#include <Math64.h>
#endif

#if WINDOWS && _MSC_VER < 1300
//this seems to be missing in MSVC6
ostream & operator << (ostream & s, const string & str) {
	return s << str.c_str();
}
#endif

const GBEnergy kDefaultMannaDensity = 150; // energy / tile
const GBNumber kDefaultMannaRate = 0.25; // energy / tile / frame
const GBEnergy kDefaultMannaSize = 400;

const short kDefaultTimeLimit = 18000;

const GBDistance kSeedRadius = 2;
const GBEnergy kDefaultSeedValue = 5000;
const GBEnergy kDefaultSeedTypePenalty = 100;


#if GBWORLD_PROFILING && MAC
	#define PROFILE_PHASE(var, code) \
		Microseconds(&phaseStart); \
		code \
		Microseconds(&end); \
		var = U64Add(U64Subtract(UnsignedWideToUInt64(end), UnsignedWideToUInt64(phaseStart)), var);
#else
	#define PROFILE_PHASE(var, code) code
#endif


void GBWorld::ThinkAllObjects() {
	//only bothers with robots
	try {
		for ( long i = 0; i <= tilesX * tilesY; i ++ )
			for ( GBObject * ob = objects[i][ocRobot]; ob != nil; ob = ob->next )
				ob->Think(this);
	} catch ( GBError & err ) {
		NonfatalError(string("Error thinking object: ") + err.ToString());
	}
}

void GBWorld::ActAllObjects() {
	try {
		for ( long i = 0; i <= tilesX * tilesY; i ++ )
			for ( GBObjectClass cur = ocRobot; cur < kNumObjectClasses; cur ++ )
				for ( GBObject * ob = objects[i][cur]; ob != nil; ob = ob->next )
					ob->Act(this);
	} catch ( GBError & err ) {
		NonfatalError(string("Error acting object: ") + err.ToString());
	}
}

void GBWorld::AddManna() {
	try {
		for ( mannaLeft += size.x * size.y * mannaRate / (kForegroundTileSize * kForegroundTileSize);
				mannaLeft > mannaSize; mannaLeft -= mannaSize )
			AddObjectDirectly(new GBManna(RandomLocation(), mannaSize));
	} catch ( GBError & err ) {
		NonfatalError(string("Error adding manna: ") + err.ToString());
	}
}

void GBWorld::PickSeedPositions(GBPosition * positions, long numSeeds) {
	if ( ! positions) throw GBNilPointerError();
	if ( numSeeds < 1 ) return;
	try {
		GBDistance wallDist = kSeedRadius + min(size.x, size.y) / 20;
		GBDistance separation = sqrt((size.x - wallDist * 2) * (size.y - wallDist * 2) / numSeeds);
		int iterations = 0;
		int iterLimit = 100 + 30 * numSeeds + numSeeds * numSeeds;
		bool inRange;
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
				if ( ++ iterations > iterLimit ) throw GBTooManyIterationsError();
			} while ( inRange ) ;
		}
		if ( reportErrors && iterations > iterLimit / 2 )
			NonfatalError("Warning: seed placement took " + ToString(iterations) + " iterations");
	// shuffle positions
		//the above algorithm is not uniform, in that the first element may have different typical location than the last
		//to fix this, permute randomly (Knuth Vol 2 page 125)
		for ( long j = numSeeds - 1; j > 0; j-- ) {  //iteration with j==0 is nop, so skip it
			long i = Randoms().LongInRange(0, j);
			GBPosition temp = positions[i];
			positions[i] = positions[j];
			positions[j] = temp;
		}
	} catch ( GBTooManyIterationsError & ) {
		if ( reportErrors ) NonfatalError("Warning: GBWorld::PickSeedPositions failsafe used.");
		for ( int i = 0; i < numSeeds; ++ i )
			positions[i] = RandomLocation();
	}
}

void GBWorld::AddInitialManna() {
	GBEnergy amount = size.x * size.y / (kForegroundTileSize * kForegroundTileSize) * mannaDensity;
	GBEnergy placed;
	for ( ; amount > 0; amount -= placed ) {
		placed = amount > mannaSize ? Randoms().InRange(mannaSize / 10, mannaSize) : amount;
		AddObjectDirectly(new GBManna(RandomLocation(), placed));
	}
}

GBWorld::GBWorld()
	: GBObjectWorld(),
	sides(nil), selectedSide(nil),
	roundScores(), tournamentScores(),
	currentFrame(0),
	followed(nil),
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

GBWorld::~GBWorld() {
	RemoveAllSides();
}

void GBWorld::SimulateOneFrame() {
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

void GBWorld::AdvanceFrame() {
	SimulateOneFrame();
	if (RoundOver())
		EndRound();
}

void GBWorld::EndRound() {
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

void GBWorld::CollectStatistics() {
// reset
	mannas = 0; corpses = 0;
	mannaValue = 0; corpseValue = 0; robotValue = 0;
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		cur->ResetSampledStatistics();
// collect
	try {
		for ( long i = 0; i <= tilesX * tilesY; i ++ ) {
		// robots and territory
			GBSide * side = nil;
			bool exclusive = true;
			for ( GBObject * robot = objects[i][ocRobot]; robot != nil; robot = robot->next ) {
				robot->CollectStatistics(this);
				if ( exclusive ) {
					if (! side ) side = robot->Owner();
					else if ( side != robot->Owner() ) exclusive = false;
				}
			}
			if ( side && exclusive && i != tilesX * tilesY ) side->Scores().ReportTerritory();
		// other classes
			for ( GBObjectClass cur = ocFood; cur < kNumObjectClasses; cur ++ )
				for ( GBObject * ob = objects[i][cur]; ob != nil; ob = ob->next )
					ob->CollectStatistics(this);
		}
	} catch ( GBError & err ) {
		NonfatalError(string("Error collecting statistics: ") + err.ToString());
	}
// report
	roundScores.Reset();
	GBSide * side;
	for ( side = sides; side != nil; side = side->next ) {
		side->Scores().ReportFrame(currentFrame);
		roundScores += side->Scores();
	}
	roundScores.OneRound();
	for ( side = sides; side != nil; side = side->next )
		side->Scores().ReportTotals(roundScores);
}

void GBWorld::AddSeed(GBSide * side, const GBPosition & where) {
	try {
		GBEnergy cost = seedValue - seedTypePenalty * side->CountTypes();
	//give side a number
		if ( ! side->ID() )
			side->SetID(++sidesSeeded);
	//add cells
		GBRobotType * type;
		GBRobot * bot = nil;
		std::vector<GBRobot *> placed;
		int lastPlaced = -1; //last value of i for last successful place
		for (int i = 0; ; i++) {
			type = side->GetSeedType(i);
			if ( ! type )
				throw GBGenericError("must have at least one type to seed");
			if ( type->Cost() <= cost) {
				bot = new GBRobot(type, where + random.Vector(kSeedRadius));
				AddObjectDirectly(bot);
				side->Scores().ReportSeeded(type->Cost());
				cost -= type->Cost();
				lastPlaced = i;
				placed.push_back(bot);
			} else
				break; //if unseedable, stop - this one will be a fetus
			//Old version: keep trying until we've gone through list once
				//without placing anything
			//if (i - lastPlaced >= side->NumSeedTypes())
			//	break;
		}
	// give excess energy as construction
		int placedIndex;
		for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
			GBRobot * placee = placed[placedIndex];
			if (cost == 0) break;
			if ( placee->hardware.constructor.MaxRate() ) {
				GBEnergy amt = min(cost, side->GetSeedType(lastPlaced + 1)->Cost());
				placee->hardware.constructor.Start(side->GetSeedType(lastPlaced + 1), amt);
				side->Scores().ReportSeeded(amt);
				cost -= amt;
				if ( cost > 0 )
					throw GBGenericError("When seeding, energy left-over after bonus fetus");
			}
		}
	//energy still left (implies constructor-less side!); try giving as energy
		for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
			if (cost == 0) break;
			GBEnergy amt = placed[placedIndex]->hardware.GiveEnergy(cost);
			side->Scores().ReportSeeded(amt);
			cost -= amt;
		}
	//all else fails, make a manna.
		if ( cost > 0 )
			AddObjectDirectly(new GBManna(where, cost)); // no ReportSeeded because it's pretty worthless

	} catch ( GBError & err ) {
		NonfatalError(string("Error adding seed:") + err.ToString());
	}
}

void GBWorld::AddSeeds() {
	long numSides = CountSides();
	long numSeeds = seedLimit ? (seedLimit > numSides ? numSides : seedLimit) : numSides;
// pick positions
	GBPosition * positions = new GBPosition[numSeeds];
	if ( ! positions ) throw GBOutOfMemoryError();
	PickSeedPositions(positions, numSeeds);
// seed sides
	long seedsLeft = numSeeds;
	long sidesLeft = numSides;
	for ( GBSide * side = sides; side != nil && seedsLeft; side = side->next, -- sidesLeft )
		if ( seedsLeft >= sidesLeft || random.Boolean(GBNumber(seedsLeft) / sidesLeft) ) {
			if ( ! seedsLeft ) throw GBTooManyIterationsError();
			side->center = positions[numSeeds - seedsLeft];
			AddSeed(side, positions[numSeeds - seedsLeft]);
			-- seedsLeft;
		}
	delete[] positions;
	if ( seedsLeft ) throw GBSimulationError();
	CollectStatistics();
	Changed();
}

void GBWorld::ReseedDeadSides() {
// since this uses side statistics, be sure statistics have been gathered
	for ( GBSide * side = sides; side != nil; side = side->next )
		if ( side->Scores().Sterile() ) {
			//side->Reset(); //why?
			AddSeed(side, RandomLocation());
		}
	CollectStatistics();
}

void GBWorld::Reset() {
	currentFrame = 0;
	mannaLeft = 0;
	sidesSeeded = 0;
	ClearLists();
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		cur->Reset();
	roundScores.Reset();
	AddInitialManna();
	Changed();
}

void GBWorld::Resize(const GBFinePoint & newsize) {
	if ( newsize == size ) return;
	GBObjectWorld::Resize(newsize);
	Reset();
}

GBFrames GBWorld::CurrentFrame() const {
	return currentFrame;
}

bool GBWorld::RoundOver() const {
	return stopOnElimination && previousSidesAlive > SidesAlive() && SidesAlive() <= 1
		|| timeLimit > 0 && CurrentFrame() % timeLimit == 0;
}

GBRandomState & GBWorld::Randoms() {
	return random;
}

GBFinePoint GBWorld::RandomLocation(GBDistance walldist) {
	return GBFinePoint(random.InRange(walldist, size.x - walldist),
						random.InRange(walldist, size.y - walldist));
}

void GBWorld::AddSide(GBSide * side) {
	if ( ! side )
		throw GBNilPointerError();
	side->next = nil;
	if ( sides == nil ) {
		sides = side;
	} else {
		GBSide * cur;
		for ( cur = sides; cur->next != nil; cur = cur->next )
			;
		cur->next = side;
	}
	Changed();
}

void GBWorld::ReplaceSide(GBSide * oldSide, GBSide * newSide) {
	if ( ! oldSide || ! newSide ) throw GBNilPointerError();
	if ( sides == oldSide ) {
		sides = newSide;
	} else {
		GBSide * cur;
		for ( cur = sides; cur != nil; cur = cur->next ) {
			if ( cur->next == oldSide ) {
				cur->next = newSide;
				break;
			}
		}
		if ( ! cur ) throw GBBadArgumentError();
	}
	if ( oldSide == selectedSide ) selectedSide = newSide;
	newSide->next = oldSide->next;
	delete oldSide;
	Changed();
}

void GBWorld::RemoveSide(GBSide * side) {
	if ( ! side ) throw GBNilPointerError();
	if ( side == selectedSide )
		selectedSide = nil;
	if ( sides == side ) {
		sides = side->next;
		delete side;
	} else {
		for ( GBSide * cur = sides; cur != nil; cur = cur->next ) {
			if ( cur->next == side ) {
				cur->next = cur->next->next;
				delete side;
			}
		}
	}
	Changed();
}

void GBWorld::RemoveAllSides() {
	GBSide * temp;
	for ( GBSide * cur = sides; cur != nil; cur = temp ) {
		temp = cur->next;
		delete cur;
	}
	sides = nil;
	selectedSide = nil;
	ResetTournamentScores();
	Changed();
}

GBSide * GBWorld::Sides() const {
	return sides;
}

GBSide * GBWorld::GetSide(long index) const {
	GBSide * side = sides;
	if ( index <= 0 ) throw GBIndexOutOfRangeError();
	for ( long i = 1; i < index; i ++ )
		if ( side->next )
			side = side->next;
		else
			throw GBIndexOutOfRangeError();
	return side;
}

GBSide * GBWorld::SelectedSide() const {
	return selectedSide;
}

void GBWorld::SelectSide(GBSide * which) {
	if ( selectedSide != which ) {
		selectedSide = which;
		Changed();
	}
}

long GBWorld::CountSides() const {
	long count = 0;
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		count ++;
	return count;
}

int GBWorld::SidesAlive() const {
	int sidesAlive = 0;
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		if ( cur->Scores().Population() > 0 )
			sidesAlive++;
	return sidesAlive;
}

int GBWorld::Mannas() const {
	return mannas;}

int GBWorld::Corpses() const {
	return corpses;}

long GBWorld::MannaValue() const {
	return mannaValue;}

long GBWorld::CorpseValue() const {
	return corpseValue;}

long GBWorld::RobotValue() const {
	return robotValue;}

void GBWorld::ReportManna(GBEnergy amount) {
	mannas ++;
	mannaValue += round(amount);
}

void GBWorld::ReportCorpse(GBEnergy amount) {
	corpses ++;
	corpseValue += round(amount);
}

void GBWorld::ReportRobot(GBEnergy amount) {
	robotValue += round(amount);
}

void GBWorld::ReportRound() {
	roundScores.Reset();
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		if ( cur->Scores().Seeded() ) {
			roundScores += cur->Scores(); // re-sum sides to get elimination right
			cur->TournamentScores() += cur->Scores();
		}
	roundScores.OneRound();
	tournamentScores += roundScores;
}

void GBWorld::ResetTournamentScores() {
	for ( GBSide * cur = sides; cur != nil; cur = cur->next )
		cur->TournamentScores().Reset();
	tournamentScores.Reset();
	Changed();
}

void GBWorld::DumpTournamentScores() {
	std::ofstream f("tournament-scores.html", std::ios::app);
	if ( !f.good() ) return;
	f << "\n<table>\n"
		"<colgroup><col><col><col><colgroup><col class=key><col><col><col><col><col><col>\n"
		"<thead><tr><th>Rank<th>Side<th>Author\n"
		"<th>Score<th>Nonsterile<br>survival<th>Early<br>death<th>Late<br>death"
		"<th>Early<br>score<th>Fraction<th>Kills\n"
		"<tbody>\n";
	std::vector<const GBSide *> sorted;
	for (const GBSide * s = sides; s; s = s->next)
		sorted.push_back(s);
	std::sort(sorted.begin(), sorted.end(), GBSide::Better);
	for (int i = 0; i < sorted.size(); ++i) {
		f << "<tr><td>" << i + 1 << "<td>";
		f << sorted[i]->Name() << "<td>" << sorted[i]->Author() << "\n";
		const GBScores & sc = sorted[i]->TournamentScores();
		f << "<td>" << ToPercentString(sc.BiomassFraction(), 1);
		f << "<td>" << ToPercentString(sc.SurvivalNotSterile(), 0);
		f << "<td>" << ToPercentString(sc.EarlyDeathRate(), 0);
		f << "<td>" << ToPercentString(sc.LateDeathRate(), 0);
		f << "<td>" << ToPercentString(sc.EarlyBiomassFraction(), 1);
		f << "<td>" << ToPercentString(sc.SurvivalBiomassFraction(), 0);
		f << "<td>" << ToPercentString(sc.KilledFraction(), 0) << "\n";
	}
	f << "<tfoot><tr><th colspan=4>Overall:<td>" << ToPercentString(tournamentScores.SurvivalNotSterile(), 0)
	  << "<td>" << ToPercentString(tournamentScores.EarlyDeathRate(), 0)
	  << "<td>" << ToPercentString(tournamentScores.LateDeathRate(), 0)
	  << "<th colspan=2><td>" << ToPercentString(tournamentScores.KillRate(), 0) << "\n</table>\n";
}

const GBScores & GBWorld::RoundScores() const {
	return roundScores;
}

const GBScores & GBWorld::TournamentScores() const {
	return tournamentScores;
}

void GBWorld::Follow(GBObject * ob) {
	if ( followed ) followed->RemoveDeletionListener(this);
	followed = ob;
	if ( followed ) followed->AddDeletionListener(this);
	GBRobot * bot = dynamic_cast<GBRobot *>(ob);
	if ( bot ) {
		bot->Owner()->SelectType(bot->Type());
		SelectSide(bot->Owner());
	}
}

GBObject * GBWorld::Followed() const { return followed; }

void GBWorld::ReportDeletion(const GBDeletionReporter * deletee) {
	if ( deletee == (const GBDeletionReporter *)followed )
		followed = nil;
}

#if GBWORLD_PROFILING && MAC
long GBWorld::TotalTime() const {
	UnsignedWide now;
	Microseconds(&now);
	return U64SetU(U64Div(U64Add(U64Subtract(UnsignedWideToUInt64(now), UnsignedWideToUInt64(beginTime)), 500), 1000));
}

long GBWorld::SimulationTime() const {
	return U64SetU(U64Div(U64Add(simulationTime, 500), 1000));
}

long GBWorld::ThinkTime() const {
	return U64SetU(U64Div(U64Add(thinkTime, 500), 1000));
}

long GBWorld::MoveTime() const {
	return U64SetU(U64Div(U64Add(moveTime, 500), 1000));
}

long GBWorld::ActTime() const {
	return U64SetU(U64Div(U64Add(actTime, 500), 1000));
}

long GBWorld::CollideTime() const {
	return U64SetU(U64Div(U64Add(collideTime, 500), 1000));
}

long GBWorld::ResortTime() const {
	return U64SetU(U64Div(U64Add(resortTime, 500), 1000));
}

long GBWorld::StatisticsTime() const {
	return U64SetU(U64Div(U64Add(statisticsTime, 500), 1000));
}

void GBWorld::ResetTimes() {
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
