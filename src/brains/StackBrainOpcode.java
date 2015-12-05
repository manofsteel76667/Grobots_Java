/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBStackBrainOpcodes.h
// opcodes and hardware variable numbers for stack brains.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package brains;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* @formatter:off */
public enum StackBrainOpcode {
	/************** Primitives *******************/
	opNop(OpCodeType.ocPrimitive, "nop"),
	// stack manipulation
	opDrop(OpCodeType.ocPrimitive, "drop"), 
	op2Drop(OpCodeType. ocPrimitive, "2drop"), 
	opNip(OpCodeType.ocPrimitive, "nip"), 
	opRDrop(OpCodeType.ocPrimitive, "rdrop"), 
	opDropN(OpCodeType.ocPrimitive, "dropn"), 
	opSwap(OpCodeType.ocPrimitive, "swap"), 
	opSubswap(OpCodeType.ocPrimitive, "subswap"), 
	op2Swap(OpCodeType.ocPrimitive,	"2swap"), 
	opRotate(OpCodeType.ocPrimitive, "rot"), 
	opReverseRotate(OpCodeType.ocPrimitive, "rrot"), 
	opDup(OpCodeType.ocPrimitive, "dup"), 
	op2Dup(OpCodeType.ocPrimitive, "2dup"), 
	opTuck(OpCodeType.ocPrimitive, "tuck"), 
	opOver(OpCodeType.ocPrimitive, "over"), 
	op2Over(OpCodeType.ocPrimitive, "2over"), 
	opStackHeight(OpCodeType.ocPrimitive, "stack"), 
	opStackLimit(OpCodeType.ocPrimitive, "stack-limit"), 
	opRoll(OpCodeType.ocPrimitive, "roll"), 
	opPick(OpCodeType.ocPrimitive, "pick"), 
	opToReturn(OpCodeType.ocPrimitive, ">r"), 
	opFromReturn(OpCodeType.ocPrimitive, "r>"), // remove?
	// branches of various sorts
	opJump(OpCodeType.ocPrimitive, "jump"), 
	opCall(OpCodeType.ocPrimitive, "call"), 
	opReturn(OpCodeType.ocPrimitive, "return"), 
	opIfGo(OpCodeType.ocPrimitive, "ifg"), 
	opIfElseGo(OpCodeType.ocPrimitive, "ifeg"), 
	opIfCall(OpCodeType.ocPrimitive, "ifc"), 
	opIfElseCall(OpCodeType.ocPrimitive, "ifec"), 
	opIfReturn(OpCodeType.ocPrimitive, "ifr"), 
	opNotIfGo(OpCodeType.ocPrimitive, "nifg"), 
	opNotIfReturn(OpCodeType.ocPrimitive, "nifr"), 
	opNotIfCall(OpCodeType.ocPrimitive, "nifc"),
	// arithmetic and friends
	opAdd(OpCodeType.ocPrimitive, "+"), 
	opSubtract(OpCodeType.ocPrimitive, "-"), 
	opNegate(OpCodeType.ocPrimitive, "negate"), 
	opMultiply(OpCodeType.ocPrimitive, "*"), 
	opDivide(OpCodeType.ocPrimitive, "/"), 
	opReciprocal(OpCodeType.ocPrimitive, "reciprocal"), 
	opMod(OpCodeType.ocPrimitive, "mod"), 
	opRem(OpCodeType.ocPrimitive, "rem"), 
	opSquare(OpCodeType.ocPrimitive, "square"), 
	opSqrt(OpCodeType.ocPrimitive, "sqrt"), 
	opExponent(OpCodeType.ocPrimitive, "exponent"), 
	opIsInteger(OpCodeType.ocPrimitive, "is-integer"), 
	opFloor(OpCodeType.ocPrimitive, "floor"), 
	opCeiling(OpCodeType.ocPrimitive, "ceiling"), 
	opRound(OpCodeType.ocPrimitive, "round"), 
	opMin(OpCodeType.ocPrimitive, "min"), 
	opMax(OpCodeType.ocPrimitive, "max"), 
	opAbs(OpCodeType.ocPrimitive, "abs"), 
	opSignum(OpCodeType.ocPrimitive, "signum"), 
	opReorient(OpCodeType.ocPrimitive, "reorient"), 
	opSine(OpCodeType.ocPrimitive, "sin"), 
	opCosine(OpCodeType.ocPrimitive, "cos"), 
	opTangent(OpCodeType.ocPrimitive, "tan"), 
	opArcSine(OpCodeType.ocPrimitive, "arcsin"), 
	opArcCosine(OpCodeType.ocPrimitive, "arccos"), 
	opArcTangent(OpCodeType.ocPrimitive, "arctan"), 
	opRandom(OpCodeType.ocPrimitive, "random"), 
	opRandomAngle(OpCodeType.ocPrimitive, "random-angle"), 
	opRandomInt(OpCodeType.ocPrimitive, "random-int"), 
	opRandomBoolean(OpCodeType.ocPrimitive, "random-bool"),
	// constants
	opPi(OpCodeType.ocPrimitive, "pi"), 
	op2Pi(OpCodeType.ocPrimitive, "2pi"), 
	opPiOver2(OpCodeType.ocPrimitive, "pi/2"), 
	opE(OpCodeType.ocPrimitive, "e"),
	// should be builtin constants - also 0 and 1
	opEpsilon(OpCodeType.ocPrimitive, "epsilon"), 
	opInfinity(OpCodeType.ocPrimitive, "infinity"),
	// vector operations
	opRectToPolar(OpCodeType.ocPrimitive, "rect-to-polar"), 
	opPolarToRect(OpCodeType.ocPrimitive, "polar-to-rect"), 
	opVectorAdd(OpCodeType.ocPrimitive, "v+"), 
	opVectorSubtract(OpCodeType.ocPrimitive, "v-"), 
	opVectorNegate(OpCodeType.ocPrimitive, "vnegate"), 
	opVectorScalarMultiply(OpCodeType.ocPrimitive, "vs*"), 
	opVectorScalarDivide(OpCodeType.ocPrimitive, "vs/"), 
	opVectorNorm(OpCodeType.ocPrimitive, "norm"), 
	opVectorAngle(OpCodeType.ocPrimitive, "angle"), 
	opDotProduct(OpCodeType.ocPrimitive, "dot"), 
	opProject(OpCodeType.ocPrimitive, "project"), 
	opCross(OpCodeType.ocPrimitive, "cross"), 
	opUnitize(OpCodeType.ocPrimitive, "unitize"), 
	opVectorRotateTo(OpCodeType.ocPrimitive, "rotate-to"), 
	opVectorRotateFrom(OpCodeType.ocPrimitive, "rotate-from"), 
	opDistance(OpCodeType.ocPrimitive, "dist"), 
	opInRange(OpCodeType.ocPrimitive, "in-range"), 
	opVectorEqual(OpCodeType.ocPrimitive, "v="), 
	opVectorNotEqual(OpCodeType.ocPrimitive, "v<>"),
	// comparisons and Boolean operations
	opEqual(OpCodeType.ocPrimitive, "="), 
	opNotEqual(OpCodeType.ocPrimitive, "<>"), 
	opLessThan(OpCodeType.ocPrimitive, "<"), 
	opGreaterThan(OpCodeType.ocPrimitive, ">"), 
	opLessThanOrEqual(OpCodeType.ocPrimitive, "<="), 
	opGreaterThanOrEqual(OpCodeType.ocPrimitive, ">="), 
	opNot(OpCodeType.ocPrimitive, "not"), 
	opAnd(OpCodeType.ocPrimitive, "and"), 
	opOr(OpCodeType.ocPrimitive, "or"), 
	opXor(OpCodeType.ocPrimitive, "xor"), 
	opNand(OpCodeType.ocPrimitive, "nand"), 
	opNor(OpCodeType.ocPrimitive, "nor"), 
	opValueConditional(OpCodeType.ocPrimitive, "ifev"),
	// misc external
	opPrint(OpCodeType.ocPrimitive, "print"), 
	opPrintVector(OpCodeType.ocPrimitive, "vprint"), 
	opBeep(OpCodeType.ocPrimitive, "beep"), 
	opPause(OpCodeType.ocPrimitive, "pause"), 
	opStop(OpCodeType.ocPrimitive, "stop"), 
	opSync(OpCodeType.ocPrimitive, "sync"),
	// basic hardware
	opSeekLocation(OpCodeType.ocPrimitive, "seek-location"), 
	opSeekMovingLocation(OpCodeType.ocPrimitive, "seek-moving-location"), 
	opRestrictPosition(OpCodeType.ocPrimitive, "restrict-position"), 
	opDie(OpCodeType.ocPrimitive, "die"), 
	opWriteLocalMemory(OpCodeType.ocPrimitive, "store"), 
	opReadLocalMemory(OpCodeType.ocPrimitive, "load"), 
	opWriteLocalVector(OpCodeType.ocPrimitive, "vstore"), 
	opReadLocalVector(OpCodeType.ocPrimitive, "vload"), 
	opWriteSharedMemory(OpCodeType.ocPrimitive, "write"), 
	opReadSharedMemory(OpCodeType.ocPrimitive, "read"), 
	opWriteSharedVector(OpCodeType.ocPrimitive, "vwrite"), 
	opReadSharedVector(OpCodeType.ocPrimitive, "vread"), 
	opMessagesWaiting(OpCodeType.ocPrimitive, "messages"), 
	opSendMessage(OpCodeType.ocPrimitive, "send"), 
	opReceiveMessage(OpCodeType.ocPrimitive, "receive"), 
	opClearMessages(OpCodeType.ocPrimitive, "clear-messages"), 
	opSkipMessages(OpCodeType.ocPrimitive, "skip-messages"), 
	opTypePopulation(OpCodeType.ocPrimitive, "type-population"), 
	opAutoConstruct(OpCodeType.ocPrimitive, "autoconstruct"), 
	opBalanceTypes(OpCodeType.ocPrimitive, "balance-type"),
	// sensors
	opFireRobotSensor(OpCodeType.ocPrimitive, "fire-robot-sensor"), 
	opFireFoodSensor(OpCodeType.ocPrimitive, "fire-food-sensor"), 
	opFireShotSensor(OpCodeType.ocPrimitive, "fire-shot-sensor"), 
	opRobotSensorNext(OpCodeType.ocPrimitive, "next-robot"), 
	opFoodSensorNext(OpCodeType.ocPrimitive, "next-food"), 
	opShotSensorNext(OpCodeType.ocPrimitive, "next-shot"), 
	opPeriodicRobotSensor(OpCodeType.ocPrimitive, "periodic-robot-sensor"), 
	opPeriodicFoodSensor(OpCodeType.ocPrimitive, "periodic-food-sensor"), 
	opPeriodicShotSensor(OpCodeType.ocPrimitive, "periodic-shot-sensor"),
	// weapons
	opFireBlaster(OpCodeType.ocPrimitive, "fire-blaster"), 
	opFireGrenade(OpCodeType.ocPrimitive, "fire-grenade"), 
	opLeadBlaster(OpCodeType.ocPrimitive, "lead-blaster"), 
	opLeadGrenade(OpCodeType.ocPrimitive, "lead-grenade"), 
	opSetForceField(OpCodeType.ocPrimitive, "set-force-field"),
	// sentinel
	opEnd(OpCodeType.ocPrimitive, "end"),

	/******* Hardware variables ******************/
	hvTime(OpCodeType.ocHardwareVariable, "time"), 
	hvTimeLimit(OpCodeType.ocHardwareVariable, "time-limit"), 
	hvWorldWidth(OpCodeType.ocHardwareVariable, "world-width"), 
	hvWorldHeight(OpCodeType.ocHardwareVariable, "world-height"), 
	hvRadius(OpCodeType.ocHardwareVariable, "radius"), 
	hvMass(OpCodeType.ocHardwareVariable, "mass"), 
	hvSpeed(OpCodeType.ocHardwareVariable, "speed"), 
	hvProcessor(OpCodeType.ocHardwareVariable, "processor"), 
	hvRemaining(OpCodeType.ocHardwareVariable, "remaining"), 
	hvSideID(OpCodeType.ocHardwareVariable, "side"), 
	hvTypeID(OpCodeType.ocHardwareVariable, "type"), 
	hvRobotID(OpCodeType.ocHardwareVariable, "id"), 
	hvParentID(OpCodeType.ocHardwareVariable, "parent-id"), 
	hvPopulation(OpCodeType.ocHardwareVariable, "population"), 
	hvEnginePower(OpCodeType.ocHardwareVariable, "engine-power"), 
	hvEngineMaxPower(OpCodeType.ocHardwareVariable, "engine-max-power"), 
	hvFlag(OpCodeType.ocHardwareVariable, "flag"), 
	hvCollision(OpCodeType.ocHardwareVariable, "collision"), 
	hvFriendlyCollision(OpCodeType.ocHardwareVariable, "friendly-collision"), 
	hvEnemyCollision(OpCodeType.ocHardwareVariable, "enemy-collision"), 
	hvFoodCollision(OpCodeType.ocHardwareVariable, "food-collision"), 
	hvShotCollision(OpCodeType.ocHardwareVariable, "shot-collision"), 
	hvWallCollision(OpCodeType.ocHardwareVariable, "wall-collision"),
	// energy
	hvEnergy(OpCodeType.ocHardwareVariable, "energy"), 
	hvMaxEnergy(OpCodeType.ocHardwareVariable, "max-energy"), 
	hvSolarCells(OpCodeType.ocHardwareVariable, "solar-cells"), 
	hvEater(OpCodeType.ocHardwareVariable, "eater"), 
	hvEaten(OpCodeType.ocHardwareVariable, "eaten"), 
	hvSyphonMaxRate(OpCodeType.ocHardwareVariable, "syphon-max-rate"), 
	hvSyphonRange(OpCodeType.ocHardwareVariable, "syphon-range"), 
	hvSyphonDistance(OpCodeType.ocHardwareVariable, "syphon-distance"), 
	hvSyphonDirection(OpCodeType.ocHardwareVariable, "syphon-direction"), 
	hvSyphonRate(OpCodeType.ocHardwareVariable, "syphon-rate"), 
	hvSyphoned(OpCodeType.ocHardwareVariable, "syphoned"), 
	hvEnemySyphonMaxRate(OpCodeType.ocHardwareVariable, "enemy-syphon-max-rate"), 
	hvEnemySyphonRange(OpCodeType.ocHardwareVariable, "enemy-syphon-range"), 
	hvEnemySyphonDistance(OpCodeType.ocHardwareVariable, "enemy-syphon-distance"), 
	hvEnemySyphonDirection(OpCodeType.ocHardwareVariable, "enemy-syphon-direction"), 
	hvEnemySyphonRate(OpCodeType.ocHardwareVariable, "enemy-syphon-rate"), 
	hvEnemySyphoned(OpCodeType.ocHardwareVariable, "enemy-syphoned"),
	// constructor
	hvConstructorMaxRate(OpCodeType.ocHardwareVariable, "constructor-max-rate"), 
	hvConstructorRate(OpCodeType.ocHardwareVariable, "constructor-rate"), 
	hvConstructorType(OpCodeType.ocHardwareVariable, "constructor-type"), 
	hvConstructorProgress(OpCodeType.ocHardwareVariable, "constructor-progress"), 
	hvConstructorRemaining(OpCodeType.ocHardwareVariable, "constructor-remaining"), 
	hvChildID(OpCodeType.ocHardwareVariable, "child-id"),
	// robot sensor
	hvRobotSensorRange(OpCodeType.ocHardwareVariable, "robot-sensor-range"), 
	hvRobotSensorFiringCost(OpCodeType.ocHardwareVariable, "robot-sensor-firing-cost"), 
	hvRobotSensorFocusDistance(OpCodeType.ocHardwareVariable, "robot-sensor-focus-distance"), 
	hvRobotSensorFocusDirection(OpCodeType.ocHardwareVariable, "robot-sensor-focus-direction"), 
	hvRobotSensorSeesFriends(OpCodeType.ocHardwareVariable, "robot-sensor-sees-friends"), 
	hvRobotSensorSeesEnemies(OpCodeType.ocHardwareVariable, "robot-sensor-sees-enemies"), 
	hvRobotSensorTime(OpCodeType.ocHardwareVariable, "robot-sensor-time"), 
	hvRobotSensorFound(OpCodeType.ocHardwareVariable, "robot-found"), 
	hvRobotSensorRangeFound(OpCodeType.ocHardwareVariable, "robot-distance"), 
	hvRobotSensorAngleFound(OpCodeType.ocHardwareVariable, "robot-direction"), 
	hvRobotSensorSideFound(OpCodeType.ocHardwareVariable, "robot-side"), 
	hvRobotSensorRadiusFound(OpCodeType.ocHardwareVariable, "robot-radius"), 
	hvRobotSensorMassFound(OpCodeType.ocHardwareVariable, "robot-mass"), 
	hvRobotSensorEnergyFound(OpCodeType.ocHardwareVariable, "robot-energy"), 
	hvRobotSensorTypeFound(OpCodeType.ocHardwareVariable, "robot-type"), 
	hvRobotSensorIDFound(OpCodeType.ocHardwareVariable, "robot-ID"), 
	hvRobotSensorShieldFractionFound(OpCodeType.ocHardwareVariable, "robot-shield-fraction"), 
	hvRobotSensorBombFound(OpCodeType.ocHardwareVariable, "robot-bomb"), 
	hvRobotSensorReloadingFound(OpCodeType.ocHardwareVariable, "robot-reloading"), 
	hvRobotSensorFlagFound(OpCodeType.ocHardwareVariable, "robot-flag"), 
	hvRobotSensorRangeOverall(OpCodeType.ocHardwareVariable, "robot-distance-overall"), 
	hvRobotSensorAngleOverall(OpCodeType.ocHardwareVariable, "robot-direction-overall"), 
	hvRobotSensorCurrentResult(OpCodeType.ocHardwareVariable, "current-robot-result"), 
	hvRobotSensorNumResults(OpCodeType.ocHardwareVariable, "num-robot-results"), 
	hvRobotSensorMaxResults(OpCodeType.ocHardwareVariable, "max-robot-results"),
	// food sensor
	hvFoodSensorRange(OpCodeType.ocHardwareVariable, "food-sensor-range"), 
	hvFoodSensorFiringCost(OpCodeType.ocHardwareVariable, "food-sensor-firing-cost"), 
	hvFoodSensorFocusDistance(OpCodeType.ocHardwareVariable, "food-sensor-focus-distance"), 
	hvFoodSensorFocusDirection(OpCodeType.ocHardwareVariable, "food-sensor-focus-direction"), 
	hvFoodSensorTime(OpCodeType.ocHardwareVariable, "food-sensor-time"), 
	hvFoodSensorFound(OpCodeType.ocHardwareVariable, "food-found"), 
	hvFoodSensorRangeFound(OpCodeType.ocHardwareVariable, "food-distance"), 
	hvFoodSensorAngleFound(OpCodeType.ocHardwareVariable, "food-direction"), 
	hvFoodSensorSideFound(OpCodeType.ocHardwareVariable, "food-side"), 
	hvFoodSensorRadiusFound(OpCodeType.ocHardwareVariable, "food-radius"), 
	hvFoodSensorMassFound(OpCodeType.ocHardwareVariable, "food-mass"), 
	hvFoodSensorEnergyFound(OpCodeType.ocHardwareVariable, "food-energy"), 
	hvFoodSensorRangeOverall(OpCodeType.ocHardwareVariable, "food-distance-overall"), 
	hvFoodSensorAngleOverall(OpCodeType.ocHardwareVariable, "food-direction-overall"), 
	hvFoodSensorCurrentResult(OpCodeType.ocHardwareVariable, "current-food-result"), 
	hvFoodSensorNumResults(OpCodeType.ocHardwareVariable, "num-food-results"), 
	hvFoodSensorMaxResults(OpCodeType.ocHardwareVariable, "max-food-results"),
	// shot sensor
	hvShotSensorRange(OpCodeType.ocHardwareVariable, "shot-sensor-range"), 
	hvShotSensorFiringCost(OpCodeType.ocHardwareVariable, "shot-sensor-firing-cost"), 
	hvShotSensorFocusDistance(OpCodeType.ocHardwareVariable, "shot-sensor-focus-distance"), 
	hvShotSensorFocusDirection(OpCodeType.ocHardwareVariable, "shot-sensor-focus-direction"), 
	hvShotSensorSeesFriendly(OpCodeType.ocHardwareVariable, "shot-sensor-sees-friendly"), 
	hvShotSensorSeesEnemy(OpCodeType.ocHardwareVariable, "shot-sensor-sees-enemy"), 
	hvShotSensorTime(OpCodeType.ocHardwareVariable, "shot-sensor-time"), 
	hvShotSensorFound(OpCodeType.ocHardwareVariable, "shot-found"), 
	hvShotSensorRangeFound(OpCodeType.ocHardwareVariable, "shot-distance"), 
	hvShotSensorAngleFound(OpCodeType.ocHardwareVariable, "shot-direction"), 
	hvShotSensorSideFound(OpCodeType.ocHardwareVariable, "shot-side"), 
	hvShotSensorRadiusFound(OpCodeType.ocHardwareVariable, "shot-radius"), 
	hvShotSensorPowerFound(OpCodeType.ocHardwareVariable, "shot-power"), 
	hvShotSensorTypeFound(OpCodeType.ocHardwareVariable, "shot-type"), 
	hvShotSensorRangeOverall(OpCodeType.ocHardwareVariable, "shot-distance-overall"), 
	hvShotSensorAngleOverall(OpCodeType.ocHardwareVariable, "shot-direction-overall"), 
	hvShotSensorCurrentResult(OpCodeType.ocHardwareVariable, "current-shot-result"), 
	hvShotSensorNumResults(OpCodeType.ocHardwareVariable, "num-shot-results"), 
	hvShotSensorMaxResults(OpCodeType.ocHardwareVariable, "max-shot-results"),
	// defenses
	hvArmor(OpCodeType.ocHardwareVariable, "armor"), 
	hvMaxArmor(OpCodeType.ocHardwareVariable, "max-armor"), 
	hvRepairRate(OpCodeType.ocHardwareVariable, "repair-rate"), 
	hvMaxRepairRate(OpCodeType.ocHardwareVariable, "max-repair-rate"), 
	hvShield(OpCodeType.ocHardwareVariable, "shield"), 
	hvMaxShield(OpCodeType.ocHardwareVariable, "max-shield"), 
	hvShieldFraction(OpCodeType.ocHardwareVariable, "shield-fraction"), 
	hvLastHit(OpCodeType.ocHardwareVariable, "last-hit"),
	// weapons
	hvBlasterDamage(OpCodeType.ocHardwareVariable, "blaster-damage"), 
	hvBlasterRange(OpCodeType.ocHardwareVariable, "blaster-range"), 
	hvBlasterSpeed(OpCodeType.ocHardwareVariable, "blaster-speed"), 
	hvBlasterLifetime(OpCodeType.ocHardwareVariable, "blaster-lifetime"), 
	hvBlasterReloadTime(OpCodeType.ocHardwareVariable, "blaster-reload-time"), 
	hvBlasterFiringCost(OpCodeType.ocHardwareVariable, "blaster-firing-cost"), 
	hvBlasterCooldown(OpCodeType.ocHardwareVariable, "blaster-cooldown"), 
	hvGrenadesDamage(OpCodeType.ocHardwareVariable, "grenades-damage"), 
	hvGrenadesRange(OpCodeType.ocHardwareVariable, "grenades-range"), 
	hvGrenadesSpeed(OpCodeType.ocHardwareVariable, "grenades-speed"), 
	hvGrenadesLifetime(OpCodeType.ocHardwareVariable, "grenades-lifetime"), 
	hvGrenadesReloadTime(OpCodeType.ocHardwareVariable, "grenades-reload-time"), 
	hvGrenadesFiringCost(OpCodeType.ocHardwareVariable, "grenades-firing-cost"), 
	hvGrenadesCooldown(OpCodeType.ocHardwareVariable, "grenades-cooldown"), 
	hvGrenadesRadius(OpCodeType.ocHardwareVariable, "grenades-radius"), 
	hvForceFieldMaxPower(OpCodeType.ocHardwareVariable, "force-field-max-power"), 
	hvForceFieldRange(OpCodeType.ocHardwareVariable, "force-field-range"), 
	hvForceFieldDistance(OpCodeType.ocHardwareVariable, "force-field-distance"), 
	hvForceFieldDirection(OpCodeType.ocHardwareVariable, "force-field-direction"), 
	hvForceFieldPower(OpCodeType.ocHardwareVariable, "force-field-power"), 
	hvForceFieldAngle(OpCodeType.ocHardwareVariable, "force-field-angle"), 
	hvForceFieldRadius(OpCodeType.ocHardwareVariable, "force-field-radius"),

	/******* Hardware vectors ***********/
	hvvWorldSize(OpCodeType.ocHardwareVector, "world-size"),
	//
	hvvPosition(OpCodeType.ocHardwareVector, "position"), 
	hvvVelocity(OpCodeType.ocHardwareVector, "velocity"),
	//
	hvvEngineVelocity(OpCodeType.ocHardwareVector, "engine-velocity"),
	// sensors
	hvvRobotSensorWhereFound(OpCodeType.ocHardwareVector, "robot-position"), 
	hvvRobotSensorWhereRelative(OpCodeType.ocHardwareVector, "robot-relative-position"), 
	hvvRobotSensorVelocityFound(OpCodeType.ocHardwareVector, "robot-velocity"), 
	hvvRobotSensorWhereOverall(OpCodeType.ocHardwareVector, "robot-position-overall"), 
	hvvFoodSensorWhereFound(OpCodeType.ocHardwareVector, "food-position"), 
	hvvFoodSensorWhereRelative(OpCodeType.ocHardwareVector, "food-relative-position"), 
	hvvFoodSensorVelocityFound(OpCodeType.ocHardwareVector, "food-velocity"), 
	hvvFoodSensorWhereOverall(OpCodeType.ocHardwareVector, "food-position-overall"), 
	hvvShotSensorWhereFound(OpCodeType.ocHardwareVector, "shot-position"), 
	hvvShotSensorWhereRelative(OpCodeType.ocHardwareVector, "shot-relative-position"), 
	hvvShotSensorVelocityFound(OpCodeType.ocHardwareVector, "shot-velocity"), 
	hvvShotSensorWhereOverall(OpCodeType.ocHardwareVector, "shot-position-overall"),

	/****************** Compile-time words ********************/
	cwNop(OpCodeType.ocCompileWord, "c-nop"), 
	cwIf(OpCodeType.ocCompileWord, "if"), 
	cwNotIf(OpCodeType.ocCompileWord, "nif"), 
	cwElse(OpCodeType.ocCompileWord, "else"), 
	cwThen(OpCodeType.ocCompileWord, "then"), 
	cwAndIf(OpCodeType.ocCompileWord, "and-if"), 
	cwNotAndIf(OpCodeType.ocCompileWord, "and-nif"), 
	cwCElse(OpCodeType.ocCompileWord, "celse"), 
	cwDo(OpCodeType.ocCompileWord, "do"), 
	cwLoop(OpCodeType.ocCompileWord, "loop"), 
	cwForever(OpCodeType.ocCompileWord, "forever"), 
	cwWhile(OpCodeType.ocCompileWord, "while"), 
	cwUntil(OpCodeType.ocCompileWord, "until"), 
	cwWhileLoop(OpCodeType.ocCompileWord, "while-loop"), 
	cwUntilLoop(OpCodeType.ocCompileWord, "until-loop")
	;
	/* @formatter:on */
	StackBrainOpcode(OpCodeType _type, String _name) {
		name = _name;
		type = _type;
		ID = this.ordinal();
	}

	static final Map<String, StackBrainOpcode> nameLookup = new HashMap<String, StackBrainOpcode>();
	static final Map<Integer, StackBrainOpcode> idLookup = new HashMap<Integer, StackBrainOpcode>();
	public static final Map<OpCodeType, Set<String>> opClass = new HashMap<OpCodeType, Set<String>>();
	private static int numPrimitives = 0;
	private static int numHardwareVariables = 0;
	private static int numHardwareVectors = 0;
	private static int numCWords = 0;

	static {
		for (OpCodeType typ : OpCodeType.values())
			opClass.put(typ, new HashSet<String>());
		for (StackBrainOpcode code : StackBrainOpcode.values()) {
			nameLookup.put(code.name.toLowerCase(), code);
			idLookup.put(code.ID, code);
			opClass.get(code.type).add(code.name);
		}
		numPrimitives = opClass.get(OpCodeType.ocPrimitive).size();
		numHardwareVariables = opClass.get(OpCodeType.ocHardwareVariable).size();
		numHardwareVectors = opClass.get(OpCodeType.ocHardwareVector).size();
		numCWords = opClass.get(OpCodeType.ocCompileWord).size();
	}

	public final OpCodeType type;
	public final int ID;

	public final static int kNumPrimitives() {
		return numPrimitives;
	}

	public final static int kNumHardwareVariables() {
		return numHardwareVariables;
	}

	public final static int kNumHardwareVectors() {
		return numHardwareVectors;
	}

	public final static int kNumCWords() {
		return numCWords;
	}

	public final String name;

	public final static StackBrainOpcode byName(String _name) {
		if (nameLookup.containsKey(_name.toLowerCase()))
			return nameLookup.get(_name.toLowerCase());
		else
			return null;
	}

	public final static StackBrainOpcode byID(int _ID) {
		return idLookup.get(_ID);
	}

	// layout: top 4 bits are opcode type, bottom 12 are index within type
	public static final int kOpcodeTypeShift = 12;
	public static final int kOpcodeIndexMask = 0x0FFF;

}
