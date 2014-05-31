package simulation;

// GBShot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBGenericError;
import exception.GBNilPointerError;

public abstract class GBShot extends GBObject {
	// protected:
	protected Side owner;
	protected double power;
	// protected void DrawTail(GBGraphics , GBProjection , double movement,
	// GBColor) ;
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

	public GBObjectClass Class() {
		return GBObjectClass.ocShot;
	}

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {

	}

	public Side Owner() {
		return owner;
	}

	public int Type() {
		return 0;
	}

	public double Power() {
		return power;
	}

	public String Description() {
		return owner != null ? "Shot from " + owner.Name() : "Shot"
				+ " (power " + power + ", speed " + Speed() + ')';
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

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		lifetime--;
	}

	public GBObjectClass Class() {
		if (lifetime > 0)
			return GBObjectClass.ocShot;
		else
			return GBObjectClass.ocDead;
	}

	public double Interest() {
		return Math.abs(power) * 10 / (lifetime < 5 ? 5 : lifetime) + 1;
	}

};

class GBBlast extends GBTimedShot {
	boolean hit;

	// public:
	public GBBlast(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, kBlastRadius, vel, who, howMuch, howLong);
	}

	public void CollideWithWall() {
		lifetime = 0;
		hit = true;
	}

	public void CollideWith(GBObject other) {
		if (!hit && other.Class() == GBObjectClass.ocRobot) {
			other.TakeDamage(power, owner);
			Push(other, power * kBlastPushRatio);
			other.PushBy(Velocity().multiply(power * kBlastMomentumPerPower));
			lifetime = 0;
			hit = true;
		}
	}

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (hit) {
			world.AddObjectNew(new GBBlasterSpark(Position()));
		}
	}

	public int Type() {
		return 1;
	}

	// Blasts fade out at the end of their lives.
	// Big blasts are saturated, smaller ones are pink or white.
	// Long-range blasts are orangish; short-range ones are magenta.
	// TODO: Put this back in when we do the GUI
	/*
	 * public static final GBColor Color() { float fadeout = hit ? 1.0f
	 * :Math.min(lifetime /Math.min((float)originallifetime, 10.0f), 1.0f);
	 * float whiteness = pow(0.95, ToDouble(power)); float blueness =
	 * pow(0.9995, originallifetime * originallifetime); return
	 * GBColor::white.Mix(whiteness, GBColor(1, 0.5f - blueness * 1.5f, blueness
	 * * 1.5f)) * fadeout; }
	 * 
	 * void Draw(GBGraphics & g, GBProjection &, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { if ( where.Width() <= 3 ) { g.DrawSolidRect(where,Color()); }
		 * else if ( hit ) { g.DrawSolidOval(where, Color()); } else {
		 * //g.DrawOpenOval(where, GBColor::gray); short cx = where.CenterX();
		 * short cy = where.CenterY(); int thickness = 2 +Math.floor(power /
		 * 20); FinePoint head = Velocity().Unit() * where.Width() / 2; short hx
		 * =Math.round(head.x); short hy = -Math.round(head.y); FinePoint tail =
		 * Velocity() * where.Width() / (radius * 2); short tx
		 * =Math.round(tail.x); short ty = -Math.round(tail.y); //g.DrawLine(cx
		 * + hx, cy + hy, cx - tx, cy - ty, Color() * 0.7, thickness + 2);
		 * //g.DrawLine(cx + hx, cy + hy, cx - hx, cy - hy, Color() +
		 * GBColor(0.2),Math.max(thickness, 2)); g.DrawLine(cx + hx, cy + hy, cx
		 * - tx, cy - ty, Color(), thickness); } }
		 */

};

class GBGrenade extends GBTimedShot {
	// public:
	public GBGrenade(FinePoint where, FinePoint vel, Side who, double howMuch,
			int howLong) {
		super(where, howMuch >= kGrenadeRadiusThreshold ? kGrenadeLargeRadius
				: kGrenadeSmallRadius, vel, who, howMuch, (howLong <= 0) ? 1
				: howLong);
	}

	public void CollideWithWall() {
		lifetime = 0;
	}

	public void CollideWith(GBObject obj) {
		// grenades ignore collisions
	}

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (lifetime <= 0) {
			world.AddObjectNew(new GBExplosion(Position(), owner, power));
		}
	}

	public int Type() {
		return 2;
	}
	// TODO: Put this back in when the GUI is done
	/*
	 * public GBColor Color() { return GBColor::yellow; }
	 * 
	 * public void Draw(GBGraphics & g, GBProjection & proj, GBRect & where,
	 * boolean detailed) { if (where.Width() <= 3)
	 * g.DrawSolidRect(where,Color()); else { if ( detailed ) DrawShadow(g,
	 * proj, Velocity() * -1.0f, GBColor::gray); g.DrawSolidOval(where,
	 * Color()); } }
	 */
};

class GBExplosion extends GBTimedShot {
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

	public GBObjectClass Class() {
		if (lifetime > 0)
			return GBObjectClass.ocArea;
		else
			return GBObjectClass.ocDead;
	}

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

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		if (lifetime == 0) {
			int maxLifetime = (int) (Math.floor(Math.sqrt(power)) * kExplosionSmokeLifetimeFactor);
			for (int i = (int) Math.round(Math.max(power
					* kExplosionSmokesPerPower, kExplosionMinSmokes)); i > 0; i--)
				world.AddObjectNew(new GBSmoke(Position().add(
						world.Randoms().Vector(Radius())), world.Randoms()
						.Vector(GBSmoke.kSmokeMaxSpeed), world.Randoms()
						.intInRange(GBSmoke.kSmokeMinLifetime, maxLifetime)));
		}
	}

	// TODO: when the GUI is done
	/*
	 * public GBColor Color() { return GBColor(1, 0.9f, 0.2f); }
	 * 
	 * public void Draw(GBGraphics & g, GBProjection &, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { g.DrawSolidOval(where, Color()); }
		 */

	public static final double PowerRadius(double pwr) {
		return Math.pow(pwr, kExplosionRadiusExponent) * kExplosionRadiusRatio;
	}

};

class GBForceField extends GBShot {
	boolean dead;
	double direction;

	// public:
	public GBForceField(FinePoint where, FinePoint vel, Side who, double pwr,
			double dir) {
		super(where, PowerRadius(pwr), vel, who, pwr);
		direction = dir;
	}

	public GBObjectClass Class() {
		return dead ? GBObjectClass.ocDead : GBObjectClass.ocArea;
	}

	public void Move() {
		// don't move! velocity is only apparent.
	}

	public void CollideWith(GBObject other) {
		double force = power / Math.max(other.Speed(), kMinEffectiveSpeed)
				* OverlapFraction(other) * Math.sqrt(other.Mass())
				* kForceFieldPushRatio;
		other.PushBy(force, direction);
	}

	public void Act(GBWorld world) throws GBNilPointerError, GBBadArgumentError {
		super.Act(world);
		dead = true;
	}

	public int Type() {
		return 5;
	}

	public double Interest() {
		return Math.abs(power) * 15 + 1;
	}

	// TODO: when the GUI is done
	/*
	 * public static final GBColor Color() { return GBColor(0, 0.8f, 1); }
	 * 
	 * void Draw(GBGraphics & g, GBProjection &proj, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { short weight = where.Width() >= 20 ? 2 : 1; FinePoint edge =
		 * Position() - Velocity().Unit() * Radius();
		 * g.DrawLine(proj.ToScreenX(edge.x), proj.ToScreenY(edge.y),
		 * proj.ToScreenX(Position().x - Velocity().x),
		 * proj.ToScreenY(Position().y - Velocity().y), Color() * 0.5f, weight);
		 * short cx = (where.right + where.left) / 2; short cy = (where.bottom +
		 * where.top) / 2; g.DrawLine(cx, cy, cx +Math.round(cos(direction) *
		 * where.Width() / 2), cy -Math.round((sin(direction) * where.Height() /
		 * 2)), owner ? owner.Color() : Color()); g.DrawOpenOval(where, Color(),
		 * weight); }
		 * 
		 * void DrawMini(GBGraphics & g, GBRect & where) { g.DrawOpenOval(where,
		 * Color()); }
		 */

	public static final double PowerRadius(double pwr) {
		return Math.pow(Math.abs(pwr), kForceFieldRadiusExponent)
				* kForceFieldRadiusRatio;
	}
};

class GBSyphon extends GBTimedShot {
	GBRobot sink;
	GBSyphonState creator;
	boolean hitsEnemies;

	// public:
	public GBSyphon(FinePoint where, double rate, GBRobot who,
			GBSyphonState state, boolean newHitsEnemies) {
		super(where, kSyphonRadius, where.subtract(who.Position()),
				who.Owner(), rate, 1);
		sink = who;
		creator = state;
		hitsEnemies = newHitsEnemies;
	}

	public void Move() {
		// don't move! velocity is only apparent.
	}

	public void CollideWith(GBObject other) throws GBAbort, GBGenericError,
			GBBadArgumentError {
		if (other.Class() == GBObjectClass.ocRobot && power != 0
				&& other != (GBObject) sink
				&& (hitsEnemies || other.Owner() == sink.Owner())) {
			if (power >= 0) {
				try {
					GBRobot otherBot = (GBRobot) other;
					// FIXME shields affect syphons twice: here and when they're
					// fired
					double efficiency = sink.ShieldFraction()
							* otherBot.ShieldFraction();
					if (other.Owner() != sink.Owner()) {
						if (otherBot.hardware.MaxEnergy() != 0)
							efficiency *= Math.min(1, otherBot.hardware
									.Energy()
									/ otherBot.hardware.MaxEnergy());
					}
					double maxTransfer = Math.min(sink.MaxGiveEnergy(), other
							.MaxTakeEnergy());
					double actual = Math.min(maxTransfer, power * efficiency);

					if (actual > 0) {
						double taken = other.TakeEnergy(actual);
						double given = sink.GiveEnergy(actual);
						power = Math.min(power - actual / efficiency, 0);
						creator.ReportUse(taken);
						if (taken > 0 && other.Owner() != owner) {
							owner.ReportKleptotrophy(taken);
							other.Owner().Scores().expenditure
									.ReportStolen(taken);
						}
						if (taken != given || taken != actual) {
							throw new GBGenericError(
									"Given != taken in CollideWith");
						}
					}
				} catch (GBError e) {
					GBError.NonfatalError("Error in CollideWith: "
							+ e.getMessage());
				}
			} else { // giving energy: like taking, but target energy isn't a
						// factor
				GBRobot otherBot = (GBRobot) other;
				double efficiency = sink.ShieldFraction()
						* otherBot.ShieldFraction();
				// efficiency does not decrease with low energy when giving
				double maxTransfer = Math.min(other.MaxGiveEnergy(), sink
						.MaxTakeEnergy());
				double actual = Math.min(maxTransfer, -power * efficiency);

				double taken = sink.TakeEnergy(actual);
				double given = other.GiveEnergy(actual);
				power = Math.max(power + actual / efficiency, 0);

				creator.ReportUse(-given);

				if (given > 0 && other.Owner() != owner) { // giving energy to
															// enemy :)
					other.Owner().ReportKleptotrophy(given);
					owner.Scores().expenditure.ReportStolen(given);
				}
				if (taken != given || taken != actual) {
					throw new GBGenericError("Given != taken in CollideWith");
				}
			}
		}
	}

	public int Type() {
		return hitsEnemies ? 4 : 3;
	}

	public double Interest() {
		return Math.abs(power) * (hitsEnemies ? 5 : 1) + (hitsEnemies ? 2 : 1);
	}
	// TODO: after GUI is done
	/*
	 * public GBColor Color() { return (hitsEnemies ? GBColor(0.6f, 1, 0) :
	 * GBColor(0.5f, 0.8f, 1)); }
	 * 
	 * public void Draw(GBGraphics & g, GBProjection & proj, GBRect & where,
	 * boolean detailed) { //draw tail showing origin, rate and whether it's
	 * working if ( detailed ) { GBColor tailcolor = Owner().Color() *
	 * (creator.Syphoned() ? 0.8f : 0.4f); FinePoint unit = - Velocity().Unit();
	 * double phase = Milliseconds() / 1000.0 * ToDouble(creator.Rate()); for
	 * (double d = Speed() + (phase -Math.floor(phase)) - 1 - sink.Radius(); d
	 * >= Radius(); d -= 1 ) { short x = proj.ToScreenX(Position().x + unit.x *
	 * d); short y = proj.ToScreenY(Position().y + unit.y * d); GBRect r(x - 1,
	 * y - 1, x + 1, y + 1); g.DrawSolidRect(r, tailcolor); } } //draw the
	 * syphon g.DrawLine(where.left, where.top, where.right, where.bottom,
	 * Color()); g.DrawLine(where.right, where.top, where.left, where.bottom,
	 * Color()); }
	 */

};