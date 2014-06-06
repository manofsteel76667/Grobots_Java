// GBObject.h
// GBObject - abstract class for anything in a GBWorld
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBObject_h
#define GBObject_h

#include "GBTypes.h"
#include "GBColor.h"
#include "GBGraphics.h"
#include "GBDeletionReporter.h"


class GBWorld;

class GBObject : public GBDeletionReporter {
private:
	GBPosition position;
	GBVelocity velocity;
protected:
	GBDistance radius;
	GBMass mass;
public:
	GBObject * next;
private:
// forbidden
	GBObject();
public:
	GBObject(const GBPosition & where, const GBDistance r);
	GBObject(const GBPosition & where, const GBDistance r, const GBVelocity & vel);
	GBObject(const GBPosition & where, const GBDistance r, const GBMass m);
	GBObject(const GBPosition & where, const GBDistance r, const GBVelocity & vel, const GBMass m);
	virtual ~GBObject();
// "physical" accessors
// position:
	GBPosition Position() const;
	void SetPosition(const GBPosition & newPosition);
	void MoveBy(const GBPosition & delta);
	void MoveBy(const GBDistance deltax, const GBDistance deltay);
	GBCoordinate Left() const;
	GBCoordinate Top() const;
	GBCoordinate Right() const;
	GBCoordinate Bottom() const;
// motion:
	GBVelocity Velocity() const;
	GBSpeed Speed() const;
	void SetVelocity(const GBSpeed sx, const GBSpeed sy);
	void SetSpeed(const GBSpeed speed);
	void Accelerate(const GBVelocity & deltav);
	void Drag(const GBAccelerationScalar friction, const GBRatio linearCoeff, const GBRatio quadraticCoeff);
	
	GBDistance Radius() const;
	GBMass Mass() const;

// interactions
	bool Intersects(const GBObject * other) const;
	GBNumber OverlapFraction(const GBObject * other) const;
	void BasicCollide(GBObject * other);
	void SolidCollide(GBObject * other, GBRatio coefficient);

// actions
	void PushBy(const GBMomentum & impulse);
	void PushBy(const GBMomentumScalar impulse, const GBAngle dir);
	void Push(GBObject * other, const GBMomentumScalar impulse) const;
	virtual void TakeDamage(const GBDamage amount, GBSide * origin);
	virtual GBEnergy TakeEnergy(const GBEnergy amount);
	virtual GBEnergy GiveEnergy(const GBEnergy amount);
	virtual GBEnergy MaxTakeEnergy();
	virtual GBEnergy MaxGiveEnergy();
	
	void ElasticBounce(GBObject * other, GBRatio coefficient = 1);
// high-level actions
	virtual void Think(GBWorld * world);
	virtual void Move();
	virtual void Act(GBWorld * world);
	virtual void CollideWithWall();
	virtual void CollideWith(GBObject * other);
	virtual void CollectStatistics(GBWorld * world) const;
// high-level queries
	virtual GBObjectClass Class() const;
	virtual GBSide * Owner() const;
	virtual GBEnergy Energy() const;
	virtual GBNumber Interest() const; // how attractive to autocamera
	virtual string Description() const;
	virtual string Details() const;
// evil antimodular drawing code
	virtual const GBColor Color() const;
	virtual void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	virtual void DrawMini(GBGraphics & g, const GBRect & where) const;
};

#endif
