// GBRobotTypeView.h
// view on a GBRobotType, showing hardware and other info
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBRobotTypeView_h
#define GBRobotTypeView_h

#include "GBListView.h"

class GBRobotType;
class GBBrainSpec;
class GBHardwareSpec;
class GBWorld;

class GBRobotTypeView : public GBListView {
	const GBWorld & world;
	GBChangeCount lastDrawn;
	const GBSide * lastSideDrawn;
	long typeID;
// drawing
	const GBRobotType * SelectedType() const;
	void DrawHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const string & arg1, const string & arg2, const string & arg3, const string & arg4,
		const GBNumber cost, const GBNumber mass);
	void DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg,
		const GBNumber cost, const GBNumber mass);
	void DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg1, const GBNumber arg2,
		const GBNumber cost, const GBNumber mass);
	void DrawNumericHardwareLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber arg1, const GBNumber arg2, const GBNumber arg3,
		const GBNumber cost, const GBNumber mass);
	void DrawHardwareSummaryLine(const GBRect & box, short base,
		const string & name, const GBColor & color,
		const GBNumber cost, const GBNumber mass);
	void DrawHardware(const GBHardwareSpec & hw, const GBRect & box);
public:
	explicit GBRobotTypeView(const GBWorld & targ);
	~GBRobotTypeView();

	const string Name() const;
	short PreferredWidth() const;

	void Draw();
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;

	long Items() const;
	short HeaderHeight() const;
	short ItemHeight() const;
	short FooterHeight() const;
	void DrawHeader(const GBRect & box);
	void DrawItem(long index, const GBRect & box);
	void DrawFooter(const GBRect & box);
	void ItemClicked(long index);
};

#endif
