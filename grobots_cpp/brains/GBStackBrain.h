// GBStackBrain.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBStackBrain_h
#define GBStackBrain_h

#include "GBStackBrainSpec.h"
#include "GBBrain.h"
#include "GBFinePoint.h"
#include "GBMessages.h"

class GBRobot;
class GBWorld;
class GBSensorState;

const long kStackLimit = 100;
const long kReturnStackLimit = 50;


class GBStackBrain : public GBBrain {
	const GBStackBrainSpec * spec;
// registers and other memory
	GBStackAddress pc;
	GBStackDatum * variables;
	GBVector * vectorVariables;
	GBStackDatum * stack;
	long stackHeight;
	GBStackAddress * returnStack;
	long returnStackHeight;
	GBStackDatum * memory;
// status
	GBInstructionCount remaining;
	GBInstructionCount used;
	string * lastPrint;
// accessing variables
public:
	GBStackDatum ReadVariable(const GBSymbolIndex index) const;
	GBVector ReadVectorVariable(const GBSymbolIndex index) const;
private:
	void WriteVariable(const GBSymbolIndex index, const GBStackDatum value);
	void WriteVectorVariable(const GBSymbolIndex index, const GBVector value);
	GBStackDatum ReadHardware(const long index, GBRobot * robot, GBWorld * world) const;
	void WriteHardware(const long index, const GBStackDatum value, GBRobot * robot, GBWorld * world);
	GBVector ReadHardwareVector(const long index, GBRobot * robot, GBWorld * world) const;
	void WriteHardwareVector(const long index, const GBVector value, GBRobot * robot, GBWorld * world);
// executing instructions
	void ExecuteInstruction(GBStackInstruction ins, GBRobot * robot, GBWorld * world);
	void ExecutePrimitive(GBSymbolIndex ins, GBRobot * robot, GBWorld * world);
	void ExecuteCall(GBStackAddress addr);
	void DoPrint(const string & str);
	void FirePeriodic(GBSensorState & sensor, GBWorld * world);
// operations
	void NumberToNumberOp(GBNumber (*op)(const GBNumber &));
	void TwoNumberToNumberOp(GBNumber (*op)(const GBNumber &, const GBNumber &));
	void VectorToVectorOp(GBFinePoint (GBFinePoint::* op)() const);
	void VectorToScalarOp(GBNumber (GBFinePoint::* op)() const);
	void TwoVectorToVectorOp(GBFinePoint (GBFinePoint::* op)(const GBFinePoint &) const);
	void TwoVectorToScalarOp(GBNumber (GBFinePoint::* op)(const GBFinePoint &) const);
// stack access
	void Push(GBStackDatum value);
	GBStackDatum Pop();
	GBStackDatum Peek(long delta = 1);
	void PushVector(const GBVector & v);
	GBVector PopVector();
	long PopInteger();
	void PushBoolean(const bool value);
	GBStackAddress PopReturn();
	void PushReturn(GBStackAddress value);
	GBStackDatum ReadLocalMemory(GBStackAddress addr, GBRobot * robot);
	void WriteLocalMemory(GBStackAddress addr, GBStackDatum val, GBRobot * robot);
// conversions
	GBStackAddress ToAddress(const GBStackDatum value); // not static because it needs to rangecheck
	static long ToInteger(const GBStackDatum value);
	static GBStackDatum FromBoolean(const bool value);
// error reporting
	void BrainError(GBError & err, GBRobot * robot, GBWorld * world);
public:
	GBStackBrain(const GBStackBrainSpec * spc); // create new brain from stack code
	~GBStackBrain();
	
	void Think(GBRobot * robot, GBWorld * world); // think until out of cycles
	void ThinkOne(GBRobot * robot, GBWorld * world); // think one instruction
	void Step(GBRobot * robot, GBWorld * world); // think one, with error handler
	bool Ready() const;
// accessors for debugger
	GBInstructionCount Remaining() const;
	GBStackAddress PC() const;
	GBLineNumber PCLine() const;
	long StackHeight() const;
	long ReturnStackHeight() const;
	GBStackDatum StackAt(long index) const;
	GBStackAddress ReturnStackAt(long index) const;
	bool ValidAddress(const GBStackAddress addr) const;
	string AddressName(const GBStackAddress addr) const;
	string AddressLastLabel(const GBStackAddress addr) const;
	string DisassembleAddress(const GBStackAddress addr) const;
	string LastPrint() const;
	GBSymbolIndex NumVariables() const;
	GBSymbolIndex NumVectorVariables() const;
	const string & VariableName(GBSymbolIndex index) const;
	const string & VectorVariableName(GBSymbolIndex index) const;
};

// errors //

class GBOffEndError : public GBBadAddressError {
public:
	string ToString() const;
};


#endif
