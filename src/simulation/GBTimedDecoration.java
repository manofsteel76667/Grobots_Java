/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;

import support.FinePoint;

public abstract class GBTimedDecoration extends GBObject {
	// convenience constants
	public static final double kSmokeMaxSpeed = 0.03;
	public static final int kSmokeMinLifetime = 30;
	public static final int kSmokeMaxLifetime = 120;
	// protected:
	int lifetime;

	// public:
	public GBTimedDecoration(FinePoint where, double r, int howLong) {
		super(where, r);
		lifetime = howLong;
	}

	public GBTimedDecoration(FinePoint where, double r, FinePoint vel,
			int howLong) {
		super(where, r, vel);
		lifetime = howLong;
	}

	@Override
	public GBObjectClass getObjectClass() {
		if (lifetime > 0)
			return GBObjectClass.ocDecoration;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public String toString() {
		return "Decoration";
	}

	@Override
	public void act(GBWorld world) {
		lifetime--;
	}

	@Override
	public abstract Color getColor();
};
