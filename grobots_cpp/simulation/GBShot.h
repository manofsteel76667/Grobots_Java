// GBShot.h
// various classes for shots
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBShot_h
#define GBShot_h

#include "GBObject.h"

class GBSyphonState;

class GBShot : public GBObject {
protected:
	GBSide * owner;
	GBDamage power;
public:
	GBShot(const GBPosition & where, const GBDistance r,
		GBSide * const who, const GBDamage howMuch);
	GBShot(const GBPosition & where, const GBDistance r, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch);
	void Act(GBWorld * world);
	GBObjectClass Class() const;
	GBSide * Owner() const;
	virtual long Type() const;
	GBEnergy Power() const;
	string Description() const;
};

class GBTimedShot : public GBShot {
protected:
	GBFrames originallifetime, lifetime;
public:
	GBTimedShot(const GBPosition & where, const GBDistance r,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong);
	GBTimedShot(const GBPosition & where, const GBDistance r, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong);
	void Act(GBWorld * world);
	GBObjectClass Class() const;
	GBNumber Interest() const;
};

class GBBlast : public GBTimedShot {
	bool hit;
public:
	GBBlast(const GBPosition & where, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong);
	void CollideWithWall();
	void CollideWith(GBObject * other);
	void Act(GBWorld * world);
	long Type() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

class GBGrenade : public GBTimedShot {
public:
	GBGrenade(const GBPosition & where, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong);
	void CollideWithWall();
	void CollideWith(GBObject * other);
	void Act(GBWorld * world);
// query
	long Type() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

class GBExplosion : public GBTimedShot {
public:
	GBExplosion(const GBPosition & where, GBSide * const who, const GBDamage howMuch);
	GBObjectClass Class() const;
	void CollideWith(GBObject * other);
	void Act(GBWorld * world);
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
//
	static GBDistance PowerRadius(GBDamage pow);
};

class GBForceField : public GBShot {
	bool dead;
	GBAngle direction;
public:
	GBForceField(const GBPosition & where, const GBVelocity & vel,
		GBSide * const who, const GBPower pwr, const GBAngle dir);
	GBObjectClass Class() const;
	void Move();
	void CollideWith(GBObject * other);
	void Act(GBWorld * world);
	long Type() const;
	GBNumber Interest() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
//
	static GBDistance PowerRadius(GBPower pow);
};

class GBRobot;

class GBSyphon : public GBTimedShot {
	GBRobot * sink;
	GBSyphonState * creator;
	bool hitsEnemies;
public:
	GBSyphon(const GBPosition & where, const GBPower rate, GBRobot * const who, GBSyphonState * const state, bool newHitsEnemies);
	void Move();
	void CollideWith(GBObject * other);
	long Type() const;
	GBNumber Interest() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

#endif
