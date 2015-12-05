/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

// GBWorld.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

/*/*#if MAC && ! HEADLESS
 #define GBWORLD_PROFILING 1
 #else
 #define GBWORLD_PROFILING 0
 #endif*/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBRandomState;
import exception.GBError;
import exception.GBSimulationError;

public class GBWorld extends GBObjectWorld {
	public static final double kRandomMinWallDistance = 2;
	List<Side> sides;
	public int currentFrame;
	public int sidesSeeded;
	public GBRandomState random;
	double mannaLeft;

	public boolean reportErrors, reportPrints;

	GBGame game; // For brain primitives like pause and timeLimit

	// simulation parameters
	public double mannaSize;
	public double mannaDensity;
	public double mannaRate;
	public double seedValue;
	public double seedTypePenalty;

	public static final double kDefaultMannaDensity = 150; // energy / tile
	public static final double kDefaultMannaRate = 0.25; // energy / tile /
															// frame
	public static final double kDefaultMannaSize = 400;

	public static final int kDefaultTimeLimit = 18000;

	public static final double kSeedRadius = 2;
	public static final double kDefaultSeedValue = 5035; // Was 5000 but some
															// giants won't seed
															// without redesign
	public static final double kDefaultSeedTypePenalty = 100;

	public GBWorld(GBGame _game) {
		game = _game;
		sides = new ArrayList<Side>();
		random = new GBRandomState();
		reportErrors = true;
		mannaSize = kDefaultMannaSize;
		mannaDensity = kDefaultMannaDensity;
		mannaRate = kDefaultMannaRate;
		seedValue = kDefaultSeedValue;
		seedTypePenalty = kDefaultSeedTypePenalty;
		addInitialManna();
	}

	void thinkAllObjects() {
		// only bothers with robots
		try {
			for (int i = 0; i <= tilesX * tilesY; i++)
				for (GBObject ob = objects.get(GBObjectClass.ocRobot)[i]; ob != null; ob = ob.next)
					ob.think(this);
		} catch (Exception err) {
			GBError.NonfatalError("Error thinking objects: " + err.getMessage());
		}
	}

	void actAllObjects() {
		try {
			for (int i = 0; i <= tilesX * tilesY; i++)
				for (GBObjectClass cur : GBObjectClass.values())
					for (GBObject ob = objects.get(cur)[i]; ob != null; ob = ob.next)
						ob.act(this);
		} catch (Exception e) {
			GBError.NonfatalError("Error acting objects: " + e.getMessage());
		}
	}

	void addManna() {
		for (mannaLeft += size.x * size.y * mannaRate
				/ (kForegroundTileSize * kForegroundTileSize); mannaLeft > mannaSize; mannaLeft -= mannaSize)
			addObjectLater(new GBManna(getRandomLocation(0), mannaSize));
	}

	void addInitialManna() {
		double amount = size.x * size.y
				/ (kForegroundTileSize * kForegroundTileSize) * mannaDensity;
		double placed;
		for (; amount > 0; amount -= placed) {
			placed = amount > mannaSize ? random.InRange(mannaSize / 10,
					mannaSize) : amount;
			addObjectImmediate(new GBManna(getRandomLocation(0), placed));
		}
		// addNewObjects();
	}

	public void simulateOneFrame() {
		addManna();
		thinkAllObjects();
		moveAllObjects();
		actAllObjects();
		resortObjects();
		collideAllObjects();
		currentFrame++;
	}

	public void addSeed(Side side, FinePoint where) {
		try {
			double cost = seedValue - seedTypePenalty * side.getTypeCount();
			// give side a number
			if (side.getID() == 0)
				side.setID(++sidesSeeded);
			// add cells
			RobotType type;
			GBRobot bot = null;
			List<GBRobot> placed = new ArrayList<GBRobot>();
			int lastPlaced = -1; // last value of i for last successful place
			for (int i = 0;; i++) {
				type = side.getSeedType(i);
				if (type == null)
					throw new GBSimulationError(
							"must have at least one type to seed");
				if (type.getCost() <= cost) {
					bot = new GBRobot(type, where.add(random
							.Vector(kSeedRadius)));
					addObjectImmediate(bot);
					side.getScores().reportSeeded(type.getCost());
					cost -= type.getCost();
					lastPlaced = i;
					placed.add(bot);
				} else
					break; // if unseedable, stop - this one will be a fetus
			}
			// give excess energy as construction
			int placedIndex;
			for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
				GBRobot placee = placed.get(placedIndex);
				if (cost == 0)
					break;
				if (placee.hardware.constructor.getMaxRate() != 0) {
					double amt = Math.min(cost, side
							.getSeedType(lastPlaced + 1).getCost());
					placee.hardware.constructor.start(
							side.getSeedType(lastPlaced + 1), amt);
					side.getScores().reportSeeded(amt);
					cost -= amt;
					if (cost > 0)
						throw new GBSimulationError(
								"When seeding, energy left-over after bonus fetus");
				}
			}
			// energy still left (implies constructor-less side!); try giving as
			// energy
			for (placedIndex = 0; placedIndex < placed.size(); placedIndex++) {
				if (cost == 0)
					break;
				double amt = placed.get(placedIndex).hardware.giveEnergy(cost);
				side.getScores().reportSeeded(amt);
				cost -= amt;
			}
			// all else fails, make a manna.
			if (cost > 0)
				addObjectImmediate(new GBManna(where, cost));
			// addNewObjects();
		} catch (Exception e) {
			throw new GBSimulationError("Error adding seed:" + e.getMessage());
		}
	}

	void pickSeedPositions(FinePoint[] positions, int numSeeds) {
		if (numSeeds < 1)
			return;
		try {
			double wallDist = kSeedRadius + Math.min(size.x, size.y) / 20;
			double separation = Math.sqrt((size.x - wallDist * 2)
					* (size.y - wallDist * 2) / numSeeds);
			int iterations = 0;
			int iterLimit = 100 + 30 * numSeeds + numSeeds * numSeeds;
			boolean inRange;
			// pick positions
			for (int i = 0; i < numSeeds; i++) {
				do {
					inRange = false;
					positions[i] = getRandomLocation(wallDist);
					// TODO in small worlds, this leaves too much space in
					// center
					if (positions[i].inRange(getSize().divide(2), separation
							- separation * iterations * 2 / iterLimit))
						inRange = true;
					else
						for (int j = 0; j < i; j++)
							if (positions[i].inRange(positions[j], separation
									- separation * iterations / iterLimit)) {
								inRange = true;
								break;
							}
					if (++iterations > iterLimit)
						throw new GBSimulationError(
								"Too many iterations picking seed positions");
				} while (inRange);
			}
			if (reportErrors && iterations > iterLimit / 2)
				throw new GBSimulationError("Warning: seed placement took "
						+ iterations + " iterations");
			// shuffle positions
			// the above algorithm is not uniform, in that the first element may
			// have different typical location than the last
			// to fix this, permute randomly (Knuth Vol 2 page 125)
			for (int j = numSeeds - 1; j > 0; j--) { // iteration with j==0 is
														// nop, so skip it
				int i = random.intInRange(0, j);
				FinePoint temp = positions[i];
				positions[i] = positions[j];
				positions[j] = temp;
			}
		} catch (GBSimulationError err) {
			if (reportErrors)
				GBError.NonfatalError("Warning: PickSeedPositions failsafe used.");
			for (int i = 0; i < numSeeds; ++i)
				positions[i] = getRandomLocation(0);
		}
	}

	public void addSeeds() {
		// pick positions
		FinePoint[] positions = new FinePoint[sides.size()];
		pickSeedPositions(positions, sides.size());
		// seed sides
		for (int i = 0; i < sides.size(); i++) {
			sides.get(i).center = positions[i];
			addSeed(sides.get(i), positions[i]);
		}
	}

	public void reset() {
		currentFrame = 0;
		mannaLeft = 0;
		sidesSeeded = 0;
		sides.clear();
		clearLists();
		addInitialManna();
	}

	@Override
	public void resize(FinePoint newsize) {
		if (newsize == size)
			return;
		super.resize(newsize);
		reset();
	}

	FinePoint getRandomLocation(double walldist) {
		return new FinePoint(random.InRange(walldist, size.x - walldist),
				random.InRange(walldist, size.y - walldist));
	}

	public void addSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to add null side");
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getName().equals(side.getName()))
				side.setName(side.getName() + '\'');
		sides.add(side);
	}

	public void replaceSide(Side oldSide, Side newSide) {
		if (oldSide == null || newSide == null)
			throw new NullPointerException("replacing null side");
		int pos = sides.indexOf(oldSide);
		sides.remove(oldSide);
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getName().equals(newSide.getName()))
				newSide.setName(newSide.getName() + '\'');
		sides.add(pos, newSide);
		clearSideObjects(oldSide);
		resortObjects();
	}

	public void removeSide(Side side) {
		if (side == null)
			throw new NullPointerException("tried to remove null side");
		sides.remove(side);
		clearSideObjects(side);
		resortObjects();
	}

	public void removeAllSides() {
		sides.clear();
		allObjects.clear();
	}

	void clearSideObjects(Side side) {
		if (side == null)
			throw new NullPointerException("can't clear objects for null side");
		synchronized (allObjects) {
			Iterator<GBObject> it = allObjects.iterator();
			while (it.hasNext())
				if (side.equals(it.next().getOwner()))
					it.remove();
		}
	}

	public Side getSide(int index) {
		if (index <= 0 || index > sides.size())
			throw new IndexOutOfBoundsException("invalid side index: " + index);
		return sides.get(index - 1);
	}

	public int getSideCount() {
		return sides.size();
	}

	public int getSidesAlive() {
		int sidesAlive = 0;
		for (int i = 0; i < sides.size(); ++i)
			if (sides.get(i).getScores().getPopulation() > 0)
				sidesAlive++;
		return sidesAlive;
	}

	public void pause() {
		// for the pause primitive
		game.running = false;
	}

	public int getTimeLimit() {
		// for the time-limit primitive
		return game.timeLimit;
	}
}
