// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRobot.h"
#include "GBColor.h"
#include "GBFood.h"
#include "GBWorld.h"
#include "GBShot.h"
#include "GBErrors.h"
#include "GBRobotType.h"
#include "GBSide.h"
#include "GBHardwareState.h"
#include "GBBrain.h"
#include "GBBrainSpec.h"
#include "GBStringUtilities.h"
#include <algorithm>


const GBRatio kRobotRadiusFactor = 0.1;
const GBMass kRobotRadiusPadding = 3;   // robots look this much heavier than they are

const GBSpeed kFriction = 0.001;
const GBRatio kLinearDragFactor = 0.01;
const GBRatio kQuadraticDragFactor = 0.15;

const GBNumber kShieldEffectiveness = 1;
const GBPower kStandardShieldPerMass = 1.0; //shield per mass for blue-white shield graphic
const float kMinMinimapBotContrast = 0.35f;
const float kMinMeterContrast = 0.25f;


void GBRobot::Recalculate() {
	mass = type->Mass() + hardware.constructor.FetusMass();
	radius = sqrt(mass + kRobotRadiusPadding) * kRobotRadiusFactor;
}

GBRobot::GBRobot(GBRobotType * rtype, const GBPosition & where)
	: GBObject(where, 0.5, rtype->Mass()),
	type(rtype),
	brain(rtype->MakeBrain()),
	id(rtype->Side()->GetNewRobotNumber()), parent(0),
	lastHit(nil),
	friendlyCollisions(0), enemyCollisions(0), foodCollisions(0), shotCollisions(0), wallCollisions(0),
	hardware(&rtype->Hardware()),
	dead(false),
	flag(0)
{
	if ( ! rtype ) throw GBNilPointerError();
	hardware.radio.Reset(Owner());
	Recalculate();
}

GBRobot::GBRobot(GBRobotType * rtype, const GBPosition & where, const GBVelocity & vel, long parentID)
	: GBObject(where, 0.5, vel, rtype->Mass()),
	type(rtype),
	brain(rtype->MakeBrain()),
	id(rtype->Side()->GetNewRobotNumber()), parent(parentID),
	lastHit(nil),
	friendlyCollisions(0), enemyCollisions(0), foodCollisions(0), shotCollisions(0), wallCollisions(0),
	hardware(&rtype->Hardware()),
	dead(false),
	flag(0)
{
	if ( ! rtype ) throw GBNilPointerError();
	hardware.radio.Reset(Owner());
	Recalculate();
}

GBRobot::~GBRobot() {
	delete brain;
}

GBRobotType * GBRobot::Type() const {
	return type;
}

long GBRobot::ID() const {
	return id;}

long GBRobot::ParentID() const {
	return parent;}

string GBRobot::Description() const {
	return type->Description() + " #" + ToString(id);
}

string GBRobot::Details() const {
	string dets = ToString(Energy(), 0) + " energy, " + ToString(hardware.Armor(), 0) + " armor";
	if (hardware.constructor.Progress())
		dets += ", " + ToPercentString(hardware.constructor.Progress() / hardware.constructor.Type()->Cost(), 0)
			+ " " + hardware.constructor.Type()->Name();
	return dets;
}

int GBRobot::Collisions() const {
	return friendlyCollisions + enemyCollisions + wallCollisions;
}

int GBRobot::FriendlyCollisions() const { return friendlyCollisions; }
int GBRobot::EnemyCollisions() const { return enemyCollisions; }
int GBRobot::FoodCollisions() const { return foodCollisions; }
int GBRobot::ShotCollisions() const { return shotCollisions; }
int GBRobot::WallCollisions() const { return wallCollisions; }

GBSide * GBRobot::LastHit() const { return lastHit; }

GBBrain * GBRobot::Brain() { return brain; }

GBNumber GBRobot::ShieldFraction() const {
	if (hardware.ActualShield())
		return GBNumber(1) / (square(hardware.ActualShield() * kShieldEffectiveness / mass) + 1);
	return 1;
}

void GBRobot::TakeDamage(const GBDamage amount, GBSide * origin) {
	hardware.TakeDamage(amount * type->MassiveDamageMultiplier(mass) * ShieldFraction());
	lastHit = origin;
}

GBEnergy GBRobot::TakeEnergy(const GBEnergy amount) {
	return hardware.UseEnergyUpTo(amount);
}

GBEnergy GBRobot::GiveEnergy(const GBEnergy amount) {
	return hardware.GiveEnergy(amount );
}

GBEnergy GBRobot::MaxTakeEnergy() {
	return hardware.Energy();
}

GBEnergy GBRobot::MaxGiveEnergy() {
	return hardware.MaxEnergy() - hardware.Energy();
}

void GBRobot::EngineSeek(const GBVector & pos, const GBVector & vel) {
	GBVector delta = pos - Position();
	if ( vel.Zero() && (delta + Velocity() * 11).Norm() < radius )
		hardware.SetEnginePower(0);
	else {
		hardware.SetEnginePower(hardware.EngineMaxPower());
		hardware.SetEngineVelocity(vel + delta * GBNumber(0.09)); // FIXME: better seek
	}
}

void GBRobot::Die(GBSide * killer) {
	dead = true;
	lastHit = killer;
}

void GBRobot::Move() {
	friendlyCollisions = 0;
	enemyCollisions = 0;
	foodCollisions = 0;
	shotCollisions = 0;
	wallCollisions = 0;
	GBObject::Move();
	Drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
}

void GBRobot::CollideWithWall() {
	wallCollisions ++;
}

void GBRobot::CollideWith(GBObject * other) {
	switch ( other->Class() ) {
		case ocRobot:
			if ( other->Owner() == Owner() ) friendlyCollisions ++;
			else enemyCollisions ++;
			break;
		case ocFood: {
			foodCollisions ++;
			//FIXME shields don't affect eaters
			GBEnergy eaten = other->TakeEnergy(hardware.EaterLimit());
			hardware.Eat(eaten);
			GBSide * source = other->Owner();
			if ( ! source )
				Owner()->ReportTheotrophy(eaten);
			else if ( source == Owner() )
				Owner()->ReportCannibalism(eaten);
			else
				Owner()->ReportHeterotrophy(eaten);
		} break;
		case ocShot:
			shotCollisions ++; break;
		default: break;
	}
}

void GBRobot::Think(GBWorld * world) {
	if ( brain )
		brain->Think(this, world);
}

void GBRobot::Act(GBWorld * world) {
	hardware.Act(this, world);
	if ( dead ) {
		if ( ! lastHit )
			; // no reports for accidents
		else if ( lastHit == Owner() )
			lastHit->ReportSuicide(Biomass());
		else
			lastHit->ReportKilled(Biomass());
		Owner()->ReportDead(Biomass());
	}
	Recalculate();
}

void GBRobot::CollectStatistics(GBWorld * world) const {
	GBEnergy bm = Biomass();
	Owner()->ReportRobot(bm, type->Hardware().constructor.Cost(), Position());
	type->ReportRobot(bm);
	world->ReportRobot(bm);
}

GBObjectClass GBRobot::Class() const {
	if ( dead )
		return ocDead;
	else
		return ocRobot;
}

GBSide * GBRobot::Owner() const {
	return type->Side();
}

GBEnergy GBRobot::Energy() const {
	return hardware.Energy();
}

GBEnergy GBRobot::Biomass() const {
	return type->Cost() - type->Hardware().InitialEnergy() + hardware.Energy() + hardware.constructor.Progress();
}

GBNumber GBRobot::Interest() const {
	GBNumber interest = Biomass() * (GBNumber(0.001) + Speed() * 0.01)
		+ hardware.ActualShield() / 2;
	if ( hardware.blaster.Cooldown() )
		interest += hardware.blaster.Damage() * 10 / hardware.blaster.ReloadTime();
	if ( hardware.grenades.Cooldown() )
		interest += hardware.grenades.Damage() * 10 / hardware.grenades.ReloadTime();
	if ( hardware.blaster.Cooldown() )
		interest += abs(hardware.forceField.Power()) * 15 + 1;
	if ( hardware.syphon.Rate() )
		interest += abs(hardware.syphon.Rate()) + 1;
	if ( hardware.enemySyphon.Rate() )
		interest += abs(hardware.enemySyphon.Rate())* 5 + 2;
	return interest;
}

//This color is only used for the minimap, so it has built-in contrast handling.
const GBColor GBRobot::Color() const {
	return Owner()->Color().EnsureContrastWithBlack(kMinMinimapBotContrast)
		.Mix(0.9f, type->Color());
}

void GBRobot::Draw(GBGraphics & g, const GBRect & where, bool detailed) const {
	if(where.Width() <= 5) {
		DrawMini(g,where);
		return;
	}
// shield
	if ( hardware.ActualShield() > 0 )
		g.DrawOpenRect(where, GBColor(0.3f, 0.5f, 1)
			* (hardware.ActualShield() / (mass * kStandardShieldPerMass)).ToDouble());
// body
	g.DrawSolidOval(where, Owner()->Color());
// meters
	if ( detailed ) {
		short meterWidth = std::max(1, (where.Width() + 10) / 10);
		short arcsize;
		GBRect meterRect = where;
		meterRect.Shrink((meterWidth + 1) / 2); //TODO is this portable?
	// energy meter
		if ( hardware.MaxEnergy() ) {
			arcsize = round(hardware.Energy() / hardware.MaxEnergy() * 180);
			g.DrawArc(meterRect, 180 - arcsize, arcsize,
				Owner()->Color().ChooseContrasting(GBColor::green, GBColor(0, 0.5f, 1), kMinMeterContrast), meterWidth);
		}
	// armor meter
		arcsize = round(max(hardware.Armor(), 0) / hardware.MaxArmor() * 180);
		g.DrawArc(meterRect, 180, arcsize,
			Owner()->Color().ChooseContrasting(GBColor::lightGray, GBColor::darkGray, kMinMeterContrast), meterWidth);
	// gestation meter
		meterRect.Shrink(meterWidth);
		if ( hardware.constructor.Progress() ) {
			arcsize = round(hardware.constructor.Fraction() * 360);
			g.DrawArc(meterRect, 360 - arcsize, arcsize,
				Owner()->Color().ChooseContrasting(GBColor(1, 1, 0), GBColor(0, 0.5f, 0), kMinMeterContrast), meterWidth);
		}
	}
// decoration
	short thickness = (15 + where.right - where.left) / 15; //was 2 for >15 else 1
	GBRect dec((where.left * 2 + where.right + 2) / 3,
		(where.top * 2 + where.bottom + 2) / 3,
		(where.left + where.right * 2 + 1) / 3,
		(where.top + where.bottom * 2 + 1) / 3);
	short dx = where.Width() / 4;
	short dy = where.Height() / 4;
	//cross, hline, and vline draw in bigDec instead of dec
	GBRect bigDec(where.CenterX() - dx, where.CenterY() - dy, where.CenterX() + dx, where.CenterY() + dy);
	switch ( type->Decoration() ) {
		case rdDot:
			g.DrawSolidOval(GBRect(where.CenterX() - thickness, where.CenterY() - thickness,
				where.CenterX() + thickness, where.CenterY() + thickness), type->DecorationColor());
			break;
		case rdCircle: g.DrawOpenOval(dec, type->DecorationColor(), thickness); break;
		case rdSquare: g.DrawOpenRect(dec, type->DecorationColor(), thickness); break;
		case rdTriangle:
			g.DrawLine(dec.left, dec.bottom, dec.CenterX(), dec.top,
				type->DecorationColor(), thickness);
			g.DrawLine(dec.CenterX(), dec.top, dec.right, dec.bottom,
				type->DecorationColor(), thickness);
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.bottom,
				type->DecorationColor(), thickness);
			break;
		case rdCross:
			g.DrawLine(where.CenterX(), bigDec.top, where.CenterX(), bigDec.bottom,
				type->DecorationColor(), thickness);
			g.DrawLine(bigDec.left, where.CenterY(), bigDec.right, where.CenterY(),
				type->DecorationColor(), thickness);
			break;
		case rdX:
			g.DrawLine(dec.left, dec.top, dec.right, dec.bottom,
				type->DecorationColor(), thickness);
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.top,
				type->DecorationColor(), thickness);
			break;
		case rdHLine:
			g.DrawLine(bigDec.left, where.CenterY(), bigDec.right, where.CenterY(),
				type->DecorationColor(), thickness);
			break;
		case rdVLine:
			g.DrawLine(where.CenterX(), bigDec.top, where.CenterX(), bigDec.bottom,
				type->DecorationColor(), thickness);
			break;
		case rdSlash:
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.top,
				type->DecorationColor(), thickness);
			break;
		case rdBackslash:
			g.DrawLine(dec.left, dec.top, dec.right, dec.bottom,
				type->DecorationColor(), thickness);
			break;
		case rdNone: default: break;
	}
//rim (last in case something is slightly in the wrong place)
	g.DrawOpenOval(where, type->Color());
}

void GBRobot::DrawMini(GBGraphics & g, const GBRect & where) const {
	if ( where.Width() <= 4 )
		g.DrawSolidRect(where, Color());
	else {
		g.DrawSolidOval(where, Owner()->Color());
		g.DrawOpenOval(where, Owner()->Color().Mix(0.5f, type->Color()));
	}
}


