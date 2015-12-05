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

public class GBSmoke extends GBTimedDecoration {
	public static final double kSmokeRadius = 0.4;
	public static final int kSmokeHalfBrightnessTime = 20;

	// public:
	public GBSmoke(FinePoint where, FinePoint vel, int life) {
		super(where, kSmokeRadius, vel, life);
		image = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public String toString() {
		return "Smoke";
	}

	@Override
	public Color getColor() {
		float intensity = 0.8f * lifetime
				/ (lifetime + kSmokeHalfBrightnessTime);
		return new Color(intensity, intensity, intensity, intensity / 10);
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setPaint(getColor());
		g2d.fillOval(0, 0, image.getWidth(), image.getHeight());
		drawImage(g, proj);	
		g2d.dispose();
	}
}