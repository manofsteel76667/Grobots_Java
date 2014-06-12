package simulation;

import support.FinePoint;
import support.GBColor;
import support.GBObjectClass;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBGenericError;

public class GBSyphon extends GBTimedShot {
	GBRobot sink;
	GBSyphonState creator;
	boolean hitsEnemies;

	// public:
	public GBSyphon(FinePoint where, double rate, GBRobot who,
			GBSyphonState state, boolean newHitsEnemies) {
		super(where, kSyphonRadius, where.subtract(who.Position()),
				who.Owner(), rate, 1);
		sink = who;
		creator = state;
		hitsEnemies = newHitsEnemies;
	}

	@Override
	public void Move() {
		// don't move! velocity is only apparent.
	}

	@Override
	public void CollideWith(GBObject other) throws GBAbort, GBGenericError,
			GBBadArgumentError {
		if (other.Class() == GBObjectClass.ocRobot && power != 0
				&& other != sink
				&& (hitsEnemies || other.Owner() == sink.Owner())) {
			if (power >= 0) {
				try {
					GBRobot otherBot = (GBRobot) other;
					// FIXME shields affect syphons twice: here and when they're
					// fired
					double efficiency = sink.ShieldFraction()
							* otherBot.ShieldFraction();
					if (other.Owner() != sink.Owner()) {
						if (otherBot.hardware.MaxEnergy() != 0)
							efficiency *= Math.min(1, otherBot.hardware
									.Energy()
									/ otherBot.hardware.MaxEnergy());
					}
					double maxTransfer = Math.min(sink.MaxGiveEnergy(), other
							.MaxTakeEnergy());
					double actual = Math.min(maxTransfer, power * efficiency);

					if (actual > 0) {
						double taken = other.TakeEnergy(actual);
						double given = sink.GiveEnergy(actual);
						power = Math.min(power - actual / efficiency, 0);
						creator.ReportUse(taken);
						if (taken > 0 && other.Owner() != owner) {
							owner.ReportKleptotrophy(taken);
							other.Owner().Scores().expenditure
									.ReportStolen(taken);
						}
						if (taken != given || taken != actual) {
							throw new GBGenericError(
									"Given != taken in CollideWith");
						}
					}
				} catch (GBError e) {
					GBError.NonfatalError("Error in CollideWith: "
							+ e.getMessage());
				}
			} else { // giving energy: like taking, but target energy isn't a
						// factor
				GBRobot otherBot = (GBRobot) other;
				double efficiency = sink.ShieldFraction()
						* otherBot.ShieldFraction();
				// efficiency does not decrease with low energy when giving
				double maxTransfer = Math.min(other.MaxGiveEnergy(), sink
						.MaxTakeEnergy());
				double actual = Math.min(maxTransfer, -power * efficiency);

				double taken = sink.TakeEnergy(actual);
				double given = other.GiveEnergy(actual);
				power = Math.max(power + actual / efficiency, 0);

				creator.ReportUse(-given);

				if (given > 0 && other.Owner() != owner) { // giving energy to
															// enemy :)
					other.Owner().ReportKleptotrophy(given);
					owner.Scores().expenditure.ReportStolen(given);
				}
				if (taken != given || taken != actual) {
					throw new GBGenericError("Given != taken in CollideWith");
				}
			}
		}
	}

	@Override
	public int Type() {
		return hitsEnemies ? 4 : 3;
	}

	@Override
	public double Interest() {
		return Math.abs(power) * (hitsEnemies ? 5 : 1) + (hitsEnemies ? 2 : 1);
	}

	// TODO: after GUI is done

	@Override
	public GBColor Color() {
		return (hitsEnemies ? new GBColor(0.6f, 1, 0) : new GBColor(0.5f, 0.8f,
				1));
	}

	/*
	 * public void Draw(GBGraphics & g, GBProjection & proj, GBRect & where,
	 * boolean detailed) { //draw tail showing origin, rate and whether it's
	 * working if ( detailed ) { GBColor tailcolor = Owner().Color() *
	 * (creator.Syphoned() ? 0.8f : 0.4f); FinePoint unit = - Velocity().Unit();
	 * double phase = Milliseconds() / 1000.0 * ToDouble(creator.Rate()); for
	 * (double d = Speed() + (phase -Math.floor(phase)) - 1 - sink.Radius(); d
	 * >= Radius(); d -= 1 ) { short x = proj.ToScreenX(Position().x + unit.x *
	 * d); short y = proj.ToScreenY(Position().y + unit.y * d); GBRect r(x - 1,
	 * y - 1, x + 1, y + 1); g.DrawSolidRect(r, tailcolor); } } //draw the
	 * syphon g.DrawLine(where.left, where.top, where.right, where.bottom,
	 * Color()); g.DrawLine(where.right, where.top, where.left, where.bottom,
	 * Color()); }
	 */

}