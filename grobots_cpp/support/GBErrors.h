// GBErrors.h
// lots of exception classes, and some error-reporting functions
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBErrors_h
#define GBErrors_h

#include <string>

using std::string;


class GBError {
public:
	GBError();
	virtual ~GBError();
	virtual string ToString() const;
};

class GBGenericError : public GBError {
	string message;
	GBGenericError();
public:
	GBGenericError(const string & msg);
	~GBGenericError();
	string ToString() const;
};

class GBBadConstructorError : public GBError {
public:
	string ToString() const;
};

class GBOutOfMemoryError : public GBError {
public:
	string ToString() const;
};

class GBNilPointerError : public GBError {
public:
	string ToString() const;
};

class GBSimulationError : public GBError {
public:
	GBSimulationError();
	~GBSimulationError();
	string ToString() const;
};

class GBBadObjectClassError : public GBSimulationError {
public:
	string ToString() const;
};

class GBBadComputedValueError : public GBSimulationError {
public:
	string ToString() const;
};

class GBBadArgumentError : public GBError {
public:
	string ToString() const;
};

class GBIndexOutOfRangeError : public GBBadArgumentError {
public:
	string ToString() const;
};

class GBTooManyIterationsError : public GBError {
public:
	string ToString() const;
};

class GBRestart {
public:
	GBRestart();
	virtual ~GBRestart();
	virtual string ToString() const;
};

class GBAbort : public GBRestart {
public:
	string ToString() const;
};


void FatalError(const string & message);
void NonfatalError(const string & message);
bool Confirm(const string & message, const string & operation = "OK");

#endif
