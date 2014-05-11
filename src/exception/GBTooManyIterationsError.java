package exception;

public class GBTooManyIterationsError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2200153704548616149L;

	public String ToString() {
		return "a loop had too many iterations";
	}
};