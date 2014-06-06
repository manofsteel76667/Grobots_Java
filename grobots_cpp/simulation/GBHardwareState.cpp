// GBHardwareState.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBHardwareState.h"
#include "GBHardwareSpec.h"
#include "GBErrors.h"
#include "GBShot.h"
#include "GBFood.h"
#include "GBSensorShot.h"
#include "GBRobot.h"
#include "GBWorld.h"
#include "GBRobotType.h"
#include "GBSide.h"
#include "GBDecorations.h"
#include "GBSound.h"


// constants //

const GBNumber kEffectivenessReduction = 0;
// 0 = no reduction, 1 = proportional to armor, 0.5 = midway

const GBSpeed kEngineMinEffectiveSpeed = 0.05;
const GBRatio kEngineEfficiency = 0.2;
const GBRatio kDeathExplosionDamagePerBiomass = 0.010;
const GBRatio kCorpsePerBiomass = 0.5;
const GBRatio kAbortionCorpseFactor = 0.75;

const GBPower kRunningCostPerRepair = 2;
const GBPower kShieldDecayPerMass = 0.002;
const GBPower kShieldDecayPerShield = 0.01;

const GBEnergy kGrenadesFiringCostPerSmoke = 10;
const GBDistance kBlastSpacing = 0.001;

const GBSpeed kBabyInitialSpeed = 0.01;
const GBSpeed kBabyDisplacementFraction = 0.6;

const GBEnergy kRadioWriteCost = 0.05;
const GBEnergy kRadioSendCost = 0.05;

const GBRatio kForceFieldRangeAttenuation = 0.05; // 1/range where efficiency halves due to range.
const GBRatio kForceFieldRecoilPerPower = 0.25;

// GBRadioState //

GBRadioState::GBRadioState()
	: writes(0), sent(0)
{
	for ( long i = 0; i < kNumMessageChannels; i ++ )
		nextMessage[i] = 0;
}

GBRadioState::~GBRadioState() {}

void GBRadioState::Write(GBNumber value, long address, GBSide * side) {
	side->WriteSharedMemory(value, address);
	writes += 1;
}

GBNumber GBRadioState::Read(long address, GBSide * side) {
	return side->ReadSharedMemory(address);
}

void GBRadioState::Send(const GBMessage & mess, long channel, GBSide * side) {
	side->SendMessage(mess, channel);
	sent += mess.Length() + 1;  // note overhead
}

long GBRadioState::MessagesWaiting(long channel, GBSide * side) const {
	if ( channel < 1 || channel > kNumMessageChannels ) throw GBIndexOutOfRangeError();
	return side->MessagesWaiting(channel, nextMessage[channel - 1]);
}

const GBMessage * GBRadioState::Receive(long channel, GBSide * side) {
	if ( channel < 1 || channel > kNumMessageChannels ) throw GBIndexOutOfRangeError();
	const GBMessage * msg = side->ReceiveMessage(channel, nextMessage[channel - 1]);
	if ( msg != 0 )
		nextMessage[channel - 1] = msg->SequenceNumber() + 1;
	return msg;
}

void GBRadioState::Reset(GBSide * side) {
	for ( long i = 1; i <= kNumMessageChannels; i ++ )
		ClearChannel(i, side);
}

void GBRadioState::ClearChannel(long channel, GBSide * side) {
	if ( channel < 1 || channel > kNumMessageChannels ) throw GBIndexOutOfRangeError();
	nextMessage[channel - 1] = side->NextMessageNumber(channel);
}

void GBRadioState::SkipMessages(long channel, long skip, GBSide * side) {
	if ( channel < 1 || channel > kNumMessageChannels ) throw GBIndexOutOfRangeError();
	if ( skip <= 0 ) return;
	nextMessage[channel - 1] += skip;
	if ( nextMessage[channel - 1] > side->NextMessageNumber(channel) )
		nextMessage[channel - 1] = side->NextMessageNumber(channel);
}

void GBRadioState::Act(GBRobot * robot, GBWorld * world) {
	if ( ! writes && ! sent ) return;
	//GBEnergy en = kRadioWriteCost * writes + kRadioSendCost * sent;
	//robot->hardware.UseEnergy(en);
	world->AddObjectNew(new GBTransmission(robot->Position(), robot->Radius(), sent > 0));
	writes = 0; sent = 0;
}

// GBConstructorState //

GBConstructorState::GBConstructorState(GBConstructorSpec * spc)
	: spec(spc),
	type(nil),
	progress(0),
	abortion(0),
	lastChild(0),
	rate(0)
{}

GBConstructorState::~GBConstructorState() {}

GBEnergy GBConstructorState::Rate() const {return rate;}

GBEnergy GBConstructorState::Progress() const {return progress;}

GBEnergy GBConstructorState::Remaining() const {
	if ( ! type )
		return 0;
	return type->Cost() - progress;
}

GBMass GBConstructorState::FetusMass() const {
	return type ? Fraction() * type->Mass() : GBMass(0);}

GBNumber GBConstructorState::Fraction() const {
	if ( ! type )
		return 0;
	return progress / type->Cost();
}

GBEnergy GBConstructorState::MaxRate() const {return spec->Rate();}

GBRobotType * GBConstructorState::Type() const {return type;}

long GBConstructorState::ChildID() const {return lastChild;}

void GBConstructorState::Start(GBRobotType * ntype, const GBEnergy free) {
	if ( type == ntype )
		return;
	abortion += progress;
	type = ntype; 
	progress = free; //could have progress but no type - harmless, but it doesn't happen
}

void GBConstructorState::SetRate(const GBEnergy nrate) {
	rate = clamp(nrate, GBNumber(0), MaxRate());}

void GBConstructorState::Act(GBRobot * robot, GBWorld * world) {
	if ( type && rate ) {
		GBPower actual = robot->hardware.UseEnergyUpTo(min(rate, Remaining()));
		robot->Owner()->Scores().Expenditure().ReportConstruction(actual);
		progress += actual;
		if ( Remaining() <= 0 && ! robot->hardware.ActualShield() ) {
			StartSound(siBirth);
			GBAngle dir = world->Randoms().Angle();
			GBRobot * child = new GBRobot(type,
				robot->Position().AddPolar(kBabyDisplacementFraction * robot->Radius(), dir),
				robot->Velocity().AddPolar(kBabyInitialSpeed, dir), robot->ID());
			world->AddObjectNew(child);
			progress = 0;
			type = nil;
			lastChild = child->ID();
		}
		robot->Recalculate();
	}
	if ( abortion > 0 ) {
		world->AddObjectNew(new GBCorpse(robot->Position(), robot->Velocity(),
			abortion * kAbortionCorpseFactor, robot->Type(), nil)); //should really be child type, but we don't know that any more
		abortion = 0;
	}
}


// GBSensorResult and GBSensorState //

GBSensorResult & GBSensorResult::operator=(const GBSensorResult & other) {
	where = other.where;
	vel = other.vel;
	dist = other.dist;
	side = other.side;
	radius = other.radius;
	mass = other.mass;
	energy = other.energy;
	type = other.type;
	ID = other.ID;
	shieldFraction = other.shieldFraction;
	bomb = other.bomb;
	reloading = other.reloading;
	flag = other.flag;
	return *this;
}

GBSensorResult::GBSensorResult() : where(), vel(), radius(0), mass(0), energy(0),
	dist(), side(nil), type(0), ID(0), shieldFraction(1), bomb(0), reloading(false), flag(0) {}

GBSensorResult::GBSensorResult(const GBObject * obj, const GBDistance dis)
	: where(obj->Position()), vel(obj->Velocity()), dist(dis),
	side(obj->Owner()), radius(obj->Radius()),
	mass(obj->Mass()), energy(obj->Energy()), type(nil), ID(0),
	shieldFraction(1), bomb(0), reloading(false), flag(0)
{
	const GBRobot * rob = dynamic_cast<const GBRobot *>(obj);
	if ( rob ) {
		type = rob->Type()->ID();
		ID = rob->ID();
		shieldFraction = rob->ShieldFraction();
		bomb = rob->hardware.Bomb();
		reloading = rob->hardware.blaster.Cooldown() || rob->hardware.grenades.Cooldown();
		flag = rob->flag;
		return;
	}
	const GBShot * shot = dynamic_cast<const GBShot *>(obj);
	if ( shot ) {
		type = shot->Type();
		energy = shot->Power();
		return;
	}
}

GBSensorState::GBSensorState(GBSensorSpec * spc)
	: spec(spc),
	firing(false), distance(0), direction(), seesFriendly(spc->Seen() == ocFood), seesEnemy(true),
	time(-1), found(0), currentResult(0), results(nil), whereOverall(), owner(nil)
{
	results = new GBSensorResult[MaxResults()];
	if ( ! results )
		throw GBGenericError("Out of memory creating sensor results array");
}

GBSensorState::~GBSensorState() { delete[] results; }

GBDistance GBSensorState::MaxRange() const {
	return spec->Range();}

// FIXME: calling SensorSpec->Set() on a spec that has a state will cause pointer
//  problems, since maxResults is not cached locally.
const int GBSensorState::MaxResults() const {
	return spec->NumResults();}

GBEnergy GBSensorState::FiringCost() const {
	return spec->FiringCost();}

GBObjectClass GBSensorState::Seen() const {
	return spec->Seen();}

bool GBSensorState::Firing() const {
	return firing;}

GBDistance GBSensorState::Distance() const {
	return distance;}

GBAngle GBSensorState::Direction() const {
	return direction;}

bool GBSensorState::SeesFriendly() const {
	return seesFriendly;}

bool GBSensorState::SeesEnemy() const {
	return seesEnemy;}

GBFrames GBSensorState::Time() const {
	return time;}

int GBSensorState::Found() const {
	return found;}

//returns false if wraparound occured
bool GBSensorState::NextResult() {
	currentResult++;
	if (currentResult < NumResults())
		return true;
	else {
		currentResult = 0;
		return false;
	}
}

int GBSensorState::NumResults() const {
	return found <= MaxResults() ? found : MaxResults(); //min
}

int GBSensorState::CurrentResult() const {
	return currentResult;
}

void GBSensorState::SetCurrentResult(int newCurrent) {
	if ( newCurrent < 0 || newCurrent >= NumResults())
		throw GBBadArgumentError();
	currentResult = newCurrent;
}

GBVector GBSensorState::WhereFound() const {
	if ( currentResult < NumResults() )
		return results[currentResult].where;
	else
		return GBPosition();
}

GBVelocity GBSensorState::Velocity() const {
	if ( currentResult < NumResults() )
		return results[currentResult].vel;
	else
		return GBVelocity();
}

long GBSensorState::Side() const {
	if ( currentResult < NumResults() )
		if ( results[currentResult].side == nil )
			return 0;
		else
			return results[currentResult].side->ID();
	else
		return nil;
}
GBDistance GBSensorState::Radius() const {
	if ( currentResult < NumResults() )
		return results[currentResult].radius;
	else
		return 0;
}
GBMass GBSensorState::Mass() const {
	if ( currentResult < NumResults() )
		return results[currentResult].mass;
	else
		return 0;
}
GBEnergy GBSensorState::Energy() const {
	if ( currentResult < NumResults() )
		return results[currentResult].energy;
	else
		return 0;
}
long GBSensorState::Type() const {
	if ( currentResult < NumResults() )
		return results[currentResult].type;
	else
		return nil;
}
long GBSensorState::ID() const {
	if ( currentResult < NumResults() ) {
		return results[currentResult].ID;
	} else
		return 0;
}

GBNumber GBSensorState::ShieldFraction() const {
	if ( currentResult < NumResults() )
		return results[currentResult].shieldFraction;
	else
		return GBNumber(1);
}

GBNumber GBSensorState::Bomb() const {
	if ( currentResult < NumResults() )
		return results[currentResult].bomb;
	else
		return 0;
}

bool GBSensorState::Reloading() const {
	return currentResult < NumResults() && results[currentResult].reloading;
}

GBNumber GBSensorState::Flag() const {
	if ( currentResult < NumResults() )
		return results[currentResult].flag;
	else
		return 0;
}

GBVector GBSensorState::WhereOverall() const {
	if ( found )
		return whereOverall / found;
	else
		return GBFinePoint(0, 0);
}

void GBSensorState::SetDistance(const GBDistance dist) {
	distance = dist;}

void GBSensorState::SetDirection(const GBAngle dir) {
	direction = dir;}

void GBSensorState::SetSeesFriendly(bool value) {
	seesFriendly = value;}

void GBSensorState::SetSeesEnemy(bool value) {
	seesEnemy = value;}

void GBSensorState::Fire() {
	firing = true;}

void GBSensorState::Report(const GBSensorResult find) {
	// check for same robot is in SensorShot::CollideWith(). Check for wrong type of object is in GBWorld.
	if ( ! ((find.side == owner) ? seesFriendly : seesEnemy) )
		return;
	GBSensorResult temp, current = find;
	// insert find in results
	for ( int i = 0; i < MaxResults(); i++ ) {
		if ( i >= found ) { // beyond end of filled part of array
			results[i] = current;
			break;
		}
		if ( current.dist < results[i].dist ) {
			temp = results[i]; // swap current and results[i]
			results[i] = current;
			current = temp;
		} else {}
	}
	found ++;
	whereOverall += find.where;
}

void GBSensorState::Act(GBRobot * robot, GBWorld * world) {
	if ( firing && spec->Seen() != ocDead /* if sensor exists */ ) {
		if ( ! robot->dead && robot->hardware.UseEnergy(FiringCost()) ) {
			robot->Owner()->Scores().Expenditure().ReportSensors(FiringCost());
			world->AddObjectNew(new GBSensorShot(robot->Position().AddPolar(distance, direction),
				robot, this));
		}
		found = 0;
		currentResult = 0;
		whereOverall.Set(0, 0);
		time = world->CurrentFrame();
		owner = robot->Owner();
	}
	firing = false;
}

// GBBlasterState //

GBBlasterState::GBBlasterState(GBBlasterSpec * spc)
	: spec(spc),
	cooldown(spc->ReloadTime()),
	firing(false), direction(0)
{}

GBBlasterState::~GBBlasterState() {}

GBFrames GBBlasterState::ReloadTime() const {
	return spec->ReloadTime();}

GBSpeed GBBlasterState::Speed() const {
	return spec->Speed();}

GBFrames GBBlasterState::MaxLifetime() const {
	return spec->Lifetime();}

GBDistance GBBlasterState::MaxRange() const {
	return spec->Range();}

GBDamage GBBlasterState::Damage() const {
	return spec->Damage();}

GBEnergy GBBlasterState::FiringCost() const {
	return spec->FiringCost();}

GBFrames GBBlasterState::Cooldown() const {
	return cooldown;}

bool GBBlasterState::Firing() const {
	return firing;}

GBAngle GBBlasterState::Direction() const {
	return direction;}

void GBBlasterState::Fire(const GBAngle dir) {
	if ( cooldown == 0 && Damage() ) {
		cooldown = ReloadTime();
		firing = true;
		direction = dir;
	}
}

void GBBlasterState::Act(GBRobot * robot, GBWorld * world) {
	if ( firing ) {
		GBNumber effectiveness = robot->hardware.EffectivenessFraction();
		if ( ! robot->hardware.ActualShield()
				&& robot->hardware.UseEnergy(FiringCost() * effectiveness) ) {
			robot->Owner()->Scores().Expenditure().ReportWeapons(FiringCost() * effectiveness);
			if ( Damage() >= 12 ) StartSound(siBlast);
			else StartSound(siSmallBlast);
			GBObject * shot = new GBBlast(robot->Position().AddPolar(robot->Radius(), direction),
				robot->Velocity().AddPolar(Speed(), direction),
				robot->Owner(), Damage() * effectiveness, MaxLifetime());
			shot->MoveBy(GBFinePoint::MakePolar(shot->Radius() + kBlastSpacing, direction)); // to avoid hitting self
			world->AddObjectNew(shot);
			cooldown = ReloadTime();
		}
		firing = false;
	}
	if ( cooldown > 0 )
		cooldown --;
}

// GBGrenadesState //

GBGrenadesState::GBGrenadesState(GBGrenadesSpec * spc)
	: spec(spc),
	cooldown(spc->ReloadTime()),
	firing(false), direction(0), distance(0)
{}

GBGrenadesState::~GBGrenadesState() {}

GBFrames GBGrenadesState::ReloadTime() const {
	return spec->ReloadTime();}

GBSpeed GBGrenadesState::Speed() const {
	return spec->Speed();}

GBFrames GBGrenadesState::MaxLifetime() const {
	return spec->Lifetime();}

GBDistance GBGrenadesState::MaxRange() const {
	return spec->Range();}

GBDamage GBGrenadesState::Damage() const {
	return spec->Damage();}

GBEnergy GBGrenadesState::FiringCost() const {
	return spec->FiringCost();}

GBFrames GBGrenadesState::Cooldown() const {
	return cooldown;}

bool GBGrenadesState::Firing() const {
	return firing;}

GBAngle GBGrenadesState::Direction() const {
	return direction;}

GBAngle GBGrenadesState::Distance() const {
	return distance;}

GBDistance GBGrenadesState::ExplosionRadius() const {
	return GBExplosion::PowerRadius(Damage());}

void GBGrenadesState::Fire(const GBDistance dist, const GBAngle dir) {
	if ( cooldown == 0 && Damage() ) {
		cooldown = ReloadTime();
		firing = true;
		direction = dir;
		distance = clamp(dist, Speed(), MaxRange());
	}
}

void GBGrenadesState::Act(GBRobot * robot, GBWorld * world) {
	if ( firing ) {
		GBNumber effectiveness = robot->hardware.EffectivenessFraction();
		if ( ! robot->hardware.ActualShield()
				&& robot->hardware.UseEnergy(FiringCost() * effectiveness) ) {
			robot->Owner()->Scores().Expenditure().ReportWeapons(FiringCost() * effectiveness);
			StartSound(siGrenade);
			GBFrames lifetime = max(floor((distance - robot->Radius()) / Speed()), 1L);
			GBObject * shot = new GBGrenade(robot->Position().AddPolar(robot->Radius(), direction),
				robot->Velocity().AddPolar(Speed(), direction),
				robot->Owner(), Damage() * effectiveness, lifetime);
			world->AddObjectNew(shot);
			for ( GBEnergy en = FiringCost() * effectiveness; en >= kGrenadesFiringCostPerSmoke; en -= kGrenadesFiringCostPerSmoke ) {
				GBObject * smoke = new GBSmoke(robot->Position().AddPolar(robot->Radius(), direction),
					world->Randoms().Vector(kSmokeMaxSpeed),
					world->Randoms().LongInRange(kSmokeMinLifetime, kSmokeMaxLifetime));
				world->AddObjectNew(smoke);
			}
			cooldown = ReloadTime();
		// recoil
			//robot->PushBy(- spec->Recoil() * effectiveness, direction);
		}
		firing = false;
	}
	if ( cooldown > 0 )
		cooldown --;
}

// GBForceFieldState //

GBForceFieldState::GBForceFieldState(GBForceFieldSpec * spc)
	: spec(spc),
	direction(0), distance(0), power(0), angle(0)
{}

GBForceFieldState::~GBForceFieldState() {}

GBDistance GBForceFieldState::MaxRange() const {
	return spec->Range();}

GBPower GBForceFieldState::MaxPower() const {
	return spec->Power();}

GBAngle GBForceFieldState::Direction() const {
	return direction;}

GBAngle GBForceFieldState::Distance() const {
	return distance;}

GBPower GBForceFieldState::Power() const {
	return power;}

GBAngle GBForceFieldState::Angle() const {
	return angle;}

GBDistance GBForceFieldState::Radius() const {
	return GBForceField::PowerRadius(power);}

void GBForceFieldState::SetDistance(const GBDistance dist) {
	distance = clamp(dist, GBNumber(0), MaxRange());}

void GBForceFieldState::SetDirection(const GBAngle dir) {
	direction = dir;}

void GBForceFieldState::SetPower(const GBPower pwr) {
	power = clamp(pwr, GBNumber(0), MaxPower());}

void GBForceFieldState::SetAngle(const GBAngle ang) {
	angle = ang;}

void GBForceFieldState::Act(GBRobot * robot, GBWorld * world) {
	if ( ! power ) return;
	GBNumber effective = power * robot->hardware.EffectivenessFraction() * robot->ShieldFraction();
	if ( power && robot->hardware.UseEnergy(effective) ) {
		robot->Owner()->Scores().Expenditure().ReportForceField(effective);
		GBPosition vel = GBFinePoint::MakePolar(distance, direction);
		GBObject * shot = new GBForceField(robot->Position() + vel, vel,
			robot->Owner(), effective / (distance * kForceFieldRangeAttenuation + 1), angle);
		world->AddObjectNew(shot);
		//robot->PushBy(- effective * kForceFieldRecoilPerPower, angle);  // recoil
		if ( gRandoms.Boolean((distance - robot->Radius() - 2) * 0.05) ) {
			GBSparkle * sparkle = new GBSparkle(
				robot->Position().AddPolar(gRandoms.InRange(robot->Radius(), distance - 2), direction),
				robot->Velocity().AddPolar(0.05, direction), shot->Color(), 15);
			world->AddObjectNew(sparkle);
		}
	}
}

// GBSyphonState //

GBSyphonState::GBSyphonState(GBSyphonSpec * spc)
	: spec(spc),
	direction(0), distance(0), rate(0), syphoned(0)
{}

GBSyphonState::~GBSyphonState() {}

GBPower GBSyphonState::MaxRate() const {return spec->Power();}

GBDistance GBSyphonState::MaxRange() const {return spec->Range();}

GBAngle GBSyphonState::Direction() const {return direction;}

GBDistance GBSyphonState::Distance() const {return distance;}

GBPower GBSyphonState::Rate() const {return rate;}

const GBPower GBSyphonState::Syphoned() const {return syphoned;}

void GBSyphonState::SetDistance(const GBDistance dist) {
	distance = max(dist, 0);}

void GBSyphonState::SetDirection(const GBAngle dir) {
	direction = dir;}

void GBSyphonState::SetRate(const GBPower pwr) {
	rate = clamp(pwr, - MaxRate(), MaxRate());
}

void GBSyphonState::ReportUse(const GBPower pwr) {
	syphoned += pwr;
}

void GBSyphonState::Act(GBRobot * robot, GBWorld * world) {
	if ( rate ) {
		GBPower limit = MaxRate() * robot->ShieldFraction(); // should maybe diminish with distance
		GBPower actual = clamp(rate, - limit, limit);
		GBObject * shot = new GBSyphon(
			robot->Position().AddPolar(min(distance, robot->Radius() + MaxRange()), direction),
			actual, robot, this, spec->HitsEnemies());
		world->AddObjectNew(shot);
		if ( gRandoms.Boolean((distance - robot->Radius() - 1) * 0.1) ) {
			//TODO making sparkles is expensive - 4% of runtime
			GBSparkle * sparkle = new GBSparkle(
				robot->Position().AddPolar(gRandoms.InRange(robot->Radius(), distance - 1) + (actual > 0 ? 1 : 0), direction),
				GBFinePoint::MakePolar(actual > 0 ? -0.1 : 0.1, direction),
				robot->Color(), 10);
			world->AddObjectNew(sparkle);
		}
	}
	syphoned = 0;
}

// GBHardwareState //

GBHardwareState::GBHardwareState(GBHardwareSpec * spc)
	: spec(spc),
	enginePower(0), engineVelocity(),
	energy(spc->InitialEnergy()),
	eater(spc->Eater()),
	armor(spc->Armor()),
	repairRate(0),
	shield(0), actualShield(0),
	radio(),
	constructor(&spc->constructor),
	sensor1(&spc->sensor1),
	sensor2(&spc->sensor2),
	sensor3(&spc->sensor3),
	blaster(&spc->blaster),
	grenades(&spc->grenades),
	forceField(&spc->forceField),
	syphon(&spc->syphon),
	enemySyphon(&spc->enemySyphon)
{}

GBHardwareState::~GBHardwareState() {}

GBInstructionCount GBHardwareState::Processor() const { return spec->Processor();}

long GBHardwareState::Memory() const { return spec->Memory();}

GBForceScalar GBHardwareState::EnginePower() const { return enginePower;}

GBVector GBHardwareState::EngineVelocity() const { return engineVelocity;}

GBForceScalar GBHardwareState::EngineMaxPower() const { return spec->Engine();}

GBEnergy GBHardwareState::Energy() const { return energy;}

GBEnergy GBHardwareState::MaxEnergy() const { return spec->MaxEnergy();}

GBPower GBHardwareState::SolarCells() const { return spec->SolarCells();}

GBEnergy GBHardwareState::Eater() const { return spec->Eater();}

GBEnergy GBHardwareState::EaterLimit() const {
	return clamp(eater, GBNumber(0), MaxEnergy() - energy);}

GBEnergy GBHardwareState::Eaten() const {
	return spec->Eater() - eater;}

GBDamage GBHardwareState::Armor() const { return armor;}

GBDamage GBHardwareState::MaxArmor() const { return spec->Armor();}

GBNumber GBHardwareState::ArmorFraction() const {
	GBDamage max = spec->Armor();
	return max ? armor / max : GBNumber(0);
}

GBNumber GBHardwareState::EffectivenessFraction() const {
	return (ArmorFraction() - 1) * kEffectivenessReduction + 1;
}

GBPower GBHardwareState::RepairRate() const { return repairRate;}

GBPower GBHardwareState::MaxRepairRate() const { return spec->RepairRate();}

GBPower GBHardwareState::Shield() const { return shield;}

GBEnergy GBHardwareState::ActualShield() const { return actualShield;}

GBPower GBHardwareState::MaxShield() const { return spec->Shield();}

GBNumber GBHardwareState::Bomb() const { return spec->Bomb(); }

void GBHardwareState::SetEnginePower(const GBPower power) {
	enginePower = clamp(power, GBNumber(0), EngineMaxPower());}

void GBHardwareState::SetEngineVelocity(const GBVector & vel) {
	engineVelocity = vel;}

void GBHardwareState::Eat(const GBEnergy amount) {
	GBEnergy actual = min(amount, EaterLimit());
	energy += actual;
	eater -= actual;
}

GBEnergy GBHardwareState::GiveEnergy(const GBEnergy amount) {
	if ( amount < 0 ) throw GBBadArgumentError();
	GBEnergy actual = min(amount, MaxEnergy() - energy);
	energy += actual;
	return actual;
}

bool GBHardwareState::UseEnergy(const GBEnergy amount) {
	if ( energy >= amount ) {
		energy -= amount;
		return true;
	} else
		return false;
}

GBEnergy GBHardwareState::UseEnergyUpTo(const GBEnergy amount) {
	GBEnergy actual = min(amount, energy);
	energy -= actual;
	return actual;
}

void GBHardwareState::TakeDamage(const GBDamage amount) {
	armor -= amount;
}

void GBHardwareState::SetRepairRate(const GBPower rate) {
	repairRate = clamp(rate, GBNumber(0), MaxRepairRate());
}

void GBHardwareState::SetShield(const GBPower power) {
	shield = clamp(power, GBNumber(0), MaxShield());}

void GBHardwareState::Act(GBRobot * robot, GBWorld * world) {
// death check
	if ( armor <= 0 || robot->dead ) {
		robot->dead = true;
		world->AddObjectNew(new GBCorpse(robot->Position(), robot->Velocity(),
			robot->Biomass() * kCorpsePerBiomass, robot->Type(), robot->LastHit()));
		world->AddObjectNew(new GBExplosion(robot->Position(), robot->Owner(),
			(robot->Biomass() * kDeathExplosionDamagePerBiomass + spec->Bomb()) * robot->ShieldFraction()));
		return;
	}
// energy intake
	energy += SolarCells();
	robot->Owner()->ReportAutotrophy(SolarCells());
	eater = spec->Eater();
// engine
	if ( enginePower > 0 ) {
		GBSpeed effective = max(robot->Speed(), kEngineMinEffectiveSpeed);
		GBVector delta = engineVelocity - robot->Velocity();
		GBPower power = delta.Norm() * robot->Mass() * effective / kEngineEfficiency;
		if ( power ) {
			power = UseEnergyUpTo(min(power, enginePower));
			robot->Owner()->Scores().Expenditure().ReportEngine(power);
			robot->PushBy(delta * (power * kEngineEfficiency / effective / delta.Norm()));
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
	if ( repairRate ) {
		GBEnergy repairCost = UseEnergyUpTo(min(repairRate, (MaxArmor() - armor) * kRunningCostPerRepair));
		robot->Owner()->Scores().Expenditure().ReportRepairs(repairCost);
		armor += repairCost / kRunningCostPerRepair;
		if ( gRandoms.Boolean(repairCost * 0.5) )
			world->AddObjectNew(new GBBlasterSpark(robot->Position() + gRandoms.Vector(robot->Radius())));
	}
// do shield
	if ( shield ) {
		GBEnergy shieldUsed = UseEnergyUpTo(shield);
		robot->Owner()->Scores().Expenditure().ReportShield(shieldUsed);
		actualShield += shieldUsed;
	}
	actualShield = max(actualShield - kShieldDecayPerMass * robot->Mass() - kShieldDecayPerShield * actualShield, 0);
// lose excess energy
	if ( energy > MaxEnergy() ) {
		robot->Owner()->Scores().Expenditure().ReportWasted(energy - MaxEnergy());
		energy = min(energy, MaxEnergy());
	}
}
