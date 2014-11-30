/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import exception.GBBrainError;
import sides.SensorSpec;
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;

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
		seesFriendly = spc.Seen() == GBObjectClass.ocFood.value;
		seesEnemy = true;
		time = -1;
		whereOverall = new FinePoint();

		results = new GBSensorResult[MaxResults()];
	}

	public double MaxRange() {
		return spec.Range();
	}

	// FIXME: calling SensorSpec.Set() on a spec that has a state will cause
	// pointer
	// problems, since maxResults is not cached locally.
	public int MaxResults() {
		return spec.NumResults();
	}

	public double FiringCost() {
		return spec.FiringCost();
	}

	public GBObjectClass Seen() {
		return GBObjectClass.byValue(spec.Seen());
	}

	public boolean Firing() {
		return firing;
	}

	public double Distance() {
		return distance;
	}

	public double Direction() {
		return direction;
	}

	public boolean SeesFriendly() {
		return seesFriendly;
	}

	public boolean SeesEnemy() {
		return seesEnemy;
	}

	public int Time() {
		return time;
	}

	public int Found() {
		return found;
	}

	// returns false if wraparound occured
	public boolean NextResult() {
		currentResult++;
		if (currentResult < NumResults())
			return true;
		else {
			currentResult = 0;
			return false;
		}
	}

	public int NumResults() {
		return found <= MaxResults() ? found : MaxResults(); // min
	}

	public int CurrentResult() {
		return currentResult;
	}

	public void SetCurrentResult(int newCurrent) {
		if (newCurrent < 0 || newCurrent >= NumResults())
			throw new GBBrainError("sensor result out of bounds");
		currentResult = newCurrent;
	}

	public FinePoint WhereFound() {
		if (currentResult < NumResults())
			return results[currentResult].where;
		else
			return new FinePoint();
	}

	public FinePoint Velocity() {
		if (currentResult < NumResults())
			return results[currentResult].vel;
		else
			return new FinePoint();
	}

	public int Side() {
		if (currentResult < NumResults())
			if (results[currentResult].side == null)
				return 0;
			else
				return results[currentResult].side.ID();
		else
			return 0;
	}

	public double Radius() {
		if (currentResult < NumResults())
			return results[currentResult].radius;
		else
			return 0;
	}

	public double Mass() {
		if (currentResult < NumResults())
			return results[currentResult].mass;
		else
			return 0;
	}

	public double Energy() {
		if (currentResult < NumResults())
			return results[currentResult].energy;
		else
			return 0;
	}

	public int Type() {
		if (currentResult < NumResults())
			return results[currentResult].type;
		else
			return 0;
	}

	public int ID() {
		if (currentResult < NumResults()) {
			return results[currentResult].ID;
		} else
			return 0;
	}

	public double ShieldFraction() {
		if (currentResult < NumResults())
			return results[currentResult].shieldFraction;
		else
			return 1;
	}

	public double Bomb() {
		if (currentResult < NumResults())
			return results[currentResult].bomb;
		else
			return 0;
	}

	public boolean Reloading() {
		return currentResult < NumResults() && results[currentResult].reloading;
	}

	public double Flag() {
		if (currentResult < NumResults())
			return results[currentResult].flag;
		else
			return 0;
	}

	public FinePoint WhereOverall() {
		if (found != 0)
			return whereOverall.divide(found);
		else
			return new FinePoint(0, 0);
	}

	public void SetDistance(double dist) {
		distance = dist;
	}

	public void SetDirection(double dir) {
		direction = dir;
	}

	public void SetSeesFriendly(boolean value) {
		seesFriendly = value;
	}

	public void SetSeesEnemy(boolean value) {
		seesEnemy = value;
	}

	public void Fire() {
		firing = true;
	}

	public void Report(GBSensorResult find) {
		// check for same robot is in SensorShot::CollideWith(). Check for wrong
		// type of object is in GBWorld.
		if (!((find.side == owner) ? seesFriendly : seesEnemy))
			return;
		GBSensorResult temp, current = find;
		// insert find in results
		for (int i = 0; i < MaxResults(); i++) {
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

	public void Act(GBRobot robot, GBWorld world) {
		if (firing && spec.Seen() != GBObjectClass.ocDead.value /*
																 * if sensor
																 * exists
																 */) {
			if (!robot.dead && robot.hardware.UseEnergy(FiringCost())) {
				robot.Owner().Scores().expenditure.ReportSensors(FiringCost());
				world.addObjectLater(new GBSensorShot(robot.Position().addPolar(
						distance, direction), robot, this));
			}
			found = 0;
			currentResult = 0;
			whereOverall.set(0, 0);
			time = world.currentFrame;
			owner = robot.Owner();
		}
		firing = false;
	}

}
