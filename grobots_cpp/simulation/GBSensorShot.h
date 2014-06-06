// GBSensorShot.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBSensorShot_h
#define GBSensorShot_h

#include "GBObject.h"


class GBRobot;
class GBSensorState;


class GBSensorShot : public GBObject {
	GBRobot * owner;
	GBSide * side;
	GBSensorState * state;
	GBObjectClass seen;
	GBFrames age;
	GBPosition focus;
	
	GBFrames Lifetime() const;
public:
	GBSensorShot(const GBPosition & fcs, GBRobot * who, GBSensorState * st);
	
	void CollideWith(GBObject * other);
	void Act(GBWorld * world);

	GBObjectClass Class() const;
// accessors for views
	const GBRobot * Firer() const;
	const GBObjectClass Seen() const;
	GBSide * Owner() const;
	string Description() const;
// drawing
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
};

#endif
