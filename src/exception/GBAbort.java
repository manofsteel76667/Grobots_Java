/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package exception;

public class GBAbort extends RuntimeException {
	/**
	 * Pseudo-exception for nonlocal exit.
	 */
	private static final long serialVersionUID = -2930047027900506003L;

	@Override
	public String toString() {
		return "Aborted";
	}

	@Override
	public String getMessage() {
		return toString();
	}
};
