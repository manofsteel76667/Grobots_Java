package exception;

public class GBGenericError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8076070680579316759L;
	public String message;

	public GBGenericError(String msg) {
		message = msg;
	}

	public GBGenericError() {
	}

	public String ToString() {
		return message;
	}

};
