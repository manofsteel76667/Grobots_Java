package sides;

public class GBSideScores extends GBScores {
	long extinctTime, sterileTime;
	public static final long kEarlyDeathTime = 4500;
	public static final long kMaxSterileConstructor = 10;

	public GBSideScores() {
	}

	public void ResetSampledStatistics() {
		population = 0;
		biomass = 0;
		constructor = economyHardware = combatHardware = totalHardware = 0;

		territory = 0;
	}

	public void Reset() {
		Reset();
		extinctTime = 0;
		sterileTime = 0;
	}

	public void ReportRobot(double botBiomass, double construc, double econ,
			double combat, double hw) {
		population += 1;
		biomass += botBiomass;
		constructor += construc;
		economyHardware += econ;
		combatHardware += combat;
		totalHardware += hw;
	}

	public void ReportDead(double en) {
		dead += en;
	}

	public void ReportKilled(double en) {
		killed += en;
	}

	public void ReportSuicide(double en) {
		suicide += en;
	}

	public void ReportDamageDone(double d) {
		damageDone += d;
	}

	public void ReportDamageTaken(double d) {
		damageTaken += d;
	}

	public void ReportFriendlyFire(double d) {
		friendlyFire += d;
	}

	void ReportSeeded(double en) {
		seeded += en;
		if (biomassHistory.size() == 1)
			biomassHistory.set(0, (long) seeded);
		sides = 1;
		rounds = 1;
	}

	void ReportTerritory() {
		++territory;
	}

	void ReportTotals(GBScores totals) {
		biomassFraction = (float) (totals.Biomass() != 0 ? biomass
				/ totals.Biomass() : 0.0);
		biomassFractionSquared = biomassFraction * biomassFraction;
		earlyBiomassFraction = (float) (totals.EarlyBiomass() != 0 ? earlyBiomass
				/ totals.EarlyBiomass()
				: 0.0);
		killedFraction = (float) (totals.Killed() != 0 ? killed
				/ totals.Killed() : 0.0);
		if (totals.survived == 1 && survived != 0)
			elimination = 1;
	}

	void ReportFrame(long frame) {
		if (seeded == 0)
			return;
		if (population != 0) {
			extinctTime = 0;
			survived = 1;
			if (constructor <= kMaxSterileConstructor) {
				if (sterile == 0) {
					sterile = 1;
					sterileTime = frame;
				}
			} else {
				sterile = 0;
				sterileTime = 0;
			}
		} else if (extinctTime == 0) {
			extinctTime = frame;
			if (sterileTime == 0)
				sterileTime = frame;
			survived = 0;
			sterile = 1;
		}
		if (frame == kEarlyDeathTime) {
			earlyBiomass = biomass;
			if (sterile != 0)
				earlyDeaths = 1;
		}
		if (frame % 100 == 0 && frame != 0)
			biomassHistory.add((long) biomass);
	}

	long ExtinctTime() {
		return extinctTime;
	}

	long SterileTime() {
		return sterileTime;
	}

	long GetNewRobotNumber() {
		return ++populationEver; // preincrement for 1-based numbering
	}

};