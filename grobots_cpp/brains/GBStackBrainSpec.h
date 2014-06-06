// GBStackBrainSpec.h
// mostly compiler and object code for stack brains.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBStackBrainSpec_h
#define GBStackBrainSpec_h

#include "GBNumber.h"
#include "GBBrainSpec.h"
#include "GBStringUtilities.h"
#include <vector>

typedef GBNumber GBStackDatum;
typedef unsigned short GBStackInstruction;
typedef long GBStackAddress;
typedef long GBSymbolIndex;
using std::vector;


class GBSymbol {
public:
	string name;
	GBSymbol();
	GBSymbol(const string & n);
	~GBSymbol();
	GBSymbol & operator=(const GBSymbol & arg);
	bool NameEquivalent(const string & other) const;
};

class GBConstant : public GBSymbol {
public:
	GBConstant();
	GBConstant(const string & n, GBStackDatum v);
	~GBConstant();
	GBConstant & operator=(const GBConstant & arg);
	GBStackDatum value;
};

class GBVectorSymbol : public GBSymbol {
public:
	GBVectorSymbol();
	GBVectorSymbol(const string & n, const GBVector & v);
	~GBVectorSymbol();
	GBVectorSymbol & operator=(const GBVectorSymbol & arg);
	GBVector value;
};

class GBLabel : public GBSymbol {
public:
	GBLabel();
	GBLabel(const string & n, GBStackAddress v, bool g = false);
	~GBLabel();
	GBLabel & operator=(const GBLabel & arg);
	bool NameEquivalent(const string & other) const;
	GBStackAddress address;
	bool gensym;
};

class GBStackBrainSpec : public GBBrainSpec {
	vector<GBStackInstruction> code;
	vector<GBLineNumber> lineNumbers;
	vector<GBConstant> constants;
	vector<GBConstant> variables;
	vector<GBVectorSymbol> vectorVariables;
	vector<GBLabel> labels;
	GBSymbolIndex startingLabel;
// compile-time stack
	vector<GBSymbolIndex> cStack;
	long gensymCounter;
public:
	GBStackBrainSpec();
	GBStackBrainSpec(const GBStackBrainSpec & original);
	~GBStackBrainSpec();
	GBBrainSpec * Copy() const;
// using
	GBBrain * MakeBrain() const;
// computed
	GBEnergy Cost() const;
	GBMass Mass() const;
private:
	void AddInstruction(GBStackInstruction ins, GBLineNumber line);
	void AddInstruction(GBStackInstruction type, GBStackInstruction index, GBLineNumber line);
// compile-time stack
	void CPush(GBSymbolIndex ind);
	GBSymbolIndex CPeek();
	GBSymbolIndex CPop();
	void ExecuteCWord(long index, GBLineNumber line);
public: // loader access
	// should all these be public?
	void AddPrimitiveCall(GBSymbolIndex index, GBLineNumber line);
	void AddHardwareRead(GBSymbolIndex index, GBLineNumber line);
	void AddHardwareWrite(GBSymbolIndex index, GBLineNumber line);
	void AddHardwareVectorRead(GBSymbolIndex index, GBLineNumber line);
	void AddHardwareVectorWrite(GBSymbolIndex index, GBLineNumber line);
	void AddConstantRead(GBSymbolIndex index, GBLineNumber line);
	void AddVariableRead(GBSymbolIndex index, GBLineNumber line);
	void AddVariableWrite(GBSymbolIndex index, GBLineNumber line);
	void AddVectorVariableRead(GBSymbolIndex index, GBLineNumber line);
	void AddVectorVariableWrite(GBSymbolIndex index, GBLineNumber line);
	void AddLabelRead(GBSymbolIndex index, GBLineNumber line);
	void AddLabelCall(GBSymbolIndex index, GBLineNumber line);
	void AddImmediate(const string & name, GBStackDatum value, GBLineNumber line);
		// add to constant table, and add a read instruction
	void AddConstant(const string & name, GBStackDatum value);
	void AddVariable(const string & name, GBStackDatum value);
	void AddVectorVariable(const string & name, GBVector value);
	void AddLabel(const string & name);
	void AddForwardLabel(const string & name);
	GBSymbolIndex LookupPrimitive(const string & name) const;
	GBSymbolIndex LookupCWord(const string & name) const;
	GBSymbolIndex LookupHardwareVariable(const string & name) const;
	GBSymbolIndex LookupHardwareVector(const string & name) const;
	GBSymbolIndex LookupConstant(const string & name) const;
	GBSymbolIndex LookupVariable(const string & name) const;
	GBSymbolIndex LookupVectorVariable(const string & name) const;
	GBSymbolIndex LookupLabel(const string & name) const;
	GBSymbolIndex LabelReferenced(const string & name);
// gensyms
	GBSymbolIndex AddGensym(const string & name);
	void ResolveGensym(GBSymbolIndex index);
//
	void SetStartingLabel(GBSymbolIndex index);
	void ParseToken(const string & token, GBLineNumber lineNum);
	void ParseLine(const string & line, GBLineNumber lineNum);
	void Check();
// brain access
	GBSymbolIndex NumInstructions() const;
	GBSymbolIndex NumConstants() const;
	GBSymbolIndex NumVariables() const;
	GBSymbolIndex NumVectorVariables() const;
	GBSymbolIndex NumLabels() const;
	GBStackInstruction ReadInstruction(const GBStackAddress index) const;
	GBStackDatum ReadConstant(const GBSymbolIndex index) const;
	GBStackDatum ReadVariable(const GBSymbolIndex index) const;
	GBVector ReadVectorVariable(const GBSymbolIndex index) const;
	GBStackAddress ReadLabel(const GBSymbolIndex index) const;
	GBStackAddress StartAddress() const;
// debugger access
	GBLineNumber LineNumber(const GBStackAddress addr) const;
	string AddressName(const GBStackAddress addr) const;
	string AddressLastLabel(const GBStackAddress addr) const;
	string DisassembleAddress(const GBStackAddress addr) const;
	string DisassembleInstruction(const GBStackInstruction ins) const;
	const string & VariableName(GBSymbolIndex index) const;
	const string & VectorVariableName(GBSymbolIndex index) const;
};

// errors //

class GBStackOverflowError : public GBBrainError {
public:
	string ToString() const;
};

class GBStackUnderflowError : public GBBrainError {
public:
	string ToString() const;
};

class GBCStackError : public GBBrainError {
public:
	string ToString() const;
};

class GBBadAddressError : public GBBrainError {
public:
	string ToString() const;
};


#endif
