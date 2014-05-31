package simulation;

import sides.RobotType;
import sides.Side;
import support.FinePoint;
import exception.GBBadArgumentError;

public class GBCorpse extends GBFood {
	RobotType type;
	Side killer;

	// public:
	public GBCorpse(FinePoint where, FinePoint vel, double val, RobotType who,
			Side cause) throws GBBadArgumentError {
		super(where, vel, val);
		type = who;
		killer = cause;
	}
	@Override
	public Side Owner() {
		return type.side;
	}

	@Override
	public void CollectStatistics(GBWorld world) {
		world.ReportCorpse(value);
	}
	@Override
	public double Interest() {
		return value / 500;
	}
	@Override
	public String toString() {
		return "Corpse of " + type.Description();
	}

	@Override
	public String Details() {
		return value + " energy, killed by " + killer != null ? killer.Name()
				: "accident";
	}
	// TODO: GUI
	/*
	 * public static final GBColor Color() { return GBColor::red; }
	 * 
	 * void Draw(GBGraphics & g, GBProjection & proj, GBRect & where, boolean
	 * detailed) { Draw(g, proj, where, detailed); if ( detailed &&
	 * where.Width() >= 4 ) { g.DrawOpenRect(where, Owner().Color()); if (
	 * killer && where.Width() >= 6 ) { GBRect dot(where.CenterX() - 1,
	 * where.CenterY() - 1, where.CenterX() + 1, where.CenterY() + 1);
	 * g.DrawSolidRect(dot, killer.Color()); } } }
	 */

}