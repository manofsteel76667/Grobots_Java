/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package exception;

public class GBSimulationError extends RuntimeException {
	/**
	 * Base class for several nonfatal simulation errors
	 */
	private static final long serialVersionUID = -5522339576919262515L;

	public GBSimulationError() {
	}

	public GBSimulationError(String string) {
		message = string;
	}

	String message;

	@Override
	public String toString() {
		if (message != null)
			return message;
		else
			return "unspecified simulation error";
	}

	@Override
	public String getMessage() {
		return this.getClass().getName() + ": " + toString();
	}
};
