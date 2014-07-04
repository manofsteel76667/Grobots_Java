package simulation;


public class GBMessage {

	public static final int kMaxMessageLength = 8;

	double[] data;
	public int length;
	public int sequenceNum;

	public GBMessage() {
		sequenceNum = -1;
		data = new double[kMaxMessageLength];
	}

	public void SetMessageNumber(int num) {
		if (num < 0)
			throw new IllegalArgumentException("message number must be positive: " + num);
		sequenceNum = num;
	}

	public void AddDatum(double elt) {
		if (length >= kMaxMessageLength)
			throw new RuntimeException(
					"Attempting to make a message that's too long");
		data[length++] = elt;
	}

	public double Datum(int n) {
		if (n < 0 || n >= kMaxMessageLength)
			throw new IndexOutOfBoundsException("invalid message data index: " + n);
		return data[n];
	}

	public void ClearDatums() {
		length = 0;
	}

};
