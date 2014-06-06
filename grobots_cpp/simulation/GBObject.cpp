// GBObject.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBObject.h"
#include "GBErrors.h"
#include "GBColor.h"
#include "GBDeletionReporter.h"

const GBAccelerationScalar kCollisionRepulsion = 0.02;
const GBSpeed kCollisionRepulsionSpeed = 0.1;
const GBDistance kMaxOverlap = 0.04;

const short kMaxSquareMiniSize = 4;


GBObject::GBObject() {
	throw GBBadConstructorError();
}

GBObject::GBObject(const GBPosition & where, const GBDistance r)
	: position(where), radius(r), velocity(), mass(0),
	next(nil)
{}

GBObject::GBObject(const GBPosition & where, const GBDistance r, const GBVelocity & vel)
	: position(where), radius(r), velocity(vel), mass(0),
	next(nil)
{}

GBObject::GBObject(const GBPosition & where, const GBDistance r, const GBMass m)
	: position(where), radius(r), velocity(), mass(m),
	next(nil)
{}

GBObject::GBObject(const GBPosition & where, const GBDistance r, const GBVelocity & vel, const GBMass m)
	: position(where), radius(r), velocity(vel), mass(m),
	next(nil)
{}

GBObject::~GBObject() {}

GBPosition GBObject::Position() const {
	return position;
}

void GBObject::SetPosition(const GBPosition & newPosition) {
	position = newPosition;
}

void GBObject::MoveBy(const GBPosition & delta) {
	position += delta;
}

void GBObject::MoveBy(const GBDistance deltax, const GBDistance deltay) {
	position.x += deltax;
	position.y += deltay;
}

GBCoordinate GBObject::Left() const {
	return position.x - radius;
}

GBCoordinate GBObject::Top() const {
	return position.y + radius;
}

GBCoordinate GBObject::Right() const {
	return position.x + radius;
}

GBCoordinate GBObject::Bottom() const {
	return position.y - radius;
}

GBVelocity GBObject::Velocity() const {
	return velocity;
}

GBSpeed GBObject::Speed() const {
	return velocity.Norm();
}

void GBObject::SetVelocity(const GBSpeed sx, const GBSpeed sy) {
	velocity.Set(sx, sy);
}

void GBObject::SetSpeed(const GBSpeed speed) {
	velocity.SetNorm(speed);
}

void GBObject::Accelerate(const GBVelocity & deltav) {
	velocity += deltav;
}

void GBObject::Drag(const GBAccelerationScalar friction, const GBRatio linearCoeff, const GBRatio quadraticCoeff) {
	if ( velocity.Zero() ) return;
	GBSpeed speed = Speed();
	speed -= speed * (speed * quadraticCoeff + linearCoeff);
	if ( speed > friction )
		SetSpeed(speed - friction);
	else
		SetVelocity(0, 0);
}

GBDistance GBObject::Radius() const {
	return radius;
}

GBMass GBObject::Mass() const {
	return mass;
}

bool GBObject::Intersects(const GBObject * other) const {
	GBDistance r = radius + other->radius;
	return other->position.x - position.x < r && position.x - other->position.x < r
		&& other->position.y - position.y < r && position.y - other->position.y < r
		&& position.InRange(other->position, r);
}

GBNumber GBObject::OverlapFraction(const GBObject * other) const {
	if ( ! radius ) return 0;
// returns the fraction of our radius that other overlaps
	GBDistance dist = (position - other->position).Norm();	// center-to-center dist
	dist = max(radius + other->radius - dist, 0);	// overlap of edges
	return min(dist / radius, GBNumber(1));	// don't overlap more than 1
}

void GBObject::BasicCollide(GBObject * other) {
	if ( Intersects(other) ) {
		CollideWith(other);
		other->CollideWith(this);
	}
}

void GBObject::SolidCollide(GBObject * other, GBRatio coefficient) {
	if ( Intersects(other) ) {
		CollideWith(other);
		other->CollideWith(this);
		ElasticBounce(other, coefficient);
	}
}

void GBObject::PushBy(const GBMomentum & impulse) {
	if ( mass )
		velocity += impulse / mass;
}

void GBObject::PushBy(const GBMomentumScalar impulse, const GBAngle dir) {
	PushBy(GBFinePoint::MakePolar(impulse, dir));
}

void GBObject::Push(GBObject * other, const GBMomentumScalar impulse) const {
	GBVector cc = other->position - position; // center-to-center
	if ( cc.Norm() == 0 )
		return; // don't push if in same location
	other->PushBy(cc / cc.Norm() * impulse);
}

void GBObject::TakeDamage(const GBDamage, GBSide *) {
}

GBEnergy GBObject::TakeEnergy(const GBEnergy) {
	return 0;
}

GBEnergy GBObject::GiveEnergy(const GBEnergy) {
	return 0;
}

GBEnergy GBObject::MaxTakeEnergy() {
	return 0;
}

GBEnergy GBObject::MaxGiveEnergy() {
	return 0;
}

void GBObject::ElasticBounce(GBObject * other, GBRatio coefficient) {
	GBMass m1 = mass;
	GBMass m2 = other->mass;
	if ( ! m1 || ! m2 )
		return; // can't bounce massless objects
	GBVector cc = other->position - position; // center-to-center
	if ( ! cc.NormSquare() )
		cc.Set(1, 0); // if in same place, pick an arbitrary cc
	GBDistance dist = cc.Norm();
	GBVelocity rv1 = velocity.Projection(cc); // radial component of velocity
	GBVelocity rv2 = other->velocity.Projection(cc);
	if ( velocity.DotProduct(cc) - other->velocity.DotProduct(cc) > 0 ) {
		// if still moving closer...
		GBVelocity center = (rv1 * m1 + rv2 * m2) / (m1 + m2); // velocity of center-of-mass
		Accelerate((rv2 - center) * (m2 * coefficient) / m1 - (rv1 - center));
		other->Accelerate((rv1 - center) * (m1 * coefficient) / m2 - (rv2 - center));
	}
	GBDistance totalr = radius + other->radius;
	GBDistance overlap = totalr - dist;
	if ( overlap > kMaxOverlap ) {
		GBVector away1 = - cc / dist * m2 / (m1 + m2);
		GBVector away2 = cc / dist * m1 / (m1 + m2);
		MoveBy(away1 * kCollisionRepulsionSpeed);
		other->MoveBy(away2 * kCollisionRepulsionSpeed);
		Accelerate(away1 * kCollisionRepulsion);
		other->Accelerate(away2 * kCollisionRepulsion);
	}
}

void GBObject::Think(GBWorld *) {}

void GBObject::Move() {
	position += velocity;
}

void GBObject::Act(GBWorld *) {}

void GBObject::CollideWithWall() {}

void GBObject::CollideWith(GBObject *) {}

void GBObject::CollectStatistics(GBWorld *) const {}

GBObjectClass GBObject::Class() const {
	return ocDecoration;
}

GBSide * GBObject::Owner() const {
	return nil;
}

GBEnergy GBObject::Energy() const {
	return 0;
}

GBNumber GBObject::Interest() const {
	return 0;
}

std::string GBObject::Description() const {
	return "Generic object";
}

std::string GBObject::Details() const {
	return "";
}

const GBColor GBObject::Color() const {
	return GBColor(1);
}

void GBObject::Draw(GBGraphics & g, const GBRect & where, bool /*detailed*/) const {
	g.DrawOpenOval(where, Color());
}

void GBObject::DrawMini(GBGraphics & g, const GBRect & where) const {
	if ( where.Width() < kMaxSquareMiniSize )
		g.DrawSolidRect(where, Color());
	else
		g.DrawSolidOval(where, Color());
}

