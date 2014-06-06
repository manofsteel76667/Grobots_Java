// GBSide.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBSide.h"
#include "GBRobotType.h"
#include "GBErrors.h"
#include "GBRandomState.h"


const float kSideCopyColorDistance = 0.3f;

GBSide::GBSide()
	: types(nil), selected(nil),
	id(0),
	seedIDs(),
	color(), name(), author(),
	scores(), cScores(), center(), groupPosition(),
	next(nil)
{
	int i;
	for ( i = 0; i < kSharedMemorySize; i ++ )
		sharedMemory[i] = 0;
	for ( i = 0; i < kNumMessageChannels; i ++ )
		msgQueues[i] = nil;
}

GBSide::~GBSide() {
	RemoveAllTypes();
	for ( int i = 0; i < kNumMessageChannels; i ++ )
		delete msgQueues[i];
}

GBSide * GBSide::Copy() const {
	GBSide * side = new GBSide();
	for ( GBRobotType * cur = types; cur != nil; cur = cur->next )
		side->AddType(cur->Copy(side));
	side->name = name;
	side->author = author;
	side->SetColor(gRandoms.ColorNear(color, kSideCopyColorDistance));
	side->seedIDs = seedIDs;
// id, comm and scores are not copied
	return side;
}

const string & GBSide::Name() const {
	return name;
}

void GBSide::SetName(const string & newname) {
	name = newname;
	Changed();
}

const string & GBSide::Author() const {
	return author;
}

void GBSide::SetAuthor(const string & newauthor) {
	author = newauthor;
	Changed();
}

long GBSide::ID() const {
	return id;
}

void GBSide::SetID(long newid) {
	id = newid;
	Changed();
}

GBColor GBSide::Color() const {
	return color;
}

void GBSide::SetColor(const GBColor & newcolor) {
	color = newcolor;
	Changed();
}

GBRobotType * GBSide::GetFirstType() const {
	return types;
}

GBRobotType * GBSide::GetType(long index) const {
	if ( index <= 0 ) throw GBIndexOutOfRangeError();
	GBRobotType * type = types;
	for ( long i = 1; i < index; i ++ )
		if ( type->next )
			type = type->next;
		else
			throw GBIndexOutOfRangeError();
	return type;
}

void GBSide::SelectType(GBRobotType * which) const {
	if ( selected != which ) {
		const_cast<GBSide *>(this)->selected = which;
		const_cast<GBSide *>(this)->Changed();
	}
}

GBRobotType * GBSide::SelectedType() const {
	return selected;
}

long GBSide::SelectedTypeID() const {
	return selected ? selected->ID() : 0;
}

// used by brains
long GBSide::GetTypeIndex(const GBRobotType * type) const {
	if ( ! type )
		return 0;
	long index = 1;
	for ( GBRobotType * cur = types; cur; cur = cur->next ) {
		if ( type == cur )
			return index;
		index ++;
	}
	return 0;
}

long GBSide::CountTypes() const {
	long index = 0;
	for ( GBRobotType * cur = types; cur; cur = cur->next )
		index ++;
	return index;
}

void GBSide::AddType(GBRobotType * type) {
// adds type at end so they will stay in order
	if ( ! type )
		throw GBNilPointerError();
	type->next = nil;
	type->Recalculate();
	if ( types == nil ) {
		types = type;
		type->SetID(1);
	} else {
		long curid = 2;
		GBRobotType * cur;
		for ( cur = types; cur->next != nil; cur = cur->next )
			curid ++;
		cur->next = type;
		type->SetID(curid);
	}
	Changed();
}

void GBSide::RemoveAllTypes() {
	GBRobotType * temp;
	for ( GBRobotType * cur = types; cur != nil; cur = temp ) {
		temp = cur->next;
		delete cur;
	}
	types = nil;
	Changed();
}

void GBSide::AddSeedID(long id) {
	seedIDs.push_back(id);
}

GBRobotType * GBSide::GetSeedType(int index) const {
	if ( index < 0 ) throw GBBadArgumentError();
	if ( seedIDs.empty() ) return nil;
	return GetType(seedIDs[index % seedIDs.size()]);
}

int GBSide::NumSeedTypes() const {
	return seedIDs.size();
}

void GBSide::Reset() {
	id = 0;
	scores.Reset();
	center.Set(0, 0);
	groupPosition.Set(0, 0);
	int i;
	for ( i = 0; i < kSharedMemorySize; i ++ )
		sharedMemory[i] = 0;
	for ( i = 0; i < kNumMessageChannels; i++ ) {
		delete msgQueues[i];
		msgQueues[i] = nil;
	}
	Changed();
}

void GBSide::ResetSampledStatistics() {
	if (scores.Population())
		center = (center + groupPosition / scores.Population()) / 2;
	groupPosition.Set(0, 0);
	scores.ResetSampledStatistics();
	for ( GBRobotType * cur = types; cur != nil; cur= cur -> next ) {
		cur->ResetSampledStatistics();
	}
	Changed();
}

void GBSide::ReportRobot(GBEnergy biomass, GBEnergy construc, const GBPosition & where) {
	scores.ReportRobot(biomass, construc);
	groupPosition += where;
	Changed();
}

void GBSide::ReportDead(const GBEnergy en) {
	scores.ReportDead(en);
	Changed();
}

void GBSide::ReportKilled(const GBEnergy en) {
	scores.ReportKilled(en);
	Changed();
}

void GBSide::ReportSuicide(const GBEnergy en) {
	scores.ReportSuicide(en);
	Changed();
}

void GBSide::ReportAutotrophy(const GBEnergy en) {
	scores.Income().ReportAutotrophy(en);
	Changed();
}

void GBSide::ReportTheotrophy(const GBEnergy en) {
	scores.Income().ReportTheotrophy(en);
	Changed();
}

void GBSide::ReportHeterotrophy(const GBEnergy en) {
	scores.Income().ReportHeterotrophy(en);
	Changed();
}

void GBSide::ReportCannibalism(const GBEnergy en) {
	scores.Income().ReportCannibalism(en);
	Changed();
}

void GBSide::ReportKleptotrophy(const GBEnergy en) {
	scores.Income().ReportKleptotrophy(en);
	Changed();
}

GBSideScores & GBSide::Scores() { return scores; }
const GBSideScores & GBSide::Scores() const { return scores; }

GBScores & GBSide::TournamentScores() { return cScores; }
const GBScores & GBSide::TournamentScores() const { return cScores; }

long GBSide::GetNewRobotNumber() {
	return scores.GetNewRobotNumber();
}

GBNumber GBSide::ReadSharedMemory(int addr) const {
	if ( addr < 1 || addr > kSharedMemorySize )
		throw GBIndexOutOfRangeError();
	return sharedMemory[addr - 1];
}

void GBSide::WriteSharedMemory(GBNumber value, int addr) {
	if ( addr < 1 || addr > kSharedMemorySize )
		throw GBIndexOutOfRangeError();
	sharedMemory[addr - 1] = value;
}

//Note: the pointer returned is to within an internal array and
// must be used and discarded before any robot calls SendMessage again!
const GBMessage * GBSide::ReceiveMessage(const int channel, const GBMessageNumber desiredMessageNumber) const {
	if ( channel < 1 || channel > kNumMessageChannels )
		throw GBIndexOutOfRangeError();
	if ( ! msgQueues[channel - 1] )
		return nil;
	return msgQueues[channel - 1]->GetMessage(desiredMessageNumber);
}

void GBSide::SendMessage(const GBMessage & msg, const int channel) {
	if ( channel < 1 || channel > kNumMessageChannels )
		throw GBIndexOutOfRangeError();
	if ( ! msgQueues[channel - 1] ) {
		msgQueues[channel - 1] = new GBMessageQueue();
		if (! msgQueues[channel - 1])
			throw GBOutOfMemoryError();
	}
	msgQueues[channel - 1]->AddMessage(msg);
}

const GBMessageNumber GBSide::NextMessageNumber(const int channel) const {
	if ( channel < 1 || channel > kNumMessageChannels )
		throw GBIndexOutOfRangeError();
	if ( ! msgQueues[channel - 1] )
		return 0;
	return msgQueues[channel - 1]->NextMessageNumber();
}

int GBSide::MessagesWaiting(const int channel, const GBMessageNumber next) const {
	if ( channel < 1 || channel > kNumMessageChannels )
		throw GBIndexOutOfRangeError();
	if ( ! msgQueues[channel - 1] ) return 0;
	return msgQueues[channel - 1]->MessagesWaiting(next);
}

//for sorting
bool GBSide::Better(const GBSide *a, const GBSide *b) {
	return a->TournamentScores().BiomassFraction() > b->TournamentScores().BiomassFraction();
}

