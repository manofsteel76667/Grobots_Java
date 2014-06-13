package simulation;

import sides.Side;
import support.FinePoint;
import support.GBGraphics;
import exception.GBBadArgumentError;
import exception.GBNilPointerError;

import java.awt.*;

public class GBGrenade extends GBTimedShot {
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

	@Override
	public Color Color() {
		return Color.yellow;
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		if (where.getWidth() <= 3)
			GBGraphics.fillRect(g, where, Color());
		else {
			if (detailed)
				DrawShadow(g, proj, Velocity().multiply(-1.0f), Color.gray);
			GBGraphics.fillOval(g, where, Color());
		}
	}

}