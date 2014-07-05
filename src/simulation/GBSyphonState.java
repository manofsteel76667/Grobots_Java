/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.SyphonSpec;
import support.GBMath;

public class GBSyphonState {
	SyphonSpec spec;
	// orders
	double direction;
	double distance;
	double rate;
	double syphoned; // amount siphoned: for reporting to brains

	// public:

	public GBSyphonState(SyphonSpec spc) {
		spec = spc;
	}

	public double MaxRate() {
		return spec.Power();
	}

	public double MaxRange() {
		return spec.Range();
	}

	public double Direction() {
		return direction;
	}

	public double Distance() {
		return distance;
	}

	public double Rate() {
		return rate;
	}

	public double Syphoned() {
		return syphoned;
	}

	public void SetDistance(double dist) {
		distance = Math.max(dist, 0);
	}

	public void SetDirection(double dir) {
		direction = dir;
	}

	public void SetRate(double pwr) {
		rate = GBMath.clamp(pwr, -MaxRate(), MaxRate());
	}

	public void ReportUse(double pwr) {
		syphoned += pwr;
	}

	public void Act(GBRobot robot, GBWorld world) {
		if (rate != 0) {
			double limit = MaxRate() * robot.ShieldFraction(); // should maybe
																// diminish with
																// distance
			double actual = GBMath.clamp(rate, -limit, limit);
			GBObject shot = new GBSyphon(
					robot.Position().addPolar(
							Math.min(distance, robot.Radius() + MaxRange()),
							direction), actual, robot, this, spec.HitsEnemies());
			world.AddObjectNew(shot);
		}
		syphoned = 0;
	}
}
