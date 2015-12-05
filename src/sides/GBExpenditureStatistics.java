/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBScores.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package sides;

public class GBExpenditureStatistics {
	public double construction;
	public double engine;
	public double weapons;
	public double forceField;
	public double shield;
	public double repairs;
	public double sensors;
	public double brain;
	public double stolen;
	public double wasted;

	// GBExpenditureStatistics //

	GBExpenditureStatistics() {
	}

	public void reset() {
		construction = 0;
		engine = 0;
		weapons = 0;
		forceField = 0;
		shield = 0;
		repairs = 0;
		sensors = 0;
		brain = 0;
		stolen = 0;
		wasted = 0;
	}

	public void reportConstruction(double en) {
		construction += en;
	}

	public void reportEngine(double en) {
		engine += en;
	}

	public void reportForceField(double en) {
		forceField += en;
	}

	public void reportWeapons(double en) {
		weapons += en;
	}

	public void reportShield(double en) {
		shield += en;
	}

	public void reportRepairs(double en) {
		repairs += en;
	}

	public void reportSensors(double en) {
		sensors += en;
	}

	public void reportBrain(double en) {
		brain += en;
	}

	public void reportStolen(double en) {
		stolen += en;
	}

	public void reportWasted(double en) {
		wasted += en;
	}

	public int total() {
		return (int) (construction + engine + weapons + forceField + shield
				+ repairs + sensors + brain + stolen + wasted);
	}

	public void add(GBExpenditureStatistics other) {
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
	}

};