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
import sides.Side;
import support.FinePoint;
import support.GBColor;

public class GBForceField extends GBShot {
	boolean dead;
	double direction;

	// public:
	public GBForceField(FinePoint where, FinePoint vel, Side who, double pwr,
			double dir) {
		super(where, getPowerRadius(pwr), vel, who, pwr);
		direction = dir;
	}

	@Override
	public GBObjectClass getObjectClass() {
		return dead ? GBObjectClass.ocDead : GBObjectClass.ocArea;
	}

	@Override
	public void move() {
		// don't move! velocity is only apparent.
	}

	@Override
	public void collideWith(GBObject other) {
		double force = power / Math.max(other.getSpeed(), kMinEffectiveSpeed)
				* overlapFraction(other) * Math.sqrt(other.getMass())
				* kForceFieldPushRatio;
		other.pushBy(force, direction);
	}

	@Override
	public void act(GBWorld world) {
		super.act(world);
		dead = true;
	}

	@Override
	public int getShotType() {
		return 5;
	}

	@Override
	public double getInterest() {
		return Math.abs(power) * 15 + 1;
	}

	@Override
	public Color getColor() {
		return new Color(0, 0.8f, 1);
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		if (where.width <= kMaxSquareMiniSize) {
			g2d.setColor(getColor());
			g2d.drawOval(where.x, where.y, where.width, where.height);
			return;
		}
		int weight = where.getWidth() >= 20 ? 2 : 1;
		FinePoint edge = getPosition().subtract(
				getVelocity().unit().multiply(getRadius()));
		// From robot to target
		g2d.setStroke(new BasicStroke(weight));
		g2d.setColor(GBColor.multiply(getColor(), 0.5f));
		g2d.drawLine(proj.toScreenX(edge.x), proj.toScreenY(edge.y),
				proj.toScreenX(getPosition().x - getVelocity().x),
				proj.toScreenY(getPosition().y - getVelocity().y));
		int cx = where.x + where.width / 2;
		int cy = where.y + where.height / 2;
		// From destination into the direction being pushed
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(owner != null ? owner.getColor() : getColor());
		g2d.drawLine(
				cx,
				cy,
				cx
						+ (int) Math.round(Math.cos(direction)
								* where.getWidth() / 2), (cy - (int) Math
						.round((Math.sin(direction) * where.getHeight() / 2))));
		// Force field radius
		g2d.setColor(getColor());
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}

	public static final double getPowerRadius(double pwr) {
		return Math.pow(Math.abs(pwr), kForceFieldRadiusExponent)
				* kForceFieldRadiusRatio;
	}
}
