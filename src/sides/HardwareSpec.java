/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

import exception.GBSimulationError;

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

	public static final double kSensorCostPerRange = 1.0;
	public static final double kSensorMassPerRange = kSensorCostPerRange
			* kStandardMassPerCost * 0.375;
	public static final double kSensorCostPerPower = 0.25;
	public static final double kSensorMassPerPower = kSensorCostPerPower
			* kStandardMassPerCost * 0.375;
	public static final double kSensorCostPerResult = 1.0;
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

	int processor;
	int memory;
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
	public HardwareItem[] hardwareList;
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
	boolean debug;

	public HardwareSpec(boolean _debug) {
		this();
		debug = _debug;
		if (debug)
			// List of items with cost and mass, used for debugging on load but
			// not cloned
			hardwareList = new HardwareItem[21];
	}

	public HardwareSpec() {
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

	/*
	 * @Override public HardwareSpec clone() { HardwareSpec ret = new
	 * HardwareSpec(); ret.processor = processor; ret.memory = memory;
	 * ret.engine = engine; ret.maxEnergy = maxEnergy; ret.initialEnergy =
	 * initialEnergy; ret.solarCells = solarCells; ret.eater = eater; ret.armor
	 * = armor; ret.repairRate = repairRate; ret.shield = shield; ret.bomb =
	 * bomb; ret.constructor = constructor; ret.sensor1 = sensor1; ret.sensor2 =
	 * sensor2; ret.sensor3 = sensor3; ret.blaster = blaster; ret.grenades =
	 * grenades; ret.forceField = forceField; ret.syphon = syphon;
	 * ret.enemySyphon = enemySyphon; ret.coolingCost = coolingCost;
	 * ret.growthCost = growthCost; ret.combatCost = combatCost;
	 * ret.hardwareCost = hardwareCost; ret.mass = mass; return ret; }
	 */

	public int Processor() {
		return processor;
	}

	public int Memory() {
		return memory;
	}

	public void SetProcessor(int speed, int mem) {
		processor = speed;
		memory = mem;
	}

	public double Engine() {
		return engine;
	}

	public void SetEngine(double power) {
		engine = Math.max(power, 0);
	}

	public double MaxEnergy() {
		return maxEnergy;
	}

	public double InitialEnergy() {
		return initialEnergy;
	}

	public void SetEnergy(double full, double initial) {
		maxEnergy = Math.max(full, 0);
		// initialEnergy = clamp(initial, 0, maxEnergy);} jma no clamp function
		// in java
		initialEnergy = Math.max(0, Math.min(initial, maxEnergy));
	}

	public double SolarCells() {
		return solarCells;
	}

	public void SetSolarCells(double amt) {
		solarCells = Math.max(amt, 0);
	}

	public double Eater() {
		return eater;
	}

	public void SetEater(double amt) {
		eater = Math.max(amt, 0);
	}

	public double Armor() {
		return armor;
	}

	public void SetArmor(double amt) {
		armor = Math.max(amt, 1);
	}

	public double RepairRate() {
		return repairRate;
	}

	public void SetRepairRate(double rate) {
		repairRate = Math.max(rate, 0);
	}

	public double Shield() {
		return shield;
	}

	public void SetShield(double amt) {
		shield = Math.max(amt, 0);
	}

	public double Bomb() {
		return bomb;
	}

	public void SetBomb(double amt) {
		bomb = Math.max(amt, 0);
	}

	public void Recalculate() {
		growthCost = EnergyHardwareCost() + SolarCellsCost() + EaterCost()
				+ constructor.getCost() + syphon.getCost();
		combatCost = ArmorCost() + RepairCost() + ShieldCost() + blaster.getCost()
				+ grenades.getCost() + enemySyphon.getCost() + BombCost();
		hardwareCost = ChassisCost() + ProcessorCost() + EngineCost()
				+ sensor1.getCost() + sensor2.getCost() + sensor3.getCost()
				+ forceField.getCost() + growthCost + combatCost;
		mass = ChassisMass() + ProcessorMass() + EngineMass() + EnergyMass()
				+ SolarCellsMass() + EaterMass() + ArmorMass() + RepairMass()
				+ ShieldMass() + BombMass() + constructor.getMass()
				+ sensor1.getMass() + sensor2.getMass() + sensor3.getMass()
				+ blaster.getMass() + grenades.getMass() + forceField.getMass()
				+ syphon.getMass() + enemySyphon.getMass();
		coolingCost = Math.pow(hardwareCost * kCoolingCostRatio, 2);
		hardwareCost += coolingCost;
		mass += CoolingMass();
		if (debug)
			buildHardwareList();
		if (hardwareCost < kBaseCost || mass < kBaseMass)
			throw new GBSimulationError("impossibly low cost or mass");
	}

	void buildHardwareList() {
		// Builds the hardware list array, used for debugging initial load but
		// not in-game
		hardwareList[0] = new HardwareItem(ChassisMass(), ChassisCost());// Chassis
		hardwareList[1] = new HardwareItem(ProcessorMass(), ProcessorCost());// Processor
		hardwareList[2] = new HardwareItem(EngineMass(), EngineCost());// Engine
		hardwareList[3] = constructor;// Constructor
		hardwareList[4] = new HardwareItem(EnergyMass(), EnergyCost());// Energy
		hardwareList[5] = new HardwareItem(SolarCellsMass(), SolarCellsCost());// Solar-Cells
		hardwareList[6] = new HardwareItem(EaterMass(), EaterCost());// Eater
		hardwareList[7] = syphon;// Syphon
		hardwareList[8] = sensor1;// Robot-Sensor
		hardwareList[9] = sensor2;// Food-Sensor
		hardwareList[10] = sensor3;// Shot-Sensor
		hardwareList[11] = new HardwareItem(ArmorMass(), ArmorCost());// Armor
		hardwareList[12] = new HardwareItem(RepairMass(), RepairCost());// Repair-rate
		hardwareList[13] = new HardwareItem(ShieldMass(), ShieldCost());// Shield
		hardwareList[14] = blaster;// Blaster
		hardwareList[15] = grenades;// Grenades
		hardwareList[16] = forceField;// Force-Field
		hardwareList[17] = enemySyphon;// Enemy-syphon
		hardwareList[18] = new HardwareItem(BombMass(), BombCost());// Bomb
		hardwareList[19] = new HardwareItem(CoolingMass(), coolingCost);// Cooling
																		// charge
		hardwareList[20] = new HardwareItem(0, 0);// Code
	}

	public double Cost() {
		return hardwareCost + initialEnergy;
	}

	public double HardwareCost() {
		return hardwareCost;
	}

	public double BaseCost() {
		return hardwareCost - coolingCost;
	}

	public double Mass() {
		return mass;
	}

	public double GrowthCost() {
		return growthCost;
	}

	public double CombatCost() {
		return combatCost;
	}

	public double ChassisCost() {
		return kBaseCost;
	}

	public double ChassisMass() {
		return kBaseMass;
	}

	public double ProcessorCost() {
		return kCostPerProcessor * processor + kCostPerMemory * memory;
	}

	public double ProcessorMass() {
		return kMassPerProcessor * processor + kMassPerMemory * memory;
	}

	public double EngineCost() {
		return engine * kCostPerEngine;
	}

	public double EngineMass() {
		return engine * kMassPerEngine;
	}

	public double EnergyHardwareCost() {
		return maxEnergy * kCostPerMaxEnergy;
	}

	public double EnergyCost() {
		return EnergyHardwareCost() + initialEnergy;
	}

	public double EnergyMass() {
		return maxEnergy * kMassPerMaxEnergy;
	}

	public double SolarCellsCost() {
		return solarCells * kCostPerSolarCells;
	}

	public double SolarCellsMass() {
		return solarCells * kMassPerSolarCells;
	}

	public double EaterCost() {
		return eater * kCostPerEater;
	}

	public double EaterMass() {
		return eater * kMassPerEater;
	}

	public double ArmorCost() {
		return armor * kCostPerArmor;
	}

	public double ArmorMass() {
		return armor * kMassPerArmor;
	}

	public double RepairCost() {
		return repairRate * kCostPerRepairRate;
	}

	public double RepairMass() {
		return repairRate * kMassPerRepairRate;
	}

	public double ShieldCost() {
		return shield * kCostPerShield;
	}

	public double ShieldMass() {
		return shield * kMassPerShield;
	}

	public double BombCost() {
		return bomb * kCostPerBomb;
	}

	public double BombMass() {
		return bomb * kMassPerBomb;
	}

	public double CoolingCost() {
		return coolingCost;
	}

	public double CoolingMass() {
		return coolingCost * kStandardMassPerCost;
	}

	public boolean hasHardware(HardwareTypes type) {
		switch (type) {
		case hcArmor:
			return true;
		case hcBlaster:
			return blaster.cost > 0;
		case hcBomb:
			return bomb > 0;
		case hcConstructor:
			return constructor.cost > 0;
		case hcEater:
			return eater > 0;
		case hcEnemySyphon:
			return enemySyphon.cost > 0;
		case hcEnergy:
			return true;
		case hcEngine:
			return this.engine > 0;
		case hcFoodSensor:
			return sensor2.cost > 0;
		case hcForceField:
			return forceField.cost > 0;
		case hcGrenades:
			return grenades.cost > 0;
		case hcNone:
			return false;
		case hcProcessor:
			return processor > 0;
		case hcRadio:
			return true;
		case hcRepairRate:
			return repairRate > 0;
		case hcRobotSensor:
			return sensor1.cost > 0;
		case hcShield:
			return this.shield > 0;
		case hcShotSensor:
			return sensor3.cost > 0;
		case hcSolarCells:
			return this.solarCells > 0;
		case hcSyphon:
			return this.syphon.cost > 0;
		default:
			return false;

		}
	}

}
