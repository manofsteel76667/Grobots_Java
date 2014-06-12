package simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBObjectClass;
import exception.GBBadArgumentError;
import exception.GBNilPointerError;

public class GBBlast extends GBTimedShot {
	boolean hit;

	// public:
	public GBBlast(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, kBlastRadius, vel, who, howMuch, howLong);
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
	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (hit) {
			world.AddObjectNew(new GBBlasterSpark(Position()));
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
	public GBColor Color() {
		float fadeout = hit ? 1.0f : Math.min(
				lifetime / Math.min(originallifetime, 10.0f), 1.0f);
		float whiteness = (float) Math.pow(0.95, power);
		float blueness = (float) Math.pow(0.9995, originallifetime
				* originallifetime);
		return new GBColor(Color.white).Mix(whiteness,
				new GBColor(1, 0.5f - blueness * 1.5f, blueness * 1.5f))
				.multiply(fadeout);
	}

	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		Graphics2D g2 = (Graphics2D) g;
		if (where.getWidth() <= 3) {
			g2.setPaint(Color());
			g2.fill(where);
		} else if (hit) {
			g2.setPaint(Color());
			g2.fillOval((int) where.getCenterX(), (int) where.getCenterY(),
					(int) where.width, (int) where.height);
		} else {
			g2.setPaint(Color.gray);
			g2.drawOval((int) where.getCenterX(), (int) where.getCenterY(),
					(int) where.width, (int) where.height);
		}
		int cx = (int) where.getCenterX();
		int cy = (int) where.getCenterY();
		int thickness = (int) (2 + Math.floor(power / 20));
		FinePoint head = Velocity().unit().multiply(where.getWidth()).divide(2);
		int hx = (int) Math.round(head.x);
		int hy = (int) (-1 * Math.round(head.y));
		FinePoint tail = Velocity().multiply(where.getWidth()).divide(
				radius * 2);
		int tx = (int) Math.round(tail.x);
		int ty = (int) (-1 * Math.round(tail.y));
		g2.setPaint(Color().multiply(0.7f));
		g2.setStroke(new BasicStroke(thickness + 2));
		g2.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
		g2.setPaint(Color().add(new GBColor(0.2f)));
		g2.setStroke(new BasicStroke(Math.max(thickness, 2)));
		g2.drawLine(cx + hx, cy + hy, cx - hx, cy - hy);
		g2.setPaint(Color());
		g2.setStroke(new BasicStroke(thickness));
		g2.drawLine(cx + hx, cy + hy, cx - tx, cy - ty);
	}

	@Override
	public void DrawUnderlay(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {

	}

	@Override
	public void DrawOverlay(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {

	}
}
