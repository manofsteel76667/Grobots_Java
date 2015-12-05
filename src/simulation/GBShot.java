/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

// GBShot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import java.awt.Color;
import java.awt.Graphics;

import Rendering.GBProjection;
import sides.Side;
import support.FinePoint;

public abstract class GBShot extends GBObject {
	// protected:
	protected Side owner;
	protected double power;

	protected void drawTail(Graphics g, GBProjection proj,
			double movement, Color color) {
	}

	// public:
	public static final double kBlastPushRatio = 0.003;
	public static final double kBlastRadius = 0.1875;
	public static final double kBlastMomentumPerPower = 0.05;

	public static final double kGrenadeRadiusThreshold = 40;
	public static final double kGrenadeLargeRadius = 0.1875;
	public static final double kGrenadeSmallRadius = 0.125;

	public static final int kExplosionLifetime = 2;
	public static final double kExplosionMinEffectiveMass = 0.1;
	public static final double kExplosionPushRatio = 0.01;
	public static final double kExplosionFoodPushRatio = 0.0025;
	public static final double kExplosionRadiusExponent = 0.33;
	public static final double kExplosionRadiusRatio = 0.6;
	public static final double kExplosionDamageMassExponent = 0.5;
	public static final double kLargeExplosionIneffectiveness = 0.5;
	public static final int kExplosionMinSmokes = 1;
	public static final double kExplosionSmokesPerPower = 0.05;
	public static final int kExplosionSmokeLifetimeFactor = 10;

	public static final double kForceFieldRadiusExponent = 0.3;
	public static final double kForceFieldRadiusRatio = 4;
	public static final double kForceFieldPushRatio = 0.03;
	public static final double kMinEffectiveSpeed = 0.05;

	public static final double kSyphonRadius = 0.125;

	// GBShot //

	public GBShot(FinePoint where, double r, Side who, double howMuch) {
		super(where, r);
		owner = who;
		power = howMuch;
	}

	public GBShot(FinePoint where, double r, FinePoint vel, Side who,
			double howMuch) {
		super(where, r, vel);
		owner = who;
		power = howMuch;
	}

	@Override
	public GBObjectClass getObjectClass() {
		return GBObjectClass.ocShot;
	}

	@Override
	public void act(GBWorld world) {

	}

	@Override
	public Side getOwner() {
		return owner;
	}

	public int getShotType() {
		return 0;
	}

	public double getPower() {
		return power;
	}

	@Override
	public String toString() {
		return owner != null ? "Shot from " + owner.getName() : "Shot"
				+ " (power " + power + ", speed " + getSpeed() + ')';
	}

};

abstract class GBTimedShot extends GBShot {
	// protected:
	protected int originallifetime, lifetime;

	// public:
	public GBTimedShot(FinePoint where, double r, Side who, double howMuch,
			int howLong) {
		super(where, r, who, howMuch);
		originallifetime = howLong;
		lifetime = howLong;
	}

	public GBTimedShot(FinePoint where, double r, FinePoint vel, Side who,
			double howMuch, int howLong) {
		super(where, r, vel, who, howMuch);
		originallifetime = howLong;
		lifetime = howLong;
	}

	@Override
	public void act(GBWorld world) {
		super.act(world);
		lifetime--;
	}

	@Override
	public GBObjectClass getObjectClass() {
		if (lifetime > 0)
			return GBObjectClass.ocShot;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public double getInterest() {
		return Math.abs(power) * 10 / (lifetime < 5 ? 5 : lifetime) + 1;
	}

};
