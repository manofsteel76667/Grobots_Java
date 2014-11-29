/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.ConstructorSpec;
import sides.RobotType;
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

	public double Rate() {
		return rate;
	}

	public double Progress() {
		return progress;
	}

	public double Remaining() {
		if (type == null)
			return 0;
		return type.Cost() - progress;
	}

	public double FetusMass() {
		return type != null ? Fraction() * type.Mass() : 0;
	}

	public double Fraction() {
		if (type == null)
			return 0;
		return progress / type.Cost();
	}

	public double MaxRate() {
		return spec.Rate();
	}

	public RobotType Type() {
		return type;
	}

	public int ChildID() {
		return lastChild;
	}

	public void Start(RobotType ntype, double free) {
		if (type == ntype)
			return;
		abortion += progress;
		type = ntype;
		progress = free; // could have progress but no type - harmless, but it
							// doesn't happen
	}

	public void SetRate(double nrate) {
		rate = GBMath.clamp(nrate, 0, MaxRate());
	}

	public void Act(GBRobot robot, GBWorld world) {
		if (type != null && rate != 0) {
			double actual = robot.hardware.UseEnergyUpTo(Math.min(rate,
					Remaining()));
			robot.Owner().Scores().expenditure.ReportConstruction(actual);
			progress += actual;
			if (Remaining() <= 0 && robot.hardware.ActualShield() == 0) {
				// TODO: put back in when sound is implemented
				// StartSound(siBirth);
				double dir = world.Randoms().Angle();
				GBRobot child = new GBRobot(type, robot.Position().addPolar(
						GBHardwareState.kBabyDisplacementFraction
								* robot.Radius(), dir), robot.Velocity()
						.addPolar(GBHardwareState.kBabyInitialSpeed, dir),
						robot.ID());
				world.AddObjectNew(child);
				progress = 0;
				type = null;
				lastChild = child.ID();
			}
			robot.Recalculate();
		}
		if (abortion > 0) {
			world.AddObjectNew(new GBCorpse(robot.Position(), robot.Velocity(),
					abortion * GBHardwareState.kAbortionCorpseFactor, robot
							.Type(), null)); // should really be child type, but
												// we don't know that any more
			abortion = 0;
		}
	}

}
