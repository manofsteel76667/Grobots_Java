// GBRandomState.h
// random number generator
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBRandomState_h
#define GBRandomState_h

#include "GBTypes.h"
#include "GBColor.h"


class GBRandomState {
	long seed;
public:
	GBRandomState();
	GBRandomState(const long newseed);
	
	long GenerateLong();
	short GenerateShort();
	long LongInRange(const long min, const long max);
	GBNumber InRange(const GBNumber min, const GBNumber max);
	float FloatInRange(const float min, const float max);
	GBAngle Angle();
	GBVector Vector(const GBDistance maxLength);
	GBColor Color();
	GBColor ColorNear(const GBColor & old, float dist);
	bool Boolean(const GBNumber probability);
	bool Boolean(const long num, const long denom);
	
	long GetSeed() const;
	void SetSeed(const long newseed);
};

extern GBRandomState gRandoms;

#endif
