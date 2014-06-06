// GBDebugger.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRobot.h"
#include "GBWorld.h"
#include "GBDebugger.h"
#include "GBStackBrain.h"
#include "GBRobotType.h"


const short kStatusBoxHeight = 30;
const short kPCBoxHeight = 95;
const short kStackBoxHeight = 65;
const short kPrintBoxHeight = 15;
const short kHardwareBoxWidth = 150;
const short kHardwareBoxHeight = 300;
const short kProfileBoxWidth = 110;
const short kProfileBoxHeight = GBWORLD_PROFILING ? 87 : 25;

void GBDebuggerView::DrawStatusBox(const GBRect & box) {
	DrawBox(box);
	DrawStringLeft(target->Description(), box.left + 4, box.top + 13, 12);
	const GBBrain * brain = target->Brain();
	const GBStackBrain * sbrain = dynamic_cast<const GBStackBrain *>(brain);
	DrawStringLeft(brain ? (sbrain ? "Stack brain." : "Unknown brain.") : "No brain.",
		box.left + 5, box.bottom - 4, 10);
	if ( brain )
		DrawStringPair("Status:", brain->StatusName(),
			(box.left * 2 + box.right + 6) / 3, (box.left + box.right * 2 - 6) / 3, box.bottom - 4,
			10, brain->Status() == bsOK ? GBColor::darkGreen :
				brain->Status() == bsError ? GBColor::darkRed : GBColor::black);
	if ( sbrain )
		DrawStringLongPair("Remaining:", sbrain->Remaining(),
			(box.left + box.right * 2 + 6) / 3, box.right - 5, box.bottom - 4, 10);
}

void GBDebuggerView::DrawPCBox(const GBRect & box, const GBStackBrain * brain) {
	DrawBox(box);
	GBStackAddress pc = brain->PC();
	DrawStringPair("PC:", brain->AddressLastLabel(pc) + " (line " + ToString(brain->PCLine()) + ')',
		box.left + 3, box.right - 3, box.top + 11, 10, GBColor::black, true);
	for ( long i = -4; i <= 3; i ++ )
		if ( brain->ValidAddress(pc + i) )
			DrawStringPair(brain->AddressName(pc + i) + ':', brain->DisassembleAddress(pc + i),
				box.left + 3, box.right - 3, box.bottom - 34 + 10 * i, 10,
				i == 0 ? GBColor::blue : GBColor::black);
}

void GBDebuggerView::DrawStackBox(const GBRect & box, const GBStackBrain * brain) {
	DrawBox(box);
	DrawStringLeft("Stack:", box.left + 3, box.top + 11, 10, GBColor::black, true);
	long height = brain->StackHeight();
	if ( height ) {
		for ( long i = 0; i < 5 && i < height; i ++ )
			DrawStringPair(ToString(height - i) + ':', ToString(brain->StackAt(height - i - 1)),
				box.left + 3, box.right - 3, box.bottom - 44 + 10 * i, 10);
	} else
		DrawStringRight("empty", box.right - 3, box.top + 31, 10);
}

void GBDebuggerView::DrawReturnStackBox(const GBRect & box, const GBStackBrain * brain) {
	DrawBox(box);
	DrawStringLeft("Return stack:", box.left + 3, box.top + 11, 10, GBColor::black, true);
	long height = brain->ReturnStackHeight();
	if ( height ) {
		for ( long i = 0; i < 5 && i < height; i ++ )
			DrawStringPair(ToString(height - i) + ':',
				brain->AddressLastLabel((brain->ReturnStackAt(height - i - 1))),
				box.left + 3, box.right - 3, box.bottom - 44 + 10 * i, 10);
	} else
		DrawStringRight("empty", box.right - 3, box.top + 31, 10);
}

void GBDebuggerView::DrawVariablesBox(const GBRect & box, const GBStackBrain * brain) {
	GBSymbolIndex vars = brain->NumVariables();
	GBSymbolIndex vvars = brain->NumVectorVariables();
	if ( vars || vvars ) {
		DrawBox(box);
		short y = box.top + 11;
		DrawStringLeft("Variables:", box.left + 3, y, 10, GBColor::black, true);
		GBSymbolIndex i;
		for ( i = 0; i < vars; i ++ ) {
			y += 10;
			DrawStringPair(brain->VariableName(i), ToString(brain->ReadVariable(i)),
				box.left + 3, box.right - 3, y, 10);
		}
		for ( i = 0; i < vvars; i ++ ) {
			y += 10;
			DrawStringPair(brain->VectorVariableName(i), ToString(brain->ReadVectorVariable(i)),
				box.left + 3, box.right - 3, y, 10);
		}
	} else
		DrawStringLeft("No variables", box.left + 3, box.top + 11, 10);
}

void GBDebuggerView::DrawPrintBox(const GBRect & box, const GBStackBrain * brain) {
	const std::string & print = brain->LastPrint();
	if ( print != "none" )
		DrawBox(box);
	DrawStringPair("Last print:", print, box.left + 3, box.right - 3, box.top + 11, 10);
}

void GBDebuggerView::DrawHardwareBox(const GBRect & box) {
	const GBHardwareState & hw = target->hardware;
	const short right = box.right - 3;
	const short left = box.left + 3;
	DrawBox(box);
	DrawStringPair("Mass:", ToString(target->Mass(), 1), left, right, box.top + 11, 10);
	DrawStringPair("Position:", ToString(target->Position(), 1), left, right, box.top + 21, 10);
	DrawStringPair("Velocity:", ToString(target->Velocity(), 2), left, right, box.top + 31, 10);
	DrawStringPair("Speed:", ToString(target->Speed(), 2), left, right, box.top + 41, 10);
	if (hw.EnginePower())
		DrawStringPair("Engine vel:", ToString(hw.EngineVelocity(), 2), left, right, box.top + 51, 10);
	DrawStringPair("Energy:", ToString(hw.Energy(), 1), left, right, box.top + 65, 10, GBColor::darkGreen);
	DrawStringPair("Eaten:", ToString(hw.Eaten(), 1), left, right, box.top + 75, 10, GBColor::darkGreen);
	DrawStringPair("Armor:", ToString(hw.Armor()) + '/' + ToString(hw.MaxArmor()),
		left, right, box.top + 85, 10);
	if (hw.ActualShield())
		DrawStringPair("Shield:", ToString(hw.ActualShield()) + " ("
			+ ToPercentString(target->ShieldFraction()) + ')', left, right, box.top + 95,
			10, GBColor::blue);
	if ( hw.constructor.Type() ) {
		DrawStringLeft("Constructor", left, box.top + 121, 10, GBColor::black, true);
		DrawStringPair("type:", hw.constructor.Type()->Name(), left, right, box.top + 131,
			10, hw.constructor.Type()->Color().ContrastingTextColor());
		DrawStringPair("progress:", ToString(hw.constructor.Progress(), 0)
				+ '/' + ToString(hw.constructor.Type()->Cost(), 0),
			left, right, box.top + 141, 10);
	}
	//sensor times? result details?
	if (hw.sensor1.Radius())
		DrawStringLongPair("robot-found:", hw.sensor1.NumResults(), left, right, box.top + 161, 10);
	if (hw.sensor2.Radius())
		DrawStringLongPair("food-found:", hw.sensor2.NumResults(), left, right, box.top + 171, 10);
	if (hw.sensor3.Radius())
		DrawStringLongPair("shot-found:", hw.sensor3.NumResults(), left, right, box.top + 181, 10);
	DrawStringLeft("Weapons:", left, box.top + 191, 10, GBColor::black, true);
	if (hw.blaster.Damage())
		DrawStringLongPair("blaster-cooldown:", hw.blaster.Cooldown(), left, right, box.top + 201, 10);
	if (hw.grenades.Damage())
		DrawStringLongPair("grenades-cooldown:", hw.grenades.Cooldown(), left, right, box.top + 211, 10);
	if (hw.forceField.MaxPower())
		DrawStringPair("force-field-angle:", ToString(hw.forceField.Angle(), 2), left, right, box.top + 221, 10);
	if (hw.syphon.MaxRate())
		DrawStringPair("syphoned:", ToString(hw.syphon.Syphoned(), 2) + '/'
			+ ToString(hw.syphon.Rate(), 2), left, right, box.top + 241, 10);
	if (hw.enemySyphon.MaxRate())
		DrawStringPair("enemy-syphoned:", ToString(hw.enemySyphon.Syphoned(), 2) + '/'
			+ ToString(hw.enemySyphon.Rate(), 2), left, right, box.top + 251, 10);
	if (target->flag)
		DrawStringPair("flag:", ToString(target->flag, 2), left, right, box.top + 271, 10);
}

void GBDebuggerView::DrawProfileBox(const GBRect & box) {
#if GBWORLD_PROFILING
	DrawBox(box);
	DrawStringLongPair("Total time:", world.TotalTime(), box.left + 5, box.right - 5, box.top + 13, 10);
	DrawStringLongPair("Simulation:", world.SimulationTime(), box.left + 5, box.right - 5, box.top + 23, 10);
	DrawStringLongPair("Move:", world.MoveTime(), box.left + 5, box.right - 5, box.top + 33, 10);
	DrawStringLongPair("Act:", world.ActTime(), box.left + 5, box.right - 5, box.top + 43, 10);
	DrawStringLongPair("Collide:", world.CollideTime(), box.left + 5, box.right - 5, box.top + 53, 10);
	DrawStringLongPair("Think:", world.ThinkTime(), box.left + 5, box.right - 5, box.top + 63, 10);
	DrawStringLongPair("Resort:", world.ResortTime(), box.left + 5, box.right - 5, box.top + 73, 10);
	DrawStringLongPair("Statistics:", world.StatisticsTime(), box.left + 5, box.right - 5, box.top + 83, 10);
	world.ResetTimes();
#endif
}

void GBDebuggerView::UpdateTarget() {
	if ( (GBObject *)target == world.Followed() ) return;
	if ( target ) target->RemoveDeletionListener(this);
	target = dynamic_cast<GBRobot *>(world.Followed());
	if ( target ) target->AddDeletionListener(this);
}

GBDebuggerView::GBDebuggerView(GBWorld & wld)
	: target(nil),
	world(wld), worldChanges(-1), redrawAnyway(true)
{}

GBDebuggerView::~GBDebuggerView() {
	if (target) target->RemoveDeletionListener(this);
}

bool GBDebuggerView::Active() const {
	const_cast<GBDebuggerView *>(this)->UpdateTarget();
	return target && target->Brain();
}

void GBDebuggerView::StartStopBrain() {
	UpdateTarget();
	if ( ! target ) return;
	GBBrain * brain = target->Brain();
	if ( ! brain ) return;
	brain->SetStatus(brain->Status() == bsOK ? bsStopped : bsOK);
}

bool GBDebuggerView::Step() {
	UpdateTarget();
	if ( ! target ) return true;
	GBBrain * brain = target->Brain();
	if ( ! brain ) return true;
	if ( ! brain->Ready() ) return true;
	redrawAnyway = true;
	brain->Step(target, &world);
	return ! brain->Ready();
}

void GBDebuggerView::ReportDeletion(const GBDeletionReporter * deletee) {
	if ( deletee == (const GBDeletionReporter *)target ) {
		target = nil;
		redrawAnyway = true;
	} else
		throw GBGenericError("Deletion reported on object GBDebuggerView isn't viewing.");
}

GBMilliseconds GBDebuggerView::RedrawInterval() const {
	return target ? 300 : 1000;
}

bool GBDebuggerView::InstantChanges() const {
	return redrawAnyway || (GBObject *)target != world.Followed();
}

bool GBDebuggerView::DelayedChanges() const {
	return world.ChangeCount() != worldChanges;
}

void GBDebuggerView::Draw() {
	UpdateTarget();
	DrawBackground();
	GBRect box;
	if ( ! target ) {
		DrawStringLeft("No robot selected", 4, 20, 12);
		box.top = kEdgeSpace;
		box.bottom = box.top + kProfileBoxHeight;
		box.right = Width() - kEdgeSpace;
		box.left = box.right - kProfileBoxWidth;
		DrawProfileBox(box);
	} else {
	// draw robot name
		box.left = box.top = kEdgeSpace;
		box.right = Width() - kEdgeSpace;
		box.bottom = box.top + kStatusBoxHeight;
		DrawStatusBox(box);
	// get brain
		const GBStackBrain * sbrain = dynamic_cast<GBStackBrain *>(target->Brain());
		if ( sbrain ) {
		// draw pc
			box.top = box.bottom + kEdgeSpace;
			box.bottom = box.top + kPCBoxHeight;
			box.right -= kHardwareBoxWidth + kEdgeSpace;
			DrawPCBox(box, sbrain);
		// draw stack
			box.top = box.bottom + kEdgeSpace;
			box.bottom = box.top + kStackBoxHeight;
			box.right = (Width() - kHardwareBoxWidth - kEdgeSpace) / 2;
			DrawStackBox(box, sbrain);
		// draw return stack
			box.left = (Width() - kHardwareBoxWidth + kEdgeSpace) / 2;
			box.right = Width() - kHardwareBoxWidth - kEdgeSpace * 2;
			DrawReturnStackBox(box, sbrain);
		// draw variables
			box.top = box.bottom + kEdgeSpace;
			box.bottom = box.top + (sbrain->NumVariables() + sbrain->NumVectorVariables()) * 10 + 15;
			box.left = kEdgeSpace;
			DrawVariablesBox(box, sbrain);
		// draw prints
			box.top = box.bottom + kEdgeSpace;
			box.bottom = box.top + kPrintBoxHeight;
			DrawPrintBox(box, sbrain);
		}
	// draw hardware
		box.top = kStatusBoxHeight + kEdgeSpace * 2;
		box.right = Width() - kEdgeSpace;
		box.left = box.right - kHardwareBoxWidth;
		box.bottom = box.top + kHardwareBoxHeight;
		DrawHardwareBox(box);
	}
// record
	worldChanges = world.ChangeCount();
	redrawAnyway = false;
}

short GBDebuggerView::PreferredWidth() const {return 350;}

short GBDebuggerView::PreferredHeight() const {
	if ( target ) {
		const GBStackBrain * sbrain = dynamic_cast<GBStackBrain *>(target->Brain());
		short brainheight = sbrain ? kPCBoxHeight + kStackBoxHeight + kPrintBoxHeight
				+ (sbrain->NumVariables() + sbrain->NumVectorVariables()) * 10 + 15
				+ 3 * kEdgeSpace : 0;
		return kStatusBoxHeight + (brainheight < kHardwareBoxHeight ?
			kHardwareBoxHeight : brainheight) + 3 * kEdgeSpace;
	} else
		return kProfileBoxHeight + kEdgeSpace * 2;
}

const string GBDebuggerView::Name() const {return "Debugger";}

void GBDebuggerView::AcceptClick(short x, short y, int /*clicks*/) {
	UpdateTarget();
	if ( target && x > Width() / 2 && x < Width() - kEdgeSpace
			&& y > kEdgeSpace + kStatusBoxHeight - 10 && y < kEdgeSpace + kStatusBoxHeight )
		StartStopBrain();
}

void GBDebuggerView::AcceptKeystroke(const char what) {
	switch (tolower(what)) {
		case 's': StartStopBrain(); break;
		case 'b':
			if ( Active() )
				Step();
			break;
		case 'f':
			world.AdvanceFrame();
			world.running = false;
			break;
		case 'r': world.running = true; break;
		case 'p': world.running = false; break;
		default: break;
	}
}
