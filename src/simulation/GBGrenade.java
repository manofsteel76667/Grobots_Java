/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import Rendering.GBProjection;
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
	public void collideWithWall() {
		lifetime = 0;
	}

	@Override
	public void collideWith(GBObject obj) {
		// grenades ignore collisions
	}

	@Override
	public void act(GBWorld world) {
		super.act(world);
		if (lifetime <= 0) {
			world.addObjectLater(new GBExplosion(getPosition(), owner, power));
		}
	}

	@Override
	public int getShotType() {
		return 2;
	}

	@Override
	public Color getColor() {
		return Color.yellow;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double where = getScreenEllipse(proj);
		if (where.getWidth() <= 3) {
			g2d.setPaint(getColor());
			g2d.fill(where.getBounds());
		} else {
			g2d.setPaint(detailed ? GBColor.shadow : getColor());
			g2d.fill(where);// The shadow moves straight
			if (detailed) {
				// The grenade arcs
				int x = lifetime;
				double a = originallifetime / 2;
				drawShadow(
						g,
						proj,
						velocity.add(new FinePoint(0, .1).multiply((2 * x - x
								* x / a)
								* kGrenadeArcFactor)), getColor());
			}
		}
	}

}
