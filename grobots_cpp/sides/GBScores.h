// GBScores.h
// various scoring classes
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBScores_h
#define GBScores_h

#include "GBTypes.h"
#include "GBLongNumber.h"
#include <vector>

class GBIncomeStatistics {
	GBLongNumber autotrophy;
	GBLongNumber theotrophy;
	GBLongNumber heterotrophy;
	GBLongNumber cannibalism;
	GBLongNumber kleptotrophy;
public:
	GBIncomeStatistics();
	~GBIncomeStatistics();
// reports
	void Reset();
	void ReportAutotrophy(const GBEnergy en);
	void ReportTheotrophy(const GBEnergy en);
	void ReportHeterotrophy(const GBEnergy en);
	void ReportCannibalism(const GBEnergy en);
	void ReportKleptotrophy(const GBEnergy en);
// accessors
	long Autotrophy() const;
	long Theotrophy() const;
	long Heterotrophy() const;
	long Cannibalism() const;
	long Kleptotrophy() const;
	long Total() const;
// combining
	GBIncomeStatistics & operator +=(const GBIncomeStatistics & other);
};

class GBExpenditureStatistics {
	GBLongNumber construction;
	GBLongNumber engine;
	GBLongNumber weapons;
	GBLongNumber forceField;
	GBLongNumber shield;
	GBLongNumber repairs;
	GBLongNumber sensors;
	GBLongNumber brain;
	GBLongNumber stolen;
	GBLongNumber wasted;
public:
	GBExpenditureStatistics();
	~GBExpenditureStatistics();
// reports
	void Reset();
	void ReportConstruction(const GBEnergy en);
	void ReportEngine(const GBEnergy en);
	void ReportForceField(const GBEnergy en);
	void ReportWeapons(const GBEnergy en);
	void ReportShield(const GBEnergy en);
	void ReportRepairs(const GBEnergy en);
	void ReportSensors(const GBEnergy en);
	void ReportBrain(const GBEnergy en);
	void ReportStolen(const GBEnergy en);
	void ReportWasted(const GBEnergy en);
// accessors
	long Construction() const;
	long Engine() const;
	long Weapons() const;
	long ForceField() const;
	long Shield() const;
	long Repairs() const;
	long Sensors() const;
	long Brain() const;
	long Stolen() const;
	long Wasted() const;
	long Total() const;
// combining
	GBExpenditureStatistics & operator +=(const GBExpenditureStatistics & other);
};

class GBScores {
protected:
//rounds
	long sides; // = number of seeds
	long rounds;
	long survived;
	long sterile;
	long earlyDeaths;
	long elimination;
// sampled
	long population, populationEver;
	GBLongNumber biomass, earlyBiomass;
	std::vector<long> biomassHistory; //may eventually be a vector of GBScores
	GBLongNumber constructor;
	long territory;
// accumulated
	GBLongNumber seeded;
	GBIncomeStatistics income;
	GBExpenditureStatistics expenditure;
	GBLongNumber dead, killed, suicide;
// fractions
	float biomassFraction, earlyBiomassFraction;
	float killedFraction;
	float biomassFractionSquared; //for standard deviations
public:
	GBScores();
	~GBScores();
	void Reset();
	void OneRound();
	GBScores & operator+=(const GBScores & other);
// basics
	long Sides() const;
	long Rounds() const;
	long Survived() const;
	float Survival() const;
	long Sterile() const;
	float SurvivalNotSterile() const;
	long EarlyDeaths() const;
	float EarlyDeathRate() const;
	long SurvivedEarly() const;
	float LateDeathRate() const;
	long Elimination() const;
	float EliminationRate() const;
// samples
	long Population() const;
	long PopulationEver() const;
	long Biomass() const;
	long EarlyBiomass() const;
	long SurvivalBiomass() const;
	long EarlySurvivalBiomass() const;
	float BiomassFraction() const;
	float EarlyBiomassFraction() const;
	float SurvivalBiomassFraction() const;
	const std::vector<long> BiomassHistory() const;
	long Constructor() const;
	long Territory() const;
// cumulative
	long Seeded() const;
	const GBIncomeStatistics & Income() const;
	GBIncomeStatistics & Income();
	const GBExpenditureStatistics & Expenditure() const;
	GBExpenditureStatistics & Expenditure();
	long Dead() const;
	long Killed() const;
	long Suicide() const;
	float KilledFraction() const;
	float KillRate() const;
// oddballs
	float Efficiency() const; // remove?
	GBFrames Doubletime(GBFrames currentTime) const;
	float BiomassFractionSD() const;
	float BiomassFractionError() const;
};

class GBSideScores : public GBScores {
	GBFrames extinctTime, sterileTime;
public:
	GBSideScores();
	~GBSideScores();
	void Reset();
// reporting
	void ReportTotals(const GBScores & totals);
	void ReportSeeded(const GBEnergy en);
	void ReportFrame(const GBFrames frame);
	void ResetSampledStatistics();
	void ReportRobot(const GBEnergy en, const GBPower construc);
	void ReportDead(const GBEnergy en);
	void ReportKilled(const GBEnergy en);
	void ReportSuicide(const GBEnergy en);
	void ReportTerritory();
// accessors
	GBFrames ExtinctTime() const;
	GBFrames SterileTime() const;
// counter
	long GetNewRobotNumber();
};

#endif
