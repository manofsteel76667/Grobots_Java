// GBScores.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package sides;

import java.util.LinkedList;

public class GBScores {
	// protected:
	// rounds
	public long sides; // = number of seeds
	public long rounds;
	public long survived;
	public long sterile;
	public long earlyDeaths;
	public long elimination;
	// sampled
	public long population, populationEver;
	public double biomass, earlyBiomass;
	public LinkedList<Long> biomassHistory; // may eventually be a vector of
											// GBScores
	public double constructor;
	public double economyHardware, combatHardware, totalHardware;
	public long territory;
	// accumulated
	public double seeded;
	public GBIncomeStatistics income;
	public GBExpenditureStatistics expenditure;
	public double dead, killed, suicide;
	public double damageDone, damageTaken, friendlyFire;
	// fractions
	public float biomassFraction, earlyBiomassFraction;
	public float killedFraction;
	public float biomassFractionSquared; // for standard deviations

	// GBScores //

	GBScores() {
		biomassHistory = new LinkedList<Long>();
		biomassHistory.add(0l);
	}

	public void Reset() {
		rounds = sides = 0;
		survived = sterile = earlyDeaths = elimination = 0;
		population = populationEver = 0;
		biomass = earlyBiomass = 0;
		constructor = 0;
		economyHardware = combatHardware = totalHardware = 0;
		territory = 0;
		seeded = 0;
		income.Reset();
		expenditure.Reset();
		dead = killed = suicide = 0;
		damageDone = damageTaken = friendlyFire = 0;
		biomassFraction = earlyBiomassFraction = 0.0f;
		killedFraction = 0.0f;
		biomassFractionSquared = 0.0f;
		biomassHistory.clear();
		biomassHistory.add(0l);
	}

	void OneRound() {
		rounds = 1;
	}

	public void add(GBScores other) {
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
		economyHardware += other.economyHardware;
		combatHardware += other.combatHardware;
		totalHardware += other.totalHardware;
		territory += other.territory;
		seeded += other.seeded;
		income.add(other.income);
		expenditure.add(other.expenditure);
		dead += other.dead;
		killed += other.killed;
		suicide += other.suicide;
		damageDone += other.damageDone;
		damageTaken += other.damageTaken;
		friendlyFire += other.friendlyFire;
		biomassFraction += other.biomassFraction;
		earlyBiomassFraction += other.earlyBiomassFraction;
		killedFraction += other.killedFraction;
		biomassFractionSquared += other.biomassFractionSquared;
		// add biomass
		for (int i = 0; i < other.biomassHistory.size(); i++) {
			if (i < biomassHistory.size())
				biomassHistory.set(i, biomassHistory.get(i)
						+ other.biomassHistory.get(i));
			else
				biomassHistory.add(other.biomassHistory.get(i));
		}
	}

	float Survival() {
		return sides != 0 ? (float) survived / sides : 0;
	}

	float SurvivalNotSterile() {
		return sides != 0 ? 1.0f - (float) sterile / sides : 0f;
	}

	float EarlyDeathRate() {
		return sides != 0 ? (float) earlyDeaths / sides : 0f;
	}

	float LateDeathRate() {
		if (sides - earlyDeaths <= 0)
			return 0;
		return 1.0f - (float) (sides - sterile) / (sides - earlyDeaths);
	}

	float EliminationRate() {
		return rounds != 0 ? (float) elimination / rounds : 0;
	}

	long Population() {
		return population / (rounds != 0 ? rounds : 1);
	}

	long PopulationEver() {
		return populationEver / (rounds != 0 ? rounds : 1);
	}

	long Biomass() {
		return (long) (biomass / rounds);
	}

	long EarlyBiomass() {
		return (long) (earlyBiomass / rounds);
	}

	long SurvivalBiomass() {
		return (long) (biomass / survived);
	}

	long EarlySurvivalBiomass() {
		return (long) (earlyBiomass / (rounds - earlyDeaths));
	}

	float BiomassFraction() {
		return biomassFraction / (rounds != 0 ? rounds : 1);
	}

	float EarlyBiomassFraction() {
		return earlyBiomassFraction / (rounds != 0 ? rounds : 1);
	}

	float SurvivalBiomassFraction() {
		return biomassFraction / (survived != 0 ? survived : 1);
	}

	LinkedList<Long> BiomassHistory() {
		if (rounds <= 1)
			return biomassHistory;
		LinkedList<Long> avg = biomassHistory;
		for (int i = 0;i<avg.size();i++)
			avg.set(i, avg.get(i)/rounds);
		return avg;
	}

	long Constructor() {
		return rounds != 0 ? (long) (constructor / rounds) : 0;
	}

	long Territory() {
		return territory;
	}

	double EconFraction() {
		return economyHardware / totalHardware;
	}

	double CombatFraction() {
		return combatHardware / totalHardware;
	}

	long Seeded() {
		return rounds != 0 ? (long) (seeded / rounds) : 0;
	}

	long Dead() {
		return (long) (dead / rounds);
	}

	long Killed() {
		return (long) (killed / rounds);
	}

	long Suicide() {
		return (long) (suicide / rounds);
	}

	float KilledFraction() {
		return (long) (killedFraction / (rounds != 0 ? rounds : 1));
	}

	float KillRate() {
		if (biomass == 0)
			return 0.0f;
		return (float) (killed / biomass);
	}

	// What fraction of income has ended up as growth?
	float Efficiency() {
		if (income.Total() == 0)
			return 0.0f;
		return (float) ((biomass - seeded) / income.Total());
	}

	long Doubletime(long currentTime) {
		if (seeded == 0 || biomass <= 1)
			return 0;
		return (long) (currentTime * Math.log(2.0) / Math.log(biomass) / seeded);
	}

	float BiomassFractionSD() {
		float frac = BiomassFraction();
		if (rounds == 0)
			return 0.0f;
		double variance = biomassFractionSquared / rounds - frac * frac;
		return (float) (variance < 0 ? 0 : Math.sqrt(variance)); // rounding
																	// error can
																	// make
																	// variance
																	// slightly
																	// negative
																	// when it
																	// should be
																	// zero
	}

	// Sampling error: twice the standard deviation of the mean.
	float BiomassFractionError() {
		return (float) (rounds > 1 ? BiomassFractionSD()
				/ Math.sqrt((float) (rounds - 1)) * 2.0 : 1.0);
	}
};

