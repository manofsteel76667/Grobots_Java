// GBBrain.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBBrain.h"
#include "GBErrors.h"

const string statusNames[kNumBrainStatuses] = {
	"OK", "stopped", "crashed"
};

GBBrain::GBBrain()
	: status(bsOK)
{}

GBBrain::~GBBrain() {}

GBBrainStatus GBBrain::Status() const {
	return status;
}

void GBBrain::SetStatus(GBBrainStatus newStatus) {
	if ( status < 0 || status >= kNumBrainStatuses )
		throw GBGenericError("Bad brain status");
	status = newStatus;
}

string GBBrain::StatusName() const {
	return statusNames[status];
}

void GBBrain::Step(GBRobot * robot, GBWorld * world) {
	Think(robot, world);
}

