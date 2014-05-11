package simulation;

import support.*;
import exception.*;
// GBObjectWorld.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

//typedef GBObject * GBObjectTile[kNumObjectClasses];

public class GBObjectWorld extends Model {
	// Background tiles are purely a display matter;
	// they are here as a reminder to use world sizes that
	// are a multiple of kBackgroundTileSize.
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
	protected GBObject[][] objects;
	protected GBObject news;

	/*
	 * #define FOR_EACH_TILE(i, body) \ for ( int i = 0; i <= tilesX * tilesY; i
	 * ++ ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_CLASS(cl, body) \ for ( GBObjectClass cl = ocRobot;
	 * cl < kNumObjectClasses; cl ++ ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_LIST(i, cl, body) \ FOR_EACH_TILE(i, {
	 * FOR_EACH_OBJECT_CLASS(cl, body) })
	 * 
	 * #define FOR_EACH_OBJECT_IN_LIST(list, ob, body) \ for ( GBObject * ob =
	 * (list); ob != null; ob = ob.next ) \ body
	 * 
	 * #define FOR_EACH_OBJECT_IN_TILE(tileno, ob, body) \
	 * FOR_EACH_OBJECT_CLASS(cl, { FOR_EACH_OBJECT_IN_LIST(objects[tileno][cl],
	 * ob, body) })
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

	protected void ClearLists() {
// clean up objects
	FOR_EACH_OBJECT_LIST(i, curClass, {
		FOR_EACH_OBJECT_IN_LIST_SAFE(objects[i][curClass], cur, temp, { delete cur; })
		objects[i][curClass] = null;
	})
// clean up news
	FOR_EACH_OBJECT_IN_LIST_SAFE(news, cur, temp, { delete cur; })
	news = null;
}

	// Puts objects in the appropriate class and tile, and deletes dead ones.
	protected void ResortObjects() {
	try {
		GBObject[][] old = objects;
		objects = MakeTiles();
		FOR_EACH_OBJECT_LIST(i, cl, {
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, { AddObjectDirectly(cur); })
		})
		delete[] old;
		AddNewObjects();
	} catch ( GBError err ) {
		GBError.NonfatalError("Error resorting objects: " + err.ToString());
	}
}

	protected void AddNewObjects() {
	FOR_EACH_OBJECT_IN_LIST_SAFE(news, cur, temp, { AddObjectDirectly(cur); })
	news = null;
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
	try {
		FOR_EACH_TILE(i, {
			if ( i < tilesX || i > tilesX * (tilesY - 1)
					|| i % tilesX == 0 || i % tilesX == tilesX - 1 )
				FOR_EACH_OBJECT_IN_TILE(i,  ob, {
					ob.Move();
					CollideObjectWithWalls(ob); // only large objects and edge tiles
				})
			else
				FOR_EACH_OBJECT_IN_TILE(i,  ob, { ob.Move(); })
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error moving objects: " + err.ToString());
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
	FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocRobot.value], bot, {
		try {
			FOR_EACH_OBJECT_IN_LIST(bot.next, bot2, { bot.SolidCollide(bot2, kRobotRestitution); })
		} catch ( GBError err ) {
			GBError.NonfatalError("Error colliding robots: " + err.ToString());
		}
		if ( ((GBRobot)bot).hardware.EaterLimit() )
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocFood.value], food, { bot.BasicCollide(food); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding robot and food: " + err.ToString());
			}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocShot.value], shot, { bot.BasicCollide(shot); })
		} catch ( GBError err ) {
			GBError.NonfatalError("Error colliding robot and shot: " + err.ToString());
		}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocArea.value], area, { bot.BasicCollide(area); })
		} catch ( GBError err ) {
			GBError.NonfatalError("Error colliding robot and area: " + err.ToString());
		}
	})
	try {
		FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocArea.value], area, {
			FOR_EACH_OBJECT_IN_LIST(objects[t][GBObjectClass.ocFood.value], food, { area.BasicCollide(food); })
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error colliding area and food: " + err.ToString());
	}
	CollideSensors(t, t);
}

	// Does this object in t1 come within 2 units of t2? If not, we don't need
	// to collide it with small objects in t2.
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
	//Now tries to avoid looking at t2 when the object in t1 isn't even close to it.
	double t2edge = (t2 / tilesX) * kForegroundTileSize - 2;
	if ( t2 == tilesX * tilesY || t2 == t1 + 1 )
		t2edge = -1000; //always do these tiles
	FOR_EACH_OBJECT_IN_LIST(objects[t1][GBObjectClass.ocRobot.value], bot, {
		if ( bot.Top() > t2edge ) {
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][GBObjectClass.ocRobot.value], bot2, { bot.SolidCollide(bot2, kRobotRestitution); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding robots: " + err.ToString());
			}
			if ( ((GBRobot)bot).hardware.EaterLimit() )
				try {
					FOR_EACH_OBJECT_IN_LIST(objects[t2][ocFood], food, { bot.BasicCollide(food); })
				} catch ( GBError err ) {
					GBError.NonfatalError("Error colliding robot and food: " + err.ToString());
				}
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocShot], shot, { bot.BasicCollide(shot); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding robot and shot: " + err.ToString());
			}
		}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t2][ocArea], area, { bot.BasicCollide(area); })
		} catch ( GBError err ) {
			GBError.NonfatalError("Error colliding robot and area: " + err.ToString());
		}
	})
	FOR_EACH_OBJECT_IN_LIST(objects[t1][ocFood], food, {
		if ( food.Top() > t2edge )
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { food.BasicCollide(bot); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding food and robot: " + err.ToString());
			}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t2][ocArea], area, { food.BasicCollide(area); })
		} catch ( GBError err ) {
			GBError.NonfatalError("Error colliding food and area: " + err.ToString());
		}
	})
	try {
		FOR_EACH_OBJECT_IN_LIST(objects[t1][ocShot], shot, {
			if ( shot.Top() > t2edge )
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { shot.BasicCollide(bot); })
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error colliding shot and robot: " + err.ToString());
	}
	FOR_EACH_OBJECT_IN_LIST(objects[t1][ocArea], area, {
		if ( area.Top() > t2edge ) {
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { area.BasicCollide(bot); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding area and robot: " + err.ToString());
			}
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocFood], food, { area.BasicCollide(food); })
			} catch ( GBError err ) {
				GBError.NonfatalError("Error colliding area and food: " + err.ToString());
			}
		}
	})
	CollideSensors(t1, t2);
	CollideSensors(t2, t1);
}

	protected void CollideSensors(int sensorTile, int otherTile) {
	try {
		double tileBottom = (otherTile / tilesX) * kForegroundTileSize - 2;
		double tileTop = (otherTile / tilesX + 1) * kForegroundTileSize - 2;
		if ( otherTile == tilesX * tilesY ) {
			tileBottom = -1000;
			tileTop = size.y + 1000;
		}
		FOR_EACH_OBJECT_IN_LIST(objects[sensorTile][ocSensorShot], sensor, {
			GBObjectClass seen = ((GBSensorShot)sensor).Seen();
			if ( seen != ocDead ) {
				CheckObjectClass(seen);
				if ( sensor.Top() > tileBottom && sensor.Bottom() < tileTop )
					FOR_EACH_OBJECT_IN_LIST(objects[otherTile][seen], ob, {
						if ( sensor.Intersects(ob) )
							sensor.CollideWith(ob);
							// note one-directional collision, since ob mustn't care it's been sensed
					})
				if ( seen == ocShot ) // shot sensors see area shots too
					FOR_EACH_OBJECT_IN_LIST(objects[otherTile][ocArea], ob, {
						if ( sensor.Intersects(ob) )
							sensor.CollideWith(ob);
					})
			}
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error colliding sensor-shot with other object: " + err.ToString());
	}
}

	protected void CheckObjectClass(GBObjectClass cl) throws GBError {
		if (cl.value < GBObjectClass.ocRobot.value
				|| cl.value >= GBObjectClass.kNumObjectClasses.value)
			throw new GBBadObjectClassError();
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

	protected GBObject[][] MakeTiles()  {
	GBObject[][] tiles = new GBObject[tilesX * tilesY + 1][GBObjectClass.values().length];
	if ( tiles == null ) throw new GBOutOfMemoryError();
	FOR_EACH_OBJECT_LIST(i, cl, { tiles[i][cl] = null; })
	return tiles;
}

	public GBObjectWorld() {
		size = new FinePoint(kWorldWidth, kWorldHeight);
		tilesX = (int) (Math.ceil(kWorldWidth / kForegroundTileSize));
		tilesY = (int) (Math.ceil(kWorldHeight / kForegroundTileSize));
		objects = MakeTiles();
	}

	public void EraseAt( FinePoint where,  GBDistance radius) {
// modified from ResortObjects. Could be replaced with some dead-marking system
	try {
		GBObject[] old = objects;
		objects = MakeTiles();
		FOR_EACH_OBJECT_LIST(i, cl, {
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, {
				if ( cur.Position().InRange(where, cur.Radius() + radius) )
					if ( cur.Class() == ocRobot ) { //must stick around a frame for sensors
							((GBRobot *)cur).Die(null);
							AddObjectNew(cur); // new so it'll be deleted next resort
						}
					else
						delete cur;
				else
					AddObjectDirectly(cur);
			})
		})
		delete[] old;
	} catch ( GBError err ) {
		GBError.NonfatalError("Error erasing objects: " + err.ToString());
	}
}

	public void AddObjectNew(GBObject newOb) throws GBError {
		if (newOb == null)
			throw new GBNilPointerError();
		newOb.next = news;
		news = newOb;
	}

	public void AddObjectDirectly(GBObject ob) {
	if ( ob == null ) throw new GBNilPointerError();
	GBObjectClass dest = ob.Class();
	if ( dest == ocDead )
		delete ob;
	else {
		int tile = ob.Radius() * 2 >= kForegroundTileSize ? tilesX * tilesY : FindTile(ob.Position());
		ob.next = objects[tile][dest];
		objects[tile][dest] = ob;
	}
}

	public void Resize( FinePoint newsize) {
	int oldLastTile = tilesX * tilesY;
	size = newsize;
	tilesX = ceil(size.x / kForegroundTileSize);
	tilesY = ceil(size.y / kForegroundTileSize);
// fix tiles
	GBObject[] old = objects;
	objects = MakeTiles();
	for ( int i = 0; i <= oldLastTile; i ++ )
		FOR_EACH_OBJECT_CLASS(cl,{
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, { AddObjectDirectly(cur); })
		})
	delete[] old;
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

	public GBObject ObjectNear( FinePoint where, boolean hitSensors)  {
	GBObject best = null;
	GBDistance dist = 5; // never see objects farther than this
	try {
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			if ( (ob.Class() != GBObjectClass.ocSensorShot || hitSensors)
					&& ob.Class() != GBObjectClass.ocDecoration
					&& (ob.Class() == GBObjectClass.ocRobot || ! best || best.Class() != GBObjectClass.ocRobot
						|| where.InRange(ob.Position(), ob.Radius()))
					&& ob.Position().InRange(where, dist) ) {
				best = ob;
				dist = (best.Position() - where).Norm();
			}
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error in ObjectNear: " + err.ToString());
	}
	return best;
}

	public GBObject GetObjects(int tilex, int tiley, GBObjectClass which) throws GBError {
		CheckObjectClass(which);
		if (tilex < 0 || tilex >= tilesX || tiley < 0 || tiley >= tilesY)
			throw new GBIndexOutOfRangeError();
		return objects[tilex + tiley * tilesX][which.value];
	}

	public GBObject GetLargeObjects(GBObjectClass which) throws GBError {
		CheckObjectClass(which);
		return objects[tilesX * tilesY][which.value];
	}

	public int CountObjects(GBObjectClass cl) throws GBError {
		CheckObjectClass(cl);
		int count = 0;
		for (int i = 0; i <= tilesX * tilesY; i++)
			for (GBObject cur = objects[i][cl.value]; cur != null; cur = cur.next)
				count++;
		return count;
	}

	public GBObject RandomInterestingObject()  {
	try {
		GBNumber totalInterest = 0;
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			totalInterest += ob.Interest();
		})
		if ( ! totalInterest ) return null;
		FOR_EACH_OBJECT_IN_WORLD(ii, cl, ob, {
			GBNumber interest = ob.Interest();
			if ( gRandoms.booleanean(interest / totalInterest) )
				return ob;
			totalInterest -= interest;
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error in RandomInterestingObject: " + err.ToString());
	}
	return null;
}

	public GBObject RandomInterestingObjectNear( FinePoint where, GBDistance radius)  {
	try {
		GBNumber totalInterest = 0;
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			if ( ob.Position().InRange(where, radius) )
				totalInterest += ob.Interest();
		})
		if ( !totalInterest ) return null;
		FOR_EACH_OBJECT_IN_WORLD(ii, cl, ob, {
			if ( ob.Position().InRange(where, radius) ) {
				GBNumber interest = ob.Interest();
				if ( gRandoms.booleanean(interest / totalInterest) )
					return ob;
				totalInterest -= interest;
			}
		})
	} catch ( GBError err ) {
		GBError.NonfatalError("Error in RandomInterestingObjectNear: " + err.ToString());
	}
	return null;
}
}
