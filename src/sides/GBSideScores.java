package sides;

public class GBSideScores extends GBScores {
	int extinctTime, sterileTime;
	public static final int kEarlyDeathTime = 4500;
	public static final int kMaxSterileConstructor = 10;

	public GBSideScores() {
	}

	public void ResetSampledStatistics() {
		population = 0;
		biomass = 0;
		constructor = economyHardware = combatHardware = totalHardware = 0;
		territory = 0;
	}

	@Override
	public void Reset() {
		super.Reset();
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

	public void ReportSeeded(double en) {
		seeded += en;
		if (biomassHistory.size() == 1)
			biomassHistory.set(0, (int) seeded);
		sides = 1;
		rounds = 1;
	}

	public void ReportTerritory() {
		++territory;
	}

	public void ReportTotals(GBScores totals) {
		biomassFraction = totals.Biomass() != 0 ? biomass / totals.Biomass()
				: 0.0;
		biomassFractionSquared = biomassFraction * biomassFraction;
		earlyBiomassFraction = totals.EarlyBiomass() != 0 ? earlyBiomass
				/ totals.EarlyBiomass() : 0.0;
		killedFraction = totals.Killed() != 0 ? killed / totals.Killed() : 0.0;
		if (totals.survived == 1 && survived != 0)
			elimination = 1;
	}

	public void ReportFrame(int frame) {
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
			biomassHistory.add((int) biomass);
	}

	public int ExtinctTime() {
		return extinctTime;
	}

	public int SterileTime() {
		return sterileTime;
	}

	int GetNewRobotNumber() {
		return ++populationEver; // preincrement for 1-based numbering
	}

};