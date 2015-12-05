/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import Rendering.GBProjection;
import sides.Side;
import support.FinePoint;

class GBSensorShot extends GBObject {
	GBRobot owner;
	Side side;
	GBSensorState state;
	GBObjectClass seen;
	int age;
	FinePoint focus;

	// public:

	public static final int kSensorShotMinLifetime = 15;

	int getLifetime() {
		return (int) Math.floor(Math.max(radius, kSensorShotMinLifetime));
	}

	public GBSensorShot(FinePoint fcs, GBRobot who, GBSensorState st) {
		super(who.getPosition(), st.getMaxRange());
		owner = who;
		side = who.getOwner();
		state = st;
		seen = st.getSeen();
		focus = fcs;
	}

	@Override
	public void collideWith(GBObject other) {
		if (other.getObjectClass() == GBObjectClass.ocRobot && (GBRobot) other == owner)
			return; // Seeing self is never allowed
		state.report(new GBSensorResult(other, (other.getPosition()
				.subtract(focus)).norm())); // most logic is now in SensorState
	}

	@Override
	public void act(GBWorld world) {
		age++;
	}

	@Override
	public GBObjectClass getObjectClass() {
		if (age >= getLifetime())
			return GBObjectClass.ocDead;
		else
			return GBObjectClass.ocSensorShot;
	}

	@Override
	public String toString() {
		String classname;
		switch (seen) {
		case ocRobot:
			classname = "Robot";
			break;
		case ocFood:
			classname = "Food";
			break;
		case ocShot:
			classname = "Shot";
			break;
		default:
			classname = "Mystery";
			break;
		}
		return classname + " sensor for " + owner.toString();
	}

	@Override
	public Color getColor() {
		float fraction = 1.0f - age / getLifetime();
		switch (seen) {
		case ocRobot:
			return new Color(0.4f * fraction, 0.8f * fraction, fraction);
		case ocFood:
			return new Color(0.5f * fraction, fraction, 0.5f * fraction);
		case ocShot:
			return new Color(fraction, fraction, 0.5f * fraction);
		default:
			return new Color(fraction, fraction, fraction);
		}
	}

	public GBRobot getFirer() {
		return owner;
	}

	public GBObjectClass getSeen() {
		return age == 0 ? seen : GBObjectClass.ocDead;
	}

	@Override
	public Side getOwner() {
		return side;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		// show focus, owner, and side?
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setColor(getColor());
		g2d.setStroke(new BasicStroke(1));
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}
}
