/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.ForceFieldSpec;
import support.FinePoint;
import support.GBMath;

public class GBForceFieldState {
	ForceFieldSpec spec;
	// orders
	double direction;
	double distance;
	double power;
	double angle;

	// public:

	public GBForceFieldState(ForceFieldSpec spc) {
		spec = spc;
	}

	public double getMaxRange() {
		return spec.getRange();
	}

	public double getMaxPower() {
		return spec.getPower();
	}

	public double getDirection() {
		return direction;
	}

	public double getDistance() {
		return distance;
	}

	public double getPower() {
		return power;
	}

	public double getAngle() {
		return angle;
	}

	public double getRadius() {
		return GBForceField.getPowerRadius(power);
	}

	public void getSetDistance(double dist) {
		distance = GBMath.clamp(dist, 0, getMaxRange());
	}

	public void getSetDirection(double dir) {
		direction = dir;
	}

	public void getSetPower(double pwr) {
		power = GBMath.clamp(pwr, 0, getMaxPower());
	}

	public void setAngle(double ang) {
		angle = ang;
	}

	public void act(GBRobot robot, GBWorld world) {
		if (power == 0)
			return;
		double effective = power * robot.hardware.getEffectivenessFraction()
				* robot.getShieldFraction();
		if (power > 0 && robot.hardware.useEnergy(effective)) {
			robot.getOwner().getScores().expenditure.reportForceField(effective);
			FinePoint vel = FinePoint.makePolar(distance, direction);
			GBObject shot = new GBForceField(
					robot.getPosition().add(vel),
					vel,
					robot.getOwner(),
					effective
							/ (distance
									* GBHardwareState.kForceFieldRangeAttenuation + 1),
					angle);
			world.addObjectLater(shot);
			// robot.PushBy(- effective * kForceFieldRecoilPerPower, angle); //
			// recoil
		}
	}

}
