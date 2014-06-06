// GBNumber.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBNumber.h"
#include <math.h>
#include "GBStringUtilities.h"

#if USE_GBNUMBER
// many methods are inline, and found in GBNumber.h

GBNumber::GBNumber(const double f)
	: data(f * (1 << kNumFractionBits))
{
	if ( f > (double)kMaxValue || f < - (double)kMaxValue )
		throw GBOverflowError();
}

double GBNumber::ToDouble() const {
	return (double)data / (double)(1 << kNumFractionBits);
}

// ToString is no longer a method, and is now in GBStringUtilities.

GBNumber GBNumber::operator*=(const GBNumber &factor) {
	GBLongLong temp = ((GBLongLong)data * (GBLongLong)factor.data) >> kNumFractionBits;
	if ( temp > kMaxValueRaw || temp < - kMaxValueRaw )
		throw GBOverflowError();
	data = temp;
	return *this;
}

GBNumber GBNumber::operator/=(const GBNumber &divisor) {
	if ( ! divisor.data ) throw GBDivideByZeroError();
	data = ((GBLongLong)data << kNumFractionBits) / (GBLongLong)divisor.data;
	return *this;
}

GBNumber GBNumber::operator/=(const int divisor) {
	if ( ! divisor ) throw GBDivideByZeroError();
	data /= divisor;
	return *this;
}

GBNumber GBNumber::operator/=(const long divisor) {
	if ( ! divisor ) throw GBDivideByZeroError();
	data /= divisor;
	return *this;
}

GBNumber GBNumber::operator*=(const double factor){
	data = factor * data;
	return *this;
}

GBNumber GBNumber::operator/=(const double divisor) {
	if ( ! divisor ) throw GBDivideByZeroError();
	data = data / divisor;
	return *this;
}

GBNumber GBNumber::operator*(const GBNumber &factor) const {
	GBLongLong temp = ((GBLongLong)data * (GBLongLong)factor.data) >> kNumFractionBits;
	if ( temp > kMaxValueRaw || temp < - kMaxValueRaw )
		throw GBOverflowError();
	return MakeRaw(temp);
}

GBNumber GBNumber::operator*(const double factor) const {
	return MakeRaw(factor * data);
}

GBNumber GBNumber::operator*(const short factor) const {
	return MakeRaw(factor * data);
}

GBNumber GBNumber::operator/(const GBNumber &divisor) const {
	if ( ! divisor.data ) throw GBDivideByZeroError();
	return MakeRaw(((GBLongLong)data << kNumFractionBits) / (GBLongLong)divisor.data);
}

GBNumber GBNumber::operator/(const int divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return MakeRaw(data / divisor);
}

GBNumber GBNumber::operator/(const long divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return MakeRaw(data / divisor);
}

GBNumber GBNumber::operator/(const double divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return MakeRaw(data / divisor); // is this right?
}

GBNumber GBNumber::operator/(const short divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return MakeRaw(data / divisor); // is this right?
}

GBNumber mod(const GBNumber &num, const GBNumber &divisor) {
	if ( ! divisor.data ) throw GBDivideByZeroError();
	if ( num.data < 0 ) {
		if ( divisor < 0 ) return GBNumber::MakeRaw(- (- num.data) % - divisor.data);
		long remainder = (- num.data) % divisor.data;
		if ( remainder ) return GBNumber::MakeRaw(divisor.data - remainder);
		return 0;
	}
	if ( divisor < 0 ) {
		long remainder = num.data % - divisor.data;
		if ( remainder ) return GBNumber::MakeRaw(divisor.data + remainder);
		return 0;
	}
	return GBNumber::MakeRaw(num.data % divisor.data);
}

GBNumber rem(const GBNumber &num, const GBNumber &divisor) {
	if ( ! divisor.data ) throw GBDivideByZeroError();
	// depending on % being REM for now...
	return GBNumber::MakeRaw(num.data % divisor.data);
}

bool GBNumber::operator==(const double other) const {
	return ToDouble() == other;}

bool GBNumber::operator!=(const double other) const {
	return ToDouble() != other;}

bool GBNumber::operator<(const double other) const {
	return ToDouble() < other;}

bool GBNumber::operator>(const double other) const {
	return ToDouble() > other;}

bool GBNumber::operator<=(const double other) const {
	return ToDouble() <= other;}

bool GBNumber::operator>=(const double other) const {
	return ToDouble() >= other;}

GBNumber sqrt(const GBNumber & x) {
	if ( x < 0 ) throw GBImaginaryError();
	double temp = x.data / (double)(1 << kNumFractionBits);
	return GBNumber(sqrt(temp));
}

GBNumber pow(const GBNumber & base, const GBNumber & ex) {
	return pow(base.ToDouble(), ex.ToDouble());
}

GBNumber signum(const GBNumber &x) {
	if ( x.data < 0 )
		return -1;
	else if ( x.data > 0 )
		return 1;
	else
		return 0;
}

long floor(const GBNumber &x) {
	return x.data >> kNumFractionBits;
}

long ceil(const GBNumber &x) {
	if ( x.data & kFractionalPartMask )
		return (x.data >> kNumFractionBits) + 1;
	else
		return x.data >> kNumFractionBits;
}

GBNumber abs(const GBNumber &x) {
	return x.data < 0 ? - x : x;
}

long truncate(const GBNumber &x) {
	return x.data < 0 ? ceil(x) : floor(x);
}

long round(const GBNumber & x) {
	return floor(x + 0.5);
}

bool IsInteger(const GBNumber &x) {
	return ! (x.data & kFractionalPartMask);
}

GBNumber fpart(const GBNumber &x) {
	return x - GBNumber(truncate(x));
}

GBNumber reorient(const GBNumber &theta) {
	return mod(theta + kPi - kEpsilon, kPi * 2) - kPi + kEpsilon;
}

GBNumber cos(const GBNumber & x) { return cos(x.ToDouble()); }
GBNumber sin(const GBNumber & x) { return sin(x.ToDouble()); }
GBNumber tan(const GBNumber & x) { return tan(x.ToDouble()); }
GBNumber acos(const GBNumber & x) { return acos(x.ToDouble()); }
GBNumber asin(const GBNumber & x) { return asin(x.ToDouble()); }
GBNumber atan(const GBNumber & x) { return atan(x.ToDouble()); }

GBNumber atan2(const GBNumber & y, const GBNumber & x) {
	return atan2(y.ToDouble(), x.ToDouble());
}
#endif

GBNumber max(const GBNumber &a, int b) {
	return a < b ? GBNumber(b) : a;
}

// errors //

GBArithmeticError::GBArithmeticError() {}
GBArithmeticError::~GBArithmeticError() {}

string GBArithmeticError::ToString() const {
	return "arithmetic error";
}

string GBDivideByZeroError::ToString() const {
	return "division by zero";
}

string GBOverflowError::ToString() const {
	return "arithmetic overflow";
}

string GBImaginaryError::ToString() const {
	return "imaginary result";
}

