/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

import sides.RobotType;
import simulation.GBConstructorState;
import simulation.GBMessage;
import simulation.GBRobot;
import simulation.GBSensorState;
import simulation.GBWorld;
import sound.SoundManager;
import support.FinePoint;
import support.GBMath;
import exception.GBAbort;
import exception.GBBrainError;
import exception.GBError;

//Clumsily trying to get around java's lack of custom opeators...
enum VectortoVectorOperator {
}

enum VectortoScalarOperator {
}

enum TwoVectortoVectorOperator {
	add, subtract, projection, rotateto, rotatefrom
}

enum TwoVectortoScalarOperator {
	cross, dot
}

enum TwoNumbertoNumberOperator {
	mod, rem
}

// NumbertoNumberOperators mostly covered by java.Math functions

public class GBStackBrain extends Brain {
	public static final int kStackLimit = 100;
	public static final int kReturnStackLimit = 50;
	double kProcessorUseCost = 0.0005;
	GBStackBrainSpec spec;
	// registers and other memory
	int pc;
	double[] variables;
	FinePoint[] vectorVariables;
	double[] stack;
	int stackHeight;
	int[] returnStack;
	int returnStackHeight;
	double[] memory;
	// status
	int remaining;
	int used;
	String lastPrint;

	public double readVariable(int index) {
		if (index < 0 || index >= spec.getVariablesCount())
			throw new GBBadSymbolIndexError();
		return variables[index];
	}

	public FinePoint readVectorVariable(int index) {
		if (index < 0 || index >= spec.getVectorVariablesCount())
			throw new GBBadSymbolIndexError();
		return vectorVariables[index];
	}

	void writeVariable(int index, double value) {
		if (index < 0 || index >= spec.getVariablesCount())
			throw new GBBadSymbolIndexError();
		variables[index] = value;
	}

	void writeVectorVariable(int index, FinePoint value) {
		if (index < 0 || index >= spec.getVectorVariablesCount())
			throw new GBBadSymbolIndexError();
		vectorVariables[index] = value;
	}

	void executeInstruction(int ins, GBRobot robot, GBWorld world) {
		int index = ins & StackBrainOpcode.kOpcodeIndexMask;
		switch (OpType.byID(ins >> StackBrainOpcode.kOpcodeTypeShift)) {
		case otPrimitive:
			executePrimitive(index, robot, world);
			break;
		case otConstantRead:
			push(spec.readConstant(index));
			break;
		case otVariableRead:
			push(readVariable(index));
			break;
		case otVariableWrite:
			writeVariable(index, pop());
			break;
		case otVectorRead:
			pushVector(readVectorVariable(index));
			break;
		case otVectorWrite:
			writeVectorVariable(index, popVector());
			break;
		case otLabelRead:
			push(spec.readLabel(index));
			break;
		case otLabelCall:
			executeCall(spec.readLabel(index));
			break;
		case otHardwareRead:
			push(executeHardwareRead(index, robot, world));
			break;
		case otHardwareWrite:
			executeHardwareWrite(index, pop(), robot, world);
			break;
		case otHardwareVectorRead:
			pushVector(executeHardwareVectorRead(index, robot, world));
			break;
		case otHardwareVectorWrite:
			executeHardwareVectorWrite(index, popVector(), robot, world);
			break;
		default:
			throw new GBUnknownInstructionError();
		}
	}

	void executeCall(int addr) {
		if (returnStackHeight >= kReturnStackLimit)
			throw new GBStackOverflowError();
		returnStack[returnStackHeight++] = pc;
		pc = addr;
	}

	void doPrint(String str) {
		lastPrint = str;
	}

	/*
	 * void NumberToNumberOp(double (*op)(doubleParam)) { if ( stackHeight < 1 )
	 * throw new GBStackUnderflowError(); stack[stackHeight - 1] =
	 * op(stack[stackHeight - 1]); }
	 */
	void twoNumberToNumberOp(TwoNumbertoNumberOperator op) {
		if (stackHeight < 2)
			throw new GBStackUnderflowError();
		--stackHeight;
		double x = stack[stackHeight - 1];
		double divisor = stack[stackHeight];
		switch (op) {
		/*
		 * GBNumber mod(GBNumberParam x, GBNumberParam divisor) { return x -
		 * floor(x / divisor) * divisor; } GBNumber rem(GBNumberParam x,
		 * GBNumberParam divisor) { return x - trunc(x / divisor) * divisor; }
		 */
		case mod:
			x = x - Math.floor(x / divisor) * divisor;
			break;
		case rem:
			x = x - (int) (x / divisor) * divisor;
			;
			break;
		default:
			break;
		}
		// double temp = op(stack[stackHeight - 1], stack[stackHeight]);
		stack[stackHeight - 1] = x;
	}

	/*
	 * void VectorToVectorOp(VectortoVectorOperator op) { FinePoint v =
	 * PopVector(); PushVector((v.*op)()); } /* void
	 * VectorToScalarOp(VectortoScalarOperator op) { FinePoint v = PopVector();
	 * Push((v.*op)()); }
	 */
	void twoVectorToVectorOp(TwoVectortoVectorOperator op) {
		FinePoint v2 = popVector();
		FinePoint v1 = popVector();
		FinePoint result = new FinePoint();
		switch (op) {
		case add:
			result = v1.add(v2);
			break;
		case projection:
			result = v1.projection(v2);
			break;
		case subtract:
			result = v1.subtract(v2);
			break;
		default:
			break;
		}
		pushVector(result);
	}

	void twoVectorToScalarOp(TwoVectortoScalarOperator op) {
		FinePoint v2 = popVector();
		FinePoint v1 = popVector();
		double result = 0;
		switch (op) {
		case cross:
			result = v1.cross(v2);
			break;
		case dot:
			result = v1.dotProduct(v2);
			break;
		default:
			break;
		}
		push(result);
	}

	void push(double value) {
		if (stackHeight >= kStackLimit)
			throw new GBStackOverflowError();
		stack[stackHeight++] = value;
	}

	double pop() {
		if (stackHeight <= 0)
			throw new GBStackUnderflowError();
		return stack[--stackHeight];
	}

	double peek(int delta) {
		if (delta < 1)
			throw new GBBrainError("peeking stack element " + delta);
		int where = stackHeight - delta;
		if (where < 0)
			throw new GBStackUnderflowError();
		if (where >= kStackLimit)
			throw new GBStackOverflowError();
		return stack[where];
	}

	void pushVector(FinePoint v) {
		if (stackHeight > kStackLimit - 2)
			throw new GBStackOverflowError();
		stack[stackHeight++] = v.x;
		stack[stackHeight++] = v.y;
	}

	FinePoint popVector() {
		if (stackHeight < 2)
			throw new GBStackUnderflowError();
		FinePoint v = new FinePoint(stack[stackHeight - 2],
				stack[stackHeight - 1]);
		stackHeight -= 2;
		return v;
	}

	int popInteger() {
		return toInteger(pop());
	}

	void pushboolean(boolean value) {
		push(fromBoolean(value));
	}

	void pushReturn(int value) {
		if (returnStackHeight >= kReturnStackLimit)
			throw new GBStackOverflowError();
		returnStack[returnStackHeight++] = value;
	}

	int popReturn() {
		if (returnStackHeight <= 0)
			throw new GBStackUnderflowError();
		return returnStack[--returnStackHeight];
	}

	double readLocalMemory(int addr, GBRobot robot) {
		if (addr < 1 || addr > robot.hardware.getMemory())
			throw new GBBrainError("tried to read at address " + addr);
		if (memory == null)
			return 0;
		return memory[addr - 1];
	}

	void writeLocalMemory(int addr, double val, GBRobot robot) {
		if (addr < 1 || addr > robot.hardware.getMemory())
			throw new GBBrainError("tried to write at address " + addr);
		if (memory == null) {
			if (robot.hardware.getMemory() == 0)
				return; // fail silently
			memory = new double[robot.hardware.getMemory()];
		}
		memory[addr - 1] = val;
	}

	int toAddress(double value) {
		int addr = (int) value;
		if (addr == value) {
			if (addr >= 0 && addr < spec.getInstructionsCount())
				return addr;
		} else
			throw new GBBadAddressError(value);
		return -1;
	}

	int toInteger(double value) {
		if (Math.floor(value) == value)
			return (int) Math.floor(value);
		else
			throw new GBNotIntegerError(value);
	}

	double fromBoolean(boolean value) {
		return value ? 1 : 0;
	}

	void raiseBrainError(Exception err, GBRobot robot, GBWorld world) {
		status = BrainStatus.bsError;
		if (world.reportErrors)
			// Handle brain errors at this level before they bubble up to the
			// rest of the world.
			// Choosing Abort will still throw an exception, but continue
			// will not.
			GBError.NonfatalError(robot.toString()
					+ " had error in brain, probably at "
					+ getAddressAndLine(pc - 1) + ": " + err.getMessage());
	}

	public GBStackBrain(GBStackBrainSpec spc) {
		spec = spc;
		pc = spc.getStartAddress();
		variables = new double[spc.variables.size()];
		vectorVariables = new FinePoint[spc.vectorVariables.size()];
		stack = new double[kStackLimit];
		returnStack = new int[kReturnStackLimit];
		memory = null;
		lastPrint = null;
		int i;
		for (i = 0; i < spc.getVariablesCount(); i++)
			variables[i] = spc.readVariable(i);
		for (i = 0; i < spc.getVectorVariablesCount(); i++)
			vectorVariables[i] = spc.readVectorVariable(i);
	}

	@Override
	public void think(GBRobot robot, GBWorld world) {
		try {
			while (status == BrainStatus.bsOK && remaining > 0)
				thinkOne(robot, world);
		} catch (GBBrainError err) {
			try {
				raiseBrainError(err, robot, world);
			} catch (GBAbort a) {
				status = BrainStatus.bsStopped;
			}
		}
		double en = kProcessorUseCost * used;
		robot.hardware.useEnergy(en);
		robot.getOwner().getScores().expenditure.reportBrain(en);
		used = 0;
		remaining = (remaining > 0 ? 0 : remaining)
				+ robot.hardware.getProcessor();
	}

	public void thinkOne(GBRobot robot, GBWorld world) {
		int ins = spec.readInstruction(pc++);
		remaining--; // currently all instructions take one cycle
		used++;
		executeInstruction(ins, robot, world);
	}

	@Override
	public void step(GBRobot robot, GBWorld world) {
		if (status != BrainStatus.bsOK)
			return;
		try {
			thinkOne(robot, world);
		} catch (GBBrainError err) {
			raiseBrainError(err, robot, world);
		} catch (GBAbort err) {
			status = BrainStatus.bsStopped;
		}
	}

	@Override
	public boolean ready() {
		return remaining > 0;
	}

	public int getRemaining() {
		return remaining;
	}

	public int getPC() {
		return pc;
	}

	public int getStackHeight() {
		return stackHeight;
	}

	public int getReturnStackHeight() {
		return returnStackHeight;
	}

	public double getStackAt(int index) {
		if (index < 0 || index >= stackHeight)
			throw new GBBrainError("tried to read stack element " + index);
		return stack[index];
	}

	public int getReturnStackAt(int index) {
		if (index < 0 || index >= returnStackHeight)
			throw new GBBrainError("tried to read return stack element "
					+ index);
		return returnStack[index];
	}

	public boolean isValidAddress(int addr) {
		return addr >= 0 && addr < spec.getInstructionsCount();
	}

	public String getAddressName(int addr) {
		return spec.getAddressName(addr);
	}

	public String getAddressDescription(int addr) {
		return spec.getAddressDescription(addr);
	}

	public String getAddressLastLabel(int addr) {
		GBLabel ret = spec.getAddressLastLabel(addr - 1);
		if (ret != null)
			return ret.name;
		else
			return "";
	}

	public String getAddressAndLine(int addr) {
		return spec.getAddressAndLine(addr);
	}

	public int getPCLine() {
		return spec.getLineNumber(pc);
	}

	public String getDisassembleAddress(int addr) {
		return spec.disassembleAddress(addr);
	}

	public String getLastPrint() {
		return lastPrint != null ? lastPrint : "none";
	}

	public int getNumVariables() {
		return spec.getVariablesCount();
	}

	public int getNumVectorVariables() {
		return spec.getVectorVariablesCount();
	}

	public String getVariableName(int index) {
		return spec.getVariableName(index);
	}

	public String getVectorVariableName(int index) {
		return spec.getVectorVariableName(index);
	}

	double executeHardwareRead(int index, GBRobot robot, GBWorld world) {
		switch (StackBrainOpcode.byID(index)) {
		// world
		case hvTime:
			return world.currentFrame;
		case hvTimeLimit:
			return world.getTimeLimit();
		case hvWorldWidth:
			return world.getSize().x;
		case hvWorldHeight:
			return world.getSize().y;
			// robot
		case hvRadius:
			return robot.getRadius();
		case hvMass:
			return robot.getMass();
		case hvSpeed:
			return robot.getSpeed();
		case hvProcessor:
			return robot.hardware.getProcessor();
		case hvRemaining:
			return remaining;
		case hvSideID:
			return robot.getOwner().getID();
		case hvTypeID:
			return robot.getRobotType().getID();
		case hvRobotID:
			return robot.getID();
		case hvParentID:
			return robot.getParentID();
		case hvPopulation:
			return robot.getOwner().getScores().getPopulation();
		case hvEnginePower:
			return robot.hardware.getEnginePower();
		case hvEngineMaxPower:
			return robot.hardware.getEngineMaxPower();
		case hvFlag:
			return robot.flag;
			// collisions
		case hvCollision:
			return robot.getCollisions();
		case hvFriendlyCollision:
			return robot.getFriendlyCollisions();
		case hvEnemyCollision:
			return robot.getEnemyCollisions();
		case hvFoodCollision:
			return robot.getFoodCollisions();
		case hvShotCollision:
			return robot.getShotCollisions();
		case hvWallCollision:
			return robot.getWallCollisions();
			// energy
		case hvEnergy:
			return robot.hardware.getEnergy();
		case hvMaxEnergy:
			return robot.hardware.getMaxEnergy();
		case hvSolarCells:
			return robot.hardware.getSolarCells();
		case hvEater:
			return robot.hardware.getEater();
		case hvEaten:
			return robot.hardware.getEaten();
		case hvSyphonMaxRate:
			return robot.hardware.syphon.getMaxRate();
		case hvSyphonRange:
			return robot.hardware.syphon.getMaxRange();
		case hvSyphonDistance:
			return robot.hardware.syphon.getDistance();
		case hvSyphonDirection:
			return robot.hardware.syphon.getDirection();
		case hvSyphonRate:
			return robot.hardware.syphon.getRate();
		case hvSyphoned:
			return robot.hardware.syphon.getSyphoned();
		case hvEnemySyphonMaxRate:
			return robot.hardware.enemySyphon.getMaxRate();
		case hvEnemySyphonRange:
			return robot.hardware.enemySyphon.getMaxRange();
		case hvEnemySyphonDistance:
			return robot.hardware.enemySyphon.getDistance();
		case hvEnemySyphonDirection:
			return robot.hardware.enemySyphon.getDirection();
		case hvEnemySyphonRate:
			return robot.hardware.enemySyphon.getRate();
		case hvEnemySyphoned:
			return robot.hardware.enemySyphon.getSyphoned();
			// constructor
		case hvConstructorMaxRate:
			return robot.hardware.constructor.getMaxRate();
		case hvConstructorRate:
			return robot.hardware.constructor.getRate();
		case hvConstructorProgress:
			return robot.hardware.constructor.getProgress();
		case hvConstructorRemaining:
			return robot.hardware.constructor.getRemaining();
		case hvConstructorType:
			return robot.getOwner()
					.getTypeIndex(robot.hardware.constructor.getRobotType());
		case hvChildID:
			return robot.hardware.constructor.getChildID();
			// robot sensor
		case hvRobotSensorRange:
			return robot.hardware.sensor1.getMaxRange();
		case hvRobotSensorFiringCost:
			return robot.hardware.sensor1.getFiringCost();
		case hvRobotSensorFocusDistance:
			return robot.hardware.sensor1.getDistance();
		case hvRobotSensorFocusDirection:
			return robot.hardware.sensor1.getDirection();
		case hvRobotSensorSeesFriends:
			return fromBoolean(robot.hardware.sensor1.getSeesFriendly());
		case hvRobotSensorSeesEnemies:
			return fromBoolean(robot.hardware.sensor1.getSeesEnemy());
		case hvRobotSensorTime:
			return robot.hardware.sensor1.getTime();
		case hvRobotSensorFound:
			return robot.hardware.sensor1.getFound();
		case hvRobotSensorAngleFound:
			return (robot.hardware.sensor1.getWhereFound().subtract(
					robot.getPosition()).angle()); // inconsistent: distance then
												// or now?
		case hvRobotSensorRangeFound:
			return (robot.hardware.sensor1.getWhereFound().subtract(
					robot.getPosition()).norm());
		case hvRobotSensorSideFound:
			return robot.hardware.sensor1.getSide();
		case hvRobotSensorRadiusFound:
			return robot.hardware.sensor1.getRadius();
		case hvRobotSensorMassFound:
			return robot.hardware.sensor1.getMass();
		case hvRobotSensorEnergyFound:
			return robot.hardware.sensor1.getEnergy();
		case hvRobotSensorTypeFound:
			return robot.hardware.sensor1.getFoundType();
		case hvRobotSensorIDFound:
			return robot.hardware.sensor1.getID();
		case hvRobotSensorShieldFractionFound:
			return robot.hardware.sensor1.getShieldFraction();
		case hvRobotSensorBombFound:
			return robot.hardware.sensor1.getBomb();
		case hvRobotSensorReloadingFound:
			return robot.hardware.sensor1.getReloading() ? 1 : 0;
		case hvRobotSensorFlagFound:
			return robot.hardware.sensor1.getFlag();
		case hvRobotSensorRangeOverall:
			return (robot.hardware.sensor1.getWhereOverall().subtract(
					robot.getPosition()).norm());
		case hvRobotSensorAngleOverall:
			return (robot.hardware.sensor1.getWhereOverall().subtract(
					robot.getPosition()).angle());
		case hvRobotSensorCurrentResult:
			return robot.hardware.sensor1.getCurrentResult();
		case hvRobotSensorNumResults:
			return robot.hardware.sensor1.getNumResults();
		case hvRobotSensorMaxResults:
			return robot.hardware.sensor1.getMaxResults();
			// food sensor
		case hvFoodSensorRange:
			return robot.hardware.sensor2.getMaxRange();
		case hvFoodSensorFiringCost:
			return robot.hardware.sensor2.getFiringCost();
		case hvFoodSensorFocusDistance:
			return robot.hardware.sensor2.getDistance();
		case hvFoodSensorFocusDirection:
			return robot.hardware.sensor2.getDirection();
		case hvFoodSensorTime:
			return robot.hardware.sensor2.getTime();
		case hvFoodSensorFound:
			return robot.hardware.sensor2.getFound();
		case hvFoodSensorAngleFound:
			return (robot.hardware.sensor2.getWhereFound().subtract(
					robot.getPosition()).angle());
		case hvFoodSensorRangeFound:
			return (robot.hardware.sensor2.getWhereFound().subtract(
					robot.getPosition()).norm());
		case hvFoodSensorSideFound:
			return robot.hardware.sensor2.getSide();
		case hvFoodSensorRadiusFound:
			return robot.hardware.sensor2.getRadius();
		case hvFoodSensorMassFound:
			return robot.hardware.sensor2.getMass();
		case hvFoodSensorEnergyFound:
			return robot.hardware.sensor2.getEnergy();
		case hvFoodSensorRangeOverall:
			return (robot.hardware.sensor2.getWhereOverall().subtract(
					robot.getPosition()).norm());
		case hvFoodSensorAngleOverall:
			return (robot.hardware.sensor2.getWhereOverall().subtract(
					robot.getPosition()).angle());
		case hvFoodSensorCurrentResult:
			return robot.hardware.sensor2.getCurrentResult();
		case hvFoodSensorNumResults:
			return robot.hardware.sensor2.getNumResults();
		case hvFoodSensorMaxResults:
			return robot.hardware.sensor2.getNumResults();
			// shot sensor
		case hvShotSensorRange:
			return robot.hardware.sensor3.getMaxRange();
		case hvShotSensorFiringCost:
			return robot.hardware.sensor3.getFiringCost();
		case hvShotSensorFocusDistance:
			return robot.hardware.sensor3.getDistance();
		case hvShotSensorFocusDirection:
			return robot.hardware.sensor3.getDirection();
		case hvShotSensorSeesFriendly:
			return fromBoolean(robot.hardware.sensor3.getSeesFriendly());
		case hvShotSensorSeesEnemy:
			return fromBoolean(robot.hardware.sensor3.getSeesEnemy());
		case hvShotSensorTime:
			return robot.hardware.sensor3.getTime();
		case hvShotSensorFound:
			return robot.hardware.sensor3.getFound();
		case hvShotSensorAngleFound:
			return (robot.hardware.sensor3.getWhereFound().subtract(
					robot.getPosition()).angle());
		case hvShotSensorRangeFound:
			return (robot.hardware.sensor3.getWhereFound().subtract(
					robot.getPosition()).norm());
		case hvShotSensorSideFound:
			return robot.hardware.sensor3.getSide();
		case hvShotSensorRadiusFound:
			return robot.hardware.sensor3.getRadius();
		case hvShotSensorPowerFound:
			return robot.hardware.sensor3.getEnergy();
		case hvShotSensorTypeFound:
			return robot.hardware.sensor3.getFoundType();
		case hvShotSensorRangeOverall:
			return (robot.hardware.sensor3.getWhereOverall().subtract(
					robot.getPosition()).norm());
		case hvShotSensorAngleOverall:
			return (robot.hardware.sensor3.getWhereOverall().subtract(
					robot.getPosition()).angle());
		case hvShotSensorCurrentResult:
			return robot.hardware.sensor3.getCurrentResult();
		case hvShotSensorNumResults:
			return robot.hardware.sensor3.getNumResults();
		case hvShotSensorMaxResults:
			return robot.hardware.sensor3.getNumResults();
			// defenses
		case hvArmor:
			return robot.hardware.getArmor();
		case hvMaxArmor:
			return robot.hardware.getMaxArmor();
		case hvRepairRate:
			return robot.hardware.getRepairRate();
		case hvMaxRepairRate:
			return robot.hardware.getMaxRepairRate();
		case hvShield:
			return robot.hardware.getShield();
		case hvMaxShield:
			return robot.hardware.getMaxShield();
		case hvShieldFraction:
			return robot.getShieldFraction();
		case hvLastHit:
			return robot.getLastHit() != null ? robot.getLastHit().getID() : 0;
			// blaster
		case hvBlasterDamage:
			return robot.hardware.blaster.getDamage();
		case hvBlasterRange:
			return robot.hardware.blaster.getMaxRange();
		case hvBlasterSpeed:
			return robot.hardware.blaster.getSpeed();
		case hvBlasterLifetime:
			return robot.hardware.blaster.getMaxLifetime();
		case hvBlasterReloadTime:
			return robot.hardware.blaster.getReloadTime();
		case hvBlasterFiringCost:
			return robot.hardware.blaster.getFiringCost();
		case hvBlasterCooldown:
			return robot.hardware.blaster.getCooldown();
			// grenades
		case hvGrenadesDamage:
			return robot.hardware.grenades.getDamage();
		case hvGrenadesSpeed:
			return robot.hardware.grenades.getSpeed();
		case hvGrenadesLifetime:
			return robot.hardware.grenades.getMaxLifetime();
		case hvGrenadesRange:
			return robot.hardware.grenades.getMaxRange();
		case hvGrenadesReloadTime:
			return robot.hardware.grenades.getReloadTime();
		case hvGrenadesFiringCost:
			return robot.hardware.grenades.getFiringCost();
		case hvGrenadesCooldown:
			return robot.hardware.grenades.getCooldown();
		case hvGrenadesRadius:
			return robot.hardware.grenades.getExplosionRadius();
			// forcefield
		case hvForceFieldMaxPower:
			return robot.hardware.forceField.getMaxPower();
		case hvForceFieldRange:
			return robot.hardware.forceField.getMaxRange();
		case hvForceFieldDistance:
			return robot.hardware.forceField.getDistance();
		case hvForceFieldDirection:
			return robot.hardware.forceField.getDirection();
		case hvForceFieldPower:
			return robot.hardware.forceField.getPower();
		case hvForceFieldAngle:
			return robot.hardware.forceField.getAngle();
		case hvForceFieldRadius:
			return robot.hardware.forceField.getRadius();
			//
		default:
			throw new GBUnknownHardwareVariableError();
		}
	}

	void executeHardwareWrite(int index, double value, GBRobot robot, GBWorld world) {
		switch (StackBrainOpcode.byID(index)) {
		case hvTime:
		case hvTimeLimit:
		case hvWorldWidth:
		case hvWorldHeight:
		case hvRadius:
		case hvMass:
		case hvSpeed:
		case hvProcessor:
		case hvRemaining:
		case hvSideID:
		case hvTypeID:
		case hvRobotID:
		case hvParentID:
		case hvPopulation:
			throw new GBReadOnlyError();
		case hvEnginePower:
			robot.hardware.setEnginePower(value);
			break;
		case hvFlag:
			robot.flag = value;
			break;
		case hvEngineMaxPower:
		case hvCollision:
		case hvFriendlyCollision:
		case hvEnemyCollision:
		case hvFoodCollision:
		case hvShotCollision:
		case hvWallCollision:
			throw new GBReadOnlyError();
			// energy
		case hvEnergy:
		case hvMaxEnergy:
		case hvSolarCells:
		case hvEater:
		case hvEaten:
		case hvSyphonMaxRate:
		case hvSyphonRange:
		case hvSyphoned:
			throw new GBReadOnlyError();
		case hvSyphonDistance:
			robot.hardware.syphon.setDistance(value);
			break;
		case hvSyphonDirection:
			robot.hardware.syphon.setDirection(value);
			break;
		case hvSyphonRate:
			robot.hardware.syphon.setRate(value);
			break;
		case hvEnemySyphonMaxRate:
		case hvEnemySyphonRange:
		case hvEnemySyphoned:
			throw new GBReadOnlyError();
		case hvEnemySyphonDistance:
			robot.hardware.enemySyphon.setDistance(value);
			break;
		case hvEnemySyphonDirection:
			robot.hardware.enemySyphon.setDirection(value);
			break;
		case hvEnemySyphonRate:
			robot.hardware.enemySyphon.setRate(value);
			break;
		// constructor
		case hvConstructorType: {
			int tmpindex = toInteger(value);
			robot.hardware.constructor.start(tmpindex != 0 ? robot.getOwner()
					.getRobotType(tmpindex) : null, 0);
		}
			break;
		case hvConstructorRate:
			robot.hardware.constructor.setRate(value);
			break;
		case hvConstructorMaxRate:
		case hvConstructorProgress:
		case hvConstructorRemaining:
		case hvChildID:
			throw new GBReadOnlyError();
			// robot sensor
		case hvRobotSensorRange:
		case hvRobotSensorFiringCost:
			throw new GBReadOnlyError();
		case hvRobotSensorFocusDistance:
			robot.hardware.sensor1.setDistance(value);
			break;
		case hvRobotSensorFocusDirection:
			robot.hardware.sensor1.setDirection(value);
			break;
		case hvRobotSensorSeesFriends:
			robot.hardware.sensor1.setSeesFriendly(value != 0);
			break;
		case hvRobotSensorSeesEnemies:
			robot.hardware.sensor1.setSeesEnemy(value != 0);
			break;
		case hvRobotSensorTime:
		case hvRobotSensorFound:
		case hvRobotSensorRangeFound:
		case hvRobotSensorAngleFound:
		case hvRobotSensorSideFound:
		case hvRobotSensorRadiusFound:
		case hvRobotSensorMassFound:
		case hvRobotSensorEnergyFound:
		case hvRobotSensorTypeFound:
		case hvRobotSensorIDFound:
		case hvRobotSensorShieldFractionFound:
		case hvRobotSensorBombFound:
		case hvRobotSensorReloadingFound:
		case hvRobotSensorRangeOverall:
		case hvRobotSensorAngleOverall:
			throw new GBReadOnlyError();
		case hvRobotSensorCurrentResult:
			robot.hardware.sensor1.getSetCurrentResult(toInteger(value));
			break;
		case hvRobotSensorNumResults:
		case hvRobotSensorMaxResults:
			throw new GBReadOnlyError();
			// food sensor
		case hvFoodSensorRange:
		case hvFoodSensorFiringCost:
			throw new GBReadOnlyError();
		case hvFoodSensorFocusDistance:
			robot.hardware.sensor2.setDistance(value);
			break;
		case hvFoodSensorFocusDirection:
			robot.hardware.sensor2.setDirection(value);
			break;
		case hvFoodSensorTime:
		case hvFoodSensorFound:
		case hvFoodSensorRangeFound:
		case hvFoodSensorAngleFound:
		case hvFoodSensorSideFound:
		case hvFoodSensorRadiusFound:
		case hvFoodSensorMassFound:
		case hvFoodSensorEnergyFound:
		case hvFoodSensorRangeOverall:
		case hvFoodSensorAngleOverall:
			throw new GBReadOnlyError();
		case hvFoodSensorCurrentResult:
			robot.hardware.sensor2.getSetCurrentResult(toInteger(value));
			break;
		case hvFoodSensorNumResults:
		case hvFoodSensorMaxResults:
			throw new GBReadOnlyError();
			// shot sensor
		case hvShotSensorRange:
		case hvShotSensorFiringCost:
			throw new GBReadOnlyError();
		case hvShotSensorFocusDistance:
			robot.hardware.sensor3.setDistance(value);
			break;
		case hvShotSensorFocusDirection:
			robot.hardware.sensor3.setDirection(value);
			break;
		case hvShotSensorSeesFriendly:
			robot.hardware.sensor3.setSeesFriendly(value != 0);
			break;
		case hvShotSensorSeesEnemy:
			robot.hardware.sensor3.setSeesEnemy(value != 0);
			break;
		case hvShotSensorTime:
		case hvShotSensorFound:
		case hvShotSensorRangeFound:
		case hvShotSensorAngleFound:
		case hvShotSensorSideFound:
		case hvShotSensorRadiusFound:
		case hvShotSensorPowerFound:
		case hvShotSensorRangeOverall:
		case hvShotSensorAngleOverall:
			throw new GBReadOnlyError();
		case hvShotSensorCurrentResult:
			robot.hardware.sensor3.getSetCurrentResult(toInteger(value));
			break;
		case hvShotSensorNumResults:
		case hvShotSensorMaxResults:
			throw new GBReadOnlyError();
			// defenses
		case hvRepairRate:
			robot.hardware.setRepairRate(value);
			break;
		case hvShield:
			robot.hardware.setShield(value);
			break;
		case hvArmor:
		case hvMaxArmor:
		case hvMaxRepairRate:
		case hvMaxShield:
		case hvShieldFraction:
			throw new GBReadOnlyError();
			// weapons
		case hvBlasterDamage:
		case hvBlasterRange:
		case hvBlasterSpeed:
		case hvBlasterLifetime:
		case hvBlasterReloadTime:
		case hvBlasterFiringCost:
		case hvBlasterCooldown:
		case hvGrenadesDamage:
		case hvGrenadesRange:
		case hvGrenadesSpeed:
		case hvGrenadesLifetime:
		case hvGrenadesReloadTime:
		case hvGrenadesFiringCost:
		case hvGrenadesCooldown:
		case hvGrenadesRadius:
		case hvForceFieldMaxPower:
		case hvForceFieldRange:
			throw new GBReadOnlyError();
		case hvForceFieldDistance:
			robot.hardware.forceField.getSetDistance(value);
			break;
		case hvForceFieldDirection:
			robot.hardware.forceField.getSetDirection(value);
			break;
		case hvForceFieldPower:
			robot.hardware.forceField.getSetPower(value);
			break;
		case hvForceFieldAngle:
			robot.hardware.forceField.setAngle(value);
			break;
		case hvForceFieldRadius:
			throw new GBReadOnlyError();
			//
		default:
			throw new GBUnknownHardwareVariableError();
		}
	}

	FinePoint executeHardwareVectorRead(int index, GBRobot robot, GBWorld world) {
		switch (StackBrainOpcode.byID(index)) {
		case hvvWorldSize:
			return world.getSize();
			//
		case hvvPosition:
			return robot.getPosition();
		case hvvVelocity:
			return robot.getVelocity();
			//
		case hvvEngineVelocity:
			return robot.hardware.getEngineVelocity();
			// sensors
		case hvvRobotSensorWhereFound:
			return robot.hardware.sensor1.getWhereFound();
		case hvvRobotSensorWhereRelative:
			return robot.hardware.sensor1.getWhereFound().subtract(
					robot.getPosition());
		case hvvRobotSensorVelocityFound:
			return robot.hardware.sensor1.getVelocity();
		case hvvRobotSensorWhereOverall:
			return robot.hardware.sensor1.getWhereOverall();
		case hvvFoodSensorWhereFound:
			return robot.hardware.sensor2.getWhereFound();
		case hvvFoodSensorWhereRelative:
			return robot.hardware.sensor2.getWhereFound().subtract(
					robot.getPosition());
		case hvvFoodSensorVelocityFound:
			return robot.hardware.sensor2.getVelocity();
		case hvvFoodSensorWhereOverall:
			return robot.hardware.sensor2.getWhereOverall();
		case hvvShotSensorWhereFound:
			return robot.hardware.sensor3.getWhereFound();
		case hvvShotSensorWhereRelative:
			return robot.hardware.sensor3.getWhereFound().subtract(
					robot.getPosition());
		case hvvShotSensorVelocityFound:
			return robot.hardware.sensor3.getVelocity();
		case hvvShotSensorWhereOverall:
			return robot.hardware.sensor3.getWhereOverall();
			//
		default:
			throw new GBUnknownHardwareVariableError();
		}
	}

	void executeHardwareVectorWrite(int index, FinePoint value, GBRobot robot,
			GBWorld world) {
		switch (StackBrainOpcode.byID(index)) {
		case hvvWorldSize:
		case hvvPosition:
		case hvvVelocity:
			throw new GBReadOnlyError();
		case hvvEngineVelocity:
			robot.hardware.setEngineVelocity(value);
			break;
		// sensors
		case hvvRobotSensorWhereFound:
		case hvvRobotSensorVelocityFound:
		case hvvRobotSensorWhereOverall:
		case hvvFoodSensorWhereFound:
		case hvvFoodSensorVelocityFound:
		case hvvFoodSensorWhereOverall:
		case hvvShotSensorWhereFound:
		case hvvShotSensorVelocityFound:
		case hvvShotSensorWhereOverall:
			throw new GBReadOnlyError();
			//
		default:
			throw new GBUnknownHardwareVariableError();
		}
	}

	void executePrimitive(int index, GBRobot robot, GBWorld world) {
		double temp, temp2, temp3;
		int tempInt;
		switch (StackBrainOpcode.byID(index)) {
		case opNop:
			break;
		// stack manipulation
		case opDrop:
			pop();
			break;
		case op2Drop:
			pop();
			pop();
			break;
		case opNip:
			temp = pop();
			pop();
			push(temp);
			break;
		case opRDrop:
			popReturn();
			break;
		case opDropN: {
			int n = popInteger();
			if (n > stackHeight)
				throw new GBBrainError("dropped " + n + " of " + stackHeight);
			stackHeight -= n;
		}
			break;
		case opSwap:
			temp = pop();
			temp2 = pop();
			push(temp);
			push(temp2);
			break;
		case op2Swap: {
			FinePoint v1 = popVector();
			FinePoint v2 = popVector();
			pushVector(v1);
			pushVector(v2);
		}
			break;
		case opRotate:
			temp = pop();
			temp2 = pop();
			temp3 = pop();
			push(temp2);
			push(temp);
			push(temp3);
			break;
		case opReverseRotate:
			temp = pop();
			temp2 = pop();
			temp3 = pop();
			push(temp);
			push(temp3);
			push(temp2);
			break;
		case opDup:
			temp = peek(1);
			push(temp);
			break;
		case op2Dup:
			temp = peek(2);
			temp2 = peek(1);
			push(temp);
			push(temp2);
			break;
		case opTuck:
			temp = pop();
			temp2 = pop();
			push(temp);
			push(temp2);
			push(temp);
			break;
		case opOver:
			temp = peek(2);
			push(temp);
			break;
		case op2Over:
			temp = peek(4);
			temp2 = peek(3);
			push(temp);
			push(temp2);
			break;
		case opStackHeight:
			push(stackHeight);
			break;
		case opStackLimit:
			push(kStackLimit);
			break;
		case opPick:
			push(peek(popInteger()));
			break;
		case opToReturn:
			pushReturn(toAddress(pop()));
			break;
		case opFromReturn:
			push(popReturn());
			break;
		// branches
		case opJump:
			pc = toAddress(pop());
			break;
		case opCall:
			executeCall(toAddress(pop()));
			break;
		case opReturn:
			pc = popReturn();
			break;
		case opIfGo:
			temp = pop();
			if (pop() != 0)
				pc = toAddress(temp);
			break;
		case opIfElseGo:
			temp = pop();
			temp2 = pop();
			if (pop() != 0)
				pc = toAddress(temp2);
			else
				pc = toAddress(temp);
			break;
		case opIfCall:
			temp = pop();
			if (pop() != 0)
				executeCall(toAddress(temp));
			break;
		case opIfElseCall:
			temp = pop();
			temp2 = pop();
			if (pop() != 0)
				executeCall(toAddress(temp2));
			else
				executeCall(toAddress(temp));
			break;
		case opIfReturn:
			if (pop() != 0)
				pc = popReturn();
			break;
		case opNotIfGo:
			temp = pop();
			if (pop() == 0)
				pc = toAddress(temp);
			break;
		case opNotIfReturn:
			if (pop() == 0)
				pc = popReturn();
			break;
		case opNotIfCall:
			temp = pop();
			if (pop() == 0)
				executeCall(toAddress(temp));
			break;
		// arithmetic
		case opAdd:
			temp = pop();
			push(pop() + temp);
			break;
		case opSubtract:
			temp = pop();
			push(pop() - temp);
			break;
		case opNegate:
			push(-pop());
			break;
		case opMultiply:
			temp = pop();
			push(pop() * temp);
			break;
		case opDivide:
			temp = pop();
			push(pop() / temp);
			break;
		case opReciprocal:
			push(1 / pop());
			break;
		case opMod:
			twoNumberToNumberOp(TwoNumbertoNumberOperator.mod);
			break;
		case opRem:
			twoNumberToNumberOp(TwoNumbertoNumberOperator.rem);
			break;
		case opSquare:
			push(Math.pow(pop(), 2));
			break;
		case opSqrt:
			push(Math.sqrt(pop()));
			break;
		case opExponent:
			temp = pop();
			push(Math.pow(pop(), temp));
			break;
		case opIsInteger:
			temp = pop();
			pushboolean(temp == Math.floor(temp));
			break;
		case opFloor:
			push(Math.floor(pop()));
			break;
		case opCeiling:
			push(Math.ceil(pop()));
			break;
		case opRound:
			push(Math.round(pop()));
			break;
		case opMin:
			push(Math.min(pop(), pop()));
			break;
		case opMax:
			push(Math.max(pop(), pop()));
			break;
		case opAbs:
			push(Math.abs(pop()));
			break;
		case opSignum:
			push(Math.signum(pop()));
			break;
		case opReorient:
			push(GBMath.reorient(pop()));
			break;
		case opSine:
			push(Math.sin(pop()));
			break;
		case opCosine:
			push(Math.cos(pop()));
			break;
		case opTangent:
			push(Math.tan(pop()));
			break;
		case opArcSine:
			push(Math.asin(pop()));
			break;
		case opArcCosine:
			push(Math.acos(pop()));
			break;
		case opArcTangent:
			push(Math.atan(pop()));
			break;
		case opRandom:
			temp = pop();
			push(world.random.InRange(pop(), temp));
			break;
		case opRandomAngle:
			push(world.random.Angle());
			break;
		case opRandomInt:
			temp = pop();
			push(world.random.intInRange((int) Math.ceil(pop()),
					(int) Math.floor(temp)));
			break;
		case opRandomBoolean:
			pushboolean(world.random.bool(pop()));
			break;
		// constants
		case opPi:
			push(GBMath.kPi);
			break;
		case op2Pi:
			push(GBMath.kPi * 2);
			break;
		case opPiOver2:
			push(GBMath.kPi / 2);
			break;
		case opE:
			push(GBMath.kE);
			break;
		case opEpsilon:
			push(GBMath.kEpsilon);
			break;
		case opInfinity:
			push(GBMath.kInfinity);
			break;
		// vector operations
		case opRectToPolar: {
			FinePoint v = popVector();
			push(v.norm());
			push(v.angle());
		}
			break;
		case opPolarToRect:
			temp = pop();
			temp2 = pop();
			pushVector(FinePoint.makePolar(temp2, temp));
			break;
		case opVectorAdd:
			twoVectorToVectorOp(TwoVectortoVectorOperator.add);
			break;
		case opVectorSubtract:
			twoVectorToVectorOp(TwoVectortoVectorOperator.subtract);
			break;
		case opVectorNegate:
			pushVector(popVector().negate());
			break;
		case opVectorScalarMultiply:
			temp = pop();
			pushVector(popVector().multiply(temp));
			break;
		case opVectorScalarDivide:
			temp = pop();
			pushVector(popVector().divide(temp));
			break;
		case opVectorNorm:
			push(popVector().norm());
			break;
		case opVectorAngle:
			push(popVector().angle());
			break;
		case opDotProduct:
			twoVectorToScalarOp(TwoVectortoScalarOperator.dot);
			break;
		case opProject:
			twoVectorToVectorOp(TwoVectortoVectorOperator.projection);
			break;
		case opCross:
			twoVectorToScalarOp(TwoVectortoScalarOperator.cross);
			break;
		case opUnitize:
			pushVector(popVector().unit());
			break;
		case opVectorRotateTo:
			twoVectorToVectorOp(TwoVectortoVectorOperator.rotateto);
			break;
		case opVectorRotateFrom:
			twoVectorToVectorOp(TwoVectortoVectorOperator.rotatefrom);
			break;
		case opDistance:
			push(popVector().subtract(popVector()).norm());
			break;
		case opInRange:
			temp = pop();
			pushboolean(popVector().inRange(popVector(), temp));
			break;
		case opRestrictPosition: {
			temp = pop(); // wall distance
			FinePoint pos = popVector();
			push(GBMath.clamp(pos.x, temp, world.getSize().x - temp));
			push(GBMath.clamp(pos.y, temp, world.getSize().y - temp));
		}
			break;
		case opVectorEqual:
			pushboolean(popVector() == popVector());
			break;
		case opVectorNotEqual:
			pushboolean(popVector() != popVector());
			break;
		// comparisons
		case opEqual:
			pushboolean(pop() == pop());
			break;
		case opNotEqual:
			pushboolean(pop() != pop());
			break;
		case opLessThan:
			temp = pop();
			pushboolean(pop() < temp);
			break;
		case opLessThanOrEqual:
			temp = pop();
			pushboolean(pop() <= temp);
			break;
		case opGreaterThan:
			temp = pop();
			pushboolean(pop() > temp);
			break;
		case opGreaterThanOrEqual:
			temp = pop();
			pushboolean(pop() >= temp);
			break;
		// booleans
		case opNot:
			pushboolean(pop() == 0);
			break;
		case opAnd:
			temp = pop();
			temp2 = pop();
			pushboolean(temp != 0 && temp2 != 0);
			break;
		case opOr:
			temp = pop();
			temp2 = pop();
			pushboolean(temp != 0 || temp2 != 0);
			break;
		case opXor:
			temp = pop();
			temp2 = pop();
			pushboolean(temp != 0 && temp2 == 0 || temp == 0 && temp2 != 0);
			break;
		case opNand:
			temp = pop();
			temp2 = pop();
			pushboolean(!(temp != 0 && temp2 != 0));
			break;
		case opNor:
			temp = pop();
			temp2 = pop();
			pushboolean(!(temp != 0 || temp2 != 0));
			break;
		case opValueConditional:
			temp = pop();
			temp2 = pop();
			if (pop() != 0)
				push(temp2);
			else
				push(temp);
			break;
		// misc external
		case opPrint:
			doPrint(Double.toString(pop()));
			if (world.reportPrints)
				GBError.NonfatalError(robot.toString() + " prints: "
						+ lastPrint);
			break;
		case opPrintVector:
			doPrint(popVector().toString());
			if (world.reportPrints)
				GBError.NonfatalError(robot.toString() + " prints: "
						+ lastPrint);
			break;
		case opBeep: 
			SoundManager.playSound(SoundManager.SoundType.stBeep);
			break;
		case opStop:
			status = BrainStatus.bsStopped;
			break;
		case opPause:
			if (world.reportErrors)
				world.pause();
			break;
		case opSync:
			remaining = 0;
			break;
		// basic hardware
		case opSeekLocation:
			robot.doEngineSeek(popVector(), new FinePoint(0, 0));
			break;
		case opSeekMovingLocation: {
			FinePoint vel = popVector();
			robot.doEngineSeek(popVector(), vel);
		}
			break;
		case opDie:
			robot.die(robot.getOwner());
			status = BrainStatus.bsStopped;
			break;
		case opWriteLocalMemory:
			tempInt = popInteger();
			writeLocalMemory(tempInt, pop(), robot);
			break;
		case opReadLocalMemory:
			tempInt = popInteger();
			push(readLocalMemory(tempInt, robot));
			break;
		case opWriteLocalVector:
			tempInt = popInteger();
			writeLocalMemory(tempInt + 1, pop(), robot);
			writeLocalMemory(tempInt, pop(), robot);
			break;
		case opReadLocalVector:
			tempInt = popInteger();
			push(readLocalMemory(tempInt, robot));
			push(readLocalMemory(tempInt + 1, robot));
			break;
		case opWriteSharedMemory:
			tempInt = popInteger();
			robot.hardware.radio.write(pop(), tempInt, robot.getOwner());
			break;
		case opReadSharedMemory:
			push(robot.hardware.radio.read(popInteger(), robot.getOwner()));
			break;
		case opWriteSharedVector:
			tempInt = popInteger();
			robot.hardware.radio.write(pop(), tempInt + 1, robot.getOwner());
			robot.hardware.radio.write(pop(), tempInt, robot.getOwner());
			break;
		case opReadSharedVector:
			tempInt = popInteger();
			push(robot.hardware.radio.read(tempInt, robot.getOwner()));
			push(robot.hardware.radio.read(tempInt + 1, robot.getOwner()));
			break;
		case opMessagesWaiting:
			push(robot.hardware.radio.getMessagesWaiting(popInteger(),
					robot.getOwner()));
			break;
		case opSendMessage: {
			GBMessage sendee = new GBMessage();
			tempInt = popInteger(); // channel
			int numArgs = toInteger(pop()); // number of numbers
			for (int i = 0; i < numArgs; i++) {
				sendee.addDatum(pop()); // higher indices in message correspond
										// with earlier numbers in stack. :(
			}
			if (numArgs <= 0)
				throw new GBBrainError(
						"Cannot send message of non-positive length");
			robot.hardware.radio.send(sendee, tempInt, robot.getOwner());
		}
			break;
		case opReceiveMessage: {
			tempInt = popInteger();
			GBMessage received = robot.hardware.radio.receive(tempInt,
					robot.getOwner());
			if (received == null) {
				push(0);
			} else {
				if (received.length <= 0) {
					throw new GBBrainError(
							"non-positive length message received");
				}
				for (int i = received.length - 1; i >= 0; i--)
					push(received.getDatum(i));
				push(received.length);
			}
		}
			break;
		case opClearMessages:
			robot.hardware.radio.clearChannel(popInteger(), robot.getOwner());
			break;
		case opSkipMessages:
			tempInt = popInteger();
			robot.hardware.radio.skipMessages(tempInt, popInteger(),
					robot.getOwner());
			break;
		case opTypePopulation: {
			RobotType theType = robot.getOwner().getRobotType(popInteger());
			if (theType != null)
				push(theType.population);
			else
				push(-1);
		}
			break;
		case opAutoConstruct: {
			GBConstructorState ctor = robot.hardware.constructor;
			if (robot.getEnergy() > robot.hardware.getMaxEnergy() * .9) {
				if (ctor.getRobotType() == null)
					ctor.start(robot.getRobotType(), 0);
				ctor.setRate(ctor.getMaxRate());
			} else
				ctor.setRate(ctor.getRobotType() != null
						&& robot.getEnergy() > ctor.getRemaining() + 10 ? ctor
						.getMaxRate() : 0);
		}
			break;
		case opBalanceTypes: { // frac type --
			RobotType theType = robot.getOwner().getRobotType(popInteger());
			double fraction = pop();
			if (theType != null
					&& theType.population < fraction
							* robot.getOwner().getScores().getPopulation())
				robot.hardware.constructor.start(theType, 0); // FIXME don't
																// abort?
		}
			break;
		// sensors
		case opFireRobotSensor:
			robot.hardware.sensor1.fire();
			break;
		case opFireFoodSensor:
			robot.hardware.sensor2.fire();
			break;
		case opFireShotSensor:
			robot.hardware.sensor3.fire();
			break;
		case opRobotSensorNext:
			push(robot.hardware.sensor1.getNextResult() ? 1 : 0);
			break;
		case opFoodSensorNext:
			push(robot.hardware.sensor2.getNextResult() ? 1 : 0);
			break;
		case opShotSensorNext:
			push(robot.hardware.sensor3.getNextResult() ? 1 : 0);
			break;
		case opPeriodicRobotSensor:
			firePeriodic(robot.hardware.sensor1, world);
			break;
		case opPeriodicFoodSensor:
			firePeriodic(robot.hardware.sensor2, world);
			break;
		case opPeriodicShotSensor:
			firePeriodic(robot.hardware.sensor3, world);
			break;
		// weapons
		case opFireBlaster:
			robot.hardware.blaster.fire(pop());
			break;
		case opFireGrenade:
			temp = pop();
			robot.hardware.grenades.fire(pop(), temp);
			break;
		case opLeadBlaster: { // pos vel --
			FinePoint vel = popVector().subtract(robot.getVelocity());
			FinePoint pos = popVector().subtract(robot.getPosition());
			FinePoint target = leadShot(pos, vel,
					robot.hardware.blaster.getSpeed(), robot.getRadius());
			if (target.isNonzero()
					&& target.norm() <= robot.hardware.blaster.getMaxRange()
							+ robot.getRadius())
				robot.hardware.blaster.fire(target.angle());
		}
			break;
		case opLeadGrenade: { // pos vel --
			FinePoint vel = popVector().subtract(robot.getVelocity());
			FinePoint pos = popVector().subtract(robot.getPosition());
			FinePoint target = leadShot(pos, vel,
					robot.hardware.grenades.getSpeed(), robot.getRadius());
			if (target.isNonzero()
					&& target.norm() <= robot.hardware.grenades.getMaxRange()
							+ robot.getRadius())
				robot.hardware.grenades.fire(target.norm(), target.angle()); // worry
																				// about
																				// short
																				// range?
		}
			break;
		case opSetForceField: { // pos angle --
			temp = pop();
			FinePoint pos = popVector().subtract(robot.getPosition());
			robot.hardware.forceField.getSetDistance(pos.norm());
			robot.hardware.forceField.getSetDirection(pos.angle());
			robot.hardware.forceField.setAngle(temp);
			robot.hardware.forceField.getSetPower(robot.hardware.forceField
					.getMaxPower());
		}
			break;
		// otherwise...
		case opEnd:
			throw new GBOffEndError(0);// TODO look up line number
		default:
			throw new GBUnknownInstructionError();
		}
	}

	// pos and vel are relative to ourself
	static FinePoint leadShot(FinePoint pos, FinePoint vel, double shotSpeed,
			double r) {
		// if (true) {
		double dt = (pos.add(vel.multiply(pos.norm() / shotSpeed))).norm()
				/ shotSpeed; // two plies for accuracy with radially moving
								// targets
		return pos.add(vel.multiply(dt));
		/*
		 * } else { // Precise version, not used yet because it changes
		 * behavior. // Solve for exact time of impact: (pos + vel * dt).Norm()
		 * = // shotSpeed * dt + r double a = vel.normSquare() - shotSpeed *
		 * shotSpeed; double b = (pos.dotProduct(vel) - shotSpeed * r) * 2;
		 * double c = pos.normSquare() - r * r; double det = b * b - a * c * 4;
		 * if (det < 0 || a == 0) return new FinePoint(0, 0); // don't shoot //
		 * try both roots and use least positive root double dt1 = b * -1 -
		 * Math.sqrt(det) / (a * 2); double dt2 = b * -1 + Math.sqrt(det) / (a *
		 * 2); if (dt1 < 0) { if (dt2 < 0) return new FinePoint(0, 0); else
		 * return pos.add(vel.multiply(dt2)); } else if (dt2 < 0 || dt1 < dt2)
		 * return pos.add(vel.multiply(dt1)); else return
		 * pos.add(vel.multiply(dt2)); }
		 */
	}

	void firePeriodic(GBSensorState sensor, GBWorld world) {
		int period = popInteger();
		if (world.currentFrame >= sensor.getTime() + period || sensor.getTime() <= 0) {
			sensor.fire();
			remaining = 0;
			pushboolean(true);
		} else
			pushboolean(false);
	}

};

// errors //

class GBOffEndError extends GBBadAddressError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GBOffEndError(double addr) {
		super(addr);
	}

	@Override
	public String toString() {
		return "fell off end of code";
	}
}
