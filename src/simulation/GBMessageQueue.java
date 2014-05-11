package simulation;
import exception.*;

public class GBMessageQueue {
	public static final long kMaxMessageNumber = 2000000;
	public static final int kMaxMessages = 50;
	public static final int kNumMessageChannels = 10;
	// private:
	GBMessage[] buffer;
	long nextNumber;

	// /// GBMessageQueue /////

	public GBMessageQueue() {
		buffer = new GBMessage[kMaxMessages];
	}

	// called by Side::Reset()
	public void Reset() {
		nextNumber = 0;
	}

	public void AddMessage(GBMessage newMess) throws GBError {
		buffer[(int) (nextNumber % kMaxMessages)] = newMess;
		buffer[(int) (nextNumber % kMaxMessages)].SetMessageNumber(nextNumber);
		if (++nextNumber >= kMaxMessageNumber)
			throw new GBGenericError("Message number got too high.");
	}

	public GBMessage GetMessage(long num) throws GBError {
		int potential = (int) (num % kMaxMessages);
		if (buffer[potential].sequenceNum == num) {
			return buffer[potential];
		}
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

	public long NextMessageNumber() {
		return nextNumber;
	}

	public int MessagesWaiting(long next) {
		if (next >= nextNumber)
			return 0;
		if (next <= nextNumber - kMaxMessages)
			return kMaxMessages;
		return (int) (nextNumber - next);
	}

};
