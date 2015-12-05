/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.ConstructorSpec;
import sides.RobotType;
import sound.SoundManager;
import support.GBMath;

public class GBConstructorState {
	ConstructorSpec spec;
	// state
	RobotType type;
	double progress;
	double abortion;
	int lastChild;
	// orders
	double rate;

	// public:

	public GBConstructorState(ConstructorSpec spc) {
		spec = spc;
	}

	public double getRate() {
		return rate;
	}

	public double getProgress() {
		return progress;
	}

	public double getRemaining() {
		if (type == null)
			return 0;
		return type.getCost() - progress;
	}

	public double getFetusMass() {
		return type != null ? getFraction() * type.getMass() : 0;
	}

	public double getFraction() {
		if (type == null)
			return 0;
		return progress / type.getCost();
	}

	public double getMaxRate() {
		return spec.getRate();
	}

	public RobotType getRobotType() {
		return type;
	}

	public int getChildID() {
		return lastChild;
	}

	public void start(RobotType ntype, double free) {
		if (type == ntype)
			return;
		abortion += progress;
		type = ntype;
		progress = free; // could have progress but no type - harmless, but it
							// doesn't happen
	}

	public void setRate(double nrate) {
		rate = GBMath.clamp(nrate, 0, getMaxRate());
	}

	public void act(GBRobot robot, GBWorld world) {
		if (type != null && rate != 0) {
			double actual = robot.hardware.useEnergyUpTo(Math.min(rate,
					getRemaining()));
			robot.getOwner().getScores().expenditure.reportConstruction(actual);
			progress += actual;
			if (getRemaining() <= 0 && robot.hardware.getActualShield() == 0) {
				SoundManager.playSound(SoundManager.SoundType.stBirth, robot.position);
				double dir = world.random.Angle();
				GBRobot child = new GBRobot(type, robot.getPosition().addPolar(
						GBHardwareState.kBabyDisplacementFraction
								* robot.getRadius(), dir), robot.getVelocity()
						.addPolar(GBHardwareState.kBabyInitialSpeed, dir),
						robot.getID());
				world.addObjectLater(child);
				progress = 0;
				type = null;
				lastChild = child.getID();
			}
			robot.recalculate();
		}
		if (abortion > 0) {
			world.addObjectLater(new GBCorpse(robot.getPosition(), robot
					.getVelocity(), abortion
					* GBHardwareState.kAbortionCorpseFactor, robot.getRobotType(), null)); // should
																					// really
																					// be
																					// child
																					// type,
																					// but
																					// we
																					// don't
																					// know
																					// that
																					// any
																					// more
			abortion = 0;
		}
	}

}
