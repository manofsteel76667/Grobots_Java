// GBMilliseconds.h
// portable timer
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBMilliseconds_h
#define GBMilliseconds_h

#if WINDOWS
	#include <time.h>
	typedef clock_t GBMilliseconds;
#else
	typedef long long GBMilliseconds;
#endif

GBMilliseconds Milliseconds();

#endif
