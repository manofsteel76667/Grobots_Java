// GBHardwareSpec.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBHardwareSpec.h"

const GBRatio kStandardMassPerCost = 0.02;
const GBEnergy kStandardCost = 1000;
const GBMass kStandardMass = kStandardCost * kStandardMassPerCost;

const GBEnergy kBaseCost = 20;
const GBMass kBaseMass = 0.5; // heavier than average

const GBRatio kCoolingCostRatio = 0.0071;

const GBEnergy kCostPerProcessor = 2;
const GBMass kMassPerProcessor = kCostPerProcessor * kStandardMassPerCost * 0.5;
const GBEnergy kCostPerMemory = 0.05;
const GBMass kMassPerMemory = kCostPerProcessor * kStandardMassPerCost * 0.5;
const GBEnergy kCostPerEngine = 800;
const GBMass kMassPerEngine = kCostPerEngine * kStandardMassPerCost;

const GBEnergy kCostPerMaxEnergy = 0.10;
const GBMass kMassPerMaxEnergy = kCostPerMaxEnergy * kStandardMassPerCost;
const GBEnergy kCostPerSolarCells = 2000; // ideal doubletime 2100
const GBMass kMassPerSolarCells = kCostPerSolarCells * kStandardMassPerCost * 1.8;
const GBEnergy kCostPerEater = 100; // ideal doubletime 200
const GBMass kMassPerEater = kCostPerEater * kStandardMassPerCost;

const GBEnergy kCostPerArmor = 1;
const GBMass kMassPerArmor = kCostPerArmor * kStandardMassPerCost * 1.5;
const GBEnergy kCostPerRepairRate = 400;
const GBMass kMassPerRepairRate = kCostPerRepairRate * kStandardMassPerCost;
const GBEnergy kCostPerShield = 500;
const GBMass kMassPerShield = kCostPerShield * kStandardMassPerCost * 0.5;

const GBEnergy kConstructorCostPerRate = 100;
const GBMass kConstructorMassPerRate = kConstructorCostPerRate * kStandardMassPerCost;

const GBEnergy kSensorCostPerRange = 1;
const GBMass kSensorMassPerRange = kSensorCostPerRange * kStandardMassPerCost * 0.375;
const GBEnergy kSensorCostPerPower = 0.25;
const GBMass kSensorMassPerPower = kSensorCostPerPower * kStandardMassPerCost * 0.375;
const GBEnergy kSensorCostPerResult = 1;
const GBMass kSensorMassPerResult = kSensorCostPerResult * kStandardMassPerCost * 0.375;
const GBRatio kSensorFiringCostPerPower = 0.005;

const GBDamage kBlasterDamageOverhead = 0.3;
const GBEnergy kBlasterCostPerDamageRate = 120;
const GBMass kBlasterMassPerDamageRate = kBlasterCostPerDamageRate * kStandardMassPerCost;
const GBEnergy kBlasterCostPerRange = 8.0;
const GBMass kBlasterMassPerRange = kBlasterCostPerRange * kStandardMassPerCost;
const GBEnergy kBlasterCostPerRangeSquared = 0;
const GBMass kBlasterMassPerRangeSquared = kBlasterCostPerRangeSquared * kStandardMassPerCost * 1.5;
const GBNumber kBlasterBarrel = 0.01; // 1/reloadtime such that barrel charge = reloader charge
const GBEnergy kBlasterFiringCostPerDamage = kBlasterCostPerDamageRate * 0.0015; //1000 frames of use is about weapon cost (ignoring barrel charge)
const GBEnergy kBlasterFiringCostPerRange = kBlasterCostPerRange * 0.0015;
const GBEnergy kBlasterFiringCostPerRangeSquared = kBlasterCostPerRangeSquared * 0.0015;

const GBRatio kBlasterLifetimeSpeedTradeoff = 250;   // range at which speed is 1
const GBNumber kBlasterSpeedExponent = 0.35;   // how much range is adjusted using speed instead of lifetime

const GBDamage kGrenadesDamageOverhead = 0.3;
const GBEnergy kGrenadesCostPerDamageRate = 110;
const GBMass kGrenadesMassPerDamageRate = kGrenadesCostPerDamageRate * kStandardMassPerCost;
const GBEnergy kGrenadesCostPerRange = 8.0;
const GBMass kGrenadesMassPerRange = kGrenadesCostPerRange * kStandardMassPerCost * 1.2;
const GBEnergy kGrenadesCostPerRangeSquared = 0;
const GBMass kGrenadesMassPerRangeSquared = kGrenadesCostPerRangeSquared * kStandardMassPerCost * 1.5;
const GBNumber kGrenadesBarrelCost = 0.005; // 1/reloadtime such that barrel charge = reloader charge
const GBNumber kGrenadesBarrelMass = kGrenadesBarrelCost;
const GBEnergy kGrenadesFiringCostPerDamage = kGrenadesCostPerDamageRate * 0.0012; //1000 frames of use = weapon cost
const GBEnergy kGrenadesFiringCostPerRange = kGrenadesCostPerRange * 0.0012;
const GBEnergy kGrenadesFiringCostPerRangeSquared = kGrenadesCostPerRangeSquared * 0.0012;

const GBDistance kGrenadesLifetimeSpeedTradeoff = 250;   // range at which speed is 1
const GBNumber kGrenadesSpeedExponent = 0.35;   // how much range is adjusted using speed instead of lifetime
const GBRatio kGrenadesRecoil = 0.001;

const GBEnergy kForceFieldCostPerPower = 800;
const GBMass kForceFieldMassPerPower = kForceFieldCostPerPower * kStandardMassPerCost;
const GBEnergy kForceFieldCostPerRange = 80;
const GBMass kForceFieldMassPerRange = kForceFieldCostPerRange * kStandardMassPerCost;

const GBEnergy kCostPerBomb = 0.25; // much higher than grenades, but who's gonna notice?
const GBMass kMassPerBomb = kCostPerBomb * kStandardMassPerCost * 1.5;

const GBEnergy kSyphonCostPerPower = 16;
const GBEnergy kSyphonCostPerRange = 4;
const GBMass kSyphonMassPerPower = kSyphonCostPerPower * kStandardMassPerCost;
const GBMass kSyphonMassPerRange = kSyphonCostPerRange * kStandardMassPerCost;
const GBEnergy kEnemySyphonCostPerPower = 30;
const GBEnergy kEnemySyphonCostPerRange = 10;
const GBMass kEnemySyphonMassPerPower = kEnemySyphonCostPerPower * kStandardMassPerCost;
const GBMass kEnemySyphonMassPerRange = kEnemySyphonCostPerRange * kStandardMassPerCost;


// GBConstructorSpec //

GBConstructorSpec::GBConstructorSpec()
	: rate(0)
{}

GBConstructorSpec::~GBConstructorSpec() {}

GBConstructorSpec & GBConstructorSpec::operator=(const GBConstructorSpec & arg) {
	rate = arg.rate;
	return *this;
}

GBPower GBConstructorSpec::Rate() const { return rate;}

void GBConstructorSpec::Set(const GBPower nrate) {
	rate = max(nrate, 0);
}

GBEnergy GBConstructorSpec::Cost() const {
	return rate * kConstructorCostPerRate;
}

GBMass GBConstructorSpec::Mass() const {
	return rate * kConstructorMassPerRate;
}

// GBSensorSpec //

GBSensorSpec::GBSensorSpec()
	: range(0), numResults(1),
	seen(ocDead)
{}

GBSensorSpec::~GBSensorSpec() {}

GBSensorSpec & GBSensorSpec::operator=(const GBSensorSpec & arg) {
	range = arg.range;
	numResults = arg.numResults;
	seen = arg.seen;
	return *this;
}

GBDistance GBSensorSpec::Range() const { return range;}
int GBSensorSpec::NumResults() const { return numResults;}
GBObjectClass GBSensorSpec::Seen() const { return seen;}

void GBSensorSpec::Set(const GBDistance rng, int rslts, GBObjectClass what) {
	range = max(rng, 0);
	numResults = rslts < 1 ? 1 : rslts;
	seen = what;
}

GBEnergy GBSensorSpec::Cost() const {
	if ( seen != ocDead )
		return range * (kSensorCostPerRange + range * kSensorCostPerPower) + kSensorCostPerResult * numResults;
	else
		return 0;
}

GBMass GBSensorSpec::Mass() const {
	if ( seen != ocDead )
		return range * (kSensorMassPerRange + range * kSensorMassPerPower) + kSensorMassPerResult * numResults;
	else
		return 0;
}

GBEnergy GBSensorSpec::FiringCost() const {
	return range * range * kSensorFiringCostPerPower;
}

// GBBlasterSpec //

GBBlasterSpec::GBBlasterSpec()
	: damage(0),
	range(0), speed(0), lifetime(0),
	reloadTime(1)
{}

GBBlasterSpec::~GBBlasterSpec() {}

GBBlasterSpec & GBBlasterSpec::operator=(const GBBlasterSpec & arg) {
	damage = arg.damage;
	range = arg.range;
	speed = arg.speed;
	lifetime = arg.lifetime;
	reloadTime = arg.reloadTime;
	return *this;
}

GBDamage GBBlasterSpec::Damage() const {
	return damage;}

GBDistance GBBlasterSpec::Range() const {
	return range;}

GBSpeed GBBlasterSpec::Speed() const {
	return speed;}

GBFrames GBBlasterSpec::Lifetime() const {
	return lifetime;}

GBFrames GBBlasterSpec::ReloadTime() const {
	return reloadTime;}

void GBBlasterSpec::Set(const GBDamage dmg, const GBDistance rng, const GBFrames reload) {
	damage = max(dmg, 0);
	range = max(rng, 0);
	speed = pow(range / kBlasterLifetimeSpeedTradeoff, kBlasterSpeedExponent);
	lifetime = ceil(range / speed);
	reloadTime = (reload < 1) ? 1 : reload; // limit to >= 1
}

GBEnergy GBBlasterSpec::Cost() const {
	if ( damage > 0 )
		return (GBNumber(1) / reloadTime + kBlasterBarrel) * (damage + kBlasterDamageOverhead) * (kBlasterCostPerDamageRate
							+ range * kBlasterCostPerRange + square(range) * kBlasterCostPerRangeSquared);
	else
		return 0;
}

GBMass GBBlasterSpec::Mass() const {
	if ( damage > 0 )
		return (GBNumber(1) / reloadTime + kBlasterBarrel) * (damage + kBlasterDamageOverhead) * (kBlasterMassPerDamageRate
							+ range * kBlasterMassPerRange + square(range) * kBlasterMassPerRangeSquared);
	else
		return 0;
}

GBEnergy GBBlasterSpec::FiringCost() const {
	return (damage + kBlasterDamageOverhead) * (kBlasterFiringCostPerDamage
					+ range * kBlasterFiringCostPerRange + square(range) * kBlasterFiringCostPerRangeSquared);
}

// GBGrenadesSpec //

GBGrenadesSpec::GBGrenadesSpec()
	: damage(0),
	range(0), speed(0),
	reloadTime(1)
{}

GBGrenadesSpec::~GBGrenadesSpec() {}

GBGrenadesSpec & GBGrenadesSpec::operator=(const GBGrenadesSpec & arg) {
	damage = arg.damage;
	range = arg.range;
	speed = arg.speed;
	reloadTime = arg.reloadTime;
	return *this;
}

GBDamage GBGrenadesSpec::Damage() const {
	return damage;}

GBDistance GBGrenadesSpec::Range() const {
	return range;}

GBSpeed GBGrenadesSpec::Speed() const {
	return speed;}

GBFrames GBGrenadesSpec::Lifetime() const {
	return ceil(range / speed);}

GBFrames GBGrenadesSpec::ReloadTime() const {
	return reloadTime;}

GBForceScalar GBGrenadesSpec::Recoil() const {
	return range * damage * kGrenadesRecoil;}

void GBGrenadesSpec::Set(const GBDamage dmg, const GBDistance rng, const GBFrames reload) {
	damage = max(dmg, 0);
	range = max(rng, 0);
	speed = pow(range / kGrenadesLifetimeSpeedTradeoff, kGrenadesSpeedExponent);
	reloadTime = (reload < 1 ? 1 : reload); // no reload time less than 1 frame
}

GBEnergy GBGrenadesSpec::Cost() const {
	if ( damage > 0 )
		return (GBNumber(1) / reloadTime + kGrenadesBarrelCost) * (damage + kGrenadesDamageOverhead)
			* (kGrenadesCostPerDamageRate + range * kGrenadesCostPerRange
				+ square(range) * kGrenadesCostPerRangeSquared);
	else
		return 0;
}

GBMass GBGrenadesSpec::Mass() const {
	if ( damage > 0 )
		return (GBNumber(1) / reloadTime + kGrenadesBarrelMass) * (damage + kGrenadesDamageOverhead)
			* (kGrenadesMassPerDamageRate + range * kGrenadesMassPerRange
				+ square(range) * kGrenadesMassPerRangeSquared);
	else
		return 0;
}

GBEnergy GBGrenadesSpec::FiringCost() const {
	return (damage + kGrenadesDamageOverhead) * (kGrenadesFiringCostPerDamage
				+ range * kGrenadesFiringCostPerRange + square(range) * kGrenadesFiringCostPerRangeSquared);
}

// GBForceFieldSpec //

GBForceFieldSpec::GBForceFieldSpec()
	: power(0),
	range(0)
{}

GBForceFieldSpec::~GBForceFieldSpec() {}

GBForceFieldSpec & GBForceFieldSpec::operator=(const GBForceFieldSpec & arg) {
	power = arg.power;
	range = arg.range;
	return *this;
}

GBPower GBForceFieldSpec::Power() const {
	return power;}

GBDistance GBForceFieldSpec::Range() const {
	return range;}

void GBForceFieldSpec::Set(const GBPower pwr, const GBDistance rng) {
	power = max(pwr, 0);
	range = max(rng, 0);
}

GBEnergy GBForceFieldSpec::Cost() const {
	if ( power > 0 )
		return power * (kForceFieldCostPerPower
						+ range * kForceFieldCostPerRange);
	else
		return 0;
}

GBMass GBForceFieldSpec::Mass() const {
	if ( power > 0 )
		return power * (kForceFieldMassPerPower
						+ range * kForceFieldMassPerRange);
	else
		return 0;
}

// GBSyphonSpec //

GBSyphonSpec::GBSyphonSpec()
	: power(0), range(1), hitsEnemies(true)
{}

GBSyphonSpec::~GBSyphonSpec() {}

GBSyphonSpec & GBSyphonSpec::operator=(const GBSyphonSpec & arg) {
	power = arg.power;
	range = arg.range;
	hitsEnemies = arg.hitsEnemies;
	return *this;
}

GBPower GBSyphonSpec::Power() const {
	return power;}
	
GBDistance GBSyphonSpec::Range() const {
	return range;}
	
bool GBSyphonSpec::HitsEnemies() const {
	return hitsEnemies;}

void GBSyphonSpec::Set(const GBPower pwr, const GBDistance rng, bool newHitsEnemies) {
	power = max(pwr, 0);
	range = max(rng, 0);
	hitsEnemies = newHitsEnemies;
}

GBEnergy GBSyphonSpec::Cost() const {
	if ( power > 0 )
		return power * (hitsEnemies ? kEnemySyphonCostPerPower + range * kEnemySyphonCostPerRange
			: kSyphonCostPerPower + range * kSyphonCostPerRange);
	else
		return 0;
}

GBMass GBSyphonSpec::Mass() const {
	if ( power > 0 )
		return power * (hitsEnemies ? kEnemySyphonMassPerPower + range * kEnemySyphonMassPerRange
			: kSyphonMassPerPower + range * kSyphonMassPerRange);
	else
		return 0;
}

// GBHardwareSpec //

GBHardwareSpec::GBHardwareSpec()
	: processor(0), memory(0),
	engine(0),
	maxEnergy(1), initialEnergy(0),
	solarCells(0), eater(0),
	armor(1),
	repairRate(0), shield(0), bomb(0),
// complex hw
	constructor(),
	sensor1(), sensor2(), sensor3(),
	blaster(), grenades(), forceField(),
	syphon(), enemySyphon(),
// calculated
	coolingCost(0),
	hardwareCost(), mass()
{}

GBHardwareSpec::~GBHardwareSpec() {}

GBHardwareSpec & GBHardwareSpec::operator=(const GBHardwareSpec & arg) {
	processor = arg.processor;
	memory = arg.memory;
	engine = arg.engine;
	maxEnergy = arg.maxEnergy;
	initialEnergy = arg.initialEnergy;
	solarCells = arg.solarCells;
	eater = arg.eater;
	armor = arg.armor;
	repairRate = arg.repairRate;
	shield = arg.shield;
	bomb = arg.bomb;
	constructor = arg.constructor;
	sensor1 = arg.sensor1;
	sensor2 = arg.sensor2;
	sensor3 = arg.sensor3;
	blaster = arg.blaster;
	grenades = arg.grenades;
	forceField = arg.forceField;
	syphon = arg.syphon;
	enemySyphon = arg.enemySyphon;
	coolingCost = arg.coolingCost;
	hardwareCost = arg.hardwareCost;
	mass = arg.mass;
	return *this;
}

GBInstructionCount GBHardwareSpec::Processor() const {
	return processor;}

long GBHardwareSpec::Memory() const {
	return memory;}

void GBHardwareSpec::SetProcessor(GBInstructionCount speed, long mem) {
	processor = speed;
	memory = mem;}

GBPower GBHardwareSpec::Engine() const {
	return engine;}

void GBHardwareSpec::SetEngine(const GBPower power) {
	engine = max(power, 0);}

GBEnergy GBHardwareSpec::MaxEnergy() const {
	return maxEnergy;}

GBEnergy GBHardwareSpec::InitialEnergy() const {
	return initialEnergy;}

void GBHardwareSpec::SetEnergy(const GBEnergy full, const GBEnergy initial) {
	maxEnergy = max(full, 0);
	initialEnergy = clamp(initial, GBNumber(0), maxEnergy);}

GBPower GBHardwareSpec::SolarCells() const {
	return solarCells;}

void GBHardwareSpec::SetSolarCells(const GBPower amt) {
	solarCells = max(amt, 0);}

GBPower GBHardwareSpec::Eater() const {
	return eater;}

void GBHardwareSpec::SetEater(const GBPower amt) {
	eater = max(amt, 0);}

GBDamage GBHardwareSpec::Armor() const {
	return armor;}

void GBHardwareSpec::SetArmor(const GBDamage amt) {
	armor = max(amt, 1);}

GBPower GBHardwareSpec::RepairRate() const {
	return repairRate;}

void GBHardwareSpec::SetRepairRate(const GBPower rate) {
	repairRate = max(rate, 0);}

GBDamage GBHardwareSpec::Shield() const {
	return shield;}

void GBHardwareSpec::SetShield(const GBDamage amt) {
	shield = max(amt, 0);}

GBDamage GBHardwareSpec::Bomb() const {
	return bomb;}

void GBHardwareSpec::SetBomb(const GBDamage amt) {
	bomb = max(amt, 0);}

void GBHardwareSpec::Recalculate() {
	hardwareCost = ChassisCost()
		+ ProcessorCost()
		+ EngineCost()
		+ sensor1.Cost() + sensor2.Cost() + sensor3.Cost()
		+ forceField.Cost()
		+ GrowthCost() + CombatCost();
	mass = ChassisMass()
		+ ProcessorMass()
		+ EngineMass()
		+ EnergyMass()
		+ SolarCellsMass() + EaterMass()
		+ ArmorMass() + RepairMass() + ShieldMass()
		+ BombMass()
		+ constructor.Mass()
		+ sensor1.Mass() + sensor2.Mass() + sensor3.Mass()
		+ blaster.Mass() + grenades.Mass() + forceField.Mass()
		+ syphon.Mass() + enemySyphon.Mass();
	coolingCost = square(hardwareCost * kCoolingCostRatio);
	hardwareCost += coolingCost;
	mass += CoolingMass();
	if ( hardwareCost < kBaseCost || mass < kBaseMass )
		throw GBBadComputedValueError();
}

GBEnergy GBHardwareSpec::Cost() const {
	return hardwareCost + initialEnergy;}

GBEnergy GBHardwareSpec::HardwareCost() const {
	return hardwareCost;}

GBEnergy GBHardwareSpec::BaseCost() const {
	return hardwareCost - coolingCost;}

GBMass GBHardwareSpec::Mass() const {
	return mass;}
	
// not cached, since these aren't called much
GBEnergy GBHardwareSpec::GrowthCost() const {
	return EnergyHardwareCost() + SolarCellsCost()
		+ EaterCost() + constructor.Cost() + syphon.Cost();
}

GBEnergy GBHardwareSpec::CombatCost() const {
	return ArmorCost() + RepairCost() + ShieldCost() +
		blaster.Cost() + grenades.Cost() + enemySyphon.Cost() + BombCost();
}

GBEnergy GBHardwareSpec::ChassisCost() const {
	return kBaseCost;}

GBMass GBHardwareSpec::ChassisMass() const {
	return kBaseMass;}

GBEnergy GBHardwareSpec::ProcessorCost() const {
	return kCostPerProcessor * processor + kCostPerMemory * memory;}

GBMass GBHardwareSpec::ProcessorMass() const {
	return kMassPerProcessor * processor + kMassPerMemory * memory;}

GBEnergy GBHardwareSpec::EngineCost() const {
	return engine * kCostPerEngine;}

GBMass GBHardwareSpec::EngineMass() const {
	return engine * kMassPerEngine;}

GBEnergy GBHardwareSpec::EnergyHardwareCost() const {
	return maxEnergy * kCostPerMaxEnergy;}

GBEnergy GBHardwareSpec::EnergyCost() const {
	return EnergyHardwareCost() + initialEnergy;}

GBMass GBHardwareSpec::EnergyMass() const {
	return maxEnergy * kMassPerMaxEnergy;}

GBEnergy GBHardwareSpec::SolarCellsCost() const {
	return solarCells * kCostPerSolarCells;}

GBMass GBHardwareSpec::SolarCellsMass() const {
	return solarCells * kMassPerSolarCells;}

GBEnergy GBHardwareSpec::EaterCost() const {
	return eater * kCostPerEater;}

GBMass GBHardwareSpec::EaterMass() const {
	return eater * kMassPerEater;}

GBEnergy GBHardwareSpec::ArmorCost() const {
	return armor * kCostPerArmor;}

GBMass GBHardwareSpec::ArmorMass() const {
	return armor * kMassPerArmor;}

GBEnergy GBHardwareSpec::RepairCost() const {
	return repairRate * kCostPerRepairRate;}

GBMass GBHardwareSpec::RepairMass() const {
	return repairRate * kMassPerRepairRate;}

GBEnergy GBHardwareSpec::ShieldCost() const {
	return shield * kCostPerShield;}

GBMass GBHardwareSpec::ShieldMass() const {
	return shield * kMassPerShield;}

GBEnergy GBHardwareSpec::BombCost() const {
	return bomb * kCostPerBomb;}

GBMass GBHardwareSpec::BombMass() const {
	return bomb * kMassPerBomb;}

GBEnergy GBHardwareSpec::CoolingCost() const {
	return coolingCost;}

GBMass GBHardwareSpec::CoolingMass() const {
	return coolingCost * kStandardMassPerCost;}

