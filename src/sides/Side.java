/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// Side.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package sides;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.awt.Color;

import exception.GBBrainError;
import exception.GBSimulationError;
import simulation.GBMessage;
import simulation.GBMessageQueue;
import support.FinePoint;
import support.GBRandomState;
import support.Model;

public class Side extends Model implements Comparable<Side> {

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

	public Side copy() {
		Side side = new Side();
		for (int i = 0; i < types.size(); ++i)
			side.AddType(new RobotType(types.get(i)));
		side.name = name;
		side.author = author;
		side.SetColor(GBRandomState.gRandoms.ColorNear(color,
				kSideCopyColorDistance));
		for (int id : seedIDs)
			side.seedIDs.add(id);
		// id, comm and scores are not copied
		return side;
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

	public String Name() {
		return name;
	}

	public void SetName(String newname) {
		name = newname;
		Changed();
	}

	public String Author() {
		if (author != null)
			return author;
		else
			return "";
	}

	public void SetAuthor(String newauthor) {
		author = newauthor;
		Changed();
	}

	public int ID() {
		return id;
	}

	public void SetID(int newid) {
		id = newid;
		Changed();
	}

	public Color Color() {
		return color;
	}

	public void SetColor(Color newcolor) {
		color = newcolor;
		Changed();
	}

	public RobotType GetType(int index) {
		if (index <= 0 || index > types.size())
			throw new IndexOutOfBoundsException("invalid type index: " + index);
		return types.get(index - 1);
	}

	// used by brains
	public int GetTypeIndex(RobotType type) {
		if (type == null)
			return 0;
		for (int i = 0; i < types.size(); ++i)
			if (type == types.get(i))
				return i + 1;
		return 0;
	}

	public int CountTypes() {
		return types.size();
	}

	public void AddType(RobotType type) {
		// adds type at end so they will stay in order
		if (type == null)
			throw new NullPointerException();
		type.Recalculate();
		types.add(type);
		type.SetID(types.size());
		Changed();
	}

	public void RemoveAllTypes() {
		types.clear();
		Changed();
	}

	public void AddSeedID(int id) {
		seedIDs.add(id);
	}

	public RobotType GetSeedType(int index) {
		if (index < 0)
			throw new GBSimulationError("type index must be positive: "
					+ index);
		if (seedIDs.isEmpty())
			return null;
		return GetType(seedIDs.get(index % seedIDs.size()));
	}

	public int NumSeedTypes() {
		return seedIDs.size();
	}

	public void Reset() {
		id = 0;
		scores.Reset();
		center.set(0, 0);
		groupPosition.set(0, 0);
		int i;
		for (i = 0; i < kSharedMemorySize; i++)
			sharedMemory[i] = 0;
		for (i = 0; i < GBMessageQueue.kNumMessageChannels; i++) {
			msgQueues[i] = null;
		}
		Changed();
	}

	public void ResetSampledStatistics() {
		if (scores.population != 0)
			center = center.add(groupPosition.divide(scores.Population()))
					.divide(2);
		groupPosition.set(0, 0);
		scores.ResetSampledStatistics();
		for (int i = 0; i < types.size(); ++i)
			types.get(i).ResetSampledStatistics();
		Changed();
	}

	public void ReportRobot(double biomass, RobotType type,
			support.FinePoint where) {
		HardwareSpec hw = type.Hardware();
		scores.ReportRobot(biomass, hw.constructor.Cost(), hw.GrowthCost(),
				hw.CombatCost(), hw.BaseCost());
		groupPosition = groupPosition.add(where);
		Changed();
	}

	public void ReportDead(double en) {
		scores.ReportDead(en);
		Changed();
	}

	public void ReportKilled(double en) {
		scores.ReportKilled(en);
		Changed();
	}

	public void ReportSuicide(double en) {
		scores.ReportSuicide(en);
		Changed();
	}

	public void ReportAutotrophy(double en) {
		scores.income.ReportAutotrophy(en);
		Changed();
	}

	public void ReportTheotrophy(double en) {
		scores.income.ReportTheotrophy(en);
		Changed();
	}

	public void ReportHeterotrophy(double en) {
		scores.income.ReportHeterotrophy(en);
		Changed();
	}

	public void ReportCannibalism(double en) {
		scores.income.ReportCannibalism(en);
		Changed();
	}

	public void ReportKleptotrophy(double en) {
		scores.income.ReportKleptotrophy(en);
		Changed();
	}

	public GBSideScores Scores() {
		return scores;
	}

	public GBScores TournamentScores() {
		return cScores;
	}

	public int GetNewRobotNumber() {
		return scores.GetNewRobotNumber();
	}

	public double ReadSharedMemory(int addr) {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBBrainError(
					"tried to read from shared memory at " + addr);
		return sharedMemory[addr - 1];
	}

	public void WriteSharedMemory(double value, int addr) {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBBrainError(
					"tried to write to shared memory at " + addr);
		sharedMemory[addr - 1] = value;
	}

	// Note: the pointer returned is to within an internal array and
	// must be used and discarded before any robot calls SendMessage again!
	public GBMessage ReceiveMessage(int channel, int desiredMessageNumber) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel "
					+ channel);
		if (msgQueues[channel - 1] == null)
			return null;
		return msgQueues[channel - 1].GetMessage(desiredMessageNumber);
	}

	public void SendMessage(GBMessage msg, int channel) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to send on channel "
					+ channel);
		if (msgQueues[channel - 1] == null) {
			msgQueues[channel - 1] = new GBMessageQueue();
		}
		msgQueues[channel - 1].AddMessage(msg);
	}

	public int NextMessageNumber(int channel) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel "
					+ channel);
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].NextMessageNumber();
	}

	public int MessagesWaiting(int channel, int next) {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBBrainError("tried to receive on channel "
					+ channel);
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].MessagesWaiting(next);
	}

	@Override
	public int compareTo(Side o) {
		// Replaces the Better function, used for sorting
		return (int) (o.TournamentScores().BiomassFraction() * 100 - TournamentScores()
				.BiomassFraction() * 100);
	}

}
