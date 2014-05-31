package simulation;

import support.FinePoint;
import support.GBObjectClass;

// GBDecorations.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

public class GBTimedDecoration extends GBObject {
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
	public GBObjectClass Class() {
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
	public void Act(GBWorld world) {
		lifetime--;
	}
};
