// GBScores.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBScores.h"
#include <math.h>

const GBFrames kEarlyDeathTime = 4500;
const long kMaxSterileConstructor = 10;

// GBIncomeStatistics //

GBIncomeStatistics::GBIncomeStatistics()
	: autotrophy(0),
	theotrophy(0),
	heterotrophy(0),
	cannibalism(0),
	kleptotrophy(0)
{}

GBIncomeStatistics::~GBIncomeStatistics() {}

void GBIncomeStatistics::Reset() {
	autotrophy = 0;
	theotrophy = 0;
	heterotrophy = 0;
	cannibalism = 0;
	kleptotrophy = 0;
}

void GBIncomeStatistics::ReportAutotrophy(const GBEnergy en) { autotrophy += en;}
void GBIncomeStatistics::ReportTheotrophy(const GBEnergy en) { theotrophy += en;}
void GBIncomeStatistics::ReportHeterotrophy(const GBEnergy en) { heterotrophy += en;}
void GBIncomeStatistics::ReportCannibalism(const GBEnergy en) { cannibalism += en;}
void GBIncomeStatistics::ReportKleptotrophy(const GBEnergy en) { kleptotrophy += en;}

long GBIncomeStatistics::Autotrophy() const { return autotrophy.Round(); }
long GBIncomeStatistics::Theotrophy() const { return theotrophy.Round(); }
long GBIncomeStatistics::Heterotrophy() const { return heterotrophy.Round(); }
long GBIncomeStatistics::Cannibalism() const { return cannibalism.Round(); }
long GBIncomeStatistics::Kleptotrophy() const { return kleptotrophy.Round(); }

long GBIncomeStatistics::Total() const {
	// excludes cannibalism and seeded
	return Autotrophy() + Theotrophy() + Heterotrophy() + Kleptotrophy();
}

GBIncomeStatistics & GBIncomeStatistics::operator +=(const GBIncomeStatistics & other) {
	autotrophy += other.autotrophy;
	theotrophy += other.theotrophy;
	heterotrophy += other.heterotrophy;
	cannibalism += other.cannibalism;
	kleptotrophy += other.kleptotrophy;
	return *this;
}

// GBExpenditureStatistics //

GBExpenditureStatistics::GBExpenditureStatistics()
	: construction(0), engine(0),
	weapons(0), forceField(0), shield(0), repairs(0),
	sensors(0), brain(0), stolen(0), wasted(0)
{}

GBExpenditureStatistics::~GBExpenditureStatistics() {}

void GBExpenditureStatistics::Reset() {
	construction = 0;
	engine = 0;
	weapons = 0; forceField = 0;
	shield = 0; repairs = 0;
	sensors = 0;
	brain = 0;
	stolen = 0; wasted = 0;
}

void GBExpenditureStatistics::ReportConstruction(const GBEnergy en) { construction += en;}
void GBExpenditureStatistics::ReportEngine(const GBEnergy en) { engine += en;}
void GBExpenditureStatistics::ReportForceField(const GBEnergy en) { forceField += en;}
void GBExpenditureStatistics::ReportWeapons(const GBEnergy en) { weapons += en;}
void GBExpenditureStatistics::ReportShield(const GBEnergy en) { shield += en;}
void GBExpenditureStatistics::ReportRepairs(const GBEnergy en) { repairs += en;}
void GBExpenditureStatistics::ReportSensors(const GBEnergy en) { sensors += en;}
void GBExpenditureStatistics::ReportBrain(const GBEnergy en) { brain += en;}
void GBExpenditureStatistics::ReportStolen(const GBEnergy en) { stolen += en;}
void GBExpenditureStatistics::ReportWasted(const GBEnergy en) { wasted += en;}

long GBExpenditureStatistics::Construction() const { return construction.Round(); }
long GBExpenditureStatistics::Engine() const { return engine.Round(); }
long GBExpenditureStatistics::Weapons() const { return weapons.Round(); }
long GBExpenditureStatistics::ForceField() const { return forceField.Round(); }
long GBExpenditureStatistics::Shield() const { return shield.Round(); }
long GBExpenditureStatistics::Repairs() const { return repairs.Round(); }
long GBExpenditureStatistics::Sensors() const { return sensors.Round(); }
long GBExpenditureStatistics::Brain() const { return brain.Round(); }
long GBExpenditureStatistics::Stolen() const { return stolen.Round(); }
long GBExpenditureStatistics::Wasted() const { return wasted.Round(); }

long GBExpenditureStatistics::Total() const {
	return Construction() + Engine()
		+ Weapons() + ForceField() + Shield() + Repairs()
		+ Sensors() + Brain() + Stolen() + Wasted();
}

GBExpenditureStatistics & GBExpenditureStatistics::operator +=(const GBExpenditureStatistics & other) {
	construction += other.construction;
	engine += other.engine;
	weapons += other.weapons;
	forceField += other.forceField;
	shield += other.shield;
	repairs += other.repairs;
	sensors += other.sensors;
	brain += other.brain;
	stolen += other.stolen;
	wasted += other.wasted;
	return *this;
}

// GBScores //

GBScores::GBScores()
	: rounds(0), sides(0), survived(0), sterile(0), earlyDeaths(0), elimination(0),
	population(0), populationEver(0),
	biomass(0), earlyBiomass(0),
	constructor(0),
	territory(0),
	dead(0), killed(0), suicide(0),
	income(), expenditure(), seeded(0),
	biomassFraction(0.0), earlyBiomassFraction(0.0),
	killedFraction(0.0),
	biomassFractionSquared(0.0)
{
	biomassHistory.resize(1, 0);
}

GBScores::~GBScores() {}

void GBScores::Reset() {
	rounds = sides = 0;
	survived = sterile = earlyDeaths = elimination = 0;
	population = populationEver = 0;
	biomass = earlyBiomass = 0;
	constructor = 0;
	territory = 0;
	seeded = 0;
	income.Reset();
	expenditure.Reset();
	dead = killed = suicide = 0;
	biomassFraction = earlyBiomassFraction = 0.0;
	killedFraction = 0.0;
	biomassFractionSquared = 0.0;
	biomassHistory.resize(1, 0);
	biomassHistory[0] = 0;
}

void GBScores::OneRound() { rounds = 1; }

GBScores & GBScores::operator +=(const GBScores & other) {
	rounds += other.rounds;
	sides += other.sides;
	survived += other.survived;
	sterile += other.sterile;
	earlyDeaths += other.earlyDeaths;
	elimination += other.elimination;
	population += other.population;
	populationEver += other.populationEver;
	biomass += other.biomass;
	earlyBiomass += other.earlyBiomass;
	constructor += other.constructor;
	territory += other.territory;
	seeded += other.seeded;
	income += other.income;
	expenditure += other.expenditure;
	dead += other.dead;
	killed += other.killed;
	suicide += other.suicide;
	biomassFraction += other.biomassFraction;
	earlyBiomassFraction += other.earlyBiomassFraction;
	killedFraction += other.killedFraction;
	biomassFractionSquared += other.biomassFractionSquared;
//add biomass
	if (biomassHistory.size() < other.biomassHistory.size())
		biomassHistory.resize(other.biomassHistory.size(), 0);
	for ( int i = 0; i < other.biomassHistory.size(); ++i )
		biomassHistory[i] += other.biomassHistory[i];
	return *this;
}

long GBScores::Sides() const {
	return sides;}

long GBScores::Rounds() const {
	return rounds;}

long GBScores::Survived() const {
	return survived;}

long GBScores::Sterile() const {
	return sterile;}

long GBScores::EarlyDeaths() const {
	return earlyDeaths;}

long GBScores::SurvivedEarly() const {
	return sides - earlyDeaths;}

float GBScores::Survival() const {
	return sides ? (float)survived / sides : 0;}

float GBScores::SurvivalNotSterile() const {
	return sides ? 1.0 - (float)sterile / sides : 0;}

float GBScores::EarlyDeathRate() const {
	return sides ? (float)earlyDeaths / sides : 0;}

float GBScores::LateDeathRate() const {
	if ( sides - earlyDeaths <= 0 ) return 0;
	return 1.0 - (float)(sides - sterile) / (sides - earlyDeaths);
}

long GBScores::Elimination() const { return elimination; }

float GBScores::EliminationRate() const {
	return rounds ? (float)elimination / rounds : 0;}

long GBScores::Population() const {
	return population / (rounds ? rounds : 1);}

long GBScores::PopulationEver() const {
	return populationEver / (rounds ? rounds : 1);}

long GBScores::Biomass() const { return biomass / rounds; }
long GBScores::EarlyBiomass() const { return earlyBiomass / rounds; }
long GBScores::SurvivalBiomass() const { return biomass / survived; }
long GBScores::EarlySurvivalBiomass() const {
	return earlyBiomass / (rounds - earlyDeaths);}

float GBScores::BiomassFraction() const {
	return biomassFraction / (rounds ? rounds : 1);}

float GBScores::EarlyBiomassFraction() const {
	return earlyBiomassFraction / (rounds ? rounds : 1);}

float GBScores::SurvivalBiomassFraction() const {
	return biomassFraction / (survived ? survived : 1);}

const std::vector<long> GBScores::BiomassHistory() const {
	if ( rounds <= 1 )
		return biomassHistory;
	std::vector<long> avg = biomassHistory;
	for ( int i = 0; i < avg.size(); ++i )
		avg[i] /= rounds;
	return avg;
}

long GBScores::Constructor() const { return constructor / rounds; }

long GBScores::Territory() const { return territory; }

long GBScores::Seeded() const { return seeded / rounds; }

GBIncomeStatistics & GBScores::Income() { return income;}
const GBIncomeStatistics & GBScores::Income() const { return income;}

GBExpenditureStatistics & GBScores::Expenditure() { return expenditure;}
const GBExpenditureStatistics & GBScores::Expenditure() const { return expenditure;}

long GBScores::Dead() const {
	return dead / rounds;}

long GBScores::Killed() const {
	return killed / rounds;}

long GBScores::Suicide() const {
	return suicide / rounds;}

float GBScores::KilledFraction() const {
	return killedFraction / (rounds ? rounds : 1);}

float GBScores::KillRate() const {
	if ( ! biomass.Round() ) return 0.0;
	return (float)killed.Round() / biomass.Round();
}

float GBScores::Efficiency() const {
	if ( ! income.Total() ) return 0.0;
	return (float)(biomass.Round() - seeded.Round()) / Income().Total();
}

GBFrames GBScores::Doubletime(GBFrames currentTime) const {
	if ( ! seeded.Round() || ! biomass.Round() ) return 0;
	return (GBFrames)(currentTime
		* (log(2.0) / log((double)biomass.Round() / (double)seeded.Round())));
}

float GBScores::BiomassFractionSD() const {
	float frac = BiomassFraction();
	if (!rounds) return 0.0;
	float variance = biomassFractionSquared / rounds - frac * frac;
	return variance < 0 ? 0 : sqrt(variance); //rounding error can make variance slightly negative when it should be zero
}

//Sampling error: twice the standard deviation of the mean.
float GBScores::BiomassFractionError() const {
	return rounds > 1 ? BiomassFractionSD() / sqrt((float)(rounds - 1)) * 2.0 : 1.0;
}

// GBSideScores //

GBSideScores::GBSideScores()
	: GBScores(), extinctTime(0), sterileTime(0)
{}

GBSideScores::~GBSideScores() {}

void GBSideScores::ResetSampledStatistics() {
	population = 0;
	biomass = 0;
	constructor = 0;
	territory = 0;
}

void GBSideScores::Reset() {
	GBScores::Reset();
	extinctTime = 0;
	sterileTime = 0;
}

void GBSideScores::ReportRobot(const GBEnergy botBiomass, const GBEnergy construc) {
	population += 1;
	biomass += botBiomass;
	constructor += construc;
}

void GBSideScores::ReportDead(const GBEnergy en) { dead += en; }
void GBSideScores::ReportKilled(const GBEnergy en) { killed += en; }
void GBSideScores::ReportSuicide(const GBEnergy en) { suicide += en; }

void GBSideScores::ReportSeeded(const GBEnergy en) {
	seeded += en;
	if ( biomassHistory.size() == 1 )
		biomassHistory[0] = seeded.Round();
	sides = 1;
	rounds = 1;
}

void GBSideScores::ReportTerritory() { ++ territory; }

void GBSideScores::ReportTotals(const GBScores & totals) {
	biomassFraction = totals.Biomass() ? (float)biomass.Round() / totals.Biomass() : 0.0;
	biomassFractionSquared = biomassFraction * biomassFraction;
	earlyBiomassFraction = totals.EarlyBiomass() ?
		(float)earlyBiomass.Round() / totals.EarlyBiomass() : 0.0;
	killedFraction = totals.Killed() ? (float)killed.Round() / totals.Killed() : 0.0;
	if ( totals.Survived() == 1 && survived ) elimination = 1;
}

void GBSideScores::ReportFrame(const GBFrames frame) {
    if ( seeded.Zero() ) return;
	if ( population ) {
		extinctTime = 0;
		survived = 1;
		if ( constructor.Round() <= kMaxSterileConstructor ) {
			if ( ! sterile ) {
				sterile = 1;
				sterileTime = frame;
			}
		} else {
			sterile = 0;
			sterileTime = 0;
		}
	} else if ( ! extinctTime ) {
		extinctTime = frame;
		if ( ! sterileTime ) sterileTime = frame;
		survived = 0;
		sterile = 1;
	}
	if ( frame == kEarlyDeathTime ) {
		earlyBiomass = biomass;
		if ( sterile ) earlyDeaths = 1;
	}
	if ( frame % 100 == 0 )
		biomassHistory.push_back(biomass.Round());
}

GBFrames GBSideScores::ExtinctTime() const { return extinctTime; }
GBFrames GBSideScores::SterileTime() const { return sterileTime; }

long GBSideScores::GetNewRobotNumber() {
	return ++ populationEver; // preincrement for 1-based numbering
}

