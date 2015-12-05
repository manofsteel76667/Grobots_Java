/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class GBSideScores extends GBScores {
	int extinctTime, sterileTime;
	public static final int kEarlyDeathTime = 4500;
	public static final int kMaxSterileConstructor = 10;

	public GBSideScores() {
	}

	public void resetSampledStatistics() {
		population = 0;
		biomass = 0;
		constructor = economyHardware = combatHardware = totalHardware = 0;
		territory = 0;
	}

	@Override
	public void reset() {
		super.reset();
		extinctTime = 0;
		sterileTime = 0;
	}

	public void reportRobot(double botBiomass, double construc, double econ,
			double combat, double hw) {
		population += 1;
		biomass += botBiomass;
		constructor += construc;
		economyHardware += econ;
		combatHardware += combat;
		totalHardware += hw;
	}

	public void reportDead(double en) {
		dead += en;
	}

	public void reportKilled(double en) {
		killed += en;
	}

	public void reportSuicide(double en) {
		suicide += en;
	}

	public void reportDamageDone(double d) {
		damageDone += d;
	}

	public void reportDamageTaken(double d) {
		damageTaken += d;
	}

	public void reportFriendlyFire(double d) {
		friendlyFire += d;
	}

	public void reportSeeded(double en) {
		seeded += en;
		if (biomassHistory.size() == 1)
			biomassHistory.set(0, (int) seeded);
		sides = 1;
		rounds = 1;
	}

	public void reportTerritory() {
		++territory;
	}

	public void reportTotals(GBScores totals) {
		biomassFraction = totals.getBiomass() != 0 ? biomass / totals.getBiomass()
				: 0.0;
		biomassFractionSquared = biomassFraction * biomassFraction;
		earlyBiomassFraction = totals.getEarlyBiomass() != 0 ? earlyBiomass
				/ totals.getEarlyBiomass() : 0.0;
		killedFraction = totals.getKilled() != 0 ? killed / totals.getKilled() : 0.0;
		if (totals.survived == 1 && survived != 0)
			elimination = 1;
	}

	public void reportFrame(int frame) {
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

	public int getExtinctTime() {
		return extinctTime;
	}

	public int getSterileTime() {
		return sterileTime;
	}

	int getNewRobotNumber() {
		return ++populationEver; // preincrement for 1-based numbering
	}

};