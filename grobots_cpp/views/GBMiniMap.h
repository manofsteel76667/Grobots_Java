// GBMiniMap.h
// miniature view of a GBWorld
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef _GBMiniMap_h
#define _GBMiniMap_h

#include "GBView.h"
#include "GBWorld.h"
#include "GBPortal.h"


class GBMiniMapView : public GBView {
	const GBWorld & world;
	GBPortal & portal;
	GBChangeCount worldChanges, portalChanges;
	GBFrames frameLastDrawn;
	GBNumber scalex, scaley;
	GBBitmap * trails;
public:
	bool showRobots, showFood, showSensors, showDecorations;
	bool showTrails;
private:
// drawing internals
	void DrawLayer(const GBObjectClass layer, const short minSize) const;
	void DrawLayerFixed(const GBObjectClass layer, const short size) const;
	void DrawObjectList(const GBObject * list, const short minSize) const;
	void DrawObjectListFixed(const GBObject * list, const short size) const;
	void DrawObjectListTrails(const GBObject * list, const short minSize) const;
// coordinate conversions
	void RecalculateScales();
	short ToScreenX(const GBCoordinate x) const;
	short ToScreenY(const GBCoordinate y) const;
	GBCoordinate FromScreenX(const short h) const;
	GBCoordinate FromScreenY(const short v) const;
	GBFinePoint FromScreen(short x, short y) const;
public:
	GBMiniMapView(const GBWorld & targ, GBPortal & port);
	~GBMiniMapView();
	void Draw();
	bool InstantChanges() const;
	bool DelayedChanges() const;
	bool GetFrontClicks() const;
	bool Resizable() const;
	short MinimumWidth() const;
	short MinimumHeight() const;
	short MaximumWidth() const;
	short MaximumHeight() const;
	short PreferredWidth() const;
	short PreferredHeight() const;
	void SetSize(short width, short height);
	void SetBounds(const GBRect & newbounds);
	void AcceptClick(short x, short y, int clicks);
	void AcceptDrag(short x, short y);
	void AcceptKeystroke(const char what);
	const string Name() const;
	GBCursor Cursor() const;
};

#endif
