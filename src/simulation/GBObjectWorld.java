package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
// GBObjectWorld.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import java.util.Map;

import support.FinePoint;
import support.GBObjectClass;
import support.GBRandomState;
import support.Model;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;

public class GBObjectWorld extends Model {
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
	public Map<GBObjectClass, GBObject[]> objects;
	// protected GBObject[][] objects;
	protected List<GBObject> news; // Head of a singly-linked list of GBObject
									// representing new objects to be handled

	/*
	 * #define FOR_EACH_TILE(i, body) \ for ( int i = 0; i <= tilesX * tilesY;
	 * i++ ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_CLASS(cl, body) \ for ( GBObjectClass cl =
	 * ocRobot;cl < kNumObjectClasses; cl ++ ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_LIST(i, cl, body) \ FOR_EACH_TILE(i,
	 * {FOR_EACH_OBJECT_CLASS(cl, body) })
	 * 
	 * #define FOR_EACH_OBJECT_IN_LIST(list, ob, body) \ for ( GBObject * ob
	 * =(list); ob != null; ob = ob.next ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_IN_TILE(tileno, ob, body) \
	 * FOR_EACH_OBJECT_CLASS(cl, {
	 * FOR_EACH_OBJECT_IN_LIST(objects[tileno][cl],ob, body) })
	 * 
	 * #define FOR_EACH_OBJECT_IN_LIST_SAFE(list, ob, temp, body) \ { \ GBObject
	 * * temp; \ for ( GBObject * ob = (list); ob != null; ob = temp ) { \ temp
	 * = ob.next; \ body \ } \ }
	 * 
	 * #define FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, body) \
	 * FOR_EACH_OBJECT_LIST(i, cl, { \ FOR_EACH_OBJECT_IN_LIST(objects[i][cl],
	 * ob, body) \ })
	 * 
	 * #define FOR_EACH_OBJECT_IN_WORLD_SAFE(i, cl, ob, temp, body) \
	 * FOR_EACH_OBJECT_LIST(i, cl, { \
	 * FOR_EACH_OBJECT_IN_LIST_SAFE(objects[i][cl], ob, temp, body) \ })
	 */
	/*
	 * Calls from GBWorld go in the order: 1) Think All 2) Move All 3) Act All
	 * 4) Resort All 5) Collide All 6) Start next turn 7) Collect statistics
	 */
	public GBObjectWorld() {
		size = new FinePoint(kWorldWidth, kWorldHeight);
		tilesX = (int) (Math.ceil(kWorldWidth / kForegroundTileSize));
		tilesY = (int) (Math.ceil(kWorldHeight / kForegroundTileSize));
		allObjects = new ArrayList<GBObject>(100000);
		objects = new HashMap<GBObjectClass, GBObject[]>();
		news = new ArrayList<GBObject>(2000);
		MakeTiles();
	}

	protected void MakeTiles() {
		GBObject[] template = new GBObject[tilesX * tilesY + 1];
		for (GBObjectClass cl : GBObjectClass.values())
			objects.put(cl, template.clone());
	}

	/**
	 * Clear all object lists except the main one, and reset each object's next
	 */
	protected void ClearLists() {
		MakeTiles();		
		for (GBObject obj : allObjects)
			obj.next = null;
		allObjects.clear();
		news.clear();
	}

	/**
	 * Puts objects in the appropriate class and tile, and deletes dead ones.
	 * 
	 * @throws GBAbort
	 *             @
	 */
	protected void ResortObjects() throws GBAbort {
		GBObject[] template = new GBObject[tilesX * tilesY + 1];
		for (GBObjectClass cl : GBObjectClass.values())
			objects.put(cl, template.clone());
		Iterator<GBObject> it = allObjects.iterator();
		while (it.hasNext()) {
			GBObject obj = it.next();
			obj.next = null;
			if (obj.Class() != GBObjectClass.ocDead) {
				AddObjectDirectly(obj);
			} else
				// TODO: check that allObjects is the only place the dead
				// object is referenced;
				// otherwise GC won't happen
				it.remove();
		}
		addNewObjects();
	}

	protected void addNewObjects() {
		Iterator<GBObject> it = news.iterator();
		while (it.hasNext()) {
			GBObject ob = it.next();
			allObjects.add(ob);
			AddObjectDirectly(ob);
		}
		news.clear();
	}

	/**
	 * Add an object to the new object list. Objects in this list will be added
	 * to the world on the next call to addNewObjects
	 * 
	 * @param newOb
	 * @throws GBNilPointerError
	 */
	public void AddObjectNew(GBObject newOb) {
		if (newOb != null)
			news.add(newOb);
	}

	private void AddObjectDirectly(GBObject ob) {
		if (ob != null)
			if (ob.Class() != GBObjectClass.ocDead) {
				// If object is dead, leave it alone and let GC take care of it.
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
		// try {
		for (int i = 0; i < tilesX * tilesY; i++)
			for (GBObjectClass cl : GBObjectClass.values())
				for (GBObject obj = objects.get(cl)[i]; obj != null; obj = obj.next) {
					obj.Move();
					if (i < tilesX || i > tilesX * (tilesY - 1)
							|| i % tilesX == 0 || i % tilesX == tilesX - 1)
						CollideObjectWithWalls(obj); // only large objects
														// and edge tiles
				}
		// }
		// catch ( GBError err ) {
		// GBError.NonfatalError("Error moving objects: " + err.toString());
		// }
	}

	protected void CollideAllObjects() throws GBAbort {
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

	protected void CollideSameTile(int t) throws GBAbort {
		for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t]; bot != null; bot = bot.next) {
			// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocRobot.value],
			// bot, {
			try {
				for (GBObject bot2 = bot.next; bot2 != null; bot2 = bot2.next) {
					// FOR_EACH_OBJECT_IN_LIST(bot.next, bot2, {
					bot.SolidCollide(bot2, kRobotRestitution);
				}// )
			} catch (GBError err) {
				GBError.NonfatalError("Error colliding robots: "
						+ err.toString());
			}
			if (((GBRobot) bot).hardware.EaterLimit() != 0)
				try {
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocFood.value],
						// food, {
						bot.BasicCollide(food);
					}// )
						// }
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding robot and food: "
							+ err.toString());
				}
			try {
				for (GBObject shot = objects.get(GBObjectClass.ocShot)[t]; shot != null; shot = shot.next) {
					// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocShot.value],
					// shot, {
					bot.BasicCollide(shot);
				}// )
					// }
			} catch (GBError err) {
				GBError.NonfatalError("Error colliding robot and shot: "
						+ err.toString());
			}
			try {
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next) {
					// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocArea.value],
					// area, {
					bot.BasicCollide(area);
				}// )
					// }
			} catch (GBError err) {
				GBError.NonfatalError("Error colliding robot and area: "
						+ err.toString());
			}
		}// )
		try {
			for (GBObject area = objects.get(GBObjectClass.ocArea)[t]; area != null; area = area.next) {
				// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocArea.value],
				// area, {
				for (GBObject food = objects.get(GBObjectClass.ocFood)[t]; food != null; food = food.next) {
					// FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocFood.value],
					// food, {
					area.BasicCollide(food);
				}// )
			}// )
		} catch (GBError err) {
			GBError.NonfatalError("Error colliding area and food: "
					+ err.toString());
		}
		CollideSensors(t, t);
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

	protected void CollideTwoTiles(int t1, int t2) throws GBAbort {
		// Now tries to avoid looking at t2 when the object in t1 isn't even
		// close to it.
		double t2edge = (t2 / tilesX) * kForegroundTileSize - 2;
		if (t2 == tilesX * tilesY || t2 == t1 + 1)
			t2edge = -1000; // always do these tiles
		for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t1]; bot != null; bot = bot.next) {
			// FOR_EACH_OBJECT_IN_LIST(objects[t1][GBObjectClass.ocRobot.value],
			// bot, {
			if (bot.Top() > t2edge) {
				try {
					for (GBObject bot2 = objects.get(GBObjectClass.ocRobot)[t2]; bot2 != null; bot2 = bot2.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocRobot.value],
						// bot2, {
						bot.SolidCollide(bot2, kRobotRestitution);
					}// )
						// }
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding robots: "
							+ err.toString());
				}
				if (((GBRobot) bot).hardware.EaterLimit() != 0)
					try {
						for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
							// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocFood.value],
							// food, {
							bot.BasicCollide(food); // })
						}
					} catch (GBError err) {
						GBError.NonfatalError("Error colliding robot and food: "
								+ err.toString());
					}
				try {
					for (GBObject shot = objects.get(GBObjectClass.ocShot)[t2]; shot != null; shot = shot.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocShot.value],
						// shot, {
						bot.BasicCollide(shot); // })
					}
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding robot and shot: "
							+ err.toString());
				}
			}
			try {
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocArea.value],
					// area, {
					bot.BasicCollide(area);
				}// )
			} catch (GBError err) {
				GBError.NonfatalError("Error colliding robot and area: "
						+ err.toString());
			}
		}// )
		for (GBObject food = objects.get(GBObjectClass.ocFood)[t1]; food != null; food = food.next) {
			// FOR_EACH_OBJECT_IN_LIST(objects[t1][GBObjectClass.ocFood], food,
			// {
			if (food.Top() > t2edge)
				try {
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocRobot.value],
						// bot, {
						food.BasicCollide(bot); // })
					}
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding food and robot: "
							+ err.toString());
				}
			try {
				for (GBObject area = objects.get(GBObjectClass.ocArea)[t2]; area != null; area = area.next) {
					// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocArea.value],
					// area, {
					food.BasicCollide(area); // })
				}
			} catch (GBError err) {
				GBError.NonfatalError("Error colliding food and area: "
						+ err.toString());
			}
		}// )
		try {
			for (GBObject shot = objects.get(GBObjectClass.ocShot)[t1]; shot != null; shot = shot.next) {
				// FOR_EACH_OBJECT_IN_LIST(objects[t1][GBObjectClass.ocShot.value],
				// shot, {
				if (shot.Top() > t2edge)
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocRobot],
						// bot, {
						shot.BasicCollide(bot); // })
					}
			}// )
		} catch (GBError err) {
			GBError.NonfatalError("Error colliding shot and robot: "
					+ err.toString());
		}
		for (GBObject area = objects.get(GBObjectClass.ocArea)[t1]; area != null; area = area.next) {
			// FOR_EACH_OBJECT_IN_LIST(objects[t1][GBObjectClass.ocArea.value],
			// area, {
			if (area.Top() > t2edge) {
				try {
					for (GBObject bot = objects.get(GBObjectClass.ocRobot)[t2]; bot != null; bot = bot.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocRobot.value],
						// bot, {
						area.BasicCollide(bot); // })
					}
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding area and robot: "
							+ err.toString());
				}
				try {
					for (GBObject food = objects.get(GBObjectClass.ocFood)[t2]; food != null; food = food.next) {
						// FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocFood.value],
						// food, {
						area.BasicCollide(food); // })
					}
				} catch (GBError err) {
					GBError.NonfatalError("Error colliding area and food: "
							+ err.toString());
				}
			}
		}// )
		CollideSensors(t1, t2);
		CollideSensors(t2, t1);
	}

	protected void CollideSensors(int sensorTile, int otherTile) throws GBAbort {
		try {
			double tileBottom = (otherTile / tilesX) * kForegroundTileSize - 2;
			double tileTop = (otherTile / tilesX + 1) * kForegroundTileSize - 2;
			if (otherTile == tilesX * tilesY) {
				tileBottom = -1000;
				tileTop = size.y + 1000;
			}
			for (GBObject sensor = objects.get(GBObjectClass.ocSensorShot)[sensorTile]; sensor != null; sensor = sensor.next) {
				// FOR_EACH_OBJECT_IN_LIST(objects[sensorTile][GBObjectClass.ocSensorShot.value],
				// sensor, {
				GBObjectClass seen = ((GBSensorShot) sensor).Seen();
				if (seen != GBObjectClass.ocDead) {
					if (sensor.Top() > tileBottom && sensor.Bottom() < tileTop)
						for (GBObject ob = objects.get(seen)[otherTile]; ob != null; ob = ob.next) {
							// FOR_EACH_OBJECT_IN_LIST(objects[otherTile][seen],
							// ob, {
							if (sensor.Intersects(ob))
								sensor.CollideWith(ob);
							// note one-directional collision, since ob mustn't
							// care it's been sensed
						}// )
					if (seen == GBObjectClass.ocShot) // shot sensors see area
														// shots too
						for (GBObject ob = objects.get(GBObjectClass.ocArea)[otherTile]; ob != null; ob = ob.next) {
							// FOR_EACH_OBJECT_IN_LIST(objects[otherTile][GBObjectClass.ocArea.value],
							// ob, {
							if (sensor.Intersects(ob))
								sensor.CollideWith(ob);
						}// )
				}
			}// )
		} catch (GBError err) {
			GBError.NonfatalError("Error colliding sensor-shot with other object: "
					+ err.toString());
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
		// modified from ResortObjects. Could be replaced with some dead-marking
		// system
		ClearLists();
		Iterator<GBObject> it = allObjects.iterator();
		while (it.hasNext()) {
			GBObject obj = it.next();
			if (obj.Position().inRange(where, obj.Radius() + radius))
				if (obj.Class() == GBObjectClass.ocRobot) { // must stick
															// around a
															// frame for
															// sensors
					((GBRobot) obj).Die(null);
					AddObjectNew(obj); // new so it'll be deleted next
										// resort
				} else
					it.remove();
			else
				AddObjectDirectly(obj);
		}
	}

	// TODO: Find where this is used and make sure it acts like it should
	public void Resize(FinePoint newsize) throws GBNilPointerError,
			GBBadArgumentError, GBAbort {
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

	public GBObject ObjectNear(FinePoint where, boolean hitSensors) {
		GBObject best = null;
		double dist = 5; // never see objects farther than this
		// try {
		for (GBObject ob : allObjects) {
			if ((ob.Class() != GBObjectClass.ocSensorShot || hitSensors)
					&& ob.Class() != GBObjectClass.ocDecoration
					&& (ob.Class() == GBObjectClass.ocRobot || best == null
							|| best.Class() != GBObjectClass.ocRobot || where
								.inRange(ob.Position(), ob.Radius()))
					&& ob.Position().inRange(where, dist)) {
				best = ob;
				dist = (best.Position().subtract(where)).norm();
			}
		}
		// } catch ( GBError err ) {
		// GBError.NonfatalError("Error in ObjectNear: " + err.toString());
		// }
		return best;
	}

	public GBObject GetObjects(int tilex, int tiley, GBObjectClass which)
			throws GBIndexOutOfRangeError {
		// CheckObjectClass(which);
		if (tilex < 0 || tilex >= tilesX || tiley < 0 || tiley >= tilesY)
			throw new GBIndexOutOfRangeError();
		return objects.get(which)[tilex + tiley * tilesX];
	}

	public GBObject GetLargeObjects(GBObjectClass which) {
		// CheckObjectClass(which);
		return objects.get(which)[tilesX * tilesY];
	}

	public int CountObjects(GBObjectClass cl) {
		int i = 0;
		for (GBObject obj : allObjects)
			if (obj.Class() == cl)
				i++;
		return i;
	}

	public GBObject RandomInterestingObject() {
		// try {
		double totalInterest = 0;
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
		// } catch ( GBError err ) {
		// GBError.NonfatalError("Error in RandomInterestingObject: " +
		// err.toString());
		// }
		return null;
	}

	public GBObject RandomInterestingObjectNear(FinePoint where, double radius) {
		// try {
		double totalInterest = 0;
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
		// })
		// } catch ( GBError err ) {
		// GBError.NonfatalError("Error in RandomInterestingObjectNear: " +
		// err.toString());
		// }
		return null;
	}
}
