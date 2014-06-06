// GBDebugger.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBDebugger_h
#define GBDebugger_h

#include "GBView.h"
#include "GBModel.h"

class GBRobot;
class GBWorld;
class GBStackBrain;

class GBDebuggerView : public GBView, public GBDeletionListener {
	GBRobot * target;
	GBWorld & world;
	GBChangeCount worldChanges;
	bool redrawAnyway;
// drawing various boxes
	void DrawStatusBox(const GBRect & box);
	void DrawPCBox(const GBRect & box, const GBStackBrain * brain);
	void DrawStackBox(const GBRect & box, const GBStackBrain * brain);
	void DrawReturnStackBox(const GBRect & box, const GBStackBrain * brain);
	void DrawVariablesBox(const GBRect & box, const GBStackBrain * brain);
	void DrawPrintBox(const GBRect & box, const GBStackBrain * brain);
	void DrawHardwareBox(const GBRect & box);
	void DrawProfileBox(const GBRect & box);
	void UpdateTarget();
public:
	explicit GBDebuggerView(GBWorld & wld);
	~GBDebuggerView();
//
	bool Active() const;
	void StartStopBrain();
	bool Step(); // return whether to advance frame
	void ReportDeletion(const GBDeletionReporter * deletee);
// drawing
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;
	void Draw();
// other view stuff
	short PreferredWidth() const;
	short PreferredHeight() const;
	const string Name() const;
	void AcceptClick(short x, short y, int clicks);
	void AcceptKeystroke(const char what);
};

#endif
