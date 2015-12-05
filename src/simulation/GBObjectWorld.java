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
		makeTiles();
	}

	protected void makeTiles() {
		GBObject[] template = new GBObject[tilesX * tilesY + 1];
		for (GBObjectClass cl : GBObjectClass.values())
			objects.put(cl, template.clone());
	}

	/**
	 * Clear all object lists and reset each object's next
	 */
	protected void clearLists() {
		makeTiles();
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
	protected void resortObjects() {
		makeTiles();
		synchronized (allObjects) {
			Iterator<GBObject> it = allObjects.iterator();
			while (it.hasNext()) {
				GBObject obj = it.next();
				obj.next = null;
				if (obj.getObjectClass() != GBObjectClass.ocDead) {
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
		return objects.get(type);
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
			if (ob.getObjectClass() != GBObjectClass.ocDead) {
				// If object is dead, leave it alone.
				int tile = ob.getRadius() * 2 >= kForegroundTileSize ? tilesX
						* tilesY : findTile(ob.getPosition());
				ob.next = objects.get(ob.getObjectClass())[tile];
				objects.get(ob.getObjectClass())[tile] = ob;
			}
	}

	protected void collideObjectWithWalls(GBObject ob) {
		GBObjectClass cl = ob.getObjectClass();
		if (ob.getLeft() < getLeft()) {
			FinePoint vel = ob.getVelocity();
			ob.setVelocity(Math.abs(vel.x) * kWallRestitution, vel.y);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.moveBy(getLeft() - ob.getLeft(), 0);
			ob.collideWithWall();
		} else if (ob.getRight() > getRight()) {
			FinePoint vel = ob.getVelocity();
			ob.setVelocity(Math.abs(vel.x) * -kWallRestitution, vel.y);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.moveBy(getRight() - ob.getRight(), 0);
			ob.collideWithWall();
		}
		if (ob.getBottom() < getBottom()) {
			FinePoint vel = ob.getVelocity();
			ob.setVelocity(vel.x, Math.abs(vel.y) * kWallRestitution);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.moveBy(0, getBottom() - ob.getBottom());
			ob.collideWithWall();
		} else if (ob.getTop() > getTop()) {
			FinePoint vel = ob.getVelocity();
			ob.setVelocity(vel.x, Math.abs(vel.y) * -kWallRestitution);
			if (cl == GBObjectClass.ocRobot || cl == GBObjectClass.ocFood)
				ob.moveBy(0, getTop() - ob.getTop());
			ob.collideWithWall();
		}
	}

	protected void moveAllObjects() {
		for (int i = 0; i < tilesX * tilesY; i++)
			for (GBObjectClass cl : GBObjectClass.values())
				for (GBObject obj = objects.get(cl)[i]; obj != null; obj = obj.next) {
					obj.move();
					if (i < tilesX || i > tilesX * (tilesY - 1)
							|| i % tilesX == 0 || i % tilesX == tilesX - 1)
						collideObjectWithWalls(obj); // only large objects
														// and edge tiles
				}
	}

	protected void collideAllObjects() {
		for (int tx = 0; tx < tilesX; tx++)
			for (int ty = 0; ty < tilesY; ty++) {
				int t = ty * tilesX + tx;
				collideSameTile(t);
				// collide with adjacent tiles
				if (tx < tilesX - 1) {
					collideTwoTiles(t, t + 1);
					if (ty < tilesY - 1)
						collideTwoTiles(t, t + tilesX + 1);
				}
				if (ty < tilesY - 1) {
					collideTwoTiles(t, t + tilesX);
					if (tx > 0)
						collideTwoTiles(t, t + tilesX - 1);
				}
				// collide with large-object tile
				collideTwoTiles(t, tilesX * tilesY);
			}
		// intercollide large objects, in case that ever matters
		collideSameTile(tilesX * tilesY);
	}

	protected void collideSameTile(int t) {
		String stage = "";
		try {
			for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t]; bot != null; bot = bot.next) {
				stage = "colliding robots";
				for (GBObject bot2 = bot.next; bot2 != null; bot2 = bot2.next) {
					bot.doSolidCollide(bot2, kRobotRestitution);
				}
				if (((GBRobot) bot).hardware.getEaterLimit() != 0) {
					stage = "colliding robot and food";
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next) {
						bot.doBasicCollide(food);
					}
				}
				stage = "colliding robot and shot";
				for (GBObject shot = objects.get(GBObjectClass.ocShot)[t]; shot != null; shot = shot.next)
					bot.doBasicCollide(shot);
				stage = "colliding robot and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next)
					bot.doBasicCollide(area);
			}
			stage = "colliding area and food";
			for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next)
				for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next)
					area.doBasicCollide(food);
			stage = "colliding sensors";
			collideSensors(t, t);
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
	protected boolean objectReachesTile(GBObject ob, int t1, int t2) {
		if (t2 == tilesX * tilesY)
			return true;
		int dt = t2 - t1;
		if ((dt == 1 || dt == tilesX + 1)
				&& ob.getRight() < (t2 % tilesX) * kForegroundTileSize - 2
				|| dt == tilesX - 1
				&& ob.getLeft() < (t1 % tilesX) * kForegroundTileSize + 2)
			return false;
		if (dt != 1 && ob.getTop() < (t2 / tilesX) * kForegroundTileSize - 2)
			return false;
		return true;
	}

	protected void collideTwoTiles(int t1, int t2) {
		// Now tries to avoid looking at t2 when the object in t1 isn't even
		// close to it.
		double t2edge = (t2 / tilesX) * kForegroundTileSize - 2;
		if (t2 == tilesX * tilesY || t2 == t1 + 1)
			t2edge = -1000; // always do these tiles
		String stage = "";
		try {
			for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t1]; bot != null; bot = bot.next) {
				if (bot.getTop() > t2edge) {
					stage = "colliding robots";
					for (GBObject bot2 = objects.get(GBObjectClass.ocRobot)[t2]; bot2 != null; bot2 = bot2.next) {
						bot.doSolidCollide(bot2, kRobotRestitution);
					}
					if (((GBRobot) bot).hardware.getEaterLimit() != 0) {
						stage = "colliding robot and food";
						for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
							bot.doBasicCollide(food);
						}
					}
					stage = "colliding robot and shot";
					for (GBObject shot = objects.get(GBObjectClass.ocShot)[t2]; shot != null; shot = shot.next) {
						bot.doBasicCollide(shot);
					}
				}
				stage = "colliding robot and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					bot.doBasicCollide(area);
				}
			}
			for (GBObject food = objects.get(GBObjectClass.ocFood)[t1]; food != null; food = food.next) {
				if (food.getTop() > t2edge)
					stage = "colliding food and robot";
				for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
					food.doBasicCollide(bot);
				}
				stage = "colliding food and area";
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					food.doBasicCollide(area);
				}
			}
			stage = "colliding shot and robot";
			for (GBObject shot = objects.get(GBObjectClass.ocShot)[t1]; shot != null; shot = shot.next) {
				if (shot.getTop() > t2edge)
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						shot.doBasicCollide(bot);
					}
			}
			for (GBObject area = objects.get(GBObjectClass.ocArea)[t1]; area != null; area = area.next) {
				if (area.getTop() > t2edge) {
					stage = "colliding area and robot";
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						area.doBasicCollide(bot);
					}
					stage = "colliding area and food";
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
						area.doBasicCollide(food);
					}
				}
			}
			stage = "colliding sensors";
			collideSensors(t1, t2);
			collideSensors(t2, t1);
		} catch (Exception e) {
			throw new GBSimulationError("Error " + stage + ": "
					+ e.getMessage());
		}
	}

	protected void collideSensors(int sensorTile, int otherTile) {
		double tileBottom = (otherTile / tilesX) * kForegroundTileSize - 2;
		double tileTop = (otherTile / tilesX + 1) * kForegroundTileSize - 2;
		if (otherTile == tilesX * tilesY) {
			tileBottom = -1000;
			tileTop = size.y + 1000;
		}
		for (GBObject sensor = objects.get(GBObjectClass.ocSensorShot)[sensorTile]; sensor != null; sensor = sensor.next) {
			GBObjectClass seen = ((GBSensorShot) sensor).getSeen();
			if (seen != GBObjectClass.ocDead) {
				if (sensor.getTop() > tileBottom && sensor.getBottom() < tileTop)
					for (GBObject ob = objects.get(seen)[otherTile]; ob != null; ob = ob.next) {
						if (sensor.intersects(ob))
							sensor.collideWith(ob);
						// note one-directional collision, since ob mustn't
						// care it's been sensed
					}
				if (seen == GBObjectClass.ocShot) // shot sensors see area
													// shots too
					for (GBObject ob = objects.get(GBObjectClass.ocArea)[otherTile]; ob != null; ob = ob.next) {
						if (sensor.intersects(ob))
							sensor.collideWith(ob);
					}
			}
		}
	}

	protected int findTile(FinePoint where) {
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

	public void eraseAt(FinePoint where, double radius) {
		makeTiles();
		synchronized (allObjects) {
			Iterator<GBObject> it = allObjects.iterator();
			while (it.hasNext()) {
				GBObject obj = it.next();
				if (obj.getPosition().inRange(where, obj.getRadius() + radius))
					if (obj.getObjectClass() == GBObjectClass.ocRobot) { // must stick
																// around a
																// frame for
																// sensors
						((GBRobot) obj).die(null);
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
	public void resize(FinePoint newsize) {
		size = newsize;
		tilesX = (int) Math.ceil(size.x / kForegroundTileSize);
		tilesY = (int) Math.ceil(size.y / kForegroundTileSize);
		// fix tiles
		makeTiles();
		clearLists();
		resortObjects();
	}

	public FinePoint getSize() {
		return size;
	}

	public double getLeft() {
		return 0;
	}

	public double getTop() {
		return size.y;
	}

	public double getRight() {
		return size.x;
	}

	public double getBottom() {
		return 0;
	}

	// eventually replace calculation with getting from Rules.
	public int getBackgroundTilesX() {
		return (int) Math.ceil(size.x / kBackgroundTileSize);
	}

	public int getBackgroundTilesY() {
		return (int) Math.ceil(size.y / kBackgroundTileSize);
	}

	public int getForegroundTilesX() {
		return tilesX;
	}

	public int getForegroundTilesY() {
		return tilesY;
	}

	// Object selectors triggered from the UI. Synchronization required
	public GBObject getObjectNear(FinePoint where, boolean hitSensors) {
		GBObject best = null;
		double dist = 25; // never see objects farther than this
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				if ((ob.getObjectClass() != GBObjectClass.ocSensorShot || hitSensors)
						&& ob.getObjectClass() != GBObjectClass.ocDecoration
						&& (ob.getObjectClass() == GBObjectClass.ocRobot || best == null
								|| best.getObjectClass() != GBObjectClass.ocRobot || where
									.inRange(ob.getPosition(), ob.getRadius()))
						&& ob.getPosition().inRangeSquared(where, dist)) {
					best = ob;
					dist = (best.getPosition().subtract(where)).normSquare();
				}
		}
		return best;
	}

	public GBObject getRandomInterestingObject() {
		double totalInterest = 0;
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				totalInterest += ob.getInterest();
			if (totalInterest == 0)
				return null;
			for (GBObject ob : allObjects) {
				double interest = ob.getInterest();
				if (GBRandomState.gRandoms.bool(interest / totalInterest))
					return ob;
				totalInterest -= interest;
			}
		}
		return null;
	}

	public GBObject getRandomInterestingObjectNear(FinePoint where, double radius) {
		double totalInterest = 0;
		synchronized (allObjects) {
			for (GBObject ob : allObjects)
				if (ob.getPosition().inRange(where, radius))
					totalInterest += ob.getInterest();
			if (totalInterest == 0)
				return null;
			for (GBObject ob : allObjects)
				if (ob.getPosition().inRange(where, radius)) {
					double interest = ob.getInterest();
					if (GBRandomState.gRandoms.bool(interest / totalInterest))
						return ob;
					totalInterest -= interest;
				}
		}
		return null;
	}
}
