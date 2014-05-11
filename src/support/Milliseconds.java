// GBMilliseconds.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#if WINDOWS
	#include <time.h>
	typedef clock_t GBMilliseconds;
#else
	typedef long long GBMilliseconds;
#endif

GBMilliseconds Milliseconds();

#include "Platform.h"
#include "Milliseconds.h"

#if MAC && ! MAC_OS_X
	#include <Events.h>
#elif UNIX || WINDOWS
	#include <time.h>
#endif

#if MAC
GBMilliseconds Milliseconds() {
	return (GBMilliseconds)(TickCount()) * 1000 / 60;
}
#elif UNIX || WINDOWS
GBMilliseconds Milliseconds() { return clock(); }
//FIXME this is process time, not real time
#else
	#warning "Need Milliseconds"
#endif

