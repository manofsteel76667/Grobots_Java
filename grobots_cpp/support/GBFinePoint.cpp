// GBFinePoint.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBFinePoint.h"
#include "GBErrors.h"
#include "GBStringUtilities.h"
#include <math.h>

// almost everything is inline

GBFinePoint GBFinePoint::operator/=(const GBNumber divisor) {
	x /= divisor;
	y /= divisor;
	return *this;
}

GBFinePoint GBFinePoint::operator/=(const int divisor) {
	x /= divisor;
	y /= divisor;
	return *this;
}

GBFinePoint GBFinePoint::operator/(const GBNumber divisor) const {
	return GBFinePoint(x / divisor, y / divisor);
}

GBFinePoint GBFinePoint::operator/(const int divisor) const {
	return GBFinePoint(x / divisor, y / divisor);
}

bool GBFinePoint::InRange(const GBFinePoint & other, const GBNumber range) const {
	double r = range.ToDouble();
	double dx = (x - other.x).ToDouble();
	double dy = (y - other.y).ToDouble();
	return (dx * dx + dy * dy) <= r * r;
}

bool GBFinePoint::Nonzero() const {
	return x || y;
}

bool GBFinePoint::Zero() const {
	return ! x && ! y;
}

void GBFinePoint::SetPolar(const GBNumber r, const GBNumber theta) {
	x = r * cos(theta);
	y = r * sin(theta);
}

GBFinePoint GBFinePoint::AddPolar(const GBNumber r, const GBNumber theta) const {
	return GBFinePoint(x + r * cos(theta), y + r * sin(theta));
}

GBFinePoint GBFinePoint::MakePolar(const GBNumber r, const GBNumber theta) {
	return GBFinePoint(r * cos(theta), r * sin(theta));
}

GBNumber GBFinePoint::NormSquare() const {
	return x * x + y * y;
}

GBNumber GBFinePoint::Norm() const {
	double xd = x.ToDouble(), yd = y.ToDouble();
	return sqrt(xd * xd + yd * yd);
}

GBFinePoint GBFinePoint::Unit() const {
	return MakePolar(1, Angle());
}

void GBFinePoint::SetNorm(GBNumber norm) {
	SetPolar(norm, Angle());
	//*this *= norm / Norm(); //faster but could overflow in some cases
}

GBAngle GBFinePoint::Angle() const {
	return atan2(y.ToDouble(), x.ToDouble());
}

void GBFinePoint::SetAngle(GBAngle angle) {
	SetPolar(Norm(), angle);
}

GBNumber GBFinePoint::DotProduct(const GBFinePoint & other) const {
	return x * other.x + y * other.y;
}

GBFinePoint GBFinePoint::Projection(const GBFinePoint & base) const {
	return base * DotProduct(base) / base.NormSquare();
}

GBNumber GBFinePoint::Cross(const GBFinePoint & other) const {
	return x * other.y - other.x * y;
}

