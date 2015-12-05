/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

import simulation.GBObjectClass;

public class SensorSpec extends HardwareItem {

	@Override
	public double getMass() {
		if (seen != GBObjectClass.ocDead.value)
			return range
					* (HardwareSpec.kSensorMassPerRange + range
							* HardwareSpec.kSensorMassPerPower)
					+ HardwareSpec.kSensorMassPerResult * numResults;
		else
			return 0;
	}

	@Override
	public double getCost() {
		if (seen != GBObjectClass.ocDead.value)
			return range
					* (HardwareSpec.kSensorCostPerRange + range
							* HardwareSpec.kSensorCostPerPower)
					+ HardwareSpec.kSensorCostPerResult * numResults;
		else
			return 0;
	}

	public SensorSpec() {
		super(0, 0);
		range = 0;
		numResults = 1;
		seen = GBObjectClass.ocDead.value;
	}

	private int numResults;
	private double range;
	private int seen;

	public double getRange() {
		return range;
	}

	public int getNumResults() {
		return numResults;
	}

	public int getSeen() {
		return seen;
	}

	public void set(double rng, int rslts, int what) {
		range = Math.max(rng, 0);
		numResults = rslts < 1 ? 1 : rslts;
		seen = what;
	}

	public double getFiringCost() {
		return range * range * HardwareSpec.kSensorFiringCostPerPower;
	}

}
