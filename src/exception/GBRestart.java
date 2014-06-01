package exception;

public class GBRestart extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -420647005436289612L;

	public GBRestart() {
	}

	@Override
	public String toString() {
		return "unspecified restart";
	}
};