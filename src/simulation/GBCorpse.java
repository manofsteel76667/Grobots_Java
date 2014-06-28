package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

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

	@Override
	public Color Color() {
		return Color.red;
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		if (detailed && where.getWidth() >= 4) {
			g2d.setColor(Owner().Color());
			g2d.drawRect(where.x, where.y, where.width, where.height);
			if (killer != null && where.getWidth() >= 6) {
				Rectangle dot = new Rectangle((int) where.getCenterX() - 1,
						(int) where.getCenterY() - 1, 2, 2);
				g2d.setPaint(killer.Color());
				g2d.fillRect(dot.x, dot.y, dot.width, dot.height);
			}
		}
	}

}