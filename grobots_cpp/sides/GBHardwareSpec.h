// GBHardwareSpec.h
// static specification of robot hardware
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef _GBHardwareSpec_h
#define _GBHardwareSpec_h

#include "GBTypes.h"


class GBConstructorSpec {
	GBPower rate;
public:
	GBConstructorSpec();
	~GBConstructorSpec();
	GBConstructorSpec & operator=(const GBConstructorSpec & arg);
// accessors
	GBPower Rate() const;
	void Set(const GBPower nrate);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
};

class GBSensorSpec {
	GBDistance range;
	int numResults;
	GBObjectClass seen;
	// features?
public:
	GBSensorSpec();
	~GBSensorSpec();
	GBSensorSpec & operator=(const GBSensorSpec & arg);
// accessors
	GBDistance Range() const;
	int NumResults() const;
	GBObjectClass Seen() const;
	void Set(const GBDistance rng, int rslts, GBObjectClass what);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
	GBEnergy FiringCost() const;
};

class GBBlasterSpec {
	GBDamage damage;
	GBDistance range;
	GBSpeed speed;
	GBFrames lifetime;
	GBFrames reloadTime;
public:
	GBBlasterSpec();
	~GBBlasterSpec();
	GBBlasterSpec & operator=(const GBBlasterSpec & arg);
// accessors
	GBDamage Damage() const;
	GBDistance Range() const;
	GBSpeed Speed() const;
	GBFrames Lifetime() const;
	GBFrames ReloadTime() const;
	GBForceScalar Recoil() const;
	void Set(const GBDamage dmg, const GBDistance range, const GBFrames reload);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
	GBEnergy FiringCost() const;
};

class GBGrenadesSpec {
	GBDamage damage;
	GBDistance range;
	GBSpeed speed;
	GBFrames reloadTime;
public:
	GBGrenadesSpec();
	~GBGrenadesSpec();
	GBGrenadesSpec & operator=(const GBGrenadesSpec & arg);
// accessors
	GBDamage Damage() const;
	GBDistance Range() const;
	GBSpeed Speed() const;
	GBFrames Lifetime() const;
	GBFrames ReloadTime() const;
	GBForceScalar Recoil() const;
	void Set(const GBDamage dmg, const GBDistance rng, const GBFrames reload);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
	GBEnergy FiringCost() const;
};

class GBForceFieldSpec {
	GBPower power;
	GBDistance range;
public:
	GBForceFieldSpec();
	~GBForceFieldSpec();
	GBForceFieldSpec & operator=(const GBForceFieldSpec & arg);
// accessors
	GBPower Power() const;
	GBDistance Range() const;
	void Set(const GBPower pwr, const GBDistance rng);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
};

class GBSyphonSpec {
	GBPower power;
	GBDistance range;
	bool hitsEnemies; // is it the friendly-only syphon?
public:
	GBSyphonSpec();
	~GBSyphonSpec();
	GBSyphonSpec & operator=(const GBSyphonSpec & arg);
// accessors
	GBPower Power() const;
	GBDistance Range() const;
	bool HitsEnemies() const;
	void Set(const GBPower pwr, const GBDistance rng, bool newHitsEnemies);
// costs
	GBEnergy Cost() const;
	GBMass Mass() const;
};

class GBHardwareSpec {
private: // simple hardware
	GBInstructionCount processor;
	long memory;
	GBPower engine;
	GBEnergy maxEnergy;
	GBEnergy initialEnergy;
	GBPower solarCells;
	GBPower eater;
	GBDamage armor;
	GBPower repairRate;
	GBPower shield;
	GBDamage bomb;
public: // complex hardware
	GBConstructorSpec constructor;
	GBSensorSpec sensor1;
	GBSensorSpec sensor2;
	GBSensorSpec sensor3;
	GBBlasterSpec blaster;
	GBGrenadesSpec grenades;
	GBForceFieldSpec forceField;
	GBSyphonSpec syphon;
	GBSyphonSpec enemySyphon;
	// cloaking device?
	// collision sensor?
private: // calculated
	GBEnergy coolingCost;
	GBEnergy hardwareCost;
	GBMass mass;
public:
	GBHardwareSpec();
	~GBHardwareSpec();
	GBHardwareSpec & operator=(const GBHardwareSpec & arg);
// accessors
	GBInstructionCount Processor() const;
	long Memory() const;
	void SetProcessor(GBInstructionCount speed, long mem);
	GBPower Engine() const;
	void SetEngine(const GBPower power);
	GBEnergy MaxEnergy() const;
	GBEnergy InitialEnergy() const;
	void SetEnergy(const GBEnergy max, const GBEnergy initial);
	GBPower SolarCells() const;
	void SetSolarCells(const GBPower amt);
	GBPower Eater() const;
	void SetEater(const GBPower amt);
	GBDamage Armor() const;
	void SetArmor(const GBDamage amt);
	GBPower RepairRate() const;
	void SetRepairRate(const GBPower rate);
	GBPower Shield() const;
	void SetShield(const GBPower amt);
	GBDamage Bomb() const;
	void SetBomb(const GBDamage amt);
// computed values
	void Recalculate();
	GBEnergy Cost() const;
	GBEnergy HardwareCost() const; // excludes initial energy
	GBEnergy BaseCost() const; // excludes initial energy and cooling
	GBMass Mass() const;
	GBEnergy GrowthCost() const;
	GBEnergy CombatCost() const;
// individual hardwares
	GBEnergy ChassisCost() const;
	GBMass ChassisMass() const;
	GBEnergy ProcessorCost() const;
	GBMass ProcessorMass() const;
	GBEnergy EngineCost() const;
	GBMass EngineMass() const;
	GBEnergy EnergyHardwareCost() const;
	GBEnergy EnergyCost() const;
	GBMass EnergyMass() const;
	GBEnergy SolarCellsCost() const;
	GBMass SolarCellsMass() const;
	GBEnergy EaterCost() const;
	GBMass EaterMass() const;
	GBEnergy ArmorCost() const;
	GBMass ArmorMass() const;
	GBEnergy RepairCost() const;
	GBMass RepairMass() const;
	GBEnergy ShieldCost() const;
	GBMass ShieldMass() const;
	GBEnergy BombCost() const;
	GBMass BombMass() const;
	GBEnergy CoolingCost() const;
	GBMass CoolingMass() const;
};

#endif
