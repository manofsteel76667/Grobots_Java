package exception;

public class GBBadComputedValueError extends GBSimulationError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8523065272051789901L;

	public String ToString() {
		return "a computed value such as mass or cost was not reasonable";
	}
};
