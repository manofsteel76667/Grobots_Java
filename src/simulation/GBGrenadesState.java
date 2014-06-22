package simulation;

import sides.GrenadesSpec;
import support.GBMath;
import exception.GBNilPointerError;

public class GBGrenadesState {
	GrenadesSpec spec;
	// state
	int cooldown;
	// orders
	boolean firing;
	double direction;
	double distance;

	// public:

	public GBGrenadesState(GrenadesSpec spc) {
		spec = spc;
		cooldown = spc.ReloadTime();
	}

	public int ReloadTime() {
		return spec.ReloadTime();
	}

	public double Speed() {
		return spec.Speed();
	}

	public int MaxLifetime() {
		return spec.Lifetime();
	}

	public double MaxRange() {
		return spec.Range();
	}

	public double Damage() {
		return spec.Damage();
	}

	public double FiringCost() {
		return spec.FiringCost();
	}

	public int Cooldown() {
		return cooldown;
	}

	public boolean Firing() {
		return firing;
	}

	public double Direction() {
		return direction;
	}

	public double Distance() {
		return distance;
	}

	public double ExplosionRadius() {
		return GBExplosion.PowerRadius(Damage());
	}

	public void Fire(double dist, double dir) {
		if (cooldown == 0 && Damage() > 0) {
			cooldown = ReloadTime();
			firing = true;
			direction = dir;
			distance = GBMath.clamp(dist, Speed(), MaxRange());
		}
	}

	public void Act(GBRobot robot, GBWorld world) throws GBNilPointerError {
		if (firing) {
			double effectiveness = robot.hardware.EffectivenessFraction();
			if (robot.hardware.ActualShield() == 0
					&& robot.hardware.UseEnergy(FiringCost() * effectiveness)) {
				robot.Owner().Scores().expenditure.ReportWeapons(FiringCost()
						* effectiveness);
				// TODO: put back in when sound is implemented
				// StartSound(siGrenade);
				int lifetime = (int) Math.max(
						Math.floor((distance - robot.Radius()) / Speed()), 1);
				GBObject shot = new GBGrenade(robot.Position().addPolar(
						robot.Radius(), direction), robot.Velocity().addPolar(
						Speed(), direction), robot.Owner(), Damage()
						* effectiveness, lifetime);
				world.AddObjectNew(shot);
				for (double en = FiringCost() * effectiveness; en >= GBHardwareState.kGrenadesFiringCostPerSmoke; en -= GBHardwareState.kGrenadesFiringCostPerSmoke) {
					GBObject smoke = new GBSmoke(robot.Position().addPolar(
							robot.Radius(), direction), world.Randoms().Vector(
							GBTimedDecoration.kSmokeMaxSpeed), world.Randoms()
							.intInRange(GBTimedDecoration.kSmokeMinLifetime,
									GBTimedDecoration.kSmokeMaxLifetime));
					world.AddObjectNew(smoke);
				}
				cooldown = ReloadTime();
				// recoil
				// robot.PushBy(- spec.Recoil() * effectiveness, direction);
			}
			firing = false;
		}
		if (cooldown > 0)
			cooldown--;
	}
}