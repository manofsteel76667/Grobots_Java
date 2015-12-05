/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class GrenadesSpec extends HardwareItem {

	@Override
	public double getMass() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kGrenadesBarrelMass)
					* (damage + HardwareSpec.kGrenadesDamageOverhead)
					* (HardwareSpec.kGrenadesMassPerDamageRate + range
							* HardwareSpec.kGrenadesMassPerRange + Math.pow(
							range, 2)
							* HardwareSpec.kGrenadesMassPerRangeSquared);
		else
			return 0;
	}

	@Override
	public double getCost() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kGrenadesBarrelCost)
					* (damage + HardwareSpec.kGrenadesDamageOverhead)
					* (HardwareSpec.kGrenadesCostPerDamageRate + range
							* HardwareSpec.kGrenadesCostPerRange + Math.pow(
							range, 2.0)
							* HardwareSpec.kGrenadesCostPerRangeSquared);
		else
			return 0;
	}

	double damage;
	double range;
	double speed;
	int reloadTime;

	public GrenadesSpec() {
		super(0, 0);
		damage = 0;
		range = 0;
		speed = 0;
		reloadTime = 1;
	}

	public double getDamage() {
		return damage;
	}

	public double getRange() {
		return range;
	}

	public double getSpeed() {
		return speed;
	}

	public int getLifetime() {
		return (int) Math.ceil(range / speed);
	}

	public int getReloadTime() {
		return reloadTime;
	}

	public double getRecoil() {
		return range * damage * HardwareSpec.kGrenadesRecoil;
	}

	public void set(double dmg, double rng, int reload) {
		damage = Math.max(dmg, 0);
		range = Math.max(rng, 0);
		speed = Math.pow(range / HardwareSpec.kGrenadesLifetimeSpeedTradeoff,
				HardwareSpec.kGrenadesSpeedExponent);
		reloadTime = (reload < 1 ? 1 : reload); // no reload time less than 1
												// frame
	}

	public double getFiringCost() {
		return (damage + HardwareSpec.kGrenadesDamageOverhead)
				* (HardwareSpec.kGrenadesFiringCostPerDamage + range
						* HardwareSpec.kGrenadesFiringCostPerRange + Math.pow(
						range, 2)
						* HardwareSpec.kGrenadesFiringCostPerRangeSquared);
	}
}
