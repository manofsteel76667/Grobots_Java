// GBListView.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBListView.h"


GBListView::GBListView()
	: GBView()
{}

GBListView::~GBListView() {}

bool GBListView::Resizable() const {
	return false;
}

void GBListView::Draw() {
	DrawBackground();
	GBRect box;
	box.left = kEdgeSpace;
	box.top = kEdgeSpace;
	box.right = Width() - kEdgeSpace;
	box.bottom = box.top + HeaderHeight();
	if ( box.bottom > box.top )
		DrawHeader(box);
	else
		box.bottom -= kEdgeSpace;
// draw items
	long items = Items();
	for ( long i = 1; i <= items; ++ i ) {
		box.top = box.bottom + kEdgeSpace;
		box.bottom = box.top + ItemHeight();
		DrawItem(i, box);
	}
// draw footer
	box.top = box.bottom + kEdgeSpace;
	box.bottom = box.top + FooterHeight();
	if ( box.bottom > box.top )
		DrawFooter(box);
}

short GBListView::PreferredHeight() const {
	short hh = HeaderHeight();
	short fh = FooterHeight();
	return (hh ? kEdgeSpace + hh : 0) + kEdgeSpace
		+ (ItemHeight() + kEdgeSpace) * Items()
		+ (fh ? kEdgeSpace + fh : 0);
}

void GBListView::AcceptClick(short x, short y, int /*clicks*/) {
	AcceptDrag(x, y);
}

void GBListView::AcceptDrag(short x, short y) {
	if ( x >= kEdgeSpace && x <= Width() - kEdgeSpace ) {
		short hh = HeaderHeight();
		short top = kEdgeSpace + (hh ? hh + kEdgeSpace : 0);
		if ( y >= top ) {
			short bh = ItemHeight() + kEdgeSpace;
			long index = (y - top) / bh + 1;
			if ( index >= 1 && index <= Items() && (y - top) % bh < ItemHeight() ) {
				ItemClicked(index);
				return;
			}
		}
	}
	ItemClicked(0);
}

long GBListView::Items() const {
	return 0;
}

short GBListView::HeaderHeight() const {
	return 0;
}

short GBListView::ItemHeight() const {
	return 30;
}

short GBListView::FooterHeight() const {
	return 0;
}

void GBListView::DrawHeader(const GBRect & box) {
	DrawBox(box);
}

void GBListView::DrawItem(long /*index*/, const GBRect & box) {
	DrawBox(box);
}

void GBListView::DrawFooter(const GBRect & box) {
	DrawBox(box);
}

void GBListView::ItemClicked(long /*index*/) {}

