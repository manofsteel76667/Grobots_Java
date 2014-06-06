// GBSideDebugger.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBSideDebugger_h
#define GBSideDebugger_h

#include "GBView.h"
#include "GBModel.h"

class GBSide;
class GBWorld;

class GBSideDebuggerView : public GBView {
	const GBWorld & world;
	GBChangeCount worldChanges;
	const GBSide * lastSideDrawn;
	short pane;
	short lastDrawnPane;
	
	void DrawSharedMemory(GBRect & box);

	GBNumber GetSM(int index, bool * worked, const GBSide * side) const;

public:
	explicit GBSideDebuggerView(const GBWorld & wrld);
	~GBSideDebuggerView();
// drawing
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;
	void Draw();
// other view stuff
	short PreferredWidth() const;
	short PreferredHeight() const;
	const string Name() const;
	void AcceptKeystroke(const char what);
};

#endif
