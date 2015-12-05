/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import exception.GBBrainError;

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
	public void reset() {
		nextNumber = 0;
	}

	public void addMessage(GBMessage newMess) {
		buffer[nextNumber % kMaxMessages] = newMess;
		buffer[nextNumber % kMaxMessages].setMessageNumber(nextNumber);
		if (++nextNumber >= kMaxMessageNumber)
			throw new GBBrainError("Message number got too high.");
	}

	public GBMessage getMessage(int num) {
		int potential = num % kMaxMessages;
		if (buffer[potential].sequenceNum == num)
			return buffer[potential];
		// don't have the message they are looking for. Return the oldest we
		// have, if it is older than requested.
		if (buffer[nextNumber % kMaxMessages].sequenceNum > num) {
			return buffer[nextNumber % kMaxMessages];
		} else if (num >= nextNumber) // Are they asking for a message that
										// hasn't appeared yet?
			return null;
		else
			throw new GBBrainError("Unexpected condition in GetMessage()");
	}

	public int getNextMessageNumber() {
		return nextNumber;
	}

	public int getMessagesWaiting(int next) {
		if (next >= nextNumber)
			return 0;
		if (next <= nextNumber - kMaxMessages)
			return kMaxMessages;
		return nextNumber - next;
	}

};
