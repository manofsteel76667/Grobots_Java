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

import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBObjectClass;

public class GBBlast extends GBTimedShot {
	boolean hit;

	// public:
	public GBBlast(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, kBlastRadius, vel, who, howMuch, howLong);
	}

	@Override
	public void CollideWithWall() {
		lifetime = 0;
		hit = true;
	}

	@Override
	public void CollideWith(GBObject other) {
		if (!hit && other.Class() == GBObjectClass.ocRobot) {
			other.TakeDamage(power, owner);
			Push(other, power * kBlastPushRatio);
			other.PushBy(Velocity().multiply(power * kBlastMomentumPerPower));
			lifetime = 0;
			hit = true;
		}
	}

	@Override
	public void Act(GBWorld world) {
		super.Act(world);
		if (hit) {
			world.AddObjectNew(new GBBlasterSpark(Position()));
		}
	}

	@Override
	public int Type() {
		return 1;
	}

	// Blasts fade out at the end of their lives.
	// Big blasts are saturated, smaller ones are pink or white.
	// Long-range blasts are orangish; short-range ones are magenta.
	@Override
	public GBColor Color() {
		float fadeout = hit ? 1.0f : Math.min(
				lifetime / Math.min(originallifetime, 10.0f), 1.0f);
		float whiteness = (float) Math.pow(0.95, power);
		float blueness = (float) Math.pow(0.9995, originallifetime
				* originallifetime);
		return new GBColor(Color.white).Mix(whiteness,
				new GBColor(1, 0.5f - blueness * 1.5f, blueness * 1.5f))
				.multiply(fadeout);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		if (where.getWidth() <= 3) {
			g2d.setPaint(Color());
			g2d.fill(where);
		} else if (hit) {
			g2d.setPaint(Color());
			g2d.fillOval((int) where.getCenterX(), (int) where.getCenterY(),
					where.width, where.height);
		} else {
			g2d.setPaint(Color.gray);
			g2d.drawOval((int) where.getCenterX(), (int) where.getCenterY(),
					where.width, where.height);
		}
		int cx = (int) where.getCenterX();
		int cy = (int) where.getCenterY();
		int thickness = (int) (2 + Math.floor(power / 20));
		FinePoint head = Velocity().unit().multiply(where.getWidth()).divide(2);
		int hx = (int) Math.round(head.x);
		int hy = (int) (-1 * Math.round(head.y));
		FinePoint tail = Velocity().multiply(where.getWidth()).divide(
				radius * 2);
		int tx = (int) Math.round(tail.x);
		int ty = (int) (-1 * Math.round(tail.y));
		g2d.setPaint(Color().multiply(0.7f));
		g2d.setStroke(new BasicStroke(thickness + 2));
		g2d.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
		g2d.setPaint(Color().add(new GBColor(0.2f)));
		g2d.setStroke(new BasicStroke(Math.max(thickness, 2)));
		g2d.drawLine(cx + hx, cy + hy, cx - hx, cy - hy);
		g2d.setPaint(Color());
		g2d.setStroke(new BasicStroke(thickness));
		g2d.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
	}
}
