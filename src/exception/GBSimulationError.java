/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
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
