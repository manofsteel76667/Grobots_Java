// GBRosterView.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBRosterView_h
#define GBRosterView_h

#include "GBListView.h"
#include "GBModel.h"
#include "GBTypes.h"

class GBWorld;
class GBSide;

class GBRosterView : public GBListView {
	GBWorld & world;
	GBChangeCount worldChanges;
	const GBSide * lastSideSelected;
	long numSides;
// framecounter
	GBFrames lastFrame;
	GBMilliseconds lastTime;
public:
	explicit GBRosterView(GBWorld & wrld);

	void Draw();
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;
	
	short PreferredWidth() const;
	
	const string Name() const;

	long Items() const;
	short HeaderHeight() const;
	short ItemHeight() const;
	void DrawHeader(const GBRect & box);
	void DrawItem(long index, const GBRect & box);
	void ItemClicked(long index);
};

#endif
