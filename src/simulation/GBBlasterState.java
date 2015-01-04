/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.BlasterSpec;
import sound.SoundManager;
import support.FinePoint;

public class GBBlasterState {
	BlasterSpec spec;
	// state
	int cooldown;
	// orders
	boolean firing;
	double direction;

	// public:

	GBBlasterState(BlasterSpec spc) {
		spec = spc;
		cooldown = spc.ReloadTime();
	}

	public int ReloadTime() {
		return spec.ReloadTime();
	}

	public double Speed() {
		return spec.Speed();
	}

	public int MaxLifetime() {
		return spec.Lifetime();
	}

	public double MaxRange() {
		return spec.Range();
	}

	public double Damage() {
		return spec.Damage();
	}

	public double FiringCost() {
		return spec.FiringCost();
	}

	public int Cooldown() {
		return cooldown;
	}

	public boolean Firing() {
		return firing;
	}

	public double Direction() {
		return direction;
	}

	public void Fire(double dir) {
		if (cooldown == 0 && Damage() != 0) {
			cooldown = ReloadTime();
			firing = true;
			direction = dir;
		}
	}

	public void Act(GBRobot robot, GBWorld world) {
		if (firing) {
			double effectiveness = robot.hardware.EffectivenessFraction();
			if (robot.hardware.ActualShield() == 0
					&& robot.hardware.UseEnergy(FiringCost() * effectiveness)) {
				robot.Owner().Scores().expenditure.ReportWeapons(FiringCost()
						* effectiveness);
				 if ( Damage() >= 12 ) 
					 SoundManager.playSound(SoundManager.SoundType.stBigBlaster, robot.position);
				 else
					 SoundManager.playSound(SoundManager.SoundType.stBlaster, robot.position);
				GBObject shot = new GBBlast(robot.Position().addPolar(
						robot.Radius(), direction), robot.Velocity().addPolar(
						Speed(), direction), robot.Owner(), Damage()
						* effectiveness, MaxLifetime());
				shot.MoveBy(FinePoint.makePolar(shot.Radius()
						+ GBHardwareState.kBlastSpacing, direction)); // to
																		// avoid
																		// hitting
																		// self
				world.addObjectLater(shot);
				cooldown = ReloadTime();
			}
			firing = false;
		}
		if (cooldown > 0)
			cooldown--;
	}

}
