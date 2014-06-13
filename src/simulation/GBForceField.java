package simulation;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBGraphics;
import support.GBObjectClass;
import exception.GBBadArgumentError;
import exception.GBNilPointerError;

public class GBForceField extends GBShot {
	boolean dead;
	double direction;

	// public:
	public GBForceField(FinePoint where, FinePoint vel, Side who, double pwr,
			double dir) {
		super(where, PowerRadius(pwr), vel, who, pwr);
		direction = dir;
	}

	@Override
	public GBObjectClass Class() {
		return dead ? GBObjectClass.ocDead : GBObjectClass.ocArea;
	}

	@Override
	public void Move() {
		// don't move! velocity is only apparent.
	}

	@Override
	public void CollideWith(GBObject other) {
		double force = power / Math.max(other.Speed(), kMinEffectiveSpeed)
				* OverlapFraction(other) * Math.sqrt(other.Mass())
				* kForceFieldPushRatio;
		other.PushBy(force, direction);
	}

	@Override
	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		dead = true;
	}

	@Override
	public int Type() {
		return 5;
	}

	@Override
	public double Interest() {
		return Math.abs(power) * 15 + 1;
	}

	// TODO: when the GUI is done

	@Override
	public GBColor Color() {
		return new GBColor(0, 0.8f, 1);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed

	) {
		int weight = where.getWidth() >= 20 ? 2 : 1;
		FinePoint edge = Position().subtract(
				Velocity().unit().multiply(Radius()));
		//From robot to target
		((Graphics2D)g).setStroke(new BasicStroke(weight));
		GBGraphics.drawLine(g, proj.ToScreenX(edge.x), proj.ToScreenY(edge.y),
				proj.ToScreenX(Position().x - Velocity().x),
				proj.ToScreenY(Position().y - Velocity().y),
				Color().multiply(0.5f));
		int cx = ((int) where.getWidth()) / 2;
		int cy = ((int) where.getHeight()) / 2;
		//From destination into the direction being pushed
		((Graphics2D)g).setStroke(new BasicStroke(1));
		GBGraphics.drawLine(g, cx, cy,  cx + Math.round(Math
				.cos(direction) * where.getWidth() / 2),  (cy - Math
				.round((Math.sin(direction) * where.getHeight() / 2))),
				owner != null ? owner.Color() : Color());
		//Force field radius
		GBGraphics.drawOval(g, where, Color());
	}

	@Override
	public void DrawMini(Graphics g, Rectangle where) {
		GBGraphics.drawOval(g, where, Color());
	}

	public static final double PowerRadius(double pwr) {
		return Math.pow(Math.abs(pwr), kForceFieldRadiusExponent)
				* kForceFieldRadiusRatio;
	}
}