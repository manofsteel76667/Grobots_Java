/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


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

