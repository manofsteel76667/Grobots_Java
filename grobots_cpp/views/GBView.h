// GBView.h
// the abstract GBView class and a few others
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBView_h
#define GBView_h

#include "GBColor.h"
#include "GBNumber.h"
#include "GBGraphics.h"
#include "GBMilliseconds.h"

typedef enum {
	cuArrow = 0,
	cuHand,
	cuCross,
	cuWait
} GBCursor;

const short kEdgeSpace = 2;

// A View is a visible interface component.
class GBView {
	GBRect bounds;
	GBMilliseconds lastDrawn;
	GBGraphics * graphics;
protected:
	GBRect CalcExternalRect(const GBRect & src) const;
// portable graphics: lines
	void DrawLine(short x1, short y1, short x2, short y2,
		const GBColor & color, short thickness = 1) const;
// rectangles
	void DrawSolidRect(const GBRect & where, const GBColor & color) const;
	void DrawOpenRect(const GBRect & where, const GBColor & color, short thickness = 1) const;
	void DrawBox(const GBRect & box, bool selected = false) const;
	void DrawBackground(const GBColor & color = GBColor::lightGray) const;
// ovals
	void DrawSolidOval(const GBRect & where, const GBColor & color) const;
	void DrawOpenOval(const GBRect & where, const GBColor & color, short thickness = 1) const;
	void DrawArc(const GBRect & where, short startAngle, short length,
		const GBColor & color, short thickness = 1) const;
// strings
	void DrawStringLeft(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false) const;
	void DrawStringRight(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false) const;
	void DrawStringCentered(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false) const;
	void DrawStringPair(const string & str1, const string & str2,
		short left, short right, short y, short size, const GBColor & color = GBColor::black, bool bold = false) const;
// longs
	void DrawLongLeft(long n, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false) const;
	void DrawLongRight(long n, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false) const;
	void DrawStringLongPair(const string & str1, long n,
		short left, short right, short y, short size, const GBColor & color = GBColor::black, bool bold = false) const;
// blitter
	void Blit(const GBBitmap & src, const GBRect & srcRect, const GBRect & destRect) const;
	void BlitAll(const GBBitmap & src, const GBRect & srcRect) const;
public:
	GBView();
	virtual ~GBView();
// sizing
	virtual bool Resizable() const;
	virtual short MinimumWidth() const;
	virtual short MinimumHeight() const;
	virtual short MaximumWidth() const;
	virtual short MaximumHeight() const;
	virtual short PreferredWidth() const;
	virtual short PreferredHeight() const;
	short Width() const;
	short Height() const;
	short CenterX() const;
	short CenterY() const;
	virtual void SetSize(short width, short height);
	virtual void SetBounds(const GBRect & newbounds);
// drawing
	GBGraphics & Graphics() const;
	void SetGraphics(GBGraphics * g);
	virtual void Draw();
	bool NeedsRedraw(bool running) const;
	bool NeedsResize() const;
	virtual GBMilliseconds RedrawInterval() const;
	virtual bool InstantChanges() const;
	virtual bool DelayedChanges() const;
// for owner to call
	void DoDraw();
	void DoClick(short x, short y, int clicksBefore);
	void DoDrag(short x, short y);
	void DoUnclick(short x, short y, int clicksBefore);
// to override
	virtual bool GetFrontClicks() const; // accept clicks that brought window to front?
	virtual void AcceptClick(short x, short y, int clicksBefore);
	virtual void AcceptDrag(short x, short y);
	virtual void AcceptUnclick(short x, short y, int clicksBefore);
	virtual void AcceptKeystroke(const char what);
// other
	virtual const string Name() const;
	virtual GBCursor Cursor() const;
};


// WrapperView must forward all messages to content
class GBWrapperView : public GBView {
protected:
	GBView * const content;
public:
	explicit GBWrapperView(GBView * what);
	~GBWrapperView();
// sizing
	bool Resizable() const;
	short MinimumWidth() const;
	short MinimumHeight() const;
	short MaximumWidth() const;
	short MaximumHeight() const;
	short PreferredWidth() const;
	short PreferredHeight() const;
	void SetSize(short width, short height);
	void SetBounds(const GBRect & newbounds);
// drawing
	void Draw();
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;
// event handling
	bool GetFrontClicks() const;
	void AcceptClick(short x, short y, int clicksBefore);
	void AcceptDrag(short x, short y);
	void AcceptUnclick(short x, short y, int clicksBefore);
	void AcceptKeystroke(const char what);
// other
	const string Name() const;
	GBCursor Cursor() const;
};


class GBDoubleBufferedView : public GBWrapperView {
	GBBitmap * offscreen;
	bool draw, flip;
public:
	explicit GBDoubleBufferedView(GBView * what);
	~GBDoubleBufferedView();
// sizing
	void SetSize(short width, short height);
	void SetBounds(const GBRect & newbounds);
// event handling
	void AcceptKeystroke(const char what);
// drawing
	void Draw();
};


#endif
