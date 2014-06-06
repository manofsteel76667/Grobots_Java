// GBMessages.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBMessages.h"

const long kMaxMessageNumber = 2000000;

///// GBMessage /////

GBMessage::GBMessage()
	: length(0), sequenceNum(-1)
{}

GBMessage::~GBMessage() {}

void GBMessage::SetMessageNumber(const GBMessageNumber num) {
	if ( num < 0 )
		throw GBBadArgumentError();
	sequenceNum = num;
}

void GBMessage::AddDatum(const GBMessageElement elt) {
	if ( length >= kMaxMessageLength )
		throw GBGenericError("Attempting to make a message that's too long");
	data[length++] = elt;
}

GBMessageElement GBMessage::Datum(const int n) const {
	if ( n < 0 || n >= kMaxMessageLength )
		throw GBBadArgumentError();
	return data[n];
}

void GBMessage::ClearDatums() {
	length = 0;
}

int GBMessage::Length() const {
	return length;
}

GBMessageNumber GBMessage::SequenceNumber() const {
	return sequenceNum;
}

///// GBMessageQueue /////

GBMessageQueue::GBMessageQueue()
	: nextNumber(0)
{}

GBMessageQueue::~GBMessageQueue() {}

// called by Side::Reset()
void GBMessageQueue::Reset() {
	nextNumber = 0;
}

void GBMessageQueue::AddMessage(const GBMessage & newMess) {
	buffer[nextNumber % kMaxMessages] = newMess;
	buffer[nextNumber % kMaxMessages].SetMessageNumber(nextNumber);
	if ( ++nextNumber >= kMaxMessageNumber )
		throw GBGenericError("Message number got too high.");
}

const GBMessage * GBMessageQueue::GetMessage(const GBMessageNumber num) const {
	int potential = num % kMaxMessages;
	if ( buffer[potential].SequenceNumber() == num ) {
		return &(buffer[potential]);
	}
	// don't have the message they are looking for. Return the oldest we have, if it is older than requested.
	if ( buffer[nextNumber % kMaxMessages].SequenceNumber() > num ) {
		return &(buffer[nextNumber % kMaxMessages]);
	} else if ( num >= nextNumber ) // Are they asking for a message that hasn't appeared yet?
		return nil;
	else
		throw GBGenericError("Unexpected condition in GBMessageQueue::GetMessage()");
	return nil; // should not be reached
}

const GBMessageNumber GBMessageQueue::NextMessageNumber() const {
	return nextNumber;
}

int GBMessageQueue::MessagesWaiting(const GBMessageNumber next) const {
	if ( next >= nextNumber ) return 0;
	if ( next <= nextNumber - kMaxMessages ) return kMaxMessages;
	return nextNumber - next;
}

