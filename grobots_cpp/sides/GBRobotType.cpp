// GBRobotType.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRobotType.h"
#include "GBErrors.h"
#include "GBBrainSpec.h"
#include "GBSide.h"

const GBMass kStandardMass = 20;

GBRobotType::GBRobotType() {
	throw GBBadConstructorError();
}

GBRobotType::GBRobotType(GBSide * owner)
	: side(owner),
	id(0),
	color(), name(),
	decoration(rdNone), decorationColor(GBColor::black),
	hardware(), brain(nil),
	next(nil),
	population(0), biomass(0)
{}

GBRobotType::~GBRobotType() {
	delete brain;
}

GBRobotType * GBRobotType::Copy(GBSide * side) const {
	GBRobotType * type = new GBRobotType(side);
	type->name = name;
	type->SetColor(color);
	type->SetDecoration(decoration, decorationColor);
	type->hardware = hardware;
	type->brain = brain->Copy();
	return type;
}

void GBRobotType::ResetSampledStatistics() {
	population = 0;
	biomass = 0;
}

void GBRobotType::ReportRobot(GBEnergy botBiomass) {
	population++;
	biomass += botBiomass;
}

long GBRobotType::Population() const { return population; }

long GBRobotType::Biomass() const { return biomass.Round(); }

GBSide * GBRobotType::Side() const { return side; }

const string & GBRobotType::Name() const { return name; }
void GBRobotType::SetName(const string & newname) {
	name = newname;
	Changed();
}

string GBRobotType::Description() const {
	const string & sidename = side->Name();
	return sidename + (sidename[sidename.size() - 1] == 's' ? "' " : "'s ") + name;
}

long GBRobotType::ID() const { return id; }
void GBRobotType::SetID(long newid) { id = newid; }

GBColor GBRobotType::Color() const { return color; }
void GBRobotType::SetColor(const GBColor & newcolor) {
	color = newcolor;
	Changed();
}

GBRobotDecoration GBRobotType::Decoration() const { return decoration; }
GBColor GBRobotType::DecorationColor() const { return decorationColor; }
void GBRobotType::SetDecoration(GBRobotDecoration dec, const GBColor & col) {
	decoration = dec;
	decorationColor = col;
}

GBHardwareSpec & GBRobotType::Hardware() { return hardware; }
const GBHardwareSpec & GBRobotType::Hardware() const { return hardware; }

GBBrainSpec * GBRobotType::Brain() const { return brain; }

void GBRobotType::SetBrain(GBBrainSpec * spec) {
	spec->Check();
	if ( brain )
		delete brain;
	brain = spec;
	Changed();
}

GBBrain * GBRobotType::MakeBrain() const {
	if ( ! brain ) {
		return nil;
	}
	return brain->MakeBrain();
}

void GBRobotType::Recalculate() {
	hardware.Recalculate();
	cost = hardware.Cost() + (brain ? brain->Cost() : GBNumber(0));
	mass = hardware.Mass() + (brain ? brain->Mass() : GBNumber(0));
	Changed();
}

GBEnergy GBRobotType::Cost() const { return cost; }
GBEnergy GBRobotType::Mass() const { return mass; }

GBNumber GBRobotType::MassiveDamageMultiplier(const GBMass mass) const {
 	GBNumber multiplier(1);
	if ( mass > kStandardMass ) multiplier += (mass - kStandardMass) / 50;
	return multiplier;
}

