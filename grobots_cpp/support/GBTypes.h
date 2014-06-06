// GBTypes.h
// Widely used types for the simulation
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBTypes_h
#define GBTypes_h


#include "GBNumber.h"
#include "GBFinePoint.h"
#include <string>


// synonyms for GBNumber

typedef GBNumber GBEnergy;
typedef GBNumber GBPower;
typedef GBNumber GBDamage;
typedef GBNumber GBDistance;
typedef GBNumber GBMass;
typedef GBNumber GBSpeed;
typedef GBNumber GBForceScalar;
typedef GBNumber GBAccelerationScalar;
typedef GBNumber GBMomentumScalar;

// synonyms for GBFinePoint

typedef GBFinePoint GBPosition;
typedef GBFinePoint GBVelocity;
typedef GBFinePoint GBMomentum;
typedef GBFinePoint GBAcceleration;
typedef GBFinePoint GBForce;

// miscellaneous other simulation types

typedef long GBFrames;
typedef long GBInstructionCount;

typedef double GBRatio;	// for ratio quantities like cost constants

class GBSide; // for scoring


// object classes

enum {
	kFirstObjectClass = 0,
	ocRobot = 0,	// a robot or other large mobile object
	ocFood,		// small and immobile
	ocShot,		// small and mobile
	ocArea,		// large; collides with food and robots
	ocSensorShot,	// collides with robots, shots, and food
	ocDecoration,	// noncolliding
	kNumObjectClasses,
	ocDead	// to delete
};
typedef int GBObjectClass;

// non-simulation types

using std::string;

#ifndef nil
	#define nil 0
#endif

#endif
