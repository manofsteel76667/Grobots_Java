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

	public void setReloaded() {
		cooldown = 0;
	}

	GBBlasterState(BlasterSpec spc) {
		spec = spc;
		cooldown = spc.getReloadTime();
	}

	public int getReloadTime() {
		return spec.getReloadTime();
	}

	public double getSpeed() {
		return spec.getSpeed();
	}

	public int getMaxLifetime() {
		return spec.getLifetime();
	}

	public double getMaxRange() {
		return spec.getRange();
	}

	public double getDamage() {
		return spec.getDamage();
	}

	public double getFiringCost() {
		return spec.getFiringCost();
	}

	public int getCooldown() {
		return cooldown;
	}

	public boolean getFiring() {
		return firing;
	}

	public double getDirection() {
		return direction;
	}

	public void fire(double dir) {
		if (cooldown == 0 && getDamage() != 0) {
			cooldown = getReloadTime();
			firing = true;
			direction = dir;
		}
	}

	public void act(GBRobot robot, GBWorld world) {
		if (firing) {
			double effectiveness = robot.hardware.getEffectivenessFraction();
			if (robot.hardware.getActualShield() == 0
					&& robot.hardware.useEnergy(getFiringCost() * effectiveness)) {
				robot.getOwner().getScores().expenditure.reportWeapons(getFiringCost()
						* effectiveness);
				 if ( getDamage() >= 12 ) 
					 SoundManager.playSound(SoundManager.SoundType.stBigBlaster, robot.position);
				 else
					 SoundManager.playSound(SoundManager.SoundType.stBlaster, robot.position);
				GBObject shot = new GBBlast(robot.getPosition().addPolar(
						robot.getRadius(), direction), robot.getVelocity().addPolar(
						getSpeed(), direction), robot.getOwner(), getDamage()
						* effectiveness, getMaxLifetime());
				shot.moveBy(FinePoint.makePolar(shot.getRadius()
						+ GBHardwareState.kBlastSpacing, direction)); // to
																		// avoid
																		// hitting
																		// self
				world.addObjectLater(shot);
				cooldown = getReloadTime();
			}
			firing = false;
		}
		if (cooldown > 0)
			cooldown--;
	}

}
