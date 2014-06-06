// GBFood.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBFood_h_
#define GBFood_h_

#include "GBObject.h"

class GBWorld;
class GBRobotType;

class GBFood : public GBObject {
protected:
	GBEnergy value;
	void Recalculate();
public:
	GBFood(const GBPosition & where, const GBEnergy val);
	GBFood(const GBPosition & where, const GBVelocity & vel, const GBEnergy val);
	~GBFood();
// accessors
	GBEnergy Energy() const;
	GBEnergy TakeEnergy(const GBEnergy limit);
	GBEnergy MaxTakeEnergy();
// queries
	GBObjectClass Class() const;
	GBSide * Owner() const;
	void Move();
	void Act(GBWorld * world);
// evil antimodular drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

class GBManna : public GBFood {
public:
	GBManna(const GBPosition & where, const GBEnergy val);
	~GBManna();
	void CollectStatistics(GBWorld * world) const;
	string Description() const;
	const GBColor Color() const;
};

class GBCorpse : public GBFood {
	GBRobotType * const type;
	GBSide * const killer;
public:
	GBCorpse(const GBPosition & where, const GBVelocity & vel,
		const GBEnergy val, GBRobotType * const who, GBSide * const cause);
	~GBCorpse();
	GBSide * Owner() const;
	void CollectStatistics(GBWorld * world) const;
	GBNumber Interest() const;
	string Description() const;
	string Details() const;
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

#endif
