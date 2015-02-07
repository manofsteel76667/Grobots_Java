/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import support.FinePoint;
import support.GBRandomState;
import exception.GBSimulationError;

public class GBObjectWorld {
	/**
	 * Background tiles are purely a display matter; they are here as a reminder
	 * to use world sizes that are a multiple of kBackgroundTileSize.
	 */
	public static final int kBackgroundTileSize = 10;
	public static final int kForegroundTileSize = 10;
	// world size must be a multiple of kBackgroundTileSize, but need not
	// be a multiple of kForegroundTileSize
	public static final int kWorldWidth = kBackgroundTileSize * 10;
	public static final int kWorldHeight = kBackgroundTileSize * 10;

	public static final int kLargeRadius = kForegroundTileSize / 2;
	// large objects are stored in the last tile, which is special.

	public static final double kWallRestitution = 0.9;
	public static final double kRobotRestitution = 0.9;

	public static final double kEraseDamage = 10000;
	protected FinePoint size;
	protected int tilesX, tilesY; // these are foreground tiles

	/**
	 * All game objects that are not dead
	 */
	protected List<GBObject> allObjects;

	/**
	 * allObjects sorted by ObjectClass and tile. Each array element represents
	 * a tile, with the GBObject being the head of a singly-linked list of
	 * GBObjects within the tile. Use GBObject.next to iterate through it.
	 */
	Map<GBObjectClass, GBObject[]> objects;
	// protected GBObject[][] objects;
	protected List<GBObject> newObjects;

	/*
	 * Calls from GBWorld go in the order: 1) Think All 2) Move All 3) Act All
	 * 4) Resort All 5) Collide All 6) Start next turn 7) Collect statistics
	 */
	public GBObjectWorld() {
		size = new FinePoint(kWorldWidth, kWorldHeight);
		tilesX = (int) (Math.ceil(kWorldWidth / kForegroundTileSize));
		tilesY = (int) (Math.ceil(kWorldHeight / kForegroundTileSize));
		allObjects = Collections
				.synchronizedList(new ArrayList<GBObject>(10000));
		objects = new HashMap<GBObjectClass, GBObject[]>();
		newObjects = new ArrayList<GBObject>(2000);
		MakeTiles();
	}

	protected void MakeTiles() {
		GBObject[] template = new GBObject[tilesX * tilesY + 1];
		for (GBObjectClass cl : GBObjectClass.values())
			objects.put(cl, template.clone());
	}

	/**
	 * Clear all object lists and reset each object's next
	 */
	protected void ClearLists() {
		MakeTiles();
		synchronized (allObjects) {
			for (GBObject obj : allObjects)
				obj.next = null;
			allObjects.clear();
		}
		newObjects.clear();
	}

	/**
	 * Puts objects in the appropriate class and tile, and deletes dead ones.
	 */
	protected void ResortObjects() {
		MakeTiles();
		synchronized (allObjects) {
			Iterator<GBObject> it = allObjects.iterator();
			while (it.hasNext()) {
				GBObject obj = it.next();
				obj.next = null;
				if (obj.Class() != GBObjectClass.ocDead) {
					addObject(obj);
				} else
					it.remove();
			}
		}
		addNewObjects();
	}

	/**
	 * Returns a list of the active objects in the world that are of the
	 * specified type.
	 * 
	 * @param type
	 * @return
	 */
	public GBObject[] getObjects(GBObjectClass type) {
		//return objects.get(type);
		synchronized (allObjects) {
			List<GBObject> ret = new ArrayList<GBObject>();
			for(GBObject obj : allObjects)
				if (obj.Class() == type)
					ret.add(obj);
			return ret.toArray(new GBObject[ret.size()]);
		}
	}

	private void addNewObjects() {
		Iterator<GBObject> it = newObjects.iterator();
		while (it.hasNext()) {
			GBObject ob = it.next();
			synchronized (allObjects) {
				allObjects.add(ob);
			}
			addObject(ob);
		}
		newObjects.clear();
	}

	/**
	 * Add an object to the new object list. Objects in this list will be added
	 * to the world on the next call to addNewObjects
	 * 
	 * @param newOb
	 */
	public void addObjectLater(GBObject newOb) {
		if (newOb != null)
			newObjects.add(newOb);
	}

	/**
	 * Add an object to the map from the UI
	 * 
	 * @param newOb
	 */
	public void addObjectImmediate(GBObject newOb) {
		synchronized (allObjects) {
			allObjects.add(newOb);
		}
		addObject(newOb);
	}

	private void addObject(GBObject ob) {
		if (ob != null)
			if (ob.Class() != GBObjectClass.ocDead) {
				// If object is dead, leave it alone.
				int tile = ob.Radius() * 2 >= kForegroundTileSize ? tilesX
						* tilesY : FindTile(ob.Position());
				ob.next = objects.get(ob.Class())[tile];
				objects.get(ob.Class())[tile] = ob;
			}
	}

	protected void CollideObjectWithWalls(GBObject ob) {
		GBObjectClass cl = ob.Class();
		if (ob.Left() < Left()) {
			FinePoint vel = ob.Velocity();
			ob.SetVelocity(Math.abs(vel.x) * kWallRestitution, vel.y);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.MoveBy(Left() - ob.Left(), 0);
			ob.CollideWithWall();
		} else if (ob.Right() > Right()) {
			FinePoint vel = ob.Velocity();
			ob.SetVelocity(Math.abs(vel.x) * -kWallRestitution, vel.y);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.MoveBy(Right() - ob.Right(), 0);
			ob.CollideWithWall();
		}
		if (ob.Bottom() < Bottom()) {
			FinePoint vel = ob.Velocity();
			ob.SetVelocity(vel.x, Math.abs(vel.y) * kWallRestitution);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.MoveBy(0, Bottom() - ob.Bottom());
			ob.CollideWithWall();
		} else if (ob.Top() > Top()) {
			FinePoint vel = ob.Velocity();
			ob.SetVelocity(vel.x, Math.abs(vel.y) * -kWallRestitution);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.MoveBy(0, Top() - ob.Top());
			ob.CollideWithWall();
		}
	}

	protected void MoveAllObjects() {
		for (int i = 0; i < tilesX * tilesY; i++)
			for (GBObjectClass cl : GBObjectClass.values())
				for (GBObject obj = objects.get(cl)[i]; obj != null; obj = obj.next) {
					obj.Move();
					if (i < tilesX || i > tilesX * (tilesY - 1)
							|| i % tilesX == 0 || i % tilesX == tilesX - 1)
						CollideObjectWithWalls(obj); // only large objects
														// and edge tiles
				}
	}

	protected void CollideAllObjects() {
		for (int tx = 0; tx < tilesX; tx++)
			for (int ty = 0; ty < tilesY; ty++) {
				int t = ty * tilesX + tx;
				CollideSameTile(t);
				// collide with adjacent tiles
				if (tx < tilesX - 1) {
					CollideTwoTiles(t, t + 1);
					if (ty < tilesY - 1)
						CollideTwoTiles(t, t + tilesX + 1);
				}
				if (ty < tilesY - 1) {
					CollideTwoTiles(t, t + tilesX);
					if (tx > 0)
						CollideTwoTiles(t, t + tilesX - 1);
				}
				// collide with large-object tile
				CollideTwoTiles(t, tilesX * tilesY);
			}
		// intercollide large objects, in case that ever matters
		CollideSameTile(tilesX * tilesY);
	}

	protected void CollideSameTile(int t) {
		String stage = "";
		try {
			for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t]; bot != null; bot = bot.next) {
				stage = "colliding robots";
				for (GBObject bot2 = bot.next; bot2 != null; bot2 = bot2.next) {
					bot.SolidCollide(bot2, kRobotRestitution);
				}
				if (((GBRobot) bot).hardware.EaterLimit() != 0) {
					stage = "colliding robot and food";
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next) {
						bot.BasicCollide(food);
					}
				}
				stage = "colliding robot and shot";
				for (GBObject shot = objects.get(GBObjectClass.ocShot)[t]; shot != null; shot = shot.next)
					bot.BasicCollide(shot);
				stage = "colliding robot and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next)
					bot.BasicCollide(area);
			}
			stage = "colliding area and food";
			for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next)
				for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next)
					area.BasicCollide(food);
			stage = "colliding sensors";
			CollideSensors(t, t);
		} catch (Exception e) {
			throw new GBSimulationError("Error " + stage + ": "
					+ e.getMessage());
		}
	}

	/**
	 * Does this object in t1 come within 2 units of t2? If not, we don't need
	 * to collide it with small objects in t2.
	 * 
	 * @param ob
	 * @param t1
	 * @param t2
	 * @return
	 */
	protected boolean ObjectReachesTile(GBObject ob, int t1, int t2) {
		if (t2 == tilesX * tilesY)
			return true;
		int dt = t2 - t1;
		if ((dt == 1 || dt == tilesX + 1)
				&& ob.Right() < (t2 % tilesX) * kForegroundTileSize - 2
				|| dt == tilesX - 1
				&& ob.Left() < (t1 % tilesX) * kForegroundTileSize + 2)
			return false;
		if (dt != 1 && ob.Top() < (t2 / tilesX) * kForegroundTileSize - 2)
			return false;
		return true;
	}

	protected void CollideTwoTiles(int t1, int t2) {
		// Now tries to avoid looking at t2 when the object in t1 isn't even
		// close to it.
		double t2edge = (t2 / tilesX) * kForegroundTileSize - 2;
		if (t2 == tilesX * tilesY || t2 == t1 + 1)
			t2edge = -1000; // always do these tiles
		String stage = "";
		try {
			for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t1]; bot != null; bot = bot.next) {
				if (bot.Top() > t2edge) {
					stage = "colliding robots";
					for (GBObject bot2 = objects.get(GBObjectClass.ocRobot)[t2]; bot2 != null; bot2 = bot2.next) {
						bot.SolidCollide(bot2, kRobotRestitution);
					}
					if (((GBRobot) bot).hardware.EaterLimit() != 0) {
						stage = "colliding robot and food";
						for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
							bot.BasicCollide(food);
						}
					}
					stage = "colliding robot and shot";
					for (GBObject shot = objects.get(GBObjectClass.ocShot)[t2]; shot != null; shot = shot.next) {
						bot.BasicCollide(shot);
					}
				}
				stage = "colliding robot and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					bot.BasicCollide(area);
				}
			}
			for (GBObject food = objects.get(GBObjectClass.ocFood)[t1]; food != null; food = food.next) {
				if (food.Top() > t2edge)
					stage = "colliding food and robot";
				for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
					food.BasicCollide(bot);
				}
				stage = "colliding food and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					food.BasicCollide(area);
				}
			}
			stage = "colliding shot and robot";
			for (GBObject shot = objects.get(GBObjectClass.ocShot)[t1]; shot != null; shot = shot.next) {
				if (shot.Top() > t2edge)
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						shot.BasicCollide(bot);
					}
			}
			for (GBObject area = objects.get(GBObjectClass.ocArea)[t1]; area != null; area = area.next) {
				if (area.Top() > t2edge) {
					stage = "colliding area and robot";
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						area.BasicCollide(bot);
					}
					stage = "colliding area and food";
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
						area.BasicCollide(food);
					}
				}
			}
			stage = "colliding sensors";
			CollideSensors(t1, t2);
			CollideSensors(t2, t1);
		} catch (Exception e) {
			throw new GBSimulationError("Error " + stage + ": "
					+ e.getMessage());
		}
	}

	protected void CollideSensors(int sensorTile, int otherTile) {
		double tileBottom = (otherTile / tilesX) * kForegroundTileSize - 2;
		double tileTop = (otherTile / tilesX + 1) * kForegroundTileSize - 2;
		if (otherTile == tilesX * tilesY) {
			tileBottom = -1000;
			tileTop = size.y + 1000;
		}
		for (GBObject sensor = objects.get(GBObjectClass.ocSensorShot)[sensorTile]; sensor != null; sensor = sensor.next) {
			GBObjectClass seen = ((GBSensorShot) sensor).Seen();
			if (seen != GBObjectClass.ocDead) {
				if (sensor.Top() > tileBottom && sensor.Bottom() < tileTop)
					for (GBObject ob = objects.get(seen)[otherTile]; ob != null; ob = ob.next) {
						if (sensor.Intersects(ob))
							sensor.CollideWith(ob);
						// note one-directional collision, since ob mustn't
						// care it's been sensed
					}
				if (seen == GBObjectClass.ocShot) // shot sensors see area
													// shots too
					for (GBObject ob = objects.get(GBObjectClass.ocArea)[otherTile]; ob != null; ob = ob.next) {
						if (sensor.Intersects(ob))
							sensor.CollideWith(ob);
					}
			}
		}
	}

	protected int FindTile(FinePoint where) {
		int tx = (int) (Math.floor(where.x) / kForegroundTileSize);
		if (tx < 0)
			tx = 0;
		if (tx >= tilesX)
			tx = tilesX - 1;
		int ty = (int) (Math.floor(where.y) / kForegroundTileSize);
		if (ty < 0)
			ty = 0;
		if (ty >= tilesY)
			ty = tilesY - 1;
		return ty * tilesX + tx;
	}

	public void EraseAt(FinePoint where, double radius) {
		MakeTiles();
		synchronized (allObjects) {
			Iterator<GBObject> it = allObjects.iterator();
			while (it.hasNext()) {
				GBObject obj = it.next();
				if (obj.Position().inRange(where, obj.Radius() + radius))
					if (obj.Class() == GBObjectClass.ocRobot) { // must stick
																// around a
																// frame for
																// sensors
						((GBRobot) obj).Die(null);
						addObjectLater(obj); // new so it'll be deleted next
												// resort
					} else
						it.remove();
				else
					addObject(obj);
			}
		}
	}

	// TODO: Find where this is used and make sure it acts like it should
	public void Resize(FinePoint newsize) {
		size = newsize;
		tilesX = (int) Math.ceil(size.x / kForegroundTileSize);
		tilesY = (int) Math.ceil(size.y / kForegroundTileSize);
		// fix tiles
		MakeTiles();
		ClearLists();
		ResortObjects();
	}

	public FinePoint Size() {
		return size;
	}

	public double Left() {
		return 0;
	}

	public double Top() {
		return size.y;
	}

	public double Right() {
		return size.x;
	}

	public double Bottom() {
		return 0;
	}

	// eventually replace calculation with getting from Rules.
	public int BackgroundTilesX() {
		return (int) Math.ceil(size.x / kBackgroundTileSize);
	}

	public int BackgroundTilesY() {
		return (int) Math.ceil(size.y / kBackgroundTileSize);
	}

	public int ForegroundTilesX() {
		return tilesX;
	}

	public int ForegroundTilesY() {
		return tilesY;
	}

	// Object selectors triggered from the UI. Synchronization required
	public GBObject ObjectNear(FinePoint where, boolean hitSensors) {
		GBObject best = null;
		double dist = 25; // never see objects farther than this
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				if ((ob.Class() != GBObjectClass.ocSensorShot || hitSensors)
						&& ob.Class() != GBObjectClass.ocDecoration
						&& (ob.Class() == GBObjectClass.ocRobot || best == null
								|| best.Class() != GBObjectClass.ocRobot || where
									.inRange(ob.Position(), ob.Radius()))
						&& ob.Position().inRangeSquared(where, dist)) {
					best = ob;
					dist = (best.Position().subtract(where)).normSquare();
				}
		}
		return best;
	}

	public GBObject RandomInterestingObject() {
		double totalInterest = 0;
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				totalInterest += ob.Interest();
			if (totalInterest == 0)
				return null;
			for (GBObject ob : allObjects) {
				double interest = ob.Interest();
				if (GBRandomState.gRandoms.bool(interest / totalInterest))
					return ob;
				totalInterest -= interest;
			}
		}
		return null;
	}

	public GBObject RandomInterestingObjectNear(FinePoint where, double radius) {
		double totalInterest = 0;
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				if (ob.Position().inRange(where, radius))
					totalInterest += ob.Interest();
			if (totalInterest == 0)
				return null;
			for (GBObject ob : allObjects)
				if (ob.Position().inRange(where, radius)) {
					double interest = ob.Interest();
					if (GBRandomState.gRandoms.bool(interest / totalInterest))
						return ob;
					totalInterest -= interest;
				}
		}
		return null;
	}
}
