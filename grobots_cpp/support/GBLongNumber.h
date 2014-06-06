// GBLongNumber.h
// Double-precision GBNumber, without most operations, since it's
//   used only as an accumulator for scores.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBLongNumber_h
#define GBLongNumber_h

#include "GBNumber.h"

class GBLongNumber {
	long ipart;
	GBNumber fpart;
public:
// constructors
	GBLongNumber();
	GBLongNumber(int n);
	~GBLongNumber();
// assignment operators
	GBLongNumber & operator=(const GBLongNumber & newvalue);
	GBLongNumber & operator=(int n);
	GBLongNumber & operator+=(const GBNumber addend);
	GBLongNumber & operator+=(const GBLongNumber & addend);
// misc
	long operator/(const GBLongNumber & divisor) const;
	long operator/(long divisor) const;
	bool Nonzero() const;
	bool Zero() const;
	long Round() const;
};


#endif
