package simulation;

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

	// TODO: after GUI

	@Override
	public GBColor Color() {
		float intensity = 0.8f * lifetime
				/ (lifetime + kSmokeHalfBrightnessTime);
		return new GBColor(intensity);
	}

	/*
	 * void Draw(GBGraphics & g, GBProjection &, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { g.DrawSolidOval(where, Color()); }
		 */
}