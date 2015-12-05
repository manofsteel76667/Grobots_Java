/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class BlasterSpec extends HardwareItem {

	@Override
	public double getMass() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kBlasterBarrel)
					* (damage + HardwareSpec.kBlasterDamageOverhead)
					* (HardwareSpec.kBlasterMassPerDamageRate + range
							* HardwareSpec.kBlasterMassPerRange + Math.pow(
							range, 2)
							* HardwareSpec.kBlasterMassPerRangeSquared);
		else
			return 0;
	}

	@Override
	public double getCost() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kBlasterBarrel)
					* (damage + HardwareSpec.kBlasterDamageOverhead)
					* (HardwareSpec.kBlasterCostPerDamageRate + range
							* HardwareSpec.kBlasterCostPerRange + Math.pow(
							range, 2)
							* HardwareSpec.kBlasterCostPerRangeSquared);
		else
			return 0;
	}

	public BlasterSpec() {
		super(0, 0);
		damage = 0;
		range = 0;
		speed = 0;
		lifetime = 0;
		reloadTime = 1;
	}

	private double damage;
	private double speed;
	private double range;
	private int reloadTime;
	private int lifetime;

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
		return lifetime;
	}

	public int getReloadTime() {
		return reloadTime;
	}

	public void set(double dmg, double rng, int reload) {
		damage = Math.max(dmg, 0);
		range = Math.max(rng, 0);
		speed = Math.pow(range / HardwareSpec.kBlasterLifetimeSpeedTradeoff,
				HardwareSpec.kBlasterSpeedExponent);
		lifetime = (int) Math.ceil(range / speed);
		reloadTime = (reload < 1) ? 1 : reload; // limit to >= 1
	}

	public double getFiringCost() {
		return (damage + HardwareSpec.kBlasterDamageOverhead)
				* (HardwareSpec.kBlasterFiringCostPerDamage + range
						* HardwareSpec.kBlasterFiringCostPerRange + Math.pow(
						range, 2)
						* HardwareSpec.kBlasterFiringCostPerRangeSquared);
	}
}
