package simulation;

import exception.GBBadArgumentError;
import exception.GBGenericError;

public class GBMessageQueue {
	public static final int kMaxMessageNumber = 2000000;
	public static final int kMaxMessages = 50;
	public static final int kNumMessageChannels = 10;
	// private:
	GBMessage[] buffer;
	int nextNumber;

	// /// GBMessageQueue /////

	public GBMessageQueue() {
		buffer = new GBMessage[kMaxMessages];
		for (int i = 0; i < kMaxMessages; i++) {
			buffer[i] = new GBMessage();
		}
	}

	// called by Side::Reset()
	public void Reset() {
		nextNumber = 0;
	}

	public void AddMessage(GBMessage newMess) throws GBGenericError,
			GBBadArgumentError {
		buffer[(int) (nextNumber % kMaxMessages)] = newMess;
		buffer[(int) (nextNumber % kMaxMessages)].SetMessageNumber(nextNumber);
		if (++nextNumber >= kMaxMessageNumber)
			throw new GBGenericError("Message number got too high.");
	}

	public GBMessage GetMessage(int num) throws GBGenericError {
		int potential = (int) (num % kMaxMessages);
		if (buffer[potential].sequenceNum == num)
			return buffer[potential];
		// don't have the message they are looking for. Return the oldest we
		// have, if it is older than requested.
		if (buffer[(int) (nextNumber % kMaxMessages)].sequenceNum > num) {
			return buffer[(int) (nextNumber % kMaxMessages)];
		} else if (num >= nextNumber) // Are they asking for a message that
										// hasn't appeared yet?
			return null;
		else
			throw new GBGenericError("Unexpected condition in GetMessage()");
	}

	public int NextMessageNumber() {
		return nextNumber;
	}

	public int MessagesWaiting(int next) {
		if (next >= nextNumber)
			return 0;
		if (next <= nextNumber - kMaxMessages)
			return kMaxMessages;
		return (int) (nextNumber - next);
	}

};
