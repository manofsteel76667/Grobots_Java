/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// Side.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package sides;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import simulation.GBMessage;
import simulation.GBMessageQueue;
import support.FinePoint;
import exception.GBBrainError;
import exception.GBSimulationError;

public class Side implements Comparable<Side> {

	public static final int kSharedMemorySize = 1000;
	public static final int kMaxSeedIDs = 20;
	public static final float kSideCopyColorDistance = 0.3f;

	public boolean debug = false;
	public List<RobotType> types;
	String name, author;
	int id;
	java.awt.Color color;
	// scores
	GBSideScores scores;
	GBScores cScores;
	FinePoint groupPosition;
	// communications
	double[] sharedMemory;
	GBMessageQueue[] msgQueues;
	// seeding
	List<Integer> seedIDs;
	// public:
	public String filename;
	public FinePoint center;

	public Side() {
		id = 0;
		seedIDs = new LinkedList<Integer>();
		color = Color.white;
		scores = new GBSideScores();
		cScores = new GBScores();
		center = new FinePoint();
		groupPosition = new FinePoint();
		sharedMemory = new double[Side.kSharedMemorySize];
		msgQueues = new GBMessageQueue[GBMessageQueue.kNumMessageChannels];
		types = new ArrayList<RobotType>();
		seedIDs = new ArrayList<Integer>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Side))
			return false;
		Side other = (Side) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String newname) {
		name = newname;
	}

	public String getAuthor() {
		if (author != null)
			return author;
		else
			return "";
	}

	public void getAuthor(String newauthor) {
		author = newauthor;
	}

	public int getID() {
		return id;
	}

	public void setID(int newid) {
		id = newid;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color newcolor) {
		color = newcolor;
	}

	public RobotType getRobotType(int index) {
		if (index <= 0 || index > types.size())
			throw new IndexOutOfBoundsException("invalid type index: " + index);
		return types.get(index - 1);
	}

	// used by brains
	public int getTypeIndex(RobotType type) {
		if (type == null)
			return 0;
		for (int i = 0; i < types.size(); ++i)
			if (type == types.get(i))
				return i + 1;
		return 0;
	}

	public int getTypeCount() {
		return types.size();
	}

	public void addType(RobotType type) {
		// adds type at end so they will stay in order
		if (type == null)
			throw new NullPointerException();
		type.recalculate();
		types.add(type);
		type.setID(types.size());
	}

	public void removeAllTypes() {
		types.clear();
	}

	public void addSeedID(int id) {
		seedIDs.add(id);
	}

	public RobotType getSeedType(int index) {
		if (index < 0)
			throw new GBSimulationError("type index must be positive: " + index);
		if (seedIDs.isEmpty())
			return null;
		return getRobotType(seedIDs.get(index % seedIDs.size()));
	}

	public int getNumSeedTypes() {
		return seedIDs.size();
	}

	public void reset() {
		id = 0;
		scores.reset();
		center.set(0, 0);
		groupPosition.set(0, 0);
		int i;
		for (i = 0; i < kSharedMemorySize; i++)
			sharedMemory[i] = 0;
		for (i = 0; i < GBMessageQueue.kNumMessageChannels; i++) {
			msgQueues[i] = null;
		}
	}

	public void resetSampledStatistics() {
		if (scores.population != 0)
			center = center.add(groupPosition.divide(scores.getPopulation()))
					.divide(2);
		groupPosition.set(0, 0);
		scores.resetSampledStatistics();
		for (int i = 0; i < types.size(); ++i)
			types.get(i).resetSampledStatistics();
	}

	public void reportRobot(double biomass, RobotType type,
			support.FinePoint where) {
		HardwareSpec hw = type.getHardware();
		scores.reportRobot(biomass, hw.constructor.getCost(), hw.GrowthCost(),
				hw.CombatCost(), hw.BaseCost());
		groupPosition = groupPosition.add(where);
	}

	public void reportDead(double en) {
		scores.reportDead(en);
	}

	public void reportKilled(double en) {
		scores.reportKilled(en);
	}

	public void reportSuicide(double en) {
		scores.reportSuicide(en);
	}

	public void reportAutotrophy(double en) {
		scores.income.reportAutotrophy(en);
	}

	public void reportTheotrophy(double en) {
		scores.income.reportTheotrophy(en);
	}

	public void reportHeterotrophy(double en) {
		scores.income.reportHeterotrophy(en);
	}

	public void reportCannibalism(double en) {
		scores.income.reportCannibalism(en);
	}

	public void reportKleptotrophy(double en) {
		scores.income.reportKleptotrophy(en);
	}

	public GBSideScores getScores() {
		return scores;
	}

	public GBScores getTournamentScores() {
		return cScores;
	}

	public int getNewRobotNumber() {
		return scores.getNewRobotNumber();
	}

	public double readSharedMemory(int addr) {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBBrainError("tried to read from shared memory at "
					+ addr);
		return sharedMemory[addr - 1];
	}

	public void writeSharedMemory(double value, int addr) {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBBrainError("tried to write to shared memory at " + addr);
		sharedMemory[addr - 1] = value;
	}

	// Note: the pointer returned is to within an internal array and
	// must be used and discarded before any robot calls SendMessage again!
	public GBMessage receiveMessage(int channel, int desiredMessageNumber) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel " + channel);
		if (msgQueues[channel - 1] == null)
			return null;
		return msgQueues[channel - 1].getMessage(desiredMessageNumber);
	}

	public void sendMessage(GBMessage msg, int channel) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to send on channel " + channel);
		if (msgQueues[channel - 1] == null) {
			msgQueues[channel - 1] = new GBMessageQueue();
		}
		msgQueues[channel - 1].addMessage(msg);
	}

	public int getNextMessageNumber(int channel) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel " + channel);
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].getNextMessageNumber();
	}

	public int getMessagesWaiting(int channel, int next) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel " + channel);
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].getMessagesWaiting(next);
	}

	@Override
	public int compareTo(Side o) {
		// Replaces the Better function, used for sorting
		return (int) (o.getTournamentScores().getBiomassFraction() * 100 - getTournamentScores()
				.getBiomassFraction() * 100);
	}

}
