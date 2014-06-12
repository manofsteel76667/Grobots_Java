package views;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;
import sides.RobotType;
import sides.Side;
import simulation.GBBlast;
import simulation.GBExplosion;
import simulation.GBForceField;
import simulation.GBManna;
import simulation.GBObject;
import simulation.GBProjection;
import simulation.GBRobot;
import simulation.GBWorld;
import support.FinePoint;
import support.GBColor;
import support.GBMath;
import support.GBObjectClass;
import support.GBRandomState;
import ui.GBApplication;

public class GBPortal extends GBView implements GBProjection {
	/**
	 * 
	 */
	private static final long serialVersionUID = -861108527551257687L;
	// GBPortal.h
	// a view of [part of] a GBWorld
	// Grobots (c) 2002-2008 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	public enum toolTypes {
		ptScroll(0,0),
		ptAddManna(1,20), ptAddRobot(2,10), ptAddSeed(10,50),
		ptMove(0,1),
		ptPull(0,1),
		ptSmite(3,10), ptBlasts(0,3),
		ptErase(0,1), ptEraseBig(0,1);
		public final double spacing;
		public final int interval;
		public int value;
		toolTypes(double _spacing, int _interval){
			spacing=_spacing;
			interval=_interval;
			value = this.ordinal();
		}
	};

		GBApplication app;
		GBWorld world;
		FinePoint viewpoint;
		public int scale; //pixels per unit
		 boolean following;
		FinePoint followPosition;
		long lastFollow;
		GBObject moving;
		long worldChanges;
		long selfChanges;
	// tool use
		 int lastx, lasty; // where mouse was last if we're dragging
		FinePoint lastClick;
		int lastFrame; // when last tool effect was
		BufferedImage background;
	//public:
		public boolean autofollow;
		public toolTypes tool;
		public  boolean showSensors;
		public  boolean showDecorations;
		public  boolean showDetails;
		public  boolean showSideNames;

	public static final  int kScale = 16; //default number of pixels per unit.
	public static final  int kMinDetailsScale = 10;
	public static final double kAutoScrollSpeed = 0.4;
	public static final double kFollowSpeed = 0.5;
	public static final double kFastFollowSpeed = 1.5;
	public static final double kFastFollowDistance = 10;
	public static final double kFollowJumpDistance = 30;
	public static final double kAutofollowNearRange = 20;
	public static final long kAutofollowPeriod = 3000L;
	public static final double kFollowViewOffEdge = 3; //how much wall to show when following near edge

	public static final double kMoveForce = 1;
	public static final double kSmiteDamage = 200;
	public static final int kNumBlasts = 10;
	public static final double kBlastRange = 10;
	public static final double kBlastSpeed = 0.2;
	public static final double kBlastDamage = 5;
	public static final double kEraseBigRadius = 2;
	
	public GBPortal(GBApplication _app){
		app = _app;
		world = _app.world;
		viewpoint=new FinePoint(app.world.Size().divide(2)); 
		scale=kScale;
		followPosition=new FinePoint(app.world.Size().divide(2));		 
		tool=toolTypes.ptScroll;
		showSensors=false; showDecorations=true; showDetails=true; showSideNames=true;
		worldChanges=-1; selfChanges=-1;
		lastClick=new FinePoint(); lastFrame=app.world.CurrentFrame();
		this.setPreferredSize(new Dimension(311,311));
		this.setDoubleBuffered(true);
		this.setOpaque(true);
		this.setBackground(GBColor.black);
	}
	@Override
	public void paintComponent(Graphics g){
		background = new BufferedImage(scale * world.ForegroundTilesX() * GBWorld.kBackgroundTileSize,
				scale * world.ForegroundTilesY() * GBWorld.kBackgroundTileSize,
				BufferedImage.TYPE_INT_ARGB);		
	}
	
	void DrawBackground() {
		 int minTileX = (int) Math.floor(ViewLeft() / GBWorld.kBackgroundTileSize);
		 int minTileY = (int) Math.floor(ViewBottom() / GBWorld.kBackgroundTileSize);
		 int maxTileX = (int) Math.ceil(ViewRight() / GBWorld.kBackgroundTileSize);
		 int maxTileY = (int) Math.ceil(ViewTop() / GBWorld.kBackgroundTileSize);
		for (  int yi = minTileY; yi <= maxTileY; yi ++ )
			for (  int xi = minTileX; xi <= maxTileX; xi ++ )
				DrawBackgroundTile(xi, yi);
	}

	void DrawBackgroundTile( int xi,  int yi) {
		Rectangle tile=new Rectangle(ToScreenX(GBWorld.kBackgroundTileSize * xi),
			ToScreenY(GBWorld.kBackgroundTileSize * (yi + 1)),
			ToScreenX(GBWorld.kBackgroundTileSize * (xi + 1)),
			ToScreenY(GBWorld.kBackgroundTileSize * yi));
	// if it's in the world, draw it:
		if ( xi >= 0 && yi >= 0 
				&& xi < world.BackgroundTilesX() && yi < world.BackgroundTilesY() ) {
		// draw tile
			Blit(background, background.get.Bounds(), tile);
		} else // it's a wall{
			setColor(GBColor.lightGray);
			fillShape(tile, GBColor.lightGray);
	}
	}

	void DrawOneTile( Rectangle b, Graphics2D g) {
	// black background
		g.setColor(GBColor.BLACK);
		g.fill(b);
	// fine grid
		GBColor fineColor=new GBColor(Math.min(0.4f + 0.04f * scale / kScale,  0.15f + 0.25f * scale / kScale));
		 int coarseThickness = 1 + scale / 20;
		GBColor coarseColor=new GBColor(0.4f + 0.4f * scale / kScale);
		g.setColor(fineColor);
		for ( int i = 1; i < GBWorld.kBackgroundTileSize; i ++ ) {
			 int x = (int) (b.getMinX() + i * scale);
			 int y = (int) (b.getMinY() + i * scale);
			g.drawLine(x, (int)b.getMinY(), x, (int)b.getMaxY());
			g.drawLine((int)b.getMinX(), y, (int)b.getMaxX(), y);
		}
	// coarse grid
		g.setColor(coarseColor);
		g.setStroke(new BasicStroke(coarseThickness));
		g.draw(b);
	}

	/*void InitBackground() {
		background = new BufferedImage(scale * GBWorld.kBackgroundTileSize, scale * GBWorld.kBackgroundTileSize);
		background.StartDrawing();
		DrawOneTile(background.Bounds(), background.Graphics());
		background.StopDrawing();
	}*/

	void DrawObjects() throws GBIndexOutOfRangeError {
		 int minTileX = (int) Math.max(Math.floor(ViewLeft() / GBWorld.kForegroundTileSize - 0.5), 0L);
		 int minTileY = (int) Math.max(Math.floor(ViewBottom() / GBWorld.kForegroundTileSize - 0.5), 0L);
		 int maxTileX = (int) Math.min(Math.ceil(ViewRight() / GBWorld.kForegroundTileSize + 0.5), world.ForegroundTilesX() - 1);
		 int maxTileY = (int) Math.min(Math.ceil(ViewTop() / GBWorld.kForegroundTileSize + 0.5), world.ForegroundTilesY() - 1);
		 int yi, xi;
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocFood));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocFood));
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocRobot));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocRobot));
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocArea));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocArea));
		for ( yi = minTileY; yi <= maxTileY; yi ++ )
			for ( xi = minTileX; xi <= maxTileX; xi ++ )
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocShot));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocShot));
		if ( showDecorations ) {
			for ( yi = minTileY; yi <= maxTileY; yi ++ )
				for ( xi = minTileX; xi <= maxTileX; xi ++ )
					DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocDecoration));
			DrawObjectList(world.GetLargeObjects(GBObjectClass.ocDecoration));
		}
		if ( showSensors ) {
			for ( yi = minTileY; yi <= maxTileY; yi ++ )
				for ( xi = minTileX; xi <= maxTileX; xi ++ )
					DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocSensorShot));
			DrawObjectList(world.GetLargeObjects(GBObjectClass.ocSensorShot));
		}
	}

	void DrawObjectList( GBObject list) {
		for ( GBObject cur = list; cur != null; cur = cur.next ) {
			 int diameter = (int) Math.round(Math.max(cur.Radius() * 2 * scale, 1));
			Rectangle where = new Rectangle();
			where.x = ToScreenX(cur.Left());
			where.y = ToScreenY(cur.Top());
			where.height = diameter;
			where.width = diameter;
			if ( where.height > 0 && where.x < Width()
					&& where.width > 0 && where.y < Height() ) {
				//cur.Draw(getGraphics(), CalcExternalRect(where), showDetails && scale >= kMinDetailsScale);
			}
		}
	}

	 int ToScreenX( double x) {
		return (int) (Math.floor((x - viewpoint.x) * scale) + CenterX());
	}

	 int ToScreenY( double y) {
		return (int) (Math.floor((viewpoint.y - y) * scale) + CenterY());
	}

	double FromScreenX(  int h) {
		return (h - CenterX()) / scale + viewpoint.x;
	}

	double FromScreenY(  int v) {
		return (CenterY() - v) / scale + viewpoint.y;
	}

	FinePoint FromScreen( int x,  int y) {
		return new FinePoint(FromScreenX(x), FromScreenY(y));
	}

	void RestrictScrolling() {
	// prevent scrolling too far off edge.
		if ( viewpoint.x < world.Left() ) viewpoint.x = (int) world.Left(); 
		if ( viewpoint.y < world.Bottom() ) viewpoint.y = (int) world.Bottom();
		if ( viewpoint.x > world.Right() ) viewpoint.x = (int) world.Right();
		if ( viewpoint.y > world.Top() ) viewpoint.y = (int) world.Top();
	}

	void ScrollToFollowed() {
		if ( world.Followed()!=null ) {
			followPosition = world.Followed().Position();
			//viewpoint += world.Followed().Velocity();
		}
		//User doesn't want to see what's off the edge.
		//a bit complicated because:
		//1) If the view is already near edge, don't scroll away from followed to fix
		//2) if zoomed out a lot, minFollowX could be greater than maxFollowX
		double minFollowX = (Width()) / (scale * 2) - kFollowViewOffEdge; //half-width of viewport
		double maxFollowX = world.Right() - minFollowX;
		double minFollowY = (Height()) / (scale * 2) - kFollowViewOffEdge;
		double maxFollowY = world.Top() - minFollowY;
		if ( followPosition.x >= maxFollowX ) {
			if(viewpoint.x <= followPosition.x)
				followPosition.x = Math.max(viewpoint.x, maxFollowX);
		} else if ( followPosition.x <= minFollowX ) {
			if ( viewpoint.x >= followPosition.x )
				followPosition.x = Math.min(viewpoint.x, minFollowX);
		} else if ( world.Followed()!=null )
			viewpoint.x += world.Followed().Velocity().x;
		
		if ( followPosition.y >= maxFollowY ) {
			if ( viewpoint.y <= followPosition.y )
				followPosition.y = Math.max(viewpoint.y, maxFollowY);
		} else if ( followPosition.y <= minFollowY ) {
			if ( viewpoint.y >= followPosition.y )
				followPosition.y = Math.min(viewpoint.y, minFollowY);
		} else if ( world.Followed()!=null )
			viewpoint.y += world.Followed().Velocity().y;
		//now scroll toward followPosition
		if ( followPosition.inRange(viewpoint, kFastFollowDistance) )
			ScrollToward(followPosition, kFollowSpeed);
		else if ( followPosition.inRange(viewpoint, kFollowJumpDistance) )
			ScrollToward(followPosition, kFastFollowSpeed);
		else
			viewpoint = followPosition;
	}

	void DrawRangeCircle( FinePoint center, double radius, GBColor color) {
		if ( radius <= 0 ) return;
		Rectangle where = new Rectangle(ToScreenX(center.x - radius), ToScreenY(center.y + radius),
					 ToScreenX(center.x + radius), ToScreenY(center.y - radius));
		//DrawOpenOval(where, color);
	}

	public void Draw() {
		if ( ViewRight() < 0 || ViewLeft() > world.Right() || ViewTop() < 0 || ViewBottom() > world.Top() )
			viewpoint = world.Size().divide(2);
		if ( autofollow && System.currentTimeMillis() > lastFollow + kAutofollowPeriod )
			FollowRandom();
		if ( following && moving==null ) 
			ScrollToFollowed();
		DrawBackground();
		GBRobot bot = (GBRobot)world.Followed();
		if ( bot!=null ) {
			DrawRangeCircle(bot.Position(), bot.hardware.blaster.MaxRange() + bot.Radius(), GBColor.magenta.multiply(0.5f));
			DrawRangeCircle(bot.Position(), bot.hardware.grenades.MaxRange() + bot.Radius(), GBColor.yellow.multiply(0.5f));
			if ( bot.hardware.syphon.MaxRate() > 0 )
				DrawRangeCircle(bot.Position(), bot.hardware.syphon.MaxRange() + bot.Radius(), new GBColor(0.25f, 0.4f, 0.5f));
			if ( bot.hardware.enemySyphon.MaxRate() > 0 )
				DrawRangeCircle(bot.Position(), bot.hardware.enemySyphon.MaxRange() + bot.Radius(), new GBColor(0.3f, 0.5f, 0));
			DrawRangeCircle(bot.Position(), bot.hardware.forceField.MaxRange(), new GBColor(0, 0.4f, 0.5f));
		}
		DrawObjects();
		if ( world.Followed()!=null ) {
			FinePoint targetPos = world.Followed().Position();
			int texty = ToScreenY(targetPos.y - (world.Followed().Radius() > 2 ? 0 : world.Followed().Radius())) + 13;
			DrawStringCentered(world.Followed().toString(), ToScreenX(targetPos.x), texty,
				10, GBColor.white);
			String details = world.Followed().Details();
			if (details.length() > 0)
				DrawStringCentered(details, ToScreenX(targetPos.x), texty + 10,
					10, GBColor.white);
		}
		if ( showSideNames ) {
			for ( Side side : world.Sides())
				if ( side.Scores().Seeded()!=0 )
					DrawStringCentered(side.Name(), ToScreenX(side.center.x), ToScreenY(side.center.y),
						14, side.Scores().sterile!=0 ? GBColor.gray : GBColor.white);
		}
	// record drawn
		//worldChanges = world.ChangeCount();
		//selfChanges = ChangeCount();
	}

	public boolean InstantChanges() {
		//TODO: check what this does
		return true;//worldChanges != world.ChangeCount() || selfChanges != ChangeCount() || Following();
	}

	public void AcceptClick( int x,  int y, int clicks) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError {
		lastx = x; lasty = y;
		lastClick = FromScreen(x, y);
		moving = null;
		DoTool(lastClick);
		lastFrame = world.CurrentFrame();
		autofollow = false;
	}

	public void AcceptDrag( int x,  int y) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError {
		FinePoint spot = FromScreen(x, y);
		if ( tool == toolTypes.ptScroll ) {
			viewpoint = viewpoint.add( FromScreen(lastx, lasty).subtract(spot));
			followPosition = viewpoint;
			lastx = x; lasty = y;
			lastClick = spot;
			lastFrame = world.CurrentFrame();
			//Changed();
		} else {
			if ( x < 0 || x > Width() 
					|| y < 0 || y > Height() ) {
				ScrollToward(spot, kAutoScrollSpeed);
				following = false;
			}
			double dist = spot.subtract(lastClick).norm();
			int frames = world.CurrentFrame() - lastFrame;
			if ( dist >= tool.spacing && tool.spacing !=0
					|| frames >= tool.interval && tool.interval!=0 ) {
				DoTool(spot);
				lastx = x; lasty = y;
				lastClick = spot;
				lastFrame = world.CurrentFrame();
			}
		}
	}

	public void AcceptUnclick( int x,  int y, int clicks) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError {
		AcceptDrag(x, y);

			moving = null;
		if ( clicks!=0 && tool == toolTypes.ptScroll )
			Follow(world.ObjectNear(FromScreen(x, y), false /*showSensors*/));
	}

	//public final String Name() {
	//	return "World";
	//}

	public  boolean Resizable() {
		return true;
	}

	public void SetSize( int width,  int height) {
		setPreferredSize(new Dimension(width, height));
		worldChanges = -1;
		//Changed();
	}

	public double ViewLeft() {
		return viewpoint.x - Width() / (scale * 2);
	}

	public double ViewTop() {
		return viewpoint.y + Height() / (scale * 2);
	}

	public double ViewRight() {
		return viewpoint.x + Width() / (scale * 2);
	}

	public double ViewBottom() {
		return viewpoint.y - Height() / (scale * 2);
	}

	public void ScrollTo( FinePoint p) {
		viewpoint = p;
		RestrictScrolling();
		//Changed();
	}

	public void ScrollToward( FinePoint p, double speed) {
		if ( viewpoint.inRange(p, speed) )
			viewpoint = p;
		else
			viewpoint = viewpoint.add(p.subtract(viewpoint).unit().multiply(speed));
		RestrictScrolling();
		//Changed();
	}

	public void ScrollBy( FinePoint delta) {
		viewpoint=viewpoint.add(delta);
		RestrictScrolling();
		//Changed();
	}

	public void Follow(GBObject ob) {
		if ( ob !=null) {
			world.Follow(ob);
			followPosition = ob.Position();
			following = true;
			lastFollow = System.currentTimeMillis();
		}
	}

	public void ResetZoom() {
		scale = kScale;
		//InitBackground();
		//Changed();
		repaint();
	}

	public void Zoom( int direction) {
		if (direction < 0 ? scale <= 4 : scale >= 64)
			return;
		scale += direction * Math.max(1, scale/9);
		//InitBackground();
		//Changed();
		repaint();
	}

	public  boolean Following() {
		return following && followPosition != viewpoint;
	}

	public void Unfollow() { following = false; autofollow = false; }
	public void Refollow() { following = true; }

	public void FollowRandom() {
		Follow(world.RandomInterestingObject());
	}

	public void FollowRandomNear() {
		Follow(world.RandomInterestingObjectNear(viewpoint, kAutofollowNearRange));
	}

	public void DoTool( FinePoint where) throws GBNilPointerError, GBBadArgumentError, GBAbort, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError {
		switch ( tool ) {
			case ptScroll: following = false; break;
			case ptAddManna:
				world.AddObjectNew(new GBManna(where, world.mannaSize));
				world.Changed();
				break;
			case ptAddRobot: DoAddRobot(where); break;
			case ptAddSeed: DoAddSeed(where); break;
			case ptMove: DoMove(where); break;
			case ptPull: DoPull(where); break;
			case ptSmite:
				world.AddObjectNew(new GBExplosion(where, null /* nobody */, kSmiteDamage));
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

	public void DoAddRobot( FinePoint where) throws GBBadSymbolIndexError, GBIndexOutOfRangeError, GBNilPointerError, GBGenericError, GBOutOfMemoryError, GBAbort {
		Side side = app.selectedSide;
		if ( side!=null ) {
			RobotType type = app.selectedType;
			if ( type==null )
				if (side.types.size() > 0)
				type = side.types.get(0);
			if ( type !=null) {
				world.AddObjectNew(new GBRobot(type, where));
				side.Scores().ReportSeeded(type.Cost());
				world.Changed();
				world.CollectStatistics();
			}
		}
	}

	public void DoAddSeed( FinePoint where) throws GBAbort {
		Side side = world.SelectedSide();
		if ( side!=null ) {
			world.AddSeed(side, where);
			world.Changed();
			world.CollectStatistics();
		}
	}

	public void DoMove( FinePoint where) {
		if ( moving==null ) {
			moving = world.ObjectNear(where, showSensors);
		}
		if ( moving!=null ) {
			moving.MoveBy(where.subtract(lastClick));
			world.Changed();
		}
	}

	public void DoPull( FinePoint where) throws GBNilPointerError {
		if ( where == lastClick ) return;
		GBForceField ff = new GBForceField(where, where.subtract(lastClick), null, kMoveForce, where.subtract(lastClick).angle());
		world.AddObjectNew(ff);
		world.Changed();
	}

	public void DoBlasts( FinePoint where) throws GBNilPointerError {
		double base = GBRandomState.gRandoms.Angle();
		for ( int i = kNumBlasts; i > 0; i -- )
			world.AddObjectNew(new GBBlast(where,
				FinePoint.makePolar(kBlastSpeed, base + GBMath.kPi * 2 * i / kNumBlasts),
				null, kBlastDamage, (int) Math.ceil(kBlastRange / kBlastSpeed)));
		world.Changed();
	}
}
