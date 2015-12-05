/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

import exception.GBBrainError;

public class GBBadSymbolIndexError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7856782923738485816L;

	@Override
	public String toString() {
		return "invalid symbol index";
	}
	@Override
	public String getMessage() {
		return toString();
	}
}