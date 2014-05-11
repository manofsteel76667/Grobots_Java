package sides;

public class ConstructorSpec implements Hardware<ConstructorSpec> {

	@Override
	public double Mass() {
		return rate * HardwareSpec.kConstructorMassPerRate;
	}

	@Override
	public double Cost() {
		return rate * HardwareSpec.kConstructorCostPerRate;
	}

	@Override
	public ConstructorSpec clone() {
		ConstructorSpec ret = new ConstructorSpec();
		ret.rate = rate;
		return ret;
	}

	public ConstructorSpec() {
		rate = 0;
	}

	private double rate;

	public double Rate() {
		return rate;
	}

	public void Set(double nrate) {
		rate = Math.max(nrate, 0);
	}

}
