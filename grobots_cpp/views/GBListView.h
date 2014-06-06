// GBListView.h
// abstract class for views that are a sequence of items, with optional header and footer.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBListView_h
#define GBListView_h

#include "GBView.h"

class GBListView : public GBView {
public:
	GBListView();
	~GBListView();
// 
	bool Resizable() const;
	void Draw();

	// PreferredWidth is left to subclasses
	short PreferredHeight() const;
// events
	void AcceptClick(short x, short y, int clicks);
	void AcceptDrag(short x, short y);
// override
	virtual long Items() const;
	virtual short HeaderHeight() const;
	virtual short ItemHeight() const;
	virtual short FooterHeight() const;
	virtual void DrawHeader(const GBRect & box);
	virtual void DrawItem(long index, const GBRect & box);
	virtual void DrawFooter(const GBRect & box);
	virtual void ItemClicked(long index);
};

#endif
