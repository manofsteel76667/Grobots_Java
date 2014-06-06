// GBApplication.h
// Top-level interface class.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBApplication_h
#define GBApplication_h


#include "GBViewsApplication.h"
#include "GBTypes.h"
#include "GBWorld.h"
#include "GBSide.h"


class GBRobot;
class GBPortal;
class GBMiniMapView;
class GBDebuggerView;
class GBScoresView;

class GBApplication : public GBViewsApplication {
	GBWorld world;
// the views and windows
	GBPortal * portal;
	GBMiniMapView * minimap;
	GBDebuggerView * debugger;
	GBScoresView * scores;
	GBWindow * minimapWindow;
	GBWindow * rosterWindow;
	GBWindow * scoresWindow;
	GBWindow * typeWindow;
	GBWindow * aboutWindow;
	GBWindow * tournamentWindow;	
	GBWindow * debuggerWindow;	
	GBWindow * sideDebuggerWindow;
	void SetupMenus();
	void DoLoadSide();
	void DoReloadSide();
	void DoRulesDialog();
public:
#if WINDOWS
	GBApplication(HINSTANCE hInstance, int showCmd);
#else
	GBApplication();
#endif
	~GBApplication();
	
	void AdjustMenus();
	void HandleMenuSelection(int item);
	void Process();
	void Redraw();
#if MAC
	long SleepTime();
	void OpenFile(FSSpec & file);
#endif
	void Quit();
};

#endif
