/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import sides.RobotType;
import sides.Side;
import support.FinePoint;

public class GBCorpse extends GBFood {
	RobotType type;
	Side killer;

	// public:
	public GBCorpse(FinePoint where, FinePoint vel, double val, RobotType who,
			Side cause) {
		super(where, vel, val);
		type = who;
		killer = cause;
	}

	@Override
	public Side Owner() {
		return type.side;
	}

	@Override
	public void CollectStatistics(ScoreKeeper keeper) {
		keeper.ReportCorpse(value);
	}

	@Override
	public double Interest() {
		return value / 500;
	}

	@Override
	public String toString() {
		return "Corpse of " + type.toString();
	}

	@Override
	public String Details() {
		return String.format("%.0f energy, killed by %s ", value, killer != null ? killer.Name() : "unknown");
	}

	@Override
	public Color Color() {
		return Color.red;
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		super.Draw(g, proj, detailed);
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
