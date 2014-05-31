package simulation;

import sides.ForceFieldSpec;
import support.FinePoint;
import support.GBMath;
import exception.GBNilPointerError;

public class GBForceFieldState {
	ForceFieldSpec spec;
	// orders
	double direction;
	double distance;
	double power;
	double angle;

	// public:

	public GBForceFieldState(ForceFieldSpec spc) {
		spec = spc;
	}

	public double MaxRange() {
		return spec.Range();
	}

	public double MaxPower() {
		return spec.Power();
	}

	public double Direction() {
		return direction;
	}

	public double Distance() {
		return distance;
	}

	public double Power() {
		return power;
	}

	public double Angle() {
		return angle;
	}

	public double Radius() {
		return GBForceField.PowerRadius(power);
	}

	public void SetDistance(double dist) {
		distance = GBMath.clamp(dist, 0, MaxRange());
	}

	public void SetDirection(double dir) {
		direction = dir;
	}

	public void SetPower(double pwr) {
		power = GBMath.clamp(pwr, 0, MaxPower());
	}

	public void SetAngle(double ang) {
		angle = ang;
	}

	public void Act(GBRobot robot, GBWorld world) throws GBNilPointerError {
		if (power == 0)
			return;
		double effective = power * robot.hardware.EffectivenessFraction()
				* robot.ShieldFraction();
		if (power > 0 && robot.hardware.UseEnergy(effective)) {
			robot.Owner().Scores().expenditure.ReportForceField(effective);
			FinePoint vel = FinePoint.makePolar(distance, direction);
			GBObject shot = new GBForceField(
					robot.Position().add(vel),
					vel,
					robot.Owner(),
					effective
							/ (distance
									* GBHardwareState.kForceFieldRangeAttenuation + 1),
					angle);
			world.AddObjectNew(shot);
			// robot.PushBy(- effective * kForceFieldRecoilPerPower, angle); //
			// recoil
		}
	}

}