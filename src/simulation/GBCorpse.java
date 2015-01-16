/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(Owner().Color());
		g2d.fillRect(7, 7, 7, 7);
		if (killer != null) {
			g2d.setPaint(killer.Color());
			g2d.fillRect(8, 8, 5, 5);
		}
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
		return String.format("%.0f energy, killed by %s ", value,
				killer != null ? killer.Name() : "unknown");
	}

	@Override
	public Color Color() {
		return Color.red;
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		drawImage(g, proj);
	}

}
