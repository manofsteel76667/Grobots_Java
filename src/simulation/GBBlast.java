/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBMath;

public class GBBlast extends GBTimedShot {
	boolean hit;
	float whiteness;
	float blueness;
	BufferedImage miniImage;
	BufferedImage hitImage;
	BufferedImage fullImage;

	public GBBlast(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, kBlastRadius, vel, who, howMuch, howLong);
		whiteness = (float) Math.pow(0.95, power);
		blueness = (float) Math
				.pow(0.9995, originallifetime * originallifetime);
		makeImages();
	}
	
	void makeImages() {
		int size = 20;
		fullImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) fullImage.getGraphics();
		int cx = size / 2;
		int cy = size / 2;
		int thickness = (int) (2 + Math.floor(power / 10));
		FinePoint head = Velocity().unit().multiply(size).divide(2);
		int hx = (int) Math.round(head.x);
		int hy = (int) (-1 * Math.round(head.y));
		FinePoint tail = Velocity().multiply(size).divide(radius * 2);
		int tx = (int) Math.round(tail.x);
		int ty = (int) (-1 * Math.round(tail.y));
		g2d.setPaint(GBColor.multiply(Color(), 0.7f));
		g2d.setStroke(new BasicStroke(thickness + 2));
		g2d.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
		g2d.setPaint(GBColor.add(Color(), GBColor.getGreyColor(0.2f)));
		g2d.setStroke(new BasicStroke(Math.max(thickness, 2)));
		g2d.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
		g2d.setPaint(Color());
		g2d.setStroke(new BasicStroke(thickness));
		g2d.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
		miniImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		g2d = (Graphics2D)miniImage.getGraphics();
		g2d.setPaint(Color());
		g2d.fillRect(0, 0, 20, 20);
		hitImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		g2d = (Graphics2D)hitImage.getGraphics();
		g2d.setPaint(Color());
		g2d.fillOval(0, 0, 20, 20);
		//g2d.dispose();
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
			world.addObjectLater(new GBBlasterSpark(Position()));
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
	public Color Color() {
		float fadeout = hit ? 1.0f : Math.min(
				lifetime / Math.min(originallifetime, 10.0f), 1.0f);
		return GBColor.multiply(GBColor.Mix(
				Color.white,
				whiteness,
				new Color(1, GBMath.clamp(0.5f - blueness * 1.5f, 0, 1), GBMath
						.clamp(blueness * 1.5f, 0, 1))), fadeout);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		if (Math.ceil(radius * 2 * proj.getScale()) <= 3) {
			image = miniImage;			
		} else if (hit) {
			image = hitImage;
		} else {
			image = fullImage;
		}
		drawImage(g, proj);
	}
}
