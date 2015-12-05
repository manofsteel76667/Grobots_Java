/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/

package simulation;

import sides.HardwareSpec;
import support.FinePoint;
import support.GBMath;
import exception.GBSimulationError;

public class GBHardwareState {
	HardwareSpec spec;
	// private: // simple hardware
	double enginePower;
	FinePoint engineVelocity;
	double energy;
	double eater; // how much energy can still be eaten this frame
	double armor;
	double repairRate;
	double shield;
	double actualShield;
	// public: // complex hardware
	public GBRadioState radio;
	public GBConstructorState constructor;
	public GBSensorState sensor1;
	public GBSensorState sensor2;
	public GBSensorState sensor3;
	public GBBlasterState blaster;
	public GBGrenadesState grenades;
	public GBForceFieldState forceField;
	public GBSyphonState syphon;
	public GBSyphonState enemySyphon;
	// public:

	// constants //

	public static final double kEffectivenessReduction = 0;
	// 0 = no reduction, 1 = proportional to armor, 0.5 = midway

	public static final double kEngineMinEffectiveSpeed = 0.05;
	public static final double kEngineEfficiency = 0.2;
	public static final double kDeathExplosionDamagePerBiomass = 0.010;
	public static final double kCorpsePerBiomass = 0.5;
	public static final double kAbortionCorpseFactor = 0.75;

	public static final double kRunningCostPerRepair = 2;
	public static final double kShieldDecayPerMass = 0.002;
	public static final double kShieldDecayPerShield = 0.01;

	public static final double kGrenadesFiringCostPerSmoke = 10;
	public static final double kBlastSpacing = 0.001;

	public static final double kBabyInitialSpeed = 0.01;
	public static final double kBabyDisplacementFraction = 0.6;

	public static final double kForceFieldRangeAttenuation = 0.05; // 1/range
																	// where
																	// efficiency
																	// halves
																	// due to
																	// range.
	public static final double kForceFieldRecoilPerPower = 0.25;

	public GBHardwareState(HardwareSpec spc) {
		spec = spc;
		enginePower = 0;
		engineVelocity = new FinePoint();
		energy = spc.InitialEnergy();
		eater = spc.Eater();
		armor = spc.Armor();
		repairRate = 0;
		shield = 0;
		actualShield = 0;
		radio = new GBRadioState();
		constructor = new GBConstructorState(spc.constructor);
		sensor1 = new GBSensorState(spc.sensor1);
		sensor2 = new GBSensorState(spc.sensor2);
		sensor3 = new GBSensorState(spc.sensor3);
		blaster = new GBBlasterState(spc.blaster);
		grenades = new GBGrenadesState(spc.grenades);
		forceField = new GBForceFieldState(spc.forceField);
		syphon = new GBSyphonState(spc.syphon);
		enemySyphon = new GBSyphonState(spc.enemySyphon);
	}

	public int getProcessor() {
		return spec.Processor();
	}

	public int getMemory() {
		return spec.Memory();
	}

	public double getEnginePower() {
		return enginePower;
	}

	public FinePoint getEngineVelocity() {
		return engineVelocity;
	}

	public double getEngineMaxPower() {
		return spec.Engine();
	}

	public double getEnergy() {
		return energy;
	}

	public double getMaxEnergy() {
		return spec.MaxEnergy();
	}

	public double getSolarCells() {
		return spec.SolarCells();
	}

	public double getEater() {
		return spec.Eater();
	}

	public double getEaterLimit() {
		return GBMath.clamp(eater, 0, getMaxEnergy() - energy);
	}

	public double getEaten() {
		return spec.Eater() - eater;
	}

	public double getArmor() {
		return armor;
	}

	public double getMaxArmor() {
		return spec.Armor();
	}

	public double getArmorFraction() {
		double max = spec.Armor();
		return max != 0 ? armor / max : 0;
	}

	public double getEffectivenessFraction() {
		return (getArmorFraction() - 1) * kEffectivenessReduction + 1;
	}

	public double getRepairRate() {
		return repairRate;
	}

	public double getMaxRepairRate() {
		return spec.RepairRate();
	}

	public double getShield() {
		return shield;
	}

	public double getActualShield() {
		return actualShield;
	}

	public double getMaxShield() {
		return spec.Shield();
	}

	public double getBomb() {
		return spec.Bomb();
	}

	public void setEnginePower(double power) {
		enginePower = GBMath.clamp(power, 0, getEngineMaxPower());
	}

	public void setEngineVelocity(FinePoint vel) {
		engineVelocity = vel;
	}

	public void eat(double amount) {
		double actual = Math.min(amount, getEaterLimit());
		energy += actual;
		eater -= actual;
	}

	public double giveEnergy(double amount) {
		if (amount < 0)
			throw new GBSimulationError("can't give negative energy'");
		double actual = Math.min(amount, getMaxEnergy() - energy);
		energy += actual;
		return actual;
	}

	public boolean useEnergy(double amount) {
		if (energy >= amount) {
			energy -= amount;
			return true;
		} else
			return false;
	}

	public double useEnergyUpTo(double amount) {
		double actual = Math.min(amount, energy);
		energy -= actual;
		return actual;
	}

	public void takeDamage(double amount) {
		armor -= amount;
	}

	public void setRepairRate(double rate) {
		repairRate = GBMath.clamp(rate, 0, getMaxRepairRate());
	}

	public void setShield(double power) {
		shield = GBMath.clamp(power, 0, getMaxShield());
	}

	public void act(GBRobot robot, GBWorld world) {
		// death check
		if (armor <= 0 || robot.dead) {
			robot.dead = true;
			world.addObjectLater(new GBCorpse(robot.getPosition(), robot
					.getVelocity(), robot.getBiomass() * kCorpsePerBiomass, robot
					.getRobotType(), robot.getLastHit()));
			world.addObjectLater(new GBExplosion(robot.getPosition(), robot
					.getOwner(), (robot.getBiomass()
					* kDeathExplosionDamagePerBiomass + spec.Bomb())
					* robot.getShieldFraction()));
			return;
		}
		// energy intake
		energy += getSolarCells();
		robot.getOwner().reportAutotrophy(getSolarCells());
		eater = spec.Eater();
		// engine
		if (enginePower > 0) {
			double effective = Math
					.max(robot.getSpeed(), kEngineMinEffectiveSpeed);
			FinePoint delta = engineVelocity.subtract(robot.getVelocity());
			double power = delta.norm() * robot.getMass() * effective
					/ kEngineEfficiency;
			if (power != 0) {
				power = useEnergyUpTo(Math.min(power, enginePower));
				robot.getOwner().getScores().expenditure.reportEngine(power);
				robot.pushBy(delta.multiply(power * kEngineEfficiency
						/ effective / delta.norm()));
			}
		}
		// complex hardware
		constructor.act(robot, world);
		sensor1.act(robot, world);
		sensor2.act(robot, world);
		sensor3.act(robot, world);
		radio.act(robot, world);
		blaster.act(robot, world);
		grenades.act(robot, world);
		forceField.act(robot, world);
		syphon.act(robot, world);
		enemySyphon.act(robot, world);
		// do repairs
		if (repairRate != 0) {
			double repairCost = useEnergyUpTo(Math.min(repairRate,
					(getMaxArmor() - armor) * kRunningCostPerRepair));
			robot.getOwner().getScores().expenditure.reportRepairs(repairCost);
			armor += repairCost / kRunningCostPerRepair;
		}
		// do shield
		if (shield != 0) {
			double shieldUsed = useEnergyUpTo(shield);
			robot.getOwner().getScores().expenditure.reportShield(shieldUsed);
			actualShield += shieldUsed;
		}
		actualShield = Math.max(
				actualShield - kShieldDecayPerMass * robot.getMass()
						- kShieldDecayPerShield * actualShield, 0);
		// lose excess energy
		if (energy > getMaxEnergy()) {
			robot.getOwner().getScores().expenditure.reportWasted(energy
					- getMaxEnergy());
			energy = Math.min(energy, getMaxEnergy());
		}
	}
}
