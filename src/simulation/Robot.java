// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

class GBRobot : public GBObject {
	GBRobotType * type;
	GBBrain * brain;
	long id, parent;
	GBSide * lastHit; // who hit us last
	GBFrames recentDamage;
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
	void DrawUnderlay(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
	void Draw(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
	void DrawOverlay(GBGraphics &, const GBProjection &, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
};


#include "Robot.h"
#include "Color.h"
#include "Food.h"
#include "World.h"
#include "Shot.h"
#include "Errors.h"
#include "RobotType.h"
#include "Side.h"
#include "HardwareState.h"
#include "Brain.h"
#include "BrainSpec.h"
#include "StringUtilities.h"
#include <algorithm>
#include "Milliseconds.h"

const GBRatio kRobotRadiusFactor = 0.1;
const GBMass kRobotRadiusPadding = 3;   // robots look this much heavier than they are

const GBSpeed kFriction = 0.001;
const GBRatio kLinearDragFactor = 0.01;
const GBRatio kQuadraticDragFactor = 0.15;

const GBNumber kShieldEffectiveness = 1;
const GBPower kStandardShieldPerMass = 1.0; //shield per mass for blue-white shield graphic
const float kMinMinimapBotContrast = 0.35f;
const float kMinMeterContrast = 0.3f;
const GBFrames kRecentDamageTime = 10;

const GBSpeed kRingGrowthRate = 0.1f;

void GBRobot::Recalculate() {
	mass = type->Mass() + hardware.constructor.FetusMass();
	radius = sqrt(mass + kRobotRadiusPadding) * kRobotRadiusFactor;
}

GBRobot::GBRobot(GBRobotType * rtype, const GBPosition & where)
	: GBObject(where, 0.5, rtype->Mass()),
	type(rtype),
	brain(rtype->MakeBrain()),
	id(rtype->Side()->GetNewRobotNumber()), parent(0),
	lastHit(nil), recentDamage(0),
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
	lastHit(nil), recentDamage(0),
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
	GBDamage actual = amount * type->MassiveDamageMultiplier(mass) * ShieldFraction();
	hardware.TakeDamage(actual);
	lastHit = origin;
	recentDamage = kRecentDamageTime;
	if ( origin == Owner() )
		Owner()->Scores().ReportFriendlyFire(actual);
	else {
		Owner()->Scores().ReportDamageTaken(actual);
		if ( origin )
			origin->Scores().ReportDamageDone(actual);
	}
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
	if ( recentDamage )
		-- recentDamage;
	Recalculate();
}

void GBRobot::CollectStatistics(GBWorld * world) const {
	GBEnergy bm = Biomass();
	Owner()->ReportRobot(bm, type, Position());
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

//Draw a meter with whichever color gives better contrast. If pulse, make the meter flash.
static void DrawMeter(GBGraphics & g, const GBNumber & fraction, const GBRect & where,
					  short zeroAngle, short oneAngle, short width,
					  const GBColor & color1, const GBColor & color2, const GBColor & bgcolor, bool pulse) {
	const GBColor & color = bgcolor.ChooseContrasting(color1, color2, kMinMeterContrast);
	short angle = ceil(fraction * (oneAngle - zeroAngle));
	float phase = Milliseconds() * 6.28 / 500;
	g.DrawArc(where, zeroAngle + (angle < 0 ? angle : 0), abs(angle),
			  color * (pulse ? 0.85 + 0.15f * sin(phase) : 1.0f), width);
}

void GBRobot::DrawUnderlay(GBGraphics & g, const GBProjection & proj, const GBRect & where, bool detailed) const {
//halo: crashes, prints
	GBRect halo(where);
	halo.Shrink(-3);
	if ( brain && brain->Status() != bsOK )
		g.DrawSolidOval(halo, brain->Status() == bsStopped ? GBColor::yellow : GBColor::red);
//velocity and engine-velocity
	//if ( Velocity().Nonzero() )
	//	DrawShadow(g, proj, Velocity() * -2.5, hardware.EnginePower() ? GBColor::gray : GBColor::darkGray);
	if ( detailed && hardware.EnginePower() && hardware.EngineVelocity().Nonzero() ) {
		GBVelocity dv = hardware.EngineVelocity() - Velocity();
		if ( dv.Norm() > 0.01 ) {
			GBPosition head = Position() + dv.Unit() * (Radius() + hardware.EnginePower() / sqrt(Mass()) * 30);
			g.DrawLine(proj.ToScreenX(Position().x), proj.ToScreenY(Position().y),
					   proj.ToScreenX(head.x), proj.ToScreenY(head.y), GBColor::darkGreen, 2);
			//DrawShadow(g, proj, dv.Unit() * hardware.EnginePower() / Mass() * -30, GBColor::darkGreen);
		}
	}
//weapon ranges?
//sensor results?
}

void GBRobot::Draw(GBGraphics & g, const GBProjection & proj, const GBRect & where, bool detailed) const {
	if(where.Width() <= 5) {
		DrawMini(g,where);
		return;
	}
	short meterWidth = max(1, (where.Width() + 10) / 10);
//background and rim
	g.DrawSolidOval(where, GBColor::darkRed.Mix(0.8f * recentDamage / kRecentDamageTime, Owner()->Color()));
	g.DrawOpenOval(where, type->Color());
// meters
	if ( detailed ) {
	// energy meter
		if ( hardware.MaxEnergy() )
			DrawMeter(g, hardware.Energy() / hardware.MaxEnergy(), where, 180, 0, meterWidth,
					  GBColor::green, GBColor(0, 0.5f, 1), Owner()->Color(),
					  hardware.Eaten() || hardware.syphon.Syphoned() || hardware.enemySyphon.Syphoned());
	// damage meter
		if ( hardware.Armor() < hardware.MaxArmor() )
			DrawMeter(g, GBNumber(1) - hardware.Armor() / hardware.MaxArmor(), where, 360, 180, meterWidth,
					  GBColor::red, GBColor::lightGray, Owner()->Color(), hardware.RepairRate());
	// gestation meter
		if ( hardware.constructor.Progress() ) {
			GBRect meterRect = where;
			meterRect.Shrink(meterWidth);
			DrawMeter(g, hardware.constructor.Fraction(), meterRect, 0, 360, 1,
					  GBColor::yellow, GBColor::darkGreen, Owner()->Color(), hardware.constructor.Rate());
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
	//flash decoration when reloading or sensing
	const GBColor & basecolor = type->Decoration() == rdNone ? Owner()->Color() : type->DecorationColor();
	GBColor color = basecolor;
	if ( hardware.grenades.Cooldown() )
		color = GBColor::yellow.Mix((float)hardware.grenades.Cooldown() / hardware.grenades.ReloadTime(), basecolor);
	else if ( hardware.blaster.Cooldown() )
		color = GBColor::magenta.Mix((float)hardware.blaster.Cooldown() / hardware.blaster.ReloadTime(), basecolor);
	switch ( type->Decoration() ) {
		case rdNone: default:
			if ( ! hardware.blaster.Cooldown() && ! hardware.grenades.Cooldown() )
				break; //if we're flashing, fall through and draw a dot
		case rdDot:
			g.DrawSolidOval(GBRect(where.CenterX() - thickness, where.CenterY() - thickness,
				where.CenterX() + thickness, where.CenterY() + thickness), color);
			break;
		case rdCircle: g.DrawOpenOval(dec, color, thickness); break;
		case rdSquare: g.DrawOpenRect(dec, color, thickness); break;
		case rdTriangle:
			g.DrawLine(dec.left, dec.bottom, dec.CenterX(), dec.top,
				color, thickness);
			g.DrawLine(dec.CenterX(), dec.top, dec.right, dec.bottom,
				color, thickness);
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.bottom,
				color, thickness);
			break;
		case rdCross:
			g.DrawLine(where.CenterX(), bigDec.top, where.CenterX(), bigDec.bottom,
				color, thickness);
			g.DrawLine(bigDec.left, where.CenterY(), bigDec.right, where.CenterY(),
				color, thickness);
			break;
		case rdX:
			g.DrawLine(dec.left, dec.top, dec.right, dec.bottom,
				color, thickness);
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.top,
				color, thickness);
			break;
		case rdHLine:
			g.DrawLine(bigDec.left, where.CenterY(), bigDec.right, where.CenterY(),
				color, thickness);
			break;
		case rdVLine:
			g.DrawLine(where.CenterX(), bigDec.top, where.CenterX(), bigDec.bottom,
				color, thickness);
			break;
		case rdSlash:
			g.DrawLine(dec.left, dec.bottom, dec.right, dec.top,
				color, thickness);
			break;
		case rdBackslash:
			g.DrawLine(dec.left, dec.top, dec.right, dec.bottom,
				color, thickness);
			break;
	}
}

void GBRobot::DrawOverlay(GBGraphics & g, const GBProjection & proj, const GBRect & where, bool /*detailed*/) const {
// shield
	if ( hardware.ActualShield() > 0 ) {
		GBRect halo(where);
		halo.Shrink(-2);
		g.DrawOpenOval(halo, GBColor(0.3f, 0.5f, 1)
					   * (hardware.ActualShield() / (mass * kStandardShieldPerMass)));
	}
//radio rings
	for ( int age = 0; age < kRadioHistory; ++ age ) {
		if ( ! hardware.radio.sent[age] && ! hardware.radio.writes[age] )
			continue;
		GBDistance r = kRingGrowthRate * (age + 1);
		GBRect ring(proj.ToScreenX(Position().x - r), proj.ToScreenY(Position().y + r),
					proj.ToScreenX(Position().x + r), proj.ToScreenY(Position().y - r));
		float intensity = min(2.0f * (kRadioHistory - age) / kRadioHistory, 1.0f);
		g.DrawOpenOval(ring, (hardware.radio.sent[age] ? GBColor(0.6f, 0.5f, 1) : GBColor(1, 0.8f, 0.5f)) * intensity);
	}
}

void GBRobot::DrawMini(GBGraphics & g, const GBRect & where) const {
	if ( where.Width() <= 4 )
		g.DrawSolidRect(where, Color());
	else {
		g.DrawSolidOval(where, Owner()->Color());
		g.DrawOpenOval(where, Owner()->Color().Mix(0.5f, type->Color()));
	}
}


