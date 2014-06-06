// GBSensorShot.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBSensorShot.h"
#include "GBHardwareState.h"
#include "GBRobot.h"

const GBFrames kSensorShotMinLifetime = 15;


GBFrames GBSensorShot::Lifetime() const {
	return floor(max(radius, kSensorShotMinLifetime));
}

GBSensorShot::GBSensorShot(const GBPosition & fcs, GBRobot * who, GBSensorState * st)
	: GBObject(who->Position(), st->MaxRange()),
	owner(who), side(who->Owner()),
	state(st),
	seen(st->Seen()),
	age(0),
	focus(fcs)
{}

void GBSensorShot::CollideWith(GBObject * other) {
	if ( other->Class() == ocRobot && (GBRobot *)other == owner )
		return; //Seeing self is never allowed
	state->Report(GBSensorResult(other, (other->Position() - focus).Norm())); //most logic is now in SensorState
}

void GBSensorShot::Act(GBWorld *) {
	age ++;
}

GBObjectClass GBSensorShot::Class() const {
	if ( age >= Lifetime() )
		return ocDead;
	else
		return ocSensorShot;
}

string GBSensorShot::Description() const {
	string classname;
	switch ( seen ) {
		case ocRobot: classname = "Robot"; break;
		case ocFood: classname = "Food"; break;
		case ocShot: classname = "Shot"; break;
		default: classname = "Mystery"; break;
	}
	return classname + " sensor for " + owner->Description();
}

const GBColor GBSensorShot::Color() const {
	float fraction = 1.0 - (float)age / Lifetime();
	switch ( seen ) {
		case ocRobot: return GBColor(0.4f, 0.8f, 1) * fraction;
		case ocFood: return GBColor(0.5f, 1, 0.5f) * fraction;
		case ocShot: return GBColor(1, 1, 0.5f) * fraction;
	}
	return GBColor(fraction);
}

const GBRobot * GBSensorShot::Firer() const {
	return owner;
}

const GBObjectClass GBSensorShot::Seen() const {
	return age == 0 ? seen : ocDead;
}

GBSide * GBSensorShot::Owner() const { return side; }

void GBSensorShot::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	// show focus, owner, and side?
	g.DrawOpenOval(where, Color());
}

void GBSensorShot::DrawMini(GBGraphics & g, const GBRect & where) const {
	g.DrawOpenOval(where, Color());
}

