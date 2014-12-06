package exception;

public class GBBrainError extends GBSimulationError {
	/**
	 * Root class for all in-game errors. To avoid proliferation of custom
	 * exception classes, most game errors (other than brain errors) will just
	 * use the base class and change the message as needed.
	 */
	private static final long serialVersionUID = -2788018746426283330L;

	public GBBrainError() {
	}

	public GBBrainError(String _message) {
		message = _message;
	}

	@Override
	public String toString() {
		if (message != null)
			return message;
		else
			return "unspecified brain error";
	}

	@Override
	public String getMessage() {
		return toString();
	}
}