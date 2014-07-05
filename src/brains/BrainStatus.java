/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

public enum BrainStatus {
	bsOK(0, "OK"), bsStopped(1, "Stopped"), // paused by user
	bsError(2, "Error"); // dead of error

	public final int value;
	public final String name;

	BrainStatus(int _value, String _name) {
		value = _value;
		name = _name;
	}
}
