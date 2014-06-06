// GBBrain.h
// abstract brain class
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBBrain_h
#define GBBrain_h

#include "GBTypes.h"


class GBRobot;
class GBWorld;


typedef enum {
	bsOK = 0,
	bsStopped, // paused by user
	bsError, // dead of error
	kNumBrainStatuses
} GBBrainStatus;


class GBBrain {
	GBBrainStatus status;
public:
	GBBrain();
	virtual ~GBBrain();
	
	GBBrainStatus Status() const;
	void SetStatus(GBBrainStatus newStatus);
	string StatusName() const;
	virtual void Think(GBRobot * robot, GBWorld * world) = 0; // think one frame
	virtual void Step(GBRobot * robot, GBWorld * world); // think one step
	virtual bool Ready() const = 0; // can we think now?
	
};

#endif
