/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


#include "GBBrainSpec.h"
#include "GBTypes.h"


GBBrainSpec::GBBrainSpec() {}
GBBrainSpec::~GBBrainSpec() {}

// errors //

GBBrainError::GBBrainError() {}
GBBrainError::~GBBrainError() {}

string GBBrainError::ToString() const {
	return "unspecified brain error";
}

string GBUnknownInstructionError::ToString() const {
	return "illegal or unimplemented instruction";
}

string GBUnknownHardwareVariableError::ToString() const {
	return "illegal or unimplemented hardware variable";
}

string GBBadSymbolIndexError::ToString() const {
	return "invalid symbol index";
}

string GBNotIntegerError::ToString() const {
	return "value was not an integer as required";
}

string GBReadOnlyError::ToString() const {
	return "tried to write a read-only variable";
}


