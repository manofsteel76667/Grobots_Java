/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Rendering.GBProjection;
import sides.Side;
import sound.SoundManager;
import support.FinePoint;
import exception.GBSimulationError;

public class GBExplosion extends GBTimedShot {
	Color color = new Color(1, 0.9f, 0.2f);

	public GBExplosion(FinePoint where, Side who, double howMuch) {
		super(where, PowerRadius(howMuch), who, howMuch, kExplosionLifetime);
		if (howMuch < 0)
			throw new GBSimulationError("negative explosion");
		if (howMuch > 100)
			SoundManager.playSound(SoundManager.SoundType.stBigExplosion,
					position);
		else if (howMuch > 30)
			SoundManager
					.playSound(SoundManager.SoundType.stExplosion, position);
		else if (howMuch > 10)
			SoundManager.playSound(SoundManager.SoundType.stSmallExplosion,
					position);
		else
			SoundManager.playSound(SoundManager.SoundType.stTinyExplosion,
					position);
		image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(getColor());
		g2d.fillOval(0, 0, 20, 20);
		g2d.dispose();
	}

	@Override
	public GBObjectClass getObjectClass() {
		if (lifetime > 0)
			return GBObjectClass.ocArea;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public void collideWith(GBObject other) {
		if (lifetime < kExplosionLifetime)
			return;
		double oMass = Math.max(other.getMass(), kExplosionMinEffectiveMass);
		if (oMass == 0)
			return; // massless objects get 0 damage
		double damage = power
				/ (Math.pow(power / oMass, kExplosionDamageMassExponent)
						* kLargeExplosionIneffectiveness + 1);
		other.takeDamage(damage * overlapFraction(other), owner);
		push(other,
				damage
						* other.overlapFraction(this)
						* (other.getObjectClass() == GBObjectClass.ocFood ? kExplosionFoodPushRatio
								: kExplosionPushRatio));
	}

	@Override
	public void act(GBWorld world) {
		super.act(world);
		if (lifetime == 0) {
			int maxLifetime = (int) (Math.floor(Math.sqrt(power)) * kExplosionSmokeLifetimeFactor);
			for (int i = (int) Math.round(Math.max(power
					* kExplosionSmokesPerPower, kExplosionMinSmokes)); i > 0; i--)
				world.addObjectLater(new GBSmoke(getPosition().add(
						world.random.Vector(getRadius())), world.random
						.Vector(GBTimedDecoration.kSmokeMaxSpeed), world.random
						.intInRange(GBTimedDecoration.kSmokeMinLifetime,
								maxLifetime)));
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		// FIXME: The scaling and interpolation make a nice fuzzy explosion with
		// jagged edges, but why does it always cut off the right and bottom edges?
		drawImage(g, proj);
	}

	public static final double PowerRadius(double pwr) {
		return Math.pow(pwr, kExplosionRadiusExponent) * kExplosionRadiusRatio;
	}

}
