// GBBrainSpec.h
// abstract brainspec
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBBrainSpec_h
#define GBBrainSpec_h

#include "GBTypes.h"

class GBBrain;

typedef unsigned short GBLineNumber;

class GBBrainSpec {
public:
	GBBrainSpec();
	virtual ~GBBrainSpec();
	virtual GBBrain * MakeBrain() const = 0;
	virtual GBBrainSpec * Copy() const = 0;
// computed
	virtual GBEnergy Cost() const = 0;
	virtual GBMass Mass() const = 0;
// loading
	virtual void ParseLine(const string & line, GBLineNumber lineNum) = 0;
	virtual void Check() = 0; // check OK to use
};

// errors //

class GBBrainError : public GBSimulationError {
public:
	GBBrainError();
	~GBBrainError();
	string ToString() const;
};

class GBUnknownInstructionError : public GBBrainError {
public:
	string ToString() const;
};

class GBUnknownHardwareVariableError : public GBBrainError {
public:
	string ToString() const;
};

class GBBadSymbolIndexError : public GBBrainError {
public:
	string ToString() const;
};

class GBNotIntegerError : public GBBrainError {
public:
	string ToString() const;
};

class GBReadOnlyError : public GBBrainError {
public:
	string ToString() const;
};


#endif
