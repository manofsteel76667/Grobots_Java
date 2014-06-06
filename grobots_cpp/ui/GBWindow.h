// GBWindow.h
// cross-platform window class for GBView/GBViewsApplication
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBWindow_h
#define GBWindow_h

#if MAC && ! MAC_OS_X
#include <Windows.h>
#elif WINDOWS
#include <Windows.h>
#endif
#include "GBView.h"

// everything possible is delegated to the window's view.

#if WINDOWS
extern const char * kWindowClassName;
#endif

class GBViewsApplication;

class GBWindow {
	GBView * view;
	bool visible;
#if MAC
	WindowPtr window;
	bool showGrowBox;
	GBGraphics graphics;
#elif WINDOWS
public:
	HWND win;
	bool isMain;
	GBViewsApplication * app;
#endif
private:
	GBWindow();
public:
#if MAC
	GBWindow(GBView * contents, short left, short top, bool vis);
#elif WINDOWS
	GBWindow(GBView * contents, int x, int y, bool vis,
		HINSTANCE hInstance, GBViewsApplication * _app);
#endif
	~GBWindow();
// window operations
	void Update(bool running);
	void DrawChanges(bool moreChancesSoon);
	void Show(); //show and bring to front
	void Hide();
	bool Visible() const;
#if MAC
	void AcceptClick(Point where, int clicksBefore);
	void AcceptDrag(Point where);
	void AcceptUnclick(Point where, int clicksBefore);
#elif WINDOWS
	void AcceptClick(int x, int y, short clicksBefore);
	void AcceptDrag(int x, int y);
	void AcceptUnclick(int x, int y, short clicksBefore);
#endif
	void AcceptKeystroke(char what);
	bool GetFrontClicks() const;
	GBCursor Cursor() const;
//sizing
	void ResizeSelf();
	void SetSize(short width, short height);
#if MAC
	void Resize(Point where); // handle grow box
	void ZoomIn(); // go to user state
	void ZoomOut(); // go to calculated state
#endif

// statics
public:
#if MAC
	static bool IsGBWindow(WindowPtr wind);
	static GBWindow * GetFromWindow(WindowPtr wind);
#elif WINDOWS
	static HRESULT CALLBACK WindowProc(HWND hWin, UINT msg, WPARAM wParam, LPARAM lParam);
#endif
	// some sort of list of extant windows? (for Window menu)
};

#endif
