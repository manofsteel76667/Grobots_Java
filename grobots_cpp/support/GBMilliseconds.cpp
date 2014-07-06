/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


#include "GBPlatform.h"
#include "GBMilliseconds.h"

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

