// GBPortal.cpp
// code for the Portal view of a GBWorld
// Grobots (c) 2002-2008 Devon and Warren Schudy

#include <ctype.h>
#include "GBPortal.h"
#include "GBFood.h"
#include "GBShot.h"
#include "GBSide.h"
#include "GBRobotType.h"
#include "GBRandomState.h"
#include "GBRobot.h"
#include "GBStringUtilities.h"

const short kScale = 16; //default number of pixels per unit.
const short kMinDetailsScale = 10;
const GBSpeed kAutoScrollSpeed = 0.4;
const GBSpeed kFollowSpeed = 0.5;
const GBSpeed kFastFollowSpeed = 1.5;
const GBDistance kFastFollowDistance = 10;
const GBDistance kFollowJumpDistance = 30;
const GBDistance kAutofollowNearRange = 20;
const GBMilliseconds kAutofollowPeriod = 3000;
const GBDistance kFollowViewOffEdge = 3; //how much wall to show when following near edge

const GBForceScalar kMoveForce = 1;
const GBDamage kSmiteDamage = 200;
const int kNumBlasts = 10;
const GBDistance kBlastRange = 10;
const GBSpeed kBlastSpeed = 0.2;
const GBDamage kBlastDamage = 5;
const GBDistance kEraseBigRadius = 2;


const GBDistance kToolSpacings[kNumPortalTools] = {
	0, 1, 2, 10, 0, 0, 3, 0, 0, 0};
const GBFrames kToolIntervals[kNumPortalTools] = {
	0, 20, 10, 50, 1, 1, 10, 3, 1, 1};


void GBPortal::DrawBackground() {
	if (!background) InitBackground();
	long minTileX = floor(ViewLeft() / kBackgroundTileSize);
	long minTileY = floor(ViewBottom() / kBackgroundTileSize);
	long maxTileX = ceil(ViewRight() / kBackgroundTileSize);
	long maxTileY = ceil(ViewTop() / kBackgroundTileSize);
	for ( long yi = minTileY; yi <= maxTileY; yi ++ )
		for ( long xi = minTileX; xi <= maxTileX; xi ++ )
			DrawBackgroundTile(xi, yi);
}

void GBPortal::DrawBackgroundTile(long xi, long yi) {
	GBRect tile(ToScreenX(kBackgroundTileSize * xi),
		ToScreenY(kBackgroundTileSize * (yi + 1)),
		ToScreenX(kBackgroundTileSize * (xi + 1)),
		ToScreenY(kBackgroundTileSize * yi));
// if it's in the world, draw it:
	if ( xi >= 0 && yi >= 0 
			&& xi < world.BackgroundTilesX() && yi < world.BackgroundTilesY() ) {
	// draw tile
		Blit(*background, background->Bounds(), tile);
	} else // it's a wall
		DrawSolidRect(tile, GBColor::lightGray);
}

void GBPortal::DrawOneTile(const GBRect & b, GBGraphics & g) {
// black background
	g.DrawSolidRect(b, GBColor::black);
// fine grid
	GBColor fineColor(min(0.4f + 0.04f * scale / kScale,  0.15f + 0.25f * scale / kScale));
	short coarseThickness = 1 + scale / 20;
	GBColor coarseColor(0.4f + 0.4f * scale / kScale);
	
	for ( int i = 1; i < kBackgroundTileSize; i ++ ) {
		short x = b.left + i * scale;
		short y = b.top + i * scale;
		g.DrawLine(x, b.top, x, b.bottom, fineColor);
		g.DrawLine(b.left, y, b.right, y, fineColor);
	}
// coarse grid
	g.DrawLine(b.left, b.top, b.left, b.bottom, coarseColor, coarseThickness);
	g.DrawLine(b.left, b.top, b.right, b.top, coarseColor, coarseThickness);
}

void GBPortal::InitBackground() {
	if (background) delete background;
	background = new GBBitmap(scale * kBackgroundTileSize, scale * kBackgroundTileSize, Graphics());
	background->StartDrawing();
	DrawOneTile(background->Bounds(), background->Graphics());
	background->StopDrawing();
}

void GBPortal::DrawObjects() {
	long minTileX = max(floor(ViewLeft() / kForegroundTileSize - 0.5), 0L);
	long minTileY = max(floor(ViewBottom() / kForegroundTileSize - 0.5), 0L);
	long maxTileX = min(ceil(ViewRight() / kForegroundTileSize + 0.5), world.ForegroundTilesX() - 1);
	long maxTileY = min(ceil(ViewTop() / kForegroundTileSize + 0.5), world.ForegroundTilesY() - 1);
	long yi, xi;
	for ( yi = minTileY; yi <= maxTileY; yi ++ )
		for ( xi = minTileX; xi <= maxTileX; xi ++ )
			DrawObjectList(world.GetObjects(xi, yi, ocFood));
	DrawObjectList(world.GetLargeObjects(ocFood));
	for ( yi = minTileY; yi <= maxTileY; yi ++ )
		for ( xi = minTileX; xi <= maxTileX; xi ++ )
			DrawObjectList(world.GetObjects(xi, yi, ocRobot));
	DrawObjectList(world.GetLargeObjects(ocRobot));
	for ( yi = minTileY; yi <= maxTileY; yi ++ )
		for ( xi = minTileX; xi <= maxTileX; xi ++ )
			DrawObjectList(world.GetObjects(xi, yi, ocArea));
	DrawObjectList(world.GetLargeObjects(ocArea));
	for ( yi = minTileY; yi <= maxTileY; yi ++ )
		for ( xi = minTileX; xi <= maxTileX; xi ++ )
			DrawObjectList(world.GetObjects(xi, yi, ocShot));
	DrawObjectList(world.GetLargeObjects(ocShot));
	if ( showDecorations ) {
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, ocDecoration));
		DrawObjectList(world.GetLargeObjects(ocDecoration));
	}
	if ( showSensors ) {
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, ocSensorShot));
		DrawObjectList(world.GetLargeObjects(ocSensorShot));
	}
}

void GBPortal::DrawObjectList(const GBObject * list) {
	for ( const GBObject * cur = list; cur != nil; cur = cur->next ) {
		short diameter = round(max(cur->Radius() * 2 * scale, 1));
		GBRect where;
		where.left = ToScreenX(cur->Left());
		where.top = ToScreenY(cur->Top());
		where.right = where.left + diameter;
		where.bottom = where.top + diameter;
		if ( where.right > 0 && where.left < Width()
				&& where.bottom > 0 && where.top < Height() ) {
			cur->Draw(Graphics(), CalcExternalRect(where), showDetails && scale >= kMinDetailsScale);
		}
	}
}

short GBPortal::ToScreenX(const GBCoordinate x) const {
	return floor((x - viewpoint.x) * scale) + CenterX();
}

short GBPortal::ToScreenY(const GBCoordinate y) const {
	return floor((viewpoint.y - y) * scale) + CenterY();
}

GBCoordinate GBPortal::FromScreenX(const short h) const {
	return GBNumber(h - CenterX()) / scale + viewpoint.x;
}

GBCoordinate GBPortal::FromScreenY(const short v) const {
	return GBNumber(CenterY() - v) / scale + viewpoint.y;
}

GBFinePoint GBPortal::FromScreen(short x, short y) const {
	return GBFinePoint(FromScreenX(x), FromScreenY(y));
}

void GBPortal::RestrictScrolling() {
// prevent scrolling too far off edge.
	if ( viewpoint.x < world.Left() ) viewpoint.x = world.Left(); 
	if ( viewpoint.y < world.Bottom() ) viewpoint.y = world.Bottom();
	if ( viewpoint.x > world.Right() ) viewpoint.x = world.Right();
	if ( viewpoint.y > world.Top() ) viewpoint.y = world.Top();
}

GBPortal::GBPortal(GBWorld & newWorld)
	: world(newWorld),
	viewpoint(newWorld.Size() / 2), scale(kScale),
	following(false), followPosition(newWorld.Size() / 2), moving(nil),
	autofollow(false), lastFollow(0),
	tool(ptScroll),
	showSensors(false), showDecorations(true), showDetails(true), showSideNames(true),
	worldChanges(-1), selfChanges(-1),
	lastx(0), lasty(0), lastClick(), lastFrame(newWorld.CurrentFrame()),
	background(nil)
{}

GBPortal::~GBPortal() {
	if ( moving ) moving->RemoveDeletionListener(this);
	delete background;
}

void GBPortal::ScrollToFollowed() {
	if ( world.Followed() ) {
		followPosition = world.Followed()->Position();
		//viewpoint += world.Followed()->Velocity();
	}
	//User doesn't want to see what's off the edge.
	//a bit complicated because:
	//1) If the view is already near edge, don't scroll away from followed to fix
	//2) if zoomed out a lot, minFollowX could be greater than maxFollowX
	GBCoordinate minFollowX = GBNumber(Width()) / (scale * 2) - kFollowViewOffEdge; //half-width of viewport
	GBCoordinate maxFollowX = world.Right() - minFollowX;
	GBCoordinate minFollowY = GBNumber(Height()) / (scale * 2) - kFollowViewOffEdge;
	GBCoordinate maxFollowY = world.Top() - minFollowY;
	if ( followPosition.x >= maxFollowX ) {
		if(viewpoint.x <= followPosition.x)
			followPosition.x = max(viewpoint.x, maxFollowX);
	} else if ( followPosition.x <= minFollowX ) {
		if ( viewpoint.x >= followPosition.x )
			followPosition.x = min(viewpoint.x, minFollowX);
	} else if ( world.Followed() )
		viewpoint.x += world.Followed()->Velocity().x;
	
	if ( followPosition.y >= maxFollowY ) {
		if ( viewpoint.y <= followPosition.y )
			followPosition.y = max(viewpoint.y, maxFollowY);
	} else if ( followPosition.y <= minFollowY ) {
		if ( viewpoint.y >= followPosition.y )
			followPosition.y = min(viewpoint.y, minFollowY);
	} else if ( world.Followed() )
		viewpoint.y += world.Followed()->Velocity().y;
	//now scroll toward followPosition
	if ( followPosition.InRange(viewpoint, kFastFollowDistance) )
		ScrollToward(followPosition, kFollowSpeed);
	else if ( followPosition.InRange(viewpoint, kFollowJumpDistance) )
		ScrollToward(followPosition, kFastFollowSpeed);
	else
		viewpoint = followPosition;
}

void GBPortal::DrawRangeCircle(const GBPosition & center, GBDistance radius, const GBColor &color) {
	if ( radius <= 0 ) return;
	GBRect where(ToScreenX(center.x - radius), ToScreenY(center.y + radius),
				 ToScreenX(center.x + radius), ToScreenY(center.y - radius));
	DrawOpenOval(where, color);
}

void GBPortal::Draw() {
	if ( ViewRight() < 0 || ViewLeft() > world.Right() || ViewTop() < 0 || ViewBottom() > world.Top() )
		viewpoint = world.Size() / 2;
	if ( autofollow && Milliseconds() > lastFollow + kAutofollowPeriod )
		FollowRandom();
	if ( following && ! moving ) 
		ScrollToFollowed();
	DrawBackground();
	const GBRobot *bot = dynamic_cast<const GBRobot *>(world.Followed());
	if ( bot ) {
		DrawRangeCircle(bot->Position(), bot->hardware.blaster.MaxRange() + bot->Radius(), GBColor::magenta * 0.5f);
		DrawRangeCircle(bot->Position(), bot->hardware.grenades.MaxRange() + bot->Radius(), GBColor::yellow * 0.5f);
		if ( bot->hardware.syphon.MaxRate() > 0 )
			DrawRangeCircle(bot->Position(), bot->hardware.syphon.MaxRange() + bot->Radius(), GBColor(0.25f, 0.4f, 0.5f));
		if ( bot->hardware.enemySyphon.MaxRate() > 0 )
			DrawRangeCircle(bot->Position(), bot->hardware.enemySyphon.MaxRange() + bot->Radius(), GBColor(0.3f, 0.5f, 0));
		DrawRangeCircle(bot->Position(), bot->hardware.forceField.MaxRange(), GBColor(0, 0.4f, 0.5f));
	}
	DrawObjects();
	if ( world.Followed() ) {
		GBPosition targetPos = world.Followed()->Position();
		int texty = ToScreenY(targetPos.y - (world.Followed()->Radius() > 2 ? GBNumber(0) : world.Followed()->Radius())) + 13;
		DrawStringCentered(world.Followed()->Description(), ToScreenX(targetPos.x), texty,
			10, GBColor::white);
		const string & details = world.Followed()->Details();
		if (details.length() > 0)
			DrawStringCentered(details, ToScreenX(targetPos.x), texty + 10,
				10, GBColor::white);
	}
	if ( showSideNames ) {
		for (const GBSide *side = world.Sides(); side; side = side->next)
			if ( side->Scores().Seeded() )
				DrawStringCentered(side->Name(), ToScreenX(side->center.x), ToScreenY(side->center.y),
					14, side->Scores().Sterile() ? GBColor::gray : GBColor::white);
	}
// record drawn
	worldChanges = world.ChangeCount();
	selfChanges = ChangeCount();
}

bool GBPortal::InstantChanges() const {
	return worldChanges != world.ChangeCount() || selfChanges != ChangeCount() || Following();
}

void GBPortal::AcceptClick(short x, short y, int /*clicks*/) {
	lastx = x; lasty = y;
	lastClick = FromScreen(x, y);
	if ( moving ) {
		moving->RemoveDeletionListener(this);
		moving = nil;
	}
	DoTool(lastClick);
	lastFrame = world.CurrentFrame();
	autofollow = false;
}

void GBPortal::AcceptDrag(short x, short y) {
	GBPosition spot = FromScreen(x, y);
	if ( tool == ptScroll ) {
		viewpoint += FromScreen(lastx, lasty) - spot;
		followPosition = viewpoint;
		lastx = x; lasty = y;
		lastClick = spot;
		lastFrame = world.CurrentFrame();
		Changed();
	} else {
		if ( x < 0 || x > Width() 
				|| y < 0 || y > Height() ) {
			ScrollToward(spot, kAutoScrollSpeed);
			following = false;
		}
		GBDistance dist = (spot - lastClick).Norm();
		GBFrames frames = world.CurrentFrame() - lastFrame;
		if ( dist >= kToolSpacings[tool] && kToolSpacings[tool]
				|| frames >= kToolIntervals[tool] && kToolIntervals[tool] ) {
			DoTool(spot);
			lastx = x; lasty = y;
			lastClick = spot;
			lastFrame = world.CurrentFrame();
		}
	}
}

void GBPortal::AcceptUnclick(short x, short y, int clicks) {
	AcceptDrag(x, y);
	if ( moving ) {
		moving->RemoveDeletionListener(this);
		moving = nil;
	}
	if ( clicks && tool == ptScroll )
		Follow(world.ObjectNear(FromScreen(x, y), false /*showSensors*/));
}

void GBPortal::AcceptKeystroke(const char what) {
	switch ( tolower(what) ) {
		case ' ': tool = ptScroll; break;
		case 'm': tool = ptAddManna; break;
		case 'r': tool = ptAddRobot; break;
		case 's': tool = ptAddSeed; break;
		case 'v': tool = ptMove; break;
		case 'p': tool = ptPull; break;
		case 'x': tool = ptSmite; break;
		case 'b': tool = ptBlasts; break;
		case 'e': tool = ptErase; break;
		case 'a': tool = ptEraseBig; break;
		case '\n': case '\r': FollowRandom(); break;
		case '\t': FollowRandomNear(); break;
		case '`': Refollow(); break;
		case '-': Zoom(-1); break;
		case '+': case '=': Zoom(1); break;
		case '0': ResetZoom(); break;
		default: break;
	}
}

const string GBPortal::Name() const {
	return "World";
}

GBCursor GBPortal::Cursor() const {
	switch ( tool ) {
		case ptScroll: return cuArrow;
		default: return cuCross;
	}
}

bool GBPortal::Resizable() const {
	return true;
}

short GBPortal::PreferredWidth() const {
	return 311;
}

short GBPortal::PreferredHeight() const {
	return 311;
}

void GBPortal::SetSize(short width, short height) {
	GBView::SetSize(width, height);
	worldChanges = -1;
	Changed();
}

GBCoordinate GBPortal::ViewLeft() const {
	return viewpoint.x - GBNumber(Width()) / (scale * 2);
}

GBCoordinate GBPortal::ViewTop() const {
	return viewpoint.y + GBNumber(Height()) / (scale * 2);
}

GBCoordinate GBPortal::ViewRight() const {
	return viewpoint.x + GBNumber(Width()) / (scale * 2);
}

GBCoordinate GBPortal::ViewBottom() const {
	return viewpoint.y - GBNumber(Height()) / (scale * 2);
}

void GBPortal::ScrollTo(const GBFinePoint p) {
	viewpoint = p;
	RestrictScrolling();
	Changed();
}

void GBPortal::ScrollToward(const GBFinePoint p, const GBSpeed speed) {
	if ( viewpoint.InRange(p, speed) )
		viewpoint = p;
	else
		viewpoint += (p - viewpoint).Unit() * speed;
	RestrictScrolling();
	Changed();
}

void GBPortal::ScrollBy(const GBFinePoint delta) {
	viewpoint += delta;
	RestrictScrolling();
	Changed();
}

void GBPortal::Follow(GBObject * ob) {
	if ( ob ) {
		world.Follow(ob);
		followPosition = ob->Position();
		following = true;
		lastFollow = Milliseconds();
	}
}

void GBPortal::ResetZoom() {
	scale = kScale;
	InitBackground();
	Changed();
}

void GBPortal::Zoom(short direction) {
	if (direction < 0 ? scale <= 4 : scale >= 64)
		return;
	scale += direction * max(1, scale/9);
	InitBackground();
	Changed();
}

bool GBPortal::Following() const {
	return following && followPosition != viewpoint;
}

void GBPortal::Unfollow() { following = false; autofollow = false; }
void GBPortal::Refollow() { following = true; }

void GBPortal::FollowRandom() {
	Follow(world.RandomInterestingObject());
}

void GBPortal::FollowRandomNear() {
	Follow(world.RandomInterestingObjectNear(viewpoint, kAutofollowNearRange));
}

void GBPortal::DoTool(const GBFinePoint where) {
	switch ( tool ) {
		case ptScroll: following = false; break;
		case ptAddManna:
			world.AddObjectDirectly(new GBManna(where, world.mannaSize));
			world.Changed();
			break;
		case ptAddRobot: DoAddRobot(where); break;
		case ptAddSeed: DoAddSeed(where); break;
		case ptMove: DoMove(where); break;
		case ptPull: DoPull(where); break;
		case ptSmite:
			world.AddObjectNew(new GBExplosion(where, nil /* nobody */, kSmiteDamage));
			world.Changed();
			break;
		case ptBlasts: DoBlasts(where); break;
		case ptErase:
			world.EraseAt(where, 0);
			world.Changed();
			world.CollectStatistics();
			break;
		case ptEraseBig:
			world.EraseAt(where, kEraseBigRadius);
			world.Changed();
			world.CollectStatistics();
			break;
		default: break;
	}
}

void GBPortal::DoAddRobot(const GBFinePoint where) {
	GBSide * side = world.SelectedSide();
	if ( side ) {
		GBRobotType * type = side->SelectedType();
		if ( ! type )
			type = side->GetFirstType();
		if ( type ) {
			world.AddObjectDirectly(new GBRobot(type, where));
			side->Scores().ReportSeeded(type->Cost());
			world.Changed();
			world.CollectStatistics();
		}
	}
}

void GBPortal::DoAddSeed(const GBFinePoint where) {
	GBSide * side = world.SelectedSide();
	if ( side ) {
		world.AddSeed(side, where);
		world.Changed();
		world.CollectStatistics();
	}
}

void GBPortal::DoMove(const GBFinePoint where) {
	if ( ! moving ) {
		moving = world.ObjectNear(where, showSensors);
		if ( moving ) moving->AddDeletionListener(this);
	}
	if ( moving ) {
		moving->MoveBy(where - lastClick);
		world.Changed();
	}
}

void GBPortal::DoPull(const GBFinePoint where) {
	if ( where == lastClick ) return;
	GBForceField * ff = new GBForceField(where, where - lastClick, nil, kMoveForce, (where - lastClick).Angle());
	world.AddObjectNew(ff);
	world.Changed();
}

void GBPortal::DoBlasts(const GBFinePoint where) {
	GBAngle base = gRandoms.Angle();
	for ( int i = kNumBlasts; i > 0; i -- )
		world.AddObjectNew(new GBBlast(where,
			GBFinePoint::MakePolar(kBlastSpeed, base + kPi * 2 * i / kNumBlasts),
			nil, kBlastDamage, ceil(kBlastRange / kBlastSpeed)));
	world.Changed();
}

void GBPortal::ReportDeletion(const GBDeletionReporter * deletee) {
	if ( deletee == (const GBDeletionReporter *)moving )
		moving = nil;
}

