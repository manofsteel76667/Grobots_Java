/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBPortal.h
// a view of [part of] a GBWorld
// Grobots (c) 2002-2008 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

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
	boolean dragged;
	/**
	 * Screen pixels per game map unit
	 */
	public int scale;
	/* Variables to control camera and viewpoint */
	public boolean following;
	/**
	 * When true, the camera will periodically pick a new object to follow,
	 * panning through various objects at random
	 */
	public boolean autofollow;
	/**
	 * Difference between viewpoint and position of the followed object
	 */
	FinePoint followPosition;
	long lastFollow;
	public GBObject followedObject;

	GBObject moving;
	long worldChanges;
	long selfChanges;
	// tool use
	int lastx, lasty; // where mouse was last if we're dragging
	FinePoint lastClick;
	int lastFrame; // when last tool effect was
	// public:

	public toolTypes currentTool;
	public boolean showSensors;
	public boolean showDecorations;
	public boolean showDetails;
	public boolean showSideNames;

	public static final int kScale = 6; // default number of pixels per unit.
	static final int kMinDetailsScale = 10;
	static final double kAutoScrollSpeed = 0.4;
	static final double kFollowSpeed = 0.5;
	static final double kFastFollowSpeed = 1.5;
	static final double kFastFollowDistance = 10;
	static final double kFollowJumpDistance = 30;
	static final double kAutofollowNearRange = 20;
	static final long kAutofollowPeriod = 3000L;
	static final double kFollowViewOffEdge = 3; // how much wall to show
												// when following near
												// edge

	static final double kMoveForce = 1;
	static final double kSmiteDamage = 200;
	static final int kNumBlasts = 10;
	static final double kBlastRange = 10;
	static final double kBlastSpeed = 0.2;
	static final double kBlastDamage = 5;
	static final double kEraseBigRadius = 2;
	static final int minZoom = 3;
	static final int maxZoom = 64;

	public GBPortal(GBApplication _app) {
		super(true);
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
				// Follow object on click
				if (!dragged)
					if (arg0.getClickCount() > 0
							&& currentTool == toolTypes.ptScroll)
						Follow(world.ObjectNear(
								FromScreen(arg0.getX(), arg0.getY()), false));
				dragged = false;
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
				dragged = true;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				// Zoom with mouse wheel
				// Zoom on the spot the mouse is on so it stays in the same
				// screen position. Not perfect but pretty good.
				int notches = arg0.getWheelRotation();
				if (notches == 0)
					return;
				int originalScale = scale;
				FinePoint zoomPosition = FromScreen(arg0.getPoint().x,
						arg0.getPoint().y);
				FinePoint dp = zoomPosition.subtract(viewpoint);
				int dir = (int) (notches / Math.abs(notches));
				doZoom(dir * -1);
				viewpoint = new FinePoint(zoomPosition.x - dp.x / scale
						* originalScale, zoomPosition.y - dp.y / scale
						* originalScale);
			}
		};
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
		// Rather than determining the edges of the map and painting a wall,
		// we will make the background wall-colored and paint over it
		// with tiles.
		setBackground(Color.LIGHT_GRAY);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		adjustViewpoint();
		drawBackground(g2d);
		drawObjects(g2d);
		drawText(g2d);
	}

	void adjustViewpoint() {
		// If the object you're following dies, handle it
		if (followedObject != null)
			if (followedObject.Class() == GBObjectClass.ocDead) {
				followedObject = null;
				following = autofollow;
			}
		// Set viewpoint to followed object, if following
		if (following) {
			if (autofollow
					&& (System.currentTimeMillis() > lastFollow
							+ kAutofollowPeriod) || followedObject == null) {
				FollowRandom();
				if (followedObject != null)
					viewpoint.set(followedObject.Position());
				lastFollow = System.currentTimeMillis();
			}
			if (followedObject != null) {
				if (panning())
					if (!viewpoint.inRange(followedObject.Position(),
							16.0d / scale)) {
						FinePoint distance = followedObject.Position()
								.subtract(viewpoint);
						distance.setNorm(16.0d / scale);
						viewpoint = viewpoint.add(distance);
					} else
						viewpoint.set(followedObject.Position());
				else
					viewpoint.set(followedObject.Position());
			}
		}
		RestrictScrolling();
	}

	static Color tileColor = Color.black;
	static Color coarseGridBaseColor = Color.white;

	void drawBackground(Graphics2D g2d) {
		// Set colors for grid lines
		int coarseThickness = 1 + scale / 20;
		Color coarseColor = GBColor.Mix(coarseGridBaseColor, (float)scale * 6f
				/ (float)maxZoom, tileColor);
		// Fine grid should be close to the coarse grid color when zoomed in and
		// disappear into the background when zoomed out
		Color fineColor = GBColor.Mix(coarseColor,
				GBMath.clamp((float)scale / (float)maxZoom, 0, .8f), tileColor);

		// Draw visible tiles
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
						GBObjectWorld.kBackgroundTileSize * scale);
				// Tile
				g2d.setColor(tileColor);
				g2d.fill(tile);
				// Fine Grid
				g2d.setColor(fineColor);
				g2d.setStroke(new BasicStroke(1));
				for (int yf = tile.y+1; yf < tile.y
						+ GBObjectWorld.kForegroundTileSize * scale-1; yf += scale)
					for (int xf = tile.x+1; xf < tile.x
							+ GBObjectWorld.kForegroundTileSize * scale-1; xf += scale) {
						g2d.drawLine(tile.x, yf, tile.x + tile.width, yf);
						g2d.drawLine(xf, tile.y, xf, tile.y + tile.height);
					}
				// Coarse grid
				g2d.setStroke(new BasicStroke(coarseThickness));
				g2d.setColor(coarseColor);
				g2d.draw(tile);
			}
	}

	void drawObjects(Graphics2D g2d) {
		// Draw all living objects in the game
		// Draws everything even if it isn't on the screen. This
		// could probably be improved
		boolean detailed = showDetails && scale >= kMinDetailsScale;
		for (GBObject spot : world.getObjects(GBObjectClass.ocFood))
			for (GBObject ob = spot; ob != null; ob = ob.next)
				ob.Draw(g2d, this, detailed);
		for (GBObject spot : world.getObjects(GBObjectClass.ocRobot))
			for (GBObject ob = spot; ob != null; ob = ob.next)
				ob.Draw(g2d, this, detailed);
		for (GBObject spot : world.getObjects(GBObjectClass.ocArea))
			for (GBObject ob = spot; ob != null; ob = ob.next)
				ob.Draw(g2d, this, detailed);
		for (GBObject spot : world.getObjects(GBObjectClass.ocShot))
			for (GBObject ob = spot; ob != null; ob = ob.next)
				ob.Draw(g2d, this, detailed);
		if (this.showDecorations)
			for (GBObject spot : world.getObjects(GBObjectClass.ocDecoration))
				for (GBObject ob = spot; ob != null; ob = ob.next)
					ob.Draw(g2d, this, detailed);
		if (this.showSensors)
			for (GBObject spot : world.getObjects(GBObjectClass.ocSensorShot))
				for (GBObject ob = spot; ob != null; ob = ob.next)
					ob.Draw(g2d, this, detailed);
	}

	void drawText(Graphics2D g2d) {
		// Details about the followed object
		if (followedObject != null) {
			FinePoint targetPos = followedObject.Position();
			Font textFont = new Font("Serif", Font.PLAIN, 10);
			g2d.setFont(textFont);
			g2d.setColor(Color.white);
			FontMetrics fm = g2d.getFontMetrics();
			String s = followedObject.toString();
			// Center the name below the object
			int x = ToScreenX(targetPos.x) - fm.stringWidth(s) / 2;
			int texty = ToScreenY(targetPos.y
					- (followedObject.Radius() > 2 ? 0 : followedObject
							.Radius())) + 13;
			g2d.drawString(s, x, texty);
			String details = followedObject.Details();
			// Details go below that
			if (details.length() > 0) {
				x = ToScreenX(targetPos.x) - fm.stringWidth(details) / 2;
				g2d.drawString(details, x, texty + 10);
			}
			// Draw range circles if following a robot
			if (followedObject.Class() == GBObjectClass.ocRobot)
				((GBRobot) followedObject).drawRangeCircles(g2d, this);
		}
		// Side names
		if (showSideNames) {
			for (Side side : world.Sides())
				if (side.Scores().Seeded() != 0) {
					Font textFont = new Font("Serif", Font.PLAIN, 10);
					g2d.setFont(textFont);
					FontMetrics fm = g2d.getFontMetrics();
					int tx = ToScreenX(side.center.x)
							- fm.stringWidth(side.Name()) / 2;
					int ty = ToScreenY(side.center.y);
					g2d.setColor(side.Scores().sterile != 0 ? Color.gray
							: Color.white);
					g2d.drawString(side.Name(), tx, ty);
				}
		}
	}

	@Override
	public int ToScreenX(double x) {
		return (int) (Math.floor((x - viewpoint.x) * scale) + this
				.getVisibleRect().getCenterX());
	}

	@Override
	public int ToScreenY(double y) {
		return (int) (Math.floor((viewpoint.y - y) * scale) + this
				.getVisibleRect().getCenterY());
	}

	@Override
	public double FromScreenX(int h) {
		return (h - this.getVisibleRect().getCenterX()) / scale + viewpoint.x;
	}

	@Override
	public double FromScreenY(int v) {
		return (this.getVisibleRect().getCenterY() - v) / scale + viewpoint.y;
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

	@Override
	public String toString() {
		return "World";
	}

	/**
	 * In-game X coordinate that corresponds to the left-most pixel on portal
	 * 
	 * @return
	 */
	double viewLeft() {
		return viewpoint.x - getWidth() / (scale * 2);
	}

	/**
	 * In-game Y coordinate that corresponds to the top-most pixel on portal
	 * 
	 * @return
	 */
	double viewTop() {
		return viewpoint.y + getHeight() / (scale * 2);
	}

	/**
	 * In-game X coordinate that corresponds to the right-most pixel on portal
	 * 
	 * @return
	 */
	double viewRight() {
		return viewpoint.x + getWidth() / (scale * 2);
	}

	/**
	 * In-game Y coordinate that corresponds to the bottom-most pixel on portal
	 * 
	 * @return
	 */
	double viewBottom() {
		return viewpoint.y - getHeight() / (scale * 2);
	}

	public void ScrollTo(FinePoint p) {
		viewpoint = p;
		RestrictScrolling();
	}

	public void ScrollToward(FinePoint p, double speed) {
		if (viewpoint.inRange(p, speed))
			viewpoint = p;
		else
			viewpoint = viewpoint.add(p.subtract(viewpoint).unit()
					.multiply(speed));
		RestrictScrolling();
	}

	public void ScrollBy(FinePoint delta) {
		viewpoint = viewpoint.add(delta);
		RestrictScrolling();
	}

	public void Follow(GBObject ob) {
		if (ob != null) {
			// TODO select side and type if it's a robot
			followedObject = ob;
			followPosition = ob.Position().subtract(viewpoint);
			following = true;
		}
	}

	public void ResetZoom() {
		scale = kScale;
	}

	public void doZoom(int direction) {
		if (direction < 0 ? scale <= minZoom : scale >= maxZoom)
			return;
		scale += direction * Math.max(1, scale / 9);
	}

	public boolean panning() {
		boolean ret = following && !followedObject.Position().equals(viewpoint);
		return ret;
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
				world.addObjectManual(new GBManna(where, world.mannaSize));
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
				break;
			case ptBlasts:
				DoBlasts(where);
				break;
			case ptErase:
				world.EraseAt(where, 0);
				world.CollectStatistics();
				break;
			case ptEraseBig:
				world.EraseAt(where, kEraseBigRadius);
				world.CollectStatistics();
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
	}

	public void DoAddRobot(FinePoint where) {
		Side side = app.getSelectedSide();
		if (side != null) {
			RobotType type = app.getSelectedType();
			if (type == null)
				if (side.types.size() > 0)
					type = side.types.get(0);
			if (type != null) {
				world.addObjectManual(new GBRobot(type, where));
				side.Scores().ReportSeeded(type.Cost());
				world.CollectStatistics();
			}
		}
	}

	public void DoAddSeed(FinePoint where) {
		Side side = app.getSelectedSide();
		if (side != null) {
			world.AddSeed(side, where);
			world.CollectStatistics();
		}
	}

	public void DoMove(FinePoint where) {
		if (moving == null) {
			moving = world.ObjectNear(where, showSensors);
		}
		if (moving != null) {
			moving.MoveBy(where.subtract(lastClick));
		}
	}

	public void DoPull(FinePoint where) {
		if (where == lastClick)
			return;
		GBForceField ff = new GBForceField(where, where.subtract(lastClick),
				null, kMoveForce, where.subtract(lastClick).angle());
		world.AddObjectNew(ff);
	}

	public void DoBlasts(FinePoint where) {
		double base = GBRandomState.gRandoms.Angle();
		for (int i = kNumBlasts; i > 0; i--)
			world.AddObjectNew(new GBBlast(where, FinePoint.makePolar(
					kBlastSpeed, base + GBMath.kPi * 2 * i / kNumBlasts), null,
					kBlastDamage, (int) Math.ceil(kBlastRange / kBlastSpeed)));
	}
}
