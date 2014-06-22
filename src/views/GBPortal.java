// GBPortal.h
// a view of [part of] a GBWorld
// Grobots (c) 2002-2008 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JLayeredPane;
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
import simulation.GBObjectWorld;
import simulation.GBProjection;
import simulation.GBRobot;
import simulation.GBWorld;
import support.FinePoint;
import support.GBColor;
import support.GBGraphics;
import support.GBMath;
import support.GBObjectClass;
import support.GBRandomState;
import ui.GBApplication;

public class GBPortal extends JPanel implements GBProjection {
	/**
	 * 
	 */
	private static final long serialVersionUID = -861108527551257687L;

	public enum toolTypes {
		ptScroll(0, 0), ptAddManna(1, 20), ptAddRobot(2, 10), ptAddSeed(10, 50), ptMove(
				0, 1), ptPull(0, 1), ptSmite(3, 10), ptBlasts(0, 3), ptErase(0,
				1), ptEraseBig(0, 1);
		public final double spacing;
		public final int interval;
		public final int value;

		toolTypes(double _spacing, int _interval) {
			spacing = _spacing;
			interval = _interval;
			value = this.ordinal();
		}
	};

	GBApplication app;
	GBWorld world;
	FinePoint viewpoint;
	public int scale; // pixels per unit
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
	// public:
	public boolean autofollow;
	public toolTypes currentTool;
	public boolean showSensors;
	public boolean showDecorations;
	public boolean showDetails;
	public boolean showSideNames;
	// Drawing panels

	public static final int kScale = 8; // default number of pixels per unit.
	public static final int kMinDetailsScale = 10;
	public static final double kAutoScrollSpeed = 0.4;
	public static final double kFollowSpeed = 0.5;
	public static final double kFastFollowSpeed = 1.5;
	public static final double kFastFollowDistance = 10;
	public static final double kFollowJumpDistance = 30;
	public static final double kAutofollowNearRange = 20;
	public static final long kAutofollowPeriod = 3000L;
	public static final double kFollowViewOffEdge = 3; // how much wall to show
														// when following near
														// edge

	public static final double kMoveForce = 1;
	public static final double kSmiteDamage = 200;
	public static final int kNumBlasts = 10;
	public static final double kBlastRange = 10;
	public static final double kBlastSpeed = 0.2;
	public static final double kBlastDamage = 5;
	public static final double kEraseBigRadius = 2;

	public GBPortal(GBApplication _app) {
		super();
		app = _app;
		world = _app.world;
		viewpoint = new FinePoint(app.world.Size().divide(2));
		scale = kScale;
		followPosition = new FinePoint(viewpoint);
		currentTool = toolTypes.ptScroll;
		showDecorations = true;
		showDetails = true;
		showSideNames = true;
		worldChanges = -1;
		selfChanges = -1;
		lastClick = new FinePoint();
		lastFrame = app.world.CurrentFrame();
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				int x = arg0.getX();
				int y = arg0.getY();
				lastx = x;
				lasty = y;
				lastClick = FromScreen(x, y);
				moving = null;
				DoTool(lastClick);
				lastFrame = world.CurrentFrame();
				autofollow = false;
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// Follow object on double-click
				if (arg0.getClickCount() != 0
						&& currentTool == toolTypes.ptScroll)
					Follow(world
							.ObjectNear(FromScreen(arg0.getX(), arg0.getY()),
									false /* showSensors */));
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				int x = arg0.getX();
				int y = arg0.getY();
				FinePoint spot = FromScreen(x, y);
				if (currentTool == toolTypes.ptScroll) {
					viewpoint = viewpoint.add(FromScreen(lastx, lasty)
							.subtract(spot));
					followPosition = viewpoint;
					lastx = x;
					lasty = y;
					lastClick = spot;
					lastFrame = world.CurrentFrame();
					// Changed();
				} else {
					if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
						ScrollToward(spot, kAutoScrollSpeed);
						following = false;
					}
					double dist = spot.subtract(lastClick).norm();
					int frames = world.CurrentFrame() - lastFrame;
					if (dist >= currentTool.spacing && currentTool.spacing != 0
							|| frames >= currentTool.interval
							&& currentTool.interval != 0) {
						DoTool(spot);
						lastx = x;
						lasty = y;
						lastClick = spot;
						lastFrame = world.CurrentFrame();
					}
				}
				moving = null;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				// Zoom with mouse wheel
				int notches = arg0.getWheelRotation();
				if (notches == 0)
					return;
				int dir = (int) (notches / Math.abs(notches));
				doZoom(dir * -1);
			}
		};
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
		setBackground(Color.LIGHT_GRAY);
		this.setIgnoreRepaint(true);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 600);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Set colors for grid lines
		GBColor fineColor = new GBColor(Math.min(0.4f + 0.04f * scale / kScale,
				0.15f + 0.25f * scale / kScale));
		// int coarseThickness = 1 + scale / 20;
		// GBColor coarseColor = new GBColor(0.4f + 0.4f * scale / kScale);
		Graphics2D g2d = (Graphics2D) g;
		int minTileX = (int) Math.max(
				Math.floor(viewLeft() / GBObjectWorld.kBackgroundTileSize), 0);
		int minTileY = (int) Math
				.max(Math.floor(viewBottom()
						/ GBObjectWorld.kBackgroundTileSize), 0);
		int maxTileX = (int) Math.ceil(Math.min(viewRight(),
				GBObjectWorld.kWorldWidth) / GBObjectWorld.kBackgroundTileSize);
		int maxTileY = (int) Math
				.ceil(Math.min(viewTop(), GBObjectWorld.kWorldHeight)
						/ GBObjectWorld.kBackgroundTileSize);
		for (int yi = minTileY; yi < maxTileY; yi++)
			for (int xi = minTileX; xi < maxTileX; xi++) {
				Rectangle tile = new Rectangle(
						ToScreenX(GBObjectWorld.kBackgroundTileSize * xi),
						ToScreenY(GBObjectWorld.kBackgroundTileSize * (yi + 1)),
						GBObjectWorld.kBackgroundTileSize * scale,
						GBObjectWorld.kBackgroundTileSize * scale); // Tile
				g2d.setColor(Color.black);
				g2d.fill(tile); // Fine grid
				g2d.setColor(fineColor);
				g2d.draw(tile); // Coarse grid }
			}
		for (GBObjectClass cl : GBObjectClass.values())
			if (cl != GBObjectClass.ocDead)
				for (GBObject spot : world.objects.get(cl))
					for (GBObject ob = spot; ob != null; ob = ob.next)
						ob.Draw(g, this, this.getBounds(), false);
	}

	void DrawObjects() throws GBIndexOutOfRangeError {
		int minTileX = (int) Math.max(Math.floor(viewLeft()
				/ GBObjectWorld.kForegroundTileSize - 0.5), 0L);
		int minTileY = (int) Math.max(
				Math.floor(viewBottom() / GBObjectWorld.kForegroundTileSize
						- 0.5), 0L);
		int maxTileX = (int) Math.min(Math.ceil(viewRight()
				/ GBObjectWorld.kForegroundTileSize + 0.5), world
				.ForegroundTilesX() - 1);
		int maxTileY = (int) Math.min(
				Math.ceil(viewTop() / GBObjectWorld.kForegroundTileSize + 0.5),
				world.ForegroundTilesY() - 1);
		int yi, xi;
		for (yi = minTileY; yi <= maxTileY; yi++)
			for (xi = minTileX; xi <= maxTileX; xi++)
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocFood));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocFood));
		for (yi = minTileY; yi <= maxTileY; yi++)
			for (xi = minTileX; xi <= maxTileX; xi++)
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocRobot));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocRobot));
		for (yi = minTileY; yi <= maxTileY; yi++)
			for (xi = minTileX; xi <= maxTileX; xi++)
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocArea));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocArea));
		for (yi = minTileY; yi <= maxTileY; yi++)
			for (xi = minTileX; xi <= maxTileX; xi++)
				DrawObjectList(world.GetObjects(xi, yi, GBObjectClass.ocShot));
		DrawObjectList(world.GetLargeObjects(GBObjectClass.ocShot));
		if (showDecorations) {
			for (yi = minTileY; yi <= maxTileY; yi++)
				for (xi = minTileX; xi <= maxTileX; xi++)
					DrawObjectList(world.GetObjects(xi, yi,
							GBObjectClass.ocDecoration));
			DrawObjectList(world.GetLargeObjects(GBObjectClass.ocDecoration));
		}
		if (showSensors) {
			for (yi = minTileY; yi <= maxTileY; yi++)
				for (xi = minTileX; xi <= maxTileX; xi++)
					DrawObjectList(world.GetObjects(xi, yi,
							GBObjectClass.ocSensorShot));
			DrawObjectList(world.GetLargeObjects(GBObjectClass.ocSensorShot));
		}
	}

	void DrawObjectList(GBObject list) {
		for (GBObject cur = list; cur != null; cur = cur.next) {
			int diameter = (int) Math.round(Math.max(cur.Radius() * 2 * scale,
					1));
			Rectangle where = new Rectangle();
			where.x = ToScreenX(cur.Left());
			where.y = ToScreenY(cur.Top());
			where.height = diameter;
			where.width = diameter;
			if (where.height > 0 && where.x < getWidth() && where.width > 0
					&& where.y < getHeight()) {
				cur.Draw(getGraphics(), this, where.union(getBounds()),
						showDetails && scale >= kMinDetailsScale);
			}
		}
	}

	@Override
	public int ToScreenX(double x) {
		return (int) (Math.floor((x - viewpoint.x) * scale) + this.getBounds()
				.getCenterX());
	}

	@Override
	public int ToScreenY(double y) {
		return (int) (Math.floor((viewpoint.y - y) * scale) + this.getBounds()
				.getCenterY());
	}

	@Override
	public double FromScreenX(int h) {
		return (h - this.getBounds().getCenterX()) / scale + viewpoint.x;
	}

	@Override
	public double FromScreenY(int v) {
		return (this.getBounds().getCenterY() - v) / scale + viewpoint.y;
	}

	@Override
	public FinePoint FromScreen(int x, int y) {
		return new FinePoint(FromScreenX(x), FromScreenY(y));
	}

	@Override
	public int getScale() {
		return scale;
	}

	void RestrictScrolling() {
		// prevent scrolling too far off edge.
		if (viewpoint.x < world.Left())
			viewpoint.x = (int) world.Left();
		if (viewpoint.y < world.Bottom())
			viewpoint.y = (int) world.Bottom();
		if (viewpoint.x > world.Right())
			viewpoint.x = (int) world.Right();
		if (viewpoint.y > world.Top())
			viewpoint.y = (int) world.Top();
	}

	void ScrollToFollowed() {
		if (world.Followed() != null) {
			followPosition = world.Followed().Position();
			// viewpoint += world.Followed().Velocity();
		}
		// User doesn't want to see what's off the edge.
		// a bit complicated because:
		// 1) If the view is already near edge, don't scroll away from followed
		// to fix
		// 2) if zoomed out a lot, minFollowX could be greater than maxFollowX
		double minFollowX = (getWidth()) / (scale * 2) - kFollowViewOffEdge; // half-width
																				// of
																				// viewport
		double maxFollowX = world.Right() - minFollowX;
		double minFollowY = (getHeight()) / (scale * 2) - kFollowViewOffEdge;
		double maxFollowY = world.Top() - minFollowY;
		if (followPosition.x >= maxFollowX) {
			if (viewpoint.x <= followPosition.x)
				followPosition.x = Math.max(viewpoint.x, maxFollowX);
		} else if (followPosition.x <= minFollowX) {
			if (viewpoint.x >= followPosition.x)
				followPosition.x = Math.min(viewpoint.x, minFollowX);
		} else if (world.Followed() != null)
			viewpoint.x += world.Followed().Velocity().x;

		if (followPosition.y >= maxFollowY) {
			if (viewpoint.y <= followPosition.y)
				followPosition.y = Math.max(viewpoint.y, maxFollowY);
		} else if (followPosition.y <= minFollowY) {
			if (viewpoint.y >= followPosition.y)
				followPosition.y = Math.min(viewpoint.y, minFollowY);
		} else if (world.Followed() != null)
			viewpoint.y += world.Followed().Velocity().y;
		// now scroll toward followPosition
		if (followPosition.inRange(viewpoint, kFastFollowDistance))
			ScrollToward(followPosition, kFollowSpeed);
		else if (followPosition.inRange(viewpoint, kFollowJumpDistance))
			ScrollToward(followPosition, kFastFollowSpeed);
		else
			viewpoint = followPosition;
	}

	void DrawRangeCircle(FinePoint center, double radius, GBColor color) {
		if (radius <= 0)
			return;
		Rectangle where = new Rectangle(ToScreenX(center.x - radius),
				ToScreenY(center.y + radius), ToScreenX(center.x + radius),
				ToScreenY(center.y - radius));
		GBGraphics.drawOval(this.getGraphics(), where, color);
	}

	public void Draw() throws GBIndexOutOfRangeError {
		if (viewRight() < 0 || viewLeft() > world.Right() || viewTop() < 0
				|| viewBottom() > world.Top())
			viewpoint = world.Size().divide(2);
		if (autofollow
				&& System.currentTimeMillis() > lastFollow + kAutofollowPeriod)
			FollowRandom();
		if (following && moving == null)
			ScrollToFollowed();
		GBRobot bot = (GBRobot) world.Followed();
		if (bot != null) {
			DrawRangeCircle(bot.Position(), bot.hardware.blaster.MaxRange()
					+ bot.Radius(), new GBColor(Color.magenta).multiply(0.5f));
			DrawRangeCircle(bot.Position(), bot.hardware.grenades.MaxRange()
					+ bot.Radius(), new GBColor(Color.yellow).multiply(0.5f));
			if (bot.hardware.syphon.MaxRate() > 0)
				DrawRangeCircle(bot.Position(), bot.hardware.syphon.MaxRange()
						+ bot.Radius(), new GBColor(0.25f, 0.4f, 0.5f));
			if (bot.hardware.enemySyphon.MaxRate() > 0)
				DrawRangeCircle(bot.Position(),
						bot.hardware.enemySyphon.MaxRange() + bot.Radius(),
						new GBColor(0.3f, 0.5f, 0));
			DrawRangeCircle(bot.Position(), bot.hardware.forceField.MaxRange(),
					new GBColor(0, 0.4f, 0.5f));
		}
		DrawObjects();
		/*
		 * if ( world.Followed()!=null ) { FinePoint targetPos =
		 * world.Followed().Position(); int texty = ToScreenY(targetPos.y -
		 * (world.Followed().Radius() > 2 ? 0 : world.Followed().Radius())) +
		 * 13; DrawStringCentered(world.Followed().toString(),
		 * ToScreenX(targetPos.x), texty, 10, GBColor.white); String details =
		 * world.Followed().Details(); if (details.length() > 0)
		 * DrawStringCentered(details, ToScreenX(targetPos.x), texty + 10, 10,
		 * GBColor.white); } if ( showSideNames ) { for ( Side side :
		 * world.Sides()) if ( side.Scores().Seeded()!=0 )
		 * DrawStringCentered(side.Name(), ToScreenX(side.center.x),
		 * ToScreenY(side.center.y), 14, side.Scores().sterile!=0 ? GBColor.gray
		 * : GBColor.white); }
		 */
		// record drawn
		// worldChanges = world.ChangeCount();
		// selfChanges = ChangeCount();
	}

	public boolean InstantChanges() {
		// TODO: check what this does
		return true;// worldChanges != world.ChangeCount() || selfChanges !=
					// ChangeCount() || Following();
	}

	public final String Name() {
		return "World";
	}

	/**
	 * In-game X coordinate that corresponds to the left-most pixel on portal
	 * 
	 * @return
	 */
	public double viewLeft() {
		return viewpoint.x - getWidth() / (scale * 2);
	}

	/**
	 * In-game Y coordinate that corresponds to the top-most pixel on portal
	 * 
	 * @return
	 */
	public double viewTop() {
		return viewpoint.y + getHeight() / (scale * 2);
	}

	/**
	 * In-game X coordinate that corresponds to the right-most pixel on portal
	 * 
	 * @return
	 */
	public double viewRight() {
		return viewpoint.x + getWidth() / (scale * 2);
	}

	/**
	 * In-game Y coordinate that corresponds to the bottom-most pixel on portal
	 * 
	 * @return
	 */
	public double viewBottom() {
		return viewpoint.y - getHeight() / (scale * 2);
	}

	public void ScrollTo(FinePoint p) {
		viewpoint = p;
		RestrictScrolling();
		// Changed();
	}

	public void ScrollToward(FinePoint p, double speed) {
		if (viewpoint.inRange(p, speed))
			viewpoint = p;
		else
			viewpoint = viewpoint.add(p.subtract(viewpoint).unit()
					.multiply(speed));
		RestrictScrolling();
		// Changed();
	}

	public void ScrollBy(FinePoint delta) {
		viewpoint = viewpoint.add(delta);
		RestrictScrolling();
		// Changed();
	}

	public void Follow(GBObject ob) {
		if (ob != null) {
			world.Follow(ob);
			followPosition = ob.Position();
			following = true;
			lastFollow = System.currentTimeMillis();
		}
	}

	public void ResetZoom() {
		scale = kScale;
		repaint();
	}

	public void doZoom(int direction) {
		if (direction < 0 ? scale <= 4 : scale >= 64)
			return;
		scale += direction * Math.max(1, scale / 9);
		repaint();
	}

	public boolean Following() {
		return following && followPosition != viewpoint;
	}

	public void Unfollow() {
		following = false;
		autofollow = false;
	}

	public void Refollow() {
		following = true;
	}

	public void FollowRandom() {
		Follow(world.RandomInterestingObject());
	}

	public void FollowRandomNear() {
		Follow(world.RandomInterestingObjectNear(viewpoint,
				kAutofollowNearRange));
	}

	public void DoTool(FinePoint where) {
		try {
			switch (currentTool) {
			case ptScroll:
				following = false;
				break;
			case ptAddManna:
				world.AddObjectNew(new GBManna(where, world.mannaSize));
				world.Changed();
				break;
			case ptAddRobot:
				DoAddRobot(where);
				break;
			case ptAddSeed:
				DoAddSeed(where);
				break;
			case ptMove:
				DoMove(where);
				break;
			case ptPull:
				DoPull(where);
				break;
			case ptSmite:
				world.AddObjectNew(new GBExplosion(where, null /* nobody */,
						kSmiteDamage));
				world.Changed();
				break;
			case ptBlasts:
				DoBlasts(where);
				break;
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
			default:
				break;
			}
		} catch (Exception e) {
		}
	}

	public void DoAddRobot(FinePoint where) throws GBBadSymbolIndexError,
			GBIndexOutOfRangeError, GBNilPointerError, GBGenericError,
			GBOutOfMemoryError, GBAbort {
		Side side = app.selectedSide;
		if (side != null) {
			RobotType type = app.selectedType;
			if (type == null)
				if (side.types.size() > 0)
					type = side.types.get(0);
			if (type != null) {
				world.AddObjectNew(new GBRobot(type, where));
				side.Scores().ReportSeeded(type.Cost());
				world.Changed();
				world.CollectStatistics();
			}
		}
	}

	public void DoAddSeed(FinePoint where) throws GBAbort {
		Side side = world.SelectedSide();
		if (side != null) {
			world.AddSeed(side, where);
			world.Changed();
			world.CollectStatistics();
		}
	}

	public void DoMove(FinePoint where) {
		if (moving == null) {
			moving = world.ObjectNear(where, showSensors);
		}
		if (moving != null) {
			moving.MoveBy(where.subtract(lastClick));
			world.Changed();
		}
	}

	public void DoPull(FinePoint where) throws GBNilPointerError {
		if (where == lastClick)
			return;
		GBForceField ff = new GBForceField(where, where.subtract(lastClick),
				null, kMoveForce, where.subtract(lastClick).angle());
		world.AddObjectNew(ff);
		world.Changed();
	}

	public void DoBlasts(FinePoint where) throws GBNilPointerError {
		double base = GBRandomState.gRandoms.Angle();
		for (int i = kNumBlasts; i > 0; i--)
			world.AddObjectNew(new GBBlast(where, FinePoint.makePolar(
					kBlastSpeed, base + GBMath.kPi * 2 * i / kNumBlasts), null,
					kBlastDamage, (int) Math.ceil(kBlastRange / kBlastSpeed)));
		world.Changed();
	}
}
