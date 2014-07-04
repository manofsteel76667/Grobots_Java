package exception;

public class GBSimulationError extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5522339576919262515L;

	@Override
	public String toString() {
		return "unspecified simulation error";
	}
};
