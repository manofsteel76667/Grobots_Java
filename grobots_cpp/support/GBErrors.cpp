// GBErrors.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBErrors.h"
#include "GBStringUtilities.h"
#include <stdlib.h>

#if HEADLESS
	#include <iostream>
	using std::cerr;
	using std::endl;
#elif MAC && ! MAC_OS_X
	#include <Dialogs.h>
#elif MAC && MAC_OS_X
	#include <Carbon/Carbon.h>
#elif WINDOWS
	#include <stdlib.h>
	#include <windows.h>
#endif


GBError::GBError() {}
GBError::~GBError() {}

string GBError::ToString() const {
	return "undifferentiated GBError (shouldn't be used)";
}

GBGenericError::GBGenericError(const string & msg)
	: message(msg)
{}

GBGenericError::GBGenericError() : message("") {}
GBGenericError::~GBGenericError() {}

string GBGenericError::ToString() const {
	return message;
}

string GBBadConstructorError::ToString() const {
	return "forbidden constructor called";
}

string GBOutOfMemoryError::ToString() const {
	return "out of memory";
}

string GBNilPointerError::ToString() const {
	return "nil pointer passed";
}

GBSimulationError::GBSimulationError() {}
GBSimulationError::~GBSimulationError() {}

string GBSimulationError::ToString() const {
	return "unspecified simulation error";
}

string GBBadObjectClassError::ToString() const {
	return "bad GBObjectClass";
}

string GBBadComputedValueError::ToString() const {
	return "a computed value such as mass or cost was not reasonable";
}

string GBBadArgumentError::ToString() const {
	return "bad argument (no more detail available)";
}

string GBIndexOutOfRangeError::ToString() const {
	return "index out of range";
}

string GBTooManyIterationsError::ToString() const {
	return "a loop had too many iterations";
}

GBRestart::GBRestart() {}
GBRestart::~GBRestart() {}

string GBRestart::ToString() const {
	return "unspecified restart";
}

string GBAbort::ToString() const {
	return "abort";
}

#if HEADLESS
void FatalError(const string & message) {
	cerr << "Fatal error: " << message << endl;
	exit(EXIT_FAILURE);
}

void NonfatalError(const string & message) {
	cerr << message << endl;
}
#elif MAC
const short kFatalErrorAlertID = 600;
const short kNonfatalErrorAlertID = 601;
const short kAbortableErrorAlertID = 602;
const short kConfirmationAlertID = 700;

void FatalError(const string & message) {
	Str255 str;
	ToPascalString(message, str);
	ParamText(str, nil, nil, nil);
	Alert(kFatalErrorAlertID, nil);
	ExitToShell();
}

void NonfatalError(const string & message) {
	Str255 str;
	ToPascalString(message, str);
	ParamText(str, nil, nil, nil);
	switch ( Alert(kAbortableErrorAlertID, nil) ) {
		case 1: // continue
		default:
			return;
		case 2: // abort
			throw GBAbort();
		case 3: // quit
			ExitToShell();
	}
}

bool Confirm(const string & message, const string & operation) {
	Str255 str, str2;
	ToPascalString(message, str);
	ToPascalString(operation, str2);
	ParamText(str, str2, nil, nil);
	return Alert(kConfirmationAlertID, nil) == 1;
}

#elif WINDOWS

void FatalError(const string & message) {
	MessageBox(NULL, message.c_str(), "Fatal Error", MB_OK | MB_ICONWARNING);
	exit(1);
}

void NonfatalError(const string & message) {
	switch ( MessageBox(NULL, message.c_str(), "Nonfatal Error", MB_ICONWARNING | MB_ABORTRETRYIGNORE) ) {
		case IDIGNORE: // continue, clicked ignore
		default:
			return;
		case IDRETRY: // abort, clicked retry
			throw GBAbort();
		case IDABORT: // quit, clicked abort
			exit(1);
	}
}

bool Confirm(const string & message, const string & operation) {
	//TODO this should actually ask
	return true;
}

#else
	#warning "Need error alert functions."
#endif
