package simulation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import support.FinePoint;
import support.GBColor;

public class GBSmoke extends GBTimedDecoration {
	public static final double kSmokeRadius = 0.4;
	public static final int kSmokeHalfBrightnessTime = 20;

	// public:
	public GBSmoke(FinePoint where, FinePoint vel, int life) {
		super(where, kSmokeRadius, vel, life);
	}

	@Override
	public String toString() {
		return "Smoke";
	}

	@Override
	public GBColor Color() {
		float intensity = 0.8f * lifetime
				/ (lifetime + kSmokeHalfBrightnessTime);
		return new GBColor(intensity);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setPaint(Color());
		g2d.fillOval(where.x, where.y, where.width, where.height);
	}

}