/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import support.FinePoint;

public class GBBlasterSpark extends GBTimedDecoration {
	public static final int kBlasterSparkLifetime = 8;
	public static final double kBlasterSparkMaxRadius = 0.3125;
	public static final double kBlasterSparkGrowthRate = 0.03125;

	// public:
	public GBBlasterSpark(FinePoint where) {
		super(where, kBlasterSparkMaxRadius, kBlasterSparkLifetime);
	}

	@Override
	public void Act(GBWorld world) {
		super.Act(world);
		radius = kBlasterSparkMaxRadius - kBlasterSparkGrowthRate
				* (lifetime - 1);
	}

	@Override
	public Color Color() {
		return Color.white;
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setColor(Color());
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}

}