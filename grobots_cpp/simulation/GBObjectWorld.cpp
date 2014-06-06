// GBObjectWorld.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBObjectWorld.h"
#include "GBRobot.h"
#include "GBSensorShot.h"


// world size must be a multiple of kBackgroundTileSize, but need not
//  be a multiple of kForegroundTileSize
const GBCoordinate kWorldWidth = kBackgroundTileSize * 10;
const GBCoordinate kWorldHeight = kBackgroundTileSize * 10;

const GBDistance kLargeRadius = kForegroundTileSize / 2;
// large objects are stored in the last tile, which is special.

const GBRatio kWallRestitution = 0.9;
const GBRatio kRobotRestitution = 0.9;

const GBDamage kEraseDamage = 10000;


#define FOR_EACH_TILE(i, body) \
	for ( long i = 0; i <= tilesX * tilesY; i ++ ) \
		body

#define FOR_EACH_OBJECT_CLASS(cl, body) \
	for ( GBObjectClass cl = ocRobot; cl < kNumObjectClasses; cl ++ ) \
		body

#define FOR_EACH_OBJECT_LIST(i, cl, body) \
	FOR_EACH_TILE(i, { FOR_EACH_OBJECT_CLASS(cl, body) })

#define FOR_EACH_OBJECT_IN_LIST(list, ob, body) \
	for ( GBObject * ob = (list); ob != nil; ob = ob->next ) \
		body

#define FOR_EACH_OBJECT_IN_TILE(tileno, ob, body) \
	FOR_EACH_OBJECT_CLASS(cl, { FOR_EACH_OBJECT_IN_LIST(objects[tileno][cl],  ob, body) })

#define FOR_EACH_OBJECT_IN_LIST_SAFE(list, ob, temp, body) \
	{ \
		GBObject * temp; \
		for ( GBObject * ob = (list); ob != nil; ob = temp ) { \
			temp = ob->next; \
			body \
		} \
	}

#define FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, body) \
	FOR_EACH_OBJECT_LIST(i, cl, { \
		FOR_EACH_OBJECT_IN_LIST(objects[i][cl], ob, body) \
	})

#define FOR_EACH_OBJECT_IN_WORLD_SAFE(i, cl, ob, temp, body) \
	FOR_EACH_OBJECT_LIST(i, cl, { \
		FOR_EACH_OBJECT_IN_LIST_SAFE(objects[i][cl], ob, temp, body) \
	})


void GBObjectWorld::ClearLists() {
// clean up objects
	FOR_EACH_OBJECT_LIST(i, curClass, {
		FOR_EACH_OBJECT_IN_LIST_SAFE(objects[i][curClass], cur, temp, { delete cur; })
		objects[i][curClass] = nil;
	})
// clean up news
	FOR_EACH_OBJECT_IN_LIST_SAFE(news, cur, temp, { delete cur; })
	news = nil;
}

// Puts objects in the appropriate class and tile, and deletes dead ones.
void GBObjectWorld::ResortObjects() {
	try {
		GBObjectTile * old = objects;
		objects = MakeTiles();
		FOR_EACH_OBJECT_LIST(i, cl, {
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, { AddObjectDirectly(cur); })
		})
		delete[] old;
		AddNewObjects();
	} catch ( GBError & err ) {
		NonfatalError(string("Error resorting objects: ") + err.ToString());
	}
}

void GBObjectWorld::AddNewObjects() {
	FOR_EACH_OBJECT_IN_LIST_SAFE(news, cur, temp, { AddObjectDirectly(cur); })
	news = nil;
}

void GBObjectWorld::CollideObjectWithWalls(GBObject * ob) {
	GBObjectClass cl = ob->Class();
	if ( ob->Left() < Left() ) {
		GBVelocity vel = ob->Velocity();
		ob->SetVelocity(abs(vel.x) * kWallRestitution, vel.y);
		if ( cl == ocRobot || cl == ocFood )
			ob->MoveBy(Left() - ob->Left(), 0);
		ob->CollideWithWall();
	} else if ( ob->Right() > Right() ) {
		GBVelocity vel = ob->Velocity();
		ob->SetVelocity(abs(vel.x) * - kWallRestitution, vel.y);
		if ( cl == ocRobot || cl == ocFood )
			ob->MoveBy(Right() - ob->Right(), 0);
		ob->CollideWithWall();
	}
	if ( ob->Bottom() < Bottom() ) {
		GBVelocity vel = ob->Velocity();
		ob->SetVelocity(vel.x, abs(vel.y) * kWallRestitution);
		if ( cl == ocRobot || cl == ocFood )
			ob->MoveBy(0, Bottom() - ob->Bottom());
		ob->CollideWithWall();
	} else if ( ob->Top() > Top() ) {
		GBVelocity vel = ob->Velocity();
		ob->SetVelocity(vel.x, abs(vel.y) * - kWallRestitution);
		if ( cl == ocRobot || cl == ocFood )
			ob->MoveBy(0, Top() - ob->Top());
		ob->CollideWithWall();
	}
}

void GBObjectWorld::MoveAllObjects() {
	try {
		FOR_EACH_TILE(i, {
			if ( i < tilesX || i > tilesX * (tilesY - 1)
					|| i % tilesX == 0 || i % tilesX == tilesX - 1 )
				FOR_EACH_OBJECT_IN_TILE(i,  ob, {
					ob->Move();
					CollideObjectWithWalls(ob); // only large objects and edge tiles
				})
			else
				FOR_EACH_OBJECT_IN_TILE(i,  ob, { ob->Move(); })
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error moving objects: ") + err.ToString());
	}
}

void GBObjectWorld::CollideAllObjects() {
	for ( long tx = 0; tx < tilesX; tx ++ )
		for ( long ty = 0; ty < tilesY; ty ++ ) {
			long t = ty * tilesX + tx;
			CollideSameTile(t);
		// collide with adjacent tiles
			if ( tx < tilesX - 1 ) {
				CollideTwoTiles(t, t + 1);
				if ( ty < tilesY - 1 )
					CollideTwoTiles(t, t + tilesX + 1);
			}
			if ( ty < tilesY - 1 ) {
				CollideTwoTiles(t, t + tilesX);
				if ( tx > 0 )
					CollideTwoTiles(t, t + tilesX - 1);
			}
		// collide with large-object tile
			CollideTwoTiles(t, tilesX * tilesY);
		}
// intercollide large objects, in case that ever matters
	CollideSameTile(tilesX * tilesY);
}

void GBObjectWorld::CollideSameTile(long t) {
	FOR_EACH_OBJECT_IN_LIST(objects[t][ocRobot], bot, {
		try {
			FOR_EACH_OBJECT_IN_LIST(bot->next, bot2, { bot->SolidCollide(bot2, kRobotRestitution); })
		} catch ( GBError & err ) {
			NonfatalError(string("Error colliding robots: ") + err.ToString());
		}
		if ( ((GBRobot *)bot)->hardware.EaterLimit() )
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t][ocFood], food, { bot->BasicCollide(food); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding robot and food: ") + err.ToString());
			}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t][ocShot], shot, { bot->BasicCollide(shot); })
		} catch ( GBError & err ) {
			NonfatalError(string("Error colliding robot and shot: ") + err.ToString());
		}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t][ocArea], area, { bot->BasicCollide(area); })
		} catch ( GBError & err ) {
			NonfatalError(string("Error colliding robot and area: ") + err.ToString());
		}
	})
	try {
		FOR_EACH_OBJECT_IN_LIST(objects[t][ocArea], area, {
			FOR_EACH_OBJECT_IN_LIST(objects[t][ocFood], food, { area->BasicCollide(food); })
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error colliding area and food: ") + err.ToString());
	}
	CollideSensors(t, t);
}

void GBObjectWorld::CollideTwoTiles(long t1, long t2) {
	//Now tries to avoid looking at t2 when the object in t1 isn't even close to it.
	GBCoordinate t2edge = (t2 / tilesX) * kForegroundTileSize - 2;
	if ( t2 == tilesX * tilesY || t2 == t1 + 1 )
		t2edge = -1000; //always do these tiles
	FOR_EACH_OBJECT_IN_LIST(objects[t1][ocRobot], bot, {
		if ( bot->Top() > t2edge ) {
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot2, { bot->SolidCollide(bot2, kRobotRestitution); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding robots: ") + err.ToString());
			}
			if ( ((GBRobot *)bot)->hardware.EaterLimit() )
				try {
					FOR_EACH_OBJECT_IN_LIST(objects[t2][ocFood], food, { bot->BasicCollide(food); })
				} catch ( GBError & err ) {
					NonfatalError(string("Error colliding robot and food: ") + err.ToString());
				}
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocShot], shot, { bot->BasicCollide(shot); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding robot and shot: ") + err.ToString());
			}
		}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t2][ocArea], area, { bot->BasicCollide(area); })
		} catch ( GBError & err ) {
			NonfatalError(string("Error colliding robot and area: ") + err.ToString());
		}
	})
	FOR_EACH_OBJECT_IN_LIST(objects[t1][ocFood], food, {
		if ( food->Top() > t2edge )
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { food->BasicCollide(bot); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding food and robot: ") + err.ToString());
			}
		try {
			FOR_EACH_OBJECT_IN_LIST(objects[t2][ocArea], area, { food->BasicCollide(area); })
		} catch ( GBError & err ) {
			NonfatalError(string("Error colliding food and area: ") + err.ToString());
		}
	})
	try {
		FOR_EACH_OBJECT_IN_LIST(objects[t1][ocShot], shot, {
			if ( shot->Top() > t2edge )
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { shot->BasicCollide(bot); })
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error colliding shot and robot: ") + err.ToString());
	}
	FOR_EACH_OBJECT_IN_LIST(objects[t1][ocArea], area, {
		if ( area->Top() > t2edge ) {
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocRobot], bot, { area->BasicCollide(bot); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding area and robot: ") + err.ToString());
			}
			try {
				FOR_EACH_OBJECT_IN_LIST(objects[t2][ocFood], food, { area->BasicCollide(food); })
			} catch ( GBError & err ) {
				NonfatalError(string("Error colliding area and food: ") + err.ToString());
			}
		}
	})
	CollideSensors(t1, t2);
	CollideSensors(t2, t1);
}

void GBObjectWorld::CollideSensors(long sensorTile, long otherTile) {
	try {
		GBCoordinate tileBottom = (otherTile / tilesX) * kForegroundTileSize - 2;
		GBCoordinate tileTop = (otherTile / tilesX + 1) * kForegroundTileSize - 2;
		if ( otherTile == tilesX * tilesY ) {
			tileBottom = -1000;
			tileTop = size.y + 1000;
		}
		FOR_EACH_OBJECT_IN_LIST(objects[sensorTile][ocSensorShot], sensor, {
			GBObjectClass seen = ((GBSensorShot *)sensor)->Seen();
			if ( seen != ocDead ) {
				CheckObjectClass(seen);
				if ( sensor->Top() > tileBottom && sensor->Bottom() < tileTop )
					FOR_EACH_OBJECT_IN_LIST(objects[otherTile][seen], ob, {
						if ( sensor->Intersects(ob) )
							sensor->CollideWith(ob);
							// note one-directional collision, since ob mustn't care it's been sensed
					})
				if ( seen == ocShot ) // shot sensors see area shots too
					FOR_EACH_OBJECT_IN_LIST(objects[otherTile][ocArea], ob, {
						if ( sensor->Intersects(ob) )
							sensor->CollideWith(ob);
					})
			}
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error colliding sensor-shot with other object: ") + err.ToString());
	}
}

void GBObjectWorld::CheckObjectClass(const GBObjectClass cl) const {
	if ( cl < ocRobot || cl >= kNumObjectClasses )
		throw GBBadObjectClassError();
}

long GBObjectWorld::FindTile(const GBPosition where) const {
	long tx = floor(where.x) / kForegroundTileSize;
	if ( tx < 0 ) tx = 0;
	if ( tx >= tilesX ) tx = tilesX - 1;
	long ty = floor(where.y) / kForegroundTileSize;
	if ( ty < 0 ) ty = 0;
	if ( ty >= tilesY ) ty = tilesY - 1;
	return ty * tilesX + tx;
}

GBObjectTile * GBObjectWorld::MakeTiles() const {
	GBObjectTile * tiles = new GBObjectTile[tilesX * tilesY + 1];
	if ( ! tiles ) throw GBOutOfMemoryError();
	FOR_EACH_OBJECT_LIST(i, cl, { tiles[i][cl] = nil; })
	return tiles;
}

GBObjectWorld::GBObjectWorld()
	: size(kWorldWidth, kWorldHeight),
	tilesX(ceil(kWorldWidth / kForegroundTileSize)),
	tilesY(ceil(kWorldHeight / kForegroundTileSize)),
	objects(nil),
	news(nil)
{
	objects = MakeTiles();
}

GBObjectWorld::~GBObjectWorld() {
	ClearLists();
	delete[] objects;
}

void GBObjectWorld::EraseAt(const GBPosition & where, const GBDistance radius) {
// modified from ResortObjects. Could be replaced with some dead-marking system
	try {
		GBObjectTile * old = objects;
		objects = MakeTiles();
		FOR_EACH_OBJECT_LIST(i, cl, {
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, {
				if ( cur->Position().InRange(where, cur->Radius() + radius) )
					if ( cur->Class() == ocRobot ) { //must stick around a frame for sensors
							((GBRobot *)cur)->Die(nil);
							AddObjectNew(cur); // new so it'll be deleted next resort
						}
					else
						delete cur;
				else
					AddObjectDirectly(cur);
			})
		})
		delete[] old;
	} catch ( GBError & err ) {
		NonfatalError(string("Error erasing objects: ") + err.ToString());
	}
}

void GBObjectWorld::AddObjectNew(GBObject * newOb) {
	if ( ! newOb ) throw GBNilPointerError();
	newOb->next = news;
	news = newOb;
}

void GBObjectWorld::AddObjectDirectly(GBObject * ob) {
	if ( ! ob ) throw GBNilPointerError();
	GBObjectClass dest = ob->Class();
	if ( dest == ocDead )
		delete ob;
	else {
		long tile = ob->Radius() * 2 >= kForegroundTileSize ? tilesX * tilesY : FindTile(ob->Position());
		ob->next = objects[tile][dest];
		objects[tile][dest] = ob;
	}
}

void GBObjectWorld::Resize(const GBFinePoint & newsize) {
	long oldLastTile = tilesX * tilesY;
	size = newsize;
	tilesX = ceil(size.x / kForegroundTileSize);
	tilesY = ceil(size.y / kForegroundTileSize);
// fix tiles
	GBObjectTile * old = objects;
	objects = MakeTiles();
	for ( long i = 0; i <= oldLastTile; i ++ )
		FOR_EACH_OBJECT_CLASS(cl,{
			FOR_EACH_OBJECT_IN_LIST_SAFE(old[i][cl], cur, temp, { AddObjectDirectly(cur); })
		})
	delete[] old;
}

GBFinePoint GBObjectWorld::Size() const {
	return size;
}

GBCoordinate GBObjectWorld::Left() const {
	return 0;
}

GBCoordinate GBObjectWorld::Top() const {
	return size.y;
}

GBCoordinate GBObjectWorld::Right() const {
	return size.x;
}

GBCoordinate GBObjectWorld::Bottom() const {
	return 0;
}

// eventually replace calculation with getting from Rules.
long GBObjectWorld::BackgroundTilesX() const {
	return ceil(size.x / kBackgroundTileSize);
}

long GBObjectWorld::BackgroundTilesY() const {
	return ceil(size.y / kBackgroundTileSize);
}

long GBObjectWorld::ForegroundTilesX() const {
	return tilesX;
}

long GBObjectWorld::ForegroundTilesY() const {
	return tilesY;
}

GBObject * GBObjectWorld::ObjectNear(const GBPosition where, bool hitSensors) const {
	GBObject * best = nil;
	GBDistance dist = 5; // never see objects farther than this
	try {
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			if ( (ob->Class() != ocSensorShot || hitSensors)
					&& ob->Class() != ocDecoration
					&& (ob->Class() == ocRobot || ! best || best->Class() != ocRobot
						|| where.InRange(ob->Position(), ob->Radius()))
					&& ob->Position().InRange(where, dist) ) {
				best = ob;
				dist = (best->Position() - where).Norm();
			}
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error in GBObjectWorld::ObjectNear: ") + err.ToString());
	}
	return best;
}

GBObject * GBObjectWorld::GetObjects(const long tilex, const long tiley, const GBObjectClass which) const {
	CheckObjectClass(which);
	if ( tilex < 0 || tilex >= tilesX ||
			tiley < 0 || tiley >= tilesY )
		throw GBIndexOutOfRangeError();
	return objects[tilex + tiley * tilesX][which];
}

GBObject * GBObjectWorld::GetLargeObjects(const GBObjectClass which) const {
	CheckObjectClass(which);
	return objects[tilesX * tilesY][which];
}

long GBObjectWorld::CountObjects(const GBObjectClass cl) const {
	CheckObjectClass(cl);
	long count = 0;
	for ( long i = 0; i <= tilesX * tilesY; i ++ )
		for ( GBObject * cur = objects[i][cl]; cur != nil; cur = cur->next )
			count ++;
	return count;
}

GBObject * GBObjectWorld::RandomInterestingObject() const {
	try {
		GBNumber totalInterest = 0;
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			totalInterest += ob->Interest();
		})
		if ( ! totalInterest ) return nil;
		FOR_EACH_OBJECT_IN_WORLD(ii, cl, ob, {
			GBNumber interest = ob->Interest();
			if ( gRandoms.Boolean(interest / totalInterest) )
				return ob;
			totalInterest -= interest;
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error in GBObjectWorld::RandomInterestingObject: ") + err.ToString());
	}
	return nil;
}

GBObject * GBObjectWorld::RandomInterestingObjectNear(const GBPosition where, GBDistance radius) const {
	try {
		GBNumber totalInterest = 0;
		FOR_EACH_OBJECT_IN_WORLD(i, cl, ob, {
			if ( ob->Position().InRange(where, radius) )
				totalInterest += ob->Interest();
		})
		if ( !totalInterest ) return nil;
		FOR_EACH_OBJECT_IN_WORLD(ii, cl, ob, {
			if ( ob->Position().InRange(where, radius) ) {
				GBNumber interest = ob->Interest();
				if ( gRandoms.Boolean(interest / totalInterest) )
					return ob;
				totalInterest -= interest;
			}
		})
	} catch ( GBError & err ) {
		NonfatalError(string("Error in GBObjectWorld::RandomInterestingObjectNear: ") + err.ToString());
	}
	return nil;
}

