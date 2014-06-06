// GBStackBrainOpcodes.h
// opcodes and hardware variable numbers for stack brains.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBStackBrainOpcodes_h
#define GBStackBrainOpcodes_h

#include <string>
using std::string;

// layout: top 4 bits are opcode type, bottom 12 are index within type

const unsigned short kOpcodeTypeShift = 12;
const unsigned short kOpcodeIndexMask = 0x0FFF;

enum { // opcode types
	otPrimitive = 0,
	otConstantRead,
	otVariableRead, otVariableWrite,
	otVectorRead, otVectorWrite,
	otLabelRead, otLabelCall,
	otHardwareRead, otHardwareWrite, // hw orders, status, and spec-constants are not distinguished here but could be.
	otHardwareVectorRead, otHardwareVectorWrite,
	kNumOpcodeTypes
};


enum { // primitives
	opNop = 0,
// stack manipulation
	opDrop, op2Drop, opNip, opRDrop, opDropN,
	opSwap, opSubswap, op2Swap, opRotate, opReverseRotate,
	opDup, op2Dup, opTuck, opOver, op2Over,
	opStackHeight, opStackLimit, opRoll, opPick,
	opToReturn, opFromReturn, // remove?
// branches of various sorts
	opJump, opCall, opReturn,
	opIfGo, opIfElseGo,
	opIfCall, opIfElseCall,
	opIfReturn,
	opNotIfGo, opNotIfReturn, opNotIfCall,
// arithmetic and friends
	opAdd, opSubtract, opNegate,
	opMultiply, opDivide, opReciprocal, opMod, opRem,
	opSquare, opSqrt,
	opExponent,
	opIsInteger,
	opFloor, opCeiling, opRound,
	opMin, opMax,
	opAbs, opSignum,
	opReorient,
	opSine, opCosine, opTangent,
	opArcSine, opArcCosine, opArcTangent,
	opRandom, opRandomAngle, opRandomInt, opRandomBoolean,
// constants
	opPi, op2Pi, opPiOver2, opE, // should be builtin constants - also 0 and 1
	opEpsilon, opInfinity,
// vector operations
	opRectToPolar, opPolarToRect, 
	opVectorAdd, opVectorSubtract, opVectorNegate,
	opVectorScalarMultiply, opVectorScalarDivide,
	opVectorNorm, opVectorAngle,
	opDotProduct, opProject, opCross, opUnitize,
	opDistance, opInRange,
	opVectorEqual, opVectorNotEqual,
// comparisons and Boolean operations
	opEqual, opNotEqual,
	opLessThan, opGreaterThan, opLessThanOrEqual, opGreaterThanOrEqual,
	opNot, opAnd, opOr, opXor, opNand, opNor,
	opValueConditional,
// misc external
	opPrint, opPrintVector, opBeep, opPause, opStop,
	opSync,
// basic hardware
	opSeekLocation, opSeekMovingLocation, opRestrictPosition,
	opDie,
	opWriteLocalMemory, opReadLocalMemory, opWriteLocalVector, opReadLocalVector,
	opWriteSharedMemory, opReadSharedMemory, opWriteSharedVector, opReadSharedVector,
	opMessagesWaiting, opSendMessage, opReceiveMessage, opClearMessages, opSkipMessages,
	opTypePopulation,
	opAutoConstruct, opBalanceTypes,
// sensors
	opFireRobotSensor, opFireFoodSensor, opFireShotSensor,
	opRobotSensorNext, opFoodSensorNext, opShotSensorNext,
	opPeriodicRobotSensor, opPeriodicFoodSensor, opPeriodicShotSensor,
// weapons
	opFireBlaster, opFireGrenade,
	opLeadBlaster, opLeadGrenade,
	opSetForceField,
// limit
	kNumPrimitives
};

// Many of these are readonly.
// one-shot order variables like blaster-direction are now inaccessible.
enum { // hardware (or other magic) variables
	hvTime = 0, hvTimeLimit,
	hvWorldWidth, hvWorldHeight,
	hvRadius, hvMass, hvSpeed,
	hvProcessor, hvRemaining,
	hvSideID, hvTypeID, hvRobotID, hvParentID,
	hvPopulation,
	hvEnginePower, hvEngineMaxPower,
	hvFlag,
	hvCollision, hvFriendlyCollision, hvEnemyCollision, hvFoodCollision, hvShotCollision, hvWallCollision,
// energy
	hvEnergy, hvMaxEnergy,
	hvSolarCells,
	hvEater, hvEaten,
	hvSyphonMaxRate, hvSyphonRange, hvSyphonDistance, hvSyphonDirection, hvSyphonRate, hvSyphoned,
	hvEnemySyphonMaxRate, hvEnemySyphonRange, hvEnemySyphonDistance,
		hvEnemySyphonDirection, hvEnemySyphonRate, hvEnemySyphoned,
// constructor
	hvConstructorMaxRate, hvConstructorRate, hvConstructorType,
	hvConstructorProgress, hvConstructorRemaining,
	hvChildID,
// robot sensor
	hvRobotSensorRange, hvRobotSensorFiringCost,
	hvRobotSensorFocusDistance, hvRobotSensorFocusDirection,
	hvRobotSensorSeesFriends, hvRobotSensorSeesEnemies,
	hvRobotSensorTime, hvRobotSensorFound, hvRobotSensorRangeFound, hvRobotSensorAngleFound,
	hvRobotSensorSideFound, hvRobotSensorRadiusFound, hvRobotSensorMassFound, hvRobotSensorEnergyFound,
	hvRobotSensorTypeFound, hvRobotSensorIDFound, hvRobotSensorShieldFractionFound,
	hvRobotSensorBombFound, hvRobotSensorReloadingFound, hvRobotSensorFlagFound,
	hvRobotSensorRangeOverall, hvRobotSensorAngleOverall,
	hvRobotSensorCurrentResult, hvRobotSensorNumResults, hvRobotSensorMaxResults,
// food sensor
	hvFoodSensorRange, hvFoodSensorFiringCost,
	hvFoodSensorFocusDistance, hvFoodSensorFocusDirection,
	hvFoodSensorTime, hvFoodSensorFound, hvFoodSensorRangeFound, hvFoodSensorAngleFound,
	hvFoodSensorSideFound, hvFoodSensorRadiusFound, hvFoodSensorMassFound, hvFoodSensorEnergyFound,
	hvFoodSensorRangeOverall, hvFoodSensorAngleOverall,
	hvFoodSensorCurrentResult, hvFoodSensorNumResults, hvFoodSensorMaxResults,
// shot sensor
	hvShotSensorRange, hvShotSensorFiringCost,
	hvShotSensorFocusDistance, hvShotSensorFocusDirection,
	hvShotSensorSeesFriendly, hvShotSensorSeesEnemy,
	hvShotSensorTime, hvShotSensorFound, hvShotSensorRangeFound, hvShotSensorAngleFound,
	hvShotSensorSideFound, hvShotSensorRadiusFound, hvShotSensorPowerFound, hvShotSensorTypeFound,
	hvShotSensorRangeOverall, hvShotSensorAngleOverall,
	hvShotSensorCurrentResult, hvShotSensorNumResults, hvShotSensorMaxResults,
// defenses
	hvArmor, hvMaxArmor,
	hvRepairRate, hvMaxRepairRate,
	hvShield, hvMaxShield, hvShieldFraction,
	hvLastHit,
// weapons
	hvBlasterDamage, hvBlasterRange, hvBlasterSpeed, hvBlasterLifetime,
		hvBlasterReloadTime, hvBlasterFiringCost, hvBlasterCooldown,
	hvGrenadesDamage, hvGrenadesRange, hvGrenadesSpeed, hvGrenadesLifetime,
		hvGrenadesReloadTime, hvGrenadesFiringCost, hvGrenadesCooldown, hvGrenadesRadius,
	hvForceFieldMaxPower, hvForceFieldRange, hvForceFieldDistance, hvForceFieldDirection,
		hvForceFieldPower, hvForceFieldAngle, hvForceFieldRadius,
// limit
	kNumHardwareVariables
};

enum { // hardware vector variables
	hvvWorldSize = 0,
//
	hvvPosition, hvvVelocity,
// 
	hvvEngineVelocity,
// sensors
	hvvRobotSensorWhereFound, hvvRobotSensorVelocityFound, hvvRobotSensorWhereOverall,
	hvvFoodSensorWhereFound, hvvFoodSensorVelocityFound, hvvFoodSensorWhereOverall,
	hvvShotSensorWhereFound, hvvShotSensorVelocityFound, hvvShotSensorWhereOverall,
//
	kNumHardwareVectors
};

enum { // compile-time words
	cwNop = 0,
	cwIf, cwNotIf, cwElse, cwThen, cwAndIf, cwNotAndIf, cwCElse,
	cwDo, cwLoop, cwForever, cwWhile, cwUntil, cwWhileLoop, cwUntilLoop,
	kNumCWords
};

// names, defined in StackOpcodes.cpp
extern const string primitiveNames[kNumPrimitives];
extern const string hardwareVariableNames[kNumHardwareVariables];
extern const string hardwareVectorNames[kNumHardwareVectors];
extern const string cWordNames[kNumCWords];

#endif
