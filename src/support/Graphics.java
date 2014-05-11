// GBGraphics.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#if MAC && ! HEADLESS
#if MAC_OS_X
#include <Carbon/Carbon.h>
#else
#include <Quickdraw.h>
#include <QDOffscreen.h>
#endif
#elif WINDOWS
#include <Windows.h>
#undef SendMessage
	//this macro was clobbering GBSide::SendMessage
#endif

#include "Color.h"
#include <string>
using std::string;

class GBRect {
public:
	short left, top, right, bottom;
	GBRect();
	GBRect(short l, short t, short r, short b);
	short Width() const;
	short Height() const;
	short CenterX() const;
	short CenterY() const;
	void Shrink(short step);
#if MAC && ! HEADLESS
	void ToRect(Rect & r) const;
	GBRect(Rect & r);
#elif WINDOWS
	void ToRect(RECT & r) const;
#endif
};

class GBBitmap;

class GBGraphics {
#if HEADLESS
public:
	GBGraphics();
#elif MAC
	// no data members
	void UseColor(const GBColor & c);
public:
	GBGraphics();
#elif WINDOWS
	HDC hdc;
	static COLORREF ColorRef(const GBColor & c);
	void DrawString(const string & str, short x, short y,
		short size, const GBColor & color, bool bold);
public:
	GBGraphics(HDC dc);
	friend class GBBitmap;
#else
	#warning "Need GBGraphics."
#endif
public:
	~GBGraphics();
// lines
	void DrawLine(short x1, short y1, short x2, short y2,
		const GBColor & color, short thickness = 1);
// rectangles
	void DrawSolidRect(const GBRect & where, const GBColor & color);
	void DrawOpenRect(const GBRect & where, const GBColor & color, short thickness = 1);
// ovals
	void DrawSolidOval(const GBRect & where, const GBColor & color);
	void DrawOpenOval(const GBRect & where, const GBColor & color, short thickness = 1);
	void DrawArc(const GBRect & where, short startAngle, short length,
		const GBColor & color, short thickness = 1);
// strings
	void DrawStringLeft(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false);
	void DrawStringRight(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false);
	void DrawStringCentered(const string & str, short x, short y,
		short size, const GBColor & color = GBColor::black, bool bold = false);
	void DrawStringPair(const string & str1, const string & str2,
		short left, short right, short y, short size, const GBColor & color, bool useBold);

// blitter
	void Blit(const GBBitmap & src, const GBRect & srcRect, const GBRect & destRect);
};

class GBBitmap {
private:
	GBRect bounds;
	GBGraphics graphics;
#if HEADLESS
#elif MAC
	GWorldPtr world;
	CGrafPtr savePort;
	GDHandle saveDevice;
public:
	BitMapPtr Bits() const;
#elif WINDOWS
	HBITMAP bits;
	HDC hdc;
#else
	#warning "Need GBBitmap."
#endif
public:
	GBBitmap(short width, short height, GBGraphics & parent);
	~GBBitmap();
	const GBRect & Bounds() const;
	GBGraphics & Graphics();
	const GBGraphics & Graphics() const;
// must call StartDrawing and StopDrawing around any drawing to the bitmap,
//  to allow saving and restoring state :(
	void StartDrawing();
	void StopDrawing();
};

#include "Graphics.h"
#include "StringUtilities.h"
#include <math.h>

// GBRect //

GBRect::GBRect() : left(0), top(0), right(0), bottom(0) {}

GBRect::GBRect(short l, short t, short r, short b)
	: left(l), top(t), right(r), bottom(b) {}

short GBRect::Width() const { return right - left; }
short GBRect::Height() const { return bottom - top; }
short GBRect::CenterX() const { return (right + left) >> 1; }
short GBRect::CenterY() const { return (top + bottom) >> 1; }

void GBRect::Shrink(short step) {
	left += step;
	top += step;
	right -= step;
	bottom -= step;
}

#if MAC && ! HEADLESS
void GBRect::ToRect(Rect & r) const {
	r.left = left;
	r.top = top;
	r.right = right;
	r.bottom = bottom;
}

GBRect::GBRect(Rect & r)
	: left(r.left), top(r.top), right(r.right), bottom(r.bottom) {}
#elif WINDOWS
void GBRect::ToRect(RECT & r) const {
	r.left = left;
	r.top = top;
	r.right = right;
	r.bottom = bottom;
}
#endif

// GBGraphics //

#if HEADLESS
GBGraphics::GBGraphics() {}
GBGraphics::~GBGraphics() {}

void GBGraphics::DrawLine(short, short, short, short, const GBColor &, short) {}

void GBGraphics::DrawSolidRect(const GBRect &, const GBColor &) {}
void GBGraphics::DrawOpenRect(const GBRect &, const GBColor &, short) {}
void GBGraphics::DrawSolidOval(const GBRect &, const GBColor &) {}
void GBGraphics::DrawOpenOval(const GBRect &, const GBColor &, short) {}
void GBGraphics::DrawArc(const GBRect &, short, short, const GBColor &, short) {}

void GBGraphics::DrawStringLeft(const string &, short, short, short, const GBColor &, bool) {}
void GBGraphics::DrawStringRight(const string &, short, short, short, const GBColor &, bool) {}
void GBGraphics::DrawStringCentered(const string &, short, short, short, const GBColor &, bool) {}

void GBGraphics::Blit(const GBBitmap &, const GBRect &, const GBRect &) {}
#elif MAC
void GBGraphics::UseColor(const GBColor & c) {
	RGBColor color;
	color.red = 0xFFFF * c.Red();
	color.green = 0xFFFF * c.Green();
	color.blue = 0xFFFF * c.Blue();
	RGBForeColor(&color);
}

GBGraphics::GBGraphics() {}
GBGraphics::~GBGraphics() {}

void GBGraphics::DrawLine(short x1, short y1, short x2, short y2,
		const GBColor & color, short thickness) {
//correct for pen drawing down and to the right
	x1 -= thickness >> 1;
	x2 -= thickness >> 1;
	y1 -= thickness >> 1;
	y2 -= thickness >> 1;
//draw
	UseColor(color);
	PenSize(thickness, thickness);
	MoveTo(x1, y1);
	LineTo(x2, y2);
}

void GBGraphics::DrawSolidRect(const GBRect & where, const GBColor & color) {
	Rect r;
	where.ToRect(r);
	UseColor(color);
	PaintRect(&r);
}

void GBGraphics::DrawOpenRect(const GBRect & where, const GBColor & color, short thickness) {
	Rect r;
	where.ToRect(r);
	UseColor(color);
	PenSize(thickness, thickness);
	FrameRect(&r);
}

void GBGraphics::DrawSolidOval(const GBRect & where, const GBColor & color) {
	Rect r;
	where.ToRect(r);
	UseColor(color);
	PaintOval(&r);
}

void GBGraphics::DrawOpenOval(const GBRect & where, const GBColor & color, short thickness) {
	Rect r;
	where.ToRect(r);
	UseColor(color);
	PenSize(thickness, thickness);
	FrameOval(&r);
}

//startAngle: degrees clockwise from up
//length: degrees
void GBGraphics::DrawArc(const GBRect & where, short startAngle, short length,
		const GBColor & color, short thickness) {
	Rect r;
	where.ToRect(r);
	UseColor(color);
	PenSize(thickness, thickness);
	FrameArc(&r, startAngle, length);
}

void GBGraphics::DrawStringLeft(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	Str255 s;
	ToPascalString(str, s);
	UseColor(color);
	TextSize(size);
	TextFace(useBold ? bold : normal);
	MoveTo(x, y);
	DrawString(s);
}

void GBGraphics::DrawStringRight(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	Str255 s;
	ToPascalString(str, s);
	UseColor(color);
	TextSize(size);
	TextFace(useBold ? bold : normal);
	MoveTo(x - StringWidth(s), y);
	DrawString(s);
}

void GBGraphics::DrawStringCentered(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	Str255 s;
	ToPascalString(str, s);
	UseColor(color);
	TextSize(size);
	TextFace(useBold ? bold : normal);
	MoveTo(x - StringWidth(s) / 2, y);
	DrawString(s);
}

void GBGraphics::Blit(const GBBitmap & src, const GBRect & srcRect, const GBRect & destRect) {
	Rect r1, r2;
	srcRect.ToRect(r1);
	destRect.ToRect(r2);
	GrafPtr port;
	GetPort(&port);
	ForeColor(blackColor);
	CopyBits(src.Bits(), GetPortBitMapForCopyBits(port), &r1, &r2, srcCopy, nil);
}

#elif WINDOWS

COLORREF GBGraphics::ColorRef(const GBColor & c) {
	return RGB(255 * c.Red(), 255 * c.Green(), 255 * c.Blue());
}

GBGraphics::GBGraphics(HDC dc) : hdc(dc) {
	SetBkMode(hdc, TRANSPARENT);
}

GBGraphics::~GBGraphics() {}

void GBGraphics::DrawLine(short x1, short y1, short x2, short y2,
		const GBColor & color, short thickness) {
	HPEN pen = CreatePen(PS_SOLID, thickness, ColorRef(color));
	HGDIOBJ old = SelectObject(hdc, pen);
	MoveToEx(hdc, x1, y1, 0);
	LineTo(hdc, x2, y2);
	SelectObject(hdc, old);
	DeleteObject(pen);
}

void GBGraphics::DrawSolidRect(const GBRect & r, const GBColor & color) {
	HBRUSH brush = CreateSolidBrush(ColorRef(color));
	RECT rect;
	r.ToRect(rect);
	FillRect(hdc, &rect, brush);
	DeleteObject(brush);
}

void GBGraphics::DrawOpenRect(const GBRect & r, const GBColor & color, short thickness) {
	HBRUSH brush = CreateSolidBrush(ColorRef(color));
	RECT rect;
	r.ToRect(rect);
	FrameRect(hdc, &rect, brush);
	DeleteObject(brush);
}

void GBGraphics::DrawSolidOval(const GBRect & r, const GBColor & color) {
	HBRUSH brush = CreateSolidBrush(ColorRef(color));
	HPEN pen = CreatePen(PS_SOLID, 0, ColorRef(color));
	HGDIOBJ oldbrush = SelectObject(hdc, brush);
	HGDIOBJ oldpen = SelectObject(hdc, pen);
	Ellipse(hdc, r.left, r.top, r.right, r.bottom);
	SelectObject(hdc, oldpen);
	SelectObject(hdc, oldbrush);
	DeleteObject(brush);
	DeleteObject(pen);
}

void GBGraphics::DrawOpenOval(const GBRect & r, const GBColor & color, short thickness) {
	DrawArc(r, 180, 360, color, thickness);
}

void GBGraphics::DrawArc(const GBRect & r, short startAngle, short length,
						 const GBColor & color, short thickness) {
	short start = 90 - startAngle - length; //different direction
	const float kPiOver180 = 3.14159265f / 180;
	HPEN pen = CreatePen(PS_SOLID, thickness, ColorRef(color));
	HGDIOBJ old = SelectObject(hdc, pen);
	MoveToEx(hdc, r.CenterX() + r.Width() * cos(start * kPiOver180) / 2,
		r.CenterY() + r.Height() * sin(start * kPiOver180) / -2, 0);
	AngleArc(hdc, r.CenterX(), r.CenterY(), r.Height() / 2, start, length);
	SelectObject(hdc, old);
	DeleteObject(pen);
}

void GBGraphics::DrawString(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	HFONT f = CreateFont(- size, 0, 0, 0, useBold ? FW_BOLD : FW_NORMAL,
		0, 0, 0, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
		DEFAULT_QUALITY, DEFAULT_PITCH | FF_SWISS, "Arial");
	HGDIOBJ old = SelectObject(hdc, f);
	SetTextColor(hdc, ColorRef(color));
	TextOut(hdc, x, y, str.c_str(), str.length());
	SelectObject(hdc, old);
	DeleteObject(f);
}

void GBGraphics::DrawStringLeft(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	SetTextAlign(hdc, TA_LEFT | TA_BASELINE);
	DrawString(str, x, y, size, color, useBold);
}

void GBGraphics::DrawStringRight(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	SetTextAlign(hdc, TA_RIGHT | TA_BASELINE);
	DrawString(str, x, y, size, color, useBold);
}

void GBGraphics::DrawStringCentered(const string & str, short x, short y,
		short size, const GBColor & color, bool useBold) {
	SetTextAlign(hdc, TA_CENTER | TA_BASELINE);
	DrawString(str, x, y, size, color, useBold);
}

void GBGraphics::Blit(const GBBitmap & src, const GBRect & srcRect, const GBRect & destRect) {
	if (!BitBlt(hdc, destRect.left, destRect.top, destRect.Width(), destRect.Height(),
			src.Graphics().hdc, srcRect.left, srcRect.top, SRCCOPY))
		DrawSolidRect(destRect, GBColor::black);
}

#else
	#warning "Need implementation of GBGraphics."
#endif

void GBGraphics::DrawStringPair(const string & str1, const string & str2,
		short left, short right, short y, short size, const GBColor & color, bool useBold) {
	DrawStringLeft(str1, left, y, size, color, useBold);
	DrawStringRight(str2, right, y, size, color, useBold);
}



// GBBitmap //

const GBRect & GBBitmap::Bounds() const {
	return bounds;}

GBGraphics & GBBitmap::Graphics() {
	return graphics;}

const GBGraphics & GBBitmap::Graphics() const {
	return graphics;}

#if HEADLESS
GBBitmap::GBBitmap(short width, short height, GBGraphics &)
	: bounds(0, 0, width, height), graphics()
{}

GBBitmap::~GBBitmap() {}

void GBBitmap::StartDrawing() {}
void GBBitmap::StopDrawing() {}

#elif MAC
GBBitmap::GBBitmap(short width, short height, GBGraphics &)
	: bounds(0, 0, width, height),
	world(nil),
	savePort(nil), saveDevice(nil),
	graphics()
{
	Rect r;
	bounds.ToRect(r);
	if ( NewGWorld(&world, 0, &r, nil, nil, 0) )
		throw GBOutOfMemoryError();
}

GBBitmap::~GBBitmap() {
	if ( world ) {
		DisposeGWorld(world);
		world = nil;
	}
}

BitMapPtr GBBitmap::Bits() const {
	return *(BitMapHandle)GetGWorldPixMap(world);
}

void GBBitmap::StartDrawing() {
	GetGWorld(&savePort, &saveDevice);
	SetGWorld(world, nil);
}

void GBBitmap::StopDrawing() {
	SetGWorld(savePort, saveDevice);
}

#elif WINDOWS

GBBitmap::GBBitmap(short width, short height, GBGraphics & parent)
	: bounds(0, 0, width, height),
	bits(CreateCompatibleBitmap(parent.hdc, width, height)),
	graphics(CreateCompatibleDC(parent.hdc))
{
	SelectObject(graphics.hdc, bits);
}

GBBitmap::~GBBitmap() {
	DeleteDC(graphics.hdc);
	DeleteObject(bits);
}

void GBBitmap::StartDrawing() {}
void GBBitmap::StopDrawing() {}

#else
	#warning "Need implementation of GBBitmap."
#endif

