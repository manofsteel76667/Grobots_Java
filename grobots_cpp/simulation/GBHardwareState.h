// GBHardwareState.h
// the state of one robot's hardware
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef HardwareState_h
#define HardwareState_h


#include "GBTypes.h"
#include "GBHardwareSpec.h"
#include "GBMessages.h"

class GBRobot;
class GBRobotType;
class GBWorld;
class GBObject;
class GBSensorResult;

class GBRadioState {
// state
	long writes;
	long sent;
	GBMessageNumber nextMessage[kNumMessageChannels];
public:
	GBRadioState();
	~GBRadioState();
// actions
	void Write(GBNumber value, long address, GBSide * side);
	GBNumber Read(long address, GBSide * side);
	void Send(const GBMessage & mess, long channel, GBSide * side);
	long MessagesWaiting(long channel, GBSide * side) const;
	const GBMessage * Receive(long channel, GBSide * side);
	void Reset(GBSide * side);
	void ClearChannel(long channel, GBSide * side);
	void SkipMessages(long channel, long skip, GBSide * side);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBConstructorState {
	GBConstructorSpec * spec;
// state
	GBRobotType * type;
	GBEnergy progress;
	GBEnergy abortion;
	long lastChild;
// orders
	GBEnergy rate;
public:
	GBConstructorState(GBConstructorSpec * spc);
	~GBConstructorState();
// accessors
	GBEnergy Rate() const;
	GBEnergy Progress() const;
	GBEnergy Remaining() const;
	GBMass FetusMass() const;
	GBNumber Fraction() const;
	GBEnergy MaxRate() const;
	GBRobotType * Type() const;
	long ChildID() const;
// actions
	void Start(GBRobotType * ntype, const GBEnergy free = 0);
	void SetRate(const GBEnergy nrate);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBSensorResult {
public:
	GBPosition where;
	GBVelocity vel;
	GBDistance dist;
	const GBSide * side;
	GBDistance radius;
	GBMass mass;
	GBEnergy energy;
	GBNumber shieldFraction;
	GBNumber bomb;
	bool reloading;
	long type;
	long ID;
	GBNumber flag;
	
	GBSensorResult & operator=(const GBSensorResult & other);
	GBSensorResult();
	GBSensorResult(const GBObject * obj, const GBDistance dis);
};

class GBSensorState {
	GBSensorSpec * spec;
	const GBSide * owner;
// orders
	bool firing;
	GBDistance distance;
	GBAngle direction;
	bool seesFriendly, seesEnemy;
// results
	GBFrames time; // when fired
	int found, currentResult;
	GBSensorResult * results; //dynamically allocated array
	GBVector whereOverall;
	// some sort of info about what was found
public:
	GBSensorState(GBSensorSpec * spc);
	~GBSensorState();
// accessors
	GBDistance MaxRange() const;
	const int MaxResults() const;
	GBEnergy FiringCost() const;
	GBObjectClass Seen() const;
	bool Firing() const;
	GBDistance Distance() const;
	GBAngle Direction() const;
	bool SeesFriendly() const;
	bool SeesEnemy() const;
	GBFrames Time() const;
	int Found() const;
	bool NextResult();
	int NumResults() const;
	int CurrentResult() const;
	void SetCurrentResult(int newCurrent);
	GBVector WhereFound() const;
	GBVelocity Velocity() const;
	long Side() const;
	GBDistance Radius() const;
	GBMass Mass() const;
	GBEnergy Energy() const;
	long Type() const;
	long ID() const;
	GBNumber ShieldFraction() const;
	GBNumber Bomb() const;
	bool Reloading() const;
	GBNumber Flag() const;
	GBVector WhereOverall() const;
// actions
	void SetDistance(const GBDistance dist);
	void SetDirection(const GBAngle dir);
	void SetSeesFriendly(bool value);
	void SetSeesEnemy(bool value);
	void Fire();
	void Report(const GBSensorResult find); // called repeatedly by GBSensorShot to report sightings
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBBlasterState {
	GBBlasterSpec * spec;
// state
	GBFrames cooldown;
// orders
	bool firing;
	GBAngle direction;
public:
	GBBlasterState(GBBlasterSpec * spc);
	~GBBlasterState();
// accessors
	GBFrames ReloadTime() const;
	GBSpeed Speed() const;
	GBFrames MaxLifetime() const;
	GBDistance MaxRange() const;
	GBDamage Damage() const;
	GBEnergy FiringCost() const;
	GBFrames Cooldown() const;
	bool Firing() const;
	GBAngle Direction() const;
// actions
	void Fire(const GBAngle dir);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBGrenadesState {
	GBGrenadesSpec * spec;
// state
	GBFrames cooldown;
// orders
	bool firing;
	GBAngle direction;
	GBDistance distance;
public:
	GBGrenadesState(GBGrenadesSpec * spc);
	~GBGrenadesState();
// accessors
	GBFrames ReloadTime() const;
	GBSpeed Speed() const;
	GBFrames MaxLifetime() const;
	GBDistance MaxRange() const;
	GBDamage Damage() const;
	GBEnergy FiringCost() const;
	GBFrames Cooldown() const;
	bool Firing() const;
	GBAngle Direction() const;
	GBDistance Distance() const;
	GBDistance ExplosionRadius() const;
// actions
	void Fire(const GBDistance dist, const GBAngle dir);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBForceFieldState {
	GBForceFieldSpec * spec;
// orders
	GBAngle direction;
	GBDistance distance;
	GBPower power;
	GBAngle angle;
public:
	GBForceFieldState(GBForceFieldSpec * spc);
	~GBForceFieldState();
// accessors
	GBDistance MaxRange() const;
	GBPower MaxPower() const;
	GBAngle Direction() const;
	GBDistance Distance() const;
	GBPower Power() const;
	GBAngle Angle() const;
	GBDistance Radius() const;
// actions
	void SetDistance(const GBDistance dist);
	void SetDirection(const GBAngle dir);
	void SetPower(const GBPower pwr);
	void SetAngle(const GBAngle ang);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBSyphonState {
	GBSyphonSpec * spec;
// orders
	GBAngle direction;
	GBDistance distance;
	GBPower rate;
	GBPower syphoned; //amount siphoned: for reporting to brains
public:
	GBSyphonState(GBSyphonSpec * spc);
	~GBSyphonState();
// accessors
	GBPower MaxRate() const;
	GBDistance MaxRange() const;
	GBAngle Direction() const;
	GBDistance Distance() const;
	GBPower Rate() const; //setting
	const GBPower Syphoned() const; //actual
// actions
	void SetDistance(const GBDistance dist);
	void SetDirection(const GBAngle dir);
	void SetRate(const GBPower pwr);
	void ReportUse(const GBPower used);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

class GBHardwareState {
	GBHardwareSpec * spec;
private: // simple hardware
	GBForceScalar enginePower;
	GBVector engineVelocity;
	GBEnergy energy;
	GBEnergy eater; // how much energy can still be eaten this frame
	GBDamage armor;
	GBDamage repairRate;
	GBPower shield;
	GBEnergy actualShield;
public: // complex hardware
	GBRadioState radio;
	GBConstructorState constructor;
	GBSensorState sensor1;
	GBSensorState sensor2;
	GBSensorState sensor3;
	GBBlasterState blaster;
	GBGrenadesState grenades;
	GBForceFieldState forceField;
	GBSyphonState syphon;
	GBSyphonState enemySyphon;
public:
	GBHardwareState(GBHardwareSpec * spc);
	~GBHardwareState();
// accessors
	GBInstructionCount Processor() const;
	long Memory() const;
	GBForceScalar EnginePower() const;
	GBVelocity EngineVelocity() const;
	GBForceScalar EngineMaxPower() const;
	GBEnergy Energy() const;
	GBEnergy MaxEnergy() const;
	GBEnergy SolarCells() const;
	GBEnergy Eater() const;
	GBEnergy EaterLimit() const; // how much we could eat, taking into account eater and battery
	GBEnergy Eaten() const; // how much eaten this frame
	GBDamage Armor() const;
	GBDamage MaxArmor() const;
	GBNumber ArmorFraction() const;
	GBNumber EffectivenessFraction() const;
	GBDamage RepairRate() const;
	GBDamage MaxRepairRate() const;
	GBPower Shield() const;
	GBEnergy ActualShield() const;
	GBPower MaxShield() const;
	GBNumber Bomb() const;
// actions
	void SetEnginePower(const GBPower power);
	void SetEngineVelocity(const GBVelocity & vel);
	void Eat(const GBEnergy amount);
	GBEnergy GiveEnergy(const GBEnergy amount); // return how much actually given
	bool UseEnergy(const GBEnergy amount); // return whether energy is available; deduct it if it is
	GBEnergy UseEnergyUpTo(const GBEnergy amount); // use as much as available; return amount used
	void TakeDamage(const GBDamage amount);
	void SetRepairRate(const GBDamage rate);
	void SetShield(const GBPower power);
// automation
	void Act(GBRobot * robot, GBWorld * world);
};

#endif
