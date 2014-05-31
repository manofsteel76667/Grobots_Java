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
		where = new FinePoint(obj.Position());
		vel = new FinePoint(obj.Velocity());
		dist = dis;
		side = obj.Owner();
		radius = obj.Radius();
		mass = obj.Mass();
		energy = obj.Energy();
		shieldFraction = 1;
		if (obj instanceof GBRobot) {
			GBRobot rob = (GBRobot) obj;
			type = rob.Type().ID();
			ID = rob.ID();
			shieldFraction = rob.ShieldFraction();
			bomb = rob.hardware.Bomb();
			reloading = rob.hardware.blaster.Cooldown() > 0
					|| rob.hardware.grenades.Cooldown() > 0;
			flag = rob.flag;
			return;
		}
		if (obj instanceof GBShot) {
			GBShot shot = (GBShot) obj;
			type = shot.Type();
			energy = shot.Power();
			return;
		}
	}

}