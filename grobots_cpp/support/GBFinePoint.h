// GBFinePoint.h
// point (or 2-vector) class based on GBNumber.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBFinePoint_h
#define GBFinePoint_h


#include "GBNumber.h"


class GBFinePoint {
public:
	GBNumber x;
	GBNumber y;
// constructors
	GBFinePoint();
	GBFinePoint(const GBNumber nx, const GBNumber ny);
	GBFinePoint(const GBFinePoint & src);
	~GBFinePoint();
// assignment operators
	GBFinePoint operator=(const GBFinePoint & newvalue);
	GBFinePoint operator+=(const GBFinePoint & addend);
	GBFinePoint operator-=(const GBFinePoint & subtrahend);
	GBFinePoint operator*=(const GBNumber factor);
	GBFinePoint operator/=(const GBNumber divisor);
	GBFinePoint operator*=(const int factor);
	GBFinePoint operator/=(const int divisor);
// math operators
	GBFinePoint operator+(const GBFinePoint & addend) const;
	GBFinePoint operator-(const GBFinePoint & subtrahend) const;
	GBFinePoint operator-() const;
	GBFinePoint operator*(const GBNumber factor) const;
	GBFinePoint operator/(const GBNumber divisor) const;
	GBFinePoint operator*(const int factor) const;
	GBFinePoint operator/(const int divisor) const;
// comparisons
	bool operator==(const GBFinePoint & other) const;
	bool operator!=(const GBFinePoint & other) const;
	bool InRange(const GBFinePoint & other, const GBNumber range) const;
	bool Nonzero() const;
	bool Zero() const;
// GBFinePoint operations
	void Set(const GBNumber nx, const GBNumber ny);
	void SetPolar(const GBNumber r, const GBNumber theta);
	GBFinePoint AddPolar(const GBNumber r, const GBNumber theta) const;
	static GBFinePoint MakePolar(const GBNumber r, const GBNumber theta);
// vector operations
	GBNumber NormSquare() const;
	GBNumber Norm() const;
	GBFinePoint Unit() const;
	void SetNorm(GBNumber norm);
	GBAngle Angle() const;
	void SetAngle(GBAngle angle);
	GBNumber DotProduct(const GBFinePoint & other) const;
	GBFinePoint Projection(const GBFinePoint & base) const;	// projection of self on base
	GBNumber Cross(const GBFinePoint & other) const; // magnitude of cross, kind of
};

typedef GBFinePoint GBVector;

// implementation //

inline GBFinePoint::GBFinePoint()
	: x(0), y(0)
{}

inline GBFinePoint::GBFinePoint(const GBNumber nx, const GBNumber ny)
	: x(nx), y(ny)
{}

inline GBFinePoint::GBFinePoint(const GBFinePoint & src)
	: x(src.x), y(src.y)
{}

inline GBFinePoint::~GBFinePoint() {}

inline GBFinePoint GBFinePoint::operator=(const GBFinePoint & newvalue) {
	x = newvalue.x;
	y = newvalue.y;
	return *this;
}

inline GBFinePoint GBFinePoint::operator+=(const GBFinePoint & addend) {
	x += addend.x;
	y += addend.y;
	return *this;
}

inline GBFinePoint GBFinePoint::operator-=(const GBFinePoint & subtrahend) {
	x -= subtrahend.x;
	y -= subtrahend.y;
	return *this;
}

inline GBFinePoint GBFinePoint::operator*=(const GBNumber factor) {
	x *= factor;
	y *= factor;
	return *this;
}

inline GBFinePoint GBFinePoint::operator*=(const int factor) {
	x *= factor;
	y *= factor;
	return *this;
}

inline GBFinePoint GBFinePoint::operator+(const GBFinePoint & addend) const {
	return GBFinePoint(x + addend.x, y + addend.y);
}

inline GBFinePoint GBFinePoint::operator-(const GBFinePoint & subtrahend) const {
	return GBFinePoint(x - subtrahend.x, y - subtrahend.y);
}

inline GBFinePoint GBFinePoint::operator-() const {
	return GBFinePoint(- x, - y);
}

inline GBFinePoint GBFinePoint::operator*(const GBNumber factor) const {
	return GBFinePoint(x * factor, y * factor);
}

inline GBFinePoint GBFinePoint::operator*(const int factor) const {
	return GBFinePoint(x * factor, y * factor);
}

// comparisons
inline bool GBFinePoint::operator==(const GBFinePoint & other) const {
	return (x == other.x && y == other.y);
}

inline bool GBFinePoint::operator!=(const GBFinePoint & other) const {
	return (x != other.x || y != other.y);
}

inline void GBFinePoint::Set(const GBNumber nx, const GBNumber ny) {
	x = nx;
	y = ny;
}


#endif
