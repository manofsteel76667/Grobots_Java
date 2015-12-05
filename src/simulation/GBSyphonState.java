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

	public double getMaxRate() {
		return spec.getPower();
	}

	public double getMaxRange() {
		return spec.getRange();
	}

	public double getDirection() {
		return direction;
	}

	public double getDistance() {
		return distance;
	}

	public double getRate() {
		return rate;
	}

	public double getSyphoned() {
		return syphoned;
	}

	public void setDistance(double dist) {
		distance = Math.max(dist, 0);
	}

	public void setDirection(double dir) {
		direction = dir;
	}

	public void setRate(double pwr) {
		rate = GBMath.clamp(pwr, -getMaxRate(), getMaxRate());
	}

	public void reportUse(double pwr) {
		syphoned += pwr;
	}

	public void act(GBRobot robot, GBWorld world) {
		if (rate != 0) {
			double limit = getMaxRate() * robot.getShieldFraction(); // should maybe
																// diminish with
																// distance
			double actual = GBMath.clamp(rate, -limit, limit);
			GBObject shot = new GBSyphon(
					robot.getPosition().addPolar(
							Math.min(distance, robot.getRadius() + getMaxRange()),
							direction), actual, robot, this, spec.getHitsEnemies());
			world.addObjectLater(shot);
		}
		syphoned = 0;
	}
}
