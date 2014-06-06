// GBRandomState.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRandomState.h"
#include "GBErrors.h"
#include "GBStringUtilities.h"
#include "GBColor.h"
#include <time.h>


// convenient global generator
GBRandomState gRandoms;


GBRandomState::GBRandomState()
	: seed((long)time(0))
{}

GBRandomState::GBRandomState(const long newseed)
	: seed(newseed)
{}

long GBRandomState::GenerateLong() {
	seed = seed * 0734652105 + 662049451; // constants as recommended by Knuth
	return seed;
}

short GBRandomState::GenerateShort() {
	return (short)(GenerateLong() >> 16);
}

long GBRandomState::LongInRange(const long min, const long max) {
	if ( min == max )
		return min;
	else if ( min > max )
		return LongInRange(max, min);
	unsigned long range = max - min + 1;
	long result = min + (((GBLongLong)(unsigned long)GenerateLong() * range) >> 32);
	if ( result < min || result > max ) {
		NonfatalError(string("GBRandomState::LongInRange(") + ToString(min) + ", "
			+ ToString(max) + ") failed with result " + ToString(result));
	}
	return result;
}

GBNumber GBRandomState::InRange(const GBNumber min, const GBNumber max) {
	return GBNumber::MakeRaw(LongInRange(min.data, max.data));
}

float GBRandomState::FloatInRange(const float min, const float max) {
	return min + (float)(unsigned long)GenerateLong() / (float)0xFFFFFFFFUL * (max - min);
}

GBAngle GBRandomState::Angle() {
	return InRange(kEpsilon - kPi, kPi);
}

GBVector GBRandomState::Vector(const GBNumber maxLength) {
	return GBFinePoint::MakePolar(InRange(0, maxLength), Angle());
}

GBColor GBRandomState::Color() {
	return GBColor(FloatInRange(0, 1), FloatInRange(0, 1), FloatInRange(0, 1));
}

// FIXME: ColorNear is broken
GBColor GBRandomState::ColorNear(const GBColor & old, float dist) {
	return GBColor(FloatInRange(old.Red() - dist, old.Red() + dist),
					FloatInRange(old.Green() - dist, old.Green() + dist),
					FloatInRange(old.Blue() - dist, old.Blue() + dist));
}

bool GBRandomState::Boolean(const GBNumber probability) {
	return InRange(0, GBNumber(1) - kEpsilon) < probability;
}

bool GBRandomState::Boolean(const long num, const long denom) {
	return LongInRange(0, denom - 1) < num;
}

long GBRandomState::GetSeed() const {
	return seed;
}

void GBRandomState::SetSeed(const long newSeed) {
	seed = newSeed;
}