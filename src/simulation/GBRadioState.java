/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.Side;
import exception.GBBrainError;

public class GBRadioState {
	// state
	// public:
	public static final int kRadioHistory = 12;
	public int[] writes;
	public int[] sent;
	// private:
	int[] nextMessage;

	// public:

	public GBRadioState() {
		writes = new int[kRadioHistory];
		sent = new int[kRadioHistory];
		nextMessage = new int[GBMessageQueue.kNumMessageChannels];

		for (int i = 0; i < GBMessageQueue.kNumMessageChannels; i++)
			nextMessage[i] = 0;
		for (int i = 0; i < kRadioHistory; ++i)
			sent[i] = writes[i] = 0;
	}

	public void write(double value, int address, Side side) {
		side.writeSharedMemory(value, address);
		writes[0] += 1;
	}

	public double read(int address, Side side) {
		return side.readSharedMemory(address);
	}

	public void send(GBMessage mess, int channel, Side side) {
		side.sendMessage(mess, channel);
		sent[0] += mess.length + 1; // note overhead
	}

	public int getMessagesWaiting(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel " + channel);
		return side.getMessagesWaiting(channel, nextMessage[channel - 1]);
	}

	public GBMessage receive(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel " + channel);
		GBMessage msg = side.receiveMessage(channel, nextMessage[channel - 1]);
		if (msg != null)
			nextMessage[channel - 1] = msg.sequenceNum + 1;
		return msg;
	}

	public void reset(Side side) {
		for (int i = 1; i <= GBMessageQueue.kNumMessageChannels; i++)
			clearChannel(i, side);
	}

	public void clearChannel(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to clear channel " + channel);
		nextMessage[channel - 1] = side.getNextMessageNumber(channel);
	}

	public void skipMessages(int channel, int skip, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to skip on channel " + channel);
		if (skip <= 0)
			return;
		nextMessage[channel - 1] += skip;
		if (nextMessage[channel - 1] > side.getNextMessageNumber(channel))
			nextMessage[channel - 1] = side.getNextMessageNumber(channel);
	}

	public void act(GBRobot robot, GBWorld world) {
		for (int i = kRadioHistory - 1; i > 0; --i) {
			sent[i] = sent[i - 1];
			writes[i] = writes[i - 1];
		}
		sent[0] = writes[0] = 0;
	}

}
