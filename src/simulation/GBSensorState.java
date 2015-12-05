/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.SensorSpec;
import sides.Side;
import support.FinePoint;
import exception.GBBrainError;

/**
 * GBSensorState is public since it gets used directly by GBBrain in
 * fireperiodic
 * 
 * @author Anderson
 * 
 */
public class GBSensorState {
	SensorSpec spec;
	Side owner;
	// orders
	boolean firing;
	double distance;
	double direction;
	boolean seesFriendly, seesEnemy;
	// results
	int time; // when fired
	int found, currentResult;
	GBSensorResult[] results; // dynamically allocated array
	FinePoint whereOverall;

	public GBSensorState(SensorSpec spc) {
		spec = spc;
		seesFriendly = spc.getSeen() == GBObjectClass.ocFood.value;
		seesEnemy = true;
		time = -1;
		whereOverall = new FinePoint();

		results = new GBSensorResult[getMaxResults()];
	}

	public double getMaxRange() {
		return spec.getRange();
	}

	// FIXME: calling SensorSpec.Set() on a spec that has a state will cause
	// pointer
	// problems, since maxResults is not cached locally.
	public int getMaxResults() {
		return spec.getNumResults();
	}

	public double getFiringCost() {
		return spec.getFiringCost();
	}

	public GBObjectClass getSeen() {
		return GBObjectClass.byValue(spec.getSeen());
	}

	public boolean getFiring() {
		return firing;
	}

	public double getDistance() {
		return distance;
	}

	public double getDirection() {
		return direction;
	}

	public boolean getSeesFriendly() {
		return seesFriendly;
	}

	public boolean getSeesEnemy() {
		return seesEnemy;
	}

	public int getTime() {
		return time;
	}

	public int getFound() {
		return found;
	}

	// returns false if wraparound occured
	public boolean getNextResult() {
		currentResult++;
		if (currentResult < getNumResults())
			return true;
		else {
			currentResult = 0;
			return false;
		}
	}

	public int getNumResults() {
		return found <= getMaxResults() ? found : getMaxResults(); // min
	}

	public int getCurrentResult() {
		return currentResult;
	}

	public void getSetCurrentResult(int newCurrent) {
		if (newCurrent < 0 || newCurrent >= getNumResults())
			throw new GBBrainError("sensor result out of bounds");
		currentResult = newCurrent;
	}

	public FinePoint getWhereFound() {
		if (currentResult < getNumResults())
			return results[currentResult].where;
		else
			return new FinePoint();
	}

	public FinePoint getVelocity() {
		if (currentResult < getNumResults())
			return results[currentResult].vel;
		else
			return new FinePoint();
	}

	public int getSide() {
		if (currentResult < getNumResults())
			if (results[currentResult].side == null)
				return 0;
			else
				return results[currentResult].side.getID();
		else
			return 0;
	}

	public double getRadius() {
		if (currentResult < getNumResults())
			return results[currentResult].radius;
		else
			return 0;
	}

	public double getMass() {
		if (currentResult < getNumResults())
			return results[currentResult].mass;
		else
			return 0;
	}

	public double getEnergy() {
		if (currentResult < getNumResults())
			return results[currentResult].energy;
		else
			return 0;
	}

	public int getFoundType() {
		if (currentResult < getNumResults())
			return results[currentResult].type;
		else
			return 0;
	}

	public int getID() {
		if (currentResult < getNumResults()) {
			return results[currentResult].ID;
		} else
			return 0;
	}

	public double getShieldFraction() {
		if (currentResult < getNumResults())
			return results[currentResult].shieldFraction;
		else
			return 1;
	}

	public double getBomb() {
		if (currentResult < getNumResults())
			return results[currentResult].bomb;
		else
			return 0;
	}

	public boolean getReloading() {
		return currentResult < getNumResults() && results[currentResult].reloading;
	}

	public double getFlag() {
		if (currentResult < getNumResults())
			return results[currentResult].flag;
		else
			return 0;
	}

	public FinePoint getWhereOverall() {
		if (found != 0)
			return whereOverall.divide(found);
		else
			return new FinePoint(0, 0);
	}

	public void setDistance(double dist) {
		distance = dist;
	}

	public void setDirection(double dir) {
		direction = dir;
	}

	public void setSeesFriendly(boolean value) {
		seesFriendly = value;
	}

	public void setSeesEnemy(boolean value) {
		seesEnemy = value;
	}

	public void fire() {
		firing = true;
	}

	public void report(GBSensorResult find) {
		// check for same robot is in SensorShot::CollideWith(). Check for wrong
		// type of object is in GBWorld.
		if (!((find.side == owner) ? seesFriendly : seesEnemy))
			return;
		GBSensorResult temp, current = find;
		// insert find in results
		for (int i = 0; i < getMaxResults(); i++) {
			if (i >= found) { // beyond end of filled part of array
				results[i] = current;
				break;
			}
			if (current.dist < results[i].dist) {
				temp = results[i]; // swap current and results[i]
				results[i] = current;
				current = temp;
			} else {
			}
		}
		found++;
		whereOverall = whereOverall.add(find.where);
	}

	public void act(GBRobot robot, GBWorld world) {
		if (firing && spec.getSeen() != GBObjectClass.ocDead.value /*
																 * if sensor
																 * exists
																 */) {
			if (!robot.dead && robot.hardware.useEnergy(getFiringCost())) {
				robot.getOwner().getScores().expenditure.reportSensors(getFiringCost());
				world.addObjectLater(new GBSensorShot(robot.getPosition()
						.addPolar(distance, direction), robot, this));
			}
			found = 0;
			currentResult = 0;
			whereOverall.set(0, 0);
			time = world.currentFrame;
			owner = robot.getOwner();
		}
		firing = false;
	}

}
