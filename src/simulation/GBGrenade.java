/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import sides.Side;
import support.FinePoint;
import support.GBColor;

public class GBGrenade extends GBTimedShot {
	static final double kGrenadeArcFactor = 3;

	public GBGrenade(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, howMuch >= kGrenadeRadiusThreshold ? kGrenadeLargeRadius
				: kGrenadeSmallRadius, vel, who, howMuch, (howLong <= 0) ? 1
				: howLong);
	}

	@Override
	public void CollideWithWall() {
		lifetime = 0;
	}

	@Override
	public void CollideWith(GBObject obj) {
		// grenades ignore collisions
	}

	@Override
	public void Act(GBWorld world) {
		super.Act(world);
		if (lifetime <= 0) {
			world.addObjectLater(new GBExplosion(Position(), owner, power));
		}
	}

	@Override
	public int Type() {
		return 2;
	}

	@Override
	public Color Color() {
		return Color.yellow;
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double where = getScreenEllipse(proj);
		if (where.getWidth() <= 3) {
			g2d.setPaint(Color());
			g2d.fill(where.getBounds());
		} else {
			g2d.setPaint(detailed ? GBColor.shadow : Color());
			g2d.fill(where);// The shadow moves straight
			if (detailed) {
				// The grenade arcs
				int x = lifetime;
				double a = originallifetime / 2;
				DrawShadow(
						g,
						proj,
						velocity.add(new FinePoint(0, .1).multiply((2 * x - x
								* x / a)
								* kGrenadeArcFactor)), Color());
			}
		}
	}

}
