// GBDecorations.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBDecorations.h"


const GBDistance kSmokeRadius = 0.4;
const GBFrames kSmokeHalfBrightnessTime = 20;

const GBFrames kBlasterSparkLifetime = 8;
const GBDistance kBlasterSparkMaxRadius = 0.3125;
const GBDistance kBlasterSparkGrowthRate = 0.03125;

const GBSpeed kTransmissionGrowthRate = 0.1;
const short kTransmissionLifetime = 12;

const GBDistance kSparkleRadius = 0.0625;


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

void GBSmoke::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
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

void GBBlasterSpark::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawOpenOval(where, Color());
}

// GBTransmission //

GBTransmission::GBTransmission(const GBPosition where, GBDistance initialradius, bool msg)
	: GBTimedDecoration(where, initialradius, kTransmissionLifetime),
	message(msg)
{}

void GBTransmission::Act(GBWorld * world) {
	GBTimedDecoration::Act(world);
	radius += kTransmissionGrowthRate;
}

string GBTransmission::Description() const {return "Radio transmission";}

const GBColor GBTransmission::Color() const {
	float intensity = min(2.0f * lifetime / kTransmissionLifetime, 1.0f);
	return (message ? GBColor(0.6f, 0.5f, 1) : GBColor(1, 0.8f, 0.5f)) * intensity;
}

void GBTransmission::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawOpenOval(where, Color());
}

void GBTransmission::DrawMini(GBGraphics & g, const GBRect & where) const {
	g.DrawOpenOval(where, Color());
}

// GBSparkle //

GBSparkle::GBSparkle(const GBPosition where, const GBVelocity vel,
		const GBColor & col, const GBFrames life)
	: GBTimedDecoration(where, kSparkleRadius, vel, life),
	color(col)
{}

const GBColor GBSparkle::Color() const {
	return color;
}

void GBSparkle::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawSolidRect(where, color);
}

void GBSparkle::DrawMini(GBGraphics & g, const GBRect & where) const {
	g.DrawSolidRect(where, color * 0.7f);
}

