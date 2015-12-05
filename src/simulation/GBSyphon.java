/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import Rendering.GBProjection;
import support.FinePoint;
import support.GBColor;
import exception.GBSimulationError;

public class GBSyphon extends GBTimedShot {
	GBRobot sink;
	GBSyphonState creator;
	boolean hitsEnemies;

	// public:
	public GBSyphon(FinePoint where, double rate, GBRobot who,
			GBSyphonState state, boolean newHitsEnemies) {
		super(where, kSyphonRadius, where.subtract(who.getPosition()),
				who.getOwner(), rate, 1);
		sink = who;
		creator = state;
		hitsEnemies = newHitsEnemies;
	}

	@Override
	public void move() {
		// don't move! velocity is only apparent.
	}

	@Override
	public void collideWith(GBObject other) {
		if (other.getObjectClass() == GBObjectClass.ocRobot && power != 0
				&& other != sink
				&& (hitsEnemies || other.getOwner() == sink.getOwner())) {
			if (power >= 0) {
				try {
					GBRobot otherBot = (GBRobot) other;
					// FIXME shields affect syphons twice: here and when they're
					// fired
					double efficiency = sink.getShieldFraction()
							* otherBot.getShieldFraction();
					if (other.getOwner() != sink.getOwner()) {
						if (otherBot.hardware.getMaxEnergy() != 0)
							efficiency *= Math.min(1,
									otherBot.hardware.getEnergy()
											/ otherBot.hardware.getMaxEnergy());
					}
					double maxTransfer = Math.min(sink.maxGiveEnergy(),
							other.maxTakeEnergy());
					double actual = Math.min(maxTransfer, power * efficiency);

					if (actual > 0) {
						double taken = other.takeEnergy(actual);
						double given = sink.giveEnergy(actual);
						power = Math.min(power - actual / efficiency, 0);
						creator.reportUse(taken);
						if (taken > 0 && other.getOwner() != owner) {
							owner.reportKleptotrophy(taken);
							other.getOwner().getScores().expenditure
									.reportStolen(taken);
						}
						if (taken != given || taken != actual) {
							throw new GBSimulationError(
									"Given != taken in CollideWith");
						}
					}
				} catch (Exception e) {
					throw new GBSimulationError("Error in CollideWith: "
							+ e.getMessage());
				}
			} else { // giving energy: like taking, but target energy isn't a
						// factor
				GBRobot otherBot = (GBRobot) other;
				double efficiency = sink.getShieldFraction()
						* otherBot.getShieldFraction();
				// efficiency does not decrease with low energy when giving
				double maxTransfer = Math.min(other.maxGiveEnergy(),
						sink.maxTakeEnergy());
				double actual = Math.min(maxTransfer, -power * efficiency);

				double taken = sink.takeEnergy(actual);
				double given = other.giveEnergy(actual);
				power = Math.max(power + actual / efficiency, 0);

				creator.reportUse(-given);

				if (given > 0 && other.getOwner() != owner) { // giving energy to
															// enemy :)
					other.getOwner().reportKleptotrophy(given);
					owner.getScores().expenditure.reportStolen(given);
				}
				if (taken != given || taken != actual) {
					throw new GBSimulationError("Given != taken in CollideWith");
				}
			}
		}
	}

	@Override
	public int getShotType() {
		return hitsEnemies ? 4 : 3;
	}

	@Override
	public double getInterest() {
		return Math.abs(power) * (hitsEnemies ? 5 : 1) + (hitsEnemies ? 2 : 1);
	}

	@Override
	public Color getColor() {
		return (hitsEnemies ? new Color(0.6f, 1, 0) : new Color(0.5f, 0.8f, 1));
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setPaint(getColor());
		// draw tail showing origin, rate and whether it's working
		// Dashed line whose dashes move in the direction of energy
		// transfer over time
		if (detailed) {
			Color tailcolor = GBColor.multiply(getOwner().getColor(),
					creator.getSyphoned() != 0 ? 0.8f : 0.4f);
			FinePoint unit = getVelocity().unit().multiply(-1);
			double phase = System.currentTimeMillis() / 1000.0 * creator.getRate();
			for (double d = getSpeed() + (phase - Math.floor(phase)) - 1
					- sink.getRadius(); d >= getRadius(); d -= 1) {
				int x = proj.toScreenX(getPosition().x + unit.x * d);
				int y = proj.toScreenY(getPosition().y + unit.y * d);
				g2d.setPaint(tailcolor);
				g2d.fillRect(x - 1, y - 1, 2, 2);
			}
		}
		// draw the syphon as a dashed X on the target
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 1, 2 }, 0));
		g2d.setColor(getColor());
		g2d.drawLine(where.x, where.y, where.x + where.width, where.y
				+ where.height);
		g2d.drawLine(where.x + where.width, where.y, where.x, where.y
				+ where.height);
	}

}
