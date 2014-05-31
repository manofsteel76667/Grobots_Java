package simulation;

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
	// TODO: after GUI
	/*
	 * public static final GBColor Color() { return GBColor::white; }
	 * 
	 * void Draw(GBGraphics & g, GBProjection &, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { g.DrawOpenOval(where, Color()); }
		 */
}