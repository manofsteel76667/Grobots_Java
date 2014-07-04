package exception;

public class GBAbort extends RuntimeException {
	/**
	 * Pseudo-exception for nonlocal exit.
	 */
	private static final long serialVersionUID = -2930047027900506003L;

	@Override
	public String toString() {
		return "abort";
	}
};
