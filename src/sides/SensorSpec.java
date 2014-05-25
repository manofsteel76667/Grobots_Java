package sides;

import support.GBObjectClass;

public class SensorSpec extends HardwareItem {

	@Override
	public double Mass() {
		if (seen != GBObjectClass.ocDead.value)
			return range
					* (HardwareSpec.kSensorMassPerRange + range
							* HardwareSpec.kSensorMassPerPower)
					+ HardwareSpec.kSensorMassPerResult * numResults;
		else
			return 0;
	}

	@Override
	public double Cost() {
		if (seen != GBObjectClass.ocDead.value)
			return range
					* (HardwareSpec.kSensorCostPerRange + range
							* HardwareSpec.kSensorCostPerPower)
					+ HardwareSpec.kSensorCostPerResult * numResults;
		else
			return 0;
	}

	@Override
	public SensorSpec clone() {
		SensorSpec ret = new SensorSpec();
		ret.range = range;
		ret.numResults = numResults;
		ret.seen = seen;
		return ret;
	}

	public SensorSpec() {
		super(0,0);
		range = 0;
		numResults = 1;
		seen = GBObjectClass.ocDead.value;
	}

	private int numResults;
	private double range;
	private int seen;

	public double Range() {
		return range;
	}

	public int NumResults() {
		return numResults;
	}

	public int Seen() {
		return seen;
	}

	public void Set(double rng, int rslts, int what) {
		range = Math.max(rng, 0);
		numResults = rslts < 1 ? 1 : rslts;
		seen = what;
	}

	public double FiringCost() {
		return range * range * HardwareSpec.kSensorFiringCostPerPower;
	}

}
