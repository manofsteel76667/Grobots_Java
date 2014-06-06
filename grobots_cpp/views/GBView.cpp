// GBView.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBView.h"
#include "GBErrors.h"
#include "GBColor.h"
#include "GBStringUtilities.h"
#include "GBTypes.h"


GBRect GBView::CalcExternalRect(const GBRect & r) const {
	return GBRect(r.left + bounds.left,
		r.top + bounds.top,
		r.right + bounds.left,
		r.bottom + bounds.top);
}

void GBView::DrawLine(short x1, short y1, short x2, short y2,
		const GBColor & color, short thickness) const {
	graphics->DrawLine(x1, y1, x2, y2, color, thickness);
}

void GBView::DrawSolidRect(const GBRect & where, const GBColor & color) const {
	graphics->DrawSolidRect(CalcExternalRect(where), color);
}

void GBView::DrawOpenRect(const GBRect & where, const GBColor & color, short thickness) const {
	graphics->DrawOpenRect(CalcExternalRect(where), color, thickness);
}

void GBView::DrawBox(const GBRect & box, bool selected) const {
	GBRect r = CalcExternalRect(box);
	if ( selected )
		graphics->DrawSolidRect(r, GBColor::black);
	else {
		graphics->DrawSolidRect(r, GBColor::white);
		graphics->DrawOpenRect(r, GBColor::black);
	}
}

void GBView::DrawBackground(const GBColor & color) const {
	graphics->DrawSolidRect(bounds, color);
}

void GBView::DrawSolidOval(const GBRect & where, const GBColor & color) const {
	graphics->DrawSolidOval(CalcExternalRect(where), color);
}

void GBView::DrawOpenOval(const GBRect & where, const GBColor & color, short thickness) const {
	graphics->DrawOpenOval(CalcExternalRect(where), color, thickness);
}

void GBView::DrawArc(const GBRect & where, short startAngle, short length,
		const GBColor & color, short thickness) const {
	graphics->DrawArc(CalcExternalRect(where), startAngle, length, color, thickness);
}

void GBView::DrawStringLeft(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) const {
	graphics->DrawStringLeft(str, x + bounds.left, y + bounds.top,
		size, color, useBold);
}

void GBView::DrawStringRight(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) const {
	graphics->DrawStringRight(str, x + bounds.left, y + bounds.top,
		size, color, useBold);
}

void GBView::DrawStringCentered(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) const {
	graphics->DrawStringCentered(str, x + bounds.left, y + bounds.top,
		size, color, useBold);
}

void GBView::DrawStringPair(const string & str1, const string & str2,
		short left, short right, short y, short size, const GBColor & color, bool useBold) const {
	DrawStringLeft(str1, left, y, size, color, useBold);
	DrawStringRight(str2, right, y, size, color, useBold);
}

void GBView::DrawLongLeft(long n, short x, short y,
		short size, const GBColor & color, bool useBold) const {
	DrawStringLeft(ToString(n), x, y, size, color, useBold);
}

void GBView::DrawLongRight(long n, short x, short y,
		short size, const GBColor & color, bool useBold) const {
	DrawStringRight(ToString(n), x, y, size, color, useBold);
}

void GBView::DrawStringLongPair(const string & str, long n,
		short left, short right, short y, short size, const GBColor & color, bool useBold) const {
	DrawStringLeft(str, left, y, size, color, useBold);
	DrawLongRight(n, right, y, size, color, useBold);
}

void GBView::Blit(const GBBitmap & src, const GBRect & srcRect, const GBRect & destRect) const {
	graphics->Blit(src, srcRect, CalcExternalRect(destRect));
}

void GBView::BlitAll(const GBBitmap & src, const GBRect & srcRect) const {
	graphics->Blit(src, srcRect, bounds);
}


GBView::GBView()
	: lastDrawn(-1), graphics(nil)
{}

GBView::~GBView() {}

bool GBView::Resizable() const {
	return false;
}

short GBView::MinimumWidth() const {
	return Resizable() ? 50 : PreferredWidth();
}

short GBView::MinimumHeight() const {
	return Resizable() ? 50 : PreferredHeight();
}

short GBView::MaximumWidth() const {
	return Resizable() ? 10000 : PreferredWidth();
}

short GBView::MaximumHeight() const {
	return Resizable() ? 10000 : PreferredHeight();
}

short GBView::PreferredWidth() const {
	return 300;
}

short GBView::PreferredHeight() const {
	return 300;
}

short GBView::Width() const {
	return bounds.Width();
}

short GBView::Height() const {
	return bounds.Height();
}

short GBView::CenterX() const {
	return bounds.CenterX();
}

short GBView::CenterY() const {
	return bounds.CenterY();
}

void GBView::SetSize(short width, short height) {
	bounds.right = bounds.left + width;
	bounds.bottom = bounds.top + height;
}

void GBView::SetBounds(const GBRect & newbounds) {
	bounds = newbounds;
}

GBGraphics & GBView::Graphics() const {
	if ( ! graphics ) throw GBNilPointerError();
	return *graphics;
}

void GBView::SetGraphics(GBGraphics * g) {
	if ( ! g ) throw GBNilPointerError();
	graphics = g;
}

void GBView::Draw() {}

bool GBView::NeedsRedraw(bool running) const {
	GBMilliseconds interval = RedrawInterval();
	if ( interval < 0 )
		return InstantChanges() || ! running && DelayedChanges();
	return InstantChanges() ||
		(! running || Milliseconds() >= lastDrawn + interval)
			&& DelayedChanges();
}

bool GBView::NeedsResize() const {
	return Width() < MinimumWidth() || Height() < MinimumHeight()
		|| Width() > MaximumWidth() || Height() > MaximumHeight();
}

GBMilliseconds GBView::RedrawInterval() const {
	return -1; // only draw delayed changes when paused
}

bool GBView::InstantChanges() const {
	return false;
}

bool GBView::DelayedChanges() const {
	return false;
}

void GBView::DoDraw() {
	if ( ! graphics ) throw GBNilPointerError();
	try {
		Draw();
	} catch ( GBError & err ) {
		NonfatalError("Error drawing: " + err.ToString());
	} catch ( GBAbort & ) {}
	lastDrawn = Milliseconds();
}

void GBView::DoClick(short x, short y, int clicksBefore) {
	AcceptClick(x - bounds.left, y - bounds.top, clicksBefore);
}

void GBView::DoDrag(short x, short y) {
	AcceptDrag(x - bounds.left, y - bounds.top);
}

void GBView::DoUnclick(short x, short y, int clicksBefore) {
	AcceptUnclick(x - bounds.left, y - bounds.top, clicksBefore);
}


bool GBView::GetFrontClicks() const {
	return false;
}

void GBView::AcceptClick(short /*x*/, short /*y*/, int /*clicksBefore*/) {}

void GBView::AcceptDrag(short /*x*/, short /*y*/) {}

void GBView::AcceptUnclick(short /*x*/, short /*y*/, int /*clicksBefore*/) {}

void GBView::AcceptKeystroke(const char) {}


const string GBView::Name() const {
	return "a view";
}

GBCursor GBView::Cursor() const {
	return cuArrow;
}


// GBWrapperView //

GBWrapperView::GBWrapperView(GBView * what)
	: content(what)
{}

GBWrapperView::~GBWrapperView() {
	delete content;
}

bool GBWrapperView::Resizable() const {
	return content->Resizable();
}

short GBWrapperView::MinimumWidth() const {
	return content->MinimumWidth();
}

short GBWrapperView::MinimumHeight() const {
	return content->MinimumHeight();
}

short GBWrapperView::MaximumWidth() const {
	return content->MaximumWidth();
}

short GBWrapperView::MaximumHeight() const {
	return content->MaximumHeight();
}

short GBWrapperView::PreferredWidth() const {
	return content->PreferredWidth();
}

short GBWrapperView::PreferredHeight() const {
	return content->PreferredHeight();
}

void GBWrapperView::SetSize(short width, short height) {
	content->SetSize(width, height);
	GBView::SetSize(content->Width(), content->Height());
}

void GBWrapperView::SetBounds(const GBRect & newbounds) {
	content->SetBounds(newbounds);
	GBView::SetBounds(newbounds);
}

void GBWrapperView::Draw() {
	content->Draw();
}

GBMilliseconds GBWrapperView::RedrawInterval() const {
	return content->RedrawInterval();
}

bool GBWrapperView::InstantChanges() const {
	return content->InstantChanges();
}

bool GBWrapperView::DelayedChanges() const {
	return content->DelayedChanges();
}

bool GBWrapperView::GetFrontClicks() const {
	return content->GetFrontClicks();
}

void GBWrapperView::AcceptClick(short x, short y, int clicksBefore) {
	content->AcceptClick(x, y, clicksBefore);
}

void GBWrapperView::AcceptDrag(short x, short y) {
	content->AcceptDrag(x, y);
}

void GBWrapperView::AcceptUnclick(short x, short y, int clicksBefore) {
	content->AcceptUnclick(x, y, clicksBefore);
}

void GBWrapperView::AcceptKeystroke(const char what) {
	content->AcceptKeystroke(what);
}

const string GBWrapperView::Name() const {
	return content->Name();
}

GBCursor GBWrapperView::Cursor() const {
	return content->Cursor();
}

// GBDoubleBufferedView //

GBDoubleBufferedView::GBDoubleBufferedView(GBView * what)
	: GBWrapperView(what),
	offscreen(nil),
	draw(true), flip(true)
{}

GBDoubleBufferedView::~GBDoubleBufferedView() {
	delete offscreen;
}

void GBDoubleBufferedView::SetSize(short width, short height) {
	GBWrapperView::SetSize(width, height);
	if ( offscreen ) {
		delete offscreen;
		offscreen = nil;
	}
}

void GBDoubleBufferedView::SetBounds(const GBRect & newbounds) {
	GBWrapperView::SetBounds(newbounds);
	if ( offscreen ) {
		delete offscreen;
		offscreen = nil;
	}
}

void GBDoubleBufferedView::AcceptKeystroke(const char what) {
	if ( what == '!' ) draw = ! draw;
	else if ( what == '@' ) flip = ! flip;
	else GBWrapperView::AcceptKeystroke(what);
}

void GBDoubleBufferedView::Draw() {
	bool newWorld = false;
	if ( ! offscreen ) {
		offscreen = new GBBitmap(Width(), Height(), Graphics());
		content->SetGraphics(&(offscreen->Graphics()));
		newWorld = true;
	}
// draw offscreen
	if ( draw && (NeedsRedraw(false) || newWorld) ) {
		offscreen->StartDrawing();
		content->Draw();
		offscreen->StopDrawing();
	}
// draw onscreen
	if ( flip )
		BlitAll(*offscreen, offscreen->Bounds());
}

