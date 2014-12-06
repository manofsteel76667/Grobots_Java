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

	int Lifetime() {
		return (int) Math.floor(Math.max(radius, kSensorShotMinLifetime));
	}

	public GBSensorShot(FinePoint fcs, GBRobot who, GBSensorState st) {
		super(who.Position(), st.MaxRange());
		owner = who;
		side = who.Owner();
		state = st;
		seen = st.Seen();
		focus = fcs;
	}

	@Override
	public void CollideWith(GBObject other) {
		if (other.Class() == GBObjectClass.ocRobot && (GBRobot) other == owner)
			return; // Seeing self is never allowed
		state.Report(new GBSensorResult(other, (other.Position()
				.subtract(focus)).norm())); // most logic is now in SensorState
	}

	@Override
	public void Act(GBWorld world) {
		age++;
	}

	@Override
	public GBObjectClass Class() {
		if (age >= Lifetime())
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
	public Color Color() {
		float fraction = 1.0f - age / Lifetime();
		switch (seen) {
		case ocRobot:
			return new Color(0.4f * fraction, 0.8f * fraction, fraction);
			// return new GBColor(0.4f, 0.8f, 1).multiply(fraction);
		case ocFood:
			return new Color(0.5f * fraction, fraction, 0.5f * fraction);
			// return new GBColor(0.5f, 1, 0.5f).multiply(fraction);
		case ocShot:
			return new Color(fraction, fraction, 0.5f * fraction);
			// return new GBColor(1, 1, 0.5f).multiply(fraction);
		default:
			break;
		}
		return new Color(fraction, fraction, fraction);
	}

	public GBRobot Firer() {
		return owner;
	}

	public GBObjectClass Seen() {
		return age == 0 ? seen : GBObjectClass.ocDead;
	}

	@Override
	public Side Owner() {
		return side;
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		// show focus, owner, and side?
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setColor(Color());
		g2d.setStroke(new BasicStroke(1));
		g2d.drawOval(where.x, where.y, where.width, where.height);
	}
}
