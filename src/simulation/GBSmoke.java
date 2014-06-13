package simulation;

import support.FinePoint;
import support.GBColor;
import support.GBGraphics;

import java.awt.*;

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

	// TODO: after GUI

	@Override
	public GBColor Color() {
		float intensity = 0.8f * lifetime
				/ (lifetime + kSmokeHalfBrightnessTime);
		return new GBColor(intensity);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		GBGraphics.fillOval(g, where, Color());
	}

}