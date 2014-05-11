// GBFood.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

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
	void Draw(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
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
	void Draw(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
};

#include "Food.h"
#include "Errors.h"
#include "World.h"
#include "Side.h"
#include "StringUtilities.h"
#include "RobotType.h"

const GBDistance kFoodMinRadius = 0.1;
const GBRatio kFoodRadiusFactor = 0.01;
const GBRatio kFoodMassPerValue = 0.015;
const GBEnergy kFoodDecayRate = 0.12;

const GBSpeed kFriction = 0.004;
const GBRatio kLinearDragFactor = 0.01;
const GBRatio kQuadraticDragFactor = 0.3;

// GBFood //

void GBFood::Recalculate() {
	radius = sqrt(value) * kFoodRadiusFactor + kFoodMinRadius;
	mass = value * kFoodMassPerValue;
}

GBFood::GBFood(const GBPosition & where, const GBEnergy val)
	: GBObject(where, sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, val * kFoodMassPerValue),
	value(val)
{
	if ( val < 0 ) throw GBBadArgumentError();
}

GBFood::GBFood(const GBPosition & where, const GBVelocity & vel, const GBEnergy val)
	: GBObject(where, sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, vel, val * kFoodMassPerValue),
	value(val)
{
	if ( val < 0 ) throw GBBadArgumentError();
}

GBFood::~GBFood() {}

GBEnergy GBFood::Energy() const {
	return value;
}

GBEnergy GBFood::TakeEnergy(const GBEnergy limit) {
	if ( limit < 0 ) throw GBBadArgumentError();
	if ( value <= limit ) {
		GBEnergy amt = value;
		value = 0;
		Recalculate();
		return amt;
	} else {
		value -= limit;
		if ( value < 0 ) throw GBBadComputedValueError();
		Recalculate();
		return limit;
	}
}

GBEnergy GBFood::MaxTakeEnergy() {
	return value;
}

GBObjectClass GBFood::Class() const {
	if ( value > 0 )
		return ocFood;
	else
		return ocDead;
}

GBSide * GBFood::Owner() const {
	return nil;
}

void GBFood::Move() {
	GBObject::Move();
	Drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
}

void GBFood::Act(GBWorld *) {
	value = max(value - kFoodDecayRate, 0);
	Recalculate(); //FIXME this is slow
}

const GBColor GBFood::Color() const {
	return GBColor::white;
}

void GBFood::Draw(GBGraphics & g, const GBProjection &, const GBRect & where, bool /*detailed*/) const {
	g.DrawSolidRect(where, Color());
}

// GBManna //

GBManna::GBManna(const GBPosition & where, const GBEnergy val)
	: GBFood(where, val)
{}

GBManna::~GBManna() {}

void GBManna::CollectStatistics(GBWorld * world) const {
	world->ReportManna(value);
}

string GBManna::Description() const {
	return string("Manna (") + ToString(value, 0) + ')';
}

const GBColor GBManna::Color() const {
	return GBColor::green;
}

// GBCorpse //

GBCorpse::GBCorpse(const GBPosition & where, const GBVelocity & vel,
		const GBEnergy val, GBRobotType * const who, GBSide * const cause)
	: GBFood(where, vel, val),
	type(who), killer(cause)
{}

GBCorpse::~GBCorpse() {}

GBSide * GBCorpse::Owner() const {
	return type->Side();
}

void GBCorpse::CollectStatistics(GBWorld * world) const {
	world->ReportCorpse(value);
}

GBNumber GBCorpse::Interest() const {
	return value / 500;
}

string GBCorpse::Description() const {
	return "Corpse of " + type->Description();
}

string GBCorpse::Details() const {
	return ToString(value, 0) + " energy, killed by " + (killer ? killer->Name() : string("accident"));
}

const GBColor GBCorpse::Color() const {
	return GBColor::red;
}

void GBCorpse::Draw(GBGraphics & g, const GBProjection & proj, const GBRect & where, bool detailed) const {
	GBFood::Draw(g, proj, where, detailed);
	if ( detailed && where.Width() >= 4 ) {
		g.DrawOpenRect(where, Owner()->Color());
		if ( killer && where.Width() >= 6 ) {
			GBRect dot(where.CenterX() - 1, where.CenterY() - 1,
				where.CenterX() + 1, where.CenterY() + 1);
			g.DrawSolidRect(dot, killer->Color());
		}
	}
}

