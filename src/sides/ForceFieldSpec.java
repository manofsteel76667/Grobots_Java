package sides;

public class ForceFieldSpec extends HardwareItem {

	@Override
	public double Mass() {
		if (power > 0)
			return power
					* (HardwareSpec.kForceFieldMassPerPower + range
							* HardwareSpec.kForceFieldMassPerRange);
		else
			return 0;
	}

	@Override
	public double Cost() {
		if (power > 0)
			return power
					* (HardwareSpec.kForceFieldCostPerPower + range
							* HardwareSpec.kForceFieldCostPerRange);
		else
			return 0;
	}

	@Override
	public ForceFieldSpec clone() {
		ForceFieldSpec ret = new ForceFieldSpec();
		ret.power = power;
		ret.range = range;
		return ret;
	}

	public double power;
	public double range;

	public ForceFieldSpec() {
		super(0,0);
		power = 0;
		range = 0;
	}

	public double Power() {
		return power;
	}

	public double Range() {
		return range;
	}

	public void Set(double pwr, double rng) {
		power = Math.max(pwr, 0);
		range = Math.max(rng, 0);
	}

}
