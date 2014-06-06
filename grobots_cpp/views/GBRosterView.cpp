// GBRosterView.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBRosterView.h"
#include "GBSide.h"
#include "GBWorld.h"
#include "GBStringUtilities.h"

const short kFramecounterHeight = 15;
const short kSideBoxHeight = 17;
const short kPopulationWidth = 30;


GBRosterView::GBRosterView(GBWorld & wrld)
	: GBListView(),
	world(wrld),
	worldChanges(-1), lastSideSelected(nil), numSides(0),
	lastFrame(0), lastTime(-1)
{}

void GBRosterView::Draw() {
	GBListView::Draw();
// record
	worldChanges = world.ChangeCount();
	lastSideSelected = world.SelectedSide();
	numSides = world.CountSides();
}

GBMilliseconds GBRosterView::RedrawInterval() const {
	return 1500;
}

bool GBRosterView::InstantChanges() const {
	return lastSideSelected != world.SelectedSide() || numSides != world.CountSides() || world.CurrentFrame() < lastFrame;
	// FIXME: when sides reloaded
}

bool GBRosterView::DelayedChanges() const {
	return worldChanges != world.ChangeCount();
}

short GBRosterView::PreferredWidth() const {
	return 270;
}

const string GBRosterView::Name() const {
	return "Roster";
}

long GBRosterView::Items() const {
	return world.CountSides();
}

short GBRosterView::HeaderHeight() const {
	return kFramecounterHeight;
}

short GBRosterView::ItemHeight() const {
	return kSideBoxHeight;
}

void GBRosterView::DrawHeader(const GBRect & box) {
	DrawBox(box);
	DrawStringLeft(string("Frame ") + ToString(world.CurrentFrame()),
		box.left + 5, box.top + 12, 10);
	string status = string(world.tournament ? "tournament " : "") + (world.running ? "running" : "paused");
	if ( world.running && lastTime >= 0 && world.CurrentFrame() > lastFrame ) {
		long frames = world.CurrentFrame() - lastFrame;
		long ms = Milliseconds() - lastTime; //TODO use GBView::lastDrawn instead
		if (ms)
			status += string(" at ") + ToString((frames * 1000 + 500) / ms) + " fps";
	}
	DrawStringRight(status, box.right - 5, box.top + 12, 10);
	lastFrame = world.CurrentFrame();
	lastTime = Milliseconds();
}

void GBRosterView::DrawItem(long index, const GBRect & box) {
	const GBSide * side = world.GetSide(index);
	bool selected = side == world.SelectedSide();
	DrawBox(box, selected);
	if ( ! side ) return;
// draw name
	DrawStringLeft(side->Name(), box.left + 30, box.top + 13, 12, selected ? GBColor::white : GBColor::black);
// draw ID and stats
	if ( side->Scores().Seeded() ) {
		DrawStringRight(ToString(side->ID()) + '.', box.left + 25, box.top + 13, 12, side->Color().ContrastingTextColor());
		if ( side->Scores().Population() ) {
			if ( side->Scores().Sterile() )
				DrawStringRight(string("Sterile at ") + ToString(side->Scores().SterileTime()),
					box.right - 4, box.top + 12, 10, GBColor::purple);
			else {
				DrawStringRight(ToPercentString(side->Scores().BiomassFraction()), box.right - kPopulationWidth, box.top + 13,
					12, (selected ? GBColor::white : GBColor::black));
				DrawStringRight(ToString(side->Scores().Population()),
					box.right - 4, box.top + 12, 10, GBColor::blue);
			}
		} else {
			DrawStringRight(string("Extinct at ") + ToString(side->Scores().ExtinctTime()),
				box.right - 4, box.top + 12, 10, GBColor::darkGray);
		}
	}
}

void GBRosterView::ItemClicked(long index) {
	world.SelectSide(index ? world.GetSide(index) : nil);
}

