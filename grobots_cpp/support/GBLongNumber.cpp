// GBLongNumber.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBLongNumber.h"

GBLongNumber::GBLongNumber()
	: ipart(0), fpart(0)
{}

GBLongNumber::GBLongNumber(int n)
	: ipart(n), fpart(0)
{}

GBLongNumber::~GBLongNumber() {}

GBLongNumber & GBLongNumber::operator =(const GBLongNumber & newvalue) {
	ipart = newvalue.ipart;
	fpart = newvalue.fpart;
	return *this;
}

GBLongNumber & GBLongNumber::operator =(int n) {
	ipart = n;
	fpart = 0;
	return *this;
}

GBLongNumber & GBLongNumber::operator +=(const GBNumber addend) {
	fpart += addend;
	if ( fpart >= 1 || fpart < 0 ) {
		ipart += floor(fpart);
		fpart = ::fpart(fpart);
	}
	return *this;
}

GBLongNumber & GBLongNumber::operator +=(const GBLongNumber & addend) {
	ipart += addend.ipart;
	fpart += addend.fpart;
	if ( fpart >= 1 || fpart < 0 ) {
		ipart += floor(fpart);
		fpart = ::fpart(fpart);
	}
	return *this;
}

long GBLongNumber::operator /(const GBLongNumber & divisor) const {
	return *this / divisor.Round();
}

long GBLongNumber::operator /(long divisor) const {
	if ( ! divisor ) return 0;
	return Round() / divisor;
}

bool GBLongNumber::Nonzero() const {
	return ipart != 0 || fpart;
}

bool GBLongNumber::Zero() const {
	return ipart == 0 && ! fpart;
}

long GBLongNumber::Round() const {
	return ipart + round(fpart);
}
