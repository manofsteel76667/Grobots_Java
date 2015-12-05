/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Rendering.GBProjection;
import support.FinePoint;

public class GBBlasterSpark extends GBTimedDecoration {
	public static final int kBlasterSparkLifetime = 8;
	public static final double kBlasterSparkMaxRadius = 0.3125;
	public static final double kBlasterSparkGrowthRate = 0.03125;

	public GBBlasterSpark(FinePoint where) {
		super(where, kBlasterSparkMaxRadius, kBlasterSparkLifetime);
		image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(getColor());
		g2d.drawOval(0, 0, 20, 20);
		g2d.dispose();
	}

	@Override
	public void act(GBWorld world) {
		super.act(world);
		radius = kBlasterSparkMaxRadius - kBlasterSparkGrowthRate
				* (lifetime - 1);
	}

	@Override
	public Color getColor() {
		return Color.white;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		drawImage(g, proj);
	}

}