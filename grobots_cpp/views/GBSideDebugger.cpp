// GBSideDebugger.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBWorld.h"
#include "GBSideDebugger.h"
#include "GBSide.h"
#include "GBWorld.h"
#include "GBStringUtilities.h"
#include "GBErrors.h"


GBSideDebuggerView::GBSideDebuggerView(const GBWorld & wrld)
	: GBView(), world(wrld),
	worldChanges(-1), pane(0), lastDrawnPane(-1), lastSideDrawn(nil)
{}

GBSideDebuggerView::~GBSideDebuggerView() {}

GBMilliseconds GBSideDebuggerView::RedrawInterval() const {
	return 500;
}

bool GBSideDebuggerView::InstantChanges() const {
	return lastSideDrawn != world.SelectedSide() || lastDrawnPane != pane;
}

bool GBSideDebuggerView::DelayedChanges() const {
	return worldChanges != world.ChangeCount();
}

GBNumber GBSideDebuggerView::GetSM(int index, bool * worked, const GBSide * side) const {
	try {
		GBNumber read = side->ReadSharedMemory(index);
		*worked = true;
		return read;
	} catch ( GBError & ) {
		*worked = false;
		return 0;
	}
}

void GBSideDebuggerView::DrawSharedMemory(GBRect & box) {
	DrawBox(box);
	short numsWide = world.BackgroundTilesX();
	short numsHigh = world.BackgroundTilesY();
	short minNumThisPane = numsWide * numsHigh * pane; //actually the min is 1 more, because of silly 1-based numbering
	
	const GBSide * side = world.SelectedSide();
	
// determine range, for color-coding
	GBNumber smallest = kInfinity;
	GBNumber largest = -kInfinity;
	int i;
	for (i = 0; i < numsHigh; i++)
		for (int j = 1; j <= numsWide; j++) {
			bool worked = false;
			GBNumber read = GetSM(minNumThisPane + i*numsWide + j, &worked, side);
			if(worked) {
				smallest = min(smallest, read);
				largest = max(largest, read);
			}
		}
	GBNumber range = max(largest - smallest, kEpsilon);
// draw main
	short curY = box.top + 16;
	for (i = numsHigh; i >= 0; i--) { //starts at numsHigh for header row
		short curX = box.left + 5;
		if (i != numsHigh) {
			DrawStringLeft(ToString(minNumThisPane + i*numsWide) + ":", curX, curY, 10, GBColor(0), true);
		} else {
			DrawStringLeft("Shared Mem", curX, curY, 10, GBColor(0,0,0), true);
		}
		curX += 30;

		for (int j = 1; j <= numsWide; j++) {
			curX += 75;
			if (numsHigh == i) {
				DrawStringRight(ToString(j), curX, curY, short(10), GBColor(0), true);
			} else {
				bool worked = false;
				short curNum = minNumThisPane + i*numsWide + j;
				GBNumber read = GetSM(curNum, &worked, side);

				GBColor color = GBColor(0,0,1).Mix(float(((read - smallest) / range).ToDouble()), GBColor(1,0,0));
				if (worked)
					DrawStringRight(ToString(read, 2, false), curX, curY, short(10), color);
			}
		}
		curY += 15;
	}
}

void GBSideDebuggerView::Draw() {
	DrawBackground();
	if ( ! world.SelectedSide() ) {
		DrawStringLeft("No side selected", 4, 20, 12);
	} else {
		GBRect box;
		box.left = kEdgeSpace;
		box.top = kEdgeSpace;
		box.right = Width() - kEdgeSpace;
		box.bottom = Height() - kEdgeSpace;
		DrawSharedMemory(box);
	}
// record
	lastDrawnPane = pane;
	lastSideDrawn = world.SelectedSide();
	worldChanges = world.ChangeCount();
}

short GBSideDebuggerView::PreferredWidth() const {
	return 820;
}

short GBSideDebuggerView::PreferredHeight() const {
	if ( world.SelectedSide() ) {
		return 190;
	} else
		return 40;
}

const string GBSideDebuggerView::Name() const {
	return "Side Debugger";
}

void GBSideDebuggerView::AcceptKeystroke(const char what) {
	if (what == 'n' || what == 'N') {
		pane = pane + 1;
	} else if (what == 'p' || what == 'P') {
		pane = pane <= 0 ? 0 : pane - 1;
	} else if ( what == '0') {
		pane = 0;
	}
}
