/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package exception;

public class GBBadObjectClassError extends GBSimulationError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8325790684915968056L;

	@Override
	public String toString() {
		return "bad GBObjectClass";
	}
};