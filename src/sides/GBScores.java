// GBScores.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package sides;

import java.util.LinkedList;
import java.util.List;

public class GBScores {
	// //protected:
	// rounds
	public int sides; // = number of seeds
	public int rounds;
	public int survived;
	public int sterile;
	public int earlyDeaths;
	public int elimination;
	// sampled
	public int population;
	int populationEver;
	public double biomass, earlyBiomass;
	public List<Integer> biomassHistory; // may eventually be a vector of
											// GBScores
	public double constructor;
	public double economyHardware, combatHardware, totalHardware;
	public int territory;
	// accumulated
	public double seeded;
	public GBIncomeStatistics income;
	public GBExpenditureStatistics expenditure;
	public double dead, killed, suicide;
	public double damageDone, damageTaken, friendlyFire;
	// fractions
	public double biomassFraction, earlyBiomassFraction;
	public double killedFraction;
	public double biomassFractionSquared; // for standard deviations

	// GBScores //

	public GBScores() {
		biomassHistory = new LinkedList<Integer>();
		biomassHistory.add(0);
		income = new GBIncomeStatistics();
		expenditure = new GBExpenditureStatistics();
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
		biomassHistory.add(0);
	}

	public void OneRound() {
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

	double Survival() {
		return sides != 0 ? (double) survived / sides : 0;
	}

	public double SurvivalNotSterile() {
		return sides != 0 ? 1.0f - (double) sterile / sides : 0f;
	}

	public double EarlyDeathRate() {
		return sides != 0 ? (double) earlyDeaths / sides : 0f;
	}

	public double LateDeathRate() {
		if (sides - earlyDeaths <= 0)
			return 0;
		return 1.0f - (double) (sides - sterile) / (sides - earlyDeaths);
	}

	double EliminationRate() {
		return rounds != 0 ? (double) elimination / rounds : 0;
	}

	public int Population() {
		return population / (rounds != 0 ? rounds : 1);
	}

	int PopulationEver() {
		return populationEver / (rounds != 0 ? rounds : 1);
	}

	public int Biomass() {
		return (int) (biomass / rounds);
	}

	int EarlyBiomass() {
		return (int) (earlyBiomass / rounds);
	}

	int SurvivalBiomass() {
		return (int) (biomass / survived);
	}

	int EarlySurvivalBiomass() {
		return (int) (earlyBiomass / (rounds - earlyDeaths));
	}

	public double BiomassFraction() {
		return biomassFraction / (rounds != 0 ? rounds : 1);
	}

	public double EarlyBiomassFraction() {
		return earlyBiomassFraction / (rounds != 0 ? rounds : 1);
	}

	public double SurvivalBiomassFraction() {
		return biomassFraction / (survived != 0 ? survived : 1);
	}

	List<Integer> BiomassHistory() {
		if (rounds <= 1)
			return biomassHistory;
		List<Integer> avg = biomassHistory;
		for (int i = 0; i < avg.size(); i++)
			avg.set(i, avg.get(i) / rounds);
		return avg;
	}

	int Constructor() {
		return rounds != 0 ? (int) (constructor / rounds) : 0;
	}

	int Territory() {
		return territory;
	}

	double EconFraction() {
		return economyHardware / totalHardware;
	}

	double CombatFraction() {
		return combatHardware / totalHardware;
	}

	public int Seeded() {
		return rounds != 0 ? (int) (seeded / rounds) : 0;
	}

	int Dead() {
		return (int) (dead / rounds);
	}

	int Killed() {
		return (int) (killed / rounds);
	}

	int Suicide() {
		return (int) (suicide / rounds);
	}

	public double KilledFraction() {
		return (int) (killedFraction / (rounds != 0 ? rounds : 1));
	}

	public double KillRate() {
		if (biomass == 0)
			return 0.0f;
		return killed / biomass;
	}

	// What fraction of income has ended up as growth?
	double Efficiency() {
		if (income.Total() == 0)
			return 0.0f;
		return (biomass - seeded) / income.Total();
	}

	int Doubletime(int currentTime) {
		if (seeded == 0 || biomass <= 1)
			return 0;
		return (int) (currentTime * Math.log(2.0) / Math.log(biomass) / seeded);
	}

	double BiomassFractionSD() {
		double frac = BiomassFraction();
		if (rounds == 0)
			return 0.0f;
		double variance = biomassFractionSquared / rounds - frac * frac;
		return variance < 0 ? 0 : Math.sqrt(variance); // rounding
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
	public double BiomassFractionError() {
		return rounds > 1 ? BiomassFractionSD() / Math.sqrt(rounds - 1) * 2.0
				: 1.0;
	}
};
