package brains;

import exception.*;
import sides.Hardware;

// GBBrainSpec.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

public class BrainSpec implements Hardware<BrainSpec> {
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

	public void Check() throws GBError {
	} // check OK to use

	public BrainSpec() {
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

	public String ToString() {
		return "unspecified brain error";
	}
};

class GBUnknownInstructionError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2958382950648962574L;

	public String ToString() {
		return "illegal or unimplemented instruction";
	}
};

class GBUnknownHardwareVariableError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2173486710426250517L;

	public String ToString() {
		return "illegal or unimplemented hardware variable";
	}
};

class GBBadSymbolIndexError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7856782923738485816L;

	public String ToString() {
		return "invalid symbol index";
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

	public String ToString() {
		return Double.toString(value) + " is not an integer";
	}
};

class GBReadOnlyError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -252704304831425647L;

	public String ToString() {
		return "tried to write a read-only variable";
	}
};
