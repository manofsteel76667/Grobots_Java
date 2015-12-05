/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class ForceFieldSpec extends HardwareItem {

	@Override
	public double getMass() {
		if (power > 0)
			return power
					* (HardwareSpec.kForceFieldMassPerPower + range
							* HardwareSpec.kForceFieldMassPerRange);
		else
			return 0;
	}

	@Override
	public double getCost() {
		if (power > 0)
			return power
					* (HardwareSpec.kForceFieldCostPerPower + range
							* HardwareSpec.kForceFieldCostPerRange);
		else
			return 0;
	}

	public double power;
	public double range;

	public ForceFieldSpec() {
		super(0, 0);
		power = 0;
		range = 0;
	}

	public double getPower() {
		return power;
	}

	public double getRange() {
		return range;
	}

	public void set(double pwr, double rng) {
		power = Math.max(pwr, 0);
		range = Math.max(rng, 0);
	}

}
