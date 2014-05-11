// Side.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package sides;

import java.util.*;

import simulation.*;
import support.*;
import exception.*;

public class Side extends Model implements Comparable<Side> {

	public static final int kSharedMemorySize = 1000;
	public static final int kMaxSeedIDs = 20;
	public static final float kSideCopyColorDistance = 0.3f;

	LinkedList<RobotType> types;
	RobotType selected; // really a view property - could remove
	String name, author;
	long id;
	// GBColor color;
	// scores
	GBSideScores scores;
	GBScores cScores;
	// LinkedList<GBSideScores> scores;
	// LinkedList<GBScores> tscores;
	support.FinePoint groupPosition;
	// communications
	double[] sharedMemory;
	GBMessageQueue[] msgQueues;
	// seeding
	LinkedList<Long> seedIDs;
	// public:
	String filename;
	support.FinePoint center;

	public Side() {
		id = 0;
		seedIDs = new LinkedList<Long>();
		// color(), name(), author(),
		scores = new GBSideScores();
		cScores = new GBScores();
		center = new FinePoint();
		groupPosition = new FinePoint();
		sharedMemory = new double[Side.kSharedMemorySize];
		msgQueues = new GBMessageQueue[GBMessageQueue.kNumMessageChannels];
		types = new LinkedList<RobotType>();
		seedIDs = new LinkedList<Long>();
	}

	public Side copy() throws GBBadComputedValueError, GBNilPointerError  {
		Side side = new Side();
		for (int i = 0; i < types.size(); ++i)
			side.AddType(types.get(i).clone());
		side.name = name;
		side.author = author;
		// side.SetColor(gRandoms.ColorNear(color, kSideCopyColorDistance));
		for (Long id : seedIDs)
			side.seedIDs.add(id);
		// id, comm and scores are not copied
		return side;
	}

	public String Name() {
		return name;
	}

	public void SetName(String newname) {
		name = newname;
		Changed();
	}

	public String Author() {
		return author;
	}

	public void SetAuthor(String newauthor) {
		author = newauthor;
		Changed();
	}

	public long ID() {
		return id;
	}

	public void SetID(long newid) {
		id = newid;
		Changed();
	}

	/*
	 * public GBColor Color() { return color; }
	 * 
	 * public void SetColor(GBColor & newcolor) { color = newcolor; Changed(); }
	 */

	public RobotType GetType(long index) throws GBIndexOutOfRangeError {
		if (index <= 0 || index > types.size())
			throw new GBIndexOutOfRangeError();
		return types.get((int) index - 1);
	}

	public void SelectType(RobotType which) {
		if (!selected.equals(which)) {
			selected = which;
			Changed();
		}
	}

	public RobotType SelectedType() {
		return selected;
	}

	public long SelectedTypeID() {
		return selected == null ? selected.ID() : 0;
	}

	// used by brains
	public long GetTypeIndex(RobotType type) {
		if (type == null)
			return 0;
		for (int i = 0; i < types.size(); ++i)
			if (type == types.get(i))
				return i + 1;
		return 0;
	}

	public long CountTypes() {
		return types.size();
	}

	public void AddType(RobotType type) throws GBNilPointerError, GBBadComputedValueError  {
		// adds type at end so they will stay in order
		if (type == null)
			throw new GBNilPointerError();
		type.Recalculate();
		types.add(type);
		type.SetID(types.size());
		Changed();
	}

	public void RemoveAllTypes() {
		types.clear();
		Changed();
	}

	public void AddSeedID(long id) {
		seedIDs.add(id);
	}

	public RobotType GetSeedType(int index) throws GBBadArgumentError {
		if (index < 0)
			throw new GBBadArgumentError();
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
			center.add(groupPosition.divide((int) scores.Population())).divide(
					2);
		// center = (center + groupPosition / (int)scores.Population()) / 2;
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
		groupPosition.add(where);
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

	public long GetNewRobotNumber() {
		return scores.GetNewRobotNumber();
	}

	public double ReadSharedMemory(int addr) throws java.lang.Exception {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBIndexOutOfRangeError();
		return sharedMemory[addr - 1];
	}

	public void WriteSharedMemory(double value, int addr)
			throws java.lang.Exception {
		if (addr < 1 || addr > kSharedMemorySize)
			throw new GBIndexOutOfRangeError();
		sharedMemory[addr - 1] = value;
	}

	// Note: the pointer returned is to within an internal array and
	// must be used and discarded before any robot calls SendMessage again!
	public GBMessage ReceiveMessage(int channel, long desiredMessageNumber)
			throws java.lang.Exception {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		if (msgQueues[channel - 1] == null)
			return null;
		return msgQueues[channel - 1].GetMessage(desiredMessageNumber);
	}

	public void SendMessage(GBMessage msg, int channel)
			throws java.lang.Exception {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		if (msgQueues[channel - 1] == null) {
			msgQueues[channel - 1] = new GBMessageQueue();
			if (msgQueues[channel - 1] == null)
				throw new GBOutOfMemoryError();
		}
		msgQueues[channel - 1].AddMessage(msg);
	}

	public long NextMessageNumber(int channel) throws java.lang.Exception {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].NextMessageNumber();
	}

	public int MessagesWaiting(int channel, long next)
			throws java.lang.Exception {
		if (channel < 1 || channel > GBMessageQueue.kNumMessageChannels)
			throw new GBIndexOutOfRangeError();
		if (msgQueues[channel - 1] == null)
			return 0;
		return msgQueues[channel - 1].MessagesWaiting(next);
	}

	/*
	 * //for sorting public boolean Better(Side a, Side b) { return
	 * a.TournamentScores().BiomassFraction() >
	 * b.TournamentScores().BiomassFraction(); }
	 */

	@Override
	public int compareTo(Side o) {
		// Replaces the Better function, used for sorting
		return (int) (TournamentScores().BiomassFraction() - o
				.TournamentScores().BiomassFraction());
	}

}
