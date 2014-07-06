/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

public class ConstructorSpec extends HardwareItem {

	@Override
	public double Mass() {
		return rate * HardwareSpec.kConstructorMassPerRate;
	}

	@Override
	public double Cost() {
		return rate * HardwareSpec.kConstructorCostPerRate;
	}

	public ConstructorSpec() {
		super(0, 0);
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
