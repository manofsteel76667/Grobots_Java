/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

import sides.Side;
import support.FinePoint;
import support.GBColor;

public class GBForceField extends GBShot {
	boolean dead;
	double direction;

	// public:
	public GBForceField(FinePoint where, FinePoint vel, Side who, double pwr,
			double dir) {
		super(where, PowerRadius(pwr), vel, who, pwr);
		direction = dir;
	}

	@Override
	public GBObjectClass Class() {
		return dead ? GBObjectClass.ocDead : GBObjectClass.ocArea;
	}

	@Override
	public void Move() {
		// don't move! velocity is only apparent.
	}

	@Override
	public void CollideWith(GBObject other) {
		double force = power / Math.max(other.Speed(), kMinEffectiveSpeed)
				* OverlapFraction(other) * Math.sqrt(other.Mass())
				* kForceFieldPushRatio;
		other.PushBy(force, direction);
	}

	@Override
	public void Act(GBWorld world) {
		super.Act(world);
		dead = true;
	}

	@Override
	public int Type() {
		return 5;
	}

	@Override
	public double Interest() {
		return Math.abs(power) * 15 + 1;
	}

	@Override
	public Color Color() {
		return new Color(0, 0.8f, 1);
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		if (where.width <= kMaxSquareMiniSize) {
			DrawMini(g, where);
			return;
		}
		int weight = where.getWidth() >= 20 ? 2 : 1;
		FinePoint edge = Position().subtract(
				Velocity().unit().multiply(Radius()));
		// From robot to target
		g2d.setStroke(new BasicStroke(weight));
		g2d.setColor(GBColor.multiply(Color(), 0.5f));
		g2d.drawLine(proj.toScreenX(edge.x), proj.toScreenY(edge.y),
				proj.toScreenX(Position().x - Velocity().x),
				proj.toScreenY(Position().y - Velocity().y));
		int cx = where.x + where.width / 2;
		int cy = where.y + where.height / 2;
		// From destination into the direction being pushed
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(owner != null ? owner.Color() : Color());
		g2d.drawLine(
				cx,
				cy,
				cx
						+ (int) Math.round(Math.cos(direction)
								* where.getWidth() / 2), (cy - (int) Math
						.round((Math.sin(direction) * where.getHeight() / 2))));
		// Force field radius
		g2d.setColor(Color());
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}

	public void DrawMini(Graphics g, Rectangle where) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color());
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}

	public static final double PowerRadius(double pwr) {
		return Math.pow(Math.abs(pwr), kForceFieldRadiusExponent)
				* kForceFieldRadiusRatio;
	}
}
