package simulation;

import java.awt.*;

import support.FinePoint;
import support.GBColor;
import support.GBGraphics;
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
							efficiency *= Math.min(1,
									otherBot.hardware.Energy()
											/ otherBot.hardware.MaxEnergy());
					}
					double maxTransfer = Math.min(sink.MaxGiveEnergy(),
							other.MaxTakeEnergy());
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
				double maxTransfer = Math.min(other.MaxGiveEnergy(),
						sink.MaxTakeEnergy());
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

	@Override
	public GBColor Color() {
		return (hitsEnemies ? new GBColor(0.6f, 1, 0) : new GBColor(0.5f, 0.8f,
				1));
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setPaint(Color());
		// draw tail showing origin, rate and whether it's working
		//Dashed line whose dashes move in the direction of energy
		//transfer over time
		if (detailed) {
			GBColor tailcolor = Owner().Color().multiply(
					creator.Syphoned() != 0 ? 0.8f : 0.4f);
			FinePoint unit = Velocity().unit().multiply(-1);
			double phase = System.currentTimeMillis() / 1000.0 * creator.Rate();
			for (double d = Speed() + (phase - Math.floor(phase)) - 1
					- sink.Radius(); d >= Radius(); d -= 1) {
				int x = proj.ToScreenX(Position().x + unit.x * d);
				int y = proj.ToScreenY(Position().y + unit.y * d);
				g2d.setPaint(tailcolor);
				g2d.fillRect(x-1, y-1, 2, 2);
			}
		}
		// draw the syphon as a dashed X on the target
		g2d.setStroke(GBGraphics.dashedStroke(10));
		g2d.setColor(Color());
		g2d.drawLine(where.x, where.y, where.x + where.width, where.y
				+ where.height);
		g2d.drawLine(where.x + where.width, where.y, where.x, where.y
				+ where.height);
	}

}