/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import exception.GBBrainError;

public class GBMessage {

	public static final int kMaxMessageLength = 8;

	double[] data;
	public int length;
	public int sequenceNum;

	public GBMessage() {
		sequenceNum = -1;
		data = new double[kMaxMessageLength];
	}

	public void setMessageNumber(int num) {
		if (num < 0)
			throw new GBBrainError("message number must be positive: " + num);
		sequenceNum = num;
	}

	public void addDatum(double elt) {
		if (length >= kMaxMessageLength)
			throw new GBBrainError(
					"Attempting to make a message that's too long");
		data[length++] = elt;
	}

	public double getDatum(int n) {
		if (n < 0 || n >= kMaxMessageLength)
			throw new GBBrainError("invalid message data index: " + n);
		return data[n];
	}

	public void clearDatums() {
		length = 0;
	}

};
