/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Rendering.GBProjection;
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
		g2d.setColor(getOwner().getColor());
		g2d.fillRect(7, 7, 7, 7);
		if (killer != null) {
			g2d.setPaint(killer.getColor());
			g2d.fillRect(8, 8, 5, 5);
		}
	}

	@Override
	public Side getOwner() {
		return type.side;
	}

	@Override
	public void collectStatistics(ScoreKeeper keeper) {
		keeper.reportCorpse(value);
	}

	@Override
	public double getInterest() {
		return value / 500;
	}

	@Override
	public String toString() {
		return "Corpse of " + type.toString();
	}

	@Override
	public String getDetails() {
		return String.format("%.0f energy, killed by %s ", value,
				killer != null ? killer.getName() : "unknown");
	}

	@Override
	public Color getColor() {
		return Color.red;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		drawImage(g, proj);
	}

}
