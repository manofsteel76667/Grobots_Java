// GBStackBrainPrimitives.cpp
// five big methods of GBStackBrain
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBStackBrain.h"
#include "GBRobot.h"
#include "GBStackBrainOpcodes.h"
#include "GBErrors.h"
#include "GBSide.h"
#include "GBRobotType.h"
#include "GBSound.h"
#include "GBWorld.h"
#include <math.h>

static GBPosition LeadShot(const GBPosition & pos, const GBPosition & vel, GBSpeed shotSpeed, GBDistance r);

GBStackDatum GBStackBrain::ReadHardware(const GBSymbolIndex index, GBRobot * robot, GBWorld * world) const {
	switch ( index ) {
	// world
		case hvTime: return world->CurrentFrame();
		case hvTimeLimit: return world->timeLimit;
		case hvWorldWidth: return world->Size().x;
		case hvWorldHeight: return world->Size().y;
	// robot
		case hvRadius: return robot->Radius();
		case hvMass: return robot->Mass();
		case hvSpeed: return robot->Speed();
		case hvProcessor: return robot->hardware.Processor();
		case hvRemaining: return remaining;
		case hvSideID: return robot->Owner()->ID();
		case hvTypeID: return robot->Type()->ID();
		case hvRobotID: return robot->ID();
		case hvParentID: return robot->ParentID();
		case hvPopulation: return robot->Owner()->Scores().Population();
		case hvEnginePower: return robot->hardware.EnginePower();
		case hvEngineMaxPower: return robot->hardware.EngineMaxPower();
		case hvFlag: return robot->flag;
	// collisions
		case hvCollision: return robot->Collisions();
		case hvFriendlyCollision: return robot->FriendlyCollisions();
		case hvEnemyCollision: return robot->EnemyCollisions();
		case hvFoodCollision: return robot->FoodCollisions();
		case hvShotCollision: return robot->ShotCollisions();
		case hvWallCollision: return robot->WallCollisions();
	// energy
		case hvEnergy: return robot->hardware.Energy();
		case hvMaxEnergy: return robot->hardware.MaxEnergy();
		case hvSolarCells: return robot->hardware.SolarCells();
		case hvEater: return robot->hardware.Eater();
		case hvEaten: return robot->hardware.Eaten();
		case hvSyphonMaxRate: return robot->hardware.syphon.MaxRate();
		case hvSyphonRange: return robot->hardware.syphon.MaxRange();
		case hvSyphonDistance: return robot->hardware.syphon.Distance();
		case hvSyphonDirection: return robot->hardware.syphon.Direction();
		case hvSyphonRate: return robot->hardware.syphon.Rate();
		case hvSyphoned: return robot->hardware.syphon.Syphoned();
		case hvEnemySyphonMaxRate: return robot->hardware.enemySyphon.MaxRate();
		case hvEnemySyphonRange: return robot->hardware.enemySyphon.MaxRange();
		case hvEnemySyphonDistance: return robot->hardware.enemySyphon.Distance();
		case hvEnemySyphonDirection: return robot->hardware.enemySyphon.Direction();
		case hvEnemySyphonRate: return robot->hardware.enemySyphon.Rate();
		case hvEnemySyphoned: return robot->hardware.enemySyphon.Syphoned();
	// constructor
		case hvConstructorMaxRate: return robot->hardware.constructor.MaxRate();
		case hvConstructorRate: return robot->hardware.constructor.Rate();
		case hvConstructorProgress: return robot->hardware.constructor.Progress();
		case hvConstructorRemaining: return robot->hardware.constructor.Remaining();
		case hvConstructorType: return robot->Owner()->GetTypeIndex(robot->hardware.constructor.Type());
		case hvChildID: return robot->hardware.constructor.ChildID();
	// robot sensor
		case hvRobotSensorRange: return robot->hardware.sensor1.MaxRange();
		case hvRobotSensorFiringCost: return robot->hardware.sensor1.FiringCost();
		case hvRobotSensorFocusDistance: return robot->hardware.sensor1.Distance();
		case hvRobotSensorFocusDirection: return robot->hardware.sensor1.Direction();
		case hvRobotSensorSeesFriends: return FromBoolean(robot->hardware.sensor1.SeesFriendly());
		case hvRobotSensorSeesEnemies: return FromBoolean(robot->hardware.sensor1.SeesEnemy());
		case hvRobotSensorTime: return robot->hardware.sensor1.Time();
		case hvRobotSensorFound: return robot->hardware.sensor1.Found();
		case hvRobotSensorAngleFound: return (robot->hardware.sensor1.WhereFound() - robot->Position()).Angle(); // inconsistent: distance then or now?
		case hvRobotSensorRangeFound: return (robot->hardware.sensor1.WhereFound() - robot->Position()).Norm();
		case hvRobotSensorSideFound: return robot->hardware.sensor1.Side();
		case hvRobotSensorRadiusFound: return robot->hardware.sensor1.Radius();
		case hvRobotSensorMassFound: return robot->hardware.sensor1.Mass();
		case hvRobotSensorEnergyFound: return robot->hardware.sensor1.Energy();
		case hvRobotSensorTypeFound: return robot->hardware.sensor1.Type();
		case hvRobotSensorIDFound: return robot->hardware.sensor1.ID();
		case hvRobotSensorShieldFractionFound: return robot->hardware.sensor1.ShieldFraction();
		case hvRobotSensorBombFound: return robot->hardware.sensor1.Bomb();
		case hvRobotSensorReloadingFound: return robot->hardware.sensor1.Reloading();
		case hvRobotSensorFlagFound: return robot->hardware.sensor1.Flag();
		case hvRobotSensorRangeOverall: return (robot->hardware.sensor1.WhereOverall() - robot->Position()).Norm();
		case hvRobotSensorAngleOverall: return (robot->hardware.sensor1.WhereOverall() - robot->Position()).Angle();
		case hvRobotSensorCurrentResult: return robot->hardware.sensor1.CurrentResult();
		case hvRobotSensorNumResults: return robot->hardware.sensor1.NumResults();
		case hvRobotSensorMaxResults: return robot->hardware.sensor1.MaxResults();
	// food sensor
		case hvFoodSensorRange: return robot->hardware.sensor2.MaxRange();
		case hvFoodSensorFiringCost: return robot->hardware.sensor2.FiringCost();
		case hvFoodSensorFocusDistance: return robot->hardware.sensor2.Distance();
		case hvFoodSensorFocusDirection: return robot->hardware.sensor2.Direction();
		case hvFoodSensorTime: return robot->hardware.sensor2.Time();
		case hvFoodSensorFound: return robot->hardware.sensor2.Found();
		case hvFoodSensorAngleFound: return (robot->hardware.sensor2.WhereFound() - robot->Position()).Angle();
		case hvFoodSensorRangeFound: return (robot->hardware.sensor2.WhereFound() - robot->Position()).Norm();
		case hvFoodSensorSideFound: return robot->hardware.sensor2.Side();
		case hvFoodSensorRadiusFound: return robot->hardware.sensor2.Radius();
		case hvFoodSensorMassFound: return robot->hardware.sensor2.Mass();
		case hvFoodSensorEnergyFound: return robot->hardware.sensor2.Energy();
		case hvFoodSensorRangeOverall: return (robot->hardware.sensor2.WhereOverall() - robot->Position()).Norm();
		case hvFoodSensorAngleOverall: return (robot->hardware.sensor2.WhereOverall() - robot->Position()).Angle();
		case hvFoodSensorCurrentResult: return robot->hardware.sensor2.CurrentResult();
		case hvFoodSensorNumResults: return robot->hardware.sensor2.NumResults();
		case hvFoodSensorMaxResults: return robot->hardware.sensor2.NumResults();
	// shot sensor
		case hvShotSensorRange: return robot->hardware.sensor3.MaxRange();
		case hvShotSensorFiringCost: return robot->hardware.sensor3.FiringCost();
		case hvShotSensorFocusDistance: return robot->hardware.sensor3.Distance();
		case hvShotSensorFocusDirection: return robot->hardware.sensor3.Direction();
		case hvShotSensorSeesFriendly: return FromBoolean(robot->hardware.sensor3.SeesFriendly());
		case hvShotSensorSeesEnemy: return FromBoolean(robot->hardware.sensor3.SeesEnemy());
		case hvShotSensorTime: return robot->hardware.sensor3.Time();
		case hvShotSensorFound: return robot->hardware.sensor3.Found();
		case hvShotSensorAngleFound: return (robot->hardware.sensor3.WhereFound() - robot->Position()).Angle();
		case hvShotSensorRangeFound: return (robot->hardware.sensor3.WhereFound() - robot->Position()).Norm();
		case hvShotSensorSideFound: return robot->hardware.sensor3.Side();
		case hvShotSensorRadiusFound: return robot->hardware.sensor3.Radius();
		case hvShotSensorPowerFound: return robot->hardware.sensor3.Energy();
		case hvShotSensorTypeFound: return robot->hardware.sensor3.Type();
		case hvShotSensorRangeOverall: return (robot->hardware.sensor3.WhereOverall() - robot->Position()).Norm();
		case hvShotSensorAngleOverall: return (robot->hardware.sensor3.WhereOverall() - robot->Position()).Angle();
		case hvShotSensorCurrentResult: return robot->hardware.sensor3.CurrentResult();
		case hvShotSensorNumResults: return robot->hardware.sensor3.NumResults();
		case hvShotSensorMaxResults: return robot->hardware.sensor3.NumResults();
	// defenses
		case hvArmor: return robot->hardware.Armor();
		case hvMaxArmor: return robot->hardware.MaxArmor();
		case hvRepairRate: return robot->hardware.RepairRate();
		case hvMaxRepairRate: return robot->hardware.MaxRepairRate();
		case hvShield: return robot->hardware.Shield();
		case hvMaxShield: return robot->hardware.MaxShield();
		case hvShieldFraction: return robot->ShieldFraction();
		case hvLastHit: return robot->LastHit() ? robot->LastHit()->ID() : 0;
	// blaster
		case hvBlasterDamage: return robot->hardware.blaster.Damage();
		case hvBlasterRange: return robot->hardware.blaster.MaxRange();
		case hvBlasterSpeed: return robot->hardware.blaster.Speed();
		case hvBlasterLifetime: return robot->hardware.blaster.MaxLifetime();
		case hvBlasterReloadTime: return robot->hardware.blaster.ReloadTime();
		case hvBlasterFiringCost: return robot->hardware.blaster.FiringCost();
		case hvBlasterCooldown: return robot->hardware.blaster.Cooldown();
	// grenades
		case hvGrenadesDamage: return robot->hardware.grenades.Damage();
		case hvGrenadesSpeed: return robot->hardware.grenades.Speed();
		case hvGrenadesLifetime: return robot->hardware.grenades.MaxLifetime();
		case hvGrenadesRange: return robot->hardware.grenades.MaxRange();
		case hvGrenadesReloadTime: return robot->hardware.grenades.ReloadTime();
		case hvGrenadesFiringCost: return robot->hardware.grenades.FiringCost();
		case hvGrenadesCooldown: return robot->hardware.grenades.Cooldown();
		case hvGrenadesRadius: return robot->hardware.grenades.ExplosionRadius();
	// forcefield
		case hvForceFieldMaxPower: return robot->hardware.forceField.MaxPower();
		case hvForceFieldRange: return robot->hardware.forceField.MaxRange();
		case hvForceFieldDistance: return robot->hardware.forceField.Distance();
		case hvForceFieldDirection: return robot->hardware.forceField.Direction();
		case hvForceFieldPower: return robot->hardware.forceField.Power();
		case hvForceFieldAngle: return robot->hardware.forceField.Angle();
		case hvForceFieldRadius: return robot->hardware.forceField.Radius();
	//
		default: throw GBUnknownHardwareVariableError();
	}
// never reached
	throw GBUnknownHardwareVariableError();
	return 0;
}

void GBStackBrain::WriteHardware(const GBSymbolIndex index, const GBStackDatum value,
		GBRobot * robot, GBWorld *) {
	switch ( index ) {
		case hvTime: case hvTimeLimit:
		case hvWorldWidth: case hvWorldHeight:
		case hvRadius: case hvMass: case hvSpeed:
		case hvProcessor: case hvRemaining:
		case hvSideID: case hvTypeID: case hvRobotID: case hvParentID:
		case hvPopulation:
			throw GBReadOnlyError();
		case hvEnginePower: robot->hardware.SetEnginePower(value); break;
		case hvFlag: robot->flag = value; break;
		case hvEngineMaxPower:
		case hvCollision: case hvFriendlyCollision: case hvEnemyCollision:
		case hvFoodCollision: case hvShotCollision: case hvWallCollision:
			throw GBReadOnlyError();
	// energy
		case hvEnergy: case hvMaxEnergy: case hvSolarCells: case hvEater: case hvEaten:
		case hvSyphonMaxRate: case hvSyphonRange: case hvSyphoned:
			throw GBReadOnlyError();
		case hvSyphonDistance: robot->hardware.syphon.SetDistance(value); break;
		case hvSyphonDirection: robot->hardware.syphon.SetDirection(value); break;
		case hvSyphonRate: robot->hardware.syphon.SetRate(value); break;
		case hvEnemySyphonMaxRate: case hvEnemySyphonRange: case hvEnemySyphoned:
			throw GBReadOnlyError();
		case hvEnemySyphonDistance: robot->hardware.enemySyphon.SetDistance(value); break;
		case hvEnemySyphonDirection: robot->hardware.enemySyphon.SetDirection(value); break;
		case hvEnemySyphonRate: robot->hardware.enemySyphon.SetRate(value); break;
	// constructor
		case hvConstructorType: {
			int index = ToInteger(value);
			robot->hardware.constructor.Start(index ? robot->Owner()->GetType(index) : nil);
		} break;
		case hvConstructorRate: robot->hardware.constructor.SetRate(value); break;
		case hvConstructorMaxRate: case hvConstructorProgress: case hvConstructorRemaining:
		case hvChildID:
			throw GBReadOnlyError();
	// robot sensor
		case hvRobotSensorRange: case hvRobotSensorFiringCost: throw GBReadOnlyError();
		case hvRobotSensorFocusDistance: robot->hardware.sensor1.SetDistance(value); break;
		case hvRobotSensorFocusDirection: robot->hardware.sensor1.SetDirection(value); break;
		case hvRobotSensorSeesFriends: robot->hardware.sensor1.SetSeesFriendly(value); break;
		case hvRobotSensorSeesEnemies: robot->hardware.sensor1.SetSeesEnemy(value); break;
		case hvRobotSensorTime: case hvRobotSensorFound: case hvRobotSensorRangeFound: case hvRobotSensorAngleFound:
		case hvRobotSensorSideFound: case hvRobotSensorRadiusFound: case hvRobotSensorMassFound: case hvRobotSensorEnergyFound:
		case hvRobotSensorTypeFound: case hvRobotSensorIDFound: case hvRobotSensorShieldFractionFound:
		case hvRobotSensorBombFound: case hvRobotSensorReloadingFound:
		case hvRobotSensorRangeOverall: case hvRobotSensorAngleOverall:
			throw GBReadOnlyError();
		case hvRobotSensorCurrentResult: robot->hardware.sensor1.SetCurrentResult(ToInteger(value)); break;
		case hvRobotSensorNumResults: case hvRobotSensorMaxResults: throw GBReadOnlyError();
	// food sensor
		case hvFoodSensorRange: case hvFoodSensorFiringCost: throw GBReadOnlyError();
		case hvFoodSensorFocusDistance: robot->hardware.sensor2.SetDistance(value); break;
		case hvFoodSensorFocusDirection: robot->hardware.sensor2.SetDirection(value); break;
		case hvFoodSensorTime: case hvFoodSensorFound: case hvFoodSensorRangeFound: case hvFoodSensorAngleFound:
		case hvFoodSensorSideFound: case hvFoodSensorRadiusFound: case hvFoodSensorMassFound: case hvFoodSensorEnergyFound:
		case hvFoodSensorRangeOverall: case hvFoodSensorAngleOverall:
			throw GBReadOnlyError();
		case hvFoodSensorCurrentResult: robot->hardware.sensor2.SetCurrentResult(ToInteger(value)); break;
		case hvFoodSensorNumResults: case hvFoodSensorMaxResults: throw GBReadOnlyError();
	// shot sensor
		case hvShotSensorRange: case hvShotSensorFiringCost: throw GBReadOnlyError();
		case hvShotSensorFocusDistance: robot->hardware.sensor3.SetDistance(value); break;
		case hvShotSensorFocusDirection: robot->hardware.sensor3.SetDirection(value); break;
		case hvShotSensorSeesFriendly: robot->hardware.sensor3.SetSeesFriendly(value); break;
		case hvShotSensorSeesEnemy: robot->hardware.sensor3.SetSeesEnemy(value); break;
		case hvShotSensorTime: case hvShotSensorFound: case hvShotSensorRangeFound: case hvShotSensorAngleFound:
		case hvShotSensorSideFound: case hvShotSensorRadiusFound: case hvShotSensorPowerFound:
		case hvShotSensorRangeOverall: case hvShotSensorAngleOverall:
			throw GBReadOnlyError();
		case hvShotSensorCurrentResult: robot->hardware.sensor3.SetCurrentResult(ToInteger(value)); break;
		case hvShotSensorNumResults: case hvShotSensorMaxResults: throw GBReadOnlyError();
	// defenses
		case hvRepairRate: robot->hardware.SetRepairRate(value); break;
		case hvShield: robot->hardware.SetShield(value); break;
		case hvArmor: case hvMaxArmor: case hvMaxRepairRate: case hvMaxShield: case hvShieldFraction:
			throw GBReadOnlyError();
	// weapons
		case hvBlasterDamage: case hvBlasterRange: case hvBlasterSpeed: case hvBlasterLifetime:
		case hvBlasterReloadTime: case hvBlasterFiringCost: case hvBlasterCooldown:
		case hvGrenadesDamage: case hvGrenadesRange: case hvGrenadesSpeed: case hvGrenadesLifetime:
		case hvGrenadesReloadTime: case hvGrenadesFiringCost: case hvGrenadesCooldown: case hvGrenadesRadius:
		case hvForceFieldMaxPower: case hvForceFieldRange:
			throw GBReadOnlyError();
		case hvForceFieldDistance: robot->hardware.forceField.SetDistance(value); break;
		case hvForceFieldDirection: robot->hardware.forceField.SetDirection(value); break;
		case hvForceFieldPower: robot->hardware.forceField.SetPower(value); break;
		case hvForceFieldAngle: robot->hardware.forceField.SetAngle(value); break;
		case hvForceFieldRadius: throw GBReadOnlyError();
	//
		default: throw GBUnknownHardwareVariableError();
	}
}

GBVector GBStackBrain::ReadHardwareVector(const GBSymbolIndex index, GBRobot * robot, GBWorld * world) const {
	switch ( index ) {
		case hvvWorldSize: return world->Size();
	//
		case hvvPosition: return robot->Position();
		case hvvVelocity: return robot->Velocity();
	//
		case hvvEngineVelocity: return robot->hardware.EngineVelocity();
	// sensors
		case hvvRobotSensorWhereFound: return robot->hardware.sensor1.WhereFound();
		case hvvRobotSensorVelocityFound: return robot->hardware.sensor1.Velocity();
		case hvvRobotSensorWhereOverall: return robot->hardware.sensor1.WhereOverall();
		case hvvFoodSensorWhereFound: return robot->hardware.sensor2.WhereFound();
		case hvvFoodSensorVelocityFound: return robot->hardware.sensor2.Velocity();
		case hvvFoodSensorWhereOverall: return robot->hardware.sensor2.WhereOverall();
		case hvvShotSensorWhereFound: return robot->hardware.sensor3.WhereFound();
		case hvvShotSensorVelocityFound: return robot->hardware.sensor3.Velocity();
		case hvvShotSensorWhereOverall: return robot->hardware.sensor3.WhereOverall();
	//
		default: throw GBUnknownHardwareVariableError();
	}
	return GBVector(0, 0);
}

void GBStackBrain::WriteHardwareVector(const GBSymbolIndex index, const GBVector value,
		GBRobot * robot, GBWorld *) {
	switch ( index ) {
		case hvvWorldSize: case hvvPosition: case hvvVelocity:
			throw GBReadOnlyError();
		case hvvEngineVelocity: robot->hardware.SetEngineVelocity(value); break;
	// sensors
		case hvvRobotSensorWhereFound: case hvvRobotSensorVelocityFound: case hvvRobotSensorWhereOverall:
		case hvvFoodSensorWhereFound: case hvvFoodSensorVelocityFound: case hvvFoodSensorWhereOverall:
		case hvvShotSensorWhereFound: case hvvShotSensorVelocityFound: case hvvShotSensorWhereOverall:
			throw GBReadOnlyError();
	//
		default: throw GBUnknownHardwareVariableError();
	}
}

void GBStackBrain::ExecutePrimitive(GBSymbolIndex index, GBRobot * robot, GBWorld * world) {
	GBStackDatum temp, temp2, temp3;
	long tempInt;
	switch ( index ) {
		case opNop: break;
	// stack manipulation
		case opDrop: Pop(); break;
		case op2Drop: Pop(); Pop(); break;
		case opNip: temp = Pop(); Pop(); Push(temp); break;
		case opRDrop: PopReturn(); break;
		case opDropN: {
			int n = PopInteger();
			if ( n > stackHeight ) throw GBBadArgumentError();
			stackHeight -= n;
		} break;
		case opSwap: temp = Pop(); temp2 = Pop(); Push(temp); Push(temp2); break;
		case op2Swap: { GBVector v1 = PopVector(); GBVector v2 = PopVector();
			PushVector(v1); PushVector(v2); } break;
		case opRotate: temp = Pop(); temp2 = Pop(); temp3 = Pop(); Push(temp2); Push(temp); Push(temp3); break;
		case opReverseRotate: temp = Pop(); temp2 = Pop(); temp3 = Pop(); Push(temp); Push(temp3); Push(temp2); break;
		case opDup: temp = Peek(); Push(temp); break;
		case op2Dup: temp = Peek(2); temp2 = Peek(); Push(temp); Push(temp2); break;
		case opTuck: temp = Pop(); temp2 = Pop(); Push(temp); Push(temp2); Push(temp); break;
		case opOver: temp = Peek(2); Push(temp); break;
		case op2Over: temp = Peek(4); temp2 = Peek(3); Push(temp); Push(temp2); break;
		case opStackHeight: Push(stackHeight); break;
		case opStackLimit: Push(kStackLimit); break;
		case opPick: Push(Peek(PopInteger())); break;
		case opToReturn: PushReturn(ToAddress(Pop())); break;
		case opFromReturn: Push(PopReturn()); break;
	// branches
		case opJump: pc = ToAddress(Pop()); break;
		case opCall: ExecuteCall(ToAddress(Pop())); break;
		case opReturn: pc = PopReturn(); break;
		case opIfGo: temp = Pop(); if ( Pop() ) pc = ToAddress(temp); break;
		case opIfElseGo: temp = Pop(); temp2 = Pop();
			if ( Pop() ) pc = ToAddress(temp2); else pc = ToAddress(temp); break;
		case opIfCall: temp = Pop(); if ( Pop() ) ExecuteCall(ToAddress(temp)); break;
		case opIfElseCall: temp = Pop(); temp2 = Pop();
			if ( Pop() ) ExecuteCall(ToAddress(temp2)); else ExecuteCall(ToAddress(temp)); break;
		case opIfReturn: if ( Pop() ) pc = PopReturn(); break;
		case opNotIfGo: temp = Pop(); if ( ! Pop() ) pc = ToAddress(temp); break;
		case opNotIfReturn: if ( ! Pop() ) pc = PopReturn(); break;
		case opNotIfCall: temp = Pop(); if ( ! Pop() ) ExecuteCall(ToAddress(temp)); break;
	// arithmetic
		case opAdd: TwoNumberToNumberOp(operator +); break;
		case opSubtract: TwoNumberToNumberOp(operator -); break;
		case opNegate: NumberToNumberOp(operator -); break;
		// mult and divide are written out because of MrCpp internal error
		case opMultiply: temp = Pop(); Push(Pop() * temp); break;
		case opDivide: temp = Pop(); Push(Pop() / temp); break;
		case opReciprocal: Push(GBNumber(1) / Pop()); break;
		case opMod: TwoNumberToNumberOp(mod); break;
		case opRem: TwoNumberToNumberOp(rem); break;
		case opSquare: Push(square(Pop())); break;
		case opSqrt: NumberToNumberOp(sqrt); break;
		case opExponent: TwoNumberToNumberOp(pow); break;
		case opIsInteger: PushBoolean(IsInteger(Pop())); break;
		case opFloor: Push(floor(Pop())); break;
		case opCeiling: Push(ceil(Pop())); break;
		case opRound: Push(round(Pop())); break;
		case opMin: Push(min(Pop(), Pop())); break;
		case opMax: Push(max(Pop(), Pop())); break;
		case opAbs: NumberToNumberOp(abs); break;
		case opSignum: NumberToNumberOp(signum); break;
		case opReorient: NumberToNumberOp(reorient); break;
		case opSine: NumberToNumberOp(sin); break;
		case opCosine: NumberToNumberOp(cos); break;
		case opTangent: NumberToNumberOp(tan); break;
		case opArcSine: NumberToNumberOp(asin); break;
		case opArcCosine: NumberToNumberOp(acos); break;
		case opArcTangent: NumberToNumberOp(atan); break;
		case opRandom: temp = Pop(); Push(world->Randoms().InRange(Pop(), temp)); break;
		case opRandomAngle: Push(world->Randoms().Angle()); break;
		case opRandomInt: temp = Pop(); Push(world->Randoms().LongInRange(ceil(Pop()), floor(temp))); break;
		case opRandomBoolean: PushBoolean(world->Randoms().Boolean(Pop())); break;
	// constants
		case opPi: Push(kPi); break;
		case op2Pi: Push(kPi * 2); break;
		case opPiOver2: Push(kPi / 2); break;
		case opE: Push(kE); break;
		case opEpsilon: Push(kEpsilon); break;
		case opInfinity: Push(kInfinity); break;
	// vector operations
		case opRectToPolar: { GBVector v = PopVector(); Push(v.Norm()); Push(v.Angle()); } break;
		case opPolarToRect: temp = Pop(); temp2 = Pop(); PushVector(GBFinePoint::MakePolar(temp2, temp)); break;
		case opVectorAdd: TwoVectorToVectorOp(&GBFinePoint::operator +); break;
		case opVectorSubtract: TwoVectorToVectorOp(&GBFinePoint::operator -); break;
		case opVectorNegate: VectorToVectorOp(&GBFinePoint::operator -); break;
		case opVectorScalarMultiply: temp = Pop(); PushVector(PopVector() * temp); break;
		case opVectorScalarDivide: temp = Pop(); PushVector(PopVector() / temp); break;
		case opVectorNorm: VectorToScalarOp(&GBFinePoint::Norm); break;
		case opVectorAngle: VectorToScalarOp(&GBFinePoint::Angle); break;
		case opDotProduct: TwoVectorToScalarOp(&GBFinePoint::DotProduct);  break;
		case opProject: TwoVectorToVectorOp(&GBFinePoint::Projection); break;
		case opCross: TwoVectorToScalarOp(&GBFinePoint::Cross); break;
		case opUnitize: VectorToVectorOp(&GBFinePoint::Unit); break;
		case opDistance: Push((PopVector() - PopVector()).Norm()); break;
		case opInRange: temp = Pop(); PushBoolean(PopVector().InRange(PopVector(), temp)); break;
		case opRestrictPosition: {
			temp = Pop(); //wall distance
			GBVector pos = PopVector();
			Push(clamp(pos.x, temp, world->Size().x - temp));
			Push(clamp(pos.y, temp, world->Size().y - temp));
			} break;
		case opVectorEqual: PushBoolean(PopVector() == PopVector()); break;
		case opVectorNotEqual: PushBoolean(PopVector() != PopVector()); break;
	// comparisons
		case opEqual: PushBoolean(Pop() == Pop()); break;
		case opNotEqual: PushBoolean(Pop() != Pop()); break;
		case opLessThan: temp = Pop(); PushBoolean(Pop() < temp); break;
		case opLessThanOrEqual: temp = Pop(); PushBoolean(Pop() <= temp); break;
		case opGreaterThan: temp = Pop(); PushBoolean(Pop() > temp); break;
		case opGreaterThanOrEqual: temp = Pop(); PushBoolean(Pop() >= temp); break;
	// booleans
		case opNot: PushBoolean(! Pop()); break;
		case opAnd: temp = Pop(); temp2 = Pop(); PushBoolean(temp && temp2); break;
		case opOr: temp = Pop(); temp2 = Pop(); PushBoolean(temp || temp2); break;
		case opXor: temp = Pop(); temp2 = Pop();
			PushBoolean(temp && ! temp2 || ! temp && temp2); break;
		case opNand: temp = Pop(); temp2 = Pop(); PushBoolean(! (temp && temp2)); break;
		case opNor: temp = Pop(); temp2 = Pop(); PushBoolean(! (temp || temp2)); break;
		case opValueConditional: temp = Pop(); temp2 = Pop();
			if ( Pop() ) Push(temp2); else Push(temp);
			break;
	// misc external
		case opPrint:
			DoPrint(ToString(Pop()));
			if ( world->reportPrints )
				NonfatalError(robot->Description() + " prints: " + *lastPrint);
			break;
		case opPrintVector:
			DoPrint(ToString(PopVector()));
			if ( world->reportPrints )
				NonfatalError(robot->Description() + " prints: " + *lastPrint);
			break;
		case opBeep: StartSound(siBeep); break;
		case opStop: SetStatus(bsStopped); break;
		case opPause: if ( world->reportErrors ) world->running = false; break;
		case opSync: remaining = 0; break;
	// basic hardware
		case opSeekLocation: robot->EngineSeek(PopVector(), GBVector(0, 0)); break;
		case opSeekMovingLocation: {
			GBVector vel = PopVector();
			robot->EngineSeek(PopVector(), vel);
			} break;
		case opDie: robot->Die(robot->Owner()); SetStatus(bsStopped); break;
		case opWriteLocalMemory:
			tempInt = PopInteger();
			WriteLocalMemory(tempInt, Pop(), robot);
			break;
		case opReadLocalMemory:
			tempInt = PopInteger();
			Push(ReadLocalMemory(tempInt, robot));
			break;
		case opWriteLocalVector:
			tempInt = PopInteger();
			WriteLocalMemory(tempInt + 1, Pop(), robot);
			WriteLocalMemory(tempInt, Pop(), robot);
			break;
		case opReadLocalVector:
			tempInt = PopInteger();
			Push(ReadLocalMemory(tempInt, robot));
			Push(ReadLocalMemory(tempInt + 1, robot));
			break;
		case opWriteSharedMemory:
			tempInt = PopInteger();
			robot->hardware.radio.Write(Pop(), tempInt, robot->Owner());
			break;
		case opReadSharedMemory:
			Push(robot->hardware.radio.Read(PopInteger(), robot->Owner()));
			break;
		case opWriteSharedVector:
			tempInt = PopInteger();
			robot->hardware.radio.Write(Pop(), tempInt + 1, robot->Owner());
			robot->hardware.radio.Write(Pop(), tempInt, robot->Owner());
			break;
		case opReadSharedVector:
			tempInt = PopInteger();
			Push(robot->hardware.radio.Read(tempInt, robot->Owner()));
			Push(robot->hardware.radio.Read(tempInt + 1, robot->Owner()));
			break;
		case opMessagesWaiting:
			Push(robot->hardware.radio.MessagesWaiting(PopInteger(), robot->Owner()));
			break;
		case opSendMessage: {
				GBMessage sendee;
				tempInt = PopInteger(); //channel
				int numArgs = ToInteger(Pop()); //number of numbers
				for ( int i = 0; i < numArgs; i++ ) {
					sendee.AddDatum(Pop()); //higher indices in message correspond with earlier numbers in stack. :(
				}
				if ( numArgs <= 0 )
					throw GBGenericError("Cannot send message of non-positive length");
				robot->hardware.radio.Send(sendee, tempInt, robot->Owner());
			} break;
		case opReceiveMessage: {
				tempInt = PopInteger();
				const GBMessage * received = robot->hardware.radio.Receive(tempInt, robot->Owner());
				if ( received == 0 ) {
					Push(0);
				} else {
					if ( received->Length() <= 0 ) {
						throw GBGenericError("non-positive length message received");
					}
					for ( int i = received->Length() - 1; i >= 0; i-- )
						Push(received->Datum(i));
					Push(received->Length());
				}
			} break;
		case opClearMessages:
			robot->hardware.radio.ClearChannel(PopInteger(), robot->Owner());
			break;
		case opSkipMessages:
			tempInt = PopInteger();
			robot->hardware.radio.SkipMessages(tempInt, PopInteger(), robot->Owner());
			break;
		case opTypePopulation: {
				GBRobotType * theType = robot->Owner()->GetType(PopInteger());
				if (theType)
					Push(theType->Population());
				else
					Push(-1);
			} break;
		case opAutoConstruct: {
			GBConstructorState & ctor = robot->hardware.constructor;
			if ( robot->Energy() > robot->hardware.MaxEnergy() * .9 ) {
				if ( ! ctor.Type() ) ctor.Start(robot->Type());
				ctor.SetRate(ctor.MaxRate());
			} else
				ctor.SetRate(ctor.Type() && robot->Energy() > ctor.Remaining() + 10 ? ctor.MaxRate() : GBNumber(0));
			} break;
		case opBalanceTypes: { // frac type --
				GBRobotType * theType = robot->Owner()->GetType(PopInteger());
				GBNumber fraction = Pop();
				if (theType && GBNumber(theType->Population()) < fraction * robot->Owner()->Scores().Population())
					robot->hardware.constructor.Start(theType); //FIXME don't abort?
			} break;
	// sensors
		case opFireRobotSensor: robot->hardware.sensor1.Fire(); break;
		case opFireFoodSensor: robot->hardware.sensor2.Fire(); break;
		case opFireShotSensor: robot->hardware.sensor3.Fire(); break;
		case opRobotSensorNext: Push(robot->hardware.sensor1.NextResult() ? 1 : 0); break;
		case opFoodSensorNext: Push(robot->hardware.sensor2.NextResult() ? 1 : 0); break;
		case opShotSensorNext: Push(robot->hardware.sensor3.NextResult() ? 1 : 0); break;
		case opPeriodicRobotSensor:
			FirePeriodic(robot->hardware.sensor1, world);
			break;
		case opPeriodicFoodSensor:
			FirePeriodic(robot->hardware.sensor2, world);
			break;
		case opPeriodicShotSensor:
			FirePeriodic(robot->hardware.sensor3, world);
			break;
	// weapons
		case opFireBlaster: robot->hardware.blaster.Fire(Pop()); break;
		case opFireGrenade: temp = Pop(); robot->hardware.grenades.Fire(Pop(), temp); break;
		case opLeadBlaster: { //pos vel --
			GBVelocity vel = PopVector() - robot->Velocity();
			GBPosition pos = PopVector() - robot->Position();
			GBPosition target = LeadShot(pos, vel, robot->hardware.blaster.Speed(), robot->Radius());
			if ( target.Nonzero() && target.Norm() <= robot->hardware.blaster.MaxRange() + robot->Radius() )
				robot->hardware.blaster.Fire(target.Angle());
			} break;
		case opLeadGrenade: { //pos vel --
			GBVelocity vel = PopVector() - robot->Velocity();
			GBPosition pos = PopVector() - robot->Position();
			GBPosition target = LeadShot(pos, vel, robot->hardware.grenades.Speed(), robot->Radius());
			if ( target.Nonzero() && target.Norm() <= robot->hardware.grenades.MaxRange() + robot->Radius() )
				robot->hardware.grenades.Fire(target.Norm(), target.Angle()); //worry about short range?
			} break;
		case opSetForceField: { //pos angle --
			temp = Pop();
			GBPosition pos = PopVector() - robot->Position();
			robot->hardware.forceField.SetDistance(pos.Norm());
			robot->hardware.forceField.SetDirection(pos.Angle());
			robot->hardware.forceField.SetAngle(temp);
			robot->hardware.forceField.SetPower(robot->hardware.forceField.MaxPower());
			} break;
	// otherwise...
		default:	
			throw GBUnknownInstructionError();
			break;
	}
}

//pos and vel are relative to ourself
static GBPosition LeadShot(const GBPosition & pos, const GBPosition & vel, GBSpeed shotSpeed, GBDistance r) {
#if 1
	GBNumber dt = (pos + vel * (pos.Norm() / shotSpeed)).Norm() / shotSpeed; //two plies for accuracy with radially moving targets
	return pos + vel * dt;
#else
//Precise version, not used yet because it changes behavior.
//Solve for exact time of impact: (pos + vel * dt).Norm() = shotSpeed * dt + r
	GBNumber a = vel.NormSquare() - square(shotSpeed);
	GBNumber b = (pos.DotProduct(vel) - shotSpeed * r) * 2;
	GBNumber c = pos.NormSquare() - square(r);
	GBNumber det = square(b) - a * c * 4;
	if (det < 0 || a == 0)
		return GBVector(0, 0); //don't shoot
//try both roots and use least positive root
	GBNumber dt1 = (- b - det.Sqrt()) / (a * 2);
	GBNumber dt2 = (- b + det.Sqrt()) / (a * 2);
	if (dt1 < 0) {
		if (dt2 < 0)
			return GBVector(0, 0);
		else
			return pos + vel * dt2;
	} else if (dt2 < 0 || dt1 < dt2)
		return pos + vel * dt1;
	else
		return pos + vel * dt2;
#endif
}

void GBStackBrain::FirePeriodic(GBSensorState & sensor, GBWorld * world) {
	GBFrames period = PopInteger();
	if ( world->CurrentFrame() >= sensor.Time() + period || sensor.Time() <= 0 ) {
		sensor.Fire();
		remaining = 0;
		PushBoolean(true);
	} else PushBoolean(false);
}
