package simulation;

import exception.GBBadArgumentError;
import exception.GBGenericError;

public class GBMessage {

	public static final int kMaxMessageLength = 8;

	double[] data;
	public int length;
	public int sequenceNum;

	public GBMessage() {
		sequenceNum = -1;
		data = new double[kMaxMessageLength];
	}

	public void SetMessageNumber(int num) throws GBBadArgumentError {
		if (num < 0)
			throw new GBBadArgumentError();
		sequenceNum = num;
	}

	public void AddDatum(double elt) throws GBGenericError {
		if (length >= kMaxMessageLength)
			throw new GBGenericError(
					"Attempting to make a message that's too long");
		data[length++] = elt;
	}

	public double Datum(int n) throws GBBadArgumentError {
		if (n < 0 || n >= kMaxMessageLength)
			throw new GBBadArgumentError();
		return data[n];
	}

	public void ClearDatums() {
		length = 0;
	}

};
