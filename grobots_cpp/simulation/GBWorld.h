// GBWorld.h
// the slightly overstuffed main simulation class.
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBWorld_h
#define GBWorld_h

#include "GBRandomState.h"
#include "GBModel.h"
#include "GBObjectWorld.h"
#include "GBScores.h"

#if MAC && ! HEADLESS
#define GBWORLD_PROFILING 1
#else
#define GBWORLD_PROFILING 0
#endif

const GBDistance kRandomMinWallDistance = 2;

class GBWorld : public GBObjectWorld, public GBModel, public GBDeletionListener {
	GBSide * sides;
	GBSide * selectedSide;
	GBFrames currentFrame;
	int previousSidesAlive; //num of non-extinct sides last frame
	int sidesSeeded;
	GBRandomState random;
	GBObject * followed;
	GBEnergy mannaLeft;
// stats
	int mannas, corpses;
	long mannaValue, corpseValue, robotValue;
	GBScores roundScores;
	GBScores tournamentScores;
// timing
#if GBWORLD_PROFILING && MAC
	UInt64 simulationTime, moveTime, collideTime, thinkTime, actTime, resortTime, statisticsTime;
	UnsignedWide beginTime;
#endif
public:
// operation and tournament
	bool running; // here so PAUSE primitive can write it :(
	GBFrames timeLimit;
	bool stopOnElimination;
	bool tournament;
	long tournamentLength;
	bool reportErrors, reportPrints;
// simulation parameters
	long seedLimit;
	bool autoReseed;
	GBEnergy mannaSize;
	GBNumber mannaDensity;
	GBNumber mannaRate;
	GBEnergy seedValue;
	GBEnergy seedTypePenalty;
private:
	void ThinkAllObjects();
	void ActAllObjects();
	void AddManna();
	void AddInitialManna();
	void PickSeedPositions(GBPosition * positions, long numSeeds);
public:
// constructors
	GBWorld();
	~GBWorld();
// operation
	void SimulateOneFrame();
	void AdvanceFrame();
	void EndRound();
	void CollectStatistics();
	void AddSeed(GBSide * side, const GBPosition & where);
	void AddSeeds();
	void ReseedDeadSides();
// control
	void Reset();
	void Resize(const GBFinePoint & newsize);
// accessors
	GBFrames CurrentFrame() const;
	bool RoundOver() const;
// randoms
	GBRandomState & Randoms();
	GBFinePoint RandomLocation(GBDistance walldist = kRandomMinWallDistance);
// adding/removing sides
	void AddSide(GBSide * side);
	void ReplaceSide(GBSide * oldSide, GBSide * newSide);
	void RemoveSide(GBSide * side);
	void RemoveAllSides();
// sides
	GBSide * Sides() const;
	GBSide * GetSide(long index) const;
// selected side
	GBSide * SelectedSide() const;
	void SelectSide(GBSide * which);
// stats
	long CountSides() const;
	int SidesAlive() const;
	int Mannas() const;
	int Corpses() const;
	long MannaValue() const;
	long CorpseValue() const;
	long RobotValue() const;
	void ReportManna(GBEnergy amount);
	void ReportCorpse(GBEnergy amount);
	void ReportRobot(GBEnergy amount);
	void ReportRound();
	void ResetTournamentScores();
	void DumpTournamentScores();
	const GBScores & RoundScores() const;
	const GBScores & TournamentScores() const;
// selected object
	void Follow(GBObject * ob);
	GBObject * Followed() const;
	void ReportDeletion(const GBDeletionReporter * rep);
// timing
#if GBWORLD_PROFILING
	long TotalTime() const;
	long SimulationTime() const;
	long ThinkTime() const;
	long MoveTime() const;
	long ActTime() const;
	long CollideTime() const;
	long ResortTime() const;
	long StatisticsTime() const;
	void ResetTimes();
#endif
};

#endif
