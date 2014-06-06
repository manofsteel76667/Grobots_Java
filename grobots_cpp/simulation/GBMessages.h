// GBMessages.h
// Messages between brains and queue for storing them.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef _GBMessages_h
#define _GBMessages_h

#include "GBTypes.h"

const int kNumMessageChannels = 10;
const int kMaxMessageLength = 8;
const int kMaxMessages = 50;
typedef GBNumber GBMessageElement;
typedef long GBMessageNumber;

class GBMessage {
private:
	GBMessageElement data[kMaxMessageLength];
	int length;
	GBMessageNumber sequenceNum;
public:
	GBMessage();
	~GBMessage();
	void SetMessageNumber(const GBMessageNumber num);
	void AddDatum(const GBMessageElement elt);
	GBMessageElement Datum(const int n) const;
	void ClearDatums();
	int Length() const;
	GBMessageNumber SequenceNumber() const;
};

class GBMessageQueue {
private:
	GBMessage buffer[kMaxMessages];
	GBMessageNumber nextNumber;
public:
	GBMessageQueue();
	~GBMessageQueue();
	void AddMessage(const GBMessage & newMess);
	const GBMessage * GetMessage(const GBMessageNumber num) const;
	const GBMessageNumber NextMessageNumber() const;
	int MessagesWaiting(const GBMessageNumber next) const;
	void Reset();
};

#endif
