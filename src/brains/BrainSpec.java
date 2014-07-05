/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


package brains;

import sides.HardwareItem;
import exception.GBSimulationError;

public class BrainSpec extends HardwareItem {
	public Brain MakeBrain() {
		return null;
	};

	@Override
	public BrainSpec clone() {
		return null;
	}

	// computed
	@Override
	public double Cost() {
		return 0;
	}

	@Override
	public double Mass() {
		return 0;
	}

	// loading
	public void ParseLine(String line, short lineNum) {
	};

	public void Check() {
	} // check OK to use

	public BrainSpec() {
		super(0, 0);
	}

}

// errors //

class GBBrainError extends GBSimulationError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2788018746426283330L;

	public GBBrainError() {
	}

	@Override
	public String toString() {
		return "unspecified brain error";
	}
};

class GBUnknownInstructionError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2958382950648962574L;

	@Override
	public String toString() {
		return "illegal or unimplemented instruction";
	}
};

class GBUnknownHardwareVariableError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2173486710426250517L;

	@Override
	public String toString() {
		return "illegal or unimplemented hardware variable";
	}
};

class GBNotIntegerError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8617608974693090845L;
	double value;

	public GBNotIntegerError(double value2) {
		value = value2;
	}

	@Override
	public String toString() {
		return Double.toString(value) + " is not an integer";
	}
};

class GBReadOnlyError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -252704304831425647L;

	@Override
	public String toString() {
		return "tried to write a read-only variable";
	}
};
