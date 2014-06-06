// GBRobotTypeView.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRobotType.h"
#include "GBRobotTypeView.h"
#include "GBSide.h"
#include "GBBrainSpec.h"
#include "GBStringUtilities.h"
#include "GBWorld.h"


const short kTypeStatsWidth = 80;
const short kTypePopulationLeft = 5;
const short kTypeBiomassLeft = 80;
const short kHardwareNameLeft = 5;
const short kHardwareArgumentsLeft = 85;
const short kHardwareCostRight = 60;
const short kHardwarePercentRight = 38;
const short kHardwareMassRight = 5;

const GBRobotType * GBRobotTypeView::SelectedType() const {
	const GBSide * side = world.SelectedSide();
	if ( ! side ) return nil;
	const GBRobotType * type = side->SelectedType();
	if ( ! type && side->CountTypes() == 1 )
		type = side->GetFirstType();
	return type;
}

void GBRobotTypeView::DrawHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const string & arg1, const string & arg2, const string & arg3, const string & arg4,
		const GBNumber cost, const GBNumber mass) {
	const GBHardwareSpec & hw = SelectedType()->Hardware();
	base += (base > 0 ? box.top : box.bottom);
// name and args
	const GBColor & textcolor = cost ? color : GBColor::lightGray;
	DrawStringLeft(name, box.left + kHardwareNameLeft, base, 9, textcolor);
	if ( cost )
		DrawStringLeft(arg1 + ' ' + arg2 + ' ' + arg3 + ' ' + arg4,
			box.left + kHardwareArgumentsLeft, base, 9, textcolor);
// cost and mass
	const GBColor & numcolor = (cost ? GBColor::black : GBColor::lightGray);
	DrawStringRight(ToString(cost, cost < 10 ? 1 : 0), box.right - kHardwareCostRight, base, 9, numcolor);
	if ( cost )
		DrawStringRight(ToString(cost / hw.BaseCost() * 100, 0),
						box.right - kHardwarePercentRight, base, 9, GBColor::gray);
	DrawStringRight(ToString(mass, 2), box.right - kHardwareMassRight, base, 9, numcolor);
}

void GBRobotTypeView::DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg,
		const GBNumber cost, const GBNumber mass) {
	DrawHardwareLine(box, base, name, color, ToString(arg, 3), "", "", "", cost, mass);
}

void GBRobotTypeView::DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg1, const GBNumber arg2,
		const GBNumber cost, const GBNumber mass) {
	DrawHardwareLine(box, base, name, color, ToString(arg1, 3), ToString(arg2, 3), "", "", cost, mass);
}

void GBRobotTypeView::DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg1, const GBNumber arg2, const GBNumber arg3,
		const GBNumber cost, const GBNumber mass) {
	DrawHardwareLine(box, base, name, color,
		ToString(arg1, 3), ToString(arg2, 3), ToString(arg3, 3), "", cost, mass);
}

void GBRobotTypeView::DrawHardwareSummaryLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber cost, const GBNumber mass) {
	base += (base > 0 ? box.top : box.bottom);
	DrawStringLeft(name, box.left + kHardwareNameLeft, base, 10, color, true);
	DrawStringRight(ToString(cost, 0), box.right - kHardwareCostRight, base, 10, color, true);
	DrawStringRight(ToString(mass, 2), box.right - kHardwareMassRight, base, 10, color, true);
}

GBRobotTypeView::GBRobotTypeView(const GBWorld & targ)
	: GBListView(),
	world(targ),
	lastSideDrawn(nil), typeID(-1), lastDrawn(-1)
{}

GBRobotTypeView::~GBRobotTypeView() {}

void GBRobotTypeView::Draw() {
	GBListView::Draw();
// record
	lastSideDrawn = world.SelectedSide();
	typeID = world.SelectedSide() ? world.SelectedSide()->SelectedTypeID() : -1;
	lastDrawn = world.ChangeCount();
}

short GBRobotTypeView::HeaderHeight() const { return 17; }
short GBRobotTypeView::ItemHeight() const { return 25; }
short GBRobotTypeView::FooterHeight() const {
	return SelectedType() ? 300 : 0;
}

void GBRobotTypeView::DrawHeader(const GBRect & box) {
	const GBSide * side = world.SelectedSide();
	if ( side ) {
		DrawBox(box);
		DrawStringLeft(side->Name(), box.left + 5, box.top + 13,
			12, side->Color().ContrastingTextColor());
		DrawStringRight(side->Author(), box.right - 5, box.top + 13,
			12, GBColor::black);
	} else
		DrawStringLeft("No side selected", box.left + 5, box.top + 13,
			12, GBColor::black);
}

void GBRobotTypeView::DrawItem(long index, const GBRect & box) {
	const GBSide * side = world.SelectedSide();
	if ( ! side ) return;
	const GBRobotType * type = side->GetType(index);
	bool selected = type == side->SelectedType();
	DrawBox(box, selected);
	if ( ! type ) return;
// draw ID and name and color
	DrawStringLeft(ToString(type->ID()) + '.', box.left + 3, box.top + 13, 12, type->Color());
	DrawStringLeft(type->Name(), box.left + 20, box.top + 13,
		12, selected ? GBColor::white : GBColor::black);
// stuff
	DrawStringPair("Cost:", ToString(type->Cost(), 0),
		box.right - kTypeStatsWidth + 5, box.right - 2, box.top + 11,
		9, selected ? GBColor::green : GBColor::darkGreen);
	const GBHardwareSpec &hw = type->Hardware();
	DrawStringRight(ToPercentString(hw.GrowthCost() / hw.BaseCost(), 0) + " economy, "
					+ ToPercentString(hw.CombatCost() / hw.BaseCost(), 0) + " combat",
					box.right - 2, box.bottom - 3, 9, selected ? GBColor::white : GBColor::black);
	//DrawStringPair("Mass:", ToString(type->Mass(), 1),
	//	box.right - kTypeStatsWidth + 5, box.right - 2, box.bottom - 3,
	//	9, selected ? GBColor::white : GBColor::black);
	if ( side->Scores().Seeded() ) {
		DrawStringLongPair("Population:", type->Population(),
			box.left + kTypePopulationLeft, box.left + kTypeBiomassLeft - 4, box.bottom - 3,
			9, selected ? GBColor::white : GBColor::blue);
		//DrawStringLongPair("Biomass:", type->Biomass(),
		//	box.left + kTypeBiomassLeft, box.right - kTypeStatsWidth - 4, box.bottom - 3,
		//	9, selected ? GBColor::green : GBColor::darkGreen);
	}
}

void GBRobotTypeView::DrawFooter(const GBRect & box) {
	const GBRobotType * type = SelectedType();
	if ( ! type ) return;
	DrawBox(box);
// hardware
	const GBHardwareSpec & hw = type->Hardware();
	DrawStringLeft("Hardware:", box.left + kHardwareNameLeft, box.top + 12, 10, GBColor::black, true);
	DrawStringRight("Cost", box.right - kHardwareCostRight, box.top + 12, 10, GBColor::black, true);
	DrawStringRight("%", box.right - kHardwarePercentRight, box.top + 12, 10, GBColor::black, true);
	DrawStringRight("Mass", box.right - kHardwareMassRight, box.top + 12, 10, GBColor::black, true);
	DrawLine(box.left, box.top + 15, box.right, box.top + 15, GBColor::black);
// basic
	DrawHardwareLine(box, 25, "chassis", GBColor::black,
		"", "", "", "", hw.ChassisCost(), hw.ChassisMass());
	DrawNumericHardwareLine(box, 40, "processor", GBColor::blue,
		hw.Processor(), hw.Memory(), hw.ProcessorCost(), hw.ProcessorMass());
	DrawNumericHardwareLine(box, 55, "engine", GBColor::black,
		hw.Engine(), hw.EngineCost(), hw.EngineMass());
	DrawNumericHardwareLine(box, 65, "constructor", GBColor::darkGreen,
		hw.constructor.Rate(), hw.constructor.Cost(), hw.constructor.Mass());
// energy-related
	DrawNumericHardwareLine(box, 80, "energy", GBColor::darkGreen,
		hw.MaxEnergy(), hw.InitialEnergy(), hw.EnergyCost(), hw.EnergyMass());
	DrawNumericHardwareLine(box, 90, "solar-cells", GBColor::darkGreen,
		hw.SolarCells(), hw.SolarCellsCost(), hw.SolarCellsMass());
	DrawNumericHardwareLine(box, 100, "eater", GBColor::darkGreen,
		hw.Eater(), hw.EaterCost(), hw.EaterMass());
	DrawNumericHardwareLine(box, 110, "syphon", GBColor::blue,
		hw.syphon.Power(), hw.syphon.Range(), hw.syphon.Cost(), hw.syphon.Mass());
// sensors
	DrawNumericHardwareLine(box, 125, "robot-sensor", GBColor::blue,
		hw.sensor1.Range(), hw.sensor1.NumResults(), hw.sensor1.Cost(), hw.sensor1.Mass());
	DrawNumericHardwareLine(box, 135, "food-sensor", GBColor::blue,
		hw.sensor2.Range(), hw.sensor2.NumResults(), hw.sensor2.Cost(), hw.sensor2.Mass());
	DrawNumericHardwareLine(box, 145, "shot-sensor", GBColor::blue,
		hw.sensor3.Range(), hw.sensor3.NumResults(), hw.sensor3.Cost(), hw.sensor3.Mass());
// defense
	DrawNumericHardwareLine(box, 160, "armor", GBColor::black,
		hw.Armor(), hw.ArmorCost(), hw.ArmorMass());
	DrawNumericHardwareLine(box, 170, "repair-rate", GBColor::darkGray,
		hw.RepairRate(), hw.RepairCost(), hw.RepairMass());
	DrawNumericHardwareLine(box, 180, "shield", GBColor::blue,
		hw.Shield(), hw.ShieldCost(), hw.ShieldMass());
// weapons
	DrawNumericHardwareLine(box, 195, "blaster", GBColor::black,
		hw.blaster.Damage(), hw.blaster.Range(), hw.blaster.ReloadTime(), hw.blaster.Cost(), hw.blaster.Mass());
	DrawNumericHardwareLine(box, 205, "grenades", GBColor::black,
		hw.grenades.Damage(), hw.grenades.Range(), hw.grenades.ReloadTime(), hw.grenades.Cost(), hw.grenades.Mass());
	DrawNumericHardwareLine(box, 215, "force-field", GBColor::blue,
		hw.forceField.Power(), hw.forceField.Range(), hw.forceField.Cost(), hw.forceField.Mass());
	DrawNumericHardwareLine(box, 225, "enemy-syphon", GBColor::blue,
		hw.enemySyphon.Power(), hw.enemySyphon.Range(), hw.enemySyphon.Cost(), hw.enemySyphon.Mass());
	DrawNumericHardwareLine(box, 235, "bomb", GBColor::red,
		hw.Bomb(), hw.BombCost(), hw.BombMass());
// totals and brain
	DrawHardwareSummaryLine(box, -50, "Ordinary hardware:", GBColor::black,
		hw.BaseCost(), hw.Mass() - hw.CoolingMass());
	DrawHardwareLine(box, -40,
		ToPercentString(hw.CoolingCost() / hw.BaseCost(), 0) + " cooling charge",
		hw.BaseCost() > 1000 ? GBColor::red : GBColor::black, "", "", "", "", hw.CoolingCost(), hw.CoolingMass());
	const GBBrainSpec * brain = type->Brain();
	if ( brain )
		DrawHardwareLine(box, -28, "code", GBColor::black, "", "", "", "", brain->Cost(), brain->Mass());
	else
		DrawStringLeft("No brain", box.left + kHardwareNameLeft, box.bottom - 30, 10, GBColor::lightGray);
	DrawLine(box.left, box.bottom - 25, box.right, box.bottom - 25, GBColor::black);
	DrawHardwareSummaryLine(box, -14, "Total:", GBColor::black, type->Cost(), type->Mass());
	GBNumber damageMult = type->MassiveDamageMultiplier(type->Mass());
	GBNumber pregnantMult = type->MassiveDamageMultiplier(type->Mass() * 2);
	DrawStringLeft("Mass-based damage multiplier: " + ToPercentString(damageMult, 0)
		+ ((hw.constructor.Rate() && pregnantMult > 1) ? " to " + ToPercentString(pregnantMult) : ""),
		box.left + kHardwareNameLeft, box.bottom - 4, 9, damageMult > 1 ? GBColor::red : GBColor::lightGray);
}

GBMilliseconds GBRobotTypeView::RedrawInterval() const {
	return 2000;
}

bool GBRobotTypeView::InstantChanges() const {
	const GBSide * side = world.SelectedSide();
	return lastSideDrawn != side || side && typeID != side->SelectedTypeID();
}

bool GBRobotTypeView::DelayedChanges() const {
	return lastDrawn != world.ChangeCount();
}

long GBRobotTypeView::Items() const {
	return world.SelectedSide() ? world.SelectedSide()->CountTypes() : 0;
}

short GBRobotTypeView::PreferredWidth() const {
	return 250;
}

const string GBRobotTypeView::Name() const {
	return "Types";
}

void GBRobotTypeView::ItemClicked(long index) {
	const GBSide * side = world.SelectedSide();
	if ( ! side || ! index ) return;
	side->SelectType(side->GetType(index));
}

