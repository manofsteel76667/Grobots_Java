// GBObjectWorld.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef _GBObjectWorld_h
#define _GBObjectWorld_h

#include "GBObject.h"


// Background tiles are purely a display matter;
// they are here as a reminder to use world sizes that
// are a multiple of kBackgroundTileSize.
const int kBackgroundTileSize = 10;
const int kForegroundTileSize = 10;


typedef GBObject * GBObjectTile[kNumObjectClasses];

class GBObjectWorld {
protected:
	GBFinePoint size;
	int tilesX, tilesY; // these are foreground tiles
	GBObjectTile * objects;
	GBObject * news;
// setup
	void ClearLists();
// operation
	void ResortObjects();
	void AddNewObjects();
	void MoveAllObjects();
	void CollideObjectWithWalls(GBObject * ob);
	void CollideAllObjects();
	void CollideSameTile(long t);
	void CollideTwoTiles(long t1, long t2);
	void CollideSensors(long sensorTile, long otherTile);
// utilities
	void CheckObjectClass(const GBObjectClass cl) const;
	long FindTile(const GBPosition where) const;
	GBObjectTile * MakeTiles() const;

public:
	GBObjectWorld();
	~GBObjectWorld();
// operation
	void EraseAt(const GBPosition & where, const GBDistance radius);
// control
	void AddObjectNew(GBObject * newObject);
	void AddObjectDirectly(GBObject * newObject);
	void Resize(const GBFinePoint & newsize);
// dimensions accessors
	GBFinePoint Size() const;
	GBCoordinate Left() const;
	GBCoordinate Top() const;
	GBCoordinate Right() const;
	GBCoordinate Bottom() const;
	long BackgroundTilesX() const;
	long BackgroundTilesY() const;
	long ForegroundTilesX() const;
	long ForegroundTilesY() const;
// other accessors
	GBObject * ObjectNear(const GBPosition where, bool hitSensors) const;
	GBObject * GetObjects(const long tilex, const long tiley, const GBObjectClass cl) const;
	GBObject * GetLargeObjects(const GBObjectClass cl) const;
	long CountObjects(const GBObjectClass cl) const;
	GBObject * RandomInterestingObject() const;
	GBObject * RandomInterestingObjectNear(const GBPosition where, GBDistance radius) const;
};

#endif
