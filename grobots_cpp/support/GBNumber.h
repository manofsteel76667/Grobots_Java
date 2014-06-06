// GBNumber.h
// Fixed-point number class, used everywhere.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBNumber_h
#define GBNumber_h

#include "GBErrors.h"

#define USE_GBNUMBER 1
#if USE_GBNUMBER

class GBNumber {
public:
	long data;
// constructors
	GBNumber();
	GBNumber(const GBNumber & src);
	GBNumber(const double f);
	GBNumber(const long l);
	GBNumber(const int i);
	~GBNumber();
// conversions
	// no implicit conversions
	double ToDouble() const;
	operator bool() const;
	int operator !() const;
// assignment operators
	GBNumber operator=(const GBNumber &);
	GBNumber operator+=(const GBNumber &);
	GBNumber operator-=(const GBNumber &);
	GBNumber operator*=(const GBNumber &);
	GBNumber operator/=(const GBNumber &);
	GBNumber operator*=(const int factor);
	GBNumber operator/=(const int divisor);
	GBNumber operator*=(const long factor);
	GBNumber operator/=(const long divisor);
	GBNumber operator*=(const double factor);
	GBNumber operator/=(const double divisor);
	GBNumber operator*=(const short factor);
	GBNumber operator/=(const short divisor);
// arithmetic
	GBNumber operator+(int) const;
	GBNumber operator-(int) const;
	GBNumber operator+(double) const;
	GBNumber operator-(double) const;
	GBNumber operator+(short) const;
	GBNumber operator-(short) const;
	GBNumber operator*(const GBNumber &) const;
	GBNumber operator/(const GBNumber &) const;
	GBNumber operator*(const int factor) const;
	GBNumber operator/(const int divisor) const;
	GBNumber operator*(const long factor) const;
	GBNumber operator/(const long divisor) const;
	GBNumber operator*(const double factor) const;
	GBNumber operator/(const double divisor) const;
	GBNumber operator*(const short factor) const;
	GBNumber operator/(const short divisor) const;
// comparisons
	bool operator==(const GBNumber &) const;
	bool operator!=(const GBNumber &) const;
	bool operator<(const GBNumber &) const;
	bool operator>(const GBNumber &) const;
	bool operator<=(const GBNumber &) const;
	bool operator>=(const GBNumber &) const;
	bool operator==(const int other) const;
	bool operator!=(const int other) const;
	bool operator<(const int other) const;
	bool operator>(const int other) const;
	bool operator<=(const int other) const;
	bool operator>=(const int other) const;
	bool operator==(const double other) const;
	bool operator!=(const double other) const;
	bool operator<(const double other) const;
	bool operator>(const double other) const;
	bool operator<=(const double other) const;
	bool operator>=(const double other) const;
// raw access
	static GBNumber MakeRaw(const long raw);
};

GBNumber operator-(const GBNumber &);
GBNumber operator+(const GBNumber &, const GBNumber &);
GBNumber operator-(const GBNumber &, const GBNumber &);

GBNumber sqrt(const GBNumber &);
GBNumber pow(const GBNumber & base, const GBNumber & ex);
long floor(const GBNumber &);
long ceil(const GBNumber &);
long round(const GBNumber &);
GBNumber abs(const GBNumber &);
GBNumber signum(const GBNumber &);

GBNumber reorient(const GBNumber &);
GBNumber cos(const GBNumber &);
GBNumber sin(const GBNumber &);
GBNumber tan(const GBNumber &);
GBNumber acos(const GBNumber &);
GBNumber asin(const GBNumber &);
GBNumber atan(const GBNumber &);
GBNumber atan2(const GBNumber & y, const GBNumber & x);

bool IsInteger(const GBNumber &);
GBNumber fpart(const GBNumber &);

#else
typedef float GBNumber;
#endif

typedef GBNumber GBAngle;
typedef GBNumber GBCoordinate;

// errors //

class GBArithmeticError : public GBError {
public:
	GBArithmeticError();
	~GBArithmeticError();
	string ToString() const;
};

class GBDivideByZeroError : public GBArithmeticError {
public:
	string ToString() const;
};

class GBOverflowError : public GBArithmeticError {
public:
	string ToString() const;
};

class GBImaginaryError : public GBArithmeticError {
public:
	string ToString() const;
};

// implementation //

#if USE_GBNUMBER

const int kNumFractionBits = 12;
const long kIntegerPartMask = -1 << kNumFractionBits;
const long kFractionalPartMask = ~ kIntegerPartMask;
const long kMaxValueRaw = 0x7FFFFFFF;
const long kMaxValue = kMaxValueRaw >> kNumFractionBits;


inline GBNumber::GBNumber()
	: data(0)
{}

inline GBNumber::GBNumber(const GBNumber & src)
	: data(src.data)
{}

inline GBNumber::GBNumber(const int i)
	: data(i << kNumFractionBits)
{
	if ( i > kMaxValue || i < - kMaxValue )
		throw GBOverflowError();
}

inline GBNumber::GBNumber(const long l)
	: data(l << kNumFractionBits)
{
	if ( l > kMaxValue || l < - kMaxValue )
		throw GBOverflowError();
}

inline GBNumber::~GBNumber() {}

inline int GBNumber::operator!() const {
	return ! data;
}

inline GBNumber GBNumber::operator=(const GBNumber &newvalue) {
	data = newvalue.data;
	return *this;
}

inline GBNumber GBNumber::operator+=(const GBNumber &addend) {
	data += addend.data;
	return *this;
}

inline GBNumber GBNumber::operator-=(const GBNumber &subtrahend) {
	data -= subtrahend.data;
	return *this;
}

inline GBNumber GBNumber::operator*=(const int factor){
	data *= factor;
	return *this;
}

inline GBNumber GBNumber::operator*=(const long factor){
	data *= factor;
	return *this;
}

inline GBNumber operator+(const GBNumber &a, const GBNumber &b) {
	return GBNumber::MakeRaw(a.data + b.data);
}

inline GBNumber operator-(const GBNumber &a, const GBNumber &b) {
	return GBNumber::MakeRaw(a.data - b.data);
}

inline GBNumber operator-(const GBNumber &x) {
	return GBNumber::MakeRaw(- x.data);
}

inline GBNumber GBNumber::operator+(int addend) const {
	return *this + GBNumber(addend);
}

inline GBNumber GBNumber::operator-(int subtrahend) const {
	return *this - GBNumber(subtrahend);
}

inline GBNumber GBNumber::operator+(double addend) const {
	return *this + GBNumber(addend);
}

inline GBNumber GBNumber::operator-(double subtrahend) const {
	return *this - GBNumber(subtrahend);
}

inline GBNumber GBNumber::operator+(short addend) const {
	return *this + GBNumber(addend);
}

inline GBNumber GBNumber::operator-(short subtrahend) const {
	return *this - GBNumber(subtrahend);
}

inline GBNumber GBNumber::operator*(const int factor) const {
	return MakeRaw(data * factor);
}

inline GBNumber GBNumber::operator*(const long factor) const {
	return MakeRaw(data * factor);
}

inline bool GBNumber::operator==(const GBNumber &other) const {
	return (data == other.data);}

inline bool GBNumber::operator!=(const GBNumber &other) const {
	return (data != other.data);}

inline bool GBNumber::operator<(const GBNumber &other) const {
	return (data < other.data);}

inline bool GBNumber::operator>(const GBNumber &other) const {
	return (data > other.data);}

inline bool GBNumber::operator<=(const GBNumber &other) const {
	return (data <= other.data);}

inline bool GBNumber::operator>=(const GBNumber &other) const {
	return (data >= other.data);}

inline bool GBNumber::operator==(const int other) const {
	return (data == other << kNumFractionBits);}

inline bool GBNumber::operator!=(const int other) const {
	return (data != other << kNumFractionBits);}

inline bool GBNumber::operator<(const int other) const {
	return (data < other << kNumFractionBits);}

inline bool GBNumber::operator>(const int other) const {
	return (data > other << kNumFractionBits);}

inline bool GBNumber::operator<=(const int other) const {
	return (data <= other << kNumFractionBits);}

inline bool GBNumber::operator>=(const int other) const {
	return (data >= other << kNumFractionBits);}

inline GBNumber::operator bool() const {
	return data != 0;
}

inline GBNumber GBNumber::MakeRaw(const long raw) {
	GBNumber rval;
	rval.data = raw;
	return rval;
}
#endif

template <typename T>
T square(T x) { return x * x; }

GBNumber mod(const GBNumber &x, const GBNumber &divisor);
GBNumber rem(const GBNumber &x, const GBNumber &divisor);

using std::min;
using std::max;

GBNumber max(const GBNumber &, int);

template <typename T>
T clamp(T x, T low, T high) { return x < low ? low : x > high ? high : x; }

// constants
const GBNumber kEpsilon = GBNumber::MakeRaw(1);
const GBNumber kInfinity = GBNumber::MakeRaw(0x7FFFFFFF);
const GBNumber kPi = 3.14159265358979;
const GBNumber kE = 2.71828182846;

#endif
