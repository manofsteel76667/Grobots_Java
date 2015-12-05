/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import sides.Side;
import support.FinePoint;

public class GBSensorResult {
	// public:
	public FinePoint where;
	public FinePoint vel;
	public double dist;
	public Side side;
	public double radius;
	public double mass;
	public double energy;
	public double shieldFraction;
	public double bomb;
	public boolean reloading;
	public int type;
	public int ID;
	public double flag;

	public GBSensorResult copy(GBSensorResult other) {
		where = new FinePoint(other.where);
		vel = new FinePoint(other.vel);
		dist = other.dist;
		side = other.side;
		radius = other.radius;
		mass = other.mass;
		energy = other.energy;
		type = other.type;
		ID = other.ID;
		shieldFraction = other.shieldFraction;
		bomb = other.bomb;
		reloading = other.reloading;
		flag = other.flag;
		return this;
	}

	public GBSensorResult() {
		shieldFraction = 1;
	}

	GBSensorResult(GBObject obj, double dis) {
		where = new FinePoint(obj.getPosition());
		vel = new FinePoint(obj.getVelocity());
		dist = dis;
		side = obj.getOwner();
		radius = obj.getRadius();
		mass = obj.getMass();
		energy = obj.getEnergy();
		shieldFraction = 1;
		if (obj instanceof GBRobot) {
			GBRobot rob = (GBRobot) obj;
			type = rob.getRobotType().getID();
			ID = rob.getID();
			shieldFraction = rob.getShieldFraction();
			bomb = rob.hardware.getBomb();
			reloading = rob.hardware.blaster.getCooldown() > 0
					|| rob.hardware.grenades.getCooldown() > 0;
			flag = rob.flag;
			return;
		}
		if (obj instanceof GBShot) {
			GBShot shot = (GBShot) obj;
			type = shot.getShotType();
			energy = shot.getPower();
			return;
		}
	}

}