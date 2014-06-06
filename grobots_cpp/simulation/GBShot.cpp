// GBShot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBShot.h"
#include "GBWorld.h"
#include "GBColor.h"
#include "GBErrors.h"
#include "GBDecorations.h"
#include "GBRobot.h"
#include "GBSide.h"
#include "GBSound.h"
#include "GBStringUtilities.h"
#include <math.h>


const GBRatio kBlastPushRatio = 0.003;
const GBDistance kBlastRadius = 0.1875;
const GBNumber kBlastMomentumPerPower = 0.05;

const GBDamage kGrenadeRadiusThreshold = 40;
const GBDistance kGrenadeLargeRadius = 0.1875;
const GBDistance kGrenadeSmallRadius = 0.125;

const GBFrames kExplosionLifetime = 2;
const GBMass kExplosionMinEffectiveMass = 0.1;
const GBRatio kExplosionPushRatio = 0.01;
const GBRatio kExplosionFoodPushRatio = 0.0025;
const GBNumber kExplosionRadiusExponent = 0.33;
const GBRatio kExplosionRadiusRatio = 0.6;
const GBNumber kExplosionDamageMassExponent = 0.5;
const GBRatio kLargeExplosionIneffectiveness = 0.5;
const int kExplosionMinSmokes = 1;
const GBRatio kExplosionSmokesPerPower = 0.05;
const GBFrames kExplosionSmokeLifetimeFactor = 10;

const GBNumber kForceFieldRadiusExponent = 0.3;
const GBRatio kForceFieldRadiusRatio = 4;
const GBRatio kForceFieldPushRatio = 0.03;
const GBSpeed kMinEffectiveSpeed = 0.05;

const GBDistance kSyphonRadius = 0.125;

// GBShot //

GBShot::GBShot(const GBPosition & where, const GBDistance r,
		GBSide * const who, const GBDamage howMuch)
	: GBObject(where, r),
	owner(who), power(howMuch)
{}

GBShot::GBShot(const GBPosition & where, const GBDistance r, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch)
	: GBObject(where, r, vel),
	owner(who), power(howMuch)
{}

void GBShot::Act(GBWorld * world) {
	GBObject::Act(world);
}

GBObjectClass GBShot::Class() const {
	return ocShot;
}

GBSide * GBShot::Owner() const {
	return owner;
}

long GBShot::Type() const { return 0; }

GBEnergy GBShot::Power() const {
	return power;
}

string GBShot::Description() const {
	return (owner ? "Shot from " + owner->Name() : string("Shot")) +
		" (power " + ToString(power) + ", speed " + ToString(Speed()) + ')';
}

// GBTimedShot //

GBTimedShot::GBTimedShot(const GBPosition & where, const GBDistance r,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong)
	: GBShot(where, r, who, howMuch),
	originallifetime(howLong), lifetime(howLong)
{}

GBTimedShot::GBTimedShot(const GBPosition & where, const GBDistance r, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong)
	: GBShot(where, r, vel, who, howMuch),
	originallifetime(howLong), lifetime(howLong)
{}

void GBTimedShot::Act(GBWorld * world) {
	GBShot::Act(world);
	lifetime--;
}

GBObjectClass GBTimedShot::Class() const {
	if ( lifetime > 0 )
		return ocShot;
	else
		return ocDead;
}

GBNumber GBTimedShot::Interest() const {
	return abs(power) * 10 / (lifetime < 5 ? 5 : lifetime) + 1;
}

// GBBlast //

GBBlast::GBBlast(const GBPosition & where, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong)
	: GBTimedShot(where, kBlastRadius, vel, who, howMuch, howLong),
	hit(false)
{}

void GBBlast::CollideWithWall() {
	lifetime = 0;
	hit = true;
}

void GBBlast::CollideWith(GBObject * other) {
	if ( !hit && other->Class() == ocRobot ) {
		other->TakeDamage(power, owner);
		Push(other, power * kBlastPushRatio);
		other->PushBy(Velocity() * power * kBlastMomentumPerPower);
		lifetime = 0;
		hit = true;
	}
}

void GBBlast::Act(GBWorld * world) {
	GBTimedShot::Act(world);
	if ( hit ) {
		world->AddObjectNew(new GBBlasterSpark(Position()));
	}
}

long GBBlast::Type() const { return 1; }

//Blasts fade out at the end of their lives.
//Big blasts are saturated, smaller ones are pink or white.
//Long-range blasts are orangish; short-range ones are magenta.
const GBColor GBBlast::Color() const {
	float fadeout = hit ? 1.0f : min(lifetime / min((float)originallifetime, 10.0f), 1.0f);
	float whiteness = pow(0.95, power.ToDouble());
	float blueness = pow(0.9995, originallifetime * originallifetime);
	return GBColor::white.Mix(whiteness, GBColor(1, 0.5f - blueness * 1.5f, blueness * 1.5f)) * fadeout;
}

void GBBlast::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	if ( where.Width() <= 3 ) {
		g.DrawSolidRect(where,Color());
	} else if ( hit ) {
		g.DrawSolidOval(where, Color());
	} else {
		//g.DrawOpenOval(where, GBColor::gray);
		short cx = where.CenterX();
		short cy = where.CenterY();
		int thickness = 2 + floor(power / 20);
		GBVector head = Velocity().Unit() * where.Width() / 2;
		short hx = round(head.x);
		short hy = - round(head.y);
		GBVector tail = Velocity() * where.Width() / (radius * 2);
		short tx = round(tail.x);
		short ty = - round(tail.y);
		//g.DrawLine(cx + hx, cy + hy, cx - tx, cy - ty, Color() * 0.7, thickness + 2);
		//g.DrawLine(cx + hx, cy + hy, cx - hx, cy - hy, Color() + GBColor(0.2), max(thickness, 2));
		g.DrawLine(cx + hx, cy + hy, cx - tx, cy - ty, Color(), thickness);
	}
}

// GBGrenade //

GBGrenade::GBGrenade(const GBPosition & where, const GBVelocity & vel,
		GBSide * const who, const GBDamage howMuch, const GBFrames howLong)
	: GBTimedShot(where, howMuch >= kGrenadeRadiusThreshold ? kGrenadeLargeRadius : kGrenadeSmallRadius, vel,
		who, howMuch, (howLong <= 0)?1:howLong)
{}

void GBGrenade::CollideWithWall() {
	lifetime = 0;
}

void GBGrenade::CollideWith(GBObject *) {
	// grenades ignore collisions
}

void GBGrenade::Act(GBWorld * world) {
	GBTimedShot::Act(world);
	if ( lifetime <= 0 ) {
		world->AddObjectNew(new GBExplosion(Position(), owner, power));
	}
}

long GBGrenade::Type() const { return 2; }

const GBColor GBGrenade::Color() const {
	return GBColor::yellow;
}

void GBGrenade::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	if (where.Width() <= 3)
		g.DrawSolidRect(where,Color());
	else
		g.DrawSolidOval(where, Color());
}

// GBExplosion //

GBExplosion::GBExplosion(const GBPosition & where, GBSide * const who, const GBDamage howMuch)
	: GBTimedShot(where, PowerRadius(howMuch), who, howMuch, kExplosionLifetime)
{
	if ( howMuch < 0 ) throw GBBadArgumentError();
	if ( howMuch > 100 ) StartSound(siLargeExplosion);
	else if ( howMuch > 30 ) StartSound(siMediumExplosion);
	else if ( howMuch > 10 ) StartSound(siSmallExplosion);
	else StartSound(siTinyExplosion);
}

GBObjectClass GBExplosion::Class() const {
	if ( lifetime > 0 )
		return ocArea;
	else
		return ocDead;
}

void GBExplosion::CollideWith(GBObject * other) {
	if ( lifetime < kExplosionLifetime ) return;
	GBMass oMass = max(other->Mass(), kExplosionMinEffectiveMass);
	if ( oMass == 0 ) return; // massless objects get 0 damage
	GBDamage damage = power / (pow(power / oMass, kExplosionDamageMassExponent) * kLargeExplosionIneffectiveness + 1);
	other->TakeDamage(damage * OverlapFraction(other), owner);
	Push(other, damage * other->OverlapFraction(this)
		* (other->Class() == ocFood ? kExplosionFoodPushRatio : kExplosionPushRatio));
}

void GBExplosion::Act(GBWorld * world) {
	GBTimedShot::Act(world);
	if ( ! lifetime ) {
		GBFrames maxLifetime = floor(sqrt(power)) * kExplosionSmokeLifetimeFactor;
		for ( int i = round(max(power * kExplosionSmokesPerPower, kExplosionMinSmokes)); i > 0; i -- )
			world->AddObjectNew(new GBSmoke(Position() + world->Randoms().Vector(Radius()),
										world->Randoms().Vector(kSmokeMaxSpeed),
										world->Randoms().LongInRange(kSmokeMinLifetime, maxLifetime)));
	}
}

const GBColor GBExplosion::Color() const {
	return GBColor(1, 0.9f, 0.2f);
}

void GBExplosion::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawSolidOval(where, Color());
}

GBDistance GBExplosion::PowerRadius(GBDamage pwr) {
	return pow(pwr, kExplosionRadiusExponent) * kExplosionRadiusRatio;
}

// GBForceField //

GBForceField::GBForceField(const GBPosition & where, const GBVelocity & vel,
						   GBSide * const who, const GBPower pwr, const GBAngle dir)
	: GBShot(where, PowerRadius(pwr), vel, who, pwr),
	dead(false),
	direction(dir)
{}

GBObjectClass GBForceField::Class() const {
	return dead ? ocDead : ocArea;
}

void GBForceField::Move() {
	// don't move! velocity is only apparent.
}

void GBForceField::CollideWith(GBObject * other) {
	GBForceScalar force = power / max(other->Speed(), kMinEffectiveSpeed)
		* OverlapFraction(other) * sqrt(other->Mass()) * kForceFieldPushRatio;
	other->PushBy(force, direction);
}

void GBForceField::Act(GBWorld * world) {
	GBShot::Act(world);
	dead = true;
}

long GBForceField::Type() const { return 5; }

GBNumber GBForceField::Interest() const {
	return abs(power) * 15 + 1;
}

const GBColor GBForceField::Color() const {
	return GBColor(0, 0.8f, 1);
}

void GBForceField::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	short cx = (where.right + where.left) / 2;
	short cy = (where.bottom + where.top) / 2;
	g.DrawLine(cx, cy, cx + round(cos(direction) * where.Width() / 2),
		cy - round((sin(direction) * where.Height() / 2)), owner ? owner->Color() : Color());
	g.DrawOpenOval(where, Color(), where.Width() >= 20 ? 2 : 1);
}

void GBForceField::DrawMini(GBGraphics & g, const GBRect & where) const {
	g.DrawOpenOval(where, Color());
}

GBDistance GBForceField::PowerRadius(GBPower pwr) {
	return pow(abs(pwr), kForceFieldRadiusExponent) * kForceFieldRadiusRatio;
}

// GBSyphon //

GBSyphon::GBSyphon(const GBPosition & where, const GBPower rate, GBRobot * const who, GBSyphonState * const state, bool newHitsEnemies)
	: GBTimedShot(where, kSyphonRadius, where - who->Position(), who->Owner(), rate, 1),
	sink(who), creator(state), hitsEnemies(newHitsEnemies)
{}

void GBSyphon::Move() {
	// don't move! velocity is only apparent.
}

void GBSyphon::CollideWith(GBObject * other) {
	if ( other->Class() == ocRobot && power
			&& other != (GBObject *)sink
			&& (hitsEnemies || other->Owner() == sink->Owner())) {
		if ( power >= 0 ) {
			try {
				GBRobot * otherBot = (GBRobot *) other;
				//FIXME shields affect syphons twice: here and when they're fired
				GBNumber efficiency = sink->ShieldFraction() * otherBot->ShieldFraction();
				if ( other->Owner() != sink->Owner() ) {
					if ( otherBot->hardware.MaxEnergy() != 0)
						efficiency *= min(GBNumber(1), otherBot->hardware.Energy() / otherBot->hardware.MaxEnergy());
				}
				GBEnergy maxTransfer = min(sink->MaxGiveEnergy(), other->MaxTakeEnergy());
				GBEnergy actual = min(maxTransfer, power * efficiency);
				
				if ( actual > 0 ) {
					GBPower taken = other->TakeEnergy(actual);
					GBPower given = sink->GiveEnergy(actual);
					power = min(power - actual / efficiency, GBNumber(0));
					creator->ReportUse(taken);
					if ( taken > 0 && other->Owner() != owner ) {
						owner->ReportKleptotrophy(taken);
						other->Owner()->Scores().Expenditure().ReportStolen(taken);
					}
					if (taken != given || taken != actual) {
						throw GBGenericError("Given != taken in GBSyphon::CollideWith");
					}
				}
			} catch ( GBError & e ) {
				NonfatalError("Error in GBSyphon::CollideWith: " + e.ToString());
			}
		} else { // giving energy: like taking, but target energy isn't a factor
			GBRobot * otherBot = (GBRobot *) other;
			GBNumber efficiency = sink->ShieldFraction() * otherBot->ShieldFraction();
			// efficiency does not decrease with low energy when giving
			GBEnergy maxTransfer = min(other->MaxGiveEnergy(), sink->MaxTakeEnergy());
			GBEnergy actual = min(maxTransfer, -power * efficiency);
			
			GBPower taken = sink->TakeEnergy(actual);
			GBPower given = other->GiveEnergy(actual);
			power = max(power + actual / efficiency, 0);
			
			creator->ReportUse(- given);		
			
			if ( given > 0 && other->Owner() != owner ) {  // giving energy to enemy :)
				other->Owner()->ReportKleptotrophy(given);
				owner->Scores().Expenditure().ReportStolen(given);
			}
			if (taken != given || taken != actual) {
				throw GBGenericError("Given != taken in GBSyphon::CollideWith");
			}
		}
	}
}

long GBSyphon::Type() const { return hitsEnemies ? 4 : 3; }

GBNumber GBSyphon::Interest() const {
	return abs(power) * (hitsEnemies ? 5 : 1) + (hitsEnemies ? 2 : 1);
}

const GBColor GBSyphon::Color() const {
	return (hitsEnemies ? GBColor(0.6f, 1, 0) : GBColor(0.5f, 0.8f, 1));
}

void GBSyphon::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawLine(where.left, where.top, where.right, where.bottom, Color());
	g.DrawLine(where.right, where.top, where.left, where.bottom, Color());
}

