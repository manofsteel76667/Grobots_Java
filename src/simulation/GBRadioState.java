package simulation;

import sides.Side;

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

	public void Write(double value, int address, Side side) {
		side.WriteSharedMemory(value, address);
		writes[0] += 1;
	}

	public double Read(int address, Side side) {
		return side.ReadSharedMemory(address);
	}

	public void Send(GBMessage mess, int channel, Side side) {
		side.SendMessage(mess, channel);
		sent[0] += mess.length + 1; // note overhead
	}

	public int MessagesWaiting(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new IndexOutOfBoundsException("tried to receive on channel " + channel);
		return side.MessagesWaiting(channel, nextMessage[channel - 1]);
	}

	public GBMessage Receive(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new IndexOutOfBoundsException("tried to receive on channel " + channel);
		GBMessage msg = side.ReceiveMessage(channel, nextMessage[channel - 1]);
		if (msg != null)
			nextMessage[channel - 1] = msg.sequenceNum + 1;
		return msg;
	}

	public void Reset(Side side) {
		for (int i = 1; i <= GBMessageQueue.kNumMessageChannels; i++)
			ClearChannel(i, side);
	}

	public void ClearChannel(int channel, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new IndexOutOfBoundsException("tried to clear channel " + channel);
		nextMessage[channel - 1] = side.NextMessageNumber(channel);
	}

	public void SkipMessages(int channel, int skip, Side side) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new IndexOutOfBoundsException("tried to skip on channel " + channel);
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
