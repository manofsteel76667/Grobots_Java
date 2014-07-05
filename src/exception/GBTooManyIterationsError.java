/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package exception;

public class GBTooManyIterationsError extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2200153704548616149L;

	@Override
	public String toString() {
		return "a loop had too many iterations";
	}
};
