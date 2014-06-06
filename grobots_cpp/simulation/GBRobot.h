// GBRobot.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef _GBRobot_h_
#define _GBRobot_h_

#include "GBObject.h"
#include "GBTypes.h"
#include "GBHardwareState.h"
#include "GBRandomState.h"


class GBBrain;
class GBRobotType;
class GBSide;

class GBRobot : public GBObject {
	GBRobotType * type;
	GBBrain * brain;
	long id, parent;
	GBSide * lastHit; // who hit us last
	int friendlyCollisions, enemyCollisions, shotCollisions, foodCollisions, wallCollisions;
public:
// hardware state
	GBHardwareState hardware;
	bool dead;
	GBNumber flag;
	void Recalculate();
public:
	GBRobot(GBRobotType * rtype, const GBPosition & where);
	GBRobot(GBRobotType * rtype, const GBPosition & where, const GBVelocity & vel, long parentID);
	~GBRobot();
// accessors
	GBRobotType * Type() const;
	long ID() const;
	long ParentID() const;
	int Collisions() const; // robots and walls
	int FriendlyCollisions() const;
	int EnemyCollisions() const;
	int FoodCollisions() const;
	int ShotCollisions() const;
	int WallCollisions() const;
	GBSide * LastHit() const;
	GBBrain * Brain();
// actions
	GBNumber ShieldFraction() const;
	void TakeDamage(const GBDamage amount, GBSide * origin);
	GBEnergy TakeEnergy(const GBEnergy amount);
	GBEnergy GiveEnergy(const GBEnergy amount);
	GBEnergy MaxTakeEnergy();
	GBEnergy MaxGiveEnergy();
	void EngineSeek(const GBVector & pos, const GBVelocity & vel);
	void Die(GBSide * killer);
// high-level actions
	void Move();
	void Act(GBWorld * world);
	void CollideWithWall();
	void CollideWith(GBObject * other);
	void Think(GBWorld * world);
	void CollectStatistics(GBWorld * world) const;
// queries
	GBObjectClass Class() const;
	GBSide * Owner() const;
	GBEnergy Energy() const;
	GBEnergy Biomass() const;
	GBNumber Interest() const;
	string Description() const;
	string Details() const;
// evil antimodular drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
};

#endif
