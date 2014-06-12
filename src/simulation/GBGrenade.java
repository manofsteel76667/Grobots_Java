package simulation;

import sides.Side;
import support.FinePoint;
import support.GBColor;
import exception.GBBadArgumentError;
import exception.GBNilPointerError;

public class GBGrenade extends GBTimedShot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (lifetime <= 0) {
			world.AddObjectNew(new GBExplosion(Position(), owner, power));
		}
	}

	@Override
	public int Type() {
		return 2;
	}

	// TODO: Put this back in when the GUI is done

	@Override
	public GBColor Color() {
		return GBColor.yellow;
	}

	/*
	 * public void Draw(GBGraphics & g, GBProjection & proj, GBRect & where,
	 * boolean detailed) { if (where.Width() <= 3)
	 * g.DrawSolidRect(where,Color()); else { if ( detailed ) DrawShadow(g,
	 * proj, Velocity() * -1.0f, GBColor::gray); g.DrawSolidOval(where,
	 * Color()); } }
	 */

}