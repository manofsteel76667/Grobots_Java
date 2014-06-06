// GBStackBrainSpec.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBStackBrainSpec.h"
#include "GBStackBrainOpcodes.h"
#include "GBErrors.h"
#include "GBStackBrain.h"

const GBRatio kBrainMassPerCost = 0.01;

const GBRatio kCostPerInstruction = 0.02;
const GBRatio kMassPerInstruction = kCostPerInstruction * kBrainMassPerCost;
const GBRatio kCostPerConstant = 0.03;
const GBRatio kMassPerConstant = kCostPerConstant * kBrainMassPerCost;
const GBRatio kCostPerVariable = 0.1;
const GBRatio kMassPerVariable = kCostPerVariable * kBrainMassPerCost;
const GBRatio kCostPerLabel = 0.02;
const GBRatio kMassPerLabel = kCostPerLabel * kBrainMassPerCost;


class GBSymbolError : public GBBrainError {
protected:
	string sym;
public:
	GBSymbolError(const string & s);
	string ToString() const = 0;
};

class GBUnknownSymbolError : public GBSymbolError {
public:
	GBUnknownSymbolError(const string & s);
	string ToString() const;
};

class GBUnresolvedSymbolError : public GBSymbolError {
public:
	GBUnresolvedSymbolError(const string & s);
	string ToString() const;
};

class GBDuplicateSymbolError : public GBSymbolError {
public:
	GBDuplicateSymbolError(const string & s);
	string ToString() const;
};


GBSymbolError::GBSymbolError(const string & s)
	: GBBrainError(), sym(s) {}

GBUnknownSymbolError::GBUnknownSymbolError(const string & s)
	: GBSymbolError(s) {}

string GBUnknownSymbolError::ToString() const {
	return string("undefined symbol: ") + sym;
}

GBUnresolvedSymbolError::GBUnresolvedSymbolError(const string & s)
	: GBSymbolError(s) {}

string GBUnresolvedSymbolError::ToString() const {
	return string("unresolved forward symbol definition: ") + sym;
}

GBDuplicateSymbolError::GBDuplicateSymbolError(const string & s)
	: GBSymbolError(s) {}

string GBDuplicateSymbolError::ToString() const {
	return string("duplicate symbol: ") + sym;
}


GBSymbol::GBSymbol()
	: name()
{}

GBSymbol::GBSymbol(const string & n)
	: name(n)
{}

GBSymbol::~GBSymbol() {}

GBSymbol & GBSymbol::operator=(const GBSymbol & arg) {
	if ( &arg == this ) return *this;
	name = arg.name;
	return *this;
}

bool GBSymbol::NameEquivalent(const string & other) const {
	return NamesEquivalent(name, other);
}

GBConstant::GBConstant()
	: GBSymbol(), value(0)
{}

GBConstant::GBConstant(const string & n, GBStackDatum v)
	: GBSymbol(n), value(v)
{}

GBConstant::~GBConstant() {}

GBConstant & GBConstant::operator=(const GBConstant & arg) {
	if ( &arg == this ) return *this;
	name = arg.name;
	value = arg.value;
	return *this;
}

GBVectorSymbol::GBVectorSymbol()
	: GBSymbol(), value()
{}

GBVectorSymbol::GBVectorSymbol(const string & n, const GBVector & v)
	: GBSymbol(n), value(v)
{}

GBVectorSymbol::~GBVectorSymbol() {}

GBVectorSymbol & GBVectorSymbol::operator=(const GBVectorSymbol & arg) {
	if ( &arg == this ) return *this;
	name = arg.name;
	value = arg.value;
	return *this;
}

GBLabel::GBLabel()
	: GBSymbol(), address(-1), gensym(false)
{}

GBLabel::GBLabel(const string & n, GBStackAddress a, bool g)
	: GBSymbol(n), address(a), gensym(g)
{}

GBLabel::~GBLabel() {}

GBLabel & GBLabel::operator=(const GBLabel & arg) {
	if ( &arg == this ) return *this;
	name = arg.name;
	address = arg.address;
	gensym = arg.gensym;
	return *this;
}

bool GBLabel::NameEquivalent(const string & other) const {
	if ( gensym ) return false; // gensyms never match by name
	return GBSymbol::NameEquivalent(other);
}


GBStackBrainSpec::GBStackBrainSpec()
	: code(), lineNumbers(),
	constants(), variables(), vectorVariables(),
	labels(), startingLabel(-1),
	cStack(),
	gensymCounter(0)
{}

GBStackBrainSpec::GBStackBrainSpec(const GBStackBrainSpec & original)
	: code(original.code),
	lineNumbers(original.lineNumbers),
	constants(original.constants),
	variables(original.variables),
	vectorVariables(original.vectorVariables),
	labels(original.labels),
	startingLabel(original.startingLabel),
	cStack(original.cStack),
	gensymCounter(original.gensymCounter)
{}

GBStackBrainSpec::~GBStackBrainSpec() {}

GBBrainSpec * GBStackBrainSpec::Copy() const {
	return new GBStackBrainSpec(*this);
}

GBBrain * GBStackBrainSpec::MakeBrain() const {
	return new GBStackBrain(this);
}

GBEnergy GBStackBrainSpec::Cost() const {
	return kCostPerInstruction * code.size()
		+ kCostPerConstant * constants.size()
		+ kCostPerVariable * variables.size()
		+ 2 * kCostPerVariable * vectorVariables.size()
		+ kCostPerLabel * labels.size();
}

GBMass GBStackBrainSpec::Mass() const {
	return kMassPerInstruction * code.size()
		+ kMassPerConstant * constants.size()
		+ kMassPerVariable * variables.size()
		+ 2 * kMassPerVariable * vectorVariables.size()
		+ kMassPerLabel * labels.size();
}

void GBStackBrainSpec::AddInstruction(GBStackInstruction ins, GBLineNumber line) {
	code.push_back(ins);
	lineNumbers.push_back(line);
}

void GBStackBrainSpec::AddInstruction(GBStackInstruction type, GBStackInstruction index, GBLineNumber line) {
	code.push_back((type << kOpcodeTypeShift) + index);
	lineNumbers.push_back(line);
}

void GBStackBrainSpec::CPush(GBSymbolIndex value) {
	cStack.push_back(value);
}

GBSymbolIndex GBStackBrainSpec::CPeek() {
	if ( cStack.empty() ) throw GBStackUnderflowError();
	return cStack[cStack.size() - 1];
}

GBSymbolIndex GBStackBrainSpec::CPop() {
	if ( cStack.empty() ) throw GBStackUnderflowError();
	GBSymbolIndex i = cStack[cStack.size() - 1];
	cStack.pop_back();
	return i;
}

void GBStackBrainSpec::ExecuteCWord(long index, GBLineNumber line) {
	GBSymbolIndex temp, temp2;
	switch ( index ) {
		case cwNop: break;
		case cwIf:
			temp = AddGensym("if-skip-g");
			AddLabelRead(temp, line); AddPrimitiveCall(opNotIfGo, line);
			CPush(temp);
			break;
		case cwNotIf:
			temp = AddGensym("nif-skip-g");
			AddLabelRead(temp, line); AddPrimitiveCall(opIfGo, line);
			CPush(temp);
			break;
		case cwElse:
			temp = AddGensym("else-skip-g");
			AddLabelRead(temp, line); AddPrimitiveCall(opJump, line);
		// and the address
			temp2 = CPop();
			ResolveGensym(temp2);
			CPush(temp);
			break;
		case cwThen:
			temp = CPop();
			ResolveGensym(temp);
			break;
		case cwAndIf:
			temp = CPeek();
			AddLabelRead(temp, line); AddPrimitiveCall(opNotIfGo, line);
			break;
		case cwNotAndIf:
			temp = CPeek();
			AddLabelRead(temp, line); AddPrimitiveCall(opIfGo, line);
			break;
		case cwCElse: // end skip -- end
			temp = CPop();
			temp2 = CPeek();
			AddLabelRead(temp2, line); AddPrimitiveCall(opJump, line);
			ResolveGensym(temp);
			break;
		case cwDo: // -- loop-start
			temp = AddGensym("loop-start-g");
			ResolveGensym(temp);
			CPush(temp);
			break;
		case cwLoop: // loop-start loop-end --
			temp = CPop();
			AddLabelRead(CPop(), line); AddPrimitiveCall(opJump, line);
			ResolveGensym(temp);
			break;
		case cwForever: // loop-start --
			AddLabelRead(CPop(), line); AddPrimitiveCall(opJump, line);
			break;
		case cwWhile: // -- loop-end
			temp = AddGensym("loop-end-g"); CPush(temp);
			AddLabelRead(temp, line); AddPrimitiveCall(opNotIfGo, line);
			break;
		case cwUntil: // -- loop-end
			temp = AddGensym("loop-end-g"); CPush(temp);
			AddLabelRead(temp, line); AddPrimitiveCall(opIfGo, line);
			break;
		case cwWhileLoop: // loop-start -- 
			AddLabelRead(CPop(), line); AddPrimitiveCall(opIfGo, line);
			break;
		case cwUntilLoop: // loop-start --
			AddLabelRead(CPop(), line); AddPrimitiveCall(opNotIfGo, line);
			break;
		default:
			throw GBUnknownInstructionError();
	}
}

void GBStackBrainSpec::AddPrimitiveCall(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= kNumPrimitives || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otPrimitive, index, line);
}

void GBStackBrainSpec::AddHardwareRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= kNumHardwareVariables || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otHardwareRead, index, line);
}

void GBStackBrainSpec::AddHardwareWrite(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= kNumHardwareVariables || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otHardwareWrite, index, line);
}

void GBStackBrainSpec::AddHardwareVectorRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= kNumHardwareVectors || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otHardwareVectorRead, index, line);
}

void GBStackBrainSpec::AddHardwareVectorWrite(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= kNumHardwareVectors || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otHardwareVectorWrite, index, line);
}

void GBStackBrainSpec::AddConstantRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= constants.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otConstantRead, index, line);
}

void GBStackBrainSpec::AddVariableRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= variables.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otVariableRead, index, line);
}

void GBStackBrainSpec::AddVariableWrite(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= variables.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otVariableWrite, index, line);
}

void GBStackBrainSpec::AddVectorVariableRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= vectorVariables.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otVectorRead, index, line);
}

void GBStackBrainSpec::AddVectorVariableWrite(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= vectorVariables.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otVectorWrite, index, line);
}

void GBStackBrainSpec::AddLabelRead(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= labels.size()  || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otLabelRead, index, line);
}

void GBStackBrainSpec::AddLabelCall(GBSymbolIndex index, GBLineNumber line) {
	if ( index >= labels.size() || index < 0)
		throw GBBadSymbolIndexError();
	AddInstruction(otLabelCall, index, line);
}

void GBStackBrainSpec::AddImmediate(const string & name, GBStackDatum value, GBLineNumber line) {
// avoid proliferation of immediates
// (but should we require name equivalence?)
	for ( int i = 0; i < constants.size(); i ++ )
		if ( constants[i].value == value && constants[i].NameEquivalent(name) ) {
			AddConstantRead(i, line);
			return;
		}
	AddConstant(name, value);
	AddConstantRead(constants.size() - 1, line);
}

void GBStackBrainSpec::AddConstant(const string & name, GBStackDatum value) {
	constants.push_back(GBConstant(name, value));
}

void GBStackBrainSpec::AddVariable(const string & name, GBStackDatum value) {
	variables.push_back(GBConstant(name, value));
}

void GBStackBrainSpec::AddVectorVariable(const string & name, GBVector value) {
	vectorVariables.push_back(GBVectorSymbol(name, value));
}

void GBStackBrainSpec::AddLabel(const string & name) {
	for ( int i = 0; i < labels.size(); i ++ )
		if ( labels[i].NameEquivalent(name) ) {
			if ( labels[i].address < 0 ) {
				labels[i].address = code.size();
				return;
			} else
				throw GBDuplicateSymbolError(labels[i].name);
		}
	labels.push_back(GBLabel(name, code.size()));
}

void GBStackBrainSpec::AddForwardLabel(const string & name) {
	labels.push_back(GBLabel(name, -1));
}

GBSymbolIndex GBStackBrainSpec::LookupPrimitive(const string & name) const {
	for ( int i = 0; i < kNumPrimitives; i ++ )
		if ( NamesEquivalent(primitiveNames[i], name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupCWord(const string & name) const {
	for ( int i = 0; i < kNumCWords; i ++ )
		if ( NamesEquivalent(cWordNames[i], name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupHardwareVariable(const string & name) const {
	for ( int i = 0; i < kNumHardwareVariables; i ++ )
		if ( NamesEquivalent(hardwareVariableNames[i], name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupHardwareVector(const string & name) const {
	for ( int i = 0; i < kNumHardwareVectors; i ++ )
		if ( NamesEquivalent(hardwareVectorNames[i], name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupConstant(const string & name) const {
	for ( int i = 0; i < constants.size(); i ++ )
		if ( constants[i].NameEquivalent(name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupVariable(const string & name) const {
	for ( int i = 0; i < variables.size(); i ++ )
		if ( variables[i].NameEquivalent(name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupVectorVariable(const string & name) const {
	for ( int i = 0; i < vectorVariables.size(); i ++ )
		if ( vectorVariables[i].NameEquivalent(name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LookupLabel(const string & name) const {
	for ( int i = 0; i < labels.size(); i ++ )
		if ( ! labels[i].gensym && labels[i].NameEquivalent(name) )
			return i;
	return -1;
}

GBSymbolIndex GBStackBrainSpec::LabelReferenced(const string & name) {
	for ( int i = 0; i < labels.size(); i ++ )
		if ( labels[i].NameEquivalent(name) )
			return i;
	AddForwardLabel(name);
	return labels.size() - 1;
}

GBSymbolIndex GBStackBrainSpec::AddGensym(const string & name) {
	labels.push_back(GBLabel(name + ToString(++gensymCounter), -1, true));
	return labels.size() - 1;
}

void GBStackBrainSpec::ResolveGensym(GBSymbolIndex index) {
	if ( index < 0 || index >= labels.size() )
		throw GBBadSymbolIndexError();
	labels[index].address = code.size();
}

void GBStackBrainSpec::SetStartingLabel(GBSymbolIndex index) {
	if ( index < 0 || index >= labels.size() )
		throw GBBadSymbolIndexError();
	startingLabel = index;
}

void GBStackBrainSpec::ParseToken(const string & token, GBLineNumber lineNum) {
	GBSymbolIndex index;
	string stem;
	// Always try user-defined symbols first, so users can shadow anything safely.
// look for adornments
	if ( token.length() >= 2 )
		switch ( token[token.length() - 1] ) {
			case '!':
				stem = token.substr(0, token.length() - 1);
				index = LookupVariable(stem);
				if ( index != -1 ) {
					AddVariableWrite(index, lineNum);
					return;
				}
				index = LookupVectorVariable(stem);
				if ( index != -1 ) {
					AddVectorVariableWrite(index, lineNum);
					return;
				}
				index = LookupHardwareVariable(stem);
				if ( index != -1 ) {
					AddHardwareWrite(index, lineNum);
					return;
				}
				index = LookupHardwareVector(stem);
				if ( index != -1 ) {
					AddHardwareVectorWrite(index, lineNum);
					return;
				}
				throw GBUnknownSymbolError(stem);
			case ':':
				AddLabel(token.substr(0, token.length() - 1));
				return;
			case '&':
				stem = token.substr(0, token.length() - 1);
				index = LabelReferenced(stem);
				if ( index != -1 ) {
					AddLabelRead(index, lineNum);
					return;
				}
				throw GBUnknownSymbolError(stem); // shouldn't happen
			case '^':
				stem = token.substr(0, token.length() - 1);
				index = LabelReferenced(stem);
				if ( index != -1 ) {
					AddLabelCall(index, lineNum);
					return;
				}
				throw GBUnknownSymbolError(stem); // shouldn't happen
			default: // unadorned
				break;
		}
// unadorned - look up in various tables
	index = LookupConstant(token);
	if ( index != -1 ) {
		AddConstantRead(index, lineNum);
		return;
	}
	index = LookupVariable(token);
	if ( index != -1 ) {
		AddVariableRead(index, lineNum);
		return;
	}
	index = LookupVectorVariable(token);
	if ( index != -1 ) {
		AddVectorVariableRead(index, lineNum);
		return;
	}
	index = LookupLabel(token);
	if ( index != -1 ) {
		AddLabelCall(index, lineNum);
		return;
	}
	index = LookupCWord(token);
	if ( index != -1 ) {
		ExecuteCWord(index, lineNum);
		return;
	}
	index = LookupHardwareVariable(token);
	if ( index != -1 ) {
		AddHardwareRead(index, lineNum);
		return;
	}
	index = LookupHardwareVector(token);
	if ( index != -1 ) {
		AddHardwareVectorRead(index, lineNum);
		return;
	}
	index = LookupPrimitive(token);
	if ( index != -1 ) {
		AddPrimitiveCall(index, lineNum);
		return;
	}
// try as a number
	GBNumber num;
	if ( ParseNumber(token, num) ) {
		AddImmediate(token, num, lineNum);
		return;
	}
// nothing's working
	throw GBUnknownSymbolError(token);
}

void GBStackBrainSpec::ParseLine(const string & line, GBLineNumber lineNum) {
	string token;
	int cur = 0;
	while ( ExtractToken(token, line, cur) )
		ParseToken(token, lineNum);
}

void GBStackBrainSpec::Check() {
// check compile-time stack
	if ( ! cStack.empty() )
		throw GBCStackError();
// check forwards
	for ( int i = 0; i < labels.size(); i ++ )
		if ( labels[i].address < 0 )
			throw GBUnresolvedSymbolError(labels[i].name);
}

GBStackAddress GBStackBrainSpec::NumInstructions() const {
	return code.size();}

GBSymbolIndex GBStackBrainSpec::NumConstants() const {
	return constants.size();}

GBSymbolIndex GBStackBrainSpec::NumVariables() const {
	return variables.size();}

GBSymbolIndex GBStackBrainSpec::NumVectorVariables() const {
	return vectorVariables.size();}

GBSymbolIndex GBStackBrainSpec::NumLabels() const {
	return labels.size();}

GBStackInstruction GBStackBrainSpec::ReadInstruction(const GBStackAddress index) const {
	if ( index < 0 )
		throw GBBadAddressError();
	if ( index >= code.size() )
		throw GBOffEndError();
	return code[index];
}

GBStackDatum GBStackBrainSpec::ReadConstant(const GBSymbolIndex index) const {
	if ( index < 0 || index >= constants.size() )
		throw GBBadSymbolIndexError();
	return constants[index].value;
}

GBStackDatum GBStackBrainSpec::ReadVariable(const GBSymbolIndex index) const {
	if ( index < 0 || index >= variables.size() )
		throw GBBadSymbolIndexError();
	return variables[index].value;
}

GBVector GBStackBrainSpec::ReadVectorVariable(const GBSymbolIndex index) const {
	if ( index < 0 || index >= vectorVariables.size() )
		throw GBBadSymbolIndexError();
	return vectorVariables[index].value;
}

GBStackAddress GBStackBrainSpec::ReadLabel(const GBSymbolIndex index) const {
	if ( index < 0 || index >= labels.size() )
		throw GBBadSymbolIndexError();
	return labels[index].address;
}

GBStackAddress GBStackBrainSpec::StartAddress() const {
	if ( startingLabel < 0 )
		return 0; // default to first instruction
	return ReadLabel(startingLabel);
}

GBLineNumber GBStackBrainSpec::LineNumber(const GBStackAddress index) const {
	if ( index < 0 || index >= code.size() )
		return 0;
	return lineNumbers[index];
}

string GBStackBrainSpec::AddressName(const GBStackAddress addr) const {
	for ( GBSymbolIndex i = 0; i < labels.size(); i ++ )
		if ( labels[i].address == addr )
			return labels[i].name;
	return ToString(addr);
}

string GBStackBrainSpec::AddressLastLabel(const GBStackAddress addr) const {
	const GBLabel * closest = nil;
	for ( GBSymbolIndex i = 0; i < labels.size(); i ++ )
		if ( !labels[i].gensym && labels[i].address < addr && (!closest || labels[i].address > closest->address) )
			closest = &labels[i];
	if ( closest ) return closest->name;
	else return ToString(addr);
}

string GBStackBrainSpec::DisassembleAddress(const GBStackAddress addr) const {
	if ( addr < 0 || addr >= code.size() )
		return "";
	return DisassembleInstruction(code[addr]);
}

string GBStackBrainSpec::DisassembleInstruction(const GBStackInstruction instruction) const {
	GBStackInstruction index = instruction & kOpcodeIndexMask;
	 switch ( instruction >> kOpcodeTypeShift ) {
	 	case otPrimitive:
			if ( index >= kNumPrimitives ) return "bad-primitive";
			return primitiveNames[index];
		case otConstantRead:
			if ( index >= constants.size() ) return "bad-constant-read";
			return constants[index].name;
		case otVariableRead:
			if ( index >= variables.size() ) return "bad-variable-read";
			return variables[index].name;
		case otVariableWrite:
			if ( index >= variables.size() ) return "bad-variable-write";
			return variables[index].name + '!';
		case otVectorRead:
			if ( index >= vectorVariables.size() ) return "bad-vector-variable-read";
			return vectorVariables[index].name;
		case otVectorWrite:
			if ( index >= vectorVariables.size() ) return "bad-vector-variable-write";
			return vectorVariables[index].name + '!';
		case otLabelRead:
			if ( index >= labels.size() ) return "bad-label-read";
			return labels[index].name + '&';
		case otLabelCall:
			if ( index >= labels.size() ) return "bad-label-call";
			return labels[index].name + '^';
		case otHardwareRead:
			if ( index >= kNumHardwareVariables ) return "bad-hardware-variable-read";
			return hardwareVariableNames[index];
		case otHardwareWrite:
			if ( index >= kNumHardwareVariables ) return "bad-hardware-variable-write";
			return hardwareVariableNames[index] + '!';
		case otHardwareVectorRead:
			if ( index >= kNumHardwareVectors ) return "bad-hardware-vector-read";
			return hardwareVectorNames[index];
		case otHardwareVectorWrite:
			if ( index >= kNumHardwareVectors ) return "bad-hardware-vector-write";
			return hardwareVectorNames[index] + '!';
		default:
			return "bad-opcode-type";
	 }
}

const string & GBStackBrainSpec::VariableName(GBSymbolIndex index) const {
	if ( index >= variables.size() ) throw GBBadSymbolIndexError();
	return variables[index].name;
}

const string & GBStackBrainSpec::VectorVariableName(GBSymbolIndex index) const {
	if ( index >= vectorVariables.size() ) throw GBBadSymbolIndexError();
	return vectorVariables[index].name;
}

// errors //

string GBStackOverflowError::ToString() const {
	return "stack overflow";
}

string GBStackUnderflowError::ToString() const {
	return "stack underflow";
}

string GBCStackError::ToString() const {
	return "item left on compile-time stack (unmatched compile-time word)";
}

string GBBadAddressError::ToString() const {
	return "invalid address";
}


