package simulation;

import sides.Side;
import exception.GBBadArgumentError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBOutOfMemoryError;

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

	public void Write(double value, int address, Side side)
			throws GBIndexOutOfRangeError {
		side.WriteSharedMemory(value, address);
		writes[0] += 1;
	}

	public double Read(int address, Side side) throws GBIndexOutOfRangeError {
		return side.ReadSharedMemory(address);
	}

	public void Send(GBMessage mess, int channel, Side side)
			throws GBOutOfMemoryError, GBGenericError, GBBadArgumentError {
		side.SendMessage(mess, channel);
		sent[0] += mess.length + 1; // note overhead
	}

	public int MessagesWaiting(int channel, Side side)
			throws GBIndexOutOfRangeError {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		return side.MessagesWaiting(channel, nextMessage[channel - 1]);
	}

	public GBMessage Receive(int channel, Side side)
			throws GBIndexOutOfRangeError, GBGenericError {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		GBMessage msg = side.ReceiveMessage(channel, nextMessage[channel - 1]);
		if (msg != null)
			nextMessage[channel - 1] = msg.sequenceNum + 1;
		return msg;
	}

	public void Reset(Side side) throws GBIndexOutOfRangeError {
		for (int i = 1; i <= GBMessageQueue.kNumMessageChannels; i++)
			ClearChannel(i, side);
	}

	public void ClearChannel(int channel, Side side)
			throws GBIndexOutOfRangeError {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		nextMessage[channel - 1] = side.NextMessageNumber(channel);
	}

	public void SkipMessages(int channel, int skip, Side side)
			throws GBIndexOutOfRangeError {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		if (skip <= 0)
			return;
		nextMessage[channel - 1] += skip;
		if (nextMessage[channel - 1] > side.NextMessageNumber(channel))
			nextMessage[channel - 1] = side.NextMessageNumber(channel);
	}

	public void Act(GBRobot robot, GBWorld world) {
		for (int i = kRadioHistory - 1; i > 0; --i) {
			sent[i] = sent[i - 1];
			writes[i] = writes[i - 1];
		}
		sent[0] = writes[0] = 0;
	}

}