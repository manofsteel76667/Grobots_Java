/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.GrenadesSpec;
import sound.SoundManager;
import support.GBMath;

public class GBGrenadesState {
	GrenadesSpec spec;
	// state
	int cooldown;
	// orders
	boolean firing;
	double direction;
	double distance;

	public void setReloaded() {
		cooldown = 0;
	}

	public GBGrenadesState(GrenadesSpec spc) {
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

	public double getDistance() {
		return distance;
	}

	public double getExplosionRadius() {
		return GBExplosion.PowerRadius(getDamage());
	}

	public void fire(double dist, double dir) {
		if (cooldown == 0 && getDamage() > 0) {
			cooldown = getReloadTime();
			firing = true;
			direction = dir;
			distance = GBMath.clamp(dist, getSpeed(), getMaxRange());
		}
	}

	public void act(GBRobot robot, GBWorld world) {
		if (firing) {
			double effectiveness = robot.hardware.getEffectivenessFraction();
			if (robot.hardware.getActualShield() == 0
					&& robot.hardware.useEnergy(getFiringCost() * effectiveness)) {
				robot.getOwner().getScores().expenditure.reportWeapons(getFiringCost()
						* effectiveness);
				SoundManager.playSound(SoundManager.SoundType.stGrenade, robot.position);
				int lifetime = (int) Math.max(
						Math.floor((distance - robot.getRadius()) / getSpeed()), 1);
				GBObject shot = new GBGrenade(robot.getPosition().addPolar(
						robot.getRadius(), direction), robot.getVelocity().addPolar(
						getSpeed(), direction), robot.getOwner(), getDamage()
						* effectiveness, lifetime);
				world.addObjectLater(shot);
				for (double en = getFiringCost() * effectiveness; en >= GBHardwareState.kGrenadesFiringCostPerSmoke; en -= GBHardwareState.kGrenadesFiringCostPerSmoke) {
					GBObject smoke = new GBSmoke(robot.getPosition().addPolar(
							robot.getRadius(), direction),
							world.random
									.Vector(GBTimedDecoration.kSmokeMaxSpeed),
							world.random.intInRange(
									GBTimedDecoration.kSmokeMinLifetime,
									GBTimedDecoration.kSmokeMaxLifetime));
					world.addObjectLater(smoke);
				}
				cooldown = getReloadTime();
				// recoil
				// robot.PushBy(- spec.Recoil() * effectiveness, direction);
			}
			firing = false;
		}
		if (cooldown > 0)
			cooldown--;
	}
}
