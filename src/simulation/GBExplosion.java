package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import support.GBGraphics;
import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBObjectClass;
import exception.GBBadArgumentError;
import exception.GBNilPointerError;

public class GBExplosion extends GBTimedShot {
	// public:
	public GBExplosion(FinePoint where, Side who, double howMuch)
			throws GBBadArgumentError {
		super(where, PowerRadius(howMuch), who, howMuch, kExplosionLifetime);
		if (howMuch < 0)
			throw new GBBadArgumentError();
		// TODO: when sound is done
		/*
		 * if ( howMuch > 100 ) StartSound(siLargeExplosion); else if ( howMuch
		 * > 30 ) StartSound(siMediumExplosion); else if ( howMuch > 10 )
		 * StartSound(siSmallExplosion); else StartSound(siTinyExplosion);
		 */
	}

	@Override
	public GBObjectClass Class() {
		if (lifetime > 0)
			return GBObjectClass.ocArea;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public void CollideWith(GBObject other) {
		if (lifetime < kExplosionLifetime)
			return;
		double oMass = Math.max(other.Mass(), kExplosionMinEffectiveMass);
		if (oMass == 0)
			return; // massless objects get 0 damage
		double damage = power
				/ (Math.pow(power / oMass, kExplosionDamageMassExponent)
						* kLargeExplosionIneffectiveness + 1);
		other.TakeDamage(damage * OverlapFraction(other), owner);
		Push(other,
				damage
						* other.OverlapFraction(this)
						* (other.Class() == GBObjectClass.ocFood ? kExplosionFoodPushRatio
								: kExplosionPushRatio));
	}

	@Override
	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (lifetime == 0) {
			int maxLifetime = (int) (Math.floor(Math.sqrt(power)) * kExplosionSmokeLifetimeFactor);
			for (int i = (int) Math.round(Math.max(power
					* kExplosionSmokesPerPower, kExplosionMinSmokes)); i > 0; i--)
				world.AddObjectNew(new GBSmoke(Position().add(
						world.Randoms().Vector(Radius())), world.Randoms()
						.Vector(GBTimedDecoration.kSmokeMaxSpeed), world
						.Randoms().intInRange(
								GBTimedDecoration.kSmokeMinLifetime,
								maxLifetime)));
		}
	}

	@Override
	public GBColor Color() {
		return new GBColor(1, 0.9f, 0.2f);
	}

	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		GBGraphics.fillOval(g, where, Color());
	}

	public static final double PowerRadius(double pwr) {
		return Math.pow(pwr, kExplosionRadiusExponent) * kExplosionRadiusRatio;
	}

}