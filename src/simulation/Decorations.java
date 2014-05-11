// GBDecorations.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

class GBTimedDecoration : public GBObject {
protected:
	GBFrames lifetime;
public:
	GBTimedDecoration(const GBPosition where, const GBDistance r, const GBFrames howLong);
	GBTimedDecoration(const GBPosition where, const GBDistance r, const GBVelocity vel,
		const GBFrames howLong);
	GBObjectClass Class() const;
	void Act(GBWorld * world);
	string Description() const;
};

// convenience constants
const GBSpeed kSmokeMaxSpeed = 0.03;
const GBFrames kSmokeMinLifetime = 30;
const GBFrames kSmokeMaxLifetime = 120;

class GBSmoke : public GBTimedDecoration {
public:
	GBSmoke(const GBPosition where, const GBVelocity vel, const GBFrames life);
	string Description() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
};


class GBBlasterSpark : public GBTimedDecoration {
public:
	GBBlasterSpark(const GBPosition where);
	void Act(GBWorld * world);
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
};


#include "Decorations.h"


const GBDistance kSmokeRadius = 0.4;
const GBFrames kSmokeHalfBrightnessTime = 20;

const GBFrames kBlasterSparkLifetime = 8;
const GBDistance kBlasterSparkMaxRadius = 0.3125;
const GBDistance kBlasterSparkGrowthRate = 0.03125;


// GBTimedDecoration //

GBTimedDecoration::GBTimedDecoration(const GBPosition where, const GBDistance r, const GBFrames howLong)
	: GBObject(where, r),
	lifetime(howLong)
{}

GBTimedDecoration::GBTimedDecoration(const GBPosition where, const GBDistance r, const GBVelocity vel,
		const GBFrames howLong)
	: GBObject(where, r, vel),
	lifetime(howLong)
{}

GBObjectClass GBTimedDecoration::Class() const {
	if ( lifetime > 0 ) return ocDecoration;
	else return ocDead;
}

string GBTimedDecoration::Description() const {return "Decoration";}

void GBTimedDecoration::Act(GBWorld *) {
	lifetime --;
}

// GBSmoke //

GBSmoke::GBSmoke(const GBPosition where, const GBVelocity vel, const GBFrames life)
	: GBTimedDecoration(where, kSmokeRadius, vel, life)
{}

string GBSmoke::Description() const {return "Smoke";}

const GBColor GBSmoke::Color() const {
	float intensity = 0.8 * (float)lifetime / (lifetime + kSmokeHalfBrightnessTime);
	return GBColor(intensity);
}

void GBSmoke::Draw(GBGraphics & g, const GBProjection &, const GBRect & where, bool /*detailed*/) const {
	g.DrawSolidOval(where, Color());
}

// GBBlasterSpark //

GBBlasterSpark::GBBlasterSpark(const GBPosition where)
	: GBTimedDecoration(where, kBlasterSparkMaxRadius, kBlasterSparkLifetime)
{}

void GBBlasterSpark::Act(GBWorld * world) {
	GBTimedDecoration::Act(world);
	radius = kBlasterSparkMaxRadius - kBlasterSparkGrowthRate * (lifetime - 1);
}

const GBColor GBBlasterSpark::Color() const {
	return GBColor::white;
}

void GBBlasterSpark::Draw(GBGraphics & g, const GBProjection &, const GBRect & where, bool /*detailed*/) const {
	g.DrawOpenOval(where, Color());
}
