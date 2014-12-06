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

	public int Processor() {
		return spec.Processor();
	}

	public int Memory() {
		return spec.Memory();
	}

	public double EnginePower() {
		return enginePower;
	}

	public FinePoint EngineVelocity() {
		return engineVelocity;
	}

	public double EngineMaxPower() {
		return spec.Engine();
	}

	public double Energy() {
		return energy;
	}

	public double MaxEnergy() {
		return spec.MaxEnergy();
	}

	public double SolarCells() {
		return spec.SolarCells();
	}

	public double Eater() {
		return spec.Eater();
	}

	public double EaterLimit() {
		return GBMath.clamp(eater, 0, MaxEnergy() - energy);
	}

	public double Eaten() {
		return spec.Eater() - eater;
	}

	public double Armor() {
		return armor;
	}

	public double MaxArmor() {
		return spec.Armor();
	}

	public double ArmorFraction() {
		double max = spec.Armor();
		return max != 0 ? armor / max : 0;
	}

	public double EffectivenessFraction() {
		return (ArmorFraction() - 1) * kEffectivenessReduction + 1;
	}

	public double RepairRate() {
		return repairRate;
	}

	public double MaxRepairRate() {
		return spec.RepairRate();
	}

	public double Shield() {
		return shield;
	}

	public double ActualShield() {
		return actualShield;
	}

	public double MaxShield() {
		return spec.Shield();
	}

	public double Bomb() {
		return spec.Bomb();
	}

	public void SetEnginePower(double power) {
		enginePower = GBMath.clamp(power, 0, EngineMaxPower());
	}

	public void SetEngineVelocity(FinePoint vel) {
		engineVelocity = vel;
	}

	public void Eat(double amount) {
		double actual = Math.min(amount, EaterLimit());
		energy += actual;
		eater -= actual;
	}

	public double GiveEnergy(double amount) {
		if (amount < 0)
			throw new GBSimulationError("can't give negative energy'");
		double actual = Math.min(amount, MaxEnergy() - energy);
		energy += actual;
		return actual;
	}

	public boolean UseEnergy(double amount) {
		if (energy >= amount) {
			energy -= amount;
			return true;
		} else
			return false;
	}

	public double UseEnergyUpTo(double amount) {
		double actual = Math.min(amount, energy);
		energy -= actual;
		return actual;
	}

	public void TakeDamage(double amount) {
		armor -= amount;
	}

	public void SetRepairRate(double rate) {
		repairRate = GBMath.clamp(rate, 0, MaxRepairRate());
	}

	public void SetShield(double power) {
		shield = GBMath.clamp(power, 0, MaxShield());
	}

	public void Act(GBRobot robot, GBWorld world) {
		// death check
		if (armor <= 0 || robot.dead) {
			robot.dead = true;
			world.addObjectLater(new GBCorpse(robot.Position(), robot.Velocity(),
					robot.Biomass() * kCorpsePerBiomass, robot.Type(), robot
							.LastHit()));
			world.addObjectLater(new GBExplosion(robot.Position(), robot.Owner(),
					(robot.Biomass() * kDeathExplosionDamagePerBiomass + spec
							.Bomb()) * robot.ShieldFraction()));
			return;
		}
		// energy intake
		energy += SolarCells();
		robot.Owner().ReportAutotrophy(SolarCells());
		eater = spec.Eater();
		// engine
		if (enginePower > 0) {
			double effective = Math
					.max(robot.Speed(), kEngineMinEffectiveSpeed);
			FinePoint delta = engineVelocity.subtract(robot.Velocity());
			double power = delta.norm() * robot.Mass() * effective
					/ kEngineEfficiency;
			if (power != 0) {
				power = UseEnergyUpTo(Math.min(power, enginePower));
				robot.Owner().Scores().expenditure.ReportEngine(power);
				robot.PushBy(delta.multiply(power * kEngineEfficiency
						/ effective / delta.norm()));
			}
		}
		// complex hardware
		constructor.Act(robot, world);
		sensor1.Act(robot, world);
		sensor2.Act(robot, world);
		sensor3.Act(robot, world);
		radio.Act(robot, world);
		blaster.Act(robot, world);
		grenades.Act(robot, world);
		forceField.Act(robot, world);
		syphon.Act(robot, world);
		enemySyphon.Act(robot, world);
		// do repairs
		if (repairRate != 0) {
			double repairCost = UseEnergyUpTo(Math.min(repairRate,
					(MaxArmor() - armor) * kRunningCostPerRepair));
			robot.Owner().Scores().expenditure.ReportRepairs(repairCost);
			armor += repairCost / kRunningCostPerRepair;
		}
		// do shield
		if (shield != 0) {
			double shieldUsed = UseEnergyUpTo(shield);
			robot.Owner().Scores().expenditure.ReportShield(shieldUsed);
			actualShield += shieldUsed;
		}
		actualShield = Math.max(
				actualShield - kShieldDecayPerMass * robot.Mass()
						- kShieldDecayPerShield * actualShield, 0);
		// lose excess energy
		if (energy > MaxEnergy()) {
			robot.Owner().Scores().expenditure.ReportWasted(energy
					- MaxEnergy());
			energy = Math.min(energy, MaxEnergy());
		}
	}
}
