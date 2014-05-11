package sides;

import exception.GBBadComputedValueError;
import exception.GBError;

public class HardwareSpec {
	public static final double kStandardMassPerCost = 0.02;
	public static final double kStandardCost = 1000;
	public static final double kStandardMass = kStandardCost
			* kStandardMassPerCost;

	public static final double kBaseCost = 20;
	public static final double kBaseMass = 0.5; // heavier than average

	public static final double kCoolingCostRatio = 0.0071;

	public static final double kCostPerProcessor = 2;
	public static final double kMassPerProcessor = kCostPerProcessor
			* kStandardMassPerCost * 0.5;
	public static final double kCostPerMemory = 0.05;
	public static final double kMassPerMemory = kCostPerProcessor
			* kStandardMassPerCost * 0.5;
	public static final double kCostPerEngine = 800;
	public static final double kMassPerEngine = kCostPerEngine
			* kStandardMassPerCost;

	public static final double kCostPerMaxEnergy = 0.10;
	public static final double kMassPerMaxEnergy = kCostPerMaxEnergy
			* kStandardMassPerCost;
	public static final double kCostPerSolarCells = 2000; // ideal doubletime
															// 2100
	public static final double kMassPerSolarCells = kCostPerSolarCells
			* kStandardMassPerCost * 1.8;
	public static final double kCostPerEater = 100; // ideal doubletime 200
	public static final double kMassPerEater = kCostPerEater
			* kStandardMassPerCost;

	public static final double kCostPerArmor = 1;
	public static final double kMassPerArmor = kCostPerArmor
			* kStandardMassPerCost * 1.5;
	public static final double kCostPerRepairRate = 400;
	public static final double kMassPerRepairRate = kCostPerRepairRate
			* kStandardMassPerCost;
	public static final double kCostPerShield = 500;
	public static final double kMassPerShield = kCostPerShield
			* kStandardMassPerCost * 0.5;

	public static final double kConstructorCostPerRate = 100;
	public static final double kConstructorMassPerRate = kConstructorCostPerRate
			* kStandardMassPerCost;

	public static final double kSensorCostPerRange = 1;
	public static final double kSensorMassPerRange = kSensorCostPerRange
			* kStandardMassPerCost * 0.375;
	public static final double kSensorCostPerPower = 0.25;
	public static final double kSensorMassPerPower = kSensorCostPerPower
			* kStandardMassPerCost * 0.375;
	public static final double kSensorCostPerResult = 1;
	public static final double kSensorMassPerResult = kSensorCostPerResult
			* kStandardMassPerCost * 0.375;
	public static final double kSensorFiringCostPerPower = 0.005;

	public static final double kBlasterDamageOverhead = 0.3;
	public static final double kBlasterCostPerDamageRate = 120;
	public static final double kBlasterMassPerDamageRate = kBlasterCostPerDamageRate
			* kStandardMassPerCost;
	public static final double kBlasterCostPerRange = 8.0;
	public static final double kBlasterMassPerRange = kBlasterCostPerRange
			* kStandardMassPerCost;
	public static final double kBlasterCostPerRangeSquared = 0;
	public static final double kBlasterMassPerRangeSquared = kBlasterCostPerRangeSquared
			* kStandardMassPerCost * 1.5;
	public static final double kBlasterBarrel = 0.01; // 1/reloadtime such that
														// barrel charge =
														// reloader charge
	public static final double kBlasterFiringCostPerDamage = kBlasterCostPerDamageRate * 0.0015; // 1000
																									// frames
																									// of
																									// use
																									// is
																									// about
																									// weapon
																									// cost
																									// (ignoring
																									// barrel
																									// charge)
	public static final double kBlasterFiringCostPerRange = kBlasterCostPerRange * 0.0015;
	public static final double kBlasterFiringCostPerRangeSquared = kBlasterCostPerRangeSquared * 0.0015;

	public static final double kBlasterLifetimeSpeedTradeoff = 250; // range at
																	// which
																	// speed is
																	// 1
	public static final double kBlasterSpeedExponent = 0.35; // how much range
																// is adjusted
																// using speed
																// instead of
																// lifetime

	public static final double kGrenadesDamageOverhead = 0.3;
	public static final double kGrenadesCostPerDamageRate = 110;
	public static final double kGrenadesMassPerDamageRate = kGrenadesCostPerDamageRate
			* kStandardMassPerCost;
	public static final double kGrenadesCostPerRange = 8.0;
	public static final double kGrenadesMassPerRange = kGrenadesCostPerRange
			* kStandardMassPerCost * 1.2;
	public static final double kGrenadesCostPerRangeSquared = 0;
	public static final double kGrenadesMassPerRangeSquared = kGrenadesCostPerRangeSquared
			* kStandardMassPerCost * 1.5;
	public static final double kGrenadesBarrelCost = 0.005; // 1/reloadtime such
															// that barrel
															// charge = reloader
															// charge
	public static final double kGrenadesBarrelMass = kGrenadesBarrelCost;
	public static final double kGrenadesFiringCostPerDamage = kGrenadesCostPerDamageRate * 0.0012; // 1000
																									// frames
																									// of
																									// use
																									// =
																									// weapon
																									// cost
	public static final double kGrenadesFiringCostPerRange = kGrenadesCostPerRange * 0.0012;
	public static final double kGrenadesFiringCostPerRangeSquared = kGrenadesCostPerRangeSquared * 0.0012;

	public static final double kGrenadesLifetimeSpeedTradeoff = 250; // range at
																		// which
																		// speed
																		// is 1
	public static final double kGrenadesSpeedExponent = 0.35; // how much range
																// is adjusted
																// using speed
																// instead of
																// lifetime
	public static final double kGrenadesRecoil = 0.001;

	public static final double kForceFieldCostPerPower = 800;
	public static final double kForceFieldMassPerPower = kForceFieldCostPerPower
			* kStandardMassPerCost;
	public static final double kForceFieldCostPerRange = 80;
	public static final double kForceFieldMassPerRange = kForceFieldCostPerRange
			* kStandardMassPerCost;

	public static final double kCostPerBomb = 0.25; // much higher than
													// grenades, but who's gonna
													// notice?
	public static final double kMassPerBomb = kCostPerBomb
			* kStandardMassPerCost * 1.5;

	public static final double kSyphonCostPerPower = 16;
	public static final double kSyphonCostPerRange = 4;
	public static final double kSyphonMassPerPower = kSyphonCostPerPower
			* kStandardMassPerCost;
	public static final double kSyphonMassPerRange = kSyphonCostPerRange
			* kStandardMassPerCost;
	public static final double kEnemySyphonCostPerPower = 30;
	public static final double kEnemySyphonCostPerRange = 10;
	public static final double kEnemySyphonMassPerPower = kEnemySyphonCostPerPower
			* kStandardMassPerCost;
	public static final double kEnemySyphonMassPerRange = kEnemySyphonCostPerRange
			* kStandardMassPerCost;

	long processor;
	long memory;
	double engine;
	double maxEnergy;
	double initialEnergy;
	double solarCells;
	double eater;
	double armor;
	double repairRate;
	double shield;
	double bomb;
	// public: // complex hardware
	public ConstructorSpec constructor;
	public SensorSpec sensor1;
	public SensorSpec sensor2;
	public SensorSpec sensor3;
	public BlasterSpec blaster;
	public GrenadesSpec grenades;
	public ForceFieldSpec forceField;
	public SyphonSpec syphon;
	public SyphonSpec enemySyphon;
	// cloaking device?
	// collision sensor?
	// private: // calculated
	double coolingCost;
	double growthCost, combatCost, hardwareCost;
	double mass;

	public HardwareSpec()  {
		processor = 0;
		memory = 0;
		engine = 0;
		maxEnergy = 1;
		initialEnergy = 0;
		solarCells = 0;
		eater = 0;
		armor = 1;
		repairRate = 0;
		shield = 0;
		bomb = 0;
		// complex hw
		constructor = new ConstructorSpec();
		sensor1 = new SensorSpec();
		sensor2 = new SensorSpec();
		sensor3 = new SensorSpec();
		blaster = new BlasterSpec();
		grenades = new GrenadesSpec();
		forceField = new ForceFieldSpec();
		syphon = new SyphonSpec();
		enemySyphon = new SyphonSpec();
		// calculated
		coolingCost = 0;
		growthCost = 0;
		combatCost = 0;
		hardwareCost = 0;
		mass = 0;
	}

	public HardwareSpec clone() {
		HardwareSpec ret = new HardwareSpec();
		ret.processor = processor;
		ret.memory = memory;
		ret.engine = engine;
		ret.maxEnergy = maxEnergy;
		ret.initialEnergy = initialEnergy;
		ret.solarCells = solarCells;
		ret.eater = eater;
		ret.armor = armor;
		ret.repairRate = repairRate;
		ret.shield = shield;
		ret.bomb = bomb;
		ret.constructor = constructor;
		ret.sensor1 = sensor1;
		ret.sensor2 = sensor2;
		ret.sensor3 = sensor3;
		ret.blaster = blaster;
		ret.grenades = grenades;
		ret.forceField = forceField;
		ret.syphon = syphon;
		ret.enemySyphon = enemySyphon;
		ret.coolingCost = coolingCost;
		ret.growthCost = growthCost;
		ret.combatCost = combatCost;
		ret.hardwareCost = hardwareCost;
		ret.mass = mass;
		return ret;
	}

	long Processor() {
		return processor;
	}

	long Memory() {
		return memory;
	}

	void SetProcessor(long speed, long mem) {
		processor = speed;
		memory = mem;
	}

	double Engine() {
		return engine;
	}

	void SetEngine(double power) {
		engine = Math.max(power, 0);
	}

	double MaxEnergy() {
		return maxEnergy;
	}

	double InitialEnergy() {
		return initialEnergy;
	}

	void SetEnergy(double full, double initial) {
		maxEnergy = Math.max(full, 0);
		// initialEnergy = clamp(initial, 0, maxEnergy);} jma no clamp function
		// in java
		initialEnergy = Math.max(0, Math.min(initial, maxEnergy));
	}

	double SolarCells() {
		return solarCells;
	}

	void SetSolarCells(double amt) {
		solarCells = Math.max(amt, 0);
	}

	double Eater() {
		return eater;
	}

	void SetEater(double amt) {
		eater = Math.max(amt, 0);
	}

	double Armor() {
		return armor;
	}

	void SetArmor(double amt) {
		armor = Math.max(amt, 1);
	}

	double RepairRate() {
		return repairRate;
	}

	void SetRepairRate(double rate) {
		repairRate = Math.max(rate, 0);
	}

	double Shield() {
		return shield;
	}

	void SetShield(double amt) {
		shield = Math.max(amt, 0);
	}

	double Bomb() {
		return bomb;
	}

	void SetBomb(double amt) {
		bomb = Math.max(amt, 0);
	}
	public void Recalculate() throws GBBadComputedValueError  {
		growthCost = EnergyHardwareCost() + SolarCellsCost() + EaterCost()
				+ constructor.Cost() + syphon.Cost();
		combatCost = ArmorCost() + RepairCost() + ShieldCost() + blaster.Cost()
				+ grenades.Cost() + enemySyphon.Cost() + BombCost();
		hardwareCost = ChassisCost() + ProcessorCost() + EngineCost()
				+ sensor1.Cost() + sensor2.Cost() + sensor3.Cost()
				+ forceField.Cost() + growthCost + combatCost;
		mass = ChassisMass() + ProcessorMass() + EngineMass() + EnergyMass()
				+ SolarCellsMass() + EaterMass() + ArmorMass() + RepairMass()
				+ ShieldMass() + BombMass() + constructor.Mass()
				+ sensor1.Mass() + sensor2.Mass() + sensor3.Mass()
				+ blaster.Mass() + grenades.Mass() + forceField.Mass()
				+ syphon.Mass() + enemySyphon.Mass();
		coolingCost = Math.pow(hardwareCost * kCoolingCostRatio, 2);
		hardwareCost += coolingCost;
		mass += CoolingMass();
		if (hardwareCost < kBaseCost || mass < kBaseMass)
			throw new GBBadComputedValueError();
	}

	double Cost() {
		return hardwareCost + initialEnergy;
	}

	double HardwareCost() {
		return hardwareCost;
	}

	double BaseCost() {
		return hardwareCost - coolingCost;
	}

	double Mass() {
		return mass;
	}

	double GrowthCost() {
		return growthCost;
	}

	double CombatCost() {
		return combatCost;
	}

	double ChassisCost() {
		return kBaseCost;
	}

	double ChassisMass() {
		return kBaseMass;
	}

	double ProcessorCost() {
		return kCostPerProcessor * processor + kCostPerMemory * memory;
	}

	double ProcessorMass() {
		return kMassPerProcessor * processor + kMassPerMemory * memory;
	}

	double EngineCost() {
		return engine * kCostPerEngine;
	}

	double EngineMass() {
		return engine * kMassPerEngine;
	}

	double EnergyHardwareCost() {
		return maxEnergy * kCostPerMaxEnergy;
	}

	double EnergyCost() {
		return EnergyHardwareCost() + initialEnergy;
	}

	double EnergyMass() {
		return maxEnergy * kMassPerMaxEnergy;
	}

	double SolarCellsCost() {
		return solarCells * kCostPerSolarCells;
	}

	double SolarCellsMass() {
		return solarCells * kMassPerSolarCells;
	}

	double EaterCost() {
		return eater * kCostPerEater;
	}

	double EaterMass() {
		return eater * kMassPerEater;
	}

	double ArmorCost() {
		return armor * kCostPerArmor;
	}

	double ArmorMass() {
		return armor * kMassPerArmor;
	}

	double RepairCost() {
		return repairRate * kCostPerRepairRate;
	}

	double RepairMass() {
		return repairRate * kMassPerRepairRate;
	}

	double ShieldCost() {
		return shield * kCostPerShield;
	}

	double ShieldMass() {
		return shield * kMassPerShield;
	}

	double BombCost() {
		return bomb * kCostPerBomb;
	}

	double BombMass() {
		return bomb * kMassPerBomb;
	}

	double CoolingCost() {
		return coolingCost;
	}

	double CoolingMass() {
		return coolingCost * kStandardMassPerCost;
	}

}
