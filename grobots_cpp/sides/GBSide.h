// GBSide.h
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBSide_h
#define GBSide_h

#include "GBPlatform.h"
#include "GBColor.h"
#include "GBModel.h"
#include "GBTypes.h"
#include "GBMessages.h"
#include "GBScores.h"
#include <vector>

//identify files according to platform
#define USE_MAC_IO (MAC)
#if USE_MAC_IO
	#if MAC_OS_X
		#include <Carbon/Carbon.h>
	#else
		#include <Files.h>
	#endif

	typedef FSSpec GBFilename;
#else
	typedef std::string GBFilename;
#endif

class GBRobotType;

const int kSharedMemorySize = 1000;
const int kMaxSeedIDs = 20;

class GBSide : public GBModel {
	GBRobotType * types;
	GBRobotType * selected; // really a view property - could remove
	string name, author;
	long id;
	GBColor color;
// scores
	GBSideScores scores;
	GBScores cScores;
	//std::vector<GBSideScores *> scores;
	//std::vector<GBScores *> tscores;
    GBVector groupPosition;
// communications
	GBNumber sharedMemory[kSharedMemorySize];
	GBMessageQueue * msgQueues[kNumMessageChannels];
// seeding
	std::vector<long> seedIDs;
public:
	GBSide * next;
	GBFilename filename;
    GBPosition center;
public:
	GBSide();
	~GBSide();
	GBSide * Copy() const;
// accessors
	const string & Name() const;
	void SetName(const string & newname);
	const string & Author() const;
	void SetAuthor(const string & newauthor);
	long ID() const;
	void SetID(long newid);
	GBColor Color() const;
	void SetColor(const GBColor & newcolor);
	GBRobotType * GetFirstType() const;
	GBRobotType * GetType(long index) const;
	void SelectType(GBRobotType * which) const;
	GBRobotType * SelectedType() const;
	long SelectedTypeID() const;
	long GetTypeIndex(const GBRobotType * type) const;
	long CountTypes() const;
// adding/removing types
	void AddType(GBRobotType * type);
	void RemoveAllTypes();
// seeding
	void AddSeedID(long id);
	GBRobotType * GetSeedType(int index) const;
	int NumSeedTypes() const;
// operation
	void Reset(); // clear status info
	void ResetSampledStatistics();
// scoring
	void ReportRobot(GBEnergy biomass, GBEnergy construc, const GBPosition & where);
	void ReportDead(const GBEnergy en);
	void ReportKilled(const GBEnergy en);
	void ReportSuicide(const GBEnergy en);
	void ReportAutotrophy(const GBEnergy en);
	void ReportTheotrophy(const GBEnergy en);
	void ReportHeterotrophy(const GBEnergy en);
	void ReportCannibalism(const GBEnergy en);
	void ReportKleptotrophy(const GBEnergy en);
	GBSideScores & Scores();
	const GBSideScores & Scores() const;
	GBScores & TournamentScores();
	const GBScores & TournamentScores() const;
// counter
	long GetNewRobotNumber();
// communications
	GBNumber ReadSharedMemory(int addr) const;
	void WriteSharedMemory(GBNumber value, int addr);
	const GBMessage * ReceiveMessage(const int channel, const GBMessageNumber desiredMessageNum) const;
	void SendMessage(const GBMessage & value, const int channel);
	const GBMessageNumber NextMessageNumber(const int channel) const;
	int MessagesWaiting(const int channel, const GBMessageNumber next) const;

	static bool Better(const GBSide *a, const GBSide *b);
};


#endif
