/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import sides.Side;
import support.FinePoint;

public class GBGrenade extends GBTimedShot {
	// public:
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
			world.AddObjectNew(new GBExplosion(Position(), owner, power));
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
		Rectangle where = getScreenRect(proj);
		g2d.setPaint(Color());
		if (where.getWidth() <= 3)
			g2d.fillRect(where.x, where.y, where.width, where.height);
		else {
			if (detailed)
				DrawShadow(g, proj, Velocity().multiply(-1.0f), Color.gray);
			g2d.fillOval(where.x, where.y, where.width, where.height);
		}
	}

}
