// GBPortal.h
// a view of [part of] a GBWorld
// Grobots (c) 2002-2008 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBPortal_h_
#define GBPortal_h_

#include "GBView.h"
#include "GBWorld.h"

enum {
	ptScroll = 0,
	ptAddManna, ptAddRobot, ptAddSeed,
	ptMove,
	ptPull,
	ptSmite, ptBlasts,
	ptErase, ptEraseBig,
	kNumPortalTools
};

class GBApplication;

class GBPortal : public GBView, public GBModel, public GBDeletionListener {
	GBWorld & world;
	GBPosition viewpoint;
	short scale; //pixels per unit
	bool following;
	GBPosition followPosition;
	GBMilliseconds lastFollow;
	GBObject * moving;
	GBChangeCount worldChanges;
	GBChangeCount selfChanges;
// tool use
	short lastx, lasty; // where mouse was last if we're dragging
	GBPosition lastClick;
	GBFrames lastFrame; // when last tool effect was
	GBBitmap * background;
public:
	bool autofollow;
	int tool;
	bool showSensors;
	bool showDecorations;
	bool showDetails;
	bool showSideNames;
private:
// drawing internals
	void DrawBackground();
	void DrawBackgroundTile(long ix, long iy);
	void DrawOneTile(const GBRect & b, GBGraphics & g);
	void InitBackground();
	void DrawObjects();
	void DrawObjectList(const GBObject * list);
	void DrawRangeCircle(const GBPosition & center, GBDistance radius, const GBColor &color);
// coordinate conversions
	short ToScreenX(const GBCoordinate x) const;
	short ToScreenY(const GBCoordinate y) const;
	GBCoordinate FromScreenX(const short h) const;
	GBCoordinate FromScreenY(const short v) const;
	GBFinePoint FromScreen(short x, short y) const;
// scrolling
	void RestrictScrolling();
	void ScrollToFollowed();
	// forbidden
	GBPortal();
public:
	GBPortal(GBWorld & newTarget);
	~GBPortal();
	
	void Draw();
	bool InstantChanges() const;
	void AcceptClick(short x, short y, int clicks);
	void AcceptDrag(short x, short y);
	void AcceptUnclick(short x, short y, int clicks);
	void AcceptKeystroke(const char what);
	void ReportDeletion(const GBDeletionReporter * deletee);
	const string Name() const;
	GBCursor Cursor() const;
// sizing
	bool Resizable() const;
	short PreferredWidth() const;
	short PreferredHeight() const;
	void SetSize(short width, short height);
// edges
	GBCoordinate ViewLeft() const;
	GBCoordinate ViewTop() const;
	GBCoordinate ViewRight() const;
	GBCoordinate ViewBottom() const;
// scrolling
	void ScrollTo(const GBFinePoint p);
	void ScrollToward(const GBFinePoint p, const GBSpeed speed);
	void ScrollBy(const GBFinePoint delta);
	void ResetZoom();
	void Zoom(short direction);
// following
	void Follow(GBObject * ob);
	bool Following() const;
	void Unfollow();
	void Refollow();
	void FollowRandom();
	void FollowRandomNear();
// tools
	void DoTool(const GBFinePoint where);
	void DoAddRobot(const GBFinePoint where);
	void DoAddSeed(const GBFinePoint where);
	void DoMove(const GBFinePoint where);
	void DoPull(const GBFinePoint where);
	void DoBlasts(const GBFinePoint where);
	void DoInspect(const GBFinePoint where);
	void DoDebug(const GBFinePoint where);
};

#endif
