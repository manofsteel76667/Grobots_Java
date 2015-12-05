/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class SyphonSpec extends HardwareItem {

	@Override
	public double getMass() {
		if (power > 0)
			return power
					* (hitsEnemies ? HardwareSpec.kEnemySyphonMassPerPower
							+ range * HardwareSpec.kEnemySyphonMassPerRange
							: HardwareSpec.kSyphonMassPerPower + range
									* HardwareSpec.kSyphonMassPerRange);
		else
			return 0;
	}

	@Override
	public double getCost() {
		if (power > 0)
			return power
					* (hitsEnemies ? HardwareSpec.kEnemySyphonCostPerPower
							+ range * HardwareSpec.kEnemySyphonCostPerRange
							: HardwareSpec.kSyphonCostPerPower + range
									* HardwareSpec.kSyphonCostPerRange);
		else
			return 0;
	}

	public SyphonSpec() {
		super(0, 0);
		power = 0;
		range = 1;
		hitsEnemies = true;
	}

	double power;
	double range;
	boolean hitsEnemies;

	public double getPower() {
		return power;
	}

	public double getRange() {
		return range;
	}

	public boolean getHitsEnemies() {
		return hitsEnemies;
	}

	public void set(double pwr, double rng, boolean newHitsEnemies) {
		power = Math.max(pwr, 0);
		range = Math.max(rng, 0);
		hitsEnemies = newHitsEnemies;
	}

}
