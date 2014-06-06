// GBWindow.cpp
// code for the Mac window class
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBWindow.h"
#include "GBErrors.h"
#include "GBStringUtilities.h"
#include "GBViewsApplication.h"

#if MAC
#if ! MAC_OS_X
#include <Windows.h>
#include <ToolUtils.h>
#endif

#if ! CARBON
	#define GetWindowPort(w) w
#endif

const short kMacWindowKind = 190;
#elif WINDOWS
const char * kWindowClassName = "Grobots window";
#endif

#if MAC
GBWindow::GBWindow(GBView * contents, short left, short top, bool vis)
	: showGrowBox(false), //TODO
	window(nil),
	view(contents),
	visible(vis)
{
	Rect bounds;
	bounds.left = left;
	bounds.top = top;
	bounds.right = left + view->PreferredWidth();
	bounds.bottom = top + view->PreferredHeight();
	Str255 s;
	ToPascalString(view->Name(), s);
	view->SetBounds(GBRect(0, 0, view->PreferredWidth(), view->PreferredHeight()));
#if CARBON
	if ( CreateNewWindow(kDocumentWindowClass,
			view->Resizable() ? kWindowStandardDocumentAttributes
				: (kWindowCloseBoxAttribute | kWindowCollapseBoxAttribute),
			&bounds, &window) )
		throw GBOutOfMemoryError();
	SetWTitle(window, s);
	SetWRefCon(window, (long)this);
#else
	window = NewCWindow(nil, &bounds, s, vis,
		view->Resizable() ? zoomDocProc : noGrowDocProc,
		(WindowPtr)(-1), true, (long)this);
	if ( ! window ) throw GBOutOfMemoryError();
#endif
	SetWindowKind(window, kMacWindowKind);
	view->SetGraphics(&graphics);
	if ( vis )
		ShowWindow(window);
}
#elif WINDOWS
GBWindow::GBWindow(GBView * v, int x, int y, bool vis,
				   HINSTANCE hInstance, GBViewsApplication * _app)
	: view(v), isMain(! _app->MainWindow()), visible(vis), app(_app)
{
//determine bounds
	RECT bounds;
	SetRect(&bounds, x, y, x + v->PreferredWidth(), y + v->PreferredHeight());
	AdjustWindowRect(&bounds, WS_OVERLAPPEDWINDOW, isMain);
	win = CreateWindow(kWindowClassName,
		(isMain ? "Grobots" : v->Name().c_str()),
		view->Resizable() ? WS_OVERLAPPEDWINDOW : WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU,
		bounds.left, bounds.top, bounds.right - bounds.left, bounds.bottom - bounds.top,
		(isMain ? 0 : app->MainWindow()->win), 0, hInstance, 0);
	if (!win)
		FatalError("Couldn't create window.");
	SetWindowLong(win, GWL_USERDATA, reinterpret_cast<long>(this));
	//TODO fix menubar from GBApplication?
	//TODO examine window for menubar instead of isMain
	//if (isMain) {
	//	HMENU mbar = LoadMenu(hInstance, MAKEINTRESOURCE(IDR_MENUBAR));
	//	if (!mbar)
	//		FatalError("Couldn't load menu bar.");
	//	SetMenu(win, mbar);
	//}
	v->SetSize(v->PreferredWidth(), v->PreferredHeight());
}
#endif

GBWindow::~GBWindow() {
	delete view;
#if MAC
	DisposeWindow(window);
#elif WINDOWS
	DestroyWindow(win);
#endif
}

void GBWindow::SetSize(short width, short height) {
	view->SetSize(width, height);
#if MAC
	SizeWindow(window, view->Width(), view->Height(), true);
#endif
}

void GBWindow::Update(bool running) {
#if MAC
	GrafPtr savePort;
	GetPort(&savePort);
	SetPort(GetWindowPort(window));
	if ( view->NeedsResize() )
		ResizeSelf();
	if ( view->NeedsRedraw(running) ) {
		BeginUpdate(window);
		EndUpdate(window);
		if ( showGrowBox ) DrawGrowIcon(window);
		view->DoDraw();
	} else {
		BeginUpdate(window);
		if ( showGrowBox ) DrawGrowIcon(window);
		view->DoDraw();
		EndUpdate(window);
	}
	SetPort(savePort);
#elif WINDOWS
	PAINTSTRUCT paint;
	HDC dc = BeginPaint(win, &paint);
	GBGraphics graphics(dc);
	view->SetGraphics(&graphics);
	view->DoDraw();
	EndPaint(win, &paint);
#endif
}

void GBWindow::DrawChanges(bool running) {
	if ( view->NeedsResize() )
		ResizeSelf();
	if ( visible && view->NeedsRedraw(running) ) {
#if MAC
		GrafPtr savePort;
		GetPort(&savePort);
		SetPort(GetWindowPort(window));
		view->DoDraw();
		SetPort(savePort);
#elif WINDOWS
		HDC dc = GetDC(win);
		GBGraphics graphics(dc);
		view->SetGraphics(&graphics);
		view->DoDraw();
		ReleaseDC(win, dc);
#endif
	}
}

void GBWindow::ResizeSelf() {
#if MAC
	SetSize(view->PreferredWidth(), view->PreferredHeight());
#elif WINDOWS
	RECT where, bounds;
	GetWindowRect(win, &where);

	SetRect(&bounds, where.left, where.top,
		where.left + view->PreferredWidth(),
		where.top + view->PreferredHeight());
	AdjustWindowRect(&bounds, WS_OVERLAPPEDWINDOW, isMain);
	MoveWindow(win, where.left, where.top, bounds.right - bounds.left,
		bounds.bottom - bounds.top, true);
#endif
}

void GBWindow::Show() {
	if ( ! visible && view->NeedsResize() )
		ResizeSelf();
#if MAC
	ShowWindow(window);
	SelectWindow(window);
#elif WINDOWS
	ShowWindow(win, SW_SHOWNORMAL);
#endif
	visible = true;
}

void GBWindow::Hide() {
#if MAC
	HideWindow(window);
#elif WINDOWS
	ShowWindow(win, SW_HIDE);
#endif
	visible = false;
}

bool GBWindow::Visible() const {
	return visible;
}

#if MAC
void GBWindow::AcceptClick(Point where, int clicksBefore) {
	GrafPtr savePort;
	GetPort(&savePort);
	SetPort(GetWindowPort(window));
	GlobalToLocal(&where);
	view->DoClick(where.h, where.v, clicksBefore);
	SetPort(savePort);
}

void GBWindow::AcceptDrag(Point where) {
	GrafPtr savePort;
	GetPort(&savePort);
	SetPort(GetWindowPort(window));
	GlobalToLocal(&where);
	view->DoDrag(where.h, where.v);
	SetPort(savePort);
}

void GBWindow::AcceptUnclick(Point where, int clicksBefore) {
	GrafPtr savePort;
	GetPort(&savePort);
	SetPort(GetWindowPort(window));
	GlobalToLocal(&where);
	view->DoUnclick(where.h, where.v, clicksBefore);
	SetPort(savePort);
}
#elif WINDOWS
void GBWindow::AcceptClick(int x, int y, short clicksBefore) {
	view->DoClick(x, y, clicksBefore);
}

void GBWindow::AcceptDrag(int x, int y) {
	view->DoDrag(x, y);
}

void GBWindow::AcceptUnclick(int x, int y, short clicksBefore) {
	view->DoUnclick(x, y, clicksBefore);
}
#endif

void GBWindow::AcceptKeystroke(char what) {
	view->AcceptKeystroke(what);
}

bool GBWindow::GetFrontClicks() const {
	return view->GetFrontClicks();
}

GBCursor GBWindow::Cursor() const {
	return view->Cursor();
}

#if MAC
bool GBWindow::IsGBWindow(WindowPtr wind) {
	if ( ! wind ) return false;
	return GetWindowKind(wind) == kMacWindowKind;
}

GBWindow * GBWindow::GetFromWindow(WindowPtr wind) {
	if ( ! wind ) return nil;
	return (GBWindow *)GetWRefCon(wind);
}
#elif WINDOWS
#endif


#if MAC
void GBWindow::Resize(Point where) {
	Rect sizeRect;
	SetRect(&sizeRect, view->MinimumWidth(), view->MinimumHeight(), view->MaximumWidth(), view->MaximumHeight());
#if CARBON
	Rect newsize;
	if ( ResizeWindow(window, where, &sizeRect, &newsize) )
		SetSize(newsize.right - newsize.left, newsize.bottom - newsize.top);
#else
	long result = GrowWindow(window, where, &sizeRect);
	if ( result )
		SetSize(LoWord(result), HiWord(result));
#endif
}

void GBWindow::ZoomIn() {
	ZoomWindow(window, inZoomIn, false);
	//view->SetSize(GetSize());
}

void GBWindow::ZoomOut() {
	//ZoomWindow(window, inZoomOut, false);
	ResizeSelf();
}
#endif //Mac resizing
