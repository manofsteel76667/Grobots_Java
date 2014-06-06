// GBStackBrain.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBStackBrain.h"
#include "GBRobot.h"
#include "GBStackBrainOpcodes.h"
#include "GBErrors.h"
#include "GBSide.h"
#include "GBRobotType.h"
#include "GBWorld.h"


const GBNumber kProcessorUseCost = 0.0005;


GBStackDatum GBStackBrain::ReadVariable(const GBSymbolIndex index) const {
	if ( index < 0 || index >= spec->NumVariables() )
		throw GBBadSymbolIndexError();
	return variables[index];
}

GBVector GBStackBrain::ReadVectorVariable(const GBSymbolIndex index) const {
	if ( index < 0 || index >= spec->NumVectorVariables() )
		throw GBBadSymbolIndexError();
	return vectorVariables[index];
}

void GBStackBrain::WriteVariable(const GBSymbolIndex index, const GBStackDatum value) {
	if ( index < 0 || index >= spec->NumVariables() )
		throw GBBadSymbolIndexError();
	variables[index] = value;
}

void GBStackBrain::WriteVectorVariable(const GBSymbolIndex index, const GBVector value) {
	if ( index < 0 || index >= spec->NumVectorVariables() )
		throw GBBadSymbolIndexError();
	vectorVariables[index] = value;
}

void GBStackBrain::ExecuteInstruction(GBStackInstruction ins, GBRobot * robot, GBWorld * world) {
	GBStackInstruction index = ins & kOpcodeIndexMask;
	switch ( ins >> kOpcodeTypeShift ) {
		case otPrimitive:
			ExecutePrimitive(index, robot, world);
			break;
		case otConstantRead:
			Push(spec->ReadConstant(index));
			break;
		case otVariableRead:
			Push(ReadVariable(index));
			break;
		case otVariableWrite:
			WriteVariable(index, Pop());
			break;
		case otVectorRead:
			PushVector(ReadVectorVariable(index));
			break;
		case otVectorWrite:
			WriteVectorVariable(index, PopVector());
			break;
		case otLabelRead:
			Push(spec->ReadLabel(index));
			break;
		case otLabelCall:
			ExecuteCall(spec->ReadLabel(index));
			break;
		case otHardwareRead:
			Push(ReadHardware(index, robot, world));
			break;
		case otHardwareWrite:
			WriteHardware(index, Pop(), robot, world);
			break;
		case otHardwareVectorRead:
			PushVector(ReadHardwareVector(index, robot, world));
			break;
		case otHardwareVectorWrite:
			WriteHardwareVector(index, PopVector(), robot, world);
			break;
		default:	
			throw GBUnknownInstructionError();
			break;
	}
}

void GBStackBrain::ExecuteCall(GBStackAddress addr) {
	if ( returnStackHeight >= kReturnStackLimit )
		throw GBStackOverflowError();
	returnStack[returnStackHeight++] = pc;
	pc = addr;
}

void GBStackBrain::DoPrint(const string & str) {
	delete lastPrint;
	lastPrint = new string(str);
}

void GBStackBrain::NumberToNumberOp(GBNumber (*op)(const GBNumber &)) {
	if ( stackHeight < 1 )
		throw GBStackUnderflowError();
	stack[stackHeight - 1] = op(stack[stackHeight - 1]);
}

void GBStackBrain::TwoNumberToNumberOp(GBNumber (*op)(const GBNumber &, const GBNumber &)) {
	if ( stackHeight < 2 )
		throw GBStackUnderflowError();
	-- stackHeight;
	GBNumber temp = op(stack[stackHeight - 1], stack[stackHeight]);
	stack[stackHeight - 1] = temp;
}

void GBStackBrain::VectorToVectorOp(GBFinePoint (GBFinePoint::* op)() const) {
	GBVector v = PopVector();
	PushVector((v.*op)());
}

void GBStackBrain::VectorToScalarOp(GBNumber (GBFinePoint::* op)() const) {
	GBVector v = PopVector();
	Push((v.*op)());
}

void GBStackBrain::TwoVectorToVectorOp(GBFinePoint (GBFinePoint::* op)(const GBFinePoint &) const) {
	GBVector v2 = PopVector();
	GBVector v1 = PopVector();
	PushVector((v1.*op)(v2));
}

void GBStackBrain::TwoVectorToScalarOp(GBNumber (GBFinePoint::* op)(const GBFinePoint &) const) {
	GBVector v2 = PopVector();
	GBVector v1 = PopVector();
	Push((v1.*op)(v2));
}

void GBStackBrain::Push(GBStackDatum value) {
	if ( stackHeight >= kStackLimit )
		throw GBStackOverflowError();
	stack[stackHeight++] = value;
}

GBStackDatum GBStackBrain::Pop() {
	if ( stackHeight <= 0 )
		throw GBStackUnderflowError();
	return stack[--stackHeight];
}

GBStackDatum GBStackBrain::Peek(long delta) {
	if ( delta < 1 )
		throw GBBadArgumentError();
	long where = stackHeight - delta;
	if ( where < 0 )
		throw GBStackUnderflowError();
	if ( where >= kStackLimit )
		throw GBStackOverflowError();
	return stack[where];
}

void GBStackBrain::PushVector(const GBVector & v) {
	if ( stackHeight > kStackLimit - 2 )
		throw GBStackOverflowError();
	stack[stackHeight++] = v.x;
	stack[stackHeight++] = v.y;
}

GBVector GBStackBrain::PopVector() {
	if ( stackHeight < 2 )
		throw GBStackUnderflowError();
	GBVector v(stack[stackHeight - 2], stack[stackHeight - 1]);
	stackHeight -= 2;
	return v;
}

long GBStackBrain::PopInteger() {
	return ToInteger(Pop());
}

void GBStackBrain::PushBoolean(const bool value) {
	Push(FromBoolean(value));
}

void GBStackBrain::PushReturn(GBStackAddress value) {
	if ( returnStackHeight >= kReturnStackLimit )
		throw GBStackOverflowError();
	returnStack[returnStackHeight++] = value;
}

GBStackAddress GBStackBrain::PopReturn() {
	if ( returnStackHeight <= 0 )
		throw GBStackUnderflowError();
	return returnStack[--returnStackHeight];
}

GBStackDatum GBStackBrain::ReadLocalMemory(GBStackAddress addr, GBRobot * robot) {
	if ( addr < 1 || addr > robot->hardware.Memory() ) throw GBIndexOutOfRangeError();
	if ( ! memory ) return 0;
	return memory[addr - 1];
}

void GBStackBrain::WriteLocalMemory(GBStackAddress addr, GBStackDatum val, GBRobot * robot) {
	if ( addr < 1 || addr > robot->hardware.Memory() ) throw GBIndexOutOfRangeError();
	if ( ! memory ) {
		if ( ! robot->hardware.Memory() ) return; // fail silently
		memory = new GBStackDatum[robot->hardware.Memory()];
		if ( ! memory ) throw GBOutOfMemoryError();
	}
	memory[addr - 1] = val;
}

GBStackAddress GBStackBrain::ToAddress(const GBStackDatum value) {
	if ( IsInteger(value) ) {
		GBStackAddress addr = floor(value);
		if ( addr >= 0 && addr <= spec->NumInstructions() )
			// note: addr immediately after the last instruction is allowed
			return addr;
	}
	throw GBBadAddressError();
	return -1;
}

long GBStackBrain::ToInteger(const GBStackDatum value) {
	if ( IsInteger(value) )
		return floor(value);
	throw GBNotIntegerError();
	return 0;
}

GBStackDatum GBStackBrain::FromBoolean(const bool value) {
	return value?1:0;
}

void GBStackBrain::BrainError(GBError & err, GBRobot * robot, GBWorld * world) {
	if ( world->reportErrors )
		NonfatalError(robot->Description() + " had error in brain, probably at line "
			+ ToString(spec->LineNumber(pc - 1)) + ": " + err.ToString());
	SetStatus(bsError);
}

GBStackBrain::GBStackBrain(const GBStackBrainSpec * spc)
	: spec(spc),
	pc(spc->StartAddress()),
	variables(new GBStackDatum[spc->NumVariables()]),
	vectorVariables(new GBVector[spc->NumVectorVariables()]),
	stack(new GBStackDatum[kStackLimit]), stackHeight(0),
	returnStack(new GBStackAddress[kReturnStackLimit]), returnStackHeight(0),
	memory(nil),
	used(0), remaining(0),
	lastPrint(nil)
{
	if ( ! variables || ! vectorVariables || ! stack || ! returnStack )
		throw GBOutOfMemoryError();
	GBSymbolIndex i;
	for ( i = 0; i < spc->NumVariables(); i ++ )
		variables[i] = spc->ReadVariable(i);
	for ( i = 0; i < spc->NumVectorVariables(); i ++ )
		vectorVariables[i] = spc->ReadVectorVariable(i);
}

GBStackBrain::~GBStackBrain() {
	delete [] variables;
	delete [] vectorVariables;
	delete [] stack;
	delete [] returnStack;
	delete [] memory;
	delete lastPrint;
}

void GBStackBrain::Think(GBRobot * robot, GBWorld * world) {
	try {
		while ( Status() == bsOK && remaining > 0 )
			ThinkOne(robot, world);
	} catch ( GBError & err ) {
		BrainError(err, robot, world);
	} catch ( GBAbort & ) {
		SetStatus(bsStopped);
	}
	GBEnergy en = kProcessorUseCost * used;
	robot->hardware.UseEnergy(en);
	robot->Owner()->Scores().Expenditure().ReportBrain(en);
	used = 0;
	remaining = (remaining > 0 ? 0 : remaining) + robot->hardware.Processor();
}

void GBStackBrain::ThinkOne(GBRobot * robot, GBWorld * world) {
	GBStackInstruction ins = spec->ReadInstruction(pc++);
	remaining --; // currently all instructions take one cycle
	used ++;
	ExecuteInstruction(ins, robot, world);
}

void GBStackBrain::Step(GBRobot * robot, GBWorld * world) {
	try {
		ThinkOne(robot, world);
	} catch ( GBError & err ) {
		BrainError(err, robot, world);
	} catch ( GBAbort & ) {
		SetStatus(bsStopped);
	}
}

bool GBStackBrain::Ready() const {
	return remaining > 0;
}

GBInstructionCount GBStackBrain::Remaining() const {return remaining;}

GBStackAddress GBStackBrain::PC() const {return pc;}

GBLineNumber GBStackBrain::PCLine() const {
	return spec->LineNumber(pc);}

long GBStackBrain::StackHeight() const {return stackHeight;}

long GBStackBrain::ReturnStackHeight() const {return returnStackHeight;}

GBStackDatum GBStackBrain::StackAt(long index) const {
	if ( index < 0 || index >= stackHeight ) throw GBIndexOutOfRangeError();
	return stack[index];
}

GBStackAddress GBStackBrain::ReturnStackAt(long index) const {
	if ( index < 0 || index >= returnStackHeight ) throw GBIndexOutOfRangeError();
	return returnStack[index];
}

bool GBStackBrain::ValidAddress(const GBStackAddress addr) const {
	return addr >= 0 && addr < spec->NumInstructions();}

string GBStackBrain::AddressName(const GBStackAddress addr) const {
	return spec->AddressName(addr);}

string GBStackBrain::AddressLastLabel(const GBStackAddress addr) const {
	return spec->AddressLastLabel(addr);}

string GBStackBrain::DisassembleAddress(const GBStackAddress addr) const {
	return spec->DisassembleAddress(addr);}

string GBStackBrain::LastPrint() const {
	return lastPrint ? *lastPrint : string("none");}

GBSymbolIndex GBStackBrain::NumVariables() const {
	return spec->NumVariables();}

GBSymbolIndex GBStackBrain::NumVectorVariables() const {
	return spec->NumVectorVariables();}

const string & GBStackBrain::VariableName(GBSymbolIndex index) const {
	return spec->VariableName(index);}

const string & GBStackBrain::VectorVariableName(GBSymbolIndex index) const {
	return spec->VectorVariableName(index);}

// errors //

string GBOffEndError::ToString() const {
	return "fell off end of code";
}

