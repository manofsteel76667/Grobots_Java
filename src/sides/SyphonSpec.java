package sides;

public class SyphonSpec extends HardwareItem {

	@Override
	public double Mass() {
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
	public double Cost() {
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

	public double Power() {
		return power;
	}

	public double Range() {
		return range;
	}

	public boolean HitsEnemies() {
		return hitsEnemies;
	}

	public void Set(double pwr, double rng, boolean newHitsEnemies) {
		power = Math.max(pwr, 0);
		range = Math.max(rng, 0);
		hitsEnemies = newHitsEnemies;
	}

}
